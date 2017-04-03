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

import static org.symphonyoss.integration.parser.ParserUtils.MESSAGEML_LINEBREAK;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.symphonyoss.integration.webhook.jira.parser.v1.JiraParserUtils;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Field that represents a text with HTML markup.
 * Created by rsanchez on 30/03/17.
 */
public class HtmlTextField extends MetadataField {

  private String path;

  @XmlAttribute
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public void process(EntityObject root, JsonNode node) {
    JsonNode resultNode = getResultNode(node, path);
    String value = resultNode.asText(StringUtils.EMPTY);
    String formattedValue =
        JiraParserUtils.stripJiraFormatting(value).replaceAll("\n", MESSAGEML_LINEBREAK);
    root.addContent(getKey(), StringEscapeUtils.escapeXml10(formattedValue));
  }

  @Override
  public String toString() {
    return "TextField{" +
        "key='" + getKey() + '\'' +
        ", path='" + path + '\'' +
        '}';
  }
}
