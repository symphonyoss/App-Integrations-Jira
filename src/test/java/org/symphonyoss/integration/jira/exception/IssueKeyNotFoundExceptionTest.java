package org.symphonyoss.integration.jira.exception;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit test for {@link IssueKeyNotFoundException}
 *
 * Created by alexandre-silva-daitan on 18/08/17.
 */
public class IssueKeyNotFoundExceptionTest {

  private static final String COMPONENT = "JIRA API";
  private static final String MESSAGE = "IssueKeyNotFoundException";
  private static final Throwable CAUSE = new Throwable();
  private static final String NO_SOLUTION =
      "No solution has been cataloged for troubleshooting this problem.";

  @Test
  public void testIssueKeyNotFoundExceptionTest() {
    IssueKeyNotFoundException exception =
        new IssueKeyNotFoundException(COMPONENT, MESSAGE, NO_SOLUTION);
    String resultMessage = exception.getMessage();
    String expectedMessage = "\n"
        + "Component: JIRA API\n"
        + "Message: IssueKeyNotFoundException\n"
        + "Solutions: \n"
        + "No solution has been cataloged for troubleshooting this problem.\n";

    assertEquals(expectedMessage, resultMessage);
  }

}