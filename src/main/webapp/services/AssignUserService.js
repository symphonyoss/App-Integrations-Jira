const assignDialog = require('../templates/assignDialog.hbs');
const errorDialog = require('../templates/errorDialog.hbs');

export default class AssignUserService {
  constructor(serviceName) {
    this.serviceName = serviceName;
    this.selectedUser = {};
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

  save(data) {
    const dialogsService = SYMPHONY.services.subscribe('dialogs');

    this.close('assignIssue');

    const template = `<dialog><h1>Issue ${data.entity.issue.key} assigned to ${this.selectedUser.prettyName}</h1></dialog>`;
    dialogsService.show('userAssigned', this.serviceName, template, {}, { title: 'Assign issue' });
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
