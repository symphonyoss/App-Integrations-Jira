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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.symphonyoss.integration.agent.api.model.V3Message;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.service.UserService;
import org.symphonyoss.integration.utils.ApplicationContextUtils;
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

  @Mock
  private UserService userService;

  @Mock
  private ApplicationContext context;

  @InjectMocks
  private MetadataParser parser = new IssueCreatedMetadataParser();

  private ApplicationContextUtils utils;

  @Before
  public void init() {
    doReturn(userService).when(context).getBean(UserService.class);

    utils = new ApplicationContextUtils(context);

    parser.init();
    parser.setIntegrationUser(MOCK_INTEGRATION_USER);


  }

  @Test
  public void testIssueCreatedWithoutUserId() throws IOException, JiraParserException {
    JsonNode node = readJsonFromFile(FILE_ISSUE_CREATED);
    V3Message result = (V3Message) parser.parse(Collections.<String, String>emptyMap(), node);

    assertNotNull(result);

    JsonNode expectedNode = readJsonFromFile(FILE_EXPECTED_ISSUE_CREATED_WITHOUT_USER);
    String expected = JsonUtils.writeValueAsString(expectedNode);

    assertEquals(expected, result.getData());
  }

  @Test
  public void testIssueCreated() throws IOException, JiraParserException {
    mockUserInfo();

    JsonNode node = readJsonFromFile(FILE_ISSUE_CREATED);
    V3Message result = (V3Message) parser.parse(Collections.<String, String>emptyMap(), node);

    assertNotNull(result);

    JsonNode expectedNode = readJsonFromFile(FILE_EXPECTED_ISSUE_CREATED);
    String expected = JsonUtils.writeValueAsString(expectedNode);

    assertEquals(expected, result.getData());
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
    V3Message result = (V3Message) parser.parse(Collections.<String, String>emptyMap(), node);

    assertNotNull(result);

    JsonNode expectedNode = readJsonFromFile(FILE_EXPECTED_ISSUE_CREATED_WITH_EPIC);
    String expected = JsonUtils.writeValueAsString(expectedNode);

    assertEquals(expected, result.getData());
  }
}
