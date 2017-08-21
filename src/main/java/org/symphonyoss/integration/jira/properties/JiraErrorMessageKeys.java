package org.symphonyoss.integration.jira.properties;

import org.symphonyoss.integration.jira.api.JiraApiResource;

/**
 * Exception message keys used by the component {@link JiraApiResource}
 *
 * Created by hamitay on 8/15/17.
 */
public class JiraErrorMessageKeys {

  public static String APPLICATION_KEY_ERROR = "integration.jira.private.key.validation";

  public static String INVALID_URL_ERROR = "integration.jira.url.api.invalid";

  public static String MISSING_FIELD = "integration.jira.missing.field";

  public static String MISSING_FIELD_SOLUTION = MISSING_FIELD + ".solution";

  public static String EMPTY_ACCESS_TOKEN = "integration.jira.missing.accessToken";

  public static String EMPTY_ACCESS_TOKEN_SOLUTION = EMPTY_ACCESS_TOKEN + ".solution";

  public static String ISSUEKEY_NOT_FOUND = "integration.jira.issueKey.not.found";

  public static String ISSUEKEY_NOT_FOUND_SOLUTION = ISSUEKEY_NOT_FOUND + ".solution";

  public static String USERNAME_INVALID = "integration.jira.username.invalid";

  public static String USERNAME_INVALID_SOLUTION = USERNAME_INVALID + ".solution";

  public static final String INTEGRATION_UNAVAILABLE = "integration.jira.unavailable";

  public static final String INTEGRATION_UNAVAILABLE_SOLUTION = INTEGRATION_UNAVAILABLE + ".solution";

  public static final String INTEGRATION_UNAUTHORIZED = "integration.jira.unauthorized";

  public static final String INTEGRATION_UNAUTHORIZED_SOLUTION = INTEGRATION_UNAUTHORIZED + ".solution";

  public static String COMPONENT = "JIRA API";

  public static final String BUNDLE_FILENAME = "integration-jira-log-messages";

}
