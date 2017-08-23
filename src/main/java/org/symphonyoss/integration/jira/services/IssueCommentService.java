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

import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys.COMPONENT;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys.MALFORMED_COMMENT;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys
    .MALFORMED_COMMENT_SOLUTION;
import static org.symphonyoss.integration.jira.webhook.JiraParserConstants.BODY_PATH;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.symphonyoss.integration.jira.exception.InvalidJiraPayloadException;
import org.symphonyoss.integration.jira.exception.JiraUnexpectedException;
import org.symphonyoss.integration.json.JsonUtils;

import java.io.IOException;
import java.net.URL;

/**
 * Service that handles the comments to an issue on the JIRA API
 *
 * Created by alexandre-silva-daitan on 21/08/17.
 */
@Component
public class IssueCommentService extends CommonJiraService {

  private static final String SERVICE_NAME = "Issue Comment Service";

  private static final String PATH_JIRA_API_COMMENT_ISSUE = "/rest/api/latest/issue/%s/comment";

  /**
   * Add a new comment to an issue.
   *
   * @param accessToken User access token
   * @param issueKey Issue key
   * @param baseUrl JIRA base URL
   * @param provider OAuth provider
   * @param comment Comment text
   * @return
   */
  public ResponseEntity addCommentToAnIssue(String accessToken, String issueKey,
      String baseUrl, OAuth1Provider provider, String comment) {
    validateIssueKeyParameter(issueKey);

    String commentBody = retrieveComment(comment);

    validateComment(commentBody);

    URL commentIssueUrl = getServiceUrl(baseUrl, String.format(PATH_JIRA_API_COMMENT_ISSUE, issueKey));

    try {
      GenericData data = new GenericData();
      data.put(BODY_PATH, commentBody);
      JsonHttpContent content = new JsonHttpContent(new JacksonFactory(), data);

      provider.makeAuthorizedRequest(accessToken, commentIssueUrl, HttpMethods.POST, content);
    } catch (OAuth1HttpRequestException e) {
      if (e.getCode() == HttpStatus.NOT_FOUND.value()) {
        handleIssueNotFound(issueKey);
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

  private String retrieveComment(String comment) {
    try {
      JsonNode nodeComment = JsonUtils.readTree(comment);
      return nodeComment.path(BODY_PATH).asText();
    } catch (IOException e) {
      String message = MSG.getMessage(MALFORMED_COMMENT);
      String solution = MSG.getMessage(MALFORMED_COMMENT_SOLUTION);

      throw new InvalidJiraPayloadException(COMPONENT, message, solution);
    }
  }

  @Override
  protected String getServiceName() {
    return SERVICE_NAME;
  }

}
