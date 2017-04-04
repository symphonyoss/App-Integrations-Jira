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
import static org.junit.Assert.assertNull;
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
 * Unit test class for {@link MetadataParser}
 * Created by rsanchez on 03/04/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class MetadataParserTest extends JiraParserTest {

  private static final String INVALID_TEMPLATE_FILE = "invalidTemplate.xml";

  private static final String INVALID_METADATA_FILE = "invalidMetadata.xml";

  private static final String EVENT_NAME = "jiraIssueCreated";

  private static final String EVENT_TYPE = "com.symphony.integration.jira.event.v2.created";

  private static final String FILE_ISSUE_CREATED =
      "parser/issueCreatedJiraParser/jiraCallbackSampleIssueCreated.json";

  private static final String FILE_ISSUE_CREATED_MESSAGEML = "templateIssueCreated.xml";

  private static final String FILE_ISSUE_CREATED_METADATA = "metadataIssueCreated.xml";

  private static final String FILE_EXPECTED_ISSUE_CREATED_WITHOUT_USER =
      "parser/issueCreatedJiraParser/v2/issueCreatedWithoutUserIdEntityJSON.json";

  @Test
  public void testInvalidTemplateFile() throws IOException {
    MetadataParser parser = new MockMetadataParser(INVALID_TEMPLATE_FILE, FILE_ISSUE_CREATED_METADATA);
    parser.init();

    JsonNode node = readJsonFromFile(FILE_ISSUE_CREATED);
    assertNull(parser.parse(Collections.<String, String>emptyMap(), node));
  }

  @Test
  public void testInvalidMetadataFile() throws IOException {
    MetadataParser parser = new MockMetadataParser(FILE_ISSUE_CREATED_MESSAGEML, INVALID_METADATA_FILE);
    parser.init();

    JsonNode node = readJsonFromFile(FILE_ISSUE_CREATED);
    assertNull(parser.parse(Collections.<String, String>emptyMap(), node));
  }

  @Test
  public void testIssueCreatedWithoutUserId() throws IOException, JiraParserException {
    MetadataParser parser = new MockMetadataParser(FILE_ISSUE_CREATED_MESSAGEML,
        FILE_ISSUE_CREATED_METADATA, EVENT_TYPE, EVENT_NAME);
    parser.init();

    JsonNode node = readJsonFromFile(FILE_ISSUE_CREATED);
    V3Message result = (V3Message) parser.parse(Collections.<String, String>emptyMap(), node);

    assertNotNull(result);

    JsonNode expectedNode = readJsonFromFile(FILE_EXPECTED_ISSUE_CREATED_WITHOUT_USER);
    String expected = JsonUtils.writeValueAsString(expectedNode);

    assertEquals(expected, result.getData());
  }
}
