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

import static org.symphonyoss.integration.jira.properties.JiraErrorMessageKeys.BUNDLE_FILENAME;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.AuthorizationRepositoryService;
import org.symphonyoss.integration.authorization.UserAuthorizationData;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1HttpRequestException;
import org.symphonyoss.integration.exception.CryptoException;
import org.symphonyoss.integration.exception.IntegrationRuntimeException;
import org.symphonyoss.integration.exception.bootstrap.CertificateNotFoundException;
import org.symphonyoss.integration.jira.authorization.oauth.v1.JiraOAuth1Data;
import org.symphonyoss.integration.jira.authorization.oauth.v1.JiraOAuth1Exception;
import org.symphonyoss.integration.jira.authorization.oauth.v1.JiraOAuth1Provider;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.logging.MessageUtils;
import org.symphonyoss.integration.model.UserKeyManagerData;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.yaml.AppAuthorizationModel;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.service.CryptoService;
import org.symphonyoss.integration.service.UserService;
import org.symphonyoss.integration.utils.IntegrationUtils;
import org.symphonyoss.integration.utils.RsaKeyUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service component responsible to provide the authentication properties from JIRA application.
 *
 * This component reads the YAML configuration file to retrieve application name and application
 * URL. It should also read the application public key configured on the filesystem and validate it.
 *
 * Created by rsanchez on 24/07/17.
 */
@Component
public class JiraAuthorizationManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(JiraAuthorizationManager.class);

  private static final String COMPONENT = "JIRA Authorization Manager";

  public static final String PRIVATE_KEY_FILENAME = "privateKeyFilename";

  public static final String PRIVATE_KEY_FILENAME_TEMPLATE = "%s_app.pkcs8";

  public static final String PUBLIC_KEY_FILENAME = "publicKeyFilename";

  public static final String PUBLIC_KEY_FILENAME_TEMPLATE = "%s_app_pub.pem";

  public static final String PUBLIC_KEY = "publicKey";

  public static final String CONSUMER_KEY = "consumerKey";

  private static final String AUTH_CALLBACK_PATH = "/v1/application/%s/authorization/authorize";

  private static final String PATH_JIRA_API_MY_SELF = "/rest/api/2/myself";

  private static final String CLOSE_POP_UP_HTML = "/closePopUp.html";

  private static final MessageUtils MSG = new MessageUtils(BUNDLE_FILENAME);

  @Autowired
  private IntegrationProperties properties;

  @Autowired
  private IntegrationUtils utils;

  @Autowired
  private RsaKeyUtils rsaKeyUtils;

  @Autowired
  private AuthorizationRepositoryService authRepoService;

  @Autowired
  private ApplicationContext context;

  @Autowired
  private UserService userService;

  @Autowired
  private CryptoService cryptoService;

  /**
   * Application public key cache
   */
  private String publicKey;

  /**
   * Application private key cache
   */
  private String privateKey;

  /**
   * Provide the authorization properties for JIRA application.
   * @param settings Integration settings
   * @return authorization properties
   */
  public AppAuthorizationModel getAuthorizationModel(IntegrationSettings settings) {
    String appType = settings.getType();
    Application application = properties.getApplication(appType);

    AppAuthorizationModel auth = application.getAuthorization();

    String publicKey = getPublicKey(auth, application);
    auth.getProperties().put(PUBLIC_KEY, publicKey);

    return auth;
  }

  /**
   * Read the application public key configured on the filesystem and validate it.
   * @param authModel authorization properties
   * @param application Application settings
   * @return Application public key
   */
  private String getPublicKey(AppAuthorizationModel authModel, Application application) {
    if (StringUtils.isNotEmpty(publicKey)) {
      return publicKey;
    }

    String filename = getPublicKeyFilename(authModel, application);
    String publicKey = readKey(filename);

    if (StringUtils.isEmpty(publicKey)) {
      return null;
    }

    String pkAsString = rsaKeyUtils.trimPublicKey(publicKey);

    try {
      PublicKey pk = rsaKeyUtils.getPublicKey(pkAsString);
      if (pk != null) {
        this.publicKey = pkAsString;

        return pkAsString;
      }
    } catch (Exception e) {
      throw new IntegrationRuntimeException(COMPONENT,
          MSG.getMessage("integration.jira.public.key.validation"), e);
    }

    LOGGER.warn("Application public key is invalid, please check the file {}", filename);
    return null;
  }

  /**
   * Retrieve the application public key filename.
   * @param authModel authorization properties
   * @param application Application settings
   * @return Application public key filename
   */
  private String getPublicKeyFilename(AppAuthorizationModel authModel, Application application) {
    String fileName = (String) authModel.getProperties().get(PUBLIC_KEY_FILENAME);

    if (StringUtils.isEmpty(fileName)) {
      return String.format(PUBLIC_KEY_FILENAME_TEMPLATE, application.getId());
    }
    return fileName;
  }

  /**
   * Read the application private key configured on the filesystem and validate it.
   * @param settings This integration settings.
   * @return Application private key
   */
  private String getPrivateKey(IntegrationSettings settings) {
    if (StringUtils.isNotEmpty(privateKey)) {
      return privateKey;
    }

    String appType = settings.getType();
    Application application = properties.getApplication(appType);
    AppAuthorizationModel authModel = application.getAuthorization();

    String filename = getPrivateKeyFilename(authModel, application);
    String privateKey = readKey(filename);

    if (StringUtils.isEmpty(privateKey)) {
      return null;
    }

    String pkAsString = rsaKeyUtils.trimPrivateKey(privateKey);

    try {
      PrivateKey pk = rsaKeyUtils.getPrivateKey(pkAsString);
      if (pk != null) {
        this.privateKey = pkAsString;

        return pkAsString;
      }
    } catch (Exception e) {
      throw new IntegrationRuntimeException(COMPONENT,
          MSG.getMessage("integration.jira.private.key.validation"), e);
    }

    LOGGER.warn("Application private key is invalid, please check the file {}", filename);
    return null;
  }

  /**
   * Retrieve the application private key filename.
   * @param authModel authorization properties
   * @param application Application settings
   * @return Application private key filename
   */
  private String getPrivateKeyFilename(AppAuthorizationModel authModel, Application application) {
    String fileName = (String) authModel.getProperties().get(PRIVATE_KEY_FILENAME);
    if (StringUtils.isEmpty(fileName)) {
      return String.format(PRIVATE_KEY_FILENAME_TEMPLATE, application.getId());
    }
    return fileName;
  }

  /**
   * Read public or private key configured on the filesystem.
   * @param fileName Public/private key filename
   * @return Application public/private key or null if the file not found
   */
  private String readKey(String fileName) {
    try {
      String certsDir = utils.getCertsDirectory();
      Path keyPath = Paths.get(certsDir + fileName);

      if (Files.exists(keyPath, LinkOption.NOFOLLOW_LINKS)) {
        byte[] pubKeyBytes = Files.readAllBytes(keyPath);
        return new String(pubKeyBytes);
      }

      LOGGER.error("Cannot read the key. Make sure the file {} already exists",
          keyPath.toAbsolutePath());
    } catch (IOException e) {
      LOGGER.error("Cannot read the file " + fileName + ". Please check the file permissions", e);
    } catch (CertificateNotFoundException e) {
      LOGGER.error(
          "Cannot find the certificate directory. Please make sure this directory was already "
              + "created properly");
    }

    return null;
  }

  /**
   * Build and return a callback URL to be used when constructing the JiraOauth1Provider.
   * @param settings Jira integration settings.
   * @return Built URL.
   */
  private String getCallbackUrl(IntegrationSettings settings) {
    String callbackUrl = String.format(AUTH_CALLBACK_PATH, settings.getConfigurationId());
    return properties.getIntegrationBridgeUrl() + callbackUrl;
  }

  /**
   * Verify if the passed user has authorized us to perform Jira API calls on behalf of him/her.
   * @param settings Jira integration settings.
   * @param url Jira base URL.
   * @param userId Symphony user ID.
   * @return <code>true</code> If the passed user has authorized the access.
   * @throws AuthorizationException Thrown in case of error.
   */
  public boolean isUserAuthorized(IntegrationSettings settings, String url, Long userId)
      throws AuthorizationException {
    UserAuthorizationData userAuthorizationData =
        getUserAuthorizationData(settings, url, userId);

    if ((userAuthorizationData == null) || (userAuthorizationData.getData() == null)) {
      return false;
    }

    JiraOAuth1Data jiraOAuth1Data = null;
    try {
      jiraOAuth1Data = getJiraOAuth1Data(settings, userAuthorizationData);
    } catch (CryptoException e) {
      LOGGER.warn("There is a cryptography problem, the OAuth process must be restarted.", e);
      return false;
    }

    if (StringUtils.isEmpty(jiraOAuth1Data.getAccessToken())) {
      return false;
    }

    JiraOAuth1Provider provider = getJiraOAuth1Provider(settings, url);

    try {
      URL myselfUrl = new URL(url);
      myselfUrl = new URL(myselfUrl, PATH_JIRA_API_MY_SELF);
      HttpResponse response =
          provider.makeAuthorizedRequest(jiraOAuth1Data.getAccessToken(), myselfUrl,
              HttpMethods.GET, null);

      return response.getStatusCode() != HttpStatusCodes.STATUS_CODE_UNAUTHORIZED;
    } catch (MalformedURLException e) {
      throw new JiraOAuth1Exception(MSG.getMessage("integration.jira.url.api.invalid", url),
          e, MSG.getMessage("integration.jira.url.api.invalid.solution"));
    } catch (OAuth1HttpRequestException e) {
      throw new JiraOAuth1Exception(MSG.getMessage("integration.jira.url.api.invalid", url),
          e, MSG.getMessage("integration.jira.url.api.invalid.solution"));
    }
  }

  /**
   * Return an URL to allow the user to authorize us to perform Jira API calls on behalf of him/her.
   * @param settings Jira integration settings.
   * @param url Jira base URL.
   * @param userId Symphony user ID.
   * @return Authorization URL.
   * @throws AuthorizationException Thrown in case of error.
   */
  public String getAuthorizationUrl(IntegrationSettings settings, String url, Long userId)
      throws AuthorizationException {

    JiraOAuth1Provider provider = getJiraOAuth1Provider(settings, url);

    String temporaryToken = provider.requestTemporaryToken();
    String authorizationUrl = provider.requestAuthorizationUrl(temporaryToken);

    JiraOAuth1Data jiraOAuth1Data = new JiraOAuth1Data(temporaryToken);
    UserAuthorizationData userAuthData = new UserAuthorizationData(url, userId, jiraOAuth1Data);

    authRepoService.save(settings.getType(), settings.getConfigurationId(), userAuthData);

    return authorizationUrl;
  }

  /**
   * Authorize a temporary token by getting a permanent access token and saving it.
   * @param settings JIRA integration settings.
   * @param temporaryToken The original temporary token used to get the authorization from a user.
   * @param verifierCode The granted access code created when a user allow our application.
   * @throws AuthorizationException Thrown when there is a problem in this operation.
   */
  public void authorizeTemporaryToken(IntegrationSettings settings, String temporaryToken,
      String verifierCode) throws AuthorizationException {

    Map<String, String> filter = new HashMap<>();
    filter.put("temporaryToken", temporaryToken);
    List<UserAuthorizationData> result =
        authRepoService.search(settings.getType(), settings.getConfigurationId(), filter);

    if (result.isEmpty()) {
      throw new JiraOAuth1Exception(
          MSG.getMessage("integration.jira.auth.user.data.not.found", temporaryToken),
          MSG.getMessage("integration.jira.auth.user.data.not.found.solution"));
    }
    UserAuthorizationData userAuthData = result.get(0);
    String url = userAuthData.getUrl();

    JiraOAuth1Provider provider = getJiraOAuth1Provider(settings, url);

    String accessToken = provider.requestAcessToken(temporaryToken, verifierCode);

    try {
      UserKeyManagerData userKMData =
          userService.getBotUserAccountKeyData(settings.getConfigurationId());
      String encryptedAccessToken = cryptoService.encrypt(accessToken, userKMData.getPrivateKey());

      JiraOAuth1Data jiraOAuth1Data = new JiraOAuth1Data(temporaryToken, encryptedAccessToken);
      userAuthData.setData(jiraOAuth1Data);

      authRepoService.save(settings.getType(), settings.getConfigurationId(), userAuthData);
    } catch (CryptoException e) {
      throw new JiraOAuth1Exception(
          MSG.getMessage("integration.jira.auth.encrypt", temporaryToken),
          MSG.getMessage("integration.jira.auth.encrypt.solution"));
    }
  }

  /**
   * Builds a JiraOAuth1Provider.
   * @param settings Integration settings.
   * @param baseUrl Base URL.
   * @return JiraOAuth1Provider configured.
   * @throws AuthorizationException Thrown in case of error.
   */
  public JiraOAuth1Provider getJiraOAuth1Provider(IntegrationSettings settings, String baseUrl)
      throws AuthorizationException {
    AppAuthorizationModel appAuthorizationModel = getAuthorizationModel(settings);
    String consumerKey = (String) appAuthorizationModel.getProperties().get(CONSUMER_KEY);
    String privateKey = getPrivateKey(settings);
    String callbackUrl = getCallbackUrl(settings);

    JiraOAuth1Provider provider = context.getBean(JiraOAuth1Provider.class);
    provider.configure(consumerKey, privateKey, baseUrl, callbackUrl);
    return provider;
  }

  /**
   * Get an access token for user to perform calls to an external system resource.
   * @param settings Integration settings
   * @param url Integration URL.
   * @param userId User id.
   * @return An access token.
   * @throws AuthorizationException Invalid JIRA authorization data or failure to read
   * authorization data.
   */
  public String getAccessToken(IntegrationSettings settings, String url, Long userId)
      throws AuthorizationException {
    UserAuthorizationData userAuthorizationData = getUserAuthorizationData(settings, url, userId);

    if ((userAuthorizationData == null) || (userAuthorizationData.getData() == null)) {
      return null;
    }

    JiraOAuth1Data jiraOAuth1Data = null;
    try {
      jiraOAuth1Data = getJiraOAuth1Data(settings, userAuthorizationData);
    } catch (CryptoException e) {
      LOGGER.warn("There is a cryptography problem, the OAuth process must be restarted.", e);
    }

    if (jiraOAuth1Data == null || StringUtils.isEmpty(jiraOAuth1Data.getAccessToken())) {
      return null;
    }

    return jiraOAuth1Data.getAccessToken();
  }

  /**
   * Retrieves the specific JIRA authorization data.
   * @param settings Integration settings
   * @param userAuthorizationData User authorization data
   * @return JIRA authorization data
   * @throws AuthorizationException Invalid JIRA authorization data.
   * @throws CryptoException Thrown when the cryptography process cannot be done.
   */
  private JiraOAuth1Data getJiraOAuth1Data(IntegrationSettings settings,
      UserAuthorizationData userAuthorizationData) throws AuthorizationException, CryptoException {
    try {
      JiraOAuth1Data jiraOAuth1Data = JsonUtils.readValue(userAuthorizationData.getData(),
          JiraOAuth1Data.class);
      String accessToken = jiraOAuth1Data.getAccessToken();
      if (!StringUtils.isEmpty(accessToken)) {
        UserKeyManagerData userKMData =
            userService.getBotUserAccountKeyData(settings.getConfigurationId());
        String decryptedAccessToken = cryptoService.decrypt(accessToken, userKMData.getPrivateKey());
        jiraOAuth1Data.setAccessToken(decryptedAccessToken);
      }
      return jiraOAuth1Data;
    } catch (IOException e) {
      throw new AuthorizationException("Invalid temporary token");
    }
  }

  /**
   * Find a user authorization data that matches with the given url and userId
   * @param settings Integration settings.
   * @param url Third-party integration url.
   * @param userId User id.
   * @return Data found or null otherwise.
   * @throws AuthorizationException Failure to read authorization data.
   */
  public UserAuthorizationData getUserAuthorizationData(IntegrationSettings settings, String url,
      Long userId) throws AuthorizationException {
    return authRepoService.find(settings.getType(), settings.getConfigurationId(), url, userId);
  }

  /**
   * Return an URL to be called after the authorization callback is called, this can be useful
   * to close the authorization popUp window, for example.
   * @param settings Integration settings.
   * @return A valid URL to redirect or null when no redirection must be performed.
   */
  public String getAuthorizationRedirectUrl(IntegrationSettings settings) {
    String appId = properties.getApplicationId(settings.getType());
    String baseUrl = properties.getApplicationUrl(appId);
    return baseUrl + CLOSE_POP_UP_HTML;
  }
}
