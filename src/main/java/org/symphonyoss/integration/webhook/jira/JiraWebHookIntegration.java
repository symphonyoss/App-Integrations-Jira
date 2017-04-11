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

package org.symphonyoss.integration.webhook.jira;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.webhook.WebHookIntegration;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;
import org.symphonyoss.integration.webhook.jira.parser.JiraParser;
import org.symphonyoss.integration.webhook.jira.parser.JiraParserException;
import org.symphonyoss.integration.webhook.jira.parser.JiraParserResolver;
import org.symphonyoss.integration.webhook.jira.parser.ParserFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Implementation of a WebHook to integrate with JIRA, rendering it's messages.
 *
 * Created by Milton Quilzini on 04/05/16.
 */
@Component
public class JiraWebHookIntegration extends WebHookIntegration {

  @Autowired
  private JiraParserResolver parserResolver;

  @Autowired
  private List<ParserFactory> factories;

  @Override
  public void onConfigChange(IntegrationSettings settings) {
    super.onConfigChange(settings);

    for (ParserFactory factory : factories) {
      factory.onConfigChange(settings);
    }
  }

  @Override
  public Message parse(WebHookPayload input) throws WebHookParseException {
    try {
      JsonNode rootNode = JsonUtils.readTree(input.getBody());
      Map<String, String> parameters = input.getParameters();

      JiraParser parser = parserResolver.getFactory().getParser(rootNode);
      return parser.parse(parameters, rootNode);
    } catch (IOException e) {
      throw new JiraParserException("Something went wrong while trying to convert your message to the expected format", e);
    }
  }

}

