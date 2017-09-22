import { MessageEnricherBase } from 'symphony-integration-commons';
import AssignUserService from '../services/assignUserService';
import CommentService from '../services/commentService';
import actionFactory from '../utils/actionFactory';

const actions = require('../templates/actions.hbs');
const errorDialog = require('../templates/errorDialog.hbs');

const enricherServiceName = 'issueState-renderer';
const messageEvents = [
  'com.symphony.integration.jira.event.v2.state',
  'com.symphony.integration.jira.event.v2.issue_commented',
];

export default class IssueStateEnricher extends MessageEnricherBase {
  constructor() {
    super(enricherServiceName, messageEvents);
    this.implements.push('selected', 'changed');

    // Create new service components responsible for actions handling
    const assignUserService = new AssignUserService(enricherServiceName);
    const commentService = new CommentService(enricherServiceName);

    // Mapping actions to the corresponding services
    this.services = {
      assignUserService,
      commentIssueService: commentService,
    };
  }

  enrich(type, entity) {
    const assignToAction = {
      id: 'assignTo',
      type: 'assignUserService',
      subtype: 'openDialog',
      label: 'Assign To',
    };
    const commentIssueAction = {
      id: 'commentIssue',
      type: 'commentIssueService',
      subtype: 'openDialog',
      label: 'Comment',
    };

    const data = actionFactory([assignToAction, commentIssueAction], enricherServiceName, entity);

    const result = {
      template: actions(),
      data,
    };

    return result;
  }

  action(data) {
    const service = this.services[data.type];

    if (service === undefined) {
      this.dialogsService.show('error', enricherServiceName, errorDialog(), {}, {});
    } else {
      service.action(data);
    }
  }

  selected(user) {
    this.services.assignDialog.selected(user);
  }

  changed(comment) {
    this.services.commentDialog.changed(comment);
  }
}
