package org.symphonyoss.integration.jira.exception;

import org.symphonyoss.integration.exception.IntegrationRuntimeException;

/**
 * Unchecked exception thrown to indicate that invalid jira URL was passed.
 *
 * Created by hamitay on 8/16/17.
 */
public class InvalidJiraURLException extends IntegrationRuntimeException {

  public static final String COMPONET = "Invalid Jira URL";

  public InvalidJiraURLException(String message, Throwable cause) {
    super(COMPONET, message, cause);
  }

}
