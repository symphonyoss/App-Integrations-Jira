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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpResponse;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.ResponseEntity;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Exception;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1HttpRequestException;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;
import org.symphonyoss.integration.jira.exception.InvalidJiraURLException;
import org.symphonyoss.integration.jira.exception.IssueKeyNotFoundException;
import org.symphonyoss.integration.jira.exception.JiraAuthorizationException;
import org.symphonyoss.integration.jira.exception.JiraUnexpectedException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Unit test for {@link SearchAssignableUsersService}
 * Created by rsanchez on 18/08/17.
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({ SearchAssignableUsersService.class, HttpResponse.class})
public class SearchAssignableUsersServiceTest {

  private static final String PATH_JIRA_API_SEARCH_USERS =
      "rest/api/latest/user/assignable/search?issueKey=%s&username=%s&maxResults=%s";

  private static final String MOCK_ACCESS_TOKEN = "as4e435tdfst4302ds8dfs9883249328dsf9";

  private static final String ISSUE_KEY = "key";

  private static final String USERNAME = "username";

  private static final String MOCK_URL = "https://test.symphony.com";

  private static URL EXPECTED_URL;

  @Mock
  private OAuth1Provider provider;

  @Mock
  private HttpResponse response;

  private SearchAssignableUsersService service = new SearchAssignableUsersService();

  @BeforeClass
  public static void init() throws MalformedURLException {
    String path = String.format(PATH_JIRA_API_SEARCH_USERS, ISSUE_KEY, USERNAME, null);

    EXPECTED_URL = new URL(new URL(MOCK_URL), path);
  }

  @Test(expected = IssueKeyNotFoundException.class)
  public void testInvalidIssueKey() {
    service.searchAssingablesUsers(MOCK_ACCESS_TOKEN, provider, MOCK_URL, null, USERNAME);
  }

  @Test(expected = InvalidJiraURLException.class)
  public void testInvalidBaseURL() {
    service.searchAssingablesUsers(MOCK_ACCESS_TOKEN, provider, "", ISSUE_KEY, USERNAME);
  }

  @Test(expected = IssueKeyNotFoundException.class)
  public void testIssueKeyNotFound() throws OAuth1Exception, OAuth1HttpRequestException {
    OAuth1HttpRequestException e = new OAuth1HttpRequestException("Issue not found", 404);

    doThrow(e).when(provider).makeAuthorizedRequest(MOCK_ACCESS_TOKEN, EXPECTED_URL, HttpMethods.GET, null);

    service.searchAssingablesUsers(MOCK_ACCESS_TOKEN, provider, MOCK_URL, ISSUE_KEY, USERNAME);
  }

  @Test(expected = JiraAuthorizationException.class)
  public void testUserUnauthorized() throws OAuth1Exception, OAuth1HttpRequestException {
    OAuth1HttpRequestException e = new OAuth1HttpRequestException("User unauthorized", 401);

    doThrow(e).when(provider).makeAuthorizedRequest(MOCK_ACCESS_TOKEN, EXPECTED_URL, HttpMethods.GET, null);

    service.searchAssingablesUsers(MOCK_ACCESS_TOKEN, provider, MOCK_URL, ISSUE_KEY, USERNAME);
  }

  @Test(expected = JiraUnexpectedException.class)
  public void testUnexpectedHttpError() throws OAuth1Exception, OAuth1HttpRequestException {
    OAuth1HttpRequestException e = new OAuth1HttpRequestException("Unexpected error", 500);

    doThrow(e).when(provider).makeAuthorizedRequest(MOCK_ACCESS_TOKEN, EXPECTED_URL, HttpMethods.GET, null);

    service.searchAssingablesUsers(MOCK_ACCESS_TOKEN, provider, MOCK_URL, ISSUE_KEY, USERNAME);
  }

  @Test(expected = JiraUnexpectedException.class)
  public void testUnexpectedException() throws OAuth1Exception, OAuth1HttpRequestException {
    doThrow(OAuth1Exception.class).when(provider)
        .makeAuthorizedRequest(MOCK_ACCESS_TOKEN, EXPECTED_URL, HttpMethods.GET, null);

    service.searchAssingablesUsers(MOCK_ACCESS_TOKEN, provider, MOCK_URL, ISSUE_KEY, USERNAME);
  }

  @Test
  public void testSuccess() throws OAuth1Exception, OAuth1HttpRequestException, IOException {
    doReturn(response).when(provider).makeAuthorizedRequest(MOCK_ACCESS_TOKEN, EXPECTED_URL, HttpMethods.GET, null);
    doReturn("OK").when(response).parseAsString();

    ResponseEntity responseEntity =
        service.searchAssingablesUsers(MOCK_ACCESS_TOKEN, provider, MOCK_URL, ISSUE_KEY, USERNAME);

    assertEquals(ResponseEntity.ok().body("OK"), responseEntity);
  }

}
