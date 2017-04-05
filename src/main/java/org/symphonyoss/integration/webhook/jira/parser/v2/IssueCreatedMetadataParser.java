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

import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.JIRA_ISSUE_CREATED;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * This class is responsible to validate the event 'jira:issue_created' sent by JIRA Webhook when
 * the Agent version is equal to or greater than '1.46.0'.
 *
 * Created by rsanchez on 30/03/17.
 */
@Component
public class IssueCreatedMetadataParser extends MetadataParser {

  private static final String METADATA_FILE = "metadataIssueCreated.xml";

  private static final String TEMPLATE_FILE = "templateIssueCreated.xml";

  private static final String JIRA_ISSUE_CREATED_NAME = "jiraIssueCreated";

  private static final String EVENT_TYPE = "com.symphony.integration.jira.event.v2.created";

  @Override
  protected String getTemplateFile() {
    return TEMPLATE_FILE;
  }

  @Override
  protected String getMetadataFile() {
    return METADATA_FILE;
  }

  @Override
  protected String getEventName() {
    return JIRA_ISSUE_CREATED_NAME;
  }

  @Override
  protected String getEventType() {
    return EVENT_TYPE;
  }

  @Override
  public List<String> getEvents() {
    return Arrays.asList(JIRA_ISSUE_CREATED);
  }
}
