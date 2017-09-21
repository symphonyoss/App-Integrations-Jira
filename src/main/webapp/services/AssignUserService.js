import BaseService from './BaseService';
import { searchAssignableUser, assignUser } from '../api/apiCalls';

const assignDialog = require('../templates/assignDialog.hbs');
const errorDialog = require('../templates/errorDialog.hbs');
const unexpectedErrorDialog = require('../templates/unexpectedErrorDialog.hbs');
const forbiddenDialog = require('../templates/forbiddenDialog.hbs');
const notFoundDialog = require('../templates/issueNotFoundDialog.hbs');
const successDialog = require('../templates/successDialog.hbs');

export default class AssignUserService extends BaseService {
  constructor(serviceName) {
    super(serviceName);
    this.selectedUser = {};
  }

  successDialog(issueKey) {
    this.closeDialog('assignIssue');

    const template = successDialog({
      issueKey,
      prettyName: this.selectedUser.prettyName,
    });

    this.openDialog('userAssigned', template, {});
  }

  openAssignDialog(data, service) {
    const template = assignDialog({
      url: data.entity.issue.url,
      key: data.entity.issue.key,
      name: data.entity.issue.assignee.displayName,
    });

    const userData = {
      user: {
        service: service.serviceName,
        crossPod: 'NONE',
      },
      assignIssue: {
        service: service.serviceName,
        label: 'OK',
        data: {
          entity: data.entity,
          type: 'assignIssue',
        },
      },
      closeAssignDialog: {
        service: service.serviceName,
        label: 'Cancel',
        data: {
          entity: data.entity,
          type: 'closeAssignDialog',
        },
      },
    };

    service.openDialog('assignIssue', template, userData);
  }

  save(data) {
    const baseUrl = data.entity.baseUrl;
    const issueKey = data.entity.issue.key;

    searchAssignableUser(baseUrl, issueKey, this.selectedUser, this.jwt)
      .then((users) => {
        if (users.data.length === 0) {
          return Promise.reject(new Error(401));
        }

        return assignUser(baseUrl, issueKey, users.data[0].name, this.jwt);
      })
      .then(() => this.successDialog(issueKey))
      .catch((error) => {
        switch (error.message) {
          case '401': {
            this.openDialog('forbiddenDialog', forbiddenDialog({ username: this.selectedUser.prettyName }), {});
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
      case 'assignDialog': {
        this.showDialog(data, this.openAssignDialog);
        break;
      }
      case 'assignIssue': {
        this.save(data);
        break;
      }
      case 'closeAssignDialog': {
        this.closeDialog('assignIssue');
        break;
      }
      default: {
        this.openDialog('error', errorDialog(), {});
        break;
      }
    }
  }

  selected(user) {
    this.selectedUser = user;
  }
}
