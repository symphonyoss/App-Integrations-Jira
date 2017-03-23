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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.mockito.Mock;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.service.UserService;

import java.io.File;
import java.io.IOException;

/**
 * Created by rsanchez on 22/07/16.
 */
@Ignore("not a test per se")
public class JiraParserTest {

  @Mock
  protected UserService userService;

  @Before
  public void setup() {
    when(userService.getUserByEmail(anyString(), anyString())).thenReturn(createEntityUser());

  }

  private org.symphonyoss.integration.entity.model.User createEntityUser() {
    org.symphonyoss.integration.entity.model.User user =
        new org.symphonyoss.integration.entity.model.User();
    user.setId(7627861918843L);
    user.setUserName("test");
    user.setEmailAddress("test@symphony.com");
    user.setDisplayName("Test User");
    return user;
  }

  protected JsonNode readJsonFromFile(String filename) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    return JsonUtils.readTree(classLoader.getResourceAsStream(filename));
  }

  public String readFile(String fileName) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String expected =
        FileUtils.readFileToString(new File(classLoader.getResource(fileName).getPath()));
    return expected = expected.replaceAll("\n", "");
  }

}
