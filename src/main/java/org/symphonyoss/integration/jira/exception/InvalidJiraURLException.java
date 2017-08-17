package org.symphonyoss.integration.jira.exception;

import org.symphonyoss.integration.exception.IntegrationRuntimeException;

/**
 * Unchecked exception thrown to indicate that invalid jira URL was passed.
 *
 * Created by hamitay on 8/16/17.
 */
public class InvalidJiraURLException extends IntegrationRuntimeException {

  public InvalidJiraURLException(String component, String message, Throwable cause) {
    super(component, message, cause);
  }

}
