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
import org.symphonyoss.integration.exception.IntegrationRuntimeException;
import org.symphonyoss.integration.jira.exception.BodyContentNotFoundException;
import org.symphonyoss.integration.jira.exception.InvalidJiraURLException;
import org.symphonyoss.integration.jira.exception.IssueKeyNotFoundException;
import org.symphonyoss.integration.jira.exception.JiraAuthorizationException;
import org.symphonyoss.integration.jira.exception.JiraUnexpectedException;
import org.symphonyoss.integration.jira.exception.JiraUserNotFoundException;
import org.symphonyoss.integration.model.ErrorResponse;

/**
 * Global exception handler for JIRA.
 *
 * Created by hamitay on 8/15/17.
 */
@ControllerAdvice
public class JiraApiResourceExceptionHandler {

  @ResponseBody
  @ExceptionHandler({ InvalidJiraURLException.class, JiraUserNotFoundException.class,
      BodyContentNotFoundException.class})
  public ResponseEntity<ErrorResponse> handleBadRequest(IntegrationRuntimeException ex) {
    ErrorResponse response =
        new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ResponseBody
  @ExceptionHandler({IssueKeyNotFoundException.class})
  public ResponseEntity<ErrorResponse> handleNotFound(IssueKeyNotFoundException ex) {
    ErrorResponse response =
        new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ResponseBody
  @ExceptionHandler(JiraAuthorizationException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorized(JiraAuthorizationException ex) {
    ErrorResponse response =
        new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ResponseBody
  @ExceptionHandler(JiraUnexpectedException.class)
  public ResponseEntity<ErrorResponse> handleInternalServerError(JiraUnexpectedException ex) {
    ErrorResponse response =
        new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

}

