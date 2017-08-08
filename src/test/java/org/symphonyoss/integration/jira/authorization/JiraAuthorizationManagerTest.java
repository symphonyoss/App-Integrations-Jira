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

package org.symphonyoss.integration.jira.authorization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.symphonyoss.integration.jira.authorization.JiraAuthorizationManager
    .PRIVATE_KEY_FILENAME;
import static org.symphonyoss.integration.jira.authorization.JiraAuthorizationManager.PUBLIC_KEY;
import static org.symphonyoss.integration.jira.authorization.JiraAuthorizationManager
    .PUBLIC_KEY_FILENAME;

import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.AuthorizationRepositoryService;
import org.symphonyoss.integration.authorization.UserAuthorizationData;
import org.symphonyoss.integration.authorization.oauth.OAuthRsaSignerFactory;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Exception;
import org.symphonyoss.integration.exception.bootstrap.CertificateNotFoundException;
import org.symphonyoss.integration.jira.authorization.oauth.v1.JiraOAuth1Data;
import org.symphonyoss.integration.jira.authorization.oauth.v1.JiraOAuth1Provider;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.yaml.AppAuthorizationModel;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.utils.IntegrationUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Unit tests for {@link JiraAuthorizationManager}
 *
 * Created by rsanchez on 25/07/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
@ContextConfiguration(classes = {IntegrationProperties.class, JiraAuthorizationManager.class})
@ActiveProfiles("jira")
public class JiraAuthorizationManagerTest {

  private static final String JIRA_APP_TYPE = "jiraWebHookIntegration";

  private static final String CONSUMER_KEY = "consumerKey";

  private static final String CONSUMER_NAME = "consumerName";

  private static final String DEFAULT_APP_NAME = "Symphony Integration for JIRA";

  private static final Integer DEFAULT_IB_PORT = 8443;

  private static final String DEFAULT_CONSUMER_KEY = "symphony_consumer";

  private static final String DEFAULT_CONSUMER_NAME = "Symphony Consumer";

  private static final String EXPECTED_PUBLIC_KEY =
      "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC6yaUNjzhKUK7/8X8vCAXkSDp+\n"
      + "zy1UGhAXMthChPPtDb5kixc6WMLDdGjRRPvzZBROo1vk7K7fum4YXCNHwcSS26Nf\n"
      + "GJCLCFoW64tKyFhxcVZ7lU43Unhji50S0Bb3reniB0ophJ8UEOjH2qiy3hOTtUNF\n"
      + "JynqT6wBqrpTGodBmwIDAQAB\n";

  private static final String INVALID_PUBLIC_KEY = "invalid_app_pub.pem";

  private static final String MOCK_PUBLIC_KEY = "jira_app_pub.pem";

  private static final String INVALID_PRIVATE_KEY = "invalid_app_prv.pcks8";

  private static final String MOCK_PRIVATE_KEY = "jira_app_prv.pcks8";

  private static final String MOCK_TOKEN = "mockToken";

  private static final String MOCK_URL = "http://mockurl.com";

  private static final Long MOCK_USER = 0L;

  private static final IntegrationSettings SETTINGS = new IntegrationSettings();

  @Autowired
  private IntegrationProperties properties;

  @MockBean
  private LogMessageSource logMessage;

  @MockBean
  private JiraOAuth1Provider jiraOAuth1Provider;

  @MockBean
  private OAuthRsaSignerFactory oAuthRsaSignerFactory;

  @MockBean
  private AuthorizationRepositoryService authRepoService;

  @MockBean
  private IntegrationUtils utils;

  @Mock
  private PublicKey publicKey;

  @Mock
  private PrivateKey privateKey;

  @Autowired
  private JiraAuthorizationManager authManager;

  @BeforeClass
  public static void startup() {
    SETTINGS.setType(JIRA_APP_TYPE);
  }

  @Test
  public void testCertificateNotFound() {
    doThrow(CertificateNotFoundException.class).when(utils).getCertsDirectory();

    AppAuthorizationModel result = authManager.getAuthorizationModel(SETTINGS);
    validateAppAuthorizationModel(result, DEFAULT_IB_PORT);

    assertNull(result.getProperties().get(PUBLIC_KEY));
  }

  @Test
  public void testIOException() {
    doThrow(IOException.class).when(utils).getCertsDirectory();

    AppAuthorizationModel result = authManager.getAuthorizationModel(SETTINGS);
    validateAppAuthorizationModel(result, DEFAULT_IB_PORT);

    assertNull(result.getProperties().get(PUBLIC_KEY));
  }

  @Test
  public void testFileNotFound() {
    String tmpDir = System.getProperty("java.io.tmpdir") + File.separator;
    doReturn(tmpDir).when(utils).getCertsDirectory();

    Application application = properties.getApplication(SETTINGS.getType());
    application.getAuthorization().getProperties().put(PUBLIC_KEY_FILENAME, UUID.randomUUID().toString());

    AppAuthorizationModel result = authManager.getAuthorizationModel(SETTINGS);
    validateAppAuthorizationModel(result, DEFAULT_IB_PORT);

    assertNull(result.getProperties().get(PUBLIC_KEY));

    application.getAuthorization().getProperties().remove(PUBLIC_KEY_FILENAME);
  }

  @Test
  public void testInvalidPublicKeyFile() throws URISyntaxException {
    URL pkURL = getClass().getClassLoader().getResource(INVALID_PUBLIC_KEY);
    Path pkPath = Paths.get(pkURL.toURI());

    String certsDirectory = pkPath.getParent().toAbsolutePath() + File.separator;
    doReturn(certsDirectory).when(utils).getCertsDirectory();

    Application application = properties.getApplication(SETTINGS.getType());
    application.getAuthorization().getProperties().put(PUBLIC_KEY_FILENAME, INVALID_PUBLIC_KEY);

    AppAuthorizationModel result = authManager.getAuthorizationModel(SETTINGS);
    validateAppAuthorizationModel(result, DEFAULT_IB_PORT);

    assertNull(result.getProperties().get(PUBLIC_KEY));

    application.getAuthorization().getProperties().remove(PUBLIC_KEY_FILENAME);
  }

  @Test(expected = NullPointerException.class)
  public void testIsUserAuthorizedException() throws AuthorizationException, URISyntaxException {
    JiraOAuth1Data jiraAuthData = new JiraOAuth1Data(MOCK_TOKEN, MOCK_TOKEN);
    UserAuthorizationData userData = new UserAuthorizationData(MOCK_URL, MOCK_USER, jiraAuthData);

    Application application = properties.getApplication(SETTINGS.getType());
    application.getAuthorization().getProperties().put(PRIVATE_KEY_FILENAME, MOCK_PRIVATE_KEY);

    URL pkURL = getClass().getClassLoader().getResource(MOCK_PRIVATE_KEY);
    Path pkPath = Paths.get(pkURL.toURI());

    String certsDirectory = pkPath.getParent().toAbsolutePath() + File.separator;
    doReturn(certsDirectory).when(utils).getCertsDirectory();

    doReturn(privateKey).when(oAuthRsaSignerFactory).getPrivateKey(anyString());

    doReturn(userData).when(authRepoService).find(anyString(),
        eq(SETTINGS.getConfigurationId()), eq(MOCK_URL), eq(MOCK_USER));

    authManager.isUserAuthorized(SETTINGS, MOCK_URL, MOCK_USER);
  }

  @Test
  public void testGetAuthorizationUrl() throws AuthorizationException, URISyntaxException {
    JiraOAuth1Data jiraAuthData = new JiraOAuth1Data(MOCK_TOKEN);
    UserAuthorizationData userData = new UserAuthorizationData(MOCK_URL, MOCK_USER, jiraAuthData);

    Application application = properties.getApplication(SETTINGS.getType());
    application.getAuthorization().getProperties().put(PRIVATE_KEY_FILENAME, MOCK_PRIVATE_KEY);

    URL pkURL = getClass().getClassLoader().getResource(MOCK_PRIVATE_KEY);
    Path pkPath = Paths.get(pkURL.toURI());

    String certsDirectory = pkPath.getParent().toAbsolutePath() + File.separator;
    doReturn(certsDirectory).when(utils).getCertsDirectory();

    doReturn(privateKey).when(oAuthRsaSignerFactory).getPrivateKey(anyString());

    doReturn(MOCK_URL).when(jiraOAuth1Provider).requestAuthorizationUrl(anyString());

    String url = authManager.getAuthorizationUrl(SETTINGS, MOCK_URL, MOCK_USER);

    assertEquals(MOCK_URL, url);
  }

  @Test
  public void testAuthorizeTemporaryToken() throws AuthorizationException, URISyntaxException {
    JiraOAuth1Data jiraAuthData = new JiraOAuth1Data(MOCK_TOKEN);
    UserAuthorizationData userData = new UserAuthorizationData(MOCK_URL, MOCK_USER, jiraAuthData);

    Application application = properties.getApplication(SETTINGS.getType());
    application.getAuthorization().getProperties().put(PRIVATE_KEY_FILENAME, MOCK_PRIVATE_KEY);

    URL pkURL = getClass().getClassLoader().getResource(MOCK_PRIVATE_KEY);
    Path pkPath = Paths.get(pkURL.toURI());

    String certsDirectory = pkPath.getParent().toAbsolutePath() + File.separator;
    doReturn(certsDirectory).when(utils).getCertsDirectory();

    doReturn(privateKey).when(oAuthRsaSignerFactory).getPrivateKey(anyString());

    List<UserAuthorizationData> listUserData = new ArrayList<>();
    JiraOAuth1Data jiraData = new JiraOAuth1Data(null);
    jiraData.setAccessToken(MOCK_TOKEN);
    jiraData.setTemporaryToken(MOCK_TOKEN);
    listUserData.add(new UserAuthorizationData(MOCK_URL, MOCK_USER, jiraData));
    doReturn(listUserData).when(authRepoService).search(anyString(), anyString(), anyMap());

    authManager.authorizeTemporaryToken(SETTINGS, MOCK_TOKEN, null);
  }

  @Test
  public void testAuthorizationModel() throws URISyntaxException, OAuth1Exception {
    URL pkURL = getClass().getClassLoader().getResource(MOCK_PUBLIC_KEY);
    Path pkPath = Paths.get(pkURL.toURI());

    String certsDirectory = pkPath.getParent().toAbsolutePath() + File.separator;
    doReturn(certsDirectory).when(utils).getCertsDirectory();

    doReturn(publicKey).when(oAuthRsaSignerFactory).getPublicKey(anyString());

    AppAuthorizationModel result = authManager.getAuthorizationModel(SETTINGS);
    validateAppAuthorizationModel(result, DEFAULT_IB_PORT);

    assertEquals(EXPECTED_PUBLIC_KEY, result.getProperties().get(PUBLIC_KEY));
  }

  private void validateAppAuthorizationModel(AppAuthorizationModel model, int port) {
    assertEquals(DEFAULT_APP_NAME, model.getApplicationName());

    String applicationURL = String.format("https://test.symphony.com:%d/integration", port);
    assertEquals(applicationURL, model.getApplicationURL());

    Map<String, Object> properties = model.getProperties();

    String consumerKey = (String) properties.get(CONSUMER_KEY);
    String consumerName = (String) properties.get(CONSUMER_NAME);

    assertEquals(DEFAULT_CONSUMER_KEY, consumerKey);
    assertEquals(DEFAULT_CONSUMER_NAME, consumerName);
  }
}
