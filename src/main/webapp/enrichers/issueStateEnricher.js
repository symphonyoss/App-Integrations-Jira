import { MessageEnricherBase } from 'symphony-integration-commons';
import AssignUserService from '../services/AssignUserService';
import CommentService from '../services/CommentService';

const actions = require('../templates/actions.hbs');
const errorDialog = require('../templates/errorDialog.hbs');

const enricherServiceName = 'issueState-renderer';
const messageEvents = ['com.symphony.integration.jira.event.v2.state'];

export default class IssueStateEnricher extends MessageEnricherBase {
  constructor() {
    super(enricherServiceName, messageEvents);
    this.implements.push('selected', 'changed');

    // Create new service components responsible for actions handling
    const assignUserService = new AssignUserService(enricherServiceName);
    const commentService = new CommentService(enricherServiceName);

    // Mapping actions to the corresponding services
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
          service: enricherServiceName,
          label: 'Assign To',
          data: {
            entity,
            type: 'assignDialog',
          },
        },
        commentIssue: {
          service: enricherServiceName,
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
