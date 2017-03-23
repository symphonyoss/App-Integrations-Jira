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

package org.symphonyoss.integration.webhook.jira.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.ISSUE_EVENT_TYPE_NAME;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.JIRA_ISSUE_COMMENTED;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.JIRA_ISSUE_CREATED;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.JIRA_ISSUE_UPDATED;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.WEBHOOK_EVENT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.message.MessageMLVersion;
import org.symphonyoss.integration.webhook.jira.parser.v1.CommentJiraParser;
import org.symphonyoss.integration.webhook.jira.parser.v1.IssueCreatedJiraParser;
import org.symphonyoss.integration.webhook.jira.parser.v1.IssueUpdatedJiraParser;
import org.symphonyoss.integration.webhook.jira.parser.v1.NullJiraParser;
import org.symphonyoss.integration.webhook.jira.parser.v1.V1JiraParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for {@link V1ParserFactory}
 * Created by rsanchez on 23/03/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class V1ParserFactoryTest {

  private static final String MOCK_INTEGRATION_TYPE = "mockType";

  @Spy
  private List<V1JiraParser> beans = new ArrayList<>();

  @Spy
  private NullJiraParser defaultJiraParser;

  @Spy
  private IssueCreatedJiraParser issueCreatedJiraParser;

  @Spy
  private IssueUpdatedJiraParser issueUpdatedJiraParser;

  @Spy
  private CommentJiraParser commentJiraParser;

  @InjectMocks
  private V1ParserFactory factory;

  @Before
  public void init() {
    beans.add(issueCreatedJiraParser);
    beans.add(issueUpdatedJiraParser);
    beans.add(commentJiraParser);
    beans.add(defaultJiraParser);

    factory.init();
  }

  @Test
  public void testNotAcceptable() {
    assertFalse(factory.accept(MessageMLVersion.V2));
  }

  @Test
  public void testAcceptable() {
    assertTrue(factory.accept(MessageMLVersion.V1));
  }

  @Test
  public void testOnConfigChange() {
    IntegrationSettings settings = new IntegrationSettings();
    settings.setType(MOCK_INTEGRATION_TYPE);

    factory.onConfigChange(settings);

    verify(issueCreatedJiraParser, times(1)).setJiraUser(MOCK_INTEGRATION_TYPE);
    verify(issueUpdatedJiraParser, times(1)).setJiraUser(MOCK_INTEGRATION_TYPE);
    verify(commentJiraParser, times(1)).setJiraUser(MOCK_INTEGRATION_TYPE);
    verify(defaultJiraParser, times(1)).setJiraUser(MOCK_INTEGRATION_TYPE);
  }

  @Test
  public void testCommentJiraParser() {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put(ISSUE_EVENT_TYPE_NAME, JIRA_ISSUE_COMMENTED);

    assertEquals(commentJiraParser, factory.getParser(node));
  }

  @Test
  public void testIssueCreatedJiraParser() {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put(WEBHOOK_EVENT, JIRA_ISSUE_CREATED);

    assertEquals(issueCreatedJiraParser, factory.getParser(node));
  }

  @Test
  public void testIssueUpdatedJiraParser() {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put(WEBHOOK_EVENT, JIRA_ISSUE_UPDATED);

    assertEquals(issueUpdatedJiraParser, factory.getParser(node));
  }

  @Test
  public void testDefaultJiraParser() {
    JsonNode node = JsonNodeFactory.instance.objectNode();
    assertEquals(defaultJiraParser, factory.getParser(node));
  }

}
