export default (actions, service, entity) =>
  actions.reduce((result, action) => {
    const actionObj = {};
    const actionId = action.id || action.type;

    const actionData = {
      service,
      label: action.label,
      data: {
        entity,
        service: action.service,
        type: action.type,
      },
    };

    actionObj[actionId] = actionData;
    return Object.assign(result, actionObj);
  }, {});
