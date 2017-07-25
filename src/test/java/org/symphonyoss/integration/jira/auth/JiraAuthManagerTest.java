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

package org.symphonyoss.integration.jira.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.symphonyoss.integration.jira.auth.JiraAuthManager.PUBLIC_KEY;
import static org.symphonyoss.integration.jira.auth.JiraAuthManager.PUBLIC_KEY_FILENAME;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.symphonyoss.integration.exception.bootstrap.CertificateNotFoundException;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.yaml.AppAuthenticationModel;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.utils.IntegrationUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * Unit tests for {@link JiraAuthManager}
 *
 * Created by rsanchez on 25/07/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
@ContextConfiguration(classes = {IntegrationProperties.class, JiraAuthManager.class})
public class JiraAuthManagerTest {

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

  private static final IntegrationSettings SETTINGS = new IntegrationSettings();

  @SpyBean
  private IntegrationProperties properties;

  @MockBean
  private IntegrationUtils utils;

  @Autowired
  private JiraAuthManager authManager;

  @BeforeClass
  public static void startup() {
    SETTINGS.setType(JIRA_APP_TYPE);
  }

  @Test
  public void testCertificateNotFound() {
    doThrow(CertificateNotFoundException.class).when(utils).getCertsDirectory();

    AppAuthenticationModel result = authManager.getAuthentcationModel(SETTINGS);
    validateAppAuthenticationModel(result, DEFAULT_IB_PORT);

    assertNull(result.getProperties().get(PUBLIC_KEY));
  }

  @Test
  public void testIOException() {
    doThrow(IOException.class).when(utils).getCertsDirectory();

    AppAuthenticationModel result = authManager.getAuthentcationModel(SETTINGS);
    validateAppAuthenticationModel(result, DEFAULT_IB_PORT);

    assertNull(result.getProperties().get(PUBLIC_KEY));
  }

  @Test
  public void testFileNotFound() {
    String tmpDir = System.getProperty("java.io.tmpdir") + File.separator;
    doReturn(tmpDir).when(utils).getCertsDirectory();

    Application application = properties.getApplication(SETTINGS.getType());
    application.getAuth().getProperties().put(PUBLIC_KEY_FILENAME, UUID.randomUUID().toString());

    AppAuthenticationModel result = authManager.getAuthentcationModel(SETTINGS);
    validateAppAuthenticationModel(result, DEFAULT_IB_PORT);

    assertNull(result.getProperties().get(PUBLIC_KEY));

    application.getAuth().getProperties().remove(PUBLIC_KEY_FILENAME);
  }

  @Test
  public void testInvalidFile() throws URISyntaxException {
    URL pkURL = getClass().getClassLoader().getResource(INVALID_PUBLIC_KEY);
    Path pkPath = Paths.get(pkURL.toURI());

    String certsDirectory = pkPath.getParent().toAbsolutePath() + File.separator;
    doReturn(certsDirectory).when(utils).getCertsDirectory();

    Application application = properties.getApplication(SETTINGS.getType());
    application.getAuth().getProperties().put(PUBLIC_KEY_FILENAME, INVALID_PUBLIC_KEY);

    AppAuthenticationModel result = authManager.getAuthentcationModel(SETTINGS);
    validateAppAuthenticationModel(result, DEFAULT_IB_PORT);

    assertNull(result.getProperties().get(PUBLIC_KEY));

    application.getAuth().getProperties().remove(PUBLIC_KEY_FILENAME);
  }

  @Test
  public void testAuthModel() throws URISyntaxException {
    URL pkURL = getClass().getClassLoader().getResource(MOCK_PUBLIC_KEY);
    Path pkPath = Paths.get(pkURL.toURI());

    String certsDirectory = pkPath.getParent().toAbsolutePath() + File.separator;
    doReturn(certsDirectory).when(utils).getCertsDirectory();

    AppAuthenticationModel result = authManager.getAuthentcationModel(SETTINGS);
    validateAppAuthenticationModel(result, DEFAULT_IB_PORT);

    assertEquals(EXPECTED_PUBLIC_KEY, result.getProperties().get(PUBLIC_KEY));
  }

  private void validateAppAuthenticationModel(AppAuthenticationModel model, int port) {
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
