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

import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.ISSUE_EVENT_TYPE_NAME;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.JIRA_ISSUE_COMMENTED;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants
    .JIRA_ISSUE_COMMENT_DELETED;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.JIRA_ISSUE_COMMENT_EDITED;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ACTION_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.BODY_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.COMMENT_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ID_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ISSUE_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.LINK_ENTITY_FIELD;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.service.UserService;
import org.symphonyoss.integration.webhook.jira.parser.v1.JiraParserUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible to validate the event 'jira:issue_created' sent by JIRA Webhook when
 * the Agent version is equal to or greater than '1.46.0'.
 *
 * Created by aurojr on 25/04/17.
 */
@Component
public class CommentMetadataParser extends JiraMetadataParser {

  private static final Pattern userCommentPattern = Pattern.compile("(\\[~)([\\w.]+)(])");
  private static final String METADATA_FILE = "metadataIssueCommented.xml";
  private static final String TEMPLATE_FILE = "templateIssueCommented.xml";
  private static final String COMMENT_LINK_SUFFIX =
      "focusedCommentId=%s&amp;page=com.atlassian.jira.plugin.system"
          + ".issuetabpanels%%3Acomment-tabpanel#comment-%s";

  private final Map<String, String> actions = new HashMap<>();


  @Autowired
  public CommentMetadataParser(UserService userService) {
    super(userService);

    actions.put(JIRA_ISSUE_COMMENTED, "Commented");
    actions.put(JIRA_ISSUE_COMMENT_EDITED, "Edited a comment");
    actions.put(JIRA_ISSUE_COMMENT_DELETED, "Deleted a comment");
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
    return Arrays.asList(JIRA_ISSUE_COMMENTED, JIRA_ISSUE_COMMENT_DELETED,
        JIRA_ISSUE_COMMENT_EDITED);
  }

  @Override
  protected void preProcessInputData(JsonNode input) {
    super.preProcessInputData(input);
    processCommentLink(input);
    processCommentAction(input);
    processCommentMentions(input);
  }

  /**
   * This method adds an action field to the metadata json with a text indicating the performed
   * comment action (add, edit, delete)
   * @param input The root json node
   */
  private void processCommentAction(JsonNode input) {
    String webHookEvent = input.path(ISSUE_EVENT_TYPE_NAME).asText();
    ObjectNode commentNode = getCommentNode(input);
    if (commentNode != null) {
      commentNode.put(ACTION_ENTITY_FIELD, actions.get(webHookEvent));
    }
  }

  /**
   * This method changes the self link sent by Jira into a common comment Jira url
   * @param input The root json node
   */
  private void processCommentLink(JsonNode input) {
    ObjectNode commentNode = getCommentNode(input);

    if (commentNode != null) {
      JsonNode issueNode = input.path(ISSUE_PATH);
      String linkedIssueField = getLinkedIssueField(issueNode);
      String linkedCommentLink = getLinkedCommentLink(linkedIssueField, issueNode);
      commentNode.put(LINK_ENTITY_FIELD, linkedCommentLink);
    }
  }

  /**
   * This method builds the comment permalink according to the issue link and the comment node,
   * i.e., the given issue link must be built based on the issue related to the given comment node
   * @param issueLink
   * @param commentNode
   * @return
   */
  private String getLinkedCommentLink(String issueLink, JsonNode commentNode) {
    String commentId = commentNode.path(ID_PATH).asText();
    StringBuilder commentLink = new StringBuilder(issueLink);

    if (!StringUtils.isEmpty(commentId)) {
      commentLink.append("?");
      commentLink.append(String.format(COMMENT_LINK_SUFFIX, commentId, commentId));
    }

    return commentLink.toString();
  }

  /**
   * This searches through the comment body and replaces
   * @param input
   */
  private void processCommentMentions(JsonNode input) {
    ObjectNode commentNode = getCommentNode(input);

    if (commentNode != null) {
      String comment = commentNode.path(BODY_PATH).asText();

      Map<String, User> userMentions = determineUserMentions(comment);
      if (userMentions != null && !userMentions.isEmpty()) {
        for (Map.Entry<String, User> userEntry : userMentions.entrySet()) {
          User user = userEntry.getValue();

          //FIXME Agent v3 doen't support tags inside metadata
//          String userMention =
//              ParserUtils.presentationFormat(MESSAGEML_MENTION_EMAIL_FORMAT, user
// .getEmailAddress())
//                  .toString();
          String userMention = user.getDisplayName();
          comment = comment.replaceAll("\\[~" + userEntry.getKey() + "]", userMention);
        }
      }

      comment = JiraParserUtils.stripJiraFormatting(comment);
      commentNode.put(BODY_PATH, comment);
    }
  }

  private ObjectNode getCommentNode(JsonNode input) {
    return input.hasNonNull(COMMENT_PATH) ? (ObjectNode) input.path(COMMENT_PATH) : null;
  }

  private Map<String, User> determineUserMentions(String comment) {
    Set<String> userMentions = new HashSet<>();
    Map<String, User> usersToMention = new HashMap<>();
    Matcher matcher = userCommentPattern.matcher(comment);
    while (matcher.find()) {
      userMentions.add(matcher.group(2));
    }
    for (String userName : userMentions) {
      User user = getUserByUserName(userName);
      if (user != null && StringUtils.isNotEmpty(user.getEmailAddress())) {
        usersToMention.put(userName, user);
      }
    }
    return usersToMention;
  }
}
