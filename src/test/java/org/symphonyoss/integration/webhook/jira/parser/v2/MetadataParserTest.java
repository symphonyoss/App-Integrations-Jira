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

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
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

  private static final String FILE_ISSUE_CREATED =
      "parser/issueCreatedJiraParser/jiraCallbackSampleIssueCreated.json";

  private static final String FILE_ISSUE_CREATED_MESSAGEML = "templateIssueCreated.xml";

  private static final String FILE_ISSUE_CREATED_METADATA = "metadataIssueCreated.xml";

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

}
