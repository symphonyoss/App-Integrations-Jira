import axios from 'axios';
import { authorizeUser, getIntegrationBaseUrl } from 'symphony-integration-commons';

const assignDialog = require('../templates/assignDialog.hbs');
const errorDialog = require('../templates/errorDialog.hbs');
const unexpectedErrorDialog = require('../templates/unexpectedErrorDialog.hbs');
const forbiddenDialog = require('../templates/forbiddenDialog.hbs');
const notFoundDialog = require('../templates/issueNotFoundDialog.hbs');
const successDialog = require('../templates/successDialog.hbs');

export default class AssignUserService {
  constructor(serviceName) {
    this.serviceName = serviceName;
    this.selectedUser = {};
    this.baseUrl = getIntegrationBaseUrl();
    this.jwt = '';
  }

  successDialog(issueKey) {
    const dialogsService = SYMPHONY.services.subscribe('dialogs');

    this.close('assignIssue');

    const template = successDialog({
      issueKey,
      prettyName: this.selectedUser.prettyName,
    });

    dialogsService.show('userAssigned', this.serviceName, template, {}, { title: 'Assign issue' });
  }

  openDialog(id, template, data) {
    const dialogsService = SYMPHONY.services.subscribe('dialogs');
    dialogsService.show(id, this.serviceName, template, data, {});
  }

  openAssignDialog(data) {
    const template = assignDialog({
      url: data.entity.issue.url,
      key: data.entity.issue.key,
      name: data.entity.issue.assignee.displayName,
    });

    const userData = {
      user: {
        service: this.serviceName,
        crossPod: 'NONE',
      },
      assignIssue: {
        service: this.serviceName,
        label: 'OK',
        data: {
          entity: data.entity,
          type: 'assignIssue',
        },
      },
      closeAssignDialog: {
        service: this.serviceName,
        label: 'Cancel',
        data: {
          entity: data.entity,
          type: 'closeAssignDialog',
        },
      },
    };

    this.openDialog('assignIssue', template, userData);
  }

  showDialog(data) {
    const baseUrl = data.entity.baseUrl;

    authorizeUser(baseUrl)
      .then((response) => {
        if (response.success) {
          this.jwt = response.jwt;
          this.openAssignDialog(data);
        }
      })
      .catch(() => this.openDialog('unexpectedErrorDialog', unexpectedErrorDialog(), {}));
  }

  searchAssignableUser(url, issueKey) {
    const apiUrl = `${this.baseUrl}/v1/jira/rest/api/user/assignable/search`;
    const emailAddress = this.selectedUser.email;

    const params = {
      url,
      issueKey,
      username: emailAddress,
    };

    return axios.get(apiUrl, {
      params,
      headers: { Authorization: `Bearer ${this.jwt}` },
    }).catch((error) => {
      const response = error.response || {};
      const status = response.status || 500;

      return Promise.reject(new Error(status));
    });
  }

  assignUser(url, issueKey, username) {
    const apiUrl = `${this.baseUrl}/v1/jira/rest/api/issue/${issueKey}/assignee`;

    return axios({
      method: 'put',
      url: apiUrl,
      headers: { Authorization: `Bearer ${this.jwt}` },
      params: {
        url,
        username,
      },
    }).catch((error) => {
      const response = error.response || {};
      const status = response.status || 500;

      return Promise.reject(new Error(status));
    });
  }

  save(data) {
    const baseUrl = data.entity.baseUrl;
    const issueKey = data.entity.issue.key;

    this.searchAssignableUser(baseUrl, issueKey)
      .then((users) => {
        if (users.data.length === 0) {
          return Promise.reject(new Error(401));
        }

        return this.assignUser(baseUrl, issueKey, users.data[0].name);
      })
      .then(() => this.successDialog(issueKey))
      .catch((error) => {
        console.log(error.message);
        console.log(typeof error.message);
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

  close(dialog) {
    const dialogsService = SYMPHONY.services.subscribe('dialogs');
    dialogsService.close(dialog);
  }

  action(data) {
    switch (data.type) {
      case 'assignDialog': {
        this.showDialog(data);
        break;
      }
      case 'assignIssue': {
        this.save(data);
        break;
      }
      case 'closeAssignDialog': {
        this.close('assignIssue');
        break;
      }
      default: {
        const dialogsService = SYMPHONY.services.subscribe('dialogs');
        dialogsService.show('error', this.serviceName, errorDialog(), {}, {});
        break;
      }
    }
  }

  selected(user) {
    this.selectedUser = user;
  }
}
