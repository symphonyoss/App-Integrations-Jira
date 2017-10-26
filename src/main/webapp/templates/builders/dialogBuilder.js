/**
 * This builder is used to build a JIRA dialog template according to some required parameters
 */
import { getIntegrationBaseUrl } from 'symphony-integration-commons';

const dialog = require('../dialog.hbs');

export default class DialogBuilder {
  constructor(actionText, innerContent, assignee) {
    this.actionText = actionText;
    this.innerContent = innerContent;
    this.showFooter = true;
    this.showError = false;
    this.buttonsDisabled = false;
    this.assignee = assignee;
  }

  headerError(errorMessage) {
    this.headerError = errorMessage;
  }

  error(message) {
    this.errorMessage = message;
    this.showError = true;
  }

  footer(value) {
    this.showFooter = value;
  }

  disableButtons(value) {
    this.buttonsDisabled = value;
  }

  build(data) {
    const template = dialog({
      exclamationUrl: `${getIntegrationBaseUrl()}/apps/jira/img/exclamation_mark.svg`,
      url: data.entity.issue.url,
      key: data.entity.issue.key,
      subject: data.entity.issue.subject,
      actionText: this.actionText,
      content: this.innerContent,
      errorMessage: this.errorMessage,
      showError: this.showError,
      headerError: this.headerError,
      footer: this.showFooter,
      disableButtons: this.buttonsDisabled,
      assignee: this.assignee,
    });

    return template;
  }
}
