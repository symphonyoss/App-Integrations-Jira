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
import org.springframework.http.HttpStatus;
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
 * Unit test for {@link IssueSearchService}
 *
 * Created by lpimentel on 18/10/17.
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({IssueSearchService.class, HttpResponse.class})
public class IssueSearchServiceTest {
  private static final String PATH_JIRA_API_COMMENT_ISSUE = "/rest/api/latest/issue/%s";

  private static final String MOCK_ACCESS_TOKEN = "as4e435tdfst4302ds8dfs9883249328dsf9";

  private static final String ISSUE_KEY = "key";

  private static final String MOCK_URL = "https://test.symphony.com";

  private static final String SAMPLE_ISSUE = "{\"fields\":{\"assignee\": \"Me\"}}";

  private static URL EXPECTED_URL;

  @Mock
  private OAuth1Provider provider;

  @Mock
  private HttpResponse response;

  private IssueSearchService service = new IssueSearchService();

  @BeforeClass
  public static void init() throws MalformedURLException {
    String path = String.format(PATH_JIRA_API_COMMENT_ISSUE, ISSUE_KEY);

    EXPECTED_URL = new URL(new URL(MOCK_URL), path);
  }


  @Test(expected = IssueKeyNotFoundException.class)
  public void testInvalidIssueKey() {
    service.getIssueInfo(MOCK_ACCESS_TOKEN, provider, MOCK_URL, null);
  }

  @Test(expected = InvalidJiraURLException.class)
  public void testInvalidBaseURL() {
    service.getIssueInfo(MOCK_ACCESS_TOKEN, provider, "", ISSUE_KEY);
  }

  @Test(expected = IssueKeyNotFoundException.class)
  public void testIssueKeyNotFound() throws OAuth1Exception, OAuth1HttpRequestException {
    OAuth1HttpRequestException e = new OAuth1HttpRequestException("Issue not found", 404);

    doThrow(e).when(provider)
        .makeAuthorizedRequest(MOCK_ACCESS_TOKEN, EXPECTED_URL, HttpMethods.GET, null);

    service.getIssueInfo(MOCK_ACCESS_TOKEN, provider, MOCK_URL, ISSUE_KEY);
  }

  @Test(expected = JiraAuthorizationException.class)
  public void testUserUnauthorized() throws OAuth1Exception, OAuth1HttpRequestException {
    OAuth1HttpRequestException e = new OAuth1HttpRequestException("User unauthorized", 401);

    doThrow(e).when(provider)
        .makeAuthorizedRequest(MOCK_ACCESS_TOKEN, EXPECTED_URL, HttpMethods.GET, null);

    service.getIssueInfo(MOCK_ACCESS_TOKEN, provider, MOCK_URL, ISSUE_KEY);
  }

  @Test(expected = JiraUnexpectedException.class)
  public void testUnexpectedHttpError() throws OAuth1Exception, OAuth1HttpRequestException {
    OAuth1HttpRequestException e = new OAuth1HttpRequestException("Unexpected error", 500);

    doThrow(e).when(provider)
        .makeAuthorizedRequest(MOCK_ACCESS_TOKEN, EXPECTED_URL, HttpMethods.GET, null);

    service.getIssueInfo(MOCK_ACCESS_TOKEN, provider, MOCK_URL, ISSUE_KEY);
  }

  @Test(expected = JiraUnexpectedException.class)
  public void testUnexpectedException() throws OAuth1Exception, OAuth1HttpRequestException {
    doThrow(OAuth1Exception.class).when(provider)
        .makeAuthorizedRequest(MOCK_ACCESS_TOKEN, EXPECTED_URL, HttpMethods.GET, null);

    service.getIssueInfo(MOCK_ACCESS_TOKEN, provider, MOCK_URL, ISSUE_KEY);
  }

  @Test
  public void testSuccess() throws OAuth1Exception, OAuth1HttpRequestException, IOException {
    doReturn(response).when(provider)
        .makeAuthorizedRequest(MOCK_ACCESS_TOKEN, EXPECTED_URL, HttpMethods.GET, null);
    doReturn(SAMPLE_ISSUE).when(response).parseAsString();
    ResponseEntity responseEntity =
        service.getIssueInfo(MOCK_ACCESS_TOKEN, provider, MOCK_URL, ISSUE_KEY);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertEquals(SAMPLE_ISSUE, responseEntity.getBody());
  }
}