export default (actions, service, entity) =>
  actions.reduce((result, action) => {
    const actionObj = {};
    const actionId = action.id || action.subtype;

    const actionData = {
      service,
      label: action.label,
      data: {
        entity,
        type: action.type,
        subtype: action.subtype,
      },
    };

    actionObj[actionId] = actionData;
    return Object.assign(result, actionObj);
  }, {});
