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

package org.symphonyoss.integration.jira.webhook.parser;

import com.fasterxml.jackson.databind.JsonNode;
import org.symphonyoss.integration.model.message.Message;

import java.util.List;
import java.util.Map;

/**
 * Interface that defines methods to validate JIRA messages
 * Created by rsanchez on 17/05/16.
 */
public interface JiraParser {

  String UNASSIGNED = "Unassigned";

  /**
   * Retrieve a list of events supported by the parser class.
   * @return Events supported by the parser class
   */
  List<String> getEvents();

  /**
   * Update the integration username.
   * @param integrationUser Integration username
   */
  void setIntegrationUser(String integrationUser);

  /**
   * Process event received from JIRA.
   * @param parameters HTTP parameters
   * @param node JSON payload
   * @return Message to be posted
   * @throws JiraParserException Failure to parse the event
   */
  Message parse(Map<String, String> parameters, JsonNode node) throws JiraParserException;

}
