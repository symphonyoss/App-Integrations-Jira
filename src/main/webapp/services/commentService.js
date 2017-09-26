import BaseService from './baseService';
import { commentIssue } from '../api/apiCalls';
import actionFactory from '../utils/actionFactory';
import DialogBuilder from '../templates/builders/dialogBuilder';

const commentDialog = require('../templates/commentDialog.hbs');
const successDialog = require('../templates/commentCreatedDialog.hbs');

export default class CommentService extends BaseService {
  constructor(serviceName) {
    super(serviceName);
    this.comment = '';
  }

  successDialog(data) {
    const content = successDialog();

    const dialogBuilder = new DialogBuilder('Comment on', content);
    dialogBuilder.footer(false);

    const template = dialogBuilder.build(data);
    this.updateDialog('commentIssue', template, {});
  }

  retrieveTemplate(dialogBuilder, data, serviceName) {
    const template = dialogBuilder.build(data);

    const commentIssueAction = {
      service: 'commentService',
      type: 'performDialogAction',
      label: 'COMMENT',
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
    const commentTemplate = commentDialog();
    const dialogBuilder = new DialogBuilder('Comment on', commentTemplate);

    const template = service.retrieveTemplate(dialogBuilder, data, service.serviceName);
    service.openDialog('commentIssue', template.layout, template.data);
  }

  save(data) {
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
            errorMessage = 'Unexpected error to perform this action, please try to reload this page ' +
                'or contact the administrator.';
            break;
          }
        }

        const commentTemplate = commentDialog();

        const dialogBuilder = new DialogBuilder('Comment on', commentTemplate);
        dialogBuilder.error(errorMessage);

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
