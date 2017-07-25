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

package org.symphonyoss.integration.jira.webhook.parser.v1;

import static org.symphonyoss.integration.messageml.MessageMLFormatConstants.MESSAGEML_END;
import static org.symphonyoss.integration.messageml.MessageMLFormatConstants.MESSAGEML_START;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.symphonyoss.integration.jira.webhook.parser.JiraParser;
import org.symphonyoss.integration.jira.webhook.parser.JiraParserException;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.model.message.MessageMLVersion;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Abstract class that contains the commons methods required by all the MessageML v1 parsers.
 *
 * Created by rsanchez on 25/07/16.
 */
public class CommonJiraParser implements JiraParser {

  protected String jiraUser;

  @Override
  public List<String> getEvents() {
    return Collections.emptyList();
  }

  @Override
  public void setIntegrationUser(String jiraUser) {
    this.jiraUser = jiraUser;
  }

  @Override
  public Message parse(Map<String, String> parameters, JsonNode node) throws JiraParserException {
    String formattedMessage = getMessage(parameters, node);

    if (StringUtils.isNotEmpty(formattedMessage)) {
      String messageML = MESSAGEML_START + formattedMessage + MESSAGEML_END;

      Message message = new Message();
      message.setFormat(Message.FormatEnum.MESSAGEML);
      message.setMessage(messageML);
      message.setVersion(MessageMLVersion.V1);

      return message;
    }

    return null;
  }

  protected String getMessage(Map<String, String> parameters, JsonNode node) throws JiraParserException {
    return null;
  }

}
