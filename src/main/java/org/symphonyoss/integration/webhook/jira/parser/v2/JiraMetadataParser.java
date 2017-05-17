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
import static org.symphonyoss.integration.entity.model.EntityConstants.USER_ID;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ASSIGNEE_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.CHANGELOG_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.DESCRIPTION_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.DISPLAY_NAME_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.EMAIL_ADDRESS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.EPIC_LINK_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.EPIC_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.FIELDS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.FIELD_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ICONURL_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ICON_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ISSUETYPE_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ISSUE_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ITEMS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.KEY_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.LABELS_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.LABELS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.LINK_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.NAME_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.PRIORITY_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.SELF_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.STATUS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.SUMMARY_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.TEXT_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.TOSTRING_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.URL_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.USERNAME_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.USER_PATH;
import static org.symphonyoss.integration.webhook.jira.parser.v1.IssueJiraParser.UNASSIGNED;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.parser.ParserUtils;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.service.UserService;
import org.symphonyoss.integration.webhook.jira.parser.JiraParser;
import org.symphonyoss.integration.webhook.jira.parser.JiraParserException;
import org.symphonyoss.integration.webhook.jira.parser.v1.JiraParserUtils;
import org.symphonyoss.integration.webhook.parser.metadata.EntityObject;
import org.symphonyoss.integration.webhook.parser.metadata.MetadataParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Abstract JIRA parser responsible to augment the JIRA input data querying the user API and
 * pre-processing the input data.
 *
 * Created by rsanchez on 10/04/17.
 */
public abstract class JiraMetadataParser extends MetadataParser implements JiraParser {

  private static final Logger logger = LoggerFactory.getLogger(JiraMetadataParser.class);

  private static final String LABELS_TYPE = "com.symphony.integration.jira.label";
  private static final String INTEGRATION_NAME = "jira";
  private static final String IMG_SUBPATH = "img";
  private static final String JIRA_LOGO_PNG = "jira_logo_rounded.png";

  private UserService userService;

  private IntegrationProperties integrationProperties;

  private String integrationUser;

  @Autowired
  public JiraMetadataParser(UserService userService, IntegrationProperties integrationProperties) {
    this.userService = userService;
    this.integrationProperties = integrationProperties;
  }

  @Override
  public void setIntegrationUser(String integrationUser) {
    this.integrationUser = integrationUser;
  }

  @Override
  public Message parse(Map<String, String> parameters, JsonNode node) throws JiraParserException {
    return parse(node);
  }

  @Override
  protected void preProcessInputData(JsonNode input) {
    processIconUrl(input);
    processIssueLink(input);
    processSummary(input);
    processDescription(input);
    processStatus(input);
    processUser(input);
    processAssignee(input);
    processEpicLink(input);
    processIconUrls(input);
  }

  /**
   * This method parses the issue description to avoid invalid characters
   * @param input
   */
  private void processSummary(JsonNode input) {
    JsonNode fieldsNode = input.path(ISSUE_PATH).path(FIELDS_PATH);
    JsonNode summaryNode = fieldsNode.path(SUMMARY_PATH);

    if (summaryNode != null) {
      SafeString summary = ParserUtils.escapeAndAddLineBreaks(summaryNode.asText());
      summary.replaceLineBreaks();
      ((ObjectNode) fieldsNode).put(SUMMARY_PATH, summary.toString());
    }
  }

  /**
   * Retrieve the jira logo absolute path to be used in the template sponsor container
   * @param input root json node
   */
  private void processIconUrl(JsonNode input) {
    String baseUrl = integrationProperties.getApplicationUrl(INTEGRATION_NAME);
    String iconUrl = StringUtils.EMPTY;

    if (!StringUtils.isEmpty(baseUrl)) {
      String urlFormat = "%s/%s/%s";
      iconUrl = String.format(urlFormat, baseUrl, IMG_SUBPATH, JIRA_LOGO_PNG);
    }

    ObjectNode iconNode = (ObjectNode) input.with(ICON_PATH);
    iconNode.put(URL_PATH, iconUrl);
  }

  @Override
  protected void postProcessOutputData(EntityObject output, JsonNode input) {
    includeLabels(output, input);
  }

  /**
   * This method change the issue status to uppercase.
   * @param input JSON input payload
   */
  private void processStatus(JsonNode input) {
    JsonNode statusNode = input.path(ISSUE_PATH).path(FIELDS_PATH).path(STATUS_PATH);

    String issueStatus = statusNode.path(NAME_PATH).asText(EMPTY);

    if (StringUtils.isNotEmpty(issueStatus)) {
      ((ObjectNode) statusNode).put(NAME_PATH, issueStatus.toUpperCase());
    }
  }

  /**
   * Retrieves the issue link from JIRA payload and creates a new field named 'link'.
   * @param input JSON input data
   */
  private void processIssueLink(JsonNode input) {
    ObjectNode issueNode = (ObjectNode) input.path(ISSUE_PATH);

    String linkedIssueField = getLinkedIssueField(issueNode);

    issueNode.put(LINK_ENTITY_FIELD, linkedIssueField);
  }

  /**
   * Return the URL from JIRA payload
   * @param issueNode the issue node of a JIRA payload
   * @return
   */
  protected String getLinkedIssueField(JsonNode issueNode) {
    String selfPath = issueNode.path(SELF_PATH).asText();
    String issueKey = issueNode.path(KEY_PATH).asText();

    return getLinkedIssueField(selfPath, issueKey);
  }

  /**
   * Return the URL from JIRA payload
   * @param selfPath Issue URL
   * @param key Issue Key
   * @return Browse issue URL or null if the selfPath is and invalid URL
   */
  protected String getLinkedIssueField(String selfPath, String key) {
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
   * @param input JSON input data
   */
  private void processDescription(JsonNode input) {
    ObjectNode fieldsNode = (ObjectNode) input.path(ISSUE_PATH).path(FIELDS_PATH);
    String description = fieldsNode.path(DESCRIPTION_PATH).asText(EMPTY);

    if (StringUtils.isNotEmpty(description)) {
      description = JiraParserUtils.stripJiraFormatting(description);
      SafeString safeDescription = ParserUtils.escapeAndAddLineBreaks(description);
      fieldsNode.put(DESCRIPTION_PATH, safeDescription.toString());
    }
  }

  /**
   * Augment user information.
   * @param input JSON input payload
   */
  private void processUser(JsonNode input) {
    // Get user that performs the action
    ObjectNode userNode = (ObjectNode) input.path(USER_PATH);
    augmentUserInformation(userNode);
  }

  /**
   * Augment user assignee information.
   * @param input JSON input payload
   */
  private void processAssignee(JsonNode input) {
    // Get user assignee
    ObjectNode fieldsNode = (ObjectNode) input.path(ISSUE_PATH).path(FIELDS_PATH);
    String assignee = fieldsNode.path(ASSIGNEE_PATH).path(DISPLAY_NAME_PATH).asText();

    if (!StringUtils.isEmpty(assignee)) {
      ObjectNode assigneeNode = (ObjectNode) fieldsNode.path(ASSIGNEE_PATH);
      augmentUserInformation(assigneeNode);
    } else {
      ObjectNode assigneeNode = fieldsNode.putObject(ASSIGNEE_PATH);
      assigneeNode.put(DISPLAY_NAME_PATH, UNASSIGNED);
    }
  }

  /**
   * Queries the user API using email address to get more information about the user.
   * @param userNode JSON node that contains user information provided by JIRA.
   */
  private void augmentUserInformation(ObjectNode userNode) {
    JsonNode emailAddressNode = userNode.path(EMAIL_ADDRESS_PATH);

    User user = userService.getUserByEmail(integrationUser, emailAddressNode.asText(EMPTY).trim());

    if ((user != null) && (user.getId() != null)) {
      userNode.put(USER_ID, user.getId());
      userNode.put(EMAIL_ADDRESS_PATH, user.getEmailAddress());
      userNode.put(USERNAME_PATH, user.getUsername());
      userNode.put(DISPLAY_NAME_PATH, user.getDisplayName());
    }
  }

  /**
   * Process custom field 'Epic'.
   *
   * If the JSON input payload have epic custom field this method should include the epic name and
   * epic link to be displayed by the renderer.
   * @param input JSON input payload
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
   * Returns epic name from a custom field.
   * @param input JSON input payload
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

  /**
   * Perform any needed processing on the icon urls
   * @param input
   */
  private void processIconUrls(JsonNode input) {
    JsonNode fieldsPath = input.path(ISSUE_PATH).path(FIELDS_PATH);

    if (fieldsPath != null) {
      formatIconUrl((ObjectNode) fieldsPath.path(ISSUETYPE_PATH));
      formatIconUrl((ObjectNode) fieldsPath.path(PRIORITY_PATH));
    }
  }

  /**
   * Since the & is not supported by MessageML - since it's expected to be a special character code
   * - this method will replace all & occurrences by &amp;
   * @param node
   */
  private void formatIconUrl(ObjectNode node) {
    if (node != null) {
      String iconUrl = node.path(ICONURL_PATH).asText();
      if (!StringUtils.isEmpty(iconUrl)) {
        iconUrl = iconUrl.replace("&", "&amp;");
        node.put(ICONURL_PATH, iconUrl);
      }
    }
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
      nestedObject.addContent(TEXT_ENTITY_FIELD,
          ParserUtils.escapeAndAddLineBreaks(label).toString());

      list.add(nestedObject);
    }

    outputIssue.addContent(LABELS_ENTITY_FIELD, list);
  }

  /**
   * Returns the user e-mail if it exists, null otherwise.
   * @param userKey the user key
   * @return the user e-mail if it exists, null otherwise.
   */
  protected User getUserByUserName(String userKey) {
    if (StringUtils.isEmpty(userKey)) {
      return null;
    }

    User user = userService.getUserByUserName(integrationUser, userKey);
    if (user == null || user.getId() == null) {
      logger.warn("User for " + userKey + " not found");
      return null;
    }

    return user;
  }

}
