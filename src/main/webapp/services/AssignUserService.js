import axios from 'axios';
import { authorizeUser } from 'symphony-integration-commons';

const assignDialog = require('../templates/assignDialog.hbs');
const errorDialog = require('../templates/errorDialog.hbs');

export default class AssignUserService {
  constructor(serviceName) {
    this.serviceName = serviceName;
    this.selectedUser = {};
    this.baseUrl = 'https://localhost.symphony.com:8186/integration';
  }

  showDialog(data) {
    const dialogsService = SYMPHONY.services.subscribe('dialogs');

    const template = assignDialog({
      url: data.entity.issue.url,
      key: data.entity.issue.key,
      name: data.entity.issue.assignee.displayName,
    });

    const userData = {
      user: {
        service: this.serviceName,
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

    dialogsService.show('assignIssue', this.serviceName, template, userData, { title: 'Assign issue' });
  }

  searchAssignableUser(url, issueKey, jwt) {
    const apiUrl = `${this.baseUrl}/v1/jira/rest/api/user/assignable/search`;
    const emailAddress = this.selectedUser.email;

    const params = {
      url,
      issueKey,
      username: emailAddress,
    };

    return axios.get(apiUrl, {
      params,
      headers: { Authorization: `Bearer ${jwt}` },
    });
  }

  assignUser(url, issueKey, username, jwt) {
    const apiUrl = `${this.baseUrl}/v1/jira/rest/api/issue/${issueKey}/assignee`;

    const params = {
      url,
      username,
    };

    return axios.put(apiUrl, {
      params,
      headers: { Authorization: `Bearer ${jwt}` },
    });
  }

  successDialog(issueKey) {
    const dialogsService = SYMPHONY.services.subscribe('dialogs');

    this.close('assignIssue');

    const template = `<dialog><h1>Issue ${issueKey} assigned to ${this.selectedUser.prettyName}</h1></dialog>`;
    dialogsService.show('userAssigned', this.serviceName, template, {}, { title: 'Assign issue' });
  }

  errorDialog() {
    const dialogsService = SYMPHONY.services.subscribe('dialogs');

    const template = `<dialog><h1>User ${this.selectedUser.prettyName} not authorized</h1></dialog>`;
    dialogsService.show('errorAssignDialog', this.serviceName, template, {}, { title: 'Assign issue' });
  }

  save(data) {
    const baseUrl = data.entity.baseUrl;
    const issueKey = data.entity.issue.key;
    let jwt;

    authorizeUser(baseUrl)
      .then((response) => {
        if (response.success) {
          jwt = response.jwt;
          return this.searchAssignableUser(baseUrl, issueKey, jwt);
        }

        return Promise.reject(new Error('unauthorized'));
      })
      .then((users) => {
        if (users.length === 0) {
          this.errorDialog();
          return Promise.reject(new Error('forbidden'));
        }

        return this.assignUser(baseUrl, issueKey, users[0].name, jwt);
      })
      .then(() => this.successDialog(issueKey));
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
