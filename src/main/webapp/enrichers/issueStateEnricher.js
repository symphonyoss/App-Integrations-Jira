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
    this.implements.push('selected', 'changed', 'deselected');

    // Create new service components responsible for actions handling
    const assignUserService = new AssignUserService(enricherServiceName);
    const commentService = new CommentService(enricherServiceName);

    // Mapping actions to the corresponding services
    this.services = {
      assignUserService,
      commentService,
    };
  }

  enrich(type, entity) {
    const assignToAction = {
      id: 'assignTo',
      service: 'assignUserService',
      type: 'openDialog',
      label: 'Assign To',
    };

    const commentIssueAction = {
      id: 'commentIssue',
      service: 'commentService',
      type: 'openDialog',
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
    const service = this.services[data.service];

    if (service === undefined) {
      this.dialogsService.show('error', enricherServiceName, errorDialog(), {}, {});
    } else {
      service.action(data);
    }
  }

  selected(user) {
    this.services.assignUserService.selected(user);
  }

  deselected() {
    this.services.assignUserService.deselected();
  }

  changed(comment) {
    this.services.commentService.changed(comment);
  }
}
