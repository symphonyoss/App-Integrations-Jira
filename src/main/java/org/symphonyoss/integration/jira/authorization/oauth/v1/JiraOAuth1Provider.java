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

package org.symphonyoss.integration.jira.authorization.oauth.v1;

import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Implementation of the abstract class {@link OAuth1Provider} by Jira.
 * Created by campidelli on 26-jul-17.
 */
public class JiraOAuth1Provider extends OAuth1Provider {

  private static final String REQUEST_TEMPORARY_TOKEN_PATH = "/plugins/servlet/oauth/request-token";
  private static final String AUTHORIZE_TEMPORARY_TOKEN_PATH = "/plugins/servlet/oauth/request-token";
  private static final String REQUEST_ACCESS_TOKEN_PATH = "/plugins/servlet/oauth/request-token";

  private String consumerKey;
  private String privateKey;
  private URL baseUrl;
  private URL authorizationCallbackUrl;
  private URL requestTemporaryTokenUrl;
  private URL authorizeTemporaryTokenUrl;
  private URL requestAccessTokenUrl;

  public JiraOAuth1Provider(String consumerKey, String privateKey, String baseUrl,
      String authorizationCallbackUrl) {

    this.consumerKey = consumerKey;
    this.privateKey = privateKey;

    try {
      this.baseUrl = new URL(baseUrl);
      requestTemporaryTokenUrl = new URL(this.baseUrl, REQUEST_TEMPORARY_TOKEN_PATH);
      authorizeTemporaryTokenUrl = new URL(this.baseUrl, AUTHORIZE_TEMPORARY_TOKEN_PATH);
      requestAccessTokenUrl = new URL(this.baseUrl, REQUEST_ACCESS_TOKEN_PATH);
    } catch (MalformedURLException e) {
      throw new JiraOAuth1Exception("Invalid base URL.", e, "Inform a valid base URL.");
    }

    try {
      this.authorizationCallbackUrl = new URL(authorizationCallbackUrl);
    } catch (MalformedURLException e) {
      throw new JiraOAuth1Exception("Invalid authorization callback URL.",
          e, "Inform a valid authorization callback URL.");
    }
  }

  @Override
  public String getConsumerKey() {
    return consumerKey;
  }

  @Override
  public String getPrivateKey() {
    return privateKey;
  }

  @Override
  public URL getAuthorizationCallbackUrl() {
    return authorizationCallbackUrl;
  }

  @Override
  public URL getRequestTemporaryTokenUrl() {
    return requestTemporaryTokenUrl;
  }

  @Override
  public URL getAuthorizeTemporaryTokenUrl() {
    return authorizeTemporaryTokenUrl;
  }

  @Override
  public URL getRequestAccessTokenUrl() {
    return requestAccessTokenUrl;
  }
}
