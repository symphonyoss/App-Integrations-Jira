const commentDialog = require('../templates/commentDialog.hbs');

export default class CommentService {
  constructor(serviceName) {
    this.serviceName = serviceName;
  }

  showDialog(data) {
    const dialogsService = SYMPHONY.services.subscribe('dialogs');

    const template = commentDialog({
      func: this.commentIssue(data),
    });

    const commentData = {
      commentIssue: {
        service: this.serviceName,
        label: 'OK',
        data: {
          type: 'commentIssue',
        },
      },
      closeCommentDialog: {
        service: this.serviceName,
        label: 'Cancel',
        data: {
          type: 'closeCommentDialog',
        },
      },
    };

    dialogsService.show('commentIssue', this.serviceName, template, commentData, { title: 'Comment issue' });
  }

  save() {
    const dialogsService = SYMPHONY.services.subscribe('dialogs');

    this.close('commentIssue');

    const template = '<messageML>Comment created</messageML>';

    dialogsService.show('commentCreated', this.serviceName, template, {}, { title: 'Comment issue' });
  }

  close(dialog) {
    const dialogsService = SYMPHONY.services.subscribe('dialogs');
    dialogsService.close(dialog);
  }

  action(data) {
    switch (data.type) {
      case 'commentDialog': {
        this.showDialog(data);
        break;
      }
      case 'commentIssue': {
        this.save(data);
        break;
      }
      case 'closeCommentDialog': {
        this.close('commentIssue');
        break;
      }
      default: {
        break;
      }
    }
  }
}
