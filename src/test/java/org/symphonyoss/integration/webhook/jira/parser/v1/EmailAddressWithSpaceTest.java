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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
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
public class EmailAddressWithSpaceTest extends JiraParserTest {

  private static final String FILENAME =
      "parser/issueUpdatedJiraParser/jiraCallbackSampleEmailAddressWithSpace.json";

  public static final String EXPECTED_EMAIL_WITH_SPACE_MESSAGEML =
      "parser/commentJiraParser/commentAndEmailAddressWithSpace.xml";

  @InjectMocks
  private CommentJiraParser commentJiraParser = new CommentJiraParser();

  private ObjectMapper mapper = new ObjectMapper();

  @Test
  public void testParseCommentAdded() throws WebHookParseException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node = mapper.readTree(classLoader.getResourceAsStream(FILENAME));
    String expectedMessage = SimpleFileUtils.readMessageMLFile(EXPECTED_EMAIL_WITH_SPACE_MESSAGEML);

    Message actual = this.commentJiraParser.parse(null, node);
    assertEquals(expectedMessage, actual.getMessage());
  }

}
