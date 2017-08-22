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

package org.symphonyoss.integration.jira.services;

import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys
    .BODY_PATH_CONTENT_NOT_FOUND;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys
    .BODY_PATH_CONTENT_NOT_FOUND_SOLUTION;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys.BUNDLE_FILENAME;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys
    .INTEGRATION_UNAUTHORIZED;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys
    .INTEGRATION_UNAUTHORIZED_SOLUTION;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys.ISSUEKEY_NOT_FOUND;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys
    .ISSUEKEY_NOT_FOUND_SOLUTION;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys.MISSING_FIELD;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys
    .MISSING_FIELD_SOLUTION;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys.USERNAME_INVALID;
import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys
    .USERNAME_INVALID_SOLUTION;

import org.apache.commons.lang3.StringUtils;
import org.symphonyoss.integration.jira.exception.BodyContentNoFoundException;
import org.symphonyoss.integration.jira.exception.IssueKeyNotFoundException;
import org.symphonyoss.integration.jira.exception.JiraAuthorizationException;
import org.symphonyoss.integration.jira.exception.JiraUserNotFoundException;
import org.symphonyoss.integration.logging.MessageUtils;

/**
 * Common service class to interact with the JIRA API's.
 *
 * Created by rsanchez on 18/08/17.
 */
public abstract class CommonJiraService {

  private static final MessageUtils MSG = new MessageUtils(BUNDLE_FILENAME);

  private static final String ISSUE_KEY = "issueKey";

  /**
   * Validate the issue key parameter.
   * @param issueKey Issue key
   */
  public void validateIssueKeyParameter(String issueKey) {
    if (StringUtils.isEmpty(issueKey)) {
      String message = MSG.getMessage(MISSING_FIELD, ISSUE_KEY);
      String solution = MSG.getMessage(MISSING_FIELD_SOLUTION, ISSUE_KEY);

      throw new IssueKeyNotFoundException(getServiceName(), message, solution);
    }
  }

  /**
   * Thrown {@link IssueKeyNotFoundException} exception
   * @param issueKey Issue key
   */
  public void handleIssueNotFound(String issueKey) {
    String message = MSG.getMessage(ISSUEKEY_NOT_FOUND, issueKey);
    String solution = MSG.getMessage(ISSUEKEY_NOT_FOUND_SOLUTION, issueKey);

    throw new IssueKeyNotFoundException(getServiceName(), message, solution);
  }

  /**
   * Thrown {@link JiraUserNotFoundException} exception
   * @param username Username
   */
  public void handleUserNotFound(String username) {
    String message = MSG.getMessage(USERNAME_INVALID, username);
    String solution = MSG.getMessage(USERNAME_INVALID_SOLUTION, username);

    throw new JiraUserNotFoundException(getServiceName(), message, solution);
  }

  /**
   * Thrown {@link JiraAuthorizationException} exception
   */
  public void handleUserUnauthorized() {
    String message = MSG.getMessage(INTEGRATION_UNAUTHORIZED);
    String solution = MSG.getMessage(INTEGRATION_UNAUTHORIZED_SOLUTION);

    throw new JiraAuthorizationException(getServiceName(), message, solution);
  }

  /**
   * Thrown {@link BodyContentNoFoundException} exception
   */
  public void handleCommentNotFound() {
      String message = MSG.getMessage(BODY_PATH_CONTENT_NOT_FOUND);
      String solution = MSG.getMessage(BODY_PATH_CONTENT_NOT_FOUND_SOLUTION);
      throw new BodyContentNoFoundException(getServiceName(), message, solution);
  }

  /**
   * Retrieves the service name
   * @return Service name
   */
  protected abstract String getServiceName();

}


