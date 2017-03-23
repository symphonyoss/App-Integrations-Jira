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
import static org.junit.Assert.assertNotNull;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.USER_KEY_PARAMETER;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.NAME_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.PROJECT_PATH;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.webhook.jira.parser.JiraParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rsanchez on 22/07/16.
 */
public class ProjectJiraParserTest {

  private static final String CREATE_PROJECT_FILENAME = "jiraCallbackSampleProjectCreated.json";

  private static final String UPDATE_PROJECT_FILENAME = "jiraCallbackSampleProjectUpdated.json";

  private static final String DELETE_PROJECT_FILENAME = "jiraCallbackSampleProjectDeleted.json";

  private ProjectJiraParser projectJiraParser = new ProjectJiraParser();

  @Test
  public void testProjectCreated() throws IOException, JiraParserException {
    ClassLoader classLoader = getClass().getClassLoader();

    Map<String, String> parameters = new HashMap<>();
    parameters.put(USER_KEY_PARAMETER, "test");

    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(CREATE_PROJECT_FILENAME));
    String result = projectJiraParser.parse(parameters, node);

    assertNotNull(result);
    assertEquals("test created Project <b>Project One</b>", result);
  }

  @Test
  public void testProjectUpdated() throws IOException, JiraParserException {
    ClassLoader classLoader = getClass().getClassLoader();

    Map<String, String> parameters = new HashMap<>();
    parameters.put(USER_KEY_PARAMETER, "test");

    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(UPDATE_PROJECT_FILENAME));
    String result = projectJiraParser.parse(parameters, node);

    assertNotNull(result);
    assertEquals("test updated Project <b>Project One</b>", result);
  }

  @Test
  public void testProjectDeleted() throws IOException, JiraParserException {
    ClassLoader classLoader = getClass().getClassLoader();

    Map<String, String> parameters = new HashMap<>();
    parameters.put(USER_KEY_PARAMETER, "test");

    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(DELETE_PROJECT_FILENAME));
    String result = projectJiraParser.parse(parameters, node);

    assertNotNull(result);
    assertEquals("test deleted Project <b>Project One</b>", result);
  }

  @Test
  public void testProjectWithoutUser() throws IOException, JiraParserException {
    ClassLoader classLoader = getClass().getClassLoader();

    Map<String, String> parameters = new HashMap<>();

    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(CREATE_PROJECT_FILENAME));
    String result = projectJiraParser.parse(parameters, node);

    assertNotNull(result);
    assertEquals("Project <b>Project One</b> created", result);

  }

  @Test
  public void testProjectWithoutProjectName() throws IOException, JiraParserException {
    ClassLoader classLoader = getClass().getClassLoader();

    Map<String, String> parameters = new HashMap<>();

    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(CREATE_PROJECT_FILENAME));
    ObjectNode projectNode = (ObjectNode) node.path(PROJECT_PATH);
    projectNode.remove(NAME_PATH);

    String result = projectJiraParser.parse(parameters, node);

    assertNotNull(result);
    assertEquals("Project <b>Unknown Project</b> created", result);
  }

}
