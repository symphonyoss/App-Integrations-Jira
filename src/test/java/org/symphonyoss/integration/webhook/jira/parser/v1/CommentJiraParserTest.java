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

package org.symphonyoss.integration.webhook.jira.parser.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;
import org.symphonyoss.integration.webhook.jira.parser.JiraParserTest;
import org.symphonyoss.integration.utils.SimpleFileUtils;

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

  private static final String COMMENT_ADDED_WITH_LINEBREAK =
      "parser/commentJiraParser/jiraCallbackSampleCommentAddedWithLinebreak.json";

  public static final String EXPECTED_USER_MENTION_NOT_FOUND_MESSAGEML =
      "parser/commentJiraParser/commentAddedMentionUserNotFoundMessageML.xml";

  public static final String EXPECTED_USER_MENTION_MESSAGEML =
      "parser/commentJiraParser/commentAddedWithMentionsMessageML.xml";

  public static final String EXPECTED_WITH_LINE_BREAK_MESSAGEML =
      "parser/commentJiraParser/commentAddedWithLinebreakMessageML.xml";

  public static final String EXPECTED_COMMENT_MESSAGEML =
      "parser/commentJiraParser/commentAddedMessageML.xml";

  public static final String EXPECTED_WITHOUT_COMMENT_MESSAGEML =
      "parser/commentJiraParser/commentAddedWithoutCommentMessageML.xml";

  public static final String EXPECTED_COMMENT_WITH_LINK_MESSAGEML =
      "parser/commentJiraParser/commentAddedWithLinkMessageML.xml";

  public static final String EXPECTED_COMMENT_WITH_MARKUP_MESSAGEML =
      "parser/commentJiraParser/commentAddedMessageMLWithMarkup.xml";

  public static final String EXPECTED_COMMENT_WITHOUT_LABELS_MESSAGEML =
      "parser/commentJiraParser/commentAddedWithoutLabelsMessageML.xml";

  public static final String EXPECTED_COMMENT_WITHOUT_ISSUE_MESSAGEML =
      "parser/commentJiraParser/commentAddedWithoutIssueMessageML.xml";

  public static final String EXPECTED_COMMENT_WITHOUT_PROJECT_NAME_MESSAGEML =
      "parser/commentJiraParser/commentAddedWithoutProjectNameMessageML.xml";

  @InjectMocks
  private CommentJiraParser commentJiraParser = new CommentJiraParser();

  private ClassLoader classLoader = getClass().getClassLoader();

  @Test
  public void testParseCommentAdded() throws WebHookParseException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME_COMPLETE_REQUEST));
    String expectedMessage = SimpleFileUtils.readMessageMLFile(EXPECTED_COMMENT_MESSAGEML);

    Message actual = this.commentJiraParser.parse(null, node);
    assertEquals(expectedMessage, actual.getMessage());
  }

  @Test
  public void testParseCommentAddedRestrictedComment() throws WebHookParseException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node = JsonUtils.readTree(
        classLoader.getResourceAsStream(FILENAME_COMMENT_ADDED_RESTRICTED_COMMENT));
    assertNull(this.commentJiraParser.parse(null, node));
  }

  @Test
  public void testParseCommentUpdatedRestrictedComment() throws WebHookParseException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node = JsonUtils.readTree(
        classLoader.getResourceAsStream(FILENAME_COMMENT_UPDATED_RESTRICTED_COMMENT));
    assertNull(this.commentJiraParser.parse(null, node));
  }

  @Test
  public void testParseCommentAddedWithURL() throws WebHookParseException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME_URL_MARKUP));
    String expectedMessage = SimpleFileUtils.readMessageMLFile(EXPECTED_COMMENT_WITH_LINK_MESSAGEML);

    Message actual = this.commentJiraParser.parse(null, node);
    assertEquals(expectedMessage, actual.getMessage());
  }

  @Test
  public void testParseCommentAddedWithFullJiraMarkup() throws WebHookParseException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node =
        JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME_COMPLETE_JIRA_MARKUP_REQUEST));

    String expected = SimpleFileUtils.readMessageMLFile(EXPECTED_COMMENT_WITH_MARKUP_MESSAGEML);

    String userKey1 = "user.Key_1";

    User returnedUserKey1 = new User();
    returnedUserKey1.setEmailAddress(userKey1 + "@symphony.com");
    returnedUserKey1.setUserName(userKey1);
    returnedUserKey1.setId(123l);

    String userKey2 = "user.Key_2";

    User returnedUserKey2 = new User();
    doReturn(returnedUserKey1).when(userService).getUserByUserName(anyString(), eq(userKey1));
    doReturn(returnedUserKey2).when(userService).getUserByUserName(anyString(), eq(userKey2));

    Message actual = this.commentJiraParser.parse(null, node);
    assertEquals(expected, actual.getMessage());
  }

  @Test
  public void testParseCommentAddedWithoutLabels() throws WebHookParseException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME_NO_LABELS_REQUEST));
    String expectedMessage = SimpleFileUtils.readMessageMLFile(EXPECTED_COMMENT_WITHOUT_LABELS_MESSAGEML);

    Message actual = this.commentJiraParser.parse(null, node);
    assertEquals(expectedMessage, actual.getMessage());
  }

  @Test
  public void testParseCommentAddedWithoutIssueType() throws WebHookParseException, IOException {
    JsonNode node =
        JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME_NO_ISSUE_TYPE_REQUEST));
    String expectedMessage = SimpleFileUtils.readMessageMLFile(EXPECTED_COMMENT_WITHOUT_ISSUE_MESSAGEML);

    Message actual = this.commentJiraParser.parse(null, node);
    assertEquals(expectedMessage, actual.getMessage());
  }

  @Test
  public void testParseCommentAddedWithoutProjectName() throws WebHookParseException, IOException {
    User user = new User();
    user.setEmailAddress("test@symphony.com");
    doReturn(user).when(userService).getUserByEmail(anyString(), anyString());

    JsonNode node =
        JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME_NO_PROJECT_NAME_REQUEST));
    String expectedMessage =
        SimpleFileUtils.readMessageMLFile(EXPECTED_COMMENT_WITHOUT_PROJECT_NAME_MESSAGEML);

    Message actual = this.commentJiraParser.parse(null, node);
    assertEquals(expectedMessage, actual.getMessage());
  }

  @Test
  public void testParseCommentAddedWithoutComment() throws WebHookParseException, IOException {
    JsonNode node =
        JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME_NO_COMMENT_REQUEST));
    String expectedMessage = SimpleFileUtils.readMessageMLFile(EXPECTED_WITHOUT_COMMENT_MESSAGEML);

    Message actual = this.commentJiraParser.parse(null, node);
    assertEquals(expectedMessage, actual.getMessage());
  }

  @Test
  public void testParseCommentAddedWithEmailWithWhitespace() throws WebHookParseException,
      IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME_EMAIL_WITH_SPACE));
    String expectedMessage = SimpleFileUtils.readMessageMLFile(EXPECTED_COMMENT_MESSAGEML);

    Message actual = this.commentJiraParser.parse(null, node);
    assertEquals(expectedMessage, actual.getMessage());
  }

  @Test
  public void testCommentAddedMentionUserNotFound() throws IOException, WebHookParseException {
    JsonNode node =
        JsonUtils.readTree(classLoader.getResourceAsStream(COMMENT_ADDED_WITH_MENTION_FILENAME));

    when(userService.getUserByUserName(anyString(), anyString())).thenReturn(new User());
    Message actual = this.commentJiraParser.parse(null, node);

    assertNotNull(actual);

    String expected = SimpleFileUtils.readMessageMLFile(EXPECTED_USER_MENTION_NOT_FOUND_MESSAGEML);

    assertEquals(expected, actual.getMessage());
  }

  @Test
  public void testCommentAddedWithMentions() throws IOException, WebHookParseException {
    JsonNode node =
        JsonUtils.readTree(classLoader.getResourceAsStream(COMMENT_ADDED_WITH_MENTIONS_FILENAME));

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

    Message actual = this.commentJiraParser.parse(null, node);

    assertNotNull(actual);

    String expected = SimpleFileUtils.readMessageMLFile(EXPECTED_USER_MENTION_MESSAGEML);

     assertEquals(expected, actual.getMessage());
  }

  @Test
  public void testParseCommentAddedWithLinebreak() throws WebHookParseException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node =
        JsonUtils.readTree(classLoader.getResourceAsStream(COMMENT_ADDED_WITH_LINEBREAK));

    String expectedMessage = SimpleFileUtils.readMessageMLFile(EXPECTED_WITH_LINE_BREAK_MESSAGEML);

    Message actual = this.commentJiraParser.parse(null, node);
    assertEquals(expectedMessage, actual.getMessage());
  }

}
