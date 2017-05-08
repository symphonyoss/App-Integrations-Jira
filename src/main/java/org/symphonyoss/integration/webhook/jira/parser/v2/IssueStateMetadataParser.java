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

package org.symphonyoss.integration.webhook.jira.parser.v2;

import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.JIRA_ISSUE_CREATED;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.JIRA_ISSUE_UPDATED;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.WEBHOOK_EVENT;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ACTION_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ISSUE_PATH;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.service.UserService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible to validate the event 'jira:issue_created' sent by JIRA Webhook when
 * the Agent version is equal to or greater than '1.46.0'.
 *
 * Created by rsanchez on 30/03/17.
 */
@Component
public class IssueStateMetadataParser extends JiraMetadataParser {

  private static final String METADATA_FILE = "metadataIssueState.xml";

  private static final String TEMPLATE_FILE = "templateIssueState.xml";

  private final Map<String, String> actions = new HashMap<>();

  @Autowired
  public IssueStateMetadataParser(UserService userService) {
    super(userService);

    actions.put(JIRA_ISSUE_CREATED, "Created");
    actions.put(JIRA_ISSUE_UPDATED, "Updated");
  }

  @Override
  protected String getTemplateFile() {
    return TEMPLATE_FILE;
  }

  @Override
  protected String getMetadataFile() {
    return METADATA_FILE;
  }

  @Override
  public List<String> getEvents() {
    return Arrays.asList(JIRA_ISSUE_CREATED, JIRA_ISSUE_UPDATED);
  }

  @Override
  protected void preProcessInputData(JsonNode input) {
    super.preProcessInputData(input);
    processIssueAction(input);
  }

  /**
   * This method adds an action field to the metadata json with a text indicating the performed
   * action (create, update)
   * @param input The root json node
   */
  private void processIssueAction(JsonNode input) {
    String webHookEvent = input.path(WEBHOOK_EVENT).asText();
    ObjectNode issueNode = (ObjectNode) input.with(ISSUE_PATH);
    issueNode.put(ACTION_ENTITY_FIELD, actions.get(webHookEvent));
  }
}
