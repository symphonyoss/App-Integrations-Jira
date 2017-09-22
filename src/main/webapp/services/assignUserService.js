import BaseService from './baseService';
import { searchAssignableUser, assignUser } from '../api/apiCalls';
import actionFactory from '../utils/actionFactory';
import DialogBuilder from '../templates/builders/dialogBuilder';

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
    const assignTemplate = assignDialog();

    const dialogBuilder = new DialogBuilder('Assign', assignTemplate);
    const template = dialogBuilder.build(data);

    const assignIssueAction = {
      type: 'assignUserService',
      subtype: 'performDialogAction',
      label: 'ASSIGN',
    };
    const closeDialogAction = {
      type: 'assignUserService',
      subtype: 'closeDialog',
      label: 'Cancel',
    };

    const actions = actionFactory(
      [assignIssueAction, closeDialogAction],
      service.serviceName,
      data.entity
    );

    const userData = Object.assign({
      user: {
        service: service.serviceName,
        crossPod: 'NONE',
      },
    }, actions);

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
    switch (data.subtype) {
      case 'openDialog': {
        this.showDialog(data, this.openAssignDialog);
        break;
      }
      case 'performDialogAction': {
        this.save(data);
        break;
      }
      case 'closeDialog': {
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
