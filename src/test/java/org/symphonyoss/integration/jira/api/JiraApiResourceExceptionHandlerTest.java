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

import static org.junit.Assert.assertEquals;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys.COMPONENT;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.symphonyoss.integration.jira.exception.InvalidJiraURLException;
import org.symphonyoss.integration.jira.exception.IssueKeyNotFoundException;
import org.symphonyoss.integration.jira.exception.JiraAuthorizationException;
import org.symphonyoss.integration.jira.exception.JiraUnexpectedException;
import org.symphonyoss.integration.model.ErrorResponse;

/**
 * Unit tests for {@link JiraApiResourceExceptionHandler}
 * Created by rsanchez on 18/08/17.
 */
public class JiraApiResourceExceptionHandlerTest {

  private JiraApiResourceExceptionHandler exceptionHandler = new JiraApiResourceExceptionHandler();

  @Test
  public void testBadRequest() {
    InvalidJiraURLException ex = new InvalidJiraURLException(COMPONENT, "Invalid JIRA Base URL",
        new RuntimeException());

    ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());

    ResponseEntity<ErrorResponse> errorResponse = exceptionHandler.handleBadRequest(ex);

    assertEquals(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response), errorResponse);
  }

  @Test
  public void testNotFound() {
    IssueKeyNotFoundException ex = new IssueKeyNotFoundException(COMPONENT, "Invalid key");

    ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());

    ResponseEntity<ErrorResponse> errorResponse = exceptionHandler.handleNotFound(ex);

    assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response), errorResponse);
  }

  @Test
  public void testUnauthorized() {
    JiraAuthorizationException ex = new JiraAuthorizationException(COMPONENT, "Unauthorized user");

    ErrorResponse response = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());

    ResponseEntity<ErrorResponse> errorResponse = exceptionHandler.handleUnauthorized(ex);

    assertEquals(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response), errorResponse);
  }

  @Test
  public void testInternalServerError() {
    JiraUnexpectedException ex =
        new JiraUnexpectedException(COMPONENT, "Unexpected error", new RuntimeException());

    ErrorResponse response = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());

    ResponseEntity<ErrorResponse> errorResponse = exceptionHandler.handleInternalServerError(ex);

    assertEquals(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response), errorResponse);
  }

}
