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

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.authentication.api.jwt.JwtAuthentication;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.AuthorizedIntegration;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Exception;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1HttpRequestException;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;
import org.symphonyoss.integration.exception.IntegrationRuntimeException;
import org.symphonyoss.integration.exception.IntegrationUnavailableException;
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

  private static final String PATH_JIRA_API_SEARCH_USERS =
      "rest/api/latest/user/assignable/search?issueKey=%s&username=%s&maxResults=%s";

  private static final String COMPONENT = "JIRA API";

  private final IntegrationBridge integrationBridge;

  private final LogMessageSource logMessage;

  private final JwtAuthentication jwtAuthentication;

  private final Integer maxResults = new Integer(10);

  public JiraApiResource(IntegrationBridge integrationBridge,
      LogMessageSource logMessage, JwtAuthentication jwtAuthentication) {
    this.integrationBridge = integrationBridge;
    this.logMessage = logMessage;
    this.jwtAuthentication = jwtAuthentication;
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
      if (accessToken.isEmpty()) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }
    } catch (AuthorizationException e) {
      ErrorResponse response = new ErrorResponse(
          HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }


    if (issueKey.isEmpty() || jiraIntegrationURL.isEmpty()) {
      ErrorResponse response = new ErrorResponse();
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    if(username == null){
      username = "";
    }

    HttpResponse response = null;
    String pahtApiJiraUsersSearch = String.format(PATH_JIRA_API_SEARCH_USERS, issueKey, username, maxResults);
    try {
      OAuth1Provider provider = authIntegration.getOAuth1Provider(jiraIntegrationURL);

      URL myselfUrl = new URL(jiraIntegrationURL);
      myselfUrl = new URL(myselfUrl, pahtApiJiraUsersSearch);
      response = provider.makeAuthorizedRequest(accessToken, myselfUrl, HttpMethods.GET, null);

    } catch (OAuth1Exception e) {
      throw new IntegrationRuntimeException(COMPONENT,
          logMessage.getMessage("integration.jira.private.key.validation"), e);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Invalid URL.", e);
    }

    return ResponseEntity.ok().body(response.parseAsString());

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
