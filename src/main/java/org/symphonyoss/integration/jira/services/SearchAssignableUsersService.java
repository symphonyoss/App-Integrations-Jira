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

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Exception;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1HttpRequestException;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;
import org.symphonyoss.integration.jira.exception.JiraUnexpectedException;

import java.io.IOException;
import java.net.URL;

/**
 * Service to get a list of potential assignable users from a specific issue.
 *
 * Created by alexandre-silva-daitan on 15/08/17.
 */
@Component
public class SearchAssignableUsersService extends CommonJiraService {

  private static final String SERVICE_NAME = "Search Assignable Users Service";

  public static final String PATH_JIRA_API_SEARCH_USERS =
      "rest/api/latest/user/assignable/search?issueKey=%s&username=%s&maxResults=%s";

  public ResponseEntity searchAssingablesUsers(String accessToken, OAuth1Provider provider,
      URL assignableUserUrl, String issueKey) {

    validateIssueKeyParameter(issueKey);

    try {
      HttpResponse response =
          provider.makeAuthorizedRequest(accessToken, assignableUserUrl, HttpMethods.GET, null);
      return ResponseEntity.ok().body(response.parseAsString());
    } catch (OAuth1HttpRequestException e) {
      if (e.getCode() == HttpStatus.NOT_FOUND.value()) {
        handleIssueNotFound(issueKey);
      }

      if (e.getCode() == HttpStatus.UNAUTHORIZED.value()) {
        handleUserUnauthorized();
      }

      throw new JiraUnexpectedException(SERVICE_NAME, e.getMessage(), e);
    } catch (OAuth1Exception | IOException e) {
      throw new JiraUnexpectedException(SERVICE_NAME, e.getMessage(), e);
    }
  }

  @Override
  protected String getServiceName() {
    return SERVICE_NAME;
  }
}


