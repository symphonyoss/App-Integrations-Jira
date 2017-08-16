package org.symphonyoss.integration.jira.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.symphonyoss.integration.exception.IntegrationRuntimeException;
import org.symphonyoss.integration.exception.IntegrationUnavailableException;
import org.symphonyoss.integration.model.ErrorResponse;

/**
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
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
    ErrorResponse response =
        new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ResponseBody
  @ExceptionHandler(IntegrationRuntimeException.class)
  public ResponseEntity<ErrorResponse> handleIntegrationRuntimeException(IntegrationRuntimeException ex) {
    ErrorResponse response =
        new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }


}

