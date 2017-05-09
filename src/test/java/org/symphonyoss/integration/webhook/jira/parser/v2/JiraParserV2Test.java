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
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.mockito.Mock;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.service.UserService;
import org.symphonyoss.integration.webhook.jira.parser.JiraParserTest;
import org.symphonyoss.integration.webhook.jira.parser.utils.FileUtils;

import java.io.IOException;
import java.util.Collections;

/**
 * Created by rsanchez on 22/07/16.
 */
public abstract class JiraParserV2Test<T extends JiraMetadataParser> extends JiraParserTest {

  private static final String MOCK_INTEGRATION_USER = "mockUser";

  private static final String MOCK_DISPLAY_NAME = "Mock user";

  private static final String MOCK_USERNAME = "integrationuser";

  private static final String MOCK_EMAIL_ADDRESS = "test@symphony.com";

  private static final Long MOCK_USER_ID = 123456L;

  @Mock
  protected UserService userService;

  @Mock
  protected IntegrationProperties integrationProperties;

  private T parser;

  @Before
  public void setUp() {

    parser = getParser();
    parser.init();
    parser.setIntegrationUser(MOCK_INTEGRATION_USER);

    when(integrationProperties.getApplicationUrl("jira")).thenReturn("http://my.url.com");
  }

  protected abstract String getExpectedTemplate() throws IOException;

  protected abstract T getParser();

  protected void testParser(String callbackJsonFilename, String expectedEntityJsonFilename)
      throws IOException {
    JsonNode node = FileUtils.readJsonFromFile(callbackJsonFilename);
    Message result = parser.parse(Collections.<String, String>emptyMap(), node);

    assertNotNull(result);

    JsonNode expectedNode = FileUtils.readJsonFromFile(expectedEntityJsonFilename);
    String expected = JsonUtils.writeValueAsString(expectedNode);

    assertEquals(expected, result.getData());
    assertEquals(getExpectedTemplate(), result.getMessage());
  }

  protected void mockUserInfo() {
    User user = new User();
    user.setId(MOCK_USER_ID);
    user.setDisplayName(MOCK_DISPLAY_NAME);
    user.setUserName(MOCK_USERNAME);
    user.setEmailAddress(MOCK_EMAIL_ADDRESS);

    doReturn(user).when(userService).getUserByEmail(eq(MOCK_INTEGRATION_USER), anyString());
  }
}
