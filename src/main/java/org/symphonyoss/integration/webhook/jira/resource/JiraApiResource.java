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

package org.symphonyoss.integration.webhook.jira.resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.authentication.jwt.JwtAuthentication;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.AuthorizedIntegration;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.service.IntegrationBridge;
import org.symphonyoss.integration.web.exception.IntegrationUnavailableException;
import org.symphonyoss.integration.web.model.ErrorResponse;

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

  private final IntegrationBridge integrationBridge;

  private final LogMessageSource logMessage;

  private final JwtAuthentication jwtAuthentication;

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
  @GetMapping("/user/assignable/search")
  public ResponseEntity searchAssignableUsers(@RequestParam String issueKey,
      @RequestParam String username, @PathVariable String configurationId,
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
      @RequestParam(name = "url") String integrationURL) {
    //TODO 1- Acesstoken (se nao encontrado 401)

    Long userId = jwtAuthentication.getUserIdFromAuthorizationHeader(authorizationHeader);
    AuthorizedIntegration authIntegration = getAuthorizedIntegration(configurationId);

    try {
      String accessToken = authIntegration.getAccessToken(integrationURL, userId);
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


    //TODO 2- Validar paremtros de entrada (issueKey n pode ser vazio 400)
    if (issueKey.isEmpty()) {
      ErrorResponse response = new ErrorResponse();
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    //TODO 3- fazer requisicao pro JIRA
    //TODO 4- tratar retorno


    return ResponseEntity.ok().build();
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
