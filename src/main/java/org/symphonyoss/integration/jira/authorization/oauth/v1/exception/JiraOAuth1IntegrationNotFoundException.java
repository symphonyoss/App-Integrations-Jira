package org.symphonyoss.integration.jira.authorization.oauth.v1.exception;

import org.symphonyoss.integration.authorization.oauth.v1.OAuth1IntegrationNotFoundException;

/**
 * Created by hamitay on 17/10/17.
 */
public class JiraOAuth1IntegrationNotFoundException extends OAuth1IntegrationNotFoundException {

  public JiraOAuth1IntegrationNotFoundException(String message, Throwable cause,
      String... solutions) {
    super(message, cause, solutions);
  }

  public JiraOAuth1IntegrationNotFoundException(String message, String... solutions) {
    super(message, solutions);
  }
}
