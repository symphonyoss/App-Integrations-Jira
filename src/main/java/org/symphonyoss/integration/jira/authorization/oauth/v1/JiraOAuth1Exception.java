package org.symphonyoss.integration.jira.authorization.oauth.v1;

import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Exception;

/**
 * Exception used to inform runtime exceptions regarding OAuth1 process in the JIRA integration.
 *
 * Created by campidelli on 7/25/17.
 */
public class JiraOAuth1Exception extends OAuth1Exception {

  public JiraOAuth1Exception(String message, Throwable cause, String... solutions) {
    super(message, cause, solutions);
  }

  public JiraOAuth1Exception(String message, String... solutions) {
    super(message, solutions);
  }

  public JiraOAuth1Exception(String message, int code,
      String... solutions) {
    super(message, code, solutions);
  }
}

