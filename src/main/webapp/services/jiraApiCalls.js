import axios from 'axios';

export const commentIssue = (baseUrl, issuename, url, comment, jwt) => {
  const auth = `Bearer ${jwt}`;
  const jiraUrl = `${baseUrl}/v1/jira/rest/api/issue/${issuename}/comment?${url}`;
  const payload = {
    body: { comment },
  };
  const config = {
    headers: { Authorization: auth },
  };
  return axios.post(jiraUrl, payload, config);
};

