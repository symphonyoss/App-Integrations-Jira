import { MessageEnricherBase } from 'symphony-integration-commons';

const name = 'issueState-renderer';
const messageEvents = ['com.symphony.integration.jira.event.v2.state'];

function renderComment() {
  const dialogTemplate = `
    <dialog>
      <h1>Comment</h1>
    </dialog>
  `;
  return dialogTemplate;
}

function renderAssignTo(data) {
  const dialogTemplate = `
    <dialog>
      <h1><a href="${data.entity.issue.url}">${data.entity.issue.key}</a></h1>
      <h3><b>Assignee:</b></h3>${data.entity.issue.assignee.displayName}
      <stextarea/>
    </dialog>
  `;
  return dialogTemplate;
}

export default class IssueStateEnricher extends MessageEnricherBase {
  constructor() {
    super(name, messageEvents);
  }

  enrich(type, entity) {
    const result = {
      template: `
        <messageML>
          <action id="assignTo" class="tempo-text-color--link"/>
          <action id="commentIssue" class="tempo-text-color--link"/>
        </messageML>
      `,
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
        dialogTemplate = `
          <dialog>
            <h1>ERROR</h1>
          </dialog>
        `;
        break;
    }
    this.dialogsService.show('action', 'issueRendered-renderer', dialogTemplate, {}, {});
  }
}
