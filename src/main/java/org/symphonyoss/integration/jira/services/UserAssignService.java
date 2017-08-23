/**
 * Copyright 2016-2017 Symphony Integrations - Symphony LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.symphonyoss.integration.jira.services;

import static org.symphonyoss.integration.jira.webhook.JiraParserConstants.NAME_PATH;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.GenericData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Exception;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1HttpRequestException;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;
import org.symphonyoss.integration.jira.exception.JiraUnexpectedException;

import java.net.URL;

/**
 * Service that handles the assign user to issue on the JIRA API
 * Created by hamitay on 8/15/17.
 */
@Component
public class UserAssignService extends CommonJiraService {

  private static final String SERVICE_NAME = "User Assign Service";

  private static final String PATH_JIRA_API_ASSIGN_ISSUE = "/rest/api/latest/issue/%s/assignee";

  public ResponseEntity assignUserToIssue(String accessToken, String issueKey, String username,
      String baseUrl, OAuth1Provider provider) {

    validateIssueKeyParameter(issueKey);

    URL assignUserUrl = getServiceUrl(baseUrl, String.format(PATH_JIRA_API_ASSIGN_ISSUE, issueKey));

    try {
      GenericData data = new GenericData();
      data.put(NAME_PATH, username);
      JsonHttpContent content = new JsonHttpContent(new JacksonFactory(), data);

      provider.makeAuthorizedRequest(accessToken, assignUserUrl, HttpMethods.PUT, content);
    } catch (OAuth1HttpRequestException e) {
      if (e.getCode() == HttpStatus.NOT_FOUND.value()) {
        handleIssueNotFound(issueKey);
      }

      if (e.getCode() == HttpStatus.BAD_REQUEST.value()) {
        handleUserNotFound(username);
      }

      if (e.getCode() == HttpStatus.UNAUTHORIZED.value()) {
        handleUserUnauthorized();
      }

      throw new JiraUnexpectedException(SERVICE_NAME, e.getMessage(), e);
    } catch (OAuth1Exception e) {
      throw new JiraUnexpectedException(SERVICE_NAME, e.getMessage(), e);
    }

    return ResponseEntity.ok(HttpStatus.OK);
  }

  @Override
  protected String getServiceName() {
    return SERVICE_NAME;
  }

}
