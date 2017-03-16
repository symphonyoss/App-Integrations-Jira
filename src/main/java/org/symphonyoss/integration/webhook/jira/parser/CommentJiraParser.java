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

import static org.symphonyoss.integration.messageml.MessageMLFormatConstants.MESSAGEML_MENTION_EMAIL_FORMAT;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.ISSUE_EVENT_TYPE_NAME;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.JIRA_ISSUE_COMMENTED;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.JIRA_ISSUE_COMMENT_DELETED;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.JIRA_ISSUE_COMMENT_EDITED;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.AUTHOR_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.AUTHOR_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.BODY_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.COMMENT_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.COMMENT_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.DISPLAY_NAME_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.JIRA;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.UPDATE_AUTHOR_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.VISIBILITY_PATH;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.exception.EntityXMLGeneratorException;
import org.symphonyoss.integration.parser.ParserUtils;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.parser.SafeStringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the event issue_commented.
 *
 * Created by mquilzini on 17/05/16.
 */
@Component
public class CommentJiraParser extends IssueJiraParser implements JiraParser {

  /**
   * Formatted message expected by user
   */
  public static final String INFO_BLOCK_FORMATTED_TEXT = "%s<br/>Comment: %s";
  /**
   * Formatted message expected by user
   */
  public static final String INFO_BLOCK_WITHOUT_COMMENT_FORMATTED_TEXT = "%s";

  /**
   * Action labels
   */
  private static final Map<String, String> actions = new HashMap<>();

  private static final Pattern userCommentPattern = Pattern.compile("(\\[\\~)([\\w\\.]+)(])");
  public static final String MENTION_MARKUP = "[~%s]";

  public CommentJiraParser() {
    actions.put(JIRA_ISSUE_COMMENTED, "commented on");
    actions.put(JIRA_ISSUE_COMMENT_EDITED, "edited a comment on");
    actions.put(JIRA_ISSUE_COMMENT_DELETED, "deleted a comment on");
  }

  @Override
  public List<String> getEvents() {
    return Arrays.asList(JIRA_ISSUE_COMMENTED, JIRA_ISSUE_COMMENT_EDITED,
        JIRA_ISSUE_COMMENT_DELETED);
  }

  @Override
  public String parse(Map<String, String> parameters, JsonNode node) throws JiraParserException {
    if (isCommentRestricted(node)) {
      return null;
    } else {
      String webHookEvent = node.path(ISSUE_EVENT_TYPE_NAME).asText();
      return getEntityML(node, webHookEvent);
    }
  }

  /**
   * JIRA comments may be restricted to certain user groups on JIRA. This is indicated by the presence of a "visibility"
   * attribute on the comment. This method will deem a comment as restricted if the "visibility" attribute is present,
   * regardless of its content, as it is not possible to evaluate the visibility restriction on JIRA against the rooms
   * the webhook will post to.
   *
   * @param node JIRA payload.
   * @return Indication on whether the comment is restricted or not.
   */
  private boolean isCommentRestricted(JsonNode node) {
    return node.path(COMMENT_PATH).has(VISIBILITY_PATH);
  }

  private String getEntityML(JsonNode node, String webHookEvent) {
    EntityBuilder builder = createBasicEntityBuilder(node, webHookEvent);
    EntityBuilder issueBuilder = createBasicIssueEntityBuilder(node);
    EntityBuilder commentBuilder = EntityBuilder.forNestedEntity(JIRA, COMMENT_ENTITY_FIELD);
    String comment = getOptionalField(node, COMMENT_PATH, BODY_PATH, "");

    SafeString safeComment = SafeString.EMPTY_SAFE_STRING;
    SafeString safeCommentPresentationML = SafeString.EMPTY_SAFE_STRING;

    if(StringUtils.isNotEmpty(comment)){
      Map<String, User> usersToMention = determineUserMentions(comment);
      comment = JiraParserUtils.stripJiraFormatting(comment);

      safeComment = new SafeString(comment);
      safeCommentPresentationML = new SafeString(comment);

      if(usersToMention != null && !usersToMention.isEmpty()){
        int count = 0;
        for (Map.Entry<String, User> userToMention : usersToMention.entrySet()) {
          User user = userToMention.getValue();

          safeComment.safeReplace(new SafeString(userToMention.getKey()),
              ParserUtils.presentationFormat(MENTION_MARKUP, user.getUsername()));

          safeCommentPresentationML.safeReplace(new SafeString(userToMention.getKey()),
              ParserUtils.presentationFormat(MESSAGEML_MENTION_EMAIL_FORMAT, user.getEmailAddress()));

          Entity mentionEntity = user.toEntity(JIRA, String.valueOf(count++));
          commentBuilder.nestedEntity(mentionEntity);
        }

        safeComment.replaceLineBreaks();
        safeCommentPresentationML.replaceLineBreaks();
      }
    }

    commentBuilder.attribute(COMMENT_ENTITY_FIELD, safeComment);
    SafeString presentationML = getPresentationML(node, webHookEvent, safeCommentPresentationML);


    Entity commentEntity = commentBuilder.build();

    if (JIRA_ISSUE_COMMENTED.equals(webHookEvent)) {
      issueBuilder.nestedEntity(commentEntity).attribute(AUTHOR_ENTITY_FIELD, getCommentDisplayName(node, AUTHOR_PATH));
    } else if (JIRA_ISSUE_COMMENT_EDITED.equals(webHookEvent)) {
      issueBuilder.nestedEntity(commentEntity).attribute(AUTHOR_ENTITY_FIELD, getCommentDisplayName(node, UPDATE_AUTHOR_PATH));
    }

    try {
      return builder.presentationML(presentationML).nestedEntity(issueBuilder.build()).generateXML();
    } catch (EntityXMLGeneratorException e) {
      throw new JiraParserException("Something went wrong while building the message for JIRA Comment event.", e);
    }
  }

  private String getCommentDisplayName(JsonNode node, String path) throws JiraParserException {
    JsonNode comment = node.path(COMMENT_PATH);
    return getOptionalField(comment, path, DISPLAY_NAME_PATH, "");
  }

  private SafeString getPresentationML(JsonNode node, String webHookEvent, SafeString comment)
      throws JiraParserException {
    String action = actions.get(webHookEvent);

    SafeString issueInfo = getIssueInfo(node, action);

    if (SafeStringUtils.isEmpty(comment)) {
      return presentationFormat(INFO_BLOCK_WITHOUT_COMMENT_FORMATTED_TEXT, issueInfo);
    }

    return presentationFormat(INFO_BLOCK_FORMATTED_TEXT, issueInfo, comment);
  }

  private Map<String, User> determineUserMentions(String comment) {
    Set<String> userMentions = new HashSet<>();
    Map<String, User> usersToMention = new HashMap<>();
    Matcher matcher = userCommentPattern.matcher(comment);
    while (matcher.find()) {
      userMentions.add(matcher.group(2));
    }
    for (String userName : userMentions) {
      User user = super.getUserByUserName(userName);
      if (user != null && StringUtils.isNotEmpty(user.getEmailAddress())) {
        usersToMention.put(userName, user);
      }
    }
    return usersToMention;
  }
}
