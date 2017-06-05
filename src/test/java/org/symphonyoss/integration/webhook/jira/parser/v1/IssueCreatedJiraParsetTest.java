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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ASSIGNEE_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.FIELDS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ISSUE_PATH;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.webhook.jira.parser.JiraParserException;
import org.symphonyoss.integration.webhook.jira.parser.JiraParserTest;
import org.symphonyoss.integration.utils.SimpleFileUtils;

import java.io.IOException;
import java.util.Collections;

/**
 * Test class to validate {@link IssueCreatedJiraParser}
 *
 * Created by rsanchez on 18/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class IssueCreatedJiraParsetTest extends JiraParserTest {

  public static final String
      ISSUE_CREATED_WITH_EPIC_MESSAGEML =
      "parser/issueCreatedJiraParser/issueCreatedWithEpicMessageML.xml";
  private static final String FILE_ISSUE_CREATED =
      "parser/issueCreatedJiraParser/jiraCallbackSampleIssueCreated.json";

  private static final String FILE_ISSUE_CREATED_JIRA_MARKUP =
      "parser/issueCreatedJiraParser/jiraCallbackSampleIssueCreatedJiraMarkup.json";

  private static final String FILE_ISSUE_CREATED_WITH_EPIC =
      "parser/issueCreatedJiraParser/jiraCallbackSampleIssueCreatedWithEpic.json";

  private static final String ISSUE_CREATED_MESSAGEML =
      "parser/issueCreatedJiraParser/issueCreatedMessageML.xml";

  public static final String ISSUE_CREATED_JIRA_MARKUP_MESSAGEML =
      "parser/issueCreatedJiraParser/issueCreatedMessageMLJiraMarkup.xml";
  public static final String ISSUE_CREATED_UNASSIGNED_MESSAGEML =
      "parser/issueCreatedJiraParser/issueCreatedUnassigneeMessageML.xml";

  @InjectMocks
  private IssueCreatedJiraParser issueCreated = new IssueCreatedJiraParser();

  private void mockUserServiceForTest2User() {
    User returnedUser = new User();
    returnedUser.setEmailAddress("test2@symphony.com");
    returnedUser.setId(123l);
    returnedUser.setUserName("test2");
    returnedUser.setDisplayName("Test2 User");
    doReturn(returnedUser).when(userService).getUserByEmail(anyString(), eq("test2@symphony.com"));
  }

  @Test
  public void testIssueCreated() throws IOException, JiraParserException {
    mockUserServiceForTest2User();

    JsonNode node = SimpleFileUtils.readJsonFromFile(FILE_ISSUE_CREATED);
    Message result = issueCreated.parse(Collections.<String, String>emptyMap(), node);

    assertNotNull(result);

    String expected = SimpleFileUtils.readMessageMLFile(ISSUE_CREATED_MESSAGEML);

    assertEquals(expected, result.getMessage());
  }


  @Test
  public void testIssueCreatedJiraMarkup() throws IOException, JiraParserException {
    mockUserServiceForTest2User();

    JsonNode node = SimpleFileUtils.readJsonFromFile(FILE_ISSUE_CREATED_JIRA_MARKUP);
    Message result = issueCreated.parse(Collections.<String, String>emptyMap(), node);

    assertNotNull(result);

    String expected =
        SimpleFileUtils.readMessageMLFile(ISSUE_CREATED_JIRA_MARKUP_MESSAGEML);

    assertEquals(expected, result.getMessage());
  }

  @Test
  public void testIssueCreatedUnassigned() throws IOException, JiraParserException {
    JsonNode node = SimpleFileUtils.readJsonFromFile(FILE_ISSUE_CREATED);
    ObjectNode fieldsNode = (ObjectNode) node.path(ISSUE_PATH).path(FIELDS_PATH);
    fieldsNode.remove(ASSIGNEE_PATH);
    fieldsNode.putNull(ASSIGNEE_PATH);

    Message result = issueCreated.parse(Collections.<String, String>emptyMap(), node);

    assertNotNull(result);

    String expected =
        SimpleFileUtils.readMessageMLFile(ISSUE_CREATED_UNASSIGNED_MESSAGEML);

    assertEquals(expected, result.getMessage());
  }

  @Test
  public void testIssueCreatedWithEpic() throws IOException, JiraParserException {
    User user = new User();
    user.setEmailAddress("test@symphony.com");
    doReturn(user).when(userService).getUserByEmail(anyString(), eq("test@symphony.com"));

    User user2 = new User();
    user2.setEmailAddress("test2@symphony.com");
    doReturn(user2).when(userService).getUserByEmail(anyString(), eq("test2@symphony.com"));

    ObjectNode node = (ObjectNode) SimpleFileUtils.readJsonFromFile(FILE_ISSUE_CREATED_WITH_EPIC);
    Message result = issueCreated.parse(Collections.<String, String>emptyMap(), node);

    String expected =
        SimpleFileUtils.readMessageMLFile(ISSUE_CREATED_WITH_EPIC_MESSAGEML);
    assertEquals(expected, result.getMessage());
  }
}
