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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.webhook.jira.parser.JiraParserException;
import org.symphonyoss.integration.utils.SimpleFileUtils;

import java.io.IOException;

/**
 * Unit test class for {@link IssueStateMetadataParser}
 * Created by rsanchez on 29/03/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class IssueStateMetadataParserTest extends JiraParserV2Test<IssueStateMetadataParser> {

  private static final String FILE_ISSUE_CREATED =
      "parser/issueCreatedJiraParser/jiraCallbackSampleIssueCreated.json";

  private static final String FILE_ISSUE_CREATED_WITH_EPIC =
      "parser/issueCreatedJiraParser/jiraCallbackSampleIssueCreatedWithEpic.json";

  private static final String FILE_ISSUE_UPDATED =
      "parser/issueUpdatedJiraParser/jiraCallbackSampleIssueUpdated.json";

  private static final String FILE_ISSUE_UPDATED_INPROGRESS =
      "parser/issueUpdatedJiraParser/jiraCallbackSampleIssueUpdatedInProgress.json";

  private static final String FILE_ISSUE_UPDATED_EPIC_UPDATED =
      "parser/issueUpdatedJiraParser/jiraCallbackSampleIssueEpicUpdated.json";

  private static final String FILE_ISSUE_UPDATED_EPIC_UPDATED_TO_NULL =
      "parser/issueUpdatedJiraParser/jiraCallbackSampleIssueEpicUpdatedToNull.json";

  private static final String FILE_ISSUE_UPDATED_EMAIL_WITH_SPACES =
      "parser/issueUpdatedJiraParser/jiraCallbackSampleEmailAddressWithSpace.json";

  private static final String FILE_EXPECTED_ISSUE_CREATED =
      "parser/issueCreatedJiraParser/v2/issueCreatedEntityJSON.json";

  private static final String FILE_EXPECTED_ISSUE_CREATED_WITHOUT_USER =
      "parser/issueCreatedJiraParser/v2/issueCreatedWithoutUserIdEntityJSON.json";

  private static final String FILE_EXPECTED_ISSUE_CREATED_WITH_EPIC =
      "parser/issueCreatedJiraParser/v2/issueCreatedWithEpicEntityJSON.json";

  private static final String FILE_EXPECTED_ISSUE_UPDATED =
      "parser/issueUpdatedJiraParser/v2/issueUpdatedEntityJSON.json";

  private static final String FILE_EXPECTED_ISSUE_UPDATED_INPROGRESS =
      "parser/issueUpdatedJiraParser/v2/issueUpdatedInProgressEntityJSON.json";

  private static final String FILE_EXPECTED_ISSUE_UPDATED_EPIC_UPDATED =
      "parser/issueUpdatedJiraParser/v2/issueUpdatedEpicUpdatedEntityJSON.json";

  private static final String FILE_EXPECTED_ISSUE_UPDATED_EPIC_UPDATED_TO_NULL =
      "parser/issueUpdatedJiraParser/v2/issueUpdatedEpicUpdatedToNullEntityJSON.json";

  private static final String FILE_EXPECTED_ISSUE_UPDATED_EMAIL_WITH_SPACES =
      "parser/issueUpdatedJiraParser/v2/issueUpdatedEmailWithSpacesEntityJSON.json";

  private IssueStateMetadataParser parserInstance;

  @Override
  protected String getExpectedTemplate() throws IOException {
    return SimpleFileUtils.readFile("templates/templateIssueState.xml");
  }

  @Override
  protected IssueStateMetadataParser getParser() {
    if (parserInstance == null) {
      parserInstance = new IssueStateMetadataParser(userService, integrationProperties);
    }
    return parserInstance;
  }

  @Test
  public void testIssueCreatedWithoutUserId() throws IOException, JiraParserException {
    testParser(FILE_ISSUE_CREATED, FILE_EXPECTED_ISSUE_CREATED_WITHOUT_USER);
  }

  @Test
  public void testIssueCreated() throws IOException, JiraParserException {
    mockUserInfo();
    testParser(FILE_ISSUE_CREATED, FILE_EXPECTED_ISSUE_CREATED);
  }

  @Test
  public void testIssueCreatedWithEpic() throws IOException, JiraParserException {
    mockUserInfo();
    testParser(FILE_ISSUE_CREATED_WITH_EPIC, FILE_EXPECTED_ISSUE_CREATED_WITH_EPIC);
  }

  @Test
  public void testIssueUpdated() throws IOException, JiraParserException {
    mockUserInfo();
    testParser(FILE_ISSUE_UPDATED, FILE_EXPECTED_ISSUE_UPDATED);
  }

  @Test
  public void testIssueUpdatedInProgress() throws IOException, JiraParserException {
    mockUserInfo();
    testParser(FILE_ISSUE_UPDATED_INPROGRESS, FILE_EXPECTED_ISSUE_UPDATED_INPROGRESS);
  }

  @Test
  public void testIssueUpdatedEpicUpdated() throws IOException, JiraParserException {
    mockUserInfo();
    testParser(FILE_ISSUE_UPDATED_EPIC_UPDATED, FILE_EXPECTED_ISSUE_UPDATED_EPIC_UPDATED);
  }

  @Test
  public void testIssueUpdatedEpicUpdatedToNull() throws IOException, JiraParserException {
    mockUserInfo();
    testParser(FILE_ISSUE_UPDATED_EPIC_UPDATED_TO_NULL,
        FILE_EXPECTED_ISSUE_UPDATED_EPIC_UPDATED_TO_NULL);
  }

  @Test
  public void testIssueUpdatedEmailWithSpaces() throws IOException, JiraParserException {
    mockUserInfo();
    testParser(FILE_ISSUE_UPDATED_EMAIL_WITH_SPACES, FILE_EXPECTED_ISSUE_UPDATED_EMAIL_WITH_SPACES);
  }
}
