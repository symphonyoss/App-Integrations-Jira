import { authorizeUser, getIntegrationBaseUrl } from 'symphony-integration-commons';

const unexpectedErrorDialog = require('../templates/unexpectedErrorDialog.hbs');
const unauthorizedErrorDialog = require('../templates/unauthorizedErrorDialog.hbs');
const configurationErrorDialog = require('../templates/configurationErrorDialog.hbs');

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
        } else {
          // This will only happen when the auth process returns a 401 but there is a configuration
          // problem inside the JiraOauth, then authorizeUser only returns FALSE
          const exclamationUrl = `${getIntegrationBaseUrl()}/apps/jira/img/exclamation_mark.svg`;
          const template = configurationErrorDialog({
            exclamationUrl,
          });
          this.openDialog('errorDialog', template, data);
        }
      })
      .catch((error) => {
        const response = error.response || {};
        const status = response.status || 500;
        const exclamationUrl = `${getIntegrationBaseUrl()}/apps/jira/img/exclamation_mark.svg`;

        let template = {};

        switch (status) {
          case 401: {
            template = unauthorizedErrorDialog({
              exclamationUrl,
            });
            break;
          }
          default: {
            template = unexpectedErrorDialog({
              baseUrl,
              exclamationUrl,
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
