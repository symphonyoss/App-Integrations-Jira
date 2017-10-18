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

package org.symphonyoss.integration.jira.webhook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.AuthorizationPayload;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1IntegrationNotFoundException;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1MissingParametersException;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.jira.authorization.JiraAuthorizationManager;
import org.symphonyoss.integration.jira.authorization.oauth.v1.JiraOAuth1Provider;
import org.symphonyoss.integration.jira.webhook.parser.JiraParserFactory;
import org.symphonyoss.integration.jira.webhook.parser.JiraParserResolver;
import org.symphonyoss.integration.jira.webhook.parser.JiraWebHookParserAdapter;
import org.symphonyoss.integration.jira.webhook.parser.NullJiraParser;
import org.symphonyoss.integration.jira.webhook.parser.v1.CommentJiraParser;
import org.symphonyoss.integration.jira.webhook.parser.v1.IssueCreatedJiraParser;
import org.symphonyoss.integration.jira.webhook.parser.v1.IssueUpdatedJiraParser;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.model.yaml.AppAuthorizationModel;
import org.symphonyoss.integration.service.UserService;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

/**
 * Unit tests for {@link JiraWebHookIntegration}.
 *
 * Created by mquilzini on 11/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class JiraWebHookIntegrationTest {

  private static final String ISSUE_CREATED_FILENAME =
      "parser/issueCreatedJiraParser/jiraCallbackSampleIssueCreated.json";

  private static final String ISSUE_UPDATED_FILENAME =
      "parser/issueUpdatedJiraParser/jiraCallbackSampleIssueUpdated.json";

  private static final String ISSUE_UPDATED_WITHOUT_HASHTAG =
      "parser/issueUpdatedJiraParser/jiraCallbackSampleIssueUpdatedWithoutHashTagLabel.json";

  private static final String COMMENT_ADDED_FILENAME = "parser/commentJiraParser/jiraCallbackSampleCommentAdded.json";

  private static final String COMMENT_ADDED_WITH_MENTION_FILENAME =
      "parser/commentJiraParser/jiraCallbackSampleCommentAddedWithMention.json";

  private static final String COMMENT_UPDATED_FILENAME =
      "parser/commentJiraParser/jiraCallbackSampleCommentUpdated.json";

  private static final String COMMENT_UPDATED_WITH_MENTION_FILENAME =
      "parser/commentJiraParser/jiraCallbackSampleCommentUpdatedWithMention.json";

  private static final String COMMENT_DELETED_FILENAME =
      "parser/commentJiraParser/jiraCallbackSampleCommentDeleted.json";
  public static final String MOCK_URL = "www.test.com";
  private static final String ACCESS_TOKEN = "ACCESS TOKEN";
  private static final String TEMPORARY_TOKEN = "oauth_token";
  private static final String VERIFICATION_CODE = "oauth_verifier";

  @Spy
  private List<JiraParserFactory> factories = new ArrayList<>();

  @Mock
  private JiraParserFactory factory;

  @Mock
  private JiraParserResolver parserResolver;

  @Mock
  private JiraAuthorizationManager authManager;

  @InjectMocks
  private JiraWebHookIntegration jiraWhi = new JiraWebHookIntegration();

  @Mock
  private NullJiraParser defaultJiraParser = new NullJiraParser();

  @Mock
  private UserService userService;

  @InjectMocks
  private IssueCreatedJiraParser issueCreatedJiraParser = new IssueCreatedJiraParser();

  @InjectMocks
  private CommentJiraParser commentJiraParser = new CommentJiraParser();

  @InjectMocks
  private IssueUpdatedJiraParser issueUpdatedJiraParser = new IssueUpdatedJiraParser();
  private String authorizationURL = "www.authorizantionURL.com";

  @Before
  public void setup() {
    mockUsers("test@symphony.com", "test2@symphony.com", "mquilzini@symphony.com",
        "ppires@symphony.com");

    factories.add(factory);

    doReturn(factory).when(parserResolver).getFactory();
  }

  @Test
  public void testOnConfigChange() {
    IntegrationSettings settings = new IntegrationSettings();

    jiraWhi.onConfigChange(settings);

    verify(factory, times(1)).onConfigChange(settings);
  }

  @Test
  public void testIssueCreated() throws IOException, WebHookParseException {
    String body = getBody(ISSUE_CREATED_FILENAME);

    WebHookPayload payload = new WebHookPayload(Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(), body);

    JiraWebHookParserAdapter parser = new JiraWebHookParserAdapter(issueCreatedJiraParser);
    doReturn(parser).when(factory).getParser(payload);

    Message result = jiraWhi.parse(payload);

    assertNotNull(result);

    String expected = readFile("parser/issueCreatedJiraParser/jiraMessageMLIssueCreated.xml");

    assertEquals(expected, result.getMessage());
  }

  @Test
  public void testIssueUpdated() throws IOException, WebHookParseException {
    testIssueUpdated(ISSUE_UPDATED_FILENAME);
  }

  @Test
  public void testIssueUpdatedWithoutHashTag() throws IOException, WebHookParseException {
    testIssueUpdated(ISSUE_UPDATED_WITHOUT_HASHTAG);
  }

  private void testIssueUpdated(String filename) throws IOException {
    String expected = readFile("parser/issueUpdatedJiraParser/jiraMessageMLIssueUpdated.xml");
    String body = getBody(filename);

    WebHookPayload payload = new WebHookPayload(Collections.<String, String>emptyMap(),
        Collections.<String, String>emptyMap(), body);

    JiraWebHookParserAdapter parser = new JiraWebHookParserAdapter(issueUpdatedJiraParser);
    doReturn(parser).when(factory).getParser(any(WebHookPayload.class));

    Message result = jiraWhi.parse(payload);

    assertNotNull(result);
    assertEquals(expected, result.getMessage());
  }

  @Test
  public void testCommentAdded() throws IOException, WebHookParseException {
    String body = getBody(COMMENT_ADDED_FILENAME);

    WebHookPayload payload = new WebHookPayload(Collections.<String, String>emptyMap(),
        Collections.<String, String>emptyMap(), body);

    JiraWebHookParserAdapter parser = new JiraWebHookParserAdapter(commentJiraParser);
    doReturn(parser).when(factory).getParser(payload);

    Message result = jiraWhi.parse(payload);

    assertNotNull(result);

    String expected = readFile("parser/commentJiraParser/jiraMessageMLIssueCommented.xml");

    assertEquals(expected, result.getMessage());
  }

  @Test
  public void testCommentAddedWithMention() throws IOException, WebHookParseException {
    String body = getBody(COMMENT_ADDED_WITH_MENTION_FILENAME);

    createNewUser();

    WebHookPayload payload = new WebHookPayload(Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(), body);

    JiraWebHookParserAdapter parser = new JiraWebHookParserAdapter(commentJiraParser);
    doReturn(parser).when(factory).getParser(payload);

    Message result = jiraWhi.parse(payload);

    assertNotNull(result);

    String expected = readFile("parser/commentJiraParser/jiraMessageMLIssueCommentedWithMention.xml");

    assertEquals(expected, result.getMessage());
  }

  @Test
  public void testCommentUpdated() throws IOException, WebHookParseException {
    String body = getBody(COMMENT_UPDATED_FILENAME);

    WebHookPayload payload = new WebHookPayload(Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(), body);

    JiraWebHookParserAdapter parser = new JiraWebHookParserAdapter(commentJiraParser);
    doReturn(parser).when(factory).getParser(payload);

    Message result = jiraWhi.parse(payload);

    assertNotNull(result);

    String expected = readFile("parser/commentJiraParser/jiraMessageMLIssueCommentUpdated.xml");

    assertEquals(expected, result.getMessage());
  }

  @Test
  public void testCommentUpdatedWithMention() throws IOException, WebHookParseException {
    String body = getBody(COMMENT_UPDATED_WITH_MENTION_FILENAME);

    User user1 = createNewUser();

    WebHookPayload payload = new WebHookPayload(Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(), body);

    JiraWebHookParserAdapter parser = new JiraWebHookParserAdapter(commentJiraParser);
    doReturn(parser).when(factory).getParser(payload);

    Message result = jiraWhi.parse(payload);

    assertNotNull(result);

    String expected = readFile("parser/commentJiraParser/jiraMessageMLIssueCommentUpdatedWithMention.xml");

    assertEquals(expected, result.getMessage());
  }

  public User createNewUser() {
    User user = new User();
    user.setEmailAddress("integrationuser@symphony.com");
    user.setId(123L);
    user.setUserName("integrationuser");
    user.setDisplayName("Integration User");
    when(userService.getUserByUserName(anyString(), eq("integrationuser"))).thenReturn(user);
    return user;
  }

  @Test
  public void testCommentDeleted() throws IOException, WebHookParseException {
    String body = getBody(COMMENT_DELETED_FILENAME);

    WebHookPayload payload = new WebHookPayload(Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(), body);

    JiraWebHookParserAdapter parser = new JiraWebHookParserAdapter(commentJiraParser);
    doReturn(parser).when(factory).getParser(payload);

    Message result = jiraWhi.parse(payload);

    assertNotNull(result);

    String expected = readFile("parser/commentJiraParser/jiraMessageMLIssueCommentDeleted.xml");

    assertEquals(expected, result.getMessage());
  }

  @Test
  public void testNoEventPayload() throws WebHookParseException {
    String body = "{ \"random\": \"json\" }";
    WebHookPayload payload = new WebHookPayload(Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(), body);

    JiraWebHookParserAdapter parser = new JiraWebHookParserAdapter(defaultJiraParser);
    doReturn(parser).when(factory).getParser(payload);

    assertNull(jiraWhi.parse(payload));
  }

  @Test
  public void testSupportedContentTypes() {
    List<MediaType> supportedContentTypes = jiraWhi.getSupportedContentTypes();

    assertNotNull(supportedContentTypes);
    assertEquals(1, supportedContentTypes.size());
    assertEquals(MediaType.WILDCARD_TYPE, supportedContentTypes.get(0));
  }

  @Test
  public void testAuthModelNullSettings() {
    AppAuthorizationModel authenticationModel = jiraWhi.getAuthorizationModel();
    assertNull(authenticationModel);
  }

  @Test
  public void testAuthModel() {
    IntegrationSettings settings = new IntegrationSettings();
    jiraWhi.onConfigChange(settings);

    AppAuthorizationModel authenticationModel = new AppAuthorizationModel();
    doReturn(authenticationModel).when(authManager).getAuthorizationModel(settings);

    AppAuthorizationModel result = jiraWhi.getAuthorizationModel();
    assertEquals(authenticationModel, result);
  }

  @Test
  public void testIsUserAuthorized() throws AuthorizationException {
    IntegrationSettings settings = new IntegrationSettings();
    jiraWhi.onConfigChange(settings);
    User user = createNewUser();
    doReturn(true).when(authManager).isUserAuthorized(settings, MOCK_URL, user.getId());
    boolean result = jiraWhi.isUserAuthorized(MOCK_URL, user.getId());
    assertTrue(result);
  }

  @Test
  public void testIsUserAuthorizedSettingsNull() throws AuthorizationException {
    User user = createNewUser();
    doReturn(true).when(authManager).isUserAuthorized(null, MOCK_URL, user.getId());
    boolean result = jiraWhi.isUserAuthorized(MOCK_URL, user.getId());
    assertFalse(result);
  }

  @Test
  public void testGetAuthorizationUrl() throws AuthorizationException {
    IntegrationSettings settings = new IntegrationSettings();
    jiraWhi.onConfigChange(settings);
    User user = createNewUser();
    doReturn(authorizationURL).when(authManager).getAuthorizationUrl(settings, MOCK_URL, user.getId());
    String result = jiraWhi.getAuthorizationUrl(MOCK_URL, user.getId());
    assertEquals(authorizationURL, result);
  }

  @Test
  public void testGetAuthorizationUrlSettingsNull() throws AuthorizationException {
    User user = createNewUser();
    doReturn(null).when(authManager).getAuthorizationUrl(null, MOCK_URL, user.getId());
    String result = jiraWhi.getAuthorizationUrl(MOCK_URL, user.getId());
    assertNull(result);
  }

  @Test
  public void testGetAccessToken() throws AuthorizationException {
    IntegrationSettings settings = new IntegrationSettings();
    jiraWhi.onConfigChange(settings);
    User user = createNewUser();
    doReturn(ACCESS_TOKEN).when(authManager).getAccessToken(settings, MOCK_URL, user.getId());
    String result = jiraWhi.getAccessToken(MOCK_URL, user.getId());
    assertEquals(result, ACCESS_TOKEN);
  }

  @Test
  public void testGetAccessTokenSettingsNull() throws AuthorizationException {
    User user = createNewUser();
    doReturn(null).when(authManager).getAccessToken(null, MOCK_URL, user.getId());
    String result = jiraWhi.getAccessToken(MOCK_URL, user.getId());
    assertNull(result);
  }

  @Test
  public void testGetOAuth1Provider() throws AuthorizationException {
    IntegrationSettings settings = new IntegrationSettings();
    jiraWhi.onConfigChange(settings);
    JiraOAuth1Provider provider = new JiraOAuth1Provider();
    doReturn(provider).when(authManager).getJiraOAuth1Provider(settings, MOCK_URL);
    OAuth1Provider result = jiraWhi.getOAuth1Provider(MOCK_URL);
    assertEquals(result, provider);
  }

  @Test
  public void testGetOAuth1ProviderSettingsNull() throws AuthorizationException {
    JiraOAuth1Provider provider = new JiraOAuth1Provider();
    doReturn(null).when(authManager).getJiraOAuth1Provider(null, MOCK_URL);
    OAuth1Provider result = jiraWhi.getOAuth1Provider(MOCK_URL);
    assertNull(result);
  }

  @Test(expected = OAuth1MissingParametersException.class)
  public void testAuthorizeTemporaryTokenBlank() throws AuthorizationException {
    AuthorizationPayload payload = new AuthorizationPayload(Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(), "body");
    jiraWhi.authorize(payload);
  }

  @Test(expected = OAuth1MissingParametersException.class)
  public void testAuthorizeVerificationCodeBlank() throws AuthorizationException {
    User user = createNewUser();
    Map<String, String> params = new HashMap<>();
    params.put(TEMPORARY_TOKEN, TEMPORARY_TOKEN);
    AuthorizationPayload payload = new AuthorizationPayload(params, Collections.<String, String>emptyMap(), "body");
    jiraWhi.authorize(payload);
  }

  @Test(expected = OAuth1IntegrationNotFoundException.class)
  public void testAuthorizeVerificationSettingsNull() throws AuthorizationException {
    Map<String, String> params = new HashMap<>();
    params.put(TEMPORARY_TOKEN, TEMPORARY_TOKEN);
    params.put(VERIFICATION_CODE, VERIFICATION_CODE);
    AuthorizationPayload payload = new AuthorizationPayload(params, Collections.<String, String>emptyMap(), "body");
    jiraWhi.authorize(payload);
  }

  @Test
  public void testAuthorize() throws AuthorizationException {
    IntegrationSettings settings = new IntegrationSettings();
    jiraWhi.onConfigChange(settings);
    Map<String, String> params = new HashMap<>();
    params.put(TEMPORARY_TOKEN, TEMPORARY_TOKEN);
    params.put(VERIFICATION_CODE, VERIFICATION_CODE);
    AuthorizationPayload payload = new AuthorizationPayload(params, Collections.<String, String>emptyMap(), "body");
    jiraWhi.authorize(payload);
    verify(authManager,times(1)).authorizeTemporaryToken(settings, payload.getParameters().get(TEMPORARY_TOKEN), payload.getParameters().get(VERIFICATION_CODE));

  }

  @Test
  public void testGetAuthorizationRedirectUrl() throws AuthorizationException {
    IntegrationSettings settings = new IntegrationSettings();
    jiraWhi.onConfigChange(settings);
    doReturn(MOCK_URL).when(authManager).getAuthorizationRedirectUrl(settings);
    String url = jiraWhi.getAuthorizationRedirectUrl();
    assertEquals(MOCK_URL, url);
  }

  @Test(expected = AuthorizationException.class)
  public void testGetAuthorizationRedirectUrlSettingsNull() throws AuthorizationException {
    jiraWhi.getAuthorizationRedirectUrl();
  }

  private String getBody(String filename) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(filename));
    return JsonUtils.writeValueAsString(node);
  }

  private String readFile(String fileName) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String expected =
        FileUtils.readFileToString(new File(classLoader.getResource(fileName).getPath()), Charset.defaultCharset());
    return expected = expected.replaceAll("\n", "");
  }

  private void mockUsers(String... emails) {
    for (String email : emails) {
      User user = new User();
      user.setEmailAddress(email);
      when(userService.getUserByEmail(anyString(), eq(email))).thenReturn(user);
    }
  }
}
