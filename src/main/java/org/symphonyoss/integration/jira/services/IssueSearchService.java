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
 * Service that retrieves issue info at JIRA API
 * Created by lpimentel on 8/15/17.
 */
@Component
public class IssueSearchService extends CommonJiraService {

  private static final String SERVICE_NAME = "Issue Service";

  private static final String PATH_JIRA_API_ISSUE = "/rest/api/latest/issue/%s";

  public ResponseEntity getIssueInfo(String accessToken, OAuth1Provider provider, String baseUrl,
      String issueKey) {

    validateIssueKeyParameter(issueKey);

    URL issueUrl = getServiceUrl(baseUrl, String.format(PATH_JIRA_API_ISSUE, issueKey));

    try {
      HttpResponse response =
          provider.makeAuthorizedRequest(accessToken, issueUrl, HttpMethods.GET, null);
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
