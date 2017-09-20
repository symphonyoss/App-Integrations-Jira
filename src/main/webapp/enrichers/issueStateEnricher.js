import { MessageEnricherBase } from 'symphony-integration-commons';
import AssignUserService from '../services/AssignUserService';
import CommentService from '../services/CommentService';

const actions = require('../templates/actions.hbs');
const errorDialog = require('../templates/errorDialog.hbs');

const name = 'issueState-renderer';
const messageEvents = ['com.symphony.integration.jira.event.v2.state'];

export default class IssueStateEnricher extends MessageEnricherBase {
  constructor() {
    super(name, messageEvents);
    this.implements = ['enrich', 'action', 'selected'];

    const assignUserService = new AssignUserService(name);
    const commentService = new CommentService(name);

    this.services = {
      assignDialog: assignUserService,
      assignIssue: assignUserService,
      commentDialog: commentService,
      commentIssue: commentService,
      closeAssignDialog: assignUserService,
      closeCommentDialog: commentService,
    };
  }

  enrich(type, entity) {
    const result = {
      template: actions(),
      data: {
        assignTo: {
          service: name,
          label: 'Assign To',
          data: {
            entity,
            type: 'assignDialog',
          },
        },
        commentIssue: {
          service: name,
          label: 'Comment',
          data: {
            entity,
            type: 'commentDialog',
          },
        },
      },
    };

    return result;
  }

  action(data) {
    const service = this.services[data.type];

    if (service === undefined) {
      this.dialogsService.show('error', name, errorDialog(), {}, {});
    } else {
      service.action(data);
    }
  }

  selected(user) {
    this.services.assignDialog.selected(user);
  }
}
