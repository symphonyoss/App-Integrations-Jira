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

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.service.IntegrationBridge;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.QueryParam;

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
  private static final String INTEGRATION_NOT_AUTH = "integration.web.integration.not.authorized";
  private static final String INTEGRATION_NOT_AUTH_SOLUTION =
      INTEGRATION_NOT_AUTH + ".solution";

  private final IntegrationBridge integrationBridge;

  private final LogMessageSource logMessage;

  public JiraApiResource(IntegrationBridge integrationBridge,
      LogMessageSource logMessage) {
    this.integrationBridge = integrationBridge;
    this.logMessage = logMessage;
  }

  /**
   * Get a list of potential assigneers users from an especific Issue.
   *
   * @param issueKey Issue identifier
   * @param username User that made a request from JIRA
   * @return List of potential assigneers users or 400 Bad Request - Returned if no issue key
   * was provided, 401 Unauthorized - Returned if the user is not authenticated ,
   * 404 Not Found - Returned if the requested user is not found.
   */
  @GetMapping("/user/assignable/search")
  public ResponseEntity searchAssignableUsers(@RequestParam String issueKey,
      @RequestParam String username) {
      return ResponseEntity.ok().build();
  }

}
