package org.symphonyoss.integration.jira.authorization.oauth.v1.exception;

import org.symphonyoss.integration.authorization.oauth.v1.OAuth1MissingParametersException;

/**
 * Created by hamitay on 17/10/17.
 */
public class JiraOAuth1MissingParametersException extends OAuth1MissingParametersException {

  public JiraOAuth1MissingParametersException(String message, Throwable cause,
      String... solutions) {
    super(message, cause, solutions);
  }

  public JiraOAuth1MissingParametersException(String message, String... solutions) {
    super(message, solutions);
  }
}