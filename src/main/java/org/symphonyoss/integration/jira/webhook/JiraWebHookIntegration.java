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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.AuthorizationPayload;
import org.symphonyoss.integration.authorization.AuthorizedIntegration;
import org.symphonyoss.integration.jira.authorization.JiraAuthorizationManager;
import org.symphonyoss.integration.jira.authorization.oauth.v1.JiraOAuth1Exception;
import org.symphonyoss.integration.jira.webhook.parser.JiraParserFactory;
import org.symphonyoss.integration.jira.webhook.parser.JiraParserResolver;
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
 * This integration class should support MessageML v1 and MessageML v2 according to the Agent Version.
 *
 * There is a component {@link JiraParserResolver} responsible to identify the correct factory should
 * be used to build the parsers according to the MessageML supported.
 *
 * Created by Milton Quilzini on 04/05/16.
 */
@Component
public class JiraWebHookIntegration extends WebHookIntegration implements AuthorizedIntegration {

  @Autowired
  private JiraParserResolver parserResolver;

  @Autowired
  private List<JiraParserFactory> factories;

  @Autowired
  private JiraAuthorizationManager authManager;

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

  @Override
  public AppAuthorizationModel getAuthorizationModel() {
    IntegrationSettings settings = getSettings();
    if (settings != null) {
      return authManager.getAuthorizationModel(settings);
    }
    return null;
  }

  @Override
  public boolean isUserAuthorized(String url, Long userId) throws AuthorizationException {
    IntegrationSettings settings = getSettings();
    if (settings != null) {
      return authManager.isUserAuthorized(settings, url, userId);
    }
    return false;
  }

  @Override
  public String getAuthorizationUrl(String url, Long userId) throws AuthorizationException {
    IntegrationSettings settings = getSettings();
    if (settings != null) {
      return authManager.getAuthorizationUrl(settings, url, userId);
    }
    return null;
  }

  @Override
  public void authorize(AuthorizationPayload authorizationPayload) throws AuthorizationException {
    String temporaryToken = authorizationPayload.getParameters().get("oauth_token");
    String verificationCode = authorizationPayload.getParameters().get("oauth_verifier");

    if (StringUtils.isBlank(temporaryToken) || StringUtils.isBlank(verificationCode)) {
      throw new JiraOAuth1Exception("Insufficient parameters.",
          "Both parameters 'oauth_token' and 'oauth_verifier' must be informed.");
    }

    IntegrationSettings settings = getSettings();
    if (settings == null) {
      throw new JiraOAuth1Exception("Integration settings not found.",
          "Check the parameters used to load the integration settings file.");
    }

    String configurationId = settings.getConfigurationId();
    authManager.authorizeTemporaryToken(settings, temporaryToken, verificationCode);
  }
}

