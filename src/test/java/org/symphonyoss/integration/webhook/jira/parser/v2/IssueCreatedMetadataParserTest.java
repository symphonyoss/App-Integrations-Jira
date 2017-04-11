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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.service.UserService;
import org.symphonyoss.integration.webhook.jira.parser.JiraParserException;
import org.symphonyoss.integration.webhook.jira.parser.JiraParserTest;

import java.io.IOException;
import java.util.Collections;

/**
 * Unit test class for {@link IssueCreatedMetadataParser}
 * Created by rsanchez on 29/03/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class IssueCreatedMetadataParserTest extends JiraParserTest {

  private static final String MOCK_INTEGRATION_USER = "mockUser";

  private static final String MOCK_DISPLAY_NAME = "Mock user";

  private static final String MOCK_USERNAME = "username";

  private static final String MOCK_EMAIL_ADDRESS = "test@symphony.com";

  private static final Long MOCK_USER_ID = 123456L;

  private static final String FILE_ISSUE_CREATED =
      "parser/issueCreatedJiraParser/jiraCallbackSampleIssueCreated.json";

  private static final String FILE_ISSUE_CREATED_WITH_EPIC =
      "parser/issueCreatedJiraParser/jiraCallbackSampleIssueCreatedWithEpic.json";

  private static final String FILE_EXPECTED_ISSUE_CREATED =
      "parser/issueCreatedJiraParser/v2/issueCreatedEntityJSON.json";

  private static final String FILE_EXPECTED_ISSUE_CREATED_WITHOUT_USER =
      "parser/issueCreatedJiraParser/v2/issueCreatedWithoutUserIdEntityJSON.json";

  private static final String FILE_EXPECTED_ISSUE_CREATED_WITH_EPIC =
      "parser/issueCreatedJiraParser/v2/issueCreatedWithEpicEntityJSON.json";

  private static final String FILE_EXPECTED_ISSUE_CREATED_TEMPLATE_MESSAGEML =
      "templates/templateIssueCreated.xml";

  @Mock
  private UserService userService;

  private MetadataParser parser;

  private String expectedTemplateFile = "<messageML>\n"
      + "    <div class=\"entity\">\n"
      + "        <card class=\"barStyle\">\n"
      + "            <header>\n"
      + "                <img src=\"${entity['jiraIssueCreated'].issue.priority.iconUrl}\" />\n"
      + "                <a href=\"${entity['jiraIssueCreated'].issue.url}\">${entity"
      + "['jiraIssueCreated'].issue.key}</a>\n"
      + "                <span>${entity['jiraIssueCreated'].issue.subject} - </span>\n"
      + "                <#if (entity['jiraIssueCreated'].user.id)??>\n"
      + "                    <mention email=\"${entity['jiraIssueCreated'].user.emailAddress}\" />\n"
      + "                <#else>\n"
      + "                    <span>${entity['jiraIssueCreated'].user.displayName}</span>\n"
      + "                </#if>\n"
      + "                <span class=\"action\">Created</span>\n"
      + "            </header>\n"
      + "            <body>\n"
      + "                <div class=\"entity\" data-entity-id=\"jiraIssueCreated\">\n"
      + "                    <div class=\"labelBackground badge\">\n"
      + "                            <span class=\"label\">Description:</span>\n"
      + "                            <span>${entity['jiraIssueCreated'].issue.description}</span>\n"
      + "                            <br/>\n"
      + "                            <span class=\"label\">Assignee:</span>\n"
      + "                            <#if (entity['jiraIssueCreated'].issue.assignee.id)??>\n"
      + "                                <mention email=\"${entity['jiraIssueCreated'].issue.assignee.emailAddress}\""
      + " />\n"
      + "                            <#else>\n"
      + "                                <span>${entity['jiraIssueCreated'].issue.assignee.displayName}</span>\n"
      + "                            </#if>\n"
      + "                    </div>\n"
      + "                    <div class=\"labelBackground badge\">\n"
      + "                            <span class=\"label\">Type:</span>\n"
      + "                            <img src=\"${entity['jiraIssueCreated'].issue.issueType}\" "
      + "/>\n"
      + "                            <span class=\"label\">Priority:</span>\n"
      + "                            <img src=\"${entity['jiraIssueCreated'].issue.priority"
      + ".iconUrl}\" />\n"
      + "                            <span>${entity['jiraIssueCreated'].issue.priority"
      + ".name}</span>\n"
      + "                            <span class=\"label\">Labels:</span>\n"
      + "                            <#list entity['jiraIssueCreated'].issue.labels as label>\n"
      + "                                <a class=\"hashTag\">#${label.text}</a>\n"
      + "                            </#list>\n"
      + "                    </div>\n"
      + "                </div>\n"
      + "            </body>\n"
      + "        </card>\n"
      + "    </div>\n"
      + "</messageML>\n";

  @Before
  public void init() {
    parser = new IssueCreatedMetadataParser(userService);

    parser.init();
    parser.setIntegrationUser(MOCK_INTEGRATION_USER);
  }

  @Test
  public void testIssueCreatedWithoutUserId() throws IOException, JiraParserException {
    JsonNode node = readJsonFromFile(FILE_ISSUE_CREATED);
    Message result = parser.parse(Collections.<String, String>emptyMap(), node);

    assertNotNull(result);

    JsonNode expectedNode = readJsonFromFile(FILE_EXPECTED_ISSUE_CREATED_WITHOUT_USER);
    String expected = JsonUtils.writeValueAsString(expectedNode);

    assertEquals(expected, result.getData());
    assertEquals(expectedTemplateFile, result.getMessage());
  }

  @Test
  public void testIssueCreated() throws IOException, JiraParserException {
    mockUserInfo();

    JsonNode node = readJsonFromFile(FILE_ISSUE_CREATED);
    Message result = parser.parse(Collections.<String, String>emptyMap(), node);

    assertNotNull(result);

    JsonNode expectedNode = readJsonFromFile(FILE_EXPECTED_ISSUE_CREATED);
    String expected = JsonUtils.writeValueAsString(expectedNode);

    assertEquals(expected, result.getData());
    assertEquals(expectedTemplateFile, result.getMessage());
  }

  private void mockUserInfo() {
    User user = new User();
    user.setId(MOCK_USER_ID);
    user.setDisplayName(MOCK_DISPLAY_NAME);
    user.setUserName(MOCK_USERNAME);
    user.setEmailAddress(MOCK_EMAIL_ADDRESS);

    doReturn(user).when(userService).getUserByEmail(eq(MOCK_INTEGRATION_USER), anyString());
  }

  @Test
  public void testIssueCreatedWithEpic() throws IOException, JiraParserException {
    mockUserInfo();

    JsonNode node = readJsonFromFile(FILE_ISSUE_CREATED_WITH_EPIC);
    Message result = parser.parse(Collections.<String, String>emptyMap(), node);

    assertNotNull(result);

    JsonNode expectedNode = readJsonFromFile(FILE_EXPECTED_ISSUE_CREATED_WITH_EPIC);
    String expected = JsonUtils.writeValueAsString(expectedNode);

    assertEquals(expected, result.getData());
    assertEquals(expectedTemplateFile, result.getMessage());
  }
}
