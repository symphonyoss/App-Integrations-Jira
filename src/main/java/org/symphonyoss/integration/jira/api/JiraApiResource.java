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

package org.symphonyoss.integration.jira.api;

import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys
    .APPLICATION_KEY_ERROR;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys.BUNDLE_FILENAME;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys.COMPONENT;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys.EMPTY_ACCESS_TOKEN;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys
    .EMPTY_ACCESS_TOKEN_SOLUTION;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys
    .INTEGRATION_UNAVAILABLE;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys
    .INTEGRATION_UNAVAILABLE_SOLUTION;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys
    .REQUIRED_PAYLOAD_NOT_FOUND;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys
    .REQUIRED_PAYLOAD_NOT_FOUND_SOLUTION;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.symphonyoss.integration.authentication.api.jwt.JwtAuthentication;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;
import org.symphonyoss.integration.exception.IntegrationUnavailableException;
import org.symphonyoss.integration.jira.exception.JiraAuthorizationException;
import org.symphonyoss.integration.jira.exception.JiraUnexpectedException;
import org.symphonyoss.integration.jira.exception.MissingRequiredPayloadException;
import org.symphonyoss.integration.jira.services.IssueCommentService;
import org.symphonyoss.integration.jira.services.SearchAssignableUsersService;
import org.symphonyoss.integration.jira.services.UserAssignService;
import org.symphonyoss.integration.jira.webhook.JiraWebHookIntegration;
import org.symphonyoss.integration.logging.MessageUtils;

import javax.ws.rs.core.MediaType;

/**
 * REST endpoint to handle requests for JIRA API.
 *
 * Created by alexandre-silva-daitan on 08/08/17.
 */
@RestController
@RequestMapping("/v1/jira/rest/api")
public class JiraApiResource {

  private static final MessageUtils MSG = new MessageUtils(BUNDLE_FILENAME);

  private final JiraWebHookIntegration jiraWebHookIntegration;

  private final JwtAuthentication jwtAuthentication;

  private final UserAssignService userAssignService;

  private final SearchAssignableUsersService searchAssignableUsersService;

  private final IssueCommentService issueCommentService;

  public JiraApiResource(JiraWebHookIntegration jiraWebHookIntegration,
      JwtAuthentication jwtAuthentication, UserAssignService userAssignService,
      SearchAssignableUsersService searchAssignableUsersService,
      IssueCommentService issueCommentService) {
    this.jiraWebHookIntegration = jiraWebHookIntegration;
    this.jwtAuthentication = jwtAuthentication;
    this.userAssignService = userAssignService;
    this.searchAssignableUsersService = searchAssignableUsersService;
    this.issueCommentService = issueCommentService;
  }

  /**
   * Get a list of potential assignable users from a specific issue.
   * @param issueKey Issue identifier
   * @param username The username you want to query from JIRA
   * @return List of potential assigneers users or 400 Bad Request - Returned if no issue key
   * was provided, 401 Unauthorized - Returned if the user is not authenticated ,
   * 404 Not Found - Returned if the requested user is not found.
   */
  @GetMapping(value = "/user/assignable/search", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity searchAssignableUsers(@RequestParam String issueKey,
      @RequestParam(required = false) String username,
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
      @RequestParam(name = "url") String jiraIntegrationURL) {
    validateIntegrationBootstrap();

    String configurationId = jiraWebHookIntegration.getSettings().getConfigurationId();
    Long userId = jwtAuthentication.getUserIdFromAuthorizationHeader(configurationId, authorizationHeader);

    if (username == null) {
      username = StringUtils.EMPTY;
    }

    String accessToken = getAccessToken(jiraIntegrationURL, userId);

    OAuth1Provider provider = getOAuth1Provider(jiraIntegrationURL);

    return searchAssignableUsersService.searchAssingablesUsers(accessToken, provider,
        jiraIntegrationURL, issueKey, username);
  }

  /**
   * Assigns an specific user to an specific Issue.
   * @param issueKey Issue identifier
   * @param username Assignee identifier
   * @return 200 - Returned if user was successfully assigned or 400 Bad Request - Returned if no
   * issue key was provided, 401 Unauthorized - Returned if the user is not authenticated ,
   * 404 Not Found - Returned if the requested user is not found.
   */
  @PutMapping("/issue/{issueKey}/assignee")
  public ResponseEntity assignIssueToUser(@PathVariable String issueKey,
      @RequestParam(value = "username", required = false) String username,
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
      @RequestParam(name = "url") String jiraIntegrationURL) {

    validateIntegrationBootstrap();

    String configurationId = jiraWebHookIntegration.getSettings().getConfigurationId();
    Long userId = jwtAuthentication.getUserIdFromAuthorizationHeader(configurationId,
        authorizationHeader);

    String accessToken = getAccessToken(jiraIntegrationURL, userId);

    OAuth1Provider provider = getOAuth1Provider(jiraIntegrationURL);

    return userAssignService.assignUserToIssue(accessToken, issueKey, username, jiraIntegrationURL, provider);
  }

  /**
   * Adds new comments on issue.
   *
   * @param issueKey Issue identifier
   * @return 200 OK - Returned if add was successful or 400 Bad Request - Returned if the input
   * is invalid (e.g. missing required fields, invalid values, and so forth), 401 Unauthorized -
   * Returned if the user is not authenticated, 404 Not Found - Returned if the issue key is not
   * found.
   */
  @PostMapping(value = "/issue/{issueKey}/comment")
  public ResponseEntity addCommentToAnIssue(@RequestBody(required = false) String comment,
      @PathVariable String issueKey,
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
      @RequestParam(name = "url") String jiraIntegrationURL) {
    validateIntegrationBootstrap();

    String configurationId = jiraWebHookIntegration.getSettings().getConfigurationId();
    Long userId = jwtAuthentication.getUserIdFromAuthorizationHeader(
        configurationId, authorizationHeader);

    String accessToken = getAccessToken(jiraIntegrationURL, userId);

    OAuth1Provider provider = getOAuth1Provider(jiraIntegrationURL);

    validateRequiredPayload(comment);

    return issueCommentService.addCommentToAnIssue(accessToken, issueKey, jiraIntegrationURL,
        provider, comment);
  }

  /**
   * Check if the integration have already bootstrapped successfully.
   */
  private void validateIntegrationBootstrap() {
    if (jiraWebHookIntegration.getSettings() == null) {
      throw new IntegrationUnavailableException(COMPONENT,
          MSG.getMessage(INTEGRATION_UNAVAILABLE),
          MSG.getMessage(INTEGRATION_UNAVAILABLE_SOLUTION));
    }
  }

  /**
   * Retrieves user access token.
   *
   * @param baseURL JIRA base URL
   * @param userId User identifier
   * @return User access token
   */
  private String getAccessToken(String baseURL, Long userId) {
    try {
      String accessToken = jiraWebHookIntegration.getAccessToken(baseURL, userId);

      if (accessToken == null || accessToken.isEmpty()) {
        throw new JiraAuthorizationException(COMPONENT, MSG.getMessage(EMPTY_ACCESS_TOKEN),
            MSG.getMessage(EMPTY_ACCESS_TOKEN_SOLUTION, baseURL));
      }

      return accessToken;
    } catch (AuthorizationException e) {
      throw new JiraUnexpectedException(COMPONENT, e.getMessage(), e);
    }
  }

  /**
   * Retrieves authentication provider
   */
  private OAuth1Provider getOAuth1Provider(String jiraIntegrationURL) {
    try {
      return jiraWebHookIntegration.getOAuth1Provider(jiraIntegrationURL);
    } catch (AuthorizationException e) {
      throw new JiraAuthorizationException(COMPONENT, MSG.getMessage(APPLICATION_KEY_ERROR), e);
    }
  }

  /**
   * Validate the required payload.
   *
   * @param payload Payload to be analyzed
   */
  private void validateRequiredPayload(String payload) {
    if (StringUtils.isEmpty(payload)) {
      String message = MSG.getMessage(REQUIRED_PAYLOAD_NOT_FOUND);
      String solution = MSG.getMessage(REQUIRED_PAYLOAD_NOT_FOUND_SOLUTION);

      throw new MissingRequiredPayloadException(COMPONENT, message, solution);
    }
  }

}
