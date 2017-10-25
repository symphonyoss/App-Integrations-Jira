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
    this.currentAssignee = null;
  }

  successDialog(data) {
    const image = `${baseUrl}/apps/jira/img/icon-checkmark-green.svg`;
    const content = successDialog({ successImg: image });

    const dialogBuilder = new DialogBuilder('Assign', content, this.currentAssignee);
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
    setTimeout(() => this.closeDialog('assignIssue'), 3000);
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
    let template = null;

    searchIssue(baseUrl, issueKey, service.jwt)
      .then((issueInfo) => {
        service.currentAssignee = (issueInfo.data.fields.assignee) ? issueInfo.data.fields.assignee.displayName : 'Unassigned';
        service.selectedUser = {};

        const dialogBuilder = new DialogBuilder('Assign', assignTemplate, service.currentAssignee);
        template = service.retrieveTemplate(dialogBuilder, data, service.serviceName);
        service.openDialog('assignIssue', template.layout, template.data);
      })
      .catch(() => {
        const dialogBuilder = new DialogBuilder('Assign', assignTemplate);
        dialogBuilder.headerError('Issue not found');
        dialogBuilder.disableButtons(true);
        template = service.retrieveTemplate(dialogBuilder, data, service.serviceName);
        service.openDialog('assignIssue', template.layout, template.data);
      });
  }

  save(data) {
    const assignTemplate = assignDialog();
    const dialogBuilder = new DialogBuilder('Assign', assignTemplate, this.currentAssignee);

    if (this.selectedUser.email === undefined) {
      dialogBuilder.error('Please select an user');

      const template = this.retrieveTemplate(dialogBuilder, data, this.serviceName);
      this.updateDialog('assignIssue', template.layout, template.data);
    } else {
      dialogBuilder.disableButtons(true);

      const template = this.retrieveTemplate(dialogBuilder, data, this.serviceName, true, 'SAVING...');
      this.updateDialog('assignIssue', template.layout, template.data);

      this.performAssignUserAction(data);
    }
  }

  performAssignUserAction(data) {
    const baseUrl = data.entity.baseUrl;
    const issueKey = data.entity.issue.key;
    let newAssignee = null;

    searchAssignableUser(baseUrl, issueKey, this.selectedUser, this.jwt)
      .then((users) => {
        if (users.data.length === 0) {
          return Promise.reject(new Error(401));
        }

        // save the display name to update the current assignee if the
        // assign action succeeds
        newAssignee = users.data[0].displayName;

        return assignUser(baseUrl, issueKey, users.data[0].name, this.jwt);
      })
      .then(() => {
        this.currentAssignee = newAssignee;
        this.successDialog(data);
      })
      .catch((error) => {
        const assignTemplate = assignDialog();
        const dialogBuilder = new DialogBuilder('Assign', assignTemplate, this.currentAssignee);

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

        const template = this.retrieveTemplate(dialogBuilder, data, this.serviceName, true);
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
