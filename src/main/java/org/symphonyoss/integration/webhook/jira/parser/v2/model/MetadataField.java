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

package org.symphonyoss.integration.webhook.jira.parser.v2.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Field that represents a pure text without any markup.
 * Created by rsanchez on 30/03/17.
 */
public class MetadataField {

  private String key;

  private String value;

  @XmlAttribute
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  @XmlAttribute
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void process(EntityObject root, JsonNode node) {
    JsonNode resultNode = getResultNode(node, value);
    String value = resultNode.asText(StringUtils.EMPTY);

    if (StringUtils.isNotEmpty(value)) {
      root.addContent(getKey(), value);
    }
  }

  private JsonNode getResultNode(JsonNode node, String jsonKey) {
    String[] nodeKeys = jsonKey.split("\\.");

    JsonNode resultNode = node;

    for (String key : nodeKeys) {
      resultNode = resultNode.path(key);
    }

    return resultNode;
  }

  @Override
  public String toString() {
    return "MetadataField{" +
        "key='" + key + '\'' +
        ", value='" + value + '\'' +
        '}';
  }
}
