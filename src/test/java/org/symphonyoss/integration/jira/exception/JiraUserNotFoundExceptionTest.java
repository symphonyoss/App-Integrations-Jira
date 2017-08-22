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

package org.symphonyoss.integration.jira.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for {@link JiraUserNotFoundException}
 *
 * Created by alexandre-silva-daitan on 18/08/17.
 */
public class JiraUserNotFoundExceptionTest {

  private static final String COMPONENT = "JIRA API";
  private static final String MESSAGE = "User abc123 does not exist.";
  private static final String NO_SOLUTION = "Make sure the user abc123 exists in the JIRA system.";

  @Test
  public void testJiraUserNotFoundExceptionTest() {
    JiraUserNotFoundException exception = new JiraUserNotFoundException(COMPONENT, MESSAGE, NO_SOLUTION);
    String resultMessage = exception.getMessage();
    String expectedMessage = "\n"
        + "Component: JIRA API\n"
        + "Message: User abc123 does not exist.\n"
        + "Solutions: \n"
        + "Make sure the user abc123 exists in the JIRA system.\n";

    assertEquals(expectedMessage,resultMessage);
  }

}