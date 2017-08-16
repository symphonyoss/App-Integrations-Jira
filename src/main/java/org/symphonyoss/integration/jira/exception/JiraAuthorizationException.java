package org.symphonyoss.integration.jira.exception;

import org.symphonyoss.integration.exception.IntegrationRuntimeException;

/**
 * Created by hamitay on 8/16/17.
 */
public class JiraAuthorizationException extends IntegrationRuntimeException {

  public JiraAuthorizationException(String component, String message, Throwable cause) {
    super(component, message, cause);
  }
}
