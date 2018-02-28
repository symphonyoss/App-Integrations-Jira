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

package org.symphonyoss.integration.jira.webhook.parser.v2;

import static org.symphonyoss.integration.jira.webhook.JiraEventConstants.JIRA_COMMENT_ADDED;
import static org.symphonyoss.integration.jira.webhook.JiraEventConstants.JIRA_COMMENT_DELETED;
import static org.symphonyoss.integration.jira.webhook.JiraEventConstants.JIRA_COMMENT_UPDATED;
import static org.symphonyoss.integration.jira.webhook.JiraEventConstants.WEBHOOK_EVENT;
import static org.symphonyoss.integration.jira.webhook.JiraParserConstants.ACTION_ENTITY_FIELD;
import static org.symphonyoss.integration.jira.webhook.JiraParserConstants.BODY_PATH;
import static org.symphonyoss.integration.jira.webhook.JiraParserConstants.COMMENT_PATH;
import static org.symphonyoss.integration.jira.webhook.JiraParserConstants.VISIBILITY_PATH;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.jira.webhook.parser.JiraParserException;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.service.UserService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible to validate the event 'jira:issue_updated' and event types
 * 'issue_commented', 'issue_comment_edited' and 'issue_comment_deleted' sent by JIRA Webhook when
 * the Agent version is equal to or greater than '1.46.0'.
 *
 * Created by aurojr on 25/04/17.
 */
@Component
public class CommentMetadataParser extends JiraMetadataParser {

  private static final String METADATA_FILE = "metadataIssueCommented.xml";
  private static final String TEMPLATE_FILE = "templateIssueCommented.xml";

  private final Map<String, String> actions = new HashMap<>();


  @Autowired
  public CommentMetadataParser(UserService userService,
      IntegrationProperties integrationProperties) {
    super(userService, integrationProperties);

    actions.put(JIRA_COMMENT_ADDED, "Commented");
    actions.put(JIRA_COMMENT_UPDATED, "Edited Comment");
    actions.put(JIRA_COMMENT_DELETED, "Deleted Comment");
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
    return Arrays.asList(JIRA_COMMENT_ADDED, JIRA_COMMENT_DELETED, JIRA_COMMENT_UPDATED);
  }

  @Override
  public Message parse(Map<String, String> parameters, JsonNode node) throws JiraParserException {
    // restricted comments are not supported, therefore no message should be sent
    return !isCommentRestricted(node) ? super.parse(parameters, node) : null;
  }

  @Override
  protected void preProcessInputData(JsonNode input) {
    super.preProcessInputData(input);
    processCommentAction(input);
    processCommentBody(input);
  }

  /**
   * This method adds an action field to the metadata json with a text indicating the performed
   * comment action (add, edit, delete)
   * @param input The root json node
   */
  private void processCommentAction(JsonNode input) {
    String webHookEvent = input.path(WEBHOOK_EVENT).asText();
    ObjectNode commentNode = (ObjectNode) input.with(COMMENT_PATH);
    if (commentNode != null) {
      commentNode.put(ACTION_ENTITY_FIELD, actions.get(webHookEvent));
    }
  }

  /**
   * This searches through the comment body and replaces
   * @param input
   */
  private void processCommentBody(JsonNode input) {
    ObjectNode commentNode = getCommentNode(input);

    if (commentNode != null) {
      String comment = formatTextContent(commentNode.path(BODY_PATH).asText(), true).toString();
      commentNode.put(BODY_PATH, comment);
    }
  }

  private ObjectNode getCommentNode(JsonNode input) {
    return input.hasNonNull(COMMENT_PATH) ? (ObjectNode) input.path(COMMENT_PATH) : null;
  }

  /**
   * JIRA comments may be restricted to certain user groups on JIRA. This is indicated by the
   * presence of a "visibility" attribute on the comment. Thus, this method will deem a comment as
   * restricted if the "visibility" attribute is present, regardless of its content, as it is not
   * possible to evaluate the visibility restriction on JIRA against the rooms the webhook will post
   * to.
   * @param node JIRA root node.
   * @return Indication on whether the comment is restricted or not.
   */
  private boolean isCommentRestricted(JsonNode node) {
    return node.path(COMMENT_PATH).has(VISIBILITY_PATH);
  }
}
