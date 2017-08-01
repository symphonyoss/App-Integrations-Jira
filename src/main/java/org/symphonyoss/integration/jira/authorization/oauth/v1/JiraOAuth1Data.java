package org.symphonyoss.integration.jira.authorization.oauth.v1;

/**
 * Wraps all information about a OAuth Session, user and instance/integration URL.
 * Created by campidelli on 25-jul-17.
 */
public class JiraOAuth1Data {

  private String temporaryToken;
  private String accessToken;

  public JiraOAuth1Data(String temporaryToken) {
    this.temporaryToken = temporaryToken;
  }

  public JiraOAuth1Data(String temporaryToken, String accessToken) {
    this.temporaryToken = temporaryToken;
    this.accessToken = accessToken;
  }

  public String getTemporaryToken() {
    return temporaryToken;
  }

  public void setTemporaryToken(String temporaryToken) {
    this.temporaryToken = temporaryToken;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
}
