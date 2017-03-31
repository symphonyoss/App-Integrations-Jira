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

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.webhook.jira.parser.JiraParser;
import org.symphonyoss.integration.webhook.jira.parser.JiraParserException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Parser to skip incoming requests from JIRA
 * Created by rsanchez on 28/07/16.
 */
@Component
public class NullJiraParser implements JiraParser {

  @Override
  public List<String> getEvents() {
    return Collections.emptyList();
  }

  @Override
  public void setIntegrationUser(String integrationUser) {
    // Do nothing
  }

  @Override
  public Message parse(Map<String, String> parameters, JsonNode node) throws JiraParserException {
    return null;
  }

}
