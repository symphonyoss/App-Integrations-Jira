import { getIntegrationBaseUrl } from 'symphony-integration-commons';
import BaseService from './baseService';
import { searchAssignableUser, assignUser, searchIssue } from '../api/apiCalls';
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

    const dialogBuilder = new DialogBuilder('Assign', content, true);
    dialogBuilder.footer(false);

    const template = dialogBuilder.build(data);

    const userData = {
      user: {
        service: this.serviceName,
        successMessage: 'Assigned',
        selected: this.selectedUser,
        renderSelectedUser: true,
      },
    };

    this.updateDialog('assignIssue', template, userData);
  }

  retrieveTemplate(dialogBuilder, data, serviceName, renderSelectedUser = false, assignLabel = 'ASSIGN') {
    const template = dialogBuilder.build(data);

    const assignIssueAction = {
      service: 'assignUserService',
      type: 'performDialogAction',
      label: assignLabel,
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
        renderSelectedUser,
        selected: this.selectedUser,
      },
    }, actions);

    return {
      layout: template,
      data: userData,
    };
  }

  openActionDialog(data, service) {
    const baseUrl = data.entity.baseUrl;
    const issueKey = data.entity.issue.key;

    const assignTemplate = assignDialog();
    const dialogBuilder = new DialogBuilder('Assign', assignTemplate, true);
    let template = null;

    searchIssue(baseUrl, issueKey, service.jwt)
      .then((issueInfo) => {
        Object.assign(data, issueInfo.data);
        service.selectedUser = {};

        template = service.retrieveTemplate(dialogBuilder, data, service.serviceName);
        service.openDialog('assignIssue', template.layout, template.data);
      })
      .catch(() => {
        dialogBuilder.headerError('Issue not found');
        template = service.retrieveTemplate(dialogBuilder, data, service.serviceName);
        service.openDialog('assignIssue', template.layout, template.data);
      });
  }

  save(data) {
    const assignTemplate = assignDialog();
    const dialogBuilder = new DialogBuilder('Assign', assignTemplate, true);

    if (this.selectedUser.email === undefined) {
      dialogBuilder.error('Please select an user');

      const template = this.retrieveTemplate(dialogBuilder, data, this.serviceName);
      this.updateDialog('assignIssue', template.layout, template.data);
    } else {
      dialogBuilder.loading(true);

      const template = this.retrieveTemplate(dialogBuilder, data, this.serviceName, true, 'SAVING...');
      this.updateDialog('assignIssue', template.layout, template.data);

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
      .then(() => searchIssue(baseUrl, issueKey, this.jwt))
      .then((issueInfo) => {
        Object.assign(data, issueInfo.data);
        this.successDialog(data);
      })
      .catch((error) => {
        this.selectedUser = {};

        const assignTemplate = assignDialog();
        const dialogBuilder = new DialogBuilder('Assign', assignTemplate, true);

        switch (error.message) {
          case '401': {
            const errorMessage = 'This person doesn’t have a valid Jira account';
            dialogBuilder.error(errorMessage);
            break;
          }
          case '404': {
            const errorMessage = `Issue ${issueKey} not found`;
            dialogBuilder.headerError(errorMessage);
            break;
          }
          default: {
            const errorMessage = 'Assignee not saved due to a network error. Please try again.';
            dialogBuilder.headerError(errorMessage);
            break;
          }
        }

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
