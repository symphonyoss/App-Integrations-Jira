package org.symphonyoss.integration.jira.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.symphonyoss.integration.authentication.api.jwt.JwtAuthentication;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;
import org.symphonyoss.integration.exception.IntegrationUnavailableException;
import org.symphonyoss.integration.jira.services.SearchAssignableUsersService;
import org.symphonyoss.integration.jira.services.UserAssignService;
import org.symphonyoss.integration.jira.webhook.JiraWebHookIntegration;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.logging.MessageUtils;
import org.symphonyoss.integration.service.IntegrationBridge;

import java.io.IOException;

/**
 * Created by hamitay on 8/16/17.
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore
public class JiraApiResourceTest {

  private static String ISSUE_KEY = "issueKey";

  private static String USERNAME = "username";

  private static String CONFIGURATION_ID = "configurationId";

  private static String AUTHORIZATION_HEADER = "authorizationHeader";

  private static String JIRA_INTEGRATION_URL = "https://jiraIntegrationUrl";

  private static String ACCESS_TOKEN = "accessToken";

  private static final Long USER_ID = 10L;

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
  public void prepareMockResource() throws AuthorizationException {
    doReturn(ACCESS_TOKEN).when(jiraWebHookIntegration).getAccessToken(JIRA_INTEGRATION_URL, USER_ID);
    doReturn(provider).when(jiraWebHookIntegration).getOAuth1Provider(JIRA_INTEGRATION_URL);

    jiraApiResource = new JiraApiResource(jiraWebHookIntegration, jwtAuthentication,
        userAssignService, searchAssignableUsersService);
  }

  public void testSearchAssignableUser() throws IOException {
    ResponseEntity expectedResponse = new ResponseEntity(HttpStatus.OK);

    ResponseEntity responseEntity =
        jiraApiResource.searchAssignableUsers(ISSUE_KEY, USERNAME, CONFIGURATION_ID,
            AUTHORIZATION_HEADER);

    assertEquals(expectedResponse, responseEntity);
  }

}
