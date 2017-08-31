import axios from 'axios';

// export const commentIssue = (baseUrl,issuename, url, jwt) => {
export const commentIssue = () => {
// const url = '{{baseUrl}}v1/jira/rest/api/issue/{{issuename}}/comment?{{url}}';
  const url = 'https://nexus2-dev.symphony.com/integration/v1/jira/rest/api/issue/TWT-4/comment?url=https://previewjira.atlassian.net';
  const payload = {
    body: 'testing the API through the FE',
    // headers: {'Authorization': "Bearer" + jwt },
    headers: { Authorization: 'Bearer' },
  };
  return axios.post(url, payload);
};
