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

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.AuthorizationRepositoryService;
import org.symphonyoss.integration.authorization.UserAuthorizationData;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.UserKeyManagerData;
import org.symphonyoss.integration.service.CryptoService;
import org.symphonyoss.integration.service.KeyManagerService;
import org.symphonyoss.integration.utils.RsaKeyUtils;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Exception;
import org.symphonyoss.integration.exception.bootstrap.CertificateNotFoundException;
import org.symphonyoss.integration.jira.authorization.oauth.v1.JiraOAuth1Data;
import org.symphonyoss.integration.jira.authorization.oauth.v1.JiraOAuth1Exception;
import org.symphonyoss.integration.jira.authorization.oauth.v1.JiraOAuth1Provider;
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
@ContextConfiguration(classes = {IntegrationProperties.class, JiraAuthorizationManager.class })
@ActiveProfiles("jira")
public class JiraAuthorizationManagerTest {

  private static final String JIRA_APP_TYPE = "jiraWebHookIntegration";

  private static final String JIRA_APP_ID = "5810d1cee4b0f884b709cc9b";

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

  private static final String EXPECTED_PRIVATE_KEY =
      "MIICeQIBADANBgkqhkiG9w0BAQEFAASCAmMwggJfAgEAAoGBAN8wcSF5AE7sL30p\n"
      + "2mnM0X3T1OZy4BDfxucZTYdYmg99vqv6uVQyjc4zKOHRiwnCh2GwatT4jBfoQfWx\n"
      + "6VUmvcxKHuZwcVCHF/u/Vw85wsMDpD4pBglpX1GsFlfSQe1E115X7mHD7tHlkQHv\n"
      + "tVplf5BmYxM6G2EljBmiRRQq4OLbAgMBAAECgYEAxu54h6tAWRgvo9IgOVk0CIE9\n"
      + "LEKL8L5knStybQbOGqyrvMJ3WdLNjlMPR2fsE8DtxmbmcfkvdUexMvtmzF0BoWDv\n"
      + "JgqnGaUr9l0gZfGCR0ir2PBJ7V9OOJz5ug4ExLz6S9WNV6RdtXOSXSbNG3/L+56t\n"
      + "ocA05JpZrZaUfK43V0ECQQDyjkokOrk54DwdnSH86V2bXn+RlzAyumhfGKJpC7pb\n"
      + "eZgcSJtkbV9RslEr+TcVuuJyHZGeWtPEStl1BaKnvRLxAkEA649aVUD1b9Cly+Q2\n"
      + "l7KbgDjny5k/Ezw7JK3hjYEKQrHjgkMejOuKSkeRz2imWD8PLoJ01GgMXLIiu+F1\n"
      + "lb06iwJBAI7NJuldiV+BnOLyd+gmnG20nPZiRIYZKQmTv0qJFRZ16A/+zz25Br1a\n"
      + "dl+lQcERXfBBaFIKt1KBnrU+tBx9PIECQQCLquG6rttXwvSrIdMkuufsbNEzLNfz\n"
      + "RcEjjF2yExLMXMEymS1iDL5gMHNJ8RjANhOAViWDU3YQ+CYUFCgt8pblAkEAhM5k\n"
      + "y54f3UViEO29UyWv2ZNaZPd17bSr8HAo/lxXyju4TRNRB3vIq79lMNalX5HKHlI9\n"
      + "EST7xXLh110xXRH9/Q==\n";

  private static final String INVALID_PUBLIC_KEY = "invalid_app_pub.pem";

  private static final String MOCK_PUBLIC_KEY = "jira_app_pub.pem";

  private static final String INVALID_PRIVATE_KEY = "invalid_app_prv.pcks8";

  private static final String MOCK_PRIVATE_KEY = "jira_app_prv.pcks8";

  private static final String MOCK_TOKEN = "mockToken";

  private static final String MOCK_URL = "https://test.symphony.com";

  private static final Long MOCK_USER = 0L;

  private static final String MOCK_ACCESS_TOKEN = "accessToken";

  private static final IntegrationSettings SETTINGS = new IntegrationSettings();

  @Autowired
  private IntegrationProperties properties;

  @MockBean
  private JiraOAuth1Provider jiraOAuth1Provider;

  @MockBean
  private RsaKeyUtils rsaKeyUtils;

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

  @MockBean
  private KeyManagerService kmService;

  @MockBean
  private CryptoService cryptoService;

  @BeforeClass
  public static void startup() {
    SETTINGS.setType(JIRA_APP_TYPE);
    SETTINGS.setConfigurationId(JIRA_APP_ID);
  }

  @Before
  public void init() {
    ReflectionTestUtils.setField(authManager, "publicKey", null);
    ReflectionTestUtils.setField(authManager, "privateKey", null);
    UserKeyManagerData userKeyManagerData = new UserKeyManagerData();
    userKeyManagerData.setPrivateKey(MOCK_PRIVATE_KEY);
    doReturn(userKeyManagerData).when(kmService).getBotUserAccountKeyByConfiguration(anyString());
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

  public void testIsUserUnauthorized() throws AuthorizationException, URISyntaxException {
    JiraOAuth1Data jiraAuthData = new JiraOAuth1Data(MOCK_TOKEN, MOCK_TOKEN);
    UserAuthorizationData userData = new UserAuthorizationData(MOCK_URL, MOCK_USER, jiraAuthData);

    Application application = properties.getApplication(SETTINGS.getType());
    application.getAuthorization().getProperties().put(PRIVATE_KEY_FILENAME, MOCK_PRIVATE_KEY);

    URL pkURL = getClass().getClassLoader().getResource(MOCK_PRIVATE_KEY);
    Path pkPath = Paths.get(pkURL.toURI());

    String certsDirectory = pkPath.getParent().toAbsolutePath() + File.separator;
    doReturn(certsDirectory).when(utils).getCertsDirectory();

    doReturn(privateKey).when(rsaKeyUtils).getPrivateKey(anyString());

    doReturn(userData).when(authRepoService).find(anyString(),
        eq(SETTINGS.getConfigurationId()), eq(MOCK_URL), eq(MOCK_USER));

    assertFalse(authManager.isUserAuthorized(SETTINGS, MOCK_URL, MOCK_USER));
  }

  @Test(expected = JiraOAuth1Exception.class)
  public void testIsUserAuthorizedInvalidURL() throws AuthorizationException {
    JiraOAuth1Data jiraAuthData = new JiraOAuth1Data(MOCK_TOKEN, MOCK_TOKEN);
    UserAuthorizationData userData = new UserAuthorizationData(MOCK_URL, MOCK_USER, jiraAuthData);

    doReturn(MOCK_ACCESS_TOKEN).when(cryptoService).decrypt(anyString(), eq(MOCK_PRIVATE_KEY));

    doReturn(userData).when(authRepoService).find(anyString(),
        eq(SETTINGS.getConfigurationId()), eq(StringUtils.EMPTY), eq(MOCK_USER));

    authManager.isUserAuthorized(SETTINGS, StringUtils.EMPTY, MOCK_USER);
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

    doReturn(EXPECTED_PRIVATE_KEY).when(rsaKeyUtils).trimPrivateKey(anyString());
    doReturn(privateKey).when(rsaKeyUtils).getPrivateKey(anyString());

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

    doReturn(privateKey).when(rsaKeyUtils).getPrivateKey(anyString());

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

    doReturn(EXPECTED_PUBLIC_KEY).when(rsaKeyUtils).trimPublicKey(anyString());
    doReturn(publicKey).when(rsaKeyUtils).getPublicKey(anyString());

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

  @Test
  public void testGetAccessTokenNullValue() throws AuthorizationException {
    String accessToken = authManager.getAccessToken(new IntegrationSettings(), MOCK_URL, MOCK_USER);
    assertNull(accessToken);
  }

  @Test
  public void testGetAccessTokenEmptyValue() throws AuthorizationException {
    doReturn(new UserAuthorizationData()).when(authRepoService).find(JIRA_APP_TYPE, JIRA_APP_ID,
        MOCK_URL, MOCK_USER);

    String accessToken = authManager.getAccessToken(SETTINGS, MOCK_URL, MOCK_USER);
    assertNull(accessToken);
  }

  @Test
  public void testGetAccessTokenNotFound() throws AuthorizationException {
    JiraOAuth1Data jiraOAuth1Data = new JiraOAuth1Data();

    UserAuthorizationData authorizationData = new UserAuthorizationData();
    authorizationData.setData(jiraOAuth1Data);

    doReturn(authorizationData).when(authRepoService).find(JIRA_APP_TYPE, JIRA_APP_ID,
        MOCK_URL, MOCK_USER);

    String accessToken = authManager.getAccessToken(SETTINGS, MOCK_URL, MOCK_USER);
    assertNull(accessToken);
  }

  @Test
  public void testGetAccessToken() throws AuthorizationException {
    JiraOAuth1Data jiraOAuth1Data = new JiraOAuth1Data();
    jiraOAuth1Data.setAccessToken(MOCK_ACCESS_TOKEN);

    UserAuthorizationData authorizationData = new UserAuthorizationData();
    authorizationData.setData(jiraOAuth1Data);

    doReturn(authorizationData).when(authRepoService).find(JIRA_APP_TYPE, JIRA_APP_ID,
        MOCK_URL, MOCK_USER);

    doReturn(MOCK_ACCESS_TOKEN).when(cryptoService).decrypt(anyString(), eq(MOCK_PRIVATE_KEY));

    String accessToken = authManager.getAccessToken(SETTINGS, MOCK_URL, MOCK_USER);
    assertEquals(MOCK_ACCESS_TOKEN, accessToken);
  }

  @Test
  public void testKeysCache() throws URISyntaxException, AuthorizationException {
    testAuthorizationModel();
    testGetAuthorizationUrl();

    Object publicKey = ReflectionTestUtils.getField(authManager, JiraAuthorizationManager.class, "publicKey");
    Object privateKey = ReflectionTestUtils.getField(authManager, JiraAuthorizationManager.class, "privateKey");

    assertNotNull(publicKey);
    assertNotNull(privateKey);

    assertEquals(EXPECTED_PUBLIC_KEY, publicKey.toString());
    assertEquals(EXPECTED_PRIVATE_KEY, privateKey.toString());
  }

  @Test
  public void testGetAuthorizationRedirectUrl() {
    String expected = MOCK_URL + "/apps/jira/closePopUp.html";
    String url = authManager.getAuthorizationRedirectUrl(SETTINGS);
    assertEquals(expected, url);
  }
}
