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

package org.symphonyoss.integration.webhook.jira.parser;

import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.ISSUE_EVENT_TYPE_NAME;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.WEBHOOK_EVENT;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.message.MessageMLVersion;
import org.symphonyoss.integration.webhook.jira.parser.v1.NullJiraParser;
import org.symphonyoss.integration.webhook.jira.parser.v1.V1JiraParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

/**
 * Parser factory for the MessageML v1.
 * Created by rsanchez on 21/03/17.
 */
@Component
public class V1ParserFactory implements ParserFactory {

  private Map<String, V1JiraParser> parsers = new HashMap<>();

  @Autowired
  private NullJiraParser defaultJiraParser;

  @Autowired
  private List<V1JiraParser> beans;

  @PostConstruct
  public void init() {
    for (V1JiraParser parser : beans) {
      List<String> events = parser.getEvents();
      for (String eventType : events) {
        this.parsers.put(eventType, parser);
      }
    }
  }

  @Override
  public boolean accept(MessageMLVersion version) {
    return MessageMLVersion.V1.equals(version);
  }

  @Override
  public void onConfigChange(IntegrationSettings settings) {
    String jiraUser = settings.getType();

    for (V1JiraParser parser : beans) {
      parser.setJiraUser(jiraUser);
    }
  }

  @Override
  public JiraParser getParser(JsonNode node) {
    String webHookEvent = node.path(WEBHOOK_EVENT).asText();
    String eventTypeName = node.path(ISSUE_EVENT_TYPE_NAME).asText();

    JiraParser result = parsers.get(eventTypeName);

    if (result == null) {
      result = parsers.get(webHookEvent);
    }

    if (result == null) {
      return defaultJiraParser;
    }

    return result;
  }

}
