import axios from 'axios';
import { getIntegrationBaseUrl } from 'symphony-integration-commons';
import BaseService from './BaseService';

const unexpectedErrorDialog = require('../templates/unexpectedErrorDialog.hbs');
const forbiddenDialog = require('../templates/forbiddenDialog.hbs');
const notFoundDialog = require('../templates/issueNotFoundDialog.hbs');
const errorDialog = require('../templates/errorDialog.hbs');
const commentDialog = require('../templates/commentDialog.hbs');

export default class CommentService extends BaseService {
  constructor(serviceName) {
    super(serviceName);
    this.baseUrl = getIntegrationBaseUrl();
    this.comment = '';
  }

  openCommentDialog(data, service) {
    const template = commentDialog();

    const commentData = {
      comment: {
        service: service.serviceName,
      },
      commentIssue: {
        service: service.serviceName,
        label: 'OK',
        data: {
          entity: data.entity,
          type: 'commentIssue',
        },
      },
      closeCommentDialog: {
        service: service.serviceName,
        label: 'Cancel',
        data: {
          entity: data.entity,
          type: 'closeCommentDialog',
        },
      },
    };

    service.openDialog('commentIssue', template, commentData);
  }

  commentIssue(url, issueKey) {
    const apiUrl = `${this.baseUrl}/v1/jira/rest/api/issue/${issueKey}/comment`;

    return axios({
      method: 'post',
      url: apiUrl,
      headers: { Authorization: `Bearer ${this.jwt}` },
      params: {
        url,
      },
      data: {
        body: this.comment,
      },
    }).catch(error => this.rejectPromise(error));
  }

  save(data) {
    const baseUrl = data.entity.baseUrl;
    const issueKey = data.entity.issue.key;

    this.commentIssue(baseUrl, issueKey)
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
