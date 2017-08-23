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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.json.JsonHttpContent;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Exception;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1HttpRequestException;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;
import org.symphonyoss.integration.jira.exception.InvalidJiraCommentException;
import org.symphonyoss.integration.jira.exception.InvalidJiraPayloadException;
import org.symphonyoss.integration.jira.exception.IssueKeyNotFoundException;
import org.symphonyoss.integration.jira.exception.JiraAuthorizationException;
import org.symphonyoss.integration.jira.exception.JiraUnexpectedException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Unit test for {@link IssueCommentService}
 *
 * Created by alexandre-silva-daitan on 22/08/17.
 */

@RunWith(MockitoJUnitRunner.class)
public class IssueCommentServiceTest {

  private static final String PATH_JIRA_API_COMMENT_ISSUE = "/rest/api/latest/issue/%s/comment";

  private static final String MOCK_ACCESS_TOKEN = "as4e435tdfst4302ds8dfs9883249328dsf9";

  private static final String ISSUE_KEY = "key";

  private static final String COMMENT = "{ \"body\": \"this is a comment\" }";

  private static final String MOCK_URL = "https://test.symphony.com";

  private static URL EXPECTED_URL;

  @Mock
  private OAuth1Provider provider;

  private IssueCommentService service = new IssueCommentService();

  @BeforeClass
  public static void init() throws MalformedURLException {
    String path = String.format(PATH_JIRA_API_COMMENT_ISSUE, ISSUE_KEY);

    EXPECTED_URL = new URL(new URL(MOCK_URL), path);
  }

  @Test(expected = IssueKeyNotFoundException.class)
  public void testInvalidIssueKey() {
    service.addCommentToAnIssue(MOCK_ACCESS_TOKEN, null, MOCK_URL, provider, COMMENT);
  }

  @Test(expected = IssueKeyNotFoundException.class)
  public void testIssueKeyNotFound() throws OAuth1Exception, OAuth1HttpRequestException {
    OAuth1HttpRequestException e = new OAuth1HttpRequestException("Issue not found", 404);

    doThrow(e).when(provider)
        .makeAuthorizedRequest(eq(MOCK_ACCESS_TOKEN), eq(EXPECTED_URL), eq(HttpMethods.POST),
            any(JsonHttpContent.class));

    service.addCommentToAnIssue(MOCK_ACCESS_TOKEN, ISSUE_KEY, MOCK_URL, provider, COMMENT);
  }

  @Test(expected = JiraAuthorizationException.class)
  public void testUserUnauthorized() throws OAuth1Exception, OAuth1HttpRequestException {
    OAuth1HttpRequestException e = new OAuth1HttpRequestException("User unauthorized", 401);

    doThrow(e).when(provider)
        .makeAuthorizedRequest(eq(MOCK_ACCESS_TOKEN), eq(EXPECTED_URL), eq(HttpMethods.POST),
            any(JsonHttpContent.class));

    service.addCommentToAnIssue(MOCK_ACCESS_TOKEN, ISSUE_KEY, MOCK_URL, provider, COMMENT);
  }

  @Test(expected = JiraUnexpectedException.class)
  public void testUnexpectedHttpError() throws OAuth1Exception, OAuth1HttpRequestException {
    OAuth1HttpRequestException e = new OAuth1HttpRequestException("Unexpected error", 500);

    doThrow(e).when(provider)
        .makeAuthorizedRequest(eq(MOCK_ACCESS_TOKEN), eq(EXPECTED_URL), eq(HttpMethods.POST),
            any(JsonHttpContent.class));

    service.addCommentToAnIssue(MOCK_ACCESS_TOKEN, ISSUE_KEY, MOCK_URL, provider, COMMENT);
  }

  @Test(expected = JiraUnexpectedException.class)
  public void testUnexpectedException() throws OAuth1Exception, OAuth1HttpRequestException {
    doThrow(OAuth1Exception.class).when(provider)
        .makeAuthorizedRequest(eq(MOCK_ACCESS_TOKEN), eq(EXPECTED_URL), eq(HttpMethods.POST),
            any(JsonHttpContent.class));

    service.addCommentToAnIssue(MOCK_ACCESS_TOKEN, ISSUE_KEY, MOCK_URL, provider, COMMENT);
  }

  @Test(expected = InvalidJiraPayloadException.class)
  public void testInvalidJiraCommentException() throws OAuth1Exception, OAuth1HttpRequestException {
    doThrow(OAuth1Exception.class).when(provider)
        .makeAuthorizedRequest(eq(MOCK_ACCESS_TOKEN), eq(EXPECTED_URL), eq(HttpMethods.POST),
            any(JsonHttpContent.class));

    service.addCommentToAnIssue(MOCK_ACCESS_TOKEN, ISSUE_KEY, MOCK_URL, provider, "");
  }

  @Test(expected = InvalidJiraCommentException.class)
  public void testEmptyJiraCommentException() throws OAuth1Exception, OAuth1HttpRequestException {
    doThrow(OAuth1Exception.class).when(provider)
        .makeAuthorizedRequest(eq(MOCK_ACCESS_TOKEN), eq(EXPECTED_URL), eq(HttpMethods.POST),
            any(JsonHttpContent.class));

    service.addCommentToAnIssue(MOCK_ACCESS_TOKEN, ISSUE_KEY, MOCK_URL, provider, "{}");
  }

  @Test
  public void testSuccess() throws OAuth1Exception, OAuth1HttpRequestException {
    ResponseEntity responseEntity =
        service.addCommentToAnIssue(MOCK_ACCESS_TOKEN, ISSUE_KEY, MOCK_URL, provider, COMMENT);

    assertEquals(ResponseEntity.ok(HttpStatus.OK), responseEntity);
  }

}