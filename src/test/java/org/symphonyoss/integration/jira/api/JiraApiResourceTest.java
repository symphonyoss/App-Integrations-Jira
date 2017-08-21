package org.symphonyoss.integration.jira.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.symphonyoss.integration.authentication.api.jwt.JwtAuthentication;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Exception;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;
import org.symphonyoss.integration.exception.IntegrationUnavailableException;
import org.symphonyoss.integration.jira.exception.InvalidJiraURLException;
import org.symphonyoss.integration.jira.exception.JiraAuthorizationException;
import org.symphonyoss.integration.jira.exception.JiraUnexpectedException;
import org.symphonyoss.integration.jira.services.SearchAssignableUsersService;
import org.symphonyoss.integration.jira.services.UserAssignService;
import org.symphonyoss.integration.jira.webhook.JiraWebHookIntegration;
import org.symphonyoss.integration.model.config.IntegrationSettings;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by hamitay on 8/16/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class JiraApiResourceTest {

  private static String ISSUE_KEY = "issueKey";

  private static String USERNAME = "username";

  private static String CONFIGURATION_ID = "configurationId";

  private static String AUTHORIZATION_HEADER = "authorizationHeader";

  private static String JIRA_INTEGRATION_URL = "https://test.symphony.com";

  private static String ACCESS_TOKEN = "accessToken";

  private static final Long USER_ID = 10L;

  private static final String PATH_JIRA_API_SEARCH_USERS =
      "rest/api/latest/user/assignable/search?issueKey=%s&username=%s&maxResults=%s";

  private static final String PATH_JIRA_API_ASSIGN_ISSUE =
      "/rest/api/latest/issue/%s/assignee";

  private JiraApiResource jiraApiResource;

  @Mock
  private JwtAuthentication jwtAuthentication;

  @Mock
  private UserAssignService userAssignService;

  @Mock
  private SearchAssignableUsersService searchAssignableUsersService;

  @Mock
  private JiraWebHookIntegration jiraWebHookIntegration;

  @Mock
  private OAuth1Provider provider;

  @Before
  public void prepareMockResource() throws AuthorizationException, MalformedURLException {
    String pathApiJiraUsersSearch = String.format(PATH_JIRA_API_SEARCH_USERS, ISSUE_KEY,
        USERNAME, null);

    URL jiraBaseUrl = new URL(JIRA_INTEGRATION_URL);

    URL assignableUserUrl = new URL(jiraBaseUrl, pathApiJiraUsersSearch);
    URL userAssigneeUrl = new URL(jiraBaseUrl, String.format(PATH_JIRA_API_ASSIGN_ISSUE, ISSUE_KEY));

    doReturn(USER_ID).when(jwtAuthentication).getUserIdFromAuthorizationHeader(AUTHORIZATION_HEADER);
    doReturn(new IntegrationSettings()).when(jiraWebHookIntegration).getSettings();
    doReturn(ACCESS_TOKEN).when(jiraWebHookIntegration).getAccessToken(JIRA_INTEGRATION_URL, USER_ID);
    doReturn(provider).when(jiraWebHookIntegration).getOAuth1Provider(JIRA_INTEGRATION_URL);

    doReturn(new ResponseEntity(HttpStatus.OK)).when(searchAssignableUsersService)
        .searchAssingablesUsers(ACCESS_TOKEN, provider, assignableUserUrl, ISSUE_KEY);

    doReturn(new ResponseEntity(HttpStatus.OK)).when(userAssignService)
        .assignUserToIssue(ACCESS_TOKEN, ISSUE_KEY, USERNAME, userAssigneeUrl, provider);

    jiraApiResource = new JiraApiResource(jiraWebHookIntegration, jwtAuthentication,
        userAssignService, searchAssignableUsersService);
  }

  @Test(expected = IntegrationUnavailableException.class)
  public void testSearchAssignableUserUnavailable() throws IOException {
    doReturn(null).when(jiraWebHookIntegration).getSettings();

    jiraApiResource.searchAssignableUsers(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER, JIRA_INTEGRATION_URL);
  }

  @Test(expected = JiraAuthorizationException.class)
  public void testSearchAssignableUserNullAccessToken() throws IOException {
    doReturn(0L).when(jwtAuthentication).getUserIdFromAuthorizationHeader(AUTHORIZATION_HEADER);
    jiraApiResource.searchAssignableUsers(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER, JIRA_INTEGRATION_URL);
  }

  @Test(expected = JiraUnexpectedException.class)
  public void testSearchAssignableUserAccessTokenFailed() throws IOException, AuthorizationException {
    doThrow(AuthorizationException.class).when(jiraWebHookIntegration).getAccessToken(JIRA_INTEGRATION_URL, USER_ID);
    jiraApiResource.searchAssignableUsers(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER, JIRA_INTEGRATION_URL);
  }

  @Test(expected = JiraAuthorizationException.class)
  public void testSearchAssignableUserProviderFailed() throws IOException, AuthorizationException {
    doThrow(OAuth1Exception.class).when(jiraWebHookIntegration).getOAuth1Provider(JIRA_INTEGRATION_URL);
    jiraApiResource.searchAssignableUsers(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER, JIRA_INTEGRATION_URL);
  }

  @Test(expected = InvalidJiraURLException.class)
  public void testSearchAssignableUserInvalidUrl() throws IOException, AuthorizationException {
    String url = "test";

    doReturn(ACCESS_TOKEN).when(jiraWebHookIntegration).getAccessToken(url, USER_ID);
    doReturn(provider).when(jiraWebHookIntegration).getOAuth1Provider(url);

    jiraApiResource.searchAssignableUsers(ISSUE_KEY, null, AUTHORIZATION_HEADER, url);
  }

  @Test
  public void testSearchAssignableUser() throws IOException {
    ResponseEntity expectedResponse = new ResponseEntity(HttpStatus.OK);

    ResponseEntity responseEntity =
        jiraApiResource.searchAssignableUsers(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER,
            JIRA_INTEGRATION_URL);

    assertEquals(expectedResponse, responseEntity);
  }

  @Test(expected = IntegrationUnavailableException.class)
  public void testAssignIssueUnavailable() throws IOException {
    doReturn(null).when(jiraWebHookIntegration).getSettings();

    jiraApiResource.assignIssueToUser(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER, JIRA_INTEGRATION_URL);
  }

  @Test(expected = JiraAuthorizationException.class)
  public void testAssignIssueNullAccessToken() throws IOException {
    doReturn(0L).when(jwtAuthentication).getUserIdFromAuthorizationHeader(AUTHORIZATION_HEADER);
    jiraApiResource.assignIssueToUser(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER, JIRA_INTEGRATION_URL);
  }

  @Test(expected = JiraUnexpectedException.class)
  public void testAssignIssueAccessTokenFailed() throws IOException, AuthorizationException {
    doThrow(AuthorizationException.class).when(jiraWebHookIntegration).getAccessToken(JIRA_INTEGRATION_URL, USER_ID);
    jiraApiResource.assignIssueToUser(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER, JIRA_INTEGRATION_URL);
  }

  @Test(expected = JiraAuthorizationException.class)
  public void testAssignIssueProviderFailed() throws IOException, AuthorizationException {
    doThrow(OAuth1Exception.class).when(jiraWebHookIntegration).getOAuth1Provider(JIRA_INTEGRATION_URL);
    jiraApiResource.assignIssueToUser(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER, JIRA_INTEGRATION_URL);
  }

  @Test(expected = InvalidJiraURLException.class)
  public void testAssignIssueInvalidUrl() throws IOException, AuthorizationException {
    String url = "test";

    doReturn(ACCESS_TOKEN).when(jiraWebHookIntegration).getAccessToken(url, USER_ID);
    doReturn(provider).when(jiraWebHookIntegration).getOAuth1Provider(url);

    jiraApiResource.assignIssueToUser(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER, url);
  }

  @Test
  public void testAssignIssue() throws IOException {
    ResponseEntity expectedResponse = new ResponseEntity(HttpStatus.OK);

    ResponseEntity responseEntity =
        jiraApiResource.assignIssueToUser(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER,
            JIRA_INTEGRATION_URL);

    assertEquals(expectedResponse, responseEntity);
  }
}
