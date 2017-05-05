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

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.webhook.jira.parser.JiraParserException;

import java.io.IOException;

/**
 * Created by apimentel on 27/04/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class CommentMetadataParserTest extends JiraParserV2Test<CommentMetadataParser> {
  private static final String FILE_COMMENT_ADDED =
      "parser/commentJiraParser/jiraCallbackSampleCommentAdded.json";

  private static final String FILE_COMMENT_ADDED_NO_COMMENT =
      "parser/commentJiraParser/jiraCallbackSampleCommentAddedWithoutComment.json";

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

  private static final String EXPECTED_TEMPLATE_FILE = "<messageML>\n"
      + "    <div class=\"entity\" data-entity-id=\"jiraIssueCommented\">\n"
      + "        <card class=\"barStyle\" accent=\"green\" iconSrc=\"\">\n"
      + "            <header>\n"
      + "                <p>\n"
      + "                    <img src=\"${entity['jiraIssueCommented'].issue.priority.iconUrl}\" "
      + "class=\"icon\" />\n"
      + "                    "
      + "<a class=\"tempo-text-color--link\" href=\"${entity['jiraIssueCommented'].issue.url}\">$"
      + "{entity['jiraIssueCommented'].issue.key}</a>\n"
      + "                    <span "
      + "class=\"tempo-text-color--normal\">${entity['jiraIssueCommented'].issue.subject} - "
      + "</span>\n"
      + "                    <#if (entity['jiraIssueCommented'].user.id)??>\n"
      + "                        <mention email=\"${entity['jiraIssueCommented'].user"
      + ".emailAddress}\" />\n"
      + "                    <#else>\n"
      + "                        <span "
      + "class=\"tempo-text-color--normal\">${entity['jiraIssueCommented'].user"
      + ".displayName}</span>\n"
      + "                    </#if>\n"
      + "                    <span class=\"tempo-text-color--green\">${entity['jiraIssueCommented"
      + "'].comment.action}</span>\n"
      + "                </p>\n"
      + "            </header>\n"
      + "            <body>\n"
      + "                <div class=\"labelBackground badge\">\n"
      + "                    <p>\n"
      + "                        <span class=\"tempo-text-color--secondary\">Comment:</span>\n"
      + "                        <span "
      + "class=\"tempo-text-color--normal\">${entity['jiraIssueCommented'].comment.body}</span>\n"
      + "                        <br/>\n"
      + "                        "
      +
      "<a class=\"tempo-text-color--link\" href=\"${entity['jiraIssueCommented'].comment.url}\">View comment</a>\n"
      + "                    </p>\n"
      + "                </div>\n"
      + "            </body>\n"
      + "        </card>\n"
      + "    </div>\n"
      + "</messageML>\n";

  @Override
  protected String getExpectedTemplate() {
    return EXPECTED_TEMPLATE_FILE;
  }

  @Override
  protected Class<CommentMetadataParser> getParserClass() {
    return CommentMetadataParser.class;
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