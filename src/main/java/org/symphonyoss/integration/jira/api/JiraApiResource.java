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

import static org.symphonyoss.integration.jira.properties.ServiceProperties.APPLICATION_KEY_ERROR;
import static org.symphonyoss.integration.jira.properties.ServiceProperties.INVALID_URL_ERROR;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.symphonyoss.integration.authentication.api.jwt.JwtAuthentication;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Exception;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;
import org.symphonyoss.integration.jira.exception.InvalidJiraURLException;
import org.symphonyoss.integration.jira.exception.JiraAuthorizationException;
import org.symphonyoss.integration.jira.services.SearchAssignableUsersService;
import org.symphonyoss.integration.jira.services.UserAssignService;
import org.symphonyoss.integration.jira.webhook.JiraWebHookIntegration;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.ErrorResponse;

import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.MediaType;

/**
 * REST endpoint to handle requests for JIRA Api.
 *
 * Created by alexandre-silva-daitan on 08/08/17.
 */
@RestController
@RequestMapping("/v1/jira/rest/api")
public class JiraApiResource {

  private static final String INTEGRATION_UNAVAILABLE = "integration.web.integration.unavailable";

  private static final String INTEGRATION_UNAVAILABLE_SOLUTION =
      INTEGRATION_UNAVAILABLE + ".solution";

  private static final String INVALID_BASE_URL = "integration.jira.url.base.invalid";

  private static final String PATH_JIRA_API_SEARCH_USERS =
      "rest/api/latest/user/assignable/search?issueKey=%s&username=%s&maxResults=%s";

  private static final String PATH_JIRA_API_ASSIGN_ISSUE =
      "/rest/api/latest/issue/%s/assignee";

  private static final String COMPONENT = "JIRA API";

  private final JiraWebHookIntegration jiraWebHookIntegration;

  private final LogMessageSource logMessage;

  private final JwtAuthentication jwtAuthentication;

  private final UserAssignService userAssignService;

  private final SearchAssignableUsersService searchAssignableUsersService;

  private final Integer maxResults = new Integer(10);

  public JiraApiResource(JiraWebHookIntegration jiraWebHookIntegration,
      LogMessageSource logMessage, JwtAuthentication jwtAuthentication,
      UserAssignService userAssignService,
      SearchAssignableUsersService searchAssignableUsersService) {
    this.jiraWebHookIntegration = jiraWebHookIntegration;
    this.logMessage = logMessage;
    this.jwtAuthentication = jwtAuthentication;
    this.userAssignService = userAssignService;
    this.searchAssignableUsersService = searchAssignableUsersService;
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

    Long userId = jwtAuthentication.getUserIdFromAuthorizationHeader(authorizationHeader);

    validateIntegrationBootstrap();

    String accessToken;

    try {
      accessToken = jiraWebHookIntegration.getAccessToken(jiraIntegrationURL, userId);
    } catch (AuthorizationException e) {
      ErrorResponse response = new ErrorResponse(
          HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    if (accessToken == null || accessToken.isEmpty()) {
      ErrorResponse response = new ErrorResponse();
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setMessage(logMessage.getMessage(INVALID_BASE_URL, jiraIntegrationURL));
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    OAuth1Provider provider = getOAuth1Provider(jiraIntegrationURL);

    if (username == null) {
      username = StringUtils.EMPTY;
    }

    String pathApiJiraUsersSearch = String.format(PATH_JIRA_API_SEARCH_USERS, issueKey, username,
        maxResults);

    try {
      URL jiraBaseUrl = new URL(jiraIntegrationURL);
      URL assignableUserUrl = new URL(jiraBaseUrl, pathApiJiraUsersSearch);

      return searchAssignableUsersService.searchAssingablesUsers(accessToken, provider,
          assignableUserUrl, COMPONENT, issueKey);
    } catch (MalformedURLException e) {
      String errorMessage = logMessage.getMessage(INVALID_URL_ERROR, jiraIntegrationURL);
      throw new InvalidJiraURLException(errorMessage, e);
    }
  }

  private void validateIntegrationBootstrap() {
    // TODO
  }

  /**
   * Assigns an specific user to an specific Issue.
   * @param issueKey Issue identifier
   * @param username Assignee identifier
   * @return 200 - Returned if user was successfully assigned or 400 Bad Request - Returned if no
   * issue key was provided, 401 Unauthorized - Returned if the user is not authenticated ,
   * 404 Not Found - Returned if the requested user is not found.
   */
  @PutMapping("/issue/{issueIdOrKey}/assignee")
  public ResponseEntity assignIssueToUser(@RequestParam String issueKey,
      @RequestParam(value = "username", required = false) String username,
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
      @RequestParam(name = "url") String jiraIntegrationURL) {

    Long userId = jwtAuthentication.getUserIdFromAuthorizationHeader(authorizationHeader);

    String accessToken;

    try {
      accessToken = jiraWebHookIntegration.getAccessToken(jiraIntegrationURL, userId);
    } catch (AuthorizationException e) {
      ErrorResponse response = new ErrorResponse(
          HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    OAuth1Provider provider = getOAuth1Provider(jiraIntegrationURL);

    try {
      URL jiraBaseUrl = new URL(jiraIntegrationURL);
      URL userAssigneeUrl = new URL(jiraBaseUrl, String.format(PATH_JIRA_API_ASSIGN_ISSUE, issueKey));

      return userAssignService.assignUserToIssue(accessToken, issueKey, username, userAssigneeUrl,
          provider);
    } catch (MalformedURLException e) {
      String errorMessage = logMessage.getMessage(INVALID_URL_ERROR, jiraIntegrationURL);
      throw new InvalidJiraURLException(errorMessage, e);
    }
  }

  public OAuth1Provider getOAuth1Provider(@RequestParam(name = "url") String jiraIntegrationURL) {
    try {
      return jiraWebHookIntegration.getOAuth1Provider(jiraIntegrationURL);
    } catch (OAuth1Exception e) {
      throw new JiraAuthorizationException(COMPONENT,
          logMessage.getMessage(APPLICATION_KEY_ERROR), e);
    }
  }

}
