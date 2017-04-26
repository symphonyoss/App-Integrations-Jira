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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.service.UserService;

import java.util.Arrays;
import java.util.List;

/**
 * This class is responsible to validate the event 'jira:issue_created' sent by JIRA Webhook when
 * the Agent version is equal to or greater than '1.46.0'.
 *
 * Created by rsanchez on 30/03/17.
 */
@Component
public class IssueCreatedMetadataParser extends JiraMetadataParser {

  private static final String METADATA_FILE = "metadataIssueCreated.xml";

  private static final String TEMPLATE_FILE = "templateIssueCreated.xml";

  @Autowired
  public IssueCreatedMetadataParser(UserService userService) {
    super(userService);
  }

  @Override
  protected String getTemplateFile() {
    return TEMPLATE_FILE;
  }

  @Override
  protected String getMetadataFile() {
    return METADATA_FILE;
  }

  @Override
  public List<String> getEvents() {
    return Arrays.asList(JIRA_ISSUE_CREATED);
  }
}
