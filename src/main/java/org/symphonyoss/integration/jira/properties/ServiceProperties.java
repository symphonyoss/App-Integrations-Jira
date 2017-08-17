package org.symphonyoss.integration.jira.properties;

import org.symphonyoss.integration.jira.api.JiraApiResource;

/**
 * Exception message keys used by the component {@link JiraApiResource}
 *
 * Created by hamitay on 8/15/17.
 */
public class ServiceProperties {

  public static String APPLICATION_KEY_ERROR = "integration.jira.private.key.validation";

  public static String INVALID_URL_ERROR = "integration.jira.url.api.invalid";

  public static String INVALID_BASE_URL = "integration.jira.url.base.invalid";

  public static String INVALID_BASE_URL_SOLUTION = INVALID_BASE_URL + ".solution";

  public static String MISSING_FIELD = "integration.jira.missing.field";

  public static String EMPTY_ACCESS_TOKEN = "integration.jira.missing.accessToken";

  public static String ISSUEKEY_NOT_FOUND = "integration.jira.issueKey.not.found";

}
