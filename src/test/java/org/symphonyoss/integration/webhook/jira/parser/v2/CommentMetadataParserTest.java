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

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.webhook.jira.parser.JiraParserException;
import org.symphonyoss.integration.webhook.jira.parser.utils.FileUtils;

import java.io.IOException;
import java.util.Collections;

/**
 * Created by apimentel on 27/04/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class CommentMetadataParserTest extends JiraParserV2Test<CommentMetadataParser> {
  private static final String FILE_COMMENT_ADDED =
      "parser/commentJiraParser/jiraCallbackSampleCommentAdded.json";

  private static final String FILE_COMMENT_ADDED_NO_COMMENT =
      "parser/commentJiraParser/jiraCallbackSampleCommentAddedWithoutComment.json";

  private static final String FILE_COMMENT_RESTRICTED =
      "parser/commentJiraParser/jiraCallbackSampleCommentAddedRestrictedComment.json";

  private static final String FILE_COMMENT_ADDED_WITH_MENTIONS =
      "parser/commentJiraParser/jiraCallbackSampleCommentAddedWithMentions.json";

  private static final String FILE_EXPECTED_COMMENT_ADDED_BY_VALID_USER =
      "parser/commentJiraParser/v2/commentAddedByValidUser.json";

  private static final String FILE_EXPECTED_COMMENT_ADDED_NO_DB_USER =
      "parser/commentJiraParser/v2/commentAddedNoDbUser.json";

  private static final String FILE_EXPECTED_COMMENT_NO_BODY =
      "parser/commentJiraParser/v2/commentAddedWithoutComment.json";

  private static final String FILE_EXPECTED_MENTIONS =
      "parser/commentJiraParser/v2/commentAddedWithMentions.json";

  private CommentMetadataParser parserInstance;

  @Override
  protected String getExpectedTemplate() throws IOException {
    // MetadataParser appends a '\n' at the end of the file
    return FileUtils.readFile("templates/templateIssueCommented.xml") + "\n";
  }

  @Override
  protected CommentMetadataParser getParser() {
    if (parserInstance == null) {
      parserInstance = new CommentMetadataParser(userService, integrationProperties);
    }
    return parserInstance;
  }

  @Test
  public void testSymphonyUserComment() throws IOException, JiraParserException {
    mockUserInfo();
    testParser(FILE_COMMENT_ADDED, FILE_EXPECTED_COMMENT_ADDED_BY_VALID_USER);
  }

  @Test
  public void testNoSymphonyUserComment() throws IOException, JiraParserException {
    testParser(FILE_COMMENT_ADDED, FILE_EXPECTED_COMMENT_ADDED_NO_DB_USER);
  }

  @Test
  public void testNoCommentText() throws IOException, JiraParserException {
    mockUserInfo();
    testParser(FILE_COMMENT_ADDED_NO_COMMENT, FILE_EXPECTED_COMMENT_NO_BODY);
  }

  @Test
  public void testRestrictedComment() throws IOException, JiraParserException {
    mockUserInfo();

    JsonNode node = FileUtils.readJsonFromFile(FILE_COMMENT_RESTRICTED);
    Message result = parserInstance.parse(Collections.<String, String>emptyMap(), node);

    assertNull(result);

    verify(userService, times(0)).getUserByUserName(anyString(), anyString());
    verify(userService, times(0)).getUserByEmail(anyString(), anyString());
    verify(integrationProperties, times(0)).getApplicationUrl(anyString());
  }

  @Test
  @Ignore("This is only valid when supporting mentions in the metadata")
  public void testWithMentions() throws IOException, JiraParserException {
    mockUserInfo();
    String integrationuser = "integrationuser";

    User user = new User();
    user.setId(123L);
    user.setDisplayName("Misterious Guy");
    user.setUserName(integrationuser);
    user.setEmailAddress("test.user@test.com");
    doReturn(user).when(userService).getUserByUserName(anyString(), eq(integrationuser));

    testParser(FILE_COMMENT_ADDED_WITH_MENTIONS, FILE_EXPECTED_MENTIONS);

    verify(userService, times(2)).getUserByEmail(anyString(), anyString());
  }
}