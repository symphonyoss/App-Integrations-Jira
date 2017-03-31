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

package org.symphonyoss.integration.webhook.jira.parser.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.integration.agent.api.model.V3Message;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.webhook.jira.parser.JiraParser;
import org.symphonyoss.integration.webhook.jira.parser.JiraParserException;
import org.symphonyoss.integration.webhook.jira.parser.v2.model.EntityObject;
import org.symphonyoss.integration.webhook.jira.parser.v2.model.Metadata;
import org.symphonyoss.integration.webhook.jira.parser.v2.model.MetadataField;
import org.symphonyoss.integration.webhook.jira.parser.v2.model.MetadataObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Abstract parser class responsible to read an XML input file that contains metadata objects to
 * be used to create an Entity JSON.
 *
 * Created by rsanchez on 29/03/17.
 */
public abstract class MetadataParser implements JiraParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetadataParser.class);

  private static final String BASE_METADATA_PATH = "metadata/";

  private static final String BASE_TEMPLATE_PATH = "templates/";

  private static final String DEFAULT_VERSION = "1.0";

  private String integrationUser;

  private Unmarshaller unmarshaller;

  private Metadata metadata;

  private String messageMLTemplate;

  /**
   * Initializes the JAXB context and unmarshaller object.
   * @throws JAXBException
   */
  public MetadataParser() {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Metadata.class);
      this.unmarshaller = jaxbContext.createUnmarshaller();
    } catch (JAXBException e) {
      throw new IllegalStateException("Fail to initialize JAXB context", e);
    }

    readMetadataFile();
    readTemplateFile();
  }

  /**
   * Read metadata file.
   */
  private void readMetadataFile() {
    try {
      String fileLocation = BASE_METADATA_PATH + getMetadataFile();

      InputStream resource = getClass().getClassLoader().getResourceAsStream(fileLocation);
      this.metadata = (Metadata) unmarshaller.unmarshal(resource);
    } catch (JAXBException e) {
      LOGGER.error("Cannot read the metadata file " + getMetadataFile(), e);
    }
  }

  /**
   * Read template file.
   */
  private void readTemplateFile() {
    String fileLocation = BASE_TEMPLATE_PATH + getTemplateFile();

    try(InputStream resource = getClass().getClassLoader().getResourceAsStream(fileLocation);
        BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {

      String line;
      StringBuilder responseData = new StringBuilder();

      while ((line = reader.readLine()) != null) {
        responseData.append(line);
        responseData.append('\n');
      }

      this.messageMLTemplate = responseData.toString();
    } catch (IOException e) {
      LOGGER.error("Cannot read the template file " + fileLocation, e);
    }
  }

  @Override
  public void setIntegrationUser(String integrationUser) {
    this.integrationUser = integrationUser;
  }

  @Override
  public Message parse(Map<String, String> parameters, JsonNode node) throws JiraParserException {
    if (StringUtils.isEmpty(messageMLTemplate)) {
      return null;
    }

    String entityJSON = getEntityJSON(node);

    if (StringUtils.isNotEmpty(entityJSON)) {
      V3Message message = new V3Message();
      message.setMessage(messageMLTemplate);
      message.setData(entityJSON);

      LOGGER.info("Message to be posted: " + message);

      return message;
    }

    return null;
  }

  /**
   * Retrieves the Entity JSON based on metadata objects.
   * @param node JSON input data
   * @return Entity JSON
   * @throws JiraParserException Failure to process JSON input data
   */
  private String getEntityJSON(JsonNode node) throws JiraParserException {
    if (metadata == null) {
      return null;
    }

    EntityObject root = new EntityObject(getEventType(), getVersion());
    List<MetadataObject> objects = metadata.getObjects();

    processMetadataObjects(root, node, objects);

    try {
      Map<String, Object> result = new LinkedHashMap<>();
      result.put(getEventName(), root);

      return JsonUtils.writeValueAsString(result);
    } catch (JsonProcessingException e) {
      LOGGER.error("Fail to parse incoming payload", e);
      return null;
    }
  }

  /**
   * Process metadata objects to generate Entity JSON. This method is called recursively for
   * the nested metadata objects. The metadata fields inside each metadata object have your own
   * logic to build the corresponding field into Entity JSON.
   * @param root Root object from Entity JSON
   * @param node JSON node received from the third-party service
   * @param objects List of metadata objects
   */
  private void processMetadataObjects(EntityObject root, JsonNode node, List<MetadataObject> objects) {
    for (MetadataObject object : objects) {
      EntityObject entity = new EntityObject(object.getType(), object.getVersion());

      if (object.getFields() != null) {
        for (MetadataField field : object.getFields()) {
          field.setIntegrationUser(integrationUser);
          field.process(entity, node);
        }
      }

      if (object.getChildren() != null) {
        processMetadataObjects(entity, node, object.getChildren());
      }

      if (!entity.getContent().isEmpty()) {
        root.addContent(object.getId(), entity);
      }
    }
  }

  /**
   * Get the template file inside the classpath
   * @return Template filename
   */
  protected abstract String getTemplateFile();

  /**
   * Get the metadata file inside the classpath
   * @return Metadata filename
   */
  protected abstract String getMetadataFile();

  /**
   * Get the event name
   * @return Event name
   */
  protected abstract String getEventName();

  /**
   * Get the event type
   * @return Event type
   */
  protected abstract String getEventType();

  /**
   * Get the event version. This version should use the notation 'Major.minor'.
   * Default version is '1.0'
   * @return Entity JSON version
   */
  protected String getVersion() {
    return DEFAULT_VERSION;
  }

}
