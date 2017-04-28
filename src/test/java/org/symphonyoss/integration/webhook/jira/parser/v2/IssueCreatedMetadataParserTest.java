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
import org.symphonyoss.integration.webhook.jira.parser.JiraParserTest;

import java.io.IOException;

/**
 * Unit test class for {@link IssueCreatedMetadataParser}
 * Created by rsanchez on 29/03/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class IssueCreatedMetadataParserTest extends JiraParserV2Test<IssueCreatedMetadataParser> {

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

  private String expectedTemplateFile = "<messageML>\n"
      + "    <div class=\"entity\">\n"
      + "        <card class=\"barStyle\">\n"
      + "            <header>\n"
      + "                <img src=\"${entity['jiraIssueCreated'].issue.priority.iconUrl}\" "
      + "class=\"icon\" />\n"
      + "                <a href=\"${entity['jiraIssueCreated'].issue.url}\">${entity"
      + "['jiraIssueCreated'].issue.key}</a>\n"
      + "                <span>${entity['jiraIssueCreated'].issue.subject} - </span>\n"
      + "                <#if (entity['jiraIssueCreated'].user.id)??>\n"
      + "                    <mention email=\"${entity['jiraIssueCreated'].user.emailAddress}\" "
      + "/>\n"
      + "                <#else>\n"
      + "                    <span>${entity['jiraIssueCreated'].user.displayName}</span>\n"
      + "                </#if>\n"
      + "                <span class=\"action\">Created</span>\n"
      + "            </header>\n"
      + "            <body>\n"
      + "                <div class=\"entity\" data-entity-id=\"jiraIssueCreated\">\n"
      + "                    <br/>\n"
      + "                    <div>\n"
      + "                        <span class=\"label\">Description:</span>\n"
      + "                        <span>${entity['jiraIssueCreated'].issue.description}</span>\n"
      + "                    </div>\n"
      + "                    <br/>\n"
      + "                    <div>\n"
      + "                        <span class=\"label\">Assignee:</span>\n"
      + "                        <#if (entity['jiraIssueCreated'].issue.assignee.id)??>\n"
      + "                            <mention email=\"${entity['jiraIssueCreated'].issue.assignee"
      + ".emailAddress}\" />\n"
      + "                        <#else>\n"
      + "                            <span>${entity['jiraIssueCreated'].issue.assignee"
      + ".displayName}</span>\n"
      + "                        </#if>\n"
      + "                    </div>\n"
      + "                    <hr/>\n"
      + "                    <div class=\"labelBackground badge\">\n"
      + "                            <span class=\"label\">Type:</span>\n"
      + "                            <img src=\"${entity['jiraIssueCreated'].issue.issueType"
      + ".iconUrl}\" class=\"icon\" />\n"
      + "                            <span>${entity['jiraIssueCreated'].issue.issueType"
      + ".name}</span>\n"
      + "                            <span class=\"label\">Priority:</span>\n"
      + "                            <img src=\"${entity['jiraIssueCreated'].issue.priority"
      + ".iconUrl}\" class=\"icon\" />\n"
      + "                            <span>${entity['jiraIssueCreated'].issue.priority"
      + ".name}</span>\n"
      + "                            <#if (entity['jiraIssueCreated'].issue.epic)??>\n"
      + "                                <span class=\"label\">Epic:</span>\n"
      + "                                "
      + "<a href=\"${entity['jiraIssueCreated'].issue.epic.link}\">${entity['jiraIssueCreated']"
      + ".issue.epic.name}</a>\n"
      + "                            </#if>\n"
      + "                            <span class=\"label\">Status:</span>\n"
      + "                            <span class=\"infoBackground "
      + "badge\">${entity['jiraIssueCreated'].issue.status}</span>\n"
      + "                            <#if (entity['jiraIssueCreated'].issue.labels)??>\n"
      + "                                <span class=\"label\">Labels:</span>\n"
      + "                                <#list entity['jiraIssueCreated'].issue.labels as label>\n"
      + "                                    <a class=\"hashTag\">#${label.text}</a>\n"
      + "                                </#list>\n"
      + "                            </#if>\n"
      + "                    </div>\n"
      + "                </div>\n"
      + "            </body>\n"
      + "        </card>\n"
      + "    </div>\n"
      + "</messageML>\n";

  @Override
  protected String getExpectedTemplate() {
    return expectedTemplateFile;
  }

  @Override
  protected Class<IssueCreatedMetadataParser> getParserClass() {
    return IssueCreatedMetadataParser.class;
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
}
