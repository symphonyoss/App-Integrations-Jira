import { getIntegrationBaseUrl } from 'symphony-integration-commons';
import BaseService from './baseService';
import { commentIssue, searchIssue } from '../api/apiCalls';
import actionFactory from '../utils/actionFactory';
import DialogBuilder from '../templates/builders/dialogBuilder';

const commentDialog = require('../templates/commentDialog.hbs');
const successDialog = require('../templates/commentCreatedDialog.hbs');

const baseUrl = getIntegrationBaseUrl();

export default class CommentService extends BaseService {
  constructor(serviceName) {
    super(serviceName);
    this.comment = '';
  }

  successDialog(data) {
    const image = `${baseUrl}/apps/jira/img/icon-checkmark-green.svg`;
    const content = successDialog({ successImg: image });

    const dialogBuilder = new DialogBuilder('Comment on', content);
    dialogBuilder.footer(false);

    const template = dialogBuilder.build(data);
    this.updateDialog('commentIssue', template, {});
    setTimeout(() => this.closeDialog('commentIssue'), 3000);
  }

  retrieveTemplate(dialogBuilder, data, serviceName, commentLabel = 'COMMENT') {
    const template = dialogBuilder.build(data);

    const commentIssueAction = {
      service: 'commentService',
      type: 'performDialogAction',
      label: commentLabel,
    };
    const closeDialogAction = {
      service: 'commentService',
      type: 'closeDialog',
      label: 'Cancel',
    };

    const actions = actionFactory(
        [commentIssueAction, closeDialogAction],
        serviceName,
        data.entity
    );

    const commentData = Object.assign({
      userComment: {
        service: serviceName,
      },
    }, actions);

    return {
      layout: template,
      data: commentData,
    };
  }

  openActionDialog(data, service) {
    const baseUrl = data.entity.baseUrl;
    const issueKey = data.entity.issue.key;

    const commentTemplate = commentDialog();
    const dialogBuilder = new DialogBuilder('Comment on', commentTemplate);
    let template = null;

    service.comment = '';

    searchIssue(baseUrl, issueKey, service.jwt)
      .then(() => {
        template = service.retrieveTemplate(dialogBuilder, data, service.serviceName);
        service.openDialog('commentIssue', template.layout, template.data);
      })
      .catch(() => {
        dialogBuilder.headerError('Issue not found');
        dialogBuilder.disableButtons(true);
        template = service.retrieveTemplate(dialogBuilder, data, service.serviceName);
        service.openDialog('commentIssue', template.layout, template.data);
      });
  }

  save(data) {
    const commentTemplate = commentDialog();
    const dialogBuilder = new DialogBuilder('Comment on', commentTemplate);

    if (this.comment === '') {
      dialogBuilder.error('Invalid comment');

      const template = this.retrieveTemplate(dialogBuilder, data, this.serviceName);
      this.updateDialog('commentIssue', template.layout, template.data);
    } else {
      dialogBuilder.disableButtons(true);

      const savedComment = this.comment;
      this.comment = savedComment;
      this.performAssignUserAction(data);
    }
  }

  performAssignUserAction(data) {
    const baseUrl = data.entity.baseUrl;
    const issueKey = data.entity.issue.key;

    commentIssue(baseUrl, issueKey, this.comment, this.jwt)
      .then(() => this.successDialog(data))
      .catch((error) => {
        let errorMessage;

        switch (error.message) {
          case '400': {
            errorMessage = 'Invalid comment';
            break;
          }
          case '401': {
            errorMessage = 'Current user is not authorized to perform this action';
            break;
          }
          case '404': {
            errorMessage = `Issue ${issueKey} not found`;
            break;
          }
          default: {
            errorMessage = 'Comment not saved due to a network error. Please try again.';
            break;
          }
        }

        this.comment = '';

        const commentTemplate = commentDialog();

        const dialogBuilder = new DialogBuilder('Comment on', commentTemplate);
        dialogBuilder.headerError(errorMessage);

        const template = this.retrieveTemplate(dialogBuilder, data, this.serviceName);
        this.updateDialog('commentIssue', template.layout, template.data);
      });
  }

  closeActionDialog() {
    this.closeDialog('commentIssue');
  }

  changed(comment) {
    this.comment = comment;
  }
}
