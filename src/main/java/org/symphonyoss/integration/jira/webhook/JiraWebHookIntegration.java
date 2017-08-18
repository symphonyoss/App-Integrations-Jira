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

package org.symphonyoss.integration.jira.webhook;

import static org.symphonyoss.integration.jira.api.JiraApiResourceConstants.BUNDLE_FILENAME;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.AuthorizationPayload;
import org.symphonyoss.integration.authorization.AuthorizedIntegration;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Exception;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1HttpRequestException;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;
import org.symphonyoss.integration.jira.authorization.JiraAuthorizationManager;
import org.symphonyoss.integration.jira.authorization.oauth.v1.JiraOAuth1Exception;
import org.symphonyoss.integration.jira.webhook.parser.JiraParserFactory;
import org.symphonyoss.integration.jira.webhook.parser.JiraParserResolver;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.logging.MessageUtils;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.model.yaml.AppAuthorizationModel;
import org.symphonyoss.integration.webhook.WebHookIntegration;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;
import org.symphonyoss.integration.webhook.parser.WebHookParser;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

/**
 * Implementation of a WebHook to integrate with JIRA, rendering it's messages.
 *
 * This integration class should support MessageML v1 and MessageML v2 according to the Agent
 * Version.
 *
 * There is a component {@link JiraParserResolver} responsible to identify the correct factory
 * should
 * be used to build the parsers according to the MessageML supported.
 *
 * Created by Milton Quilzini on 04/05/16.
 */
@Component
public class JiraWebHookIntegration extends WebHookIntegration implements AuthorizedIntegration {

  public static final String MSG_INSUFFICIENT_PARAMS =
      "integration.jira.authorize.insufficient.params";
  public static final String MSG_INSUFFICIENT_PARAMS_SOLUTION =
      MSG_INSUFFICIENT_PARAMS + ".solution";
  public static final String MSG_NO_INTEGRATION_FOUND =
      "integration.jira.authorize.no.integration.settings";
  public static final String MSG_NO_INTEGRATION_FOUND_SOLUTION =
      MSG_NO_INTEGRATION_FOUND + ".solution";

  public static final String OAUTH_TOKEN = "oauth_token";

  public static final String OAUTH_VERIFIER = "oauth_verifier";

  @Autowired
  private static final MessageUtils MSG = new MessageUtils(BUNDLE_FILENAME);

  @Autowired
  private JiraParserResolver parserResolver;

  @Autowired
  private List<JiraParserFactory> factories;

  @Autowired
  private JiraAuthorizationManager authManager;

  @Override
  public void onCreate(String integrationUser) {
    super.onCreate(integrationUser);

    registerApp(integrationUser);
  }

  /**
   * Callback to update the integration settings in the parser classes.
   * @param settings Integration settings
   */
  @Override
  public void onConfigChange(IntegrationSettings settings) {
    super.onConfigChange(settings);

    for (JiraParserFactory factory : factories) {
      factory.onConfigChange(settings);
    }
  }

  /**
   * Parse message received from JIRA according to the event type and MessageML version supported.
   * @param input Message received from JIRA
   * @return Message to be posted
   * @throws WebHookParseException Failure to parse the incoming payload
   */
  @Override
  public Message parse(WebHookPayload input) throws WebHookParseException {
    WebHookParser parser = parserResolver.getFactory().getParser(input);
    return parser.parse(input);
  }

  /**
   * @see WebHookIntegration#getSupportedContentTypes()
   */
  @Override
  public List<MediaType> getSupportedContentTypes() {
    List<MediaType> supportedContentTypes = new ArrayList<>();
    supportedContentTypes.add(MediaType.WILDCARD_TYPE);
    return supportedContentTypes;
  }

  /**
   * @see AuthorizedIntegration#getAuthorizationModel()
   */
  @Override
  public AppAuthorizationModel getAuthorizationModel() {
    IntegrationSettings settings = getSettings();
    if (settings != null) {
      return authManager.getAuthorizationModel(settings);
    }
    return null;
  }

  /**
   * @see AuthorizedIntegration#isUserAuthorized(String, Long)
   */
  @Override
  public boolean isUserAuthorized(String url, Long userId)
      throws AuthorizationException {
    IntegrationSettings settings = getSettings();
    if (settings != null) {
      return authManager.isUserAuthorized(settings, url, userId);
    }
    return false;
  }

  /**
   * @see AuthorizedIntegration#getAuthorizationUrl(String, Long)
   */
  @Override
  public String getAuthorizationUrl(String url, Long userId) throws AuthorizationException {
    IntegrationSettings settings = getSettings();
    if (settings != null) {
      return authManager.getAuthorizationUrl(settings, url, userId);
    }
    return null;
  }

  /**
   * @see AuthorizedIntegration#authorize(AuthorizationPayload)
   */
  @Override
  public void authorize(AuthorizationPayload authorizationPayload) throws AuthorizationException {
    String temporaryToken = authorizationPayload.getParameters().get(OAUTH_TOKEN);
    String verificationCode = authorizationPayload.getParameters().get(OAUTH_VERIFIER);

    if (StringUtils.isBlank(temporaryToken) || StringUtils.isBlank(verificationCode)) {
      throw new JiraOAuth1Exception(MSG.getMessage(MSG_INSUFFICIENT_PARAMS),
          MSG.getMessage(MSG_INSUFFICIENT_PARAMS_SOLUTION));
    }

    IntegrationSettings settings = getSettings();
    if (settings == null) {
      throw new JiraOAuth1Exception(MSG.getMessage(MSG_NO_INTEGRATION_FOUND),
          MSG.getMessage(MSG_NO_INTEGRATION_FOUND_SOLUTION));
    }

    authManager.authorizeTemporaryToken(settings, temporaryToken, verificationCode);
  }

  /**
   * @see AuthorizedIntegration#getAccessToken(String, Long)
   */
  @Override
  public String getAccessToken(String url, Long userId) throws AuthorizationException {
    IntegrationSettings settings = getSettings();
    if (settings != null) {
      return authManager.getAccessToken(settings, url, userId);
    }
    return null;
  }

  /**
   * Get an OAuth1 provider for user to perform calls to an external system resource.
   * @param url Integration URL.
   * @return JIRA OAuth1Provider.
   * @throws OAuth1Exception Thrown in any case of error.
   */
  public OAuth1Provider getOAuth1Provider(String url) throws OAuth1Exception {
    IntegrationSettings settings = getSettings();

    if (settings != null) {
      return authManager.getJiraOAuth1Provider(settings, url);
    }

    return null;
  }

}

