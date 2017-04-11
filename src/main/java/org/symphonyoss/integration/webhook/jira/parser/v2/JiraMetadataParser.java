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

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.symphonyoss.integration.entity.model.EntityConstants.DISPLAY_NAME_ENTITY_FIELD;
import static org.symphonyoss.integration.entity.model.EntityConstants.EMAIL_ADDRESS_ENTITY_FIELD;
import static org.symphonyoss.integration.entity.model.EntityConstants.USERNAME_ENTITY_FIELD;
import static org.symphonyoss.integration.entity.model.EntityConstants.USER_ENTITY_FIELD;
import static org.symphonyoss.integration.entity.model.EntityConstants.USER_ID;
import static org.symphonyoss.integration.parser.ParserUtils.MESSAGEML_LINEBREAK;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ASSIGNEE_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.CHANGELOG_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.DESCRIPTION_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.DISPLAY_NAME_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.EPIC_LINK_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.EPIC_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.FIELDS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.FIELD_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ISSUE_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ITEMS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.KEY_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.LABELS_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.LABELS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.LINK_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.NAME_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.SELF_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.TEXT_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.TOSTRING_PATH;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.service.UserService;
import org.symphonyoss.integration.webhook.jira.parser.v1.JiraParserUtils;
import org.symphonyoss.integration.webhook.jira.parser.v2.model.EntityObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract JIRA parser responsible to augment the JIRA input data querying the user API and
 * pre-processing the input data.
 *
 * Created by rsanchez on 10/04/17.
 */
public abstract class JiraMetadataParser extends MetadataParser {

  private static final String LABELS_TYPE = "com.symphony.integration.jira.label";

  private UserService userService;

  @Autowired
  public JiraMetadataParser(UserService userService) {
    this.userService = userService;
  }

  @Override
  protected void preProcessInputData(JsonNode input) {
    processIssueLink(input);
    processDescription(input);
    processUser(input);
    processAssignee(input);
    processEpicLink(input);
  }

  /**
   * Process issue link.
   *
   * @param input JSON input data
   */
  private void processIssueLink(JsonNode input) {
    ObjectNode issueNode = (ObjectNode) input.path(ISSUE_PATH);

    String selfPath = issueNode.path(SELF_PATH).asText();
    String issueKey = issueNode.path(KEY_PATH).asText();

    String linkedIssueField = getLinkedIssueField(selfPath, issueKey);

    issueNode.put(LINK_ENTITY_FIELD, linkedIssueField);
  }

  /**
   * Return the URL from jira's json
   * @param selfPath Issue URL
   * @param key Issue Key
   * @return Browse issue URL or null if the selfPath is and invalid URL
   */
  private String getLinkedIssueField(String selfPath, String key) {
    if (StringUtils.isEmpty(selfPath) || StringUtils.isEmpty(key)) {
      return EMPTY;
    }

    try {
      URL url = new URL(selfPath);

      StringBuilder issueUrl = new StringBuilder();

      issueUrl.append(url.getProtocol());
      issueUrl.append("://");
      issueUrl.append(url.getHost());

      if (url.getPort() != -1) {
        issueUrl.append(":");
        issueUrl.append(url.getPort());
      }

      issueUrl.append("/browse/");
      issueUrl.append(key);

      return issueUrl.toString();
    } catch (MalformedURLException e) {
      // if the url is not valid, will only mention the issue key on a comment.
      return EMPTY;
    }
  }

  /**
   * Process issue description removing the JIRA formatting and replacing line break to <br/>
   * tags. It also escapes special characters.
   *
   * @param input JSON input data
   */
  private void processDescription(JsonNode input) {
    ObjectNode fieldsNode = (ObjectNode) input.path(ISSUE_PATH).path(FIELDS_PATH);
    String description = fieldsNode.path(DESCRIPTION_PATH).asText(EMPTY);

    if (StringUtils.isNotEmpty(description)) {
      String formattedValue =
          JiraParserUtils.stripJiraFormatting(description).replaceAll("\n", MESSAGEML_LINEBREAK);
      fieldsNode.put(DESCRIPTION_PATH, StringEscapeUtils.escapeXml10(formattedValue));
    }
  }

  /**
   * Process user information.
   * @param input JSON input data
   */
  private void processUser(JsonNode input) {
    // Get user that performs the action
    ObjectNode userNode = (ObjectNode) input.path(USER_ENTITY_FIELD);
    augmentUserInformation(userNode);
  }

  /**
   * Process user assignee information.
   * @param input JSON input data
   */
  private void processAssignee(JsonNode input) {
    // Get user assignee
    ObjectNode assigneeNode = (ObjectNode) input.path(ISSUE_PATH).path(FIELDS_PATH).path(ASSIGNEE_PATH);
    String assignee = assigneeNode.path(DISPLAY_NAME_PATH).asText();

    if (!StringUtils.isEmpty(assignee)) {
      augmentUserInformation(assigneeNode);
    }
  }

  /**
   * Queries the user API to get more information about the user.
   * @param userNode JSON node that contains user information provided by JIRA.
   */
  private void augmentUserInformation(ObjectNode userNode) {
    JsonNode emailAddressNode = userNode.path(EMAIL_ADDRESS_ENTITY_FIELD);

    User user = userService.getUserByEmail(integrationUser, emailAddressNode.asText(EMPTY).trim());

    if ((user != null) && (user.getId() != null)) {
      userNode.put(USER_ID, user.getId());
      userNode.put(EMAIL_ADDRESS_ENTITY_FIELD, user.getEmailAddress());
      userNode.put(USERNAME_ENTITY_FIELD, user.getUsername());
      userNode.put(DISPLAY_NAME_ENTITY_FIELD, user.getDisplayName());
    }
  }

  /**
   * Process optional field 'Epic'
   * @param input JSON input data
   */
  private void processEpicLink(JsonNode input) {
    String epic = getEpicName(input);

    if (StringUtils.isNotEmpty(epic)) {
      ObjectNode issueNode = (ObjectNode) input.path(ISSUE_PATH);

      ObjectNode epicNode = issueNode.putObject(EPIC_PATH);
      epicNode.put(NAME_PATH, epic);

      String selfPath = issueNode.path(SELF_PATH).asText();
      String epicLink = getLinkedIssueField(selfPath, epic);

      if (StringUtils.isNotEmpty(epicLink)) {
        epicNode.put(LINK_ENTITY_FIELD, epicLink);
      }
    }
  }

  /**
   * Returns epic name
   * @param input JSON input data
   * @return Epic name or empty string
   */
  private String getEpicName(JsonNode input) {
    JsonNode items = input.path(CHANGELOG_PATH).path(ITEMS_PATH);
    if (items.size() == 0) {
      return EMPTY;
    }

    for (int i = 0; i < items.size(); i++) {
      JsonNode item = items.get(i);
      String field = item.get(FIELD_PATH).asText();
      if (EPIC_LINK_PATH.equals(field)) {
        return item.get(TOSTRING_PATH).asText();
      }
    }

    return EMPTY;
  }

  @Override
  protected void postProcessOutputData(EntityObject output, JsonNode input) {
    includeLabels(output, input);
  }

  /**
   * Augment the output entity JSON with the JIRA labels.
   * @param output Output Entity JSON
   * @param input JSON input data
   */
  private void includeLabels(EntityObject output, JsonNode input) {
    EntityObject outputIssue = (EntityObject) output.getContent().get(ISSUE_PATH);

    JsonNode labelsNode = input.path(ISSUE_PATH).path(FIELDS_PATH).path(LABELS_PATH);

    if (labelsNode.size() == 0) {
      return;
    }

    List<EntityObject> list = new ArrayList<>();

    for (int i = 0; i < labelsNode.size(); i++) {
      String name = labelsNode.get(i).asText();
      String label = name.replaceAll("#", "");

      EntityObject nestedObject = new EntityObject(LABELS_TYPE, getVersion());
      nestedObject.addContent(TEXT_ENTITY_FIELD, label);

      list.add(nestedObject);
    }

    outputIssue.addContent(LABELS_ENTITY_FIELD, list);
  }

}
