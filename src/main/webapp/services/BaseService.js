import { authorizeUser } from 'symphony-integration-commons';

const unexpectedErrorDialog = require('../templates/unexpectedErrorDialog.hbs');

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
      .catch(() => this.openDialog('unexpectedErrorDialog', unexpectedErrorDialog(), {}));
  }

  rejectPromise(error) {
    const response = error.response || {};
    const status = response.status || 500;

    return Promise.reject(new Error(status));
  }

  closeDialog(dialog) {
    const dialogsService = SYMPHONY.services.subscribe('dialogs');
    dialogsService.close(dialog);
  }
}
