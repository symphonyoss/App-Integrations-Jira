package org.symphonyoss.integration.jira.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.exception.IntegrationRuntimeException;
import org.symphonyoss.integration.model.ErrorResponse;

/**
 * Created by hamitay on 8/15/17.
 */

@ControllerAdvice
public class JiraApiResourceExceptionHandler {

  @ResponseBody
  @ExceptionHandler(AuthorizationException.class)
  public ResponseEntity<ErrorResponse> handleIntegrationRuntimeException(IntegrationRuntimeException e) {
    ErrorResponse response =
        new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }



}

