package org.symphonyoss.integration.jira.exception;

import static org.junit.Assert.*;

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