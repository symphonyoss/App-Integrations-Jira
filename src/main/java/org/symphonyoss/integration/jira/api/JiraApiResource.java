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
import static org.symphonyoss.integration.jira.properties.ServiceProperties.EMPTY_ACCESS_TOKEN;
import static org.symphonyoss.integration.jira.properties.ServiceProperties.INVALID_BASE_URL;
import static org.symphonyoss.integration.jira.properties.ServiceProperties
    .INVALID_BASE_URL_SOLUTION;
import static org.symphonyoss.integration.jira.properties.ServiceProperties.INVALID_URL_ERROR;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.authentication.api.jwt.JwtAuthentication;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.AuthorizedIntegration;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Exception;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1HttpRequestException;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;
import org.symphonyoss.integration.exception.IntegrationRuntimeException;
import org.symphonyoss.integration.exception.IntegrationUnavailableException;
import org.symphonyoss.integration.jira.service.SearchAssignableUsersService;
import org.symphonyoss.integration.jira.services.UserAssignService;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.ErrorResponse;
import org.symphonyoss.integration.service.IntegrationBridge;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.MediaType;

/**
 * REST endpoint to handle requests for JIRA Api.
 *
 * Created by alexandre-silva-daitan on 08/08/17.
 */
@RestController
@RequestMapping("/v1/{configurationId}/rest/api")
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

  private final IntegrationBridge integrationBridge;

  private final LogMessageSource logMessage;

  private final JwtAuthentication jwtAuthentication;

  private final UserAssignService userAssignService;

  private final SearchAssignableUsersService searchAssignableUsersService;

  private final Integer maxResults = new Integer(10);

  public JiraApiResource(IntegrationBridge integrationBridge,
      LogMessageSource logMessage, JwtAuthentication jwtAuthentication,
      UserAssignService userAssignService,
      SearchAssignableUsersService searchAssignableUsersService) {
    this.integrationBridge = integrationBridge;
    this.logMessage = logMessage;
    this.jwtAuthentication = jwtAuthentication;
    this.userAssignService = userAssignService;
    this.searchAssignableUsersService = searchAssignableUsersService;
  }

  /**
   * Get a list of potential assigneers users from an especific Issue.
   * @param issueKey Issue identifier
   * @param username User that made a request from JIRA
   * @return List of potential assigneers users or 400 Bad Request - Returned if no issue key
   * was provided, 401 Unauthorized - Returned if the user is not authenticated ,
   * 404 Not Found - Returned if the requested user is not found.
   */
  @GetMapping(value = "/user/assignable/search", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity searchAssignableUsers(@RequestParam String issueKey,
      @RequestParam(required = false) String username, @PathVariable String configurationId,
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
      @RequestParam(name = "url") String jiraIntegrationURL)
      throws IOException {

    Long userId = jwtAuthentication.getUserIdFromAuthorizationHeader(authorizationHeader);
    AuthorizedIntegration authIntegration = getAuthorizedIntegration(configurationId);
    String accessToken;
    try {
      accessToken = authIntegration.getAccessToken(jiraIntegrationURL, userId);
      if (accessToken == null || accessToken.isEmpty()) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setMessage(logMessage.getMessage(INVALID_BASE_URL, jiraIntegrationURL));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }
    } catch (AuthorizationException e) {
      ErrorResponse response = new ErrorResponse(
          HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    OAuth1Provider provider = getOAuth1Provider(jiraIntegrationURL, authIntegration);

    if (issueKey.isEmpty() || jiraIntegrationURL.isEmpty()) {
      ErrorResponse response = new ErrorResponse();
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    if (username == null) {
      username = "";
    }

    String pahtApiJiraUsersSearch =
        String.format(PATH_JIRA_API_SEARCH_USERS, issueKey, username, maxResults);
    URL myselfUrl;
    try {
      myselfUrl = new URL(jiraIntegrationURL);
      myselfUrl = new URL(myselfUrl, pahtApiJiraUsersSearch);
    } catch (MalformedURLException e) {
      String errorMessage = logMessage.getMessage(INVALID_URL_ERROR, jiraIntegrationURL);
      throw new RuntimeException(errorMessage, e);
    }

    return searchAssignableUsersService.searchAssingablesUsers(accessToken, provider, myselfUrl,
        COMPONENT);

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
      @PathVariable String configurationId,
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
      @RequestParam(name = "url") String jiraIntegrationURL) throws IOException {

    //AccessToken
    Long userId = jwtAuthentication.getUserIdFromAuthorizationHeader(authorizationHeader);
    AuthorizedIntegration authIntegration = getAuthorizedIntegration(configurationId);
    String accessToken;

    try {
      accessToken = authIntegration.getAccessToken(jiraIntegrationURL, userId);
      if (accessToken.isEmpty()) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setMessage(logMessage.getMessage(EMPTY_ACCESS_TOKEN));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }
    } catch (AuthorizationException e) {
      ErrorResponse response =
          new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    OAuth1Provider provider = getOAuth1Provider(jiraIntegrationURL, authIntegration);

    //Build the URL
    URL userAssigneeUrl;
    try {
      userAssigneeUrl = new URL(jiraIntegrationURL);
      userAssigneeUrl =
          new URL(userAssigneeUrl, String.format(PATH_JIRA_API_ASSIGN_ISSUE, issueKey));
    } catch (MalformedURLException e) {
      String errorMessage = logMessage.getMessage(INVALID_URL_ERROR, jiraIntegrationURL);
      throw new RuntimeException(errorMessage, e);
    }

    return userAssignService.assignUserToIssue(accessToken, issueKey, username, userAssigneeUrl,
        provider);
  }

  public OAuth1Provider getOAuth1Provider(@RequestParam(name = "url") String jiraIntegrationURL,
      AuthorizedIntegration authIntegration) {
    OAuth1Provider provider = null;
    try {
      provider = authIntegration.getOAuth1Provider(jiraIntegrationURL);
    } catch (OAuth1Exception e) {
      throw new IntegrationRuntimeException(COMPONENT,
          logMessage.getMessage(APPLICATION_KEY_ERROR), e);
    }
    return provider;
  }

  /**
   * Get an AuthorizedIntegration based on a configuraton ID.
   * @param configurationId Configuration ID used to retrieve the AuthorizedIntegration.
   * @return AuthorizedIntegration found or an IntegrationUnavailableException if it was not
   * found or is invalid.
   */
  private AuthorizedIntegration getAuthorizedIntegration(@PathVariable String configurationId) {
    Integration integration = this.integrationBridge.getIntegrationById(configurationId);
    if (integration == null) {
      throw new IntegrationUnavailableException(
          logMessage.getMessage(INTEGRATION_UNAVAILABLE, configurationId),
          logMessage.getMessage(INTEGRATION_UNAVAILABLE_SOLUTION));
    }
    return (AuthorizedIntegration) integration;
  }

}
