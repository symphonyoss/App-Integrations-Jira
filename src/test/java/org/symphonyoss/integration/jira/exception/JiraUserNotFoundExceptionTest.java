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