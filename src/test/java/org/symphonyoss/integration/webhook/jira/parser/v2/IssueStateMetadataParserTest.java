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

  private static final String FILE_EXPECTED_ISSUE_UPDATED_EMAIL_WITH_SPACES =
      "parser/issueUpdatedJiraParser/v2/issueUpdatedEmailWithSpacesEntityJSON.json";

  private static final String EXPECTED_TEMPLATE_FILE = "<messageML>\n"
      + "    <div class=\"entity\" data-entity-id=\"jiraIssue\">\n"
      + "        <card class=\"barStyle\" accent=\"green\" iconSrc=\"${entity['jiraIssue'].icon"
      + ".url}\">\n"
      + "            <header>\n"
      + "                <p>\n"
      + "                    <img src=\"${entity['jiraIssue'].issue.priority.iconUrl}\" "
      + "class=\"icon\" />\n"
      + "                    "
      + "<a class=\"tempo-text-color--link\" href=\"${entity['jiraIssue'].issue.url}\">${entity"
      + "['jiraIssue'].issue.key}</a>\n"
      + "                    <span class=\"tempo-text-color--normal\">${entity['jiraIssue'].issue"
      + ".subject} - </span>\n"
      + "                    <#if (entity['jiraIssue'].user.id)??>\n"
      + "                        <mention email=\"${entity['jiraIssue'].user.emailAddress}\" />\n"
      + "                    <#else>\n"
      + "                        <span>${entity['jiraIssue'].user.displayName}</span>\n"
      + "                    </#if>\n"
      + "                    <span class=\"tempo-text-color--green\">${entity['jiraIssue'].issue"
      + ".action}</span>\n"
      + "                </p>\n"
      + "            </header>\n"
      + "            <body>\n"
      + "                <div class=\"labelBackground badge\">\n"
      + "                    <p>\n"
      + "                        <#if (entity['jiraIssue'].issue.description)??>\n"
      + "                            <span "
      + "class=\"tempo-text-color--secondary\">Description:</span>\n"
      + "                            <span "
      + "class=\"tempo-text-color--normal\">${entity['jiraIssue'].issue.description}</span>\n"
      + "                        </#if>\n"
      + "\n"
      + "                        <br/>\n"
      + "                        <span class=\"tempo-text-color--secondary\">Assignee:</span>\n"
      + "                        <#if (entity['jiraIssue'].issue.assignee.id)??>\n"
      + "                            <mention email=\"${entity['jiraIssue'].issue.assignee"
      + ".emailAddress}\" />\n"
      + "                        <#else>\n"
      + "                            <span "
      + "class=\"tempo-text-color--normal\">${entity['jiraIssue'].issue.assignee"
      + ".displayName}</span>\n"
      + "                        </#if>\n"
      + "                    </p>\n"
      + "                    <p>\n"
      + "                        <span class=\"label\">Type:</span>\n"
      + "                        <img src=\"${entity['jiraIssue'].issue.issueType.iconUrl}\" "
      + "class=\"icon\" />\n"
      + "                        <span class=\"tempo-text-color--normal\">${entity['jiraIssue']"
      + ".issue.issueType.name}</span>\n"
      + "\n"
      + "                        <br/>\n"
      + "                        <span class=\"tempo-text-color--secondary\">Priority:</span>\n"
      + "                        <img src=\"${entity['jiraIssue'].issue.priority.iconUrl}\" "
      + "class=\"icon\" />\n"
      + "                        <span class=\"tempo-text-color--normal\">${entity['jiraIssue']"
      + ".issue.priority.name}</span>\n"
      + "\n"
      + "\n"
      + "                        <#if (entity['jiraIssue'].issue.epic)??>\n"
      + "                            <br/>\n"
      + "                            <span class=\"tempo-text-color--secondary\">Epic:</span>\n"
      + "                            <a href=\"${entity['jiraIssue'].issue.epic.link}\">${entity"
      + "['jiraIssue'].issue.epic.name}</a>\n"
      + "                        </#if>\n"
      + "\n"
      + "                        <br/>\n"
      + "                        <span class=\"tempo-text-color--secondary\">Status:</span>\n"
      + "                        <span class=\"tempo-text-color--normal\">${entity['jiraIssue']"
      + ".issue.status?capitalize}</span>\n"
      + "\n"
      + "\n"
      + "                        <#if (entity['jiraIssue'].issue.labels)??>\n"
      + "                            <br/>\n"
      + "                            <span class=\"tempo-text-color--secondary\">Labels:</span>\n"
      + "                            <#list entity['jiraIssue'].issue.labels as label>\n"
      + "                                <span class=\"hashTag\">#${label.text}</span>\n"
      + "                            </#list>\n"
      + "                        </#if>\n"
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
  protected Class<IssueStateMetadataParser> getParserClass() {
    return IssueStateMetadataParser.class;
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
  public void testIssueUpdatedEmailWithSpaces() throws IOException, JiraParserException {
    mockUserInfo();
    testParser(FILE_ISSUE_UPDATED_EMAIL_WITH_SPACES, FILE_EXPECTED_ISSUE_UPDATED_EMAIL_WITH_SPACES);
  }
}
