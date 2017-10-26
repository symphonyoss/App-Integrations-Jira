import { authorizeUser } from 'symphony-integration-commons';

const unexpectedErrorDialog = require('../templates/unexpectedErrorDialog.hbs');
const unauthorizedErrorDialog = require('../templates/unauthorizedErrorDialog.hbs');
const errorDialog = require('../templates/errorDialog.hbs');

export default class BaseService {
  constructor(serviceName) {
    this.serviceName = serviceName;
    this.jwt = '';
  }

  openDialog(id, template, data) {
    const dialogsService = SYMPHONY.services.subscribe('dialogs');
    dialogsService.show(id, this.serviceName, template, data, {});
  }

  showDialog(data, callback) {
    const baseUrl = data.entity.baseUrl;

    authorizeUser(baseUrl)
      .then((response) => {
        if (response.success) {
          this.jwt = response.jwt;

          if (typeof callback === 'function') {
            callback(data, this);
          } else {
            Promise.reject(new Error(500));
          }
        }
      })
      .catch((error) => {
        const response = error.response || {};
        const status = response.status || 500;

        let template = {};

        switch (status) {
          case 401: {
            template = unauthorizedErrorDialog();
            break;
          }
          default: {
            template = unexpectedErrorDialog({
              baseUrl,
            });
            break;
          }
        }

        this.openDialog('errorDialog', template, data);
      });
  }

  closeDialog(dialog) {
    const dialogsService = SYMPHONY.services.subscribe('dialogs');
    dialogsService.close(dialog);
  }

  action(data) {
    switch (data.type) {
      case 'openDialog': {
        this.showDialog(data, this.openActionDialog);
        break;
      }
      case 'performDialogAction': {
        this.save(data);
        break;
      }
      case 'closeDialog': {
        this.closeActionDialog();
        break;
      }
      default: {
        this.openDialog('error', errorDialog(), {});
        break;
      }
    }
  }

  updateDialog(id, template, data) {
    const dialogsService = SYMPHONY.services.subscribe('dialogs');
    dialogsService.close(id);
    dialogsService.show(id, this.serviceName, template, data, {});
  }
}
