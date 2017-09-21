import axios from 'axios';
import { getIntegrationBaseUrl } from 'symphony-integration-commons';

const baseUrl = getIntegrationBaseUrl();

const rejectPromise = (error) => {
  const response = error.response || {};
  const status = response.status || 500;

  return Promise.reject(new Error(status));
};

export const searchAssignableUser = (url, issueKey, user, jwt) => {
  const apiUrl = `${baseUrl}/v1/jira/rest/api/user/assignable/search`;
  const emailAddress = user.email;

  const params = {
    url,
    issueKey,
    username: emailAddress,
  };

  return axios.get(apiUrl, {
    params,
    headers: { Authorization: `Bearer ${jwt}` },
  }).catch(error => rejectPromise(error));
};

export const assignUser = (url, issueKey, username, jwt) => {
  const apiUrl = `${baseUrl}/v1/jira/rest/api/issue/${issueKey}/assignee`;

  return axios({
    method: 'put',
    url: apiUrl,
    headers: { Authorization: `Bearer ${jwt}` },
    params: {
      url,
      username,
    },
  }).catch(error => rejectPromise(error));
};

export const commentIssue = (url, issueKey, comment, jwt) => {
  const apiUrl = `${baseUrl}/v1/jira/rest/api/issue/${issueKey}/comment`;

  return axios({
    method: 'post',
    url: apiUrl,
    headers: { Authorization: `Bearer ${jwt}` },
    params: {
      url,
    },
    data: {
      body: comment,
    },
  }).catch(error => rejectPromise(error));
};
