/**
 * This builder is used to build a JIRA dialog template according to some required parameters
 */
import { getIntegrationBaseUrl } from 'symphony-integration-commons';

const dialog = require('../dialog.hbs');

export default class DialogBuilder {
  constructor(actionText, innerContent, hasAssignee = false) {
    this.actionText = actionText;
    this.innerContent = innerContent;
    this.showFooter = true;
    this.showError = false;
    this.isLoading = false;
    this.hasAssignee = hasAssignee;
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

  loading(value) {
    this.isLoading = value;
  }

  build(data) {
    const template = dialog({
      exclamationUrl: `${getIntegrationBaseUrl()}/apps/jira/img/exclamation_mark.svg`,
      url: data.entity.issue.url,
      key: data.entity.issue.key,
      subject: data.entity.issue.subject.replace(/<mention email="(.*)"\/>/, '$1'),
      actionText: this.actionText,
      content: this.innerContent,
      errorMessage: this.errorMessage,
      showError: this.showError,
      headerError: this.headerError,
      footer: this.showFooter,
      isLoading: this.isLoading,
      hasAssignee: this.hasAssignee,
      assignee: (this.hasAssignee && data.fields) ? data.fields.assignee.displayName : 'Not available',
    });

    return template;
  }
}
