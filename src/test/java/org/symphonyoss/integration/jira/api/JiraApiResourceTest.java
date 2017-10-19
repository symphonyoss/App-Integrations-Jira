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
import org.symphonyoss.integration.jira.exception.InvalidJiraPayloadException;
import org.symphonyoss.integration.jira.exception.MissingRequiredPayloadException;
import org.symphonyoss.integration.jira.exception.InvalidJiraURLException;
import org.symphonyoss.integration.jira.exception.JiraAuthorizationException;
import org.symphonyoss.integration.jira.exception.JiraUnexpectedException;
import org.symphonyoss.integration.jira.services.IssueCommentService;
import org.symphonyoss.integration.jira.services.IssueSearchService;
import org.symphonyoss.integration.jira.services.SearchAssignableUsersService;
import org.symphonyoss.integration.jira.services.UserAssignService;
import org.symphonyoss.integration.jira.webhook.JiraWebHookIntegration;
import org.symphonyoss.integration.model.config.IntegrationSettings;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Unit tests for {@link JiraApiResource}
 *
 * Created by hamitay on 8/16/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class JiraApiResourceTest {

  private static String COMMENT = "{\"body\":\"this is a comment\"}";

  private static String ISSUE_KEY = "issueKey";

  private static String USERNAME = "username";

  private static String CONFIGURATION_ID = "configurationId";

  private static String AUTHORIZATION_HEADER = "authorizationHeader";

  private static String JIRA_INTEGRATION_URL = "https://test.symphony.com";

  private static String ACCESS_TOKEN = "accessToken";

  private static final String SAMPLE_ISSUE = "{\"fields\":{\"assignee\": \"Me\"}}";

  private static final Long USER_ID = 10L;

  private JiraApiResource jiraApiResource;

  @Mock
  private JwtAuthentication jwtAuthentication;

  @Mock
  private UserAssignService userAssignService;

  @Mock
  private SearchAssignableUsersService searchAssignableUsersService;

  @Mock
  private IssueCommentService issueCommentService;

  @Mock
  private IssueSearchService issueSearchService;

  @Mock
  private JiraWebHookIntegration jiraWebHookIntegration;

  @Mock
  private OAuth1Provider provider;

  @Before
  public void prepareMockResource() throws AuthorizationException {
    IntegrationSettings settings = new IntegrationSettings();
    settings.setConfigurationId(CONFIGURATION_ID);

    doReturn(settings).when(jiraWebHookIntegration).getSettings();

    doReturn(USER_ID).when(jwtAuthentication)
        .getUserIdFromAuthorizationHeader(CONFIGURATION_ID, AUTHORIZATION_HEADER);

    doReturn(ACCESS_TOKEN).when(jiraWebHookIntegration)
        .getAccessToken(JIRA_INTEGRATION_URL, USER_ID);

    doReturn(provider).when(jiraWebHookIntegration).getOAuth1Provider(JIRA_INTEGRATION_URL);

    doReturn(new ResponseEntity(HttpStatus.OK)).when(searchAssignableUsersService)
        .searchAssingablesUsers(ACCESS_TOKEN, provider, JIRA_INTEGRATION_URL, ISSUE_KEY, USERNAME);

    doReturn(new ResponseEntity(HttpStatus.OK)).when(userAssignService)
        .assignUserToIssue(ACCESS_TOKEN, ISSUE_KEY, USERNAME, JIRA_INTEGRATION_URL, provider);

    doReturn(new ResponseEntity(HttpStatus.OK)).when(issueCommentService)
        .addCommentToAnIssue(ACCESS_TOKEN, ISSUE_KEY, JIRA_INTEGRATION_URL, provider, COMMENT);

    ResponseEntity issueResponseEntity = new ResponseEntity(SAMPLE_ISSUE, HttpStatus.OK);
    doReturn(issueResponseEntity).when(issueSearchService)
        .getIssueInfo(ACCESS_TOKEN, provider, JIRA_INTEGRATION_URL, ISSUE_KEY);

    jiraApiResource = new JiraApiResource(jiraWebHookIntegration, jwtAuthentication,
        userAssignService, searchAssignableUsersService, issueCommentService, issueSearchService);
  }

  @Test(expected = IntegrationUnavailableException.class)
  public void testSearchAssignableUserUnavailable() throws IOException {
    doReturn(null).when(jiraWebHookIntegration).getSettings();

    jiraApiResource.searchAssignableUsers(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER,
        JIRA_INTEGRATION_URL);
  }

  @Test(expected = JiraAuthorizationException.class)
  public void testSearchAssignableUserNullAccessToken() throws IOException {
    doReturn(0L).when(jwtAuthentication)
        .getUserIdFromAuthorizationHeader(CONFIGURATION_ID, AUTHORIZATION_HEADER);
    jiraApiResource.searchAssignableUsers(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER,
        JIRA_INTEGRATION_URL);
  }

  @Test(expected = JiraUnexpectedException.class)
  public void testSearchAssignableUserAccessTokenFailed()
      throws IOException, AuthorizationException {
    doThrow(AuthorizationException.class).when(jiraWebHookIntegration)
        .getAccessToken(JIRA_INTEGRATION_URL, USER_ID);
    jiraApiResource.searchAssignableUsers(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER,
        JIRA_INTEGRATION_URL);
  }

  @Test(expected = JiraAuthorizationException.class)
  public void testSearchAssignableUserProviderFailed() throws IOException, AuthorizationException {
    doThrow(OAuth1Exception.class).when(jiraWebHookIntegration)
        .getOAuth1Provider(JIRA_INTEGRATION_URL);
    jiraApiResource.searchAssignableUsers(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER,
        JIRA_INTEGRATION_URL);
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

    jiraApiResource.assignIssueToUser(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER,
        JIRA_INTEGRATION_URL);
  }

  @Test(expected = JiraAuthorizationException.class)
  public void testAssignIssueNullAccessToken() throws IOException {
    doReturn(0L).when(jwtAuthentication)
        .getUserIdFromAuthorizationHeader(CONFIGURATION_ID, AUTHORIZATION_HEADER);
    jiraApiResource.assignIssueToUser(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER,
        JIRA_INTEGRATION_URL);
  }

  @Test(expected = JiraUnexpectedException.class)
  public void testAssignIssueAccessTokenFailed() throws IOException, AuthorizationException {
    doThrow(AuthorizationException.class).when(jiraWebHookIntegration)
        .getAccessToken(JIRA_INTEGRATION_URL, USER_ID);
    jiraApiResource.assignIssueToUser(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER,
        JIRA_INTEGRATION_URL);
  }

  @Test(expected = JiraAuthorizationException.class)
  public void testAssignIssueProviderFailed() throws IOException, AuthorizationException {
    doThrow(OAuth1Exception.class).when(jiraWebHookIntegration)
        .getOAuth1Provider(JIRA_INTEGRATION_URL);
    jiraApiResource.assignIssueToUser(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER,
        JIRA_INTEGRATION_URL);
  }

  @Test
  public void testAssignIssue() throws IOException {
    ResponseEntity expectedResponse = new ResponseEntity(HttpStatus.OK);

    ResponseEntity responseEntity =
        jiraApiResource.assignIssueToUser(ISSUE_KEY, USERNAME, AUTHORIZATION_HEADER,
            JIRA_INTEGRATION_URL);

    assertEquals(expectedResponse, responseEntity);
  }

  @Test(expected = IntegrationUnavailableException.class)
  public void testAddCommentToAnIssueUnavailable() throws IOException {
    doReturn(null).when(jiraWebHookIntegration).getSettings();

    jiraApiResource.addCommentToAnIssue(COMMENT, ISSUE_KEY, AUTHORIZATION_HEADER,
        JIRA_INTEGRATION_URL);
  }

  @Test(expected = JiraAuthorizationException.class)
  public void testAddCommentToAnIssueNullAccessToken() throws IOException {
    doReturn(0L).when(jwtAuthentication)
        .getUserIdFromAuthorizationHeader(CONFIGURATION_ID, AUTHORIZATION_HEADER);
    jiraApiResource.addCommentToAnIssue(COMMENT, ISSUE_KEY, AUTHORIZATION_HEADER,
        JIRA_INTEGRATION_URL);
  }

  @Test(expected = JiraUnexpectedException.class)
  public void testAddCommentToAnIssueAccessTokenFailed()
      throws IOException, AuthorizationException {
    doThrow(AuthorizationException.class).when(jiraWebHookIntegration)
        .getAccessToken(JIRA_INTEGRATION_URL, USER_ID);
    jiraApiResource.addCommentToAnIssue(COMMENT, ISSUE_KEY, AUTHORIZATION_HEADER,
        JIRA_INTEGRATION_URL);
  }

  @Test(expected = JiraAuthorizationException.class)
  public void testAddCommentToAnIssueProviderFailed() throws IOException, AuthorizationException {
    doThrow(OAuth1Exception.class).when(jiraWebHookIntegration)
        .getOAuth1Provider(JIRA_INTEGRATION_URL);
    jiraApiResource.addCommentToAnIssue(COMMENT, ISSUE_KEY, AUTHORIZATION_HEADER,
        JIRA_INTEGRATION_URL);
  }

  @Test(expected = MissingRequiredPayloadException.class)
  public void addAnEmptyCommentToAnIssue() throws IOException {
    jiraApiResource.addCommentToAnIssue("", ISSUE_KEY, AUTHORIZATION_HEADER, JIRA_INTEGRATION_URL);
  }

  @Test
  public void addCommentToAnIssue() throws IOException {
    ResponseEntity expectedResponse = new ResponseEntity(HttpStatus.OK);

    ResponseEntity responseEntity =
        jiraApiResource.addCommentToAnIssue(COMMENT, ISSUE_KEY, AUTHORIZATION_HEADER,
            JIRA_INTEGRATION_URL);

    assertEquals(expectedResponse, responseEntity);
  }

  @Test(expected = IntegrationUnavailableException.class)
  public void testGetIssueInfoUnavailable() throws IOException {
    doReturn(null).when(jiraWebHookIntegration).getSettings();

    jiraApiResource.getIssueInfo(ISSUE_KEY, AUTHORIZATION_HEADER, JIRA_INTEGRATION_URL);
  }

  @Test(expected = JiraAuthorizationException.class)
  public void testGetIssueInfoProviderFailed() throws IOException, AuthorizationException {
    doThrow(OAuth1Exception.class).when(jiraWebHookIntegration)
        .getOAuth1Provider(JIRA_INTEGRATION_URL);
    jiraApiResource.getIssueInfo(ISSUE_KEY, AUTHORIZATION_HEADER, JIRA_INTEGRATION_URL);
  }

  @Test(expected = JiraAuthorizationException.class)
  public void testGetIssueInfoNullAccessToken() throws IOException {
    doReturn(0L).when(jwtAuthentication)
        .getUserIdFromAuthorizationHeader(CONFIGURATION_ID, AUTHORIZATION_HEADER);
    jiraApiResource.getIssueInfo(ISSUE_KEY, AUTHORIZATION_HEADER, JIRA_INTEGRATION_URL);
  }

  @Test
  public void testGetIssueInfo() {
    ResponseEntity issueInfo =
        jiraApiResource.getIssueInfo(ISSUE_KEY, AUTHORIZATION_HEADER, JIRA_INTEGRATION_URL);

    assertEquals(HttpStatus.OK, issueInfo.getStatusCode());
    assertEquals(SAMPLE_ISSUE, issueInfo.getBody());
  }
}
