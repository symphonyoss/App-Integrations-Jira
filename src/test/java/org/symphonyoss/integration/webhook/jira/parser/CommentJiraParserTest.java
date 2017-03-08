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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;

import java.io.IOException;

/**
 * Unit tests for {@link CommentJiraParser}.
 *
 * Created by mquilzini on 18/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class CommentJiraParserTest extends JiraParserTest {

  private static final String FILENAME_COMPLETE_REQUEST =
      "parser/commentJiraParser/jiraCallbackSampleCommentAdded.json";

  private static final String FILENAME_COMMENT_ADDED_RESTRICTED_COMMENT =
      "parser/commentJiraParser/jiraCallbackSampleCommentAddedRestrictedComment.json";

  private static final String FILENAME_COMMENT_UPDATED_RESTRICTED_COMMENT =
      "parser/commentJiraParser/jiraCallbackSampleCommentUpdatedRestrictedComment.json";

  private static final String FILENAME_URL_MARKUP =
      "parser/issueUpdatedJiraParser/jiraCallbackSampleMarkUpLinkDescription.json";

  private static final String FILENAME_COMPLETE_JIRA_MARKUP_REQUEST =
      "parser/commentJiraParser/jiraCallbackSampleCommentAddedJiraMarkup.json";

  private static final String FILENAME_INCOMPLETE_REQUEST =
      "jiraCallbackSampleCommentAddedAlt.json";

  private static final String FILENAME_NO_LABELS_REQUEST =
      "parser/commentJiraParser/jiraCallbackSampleCommentAddedWithoutLabels.json";

  private static final String FILENAME_NO_ISSUE_TYPE_REQUEST =
      "parser/commentJiraParser/jiraCallbackSampleCommentAddedWithoutIssueType.json";

  private static final String FILENAME_NO_PROJECT_NAME_REQUEST =
      "parser/commentJiraParser/jiraCallbackSampleCommentAddedWithoutProjectName.json";

  private static final String FILENAME_NO_COMMENT_REQUEST =
      "parser/commentJiraParser/jiraCallbackSampleCommentAddedWithoutComment.json";

  private static final String FILENAME_EMAIL_WITH_SPACE =
      "parser/commentJiraParser/jiraCallbackSampleCommentAddedAndEmailWithSpace.json";

  private static final String COMMENT_ADDED_WITH_MENTION_FILENAME =
      "parser/commentJiraParser/jiraCallbackSampleCommentAddedWithMention.json";

  private static final String COMMENT_ADDED_WITH_MENTIONS_FILENAME =
      "parser/commentJiraParser/jiraCallbackSampleCommentAddedWithMentions.json";

  @InjectMocks
  private CommentJiraParser commentJiraParser = new CommentJiraParser();

  private ClassLoader classLoader = getClass().getClassLoader();

  @Test
  public void testParseCommentAdded() throws WebHookParseException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME_COMPLETE_REQUEST));
    String expectedMessage = readFile("parser/commentJiraParser/commentAddedMessageML.xml");
    assertEquals(expectedMessage, this.commentJiraParser.parse(null, node));
  }

  @Test
  public void testParseCommentAddedRestrictedComment() throws WebHookParseException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME_COMMENT_ADDED_RESTRICTED_COMMENT));
    assertEquals(null, this.commentJiraParser.parse(null, node));
  }

  @Test
  public void testParseCommentUpdatedRestrictedComment() throws WebHookParseException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME_COMMENT_UPDATED_RESTRICTED_COMMENT));
    assertEquals(null, this.commentJiraParser.parse(null, node));
  }

  @Test
  public void testParseCommentAddedWithURL() throws WebHookParseException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME_URL_MARKUP));
    String expectedMessage = readFile("parser/commentJiraParser/commentAddedWithLinkMessageML.xml");
    assertEquals(expectedMessage, this.commentJiraParser.parse(null, node));
  }

  @Test
  public void testParseCommentAddedWithFullJiraMarkup() throws WebHookParseException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node =
        JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME_COMPLETE_JIRA_MARKUP_REQUEST));

    String expected = readFile("parser/commentJiraParser/commentAddedMessageMLWithMarkup.xml");

    String userKey1 = "user.Key_1";

    User returnedUserKey1 = new User();
    returnedUserKey1.setEmailAddress(userKey1 + "@symphony.com");
    returnedUserKey1.setUserName(userKey1);
    returnedUserKey1.setId(123l);

    String userKey2 = "user.Key_2";

    User returnedUserKey2 = new User();
    doReturn(returnedUserKey1).when(userService).getUserByUserName(anyString(), eq(userKey1));
    doReturn(returnedUserKey2).when(userService).getUserByUserName(anyString(), eq(userKey2));
    String actual = this.commentJiraParser.parse(null, node);
    assertEquals(expected, actual);
  }

  @Test
  public void testParseCommentAddedWithoutLabels() throws WebHookParseException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME_NO_LABELS_REQUEST));
    String expectedMessage = readFile(
        "parser/commentJiraParser/commentAddedWithoutLabelsMessageML.xml");
    assertEquals(expectedMessage, this.commentJiraParser.parse(null, node));
  }

  @Test
  public void testParseCommentAddedWithoutIssueType() throws WebHookParseException, IOException {
    JsonNode node =
        JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME_NO_ISSUE_TYPE_REQUEST));
    String expectedMessage = readFile(
        "parser/commentJiraParser/commentAddedWithoutIssueMessageML.xml");
    assertEquals(expectedMessage, this.commentJiraParser.parse(null, node));
  }

  @Test
  public void testParseCommentAddedWithoutProjectName() throws WebHookParseException, IOException {
    User user = new User();
    user.setEmailAddress("test@symphony.com");
    doReturn(user).when(userService).getUserByEmail(anyString(), anyString());

    JsonNode node =
        JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME_NO_PROJECT_NAME_REQUEST));
    String expectedMessage = readFile(
        "parser/commentJiraParser/commentAddedWithoutProjectNameMessageML.xml");
    assertEquals(expectedMessage, this.commentJiraParser.parse(null, node));
  }

  @Test
  public void testParseCommentAddedWithoutComment() throws WebHookParseException, IOException {
    JsonNode node =
        JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME_NO_COMMENT_REQUEST));
    String expectedMessage = readFile(
        "parser/commentJiraParser/commentAddedWithoutCommentMessageML.xml");

    assertEquals(expectedMessage, this.commentJiraParser.parse(null, node));
  }

  @Test
  public void testParseCommentAddedWithEmailWithWhitespace() throws WebHookParseException,
      IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME_EMAIL_WITH_SPACE));
    String expectedMessage = readFile("parser/commentJiraParser/commentAddedMessageML.xml");
    assertEquals(expectedMessage, this.commentJiraParser.parse(null, node));
  }

  @Test
  public void testCommentAddedMentionUserNotFound() throws IOException, WebHookParseException {
    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(COMMENT_ADDED_WITH_MENTION_FILENAME));

    when(userService.getUserByUserName(anyString(), anyString())).thenReturn(new User());
    String result = this.commentJiraParser.parse(null, node);

    assertNotNull(result);

    String expected = readFile(
        "parser/commentJiraParser/commentAddedMentionUserNotFoundMessageML.xml");

    assertEquals(expected, result);
  }

  @Test
  public void testCommentAddedWithMentions() throws IOException, WebHookParseException {
    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(COMMENT_ADDED_WITH_MENTIONS_FILENAME));

    User user = new User();
    user.setEmailAddress("integrationuser@symphony.com");
    user.setId(123L);
    user.setUserName("integrationuser");
    user.setDisplayName("Integration User");
    doReturn(user).when(userService).getUserByUserName(anyString(), eq("integrationuser"));

    User user2 = new User();
    user2.setEmailAddress("user2@symphony.com");
    user2.setId(456L);
    user2.setUserName("user2");
    user2.setDisplayName("User 2");
    doReturn(user2).when(userService).getUserByUserName(anyString(), eq("user2"));

    String result = this.commentJiraParser.parse(null, node);

    assertNotNull(result);

    String expected = readFile(
        "parser/commentJiraParser/commentAddedWithMentionsMessageML.xml");

    assertEquals(expected, result);
  }


}
