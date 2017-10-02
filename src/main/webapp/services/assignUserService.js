import { getIntegrationBaseUrl } from 'symphony-integration-commons';
import BaseService from './baseService';
import { searchAssignableUser, assignUser } from '../api/apiCalls';
import actionFactory from '../utils/actionFactory';
import DialogBuilder from '../templates/builders/dialogBuilder';

const assignDialog = require('../templates/assignDialog.hbs');
const successDialog = require('../templates/userAssignedDialog.hbs');

const baseUrl = getIntegrationBaseUrl();

export default class AssignUserService extends BaseService {
  constructor(serviceName) {
    super(serviceName);
    this.selectedUser = {};
  }

  successDialog(data) {
    const image = `${baseUrl}/apps/jira/img/icon-checkmark-green.svg`;
    const content = successDialog({ successImg: image });

    const dialogBuilder = new DialogBuilder('Assign', content);
    dialogBuilder.footer(false);

    const template = dialogBuilder.build(data);

    const userData = {
      user: {
        service: this.serviceName,
        successMessage: 'Assigned',
        selected: this.selectedUser,
      },
    };

    this.updateDialog('assignIssue', template, userData);
  }

  retrieveTemplate(dialogBuilder, data, serviceName) {
    const template = dialogBuilder.build(data);

    const assignIssueAction = {
      service: 'assignUserService',
      type: 'performDialogAction',
      label: 'ASSIGN',
    };
    const closeDialogAction = {
      service: 'assignUserService',
      type: 'closeDialog',
      label: 'Cancel',
    };

    const actions = actionFactory(
        [assignIssueAction, closeDialogAction],
        serviceName,
        data.entity
    );

    const userData = Object.assign({
      user: {
        service: serviceName,
        crossPod: 'NONE',
      },
    }, actions);

    return {
      layout: template,
      data: userData,
    };
  }

  openActionDialog(data, service) {
    service.selectedUser = {};

    const assignTemplate = assignDialog();
    const dialogBuilder = new DialogBuilder('Assign', assignTemplate);

    const template = service.retrieveTemplate(dialogBuilder, data, service.serviceName);
    service.openDialog('assignIssue', template.layout, template.data);
  }

  save(data) {
    if (this.selectedUser.email === undefined) {
      const assignTemplate = assignDialog();

      const dialogBuilder = new DialogBuilder('Assign', assignTemplate);
      dialogBuilder.error('Please select an user');

      const template = this.retrieveTemplate(dialogBuilder, data, this.serviceName);
      this.updateDialog('assignIssue', template.layout, template.data);
    } else {
      this.performAssignUserAction(data);
    }
  }

  performAssignUserAction(data) {
    const baseUrl = data.entity.baseUrl;
    const issueKey = data.entity.issue.key;

    searchAssignableUser(baseUrl, issueKey, this.selectedUser, this.jwt)
      .then((users) => {
        if (users.data.length === 0) {
          return Promise.reject(new Error(401));
        }

        return assignUser(baseUrl, issueKey, users.data[0].name, this.jwt);
      })
      .then(() => this.successDialog(data))
      .catch((error) => {
        let errorMessage;

        switch (error.message) {
          case '401': {
            errorMessage = `User ${this.selectedUser.prettyName} is not authorized to perform this action`;
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

        this.selectedUser = {};

        const assignTemplate = assignDialog();
        const dialogBuilder = new DialogBuilder('Assign', assignTemplate);

        dialogBuilder.error(errorMessage);

        const template = this.retrieveTemplate(dialogBuilder, data, this.serviceName);
        this.updateDialog('assignIssue', template.layout, template.data);
      });
  }

  closeActionDialog() {
    this.closeDialog('assignIssue');
  }

  selected(user) {
    this.selectedUser = user;
  }

  deselected() {
    this.selectedUser = {};
  }
}
