/**
 * This builder is used to build a JIRA dialog template according to some required parameters
 */

const dialog = require('../dialog.hbs');

export default class DialogBuilder {
  constructor(actionText, innerContent) {
    this.actionText = actionText;
    this.innerContent = innerContent;
  }

  build(data) {
    const template = dialog({
      url: data.entity.issue.url,
      key: data.entity.issue.key,
      subject: data.entity.issue.subject,
      actionText: this.actionText,
      content: this.innerContent,
    });
    return template;
  }
}
