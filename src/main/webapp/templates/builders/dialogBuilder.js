/**
 * This builder is used to build a JIRA dialog template according to some required parameters
 */

const dialog = require('../dialog.hbs');

export default class DialogBuilder {
  constructor(actionText, innerContent) {
    this.actionText = actionText;
    this.innerContent = innerContent;
    this.showFooter = true;
    this.showError = false;
    this.isLoading = false;
  }

  error(message) {
    this.errorMessage = message;
    this.showError = true;
  }

  footer(value) {
    this.showFooter = value;
  }

  loading(value) {
    this.isLoading = value;
  }

  build(data) {
    const template = dialog({
      url: data.entity.issue.url,
      key: data.entity.issue.key,
      subject: data.entity.issue.subject,
      actionText: this.actionText,
      content: this.innerContent,
      errorMessage: this.errorMessage,
      showError: this.showError,
      footer: this.showFooter,
      isLoading: this.isLoading,
    });
    return template;
  }
}
