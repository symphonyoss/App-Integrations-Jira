import { MessageEnricherBase, getUserJWT } from 'symphony-integration-commons';
import { commentIssue } from '../services/jiraApiCalls';

const commentDialog = require('../templates/commentDialog.hbs');
const assingDialog = require('../templates/assignDialog.hbs');
const erroDialog = require('../templates/errorDialog.hbs');
const messageML = require('../templates/messageML.hbs');

const name = 'issueState-renderer';
const messageEvents = ['com.symphony.integration.jira.event.v2.state'];

function renderComment() {
  return commentDialog({
    func: commentIssue(),
  });
}

function renderAssignTo(data) {
  return assingDialog({
    url: data.entity.issue.url,
    key: data.entity.issue.key,
    name: data.entity.issue.assignee.displayName,
  });
}

export default class IssueStateEnricher extends MessageEnricherBase {
  constructor() {
    super(name, messageEvents);
  }

  enrich(type, entity) {
    const result = {
      template: messageML(),
      data: {
        assignTo: {
          service: name,
          label: 'Assign To',
          data: {
            entity,
            type: 'AssignTo',
          },
        },
        commentIssue: {
          service: name,
          label: 'Comment',
          data: {
            entity,
            type: 'Comment',
          },
        },
        frame: {
          src: 'https://localhost.symphony.com:8186/apps/jira/bundle.json',
          height: 200,
        },
      },
    };

    return result;
  }

  action(data) {
    let dialogTemplate = null;
    switch (data.type) {
      case 'Comment':
        dialogTemplate = renderComment(data);
        break;
      case 'AssignTo':
        dialogTemplate = renderAssignTo(data);
        break;
      default:
        dialogTemplate = erroDialog();
        break;
    }
    this.dialogsService.show('action', 'issueRendered-renderer', dialogTemplate, {}, {});
  }
}
