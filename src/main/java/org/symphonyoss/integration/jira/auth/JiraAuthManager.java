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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.exception.IntegrationRuntimeException;
import org.symphonyoss.integration.model.yaml.AppAuthenticationModel;
import org.symphonyoss.integration.exception.bootstrap.CertificateNotFoundException;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.utils.IntegrationUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * Service component responsible to provide the authentication properties from JIRA application.
 *
 * This component reads the YAML configuration file to retrieve application name and application
 * URL. It should also read the application public key configured on the filesystem and validate it.
 *
 * Created by rsanchez on 24/07/17.
 */
@Component
public class JiraAuthManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(JiraAuthManager.class);

  private static final String COMPONENT = "JIRA Authentication Manager";

  public static final String PUBLIC_KEY_FILENAME = "publicKeyFilename";

  public static final String PUBLIC_KEY_FILENAME_TEMPLATE = "%s_app_pub.pem";

  public static final String PUBLIC_KEY = "publicKey";

  private static final String PUBLIC_KEY_PREFIX = "-----BEGIN PUBLIC KEY-----\n";

  private static final String PUBLIC_KEY_SUFFIX = "-----END PUBLIC KEY-----\n";

  @Autowired
  private IntegrationProperties properties;

  @Autowired
  private IntegrationUtils utils;

  /**
   * Provide the authentication properties from JIRA application.
   *
   * @param settings Integration settings
   * @return Authentication properties
   */
  public AppAuthenticationModel getAuthentcationModel(IntegrationSettings settings) {
    String appType = settings.getType();
    Application application = properties.getApplication(appType);

    AppAuthenticationModel auth = application.getAuth();

    String publicKey = getPublicKey(auth, application);
    auth.getProperties().put(PUBLIC_KEY, publicKey);

    return auth;
  }

  /**
   * Read the application public key configured on the filesystem and validate it.
   *
   * @param authModel Authentication properties
   * @param application Application settings
   * @return Application public key
   */
  private String getPublicKey(AppAuthenticationModel authModel, Application application) {
    String filename = getPublicKeyFilename(authModel, application);
    String publicKey = readPublicKey(filename);

    if (StringUtils.isEmpty(publicKey)) {
      return null;
    }

    String pk = publicKey.replace(PUBLIC_KEY_PREFIX, StringUtils.EMPTY)
        .replace(PUBLIC_KEY_SUFFIX, StringUtils.EMPTY);

    if (validatePK(pk)) {
      return pk;
    }

    LOGGER.warn("Application public key is invalid, please check the file {}", filename);
    return null;
  }

  /**
   * Retrieve the application public key filename.
   *
   * @param authModel Authentication properties
   * @param application Application settings
   * @return Application public key filename
   */
  private String getPublicKeyFilename(AppAuthenticationModel authModel, Application application) {
    String fileName = (String) authModel.getProperties().get(PUBLIC_KEY_FILENAME);

    if (StringUtils.isEmpty(fileName)) {
      return String.format(PUBLIC_KEY_FILENAME_TEMPLATE, application.getId());
    }

    return fileName;
  }

  /**
   * Read the public key configured on the filesystem.
   *
   * @param fileName Public key filename
   * @return Application public key or null if the file not found
   */
  private String readPublicKey(String fileName) {
    try {
      String certsDir = utils.getCertsDirectory();
      Path pubKeyPath = Paths.get(certsDir + fileName);

      if (Files.exists(pubKeyPath, LinkOption.NOFOLLOW_LINKS)) {
        byte[] pubKeyBytes = Files.readAllBytes(pubKeyPath);
        return new String(pubKeyBytes);
      }

      LOGGER.error("Cannot read the public key. Make sure the file {} already exists",
          pubKeyPath.toAbsolutePath());
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
   * Validate the application public key.
   *
   * @param publicKey Application public key
   * @return true if the public key is valid or false otherwise
   */
  private boolean validatePK(String publicKey) {
    try {
      byte[] encoded = Base64.decodeBase64(publicKey);
      X509EncodedKeySpec spec = new X509EncodedKeySpec(encoded);

      KeyFactory kf = KeyFactory.getInstance("RSA");
      kf.generatePublic(spec);

      return true;
    } catch (NoSuchAlgorithmException e) {
      LOGGER.error("RSA algorithm is not supported in this environment", e);
    } catch (InvalidKeySpecException e) {
      LOGGER.error("Invalid public key", e);
    } catch (Exception e) {
      throw new IntegrationRuntimeException(COMPONENT,
          "Unexpected error when validate the application public key", e);
    }

    return false;
  }

}
