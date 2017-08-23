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
 * Unit test for {@link InvalidJiraURLException}
 *
 * Created by alexandre-silva-daitan on 18/08/17.
 */
public class InvalidJiraURLExceptionTest {

  private static final String COMPONENT = "JIRA API";
  private static final String MESSAGE = "InvalidJiraURLException";
  private static final Throwable CAUSE = new Throwable();

  @Test
  public void testInvalidJiraURLException() {
    InvalidJiraURLException exception = new InvalidJiraURLException(COMPONENT, MESSAGE, CAUSE.getCause());
    String resultMessage = exception.getMessage();
    String expectedMessage = "\n"
        + "Component: JIRA API\n"
        + "Message: InvalidJiraURLException\n"
        + "Solutions: \n"
        + "No solution has been cataloged for troubleshooting this problem.\n";

    assertEquals(expectedMessage,resultMessage);
  }
}