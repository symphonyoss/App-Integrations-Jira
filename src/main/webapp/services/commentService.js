import BaseService from './baseService';
import { commentIssue } from '../api/apiCalls';
import actionFactory from '../utils/actionFactory';

const unexpectedErrorDialog = require('../templates/unexpectedErrorDialog.hbs');
const forbiddenDialog = require('../templates/forbiddenDialog.hbs');
const notFoundDialog = require('../templates/issueNotFoundDialog.hbs');
const errorDialog = require('../templates/errorDialog.hbs');
const commentDialog = require('../templates/commentDialog.hbs');

export default class CommentService extends BaseService {
  constructor(serviceName) {
    super(serviceName);
    this.comment = '';
  }

  openCommentDialog(data, service) {
    const template = commentDialog();

    const commentIssueAction = { type: 'commentIssue', label: 'COMMENT' };
    const closeDialogAction = { type: 'closeCommentDialog', label: 'Cancel' };

    const actions = actionFactory([commentIssueAction, closeDialogAction], service.serviceName, data.entity);

    const commentData = Object.assign({
      comment: {
        service: service.serviceName,
      },
    }, actions);

    service.openDialog('commentIssue', template, commentData);
  }

  save(data) {
    const baseUrl = data.entity.baseUrl;
    const issueKey = data.entity.issue.key;

    commentIssue(baseUrl, issueKey, this.comment, this.jwt)
      .then(() => this.closeDialog('commentIssue'))
      .catch((error) => {
        switch (error.message) {
          case '401': {
            this.openDialog('forbiddenDialog', forbiddenDialog({ username: '' }), {});
            break;
          }
          case '404': {
            this.openDialog('issueNotFoundDialog', notFoundDialog({ issueKey }), {});
            break;
          }
          default: {
            this.openDialog('unexpectedErrorDialog', unexpectedErrorDialog(), {});
            break;
          }
        }
      });
  }

  action(data) {
    switch (data.type) {
      case 'commentDialog': {
        this.showDialog(data, this.openCommentDialog);
        break;
      }
      case 'commentIssue': {
        this.save(data);
        break;
      }
      case 'closeCommentDialog': {
        this.closeDialog('commentIssue');
        break;
      }
      default: {
        this.openDialog('error', errorDialog(), {});
        break;
      }
    }
  }

  changed(comment) {
    this.comment = comment;
  }
}
