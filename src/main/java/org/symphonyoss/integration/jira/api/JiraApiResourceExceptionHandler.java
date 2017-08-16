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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.symphonyoss.integration.exception.IntegrationUnavailableException;
import org.symphonyoss.integration.jira.exception.InvalidJiraURLException;
import org.symphonyoss.integration.jira.exception.JiraAuthorizationException;
import org.symphonyoss.integration.model.ErrorResponse;

/**
 * Class responsble to handler and treat JiraApiResource Excetions
 * Created by hamitay on 8/15/17.
 */

@ControllerAdvice
public class JiraApiResourceExceptionHandler {

  @ResponseBody
  @ExceptionHandler(IntegrationUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleIntegrationUnavailableException(IntegrationUnavailableException ex) {
    ErrorResponse response =
        new ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
  }

  @ResponseBody
  @ExceptionHandler(InvalidJiraURLException.class)
  public ResponseEntity<ErrorResponse> handleInvalidJiraURLException(InvalidJiraURLException ex) {
    ErrorResponse response =
        new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ResponseBody
  @ExceptionHandler(JiraAuthorizationException.class)
  public ResponseEntity<ErrorResponse> handleJiraAuthorizationException(JiraAuthorizationException ex) {
    ErrorResponse response =
        new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }


}

