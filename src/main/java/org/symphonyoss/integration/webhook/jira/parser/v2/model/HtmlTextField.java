package org.symphonyoss.integration.webhook.jira.parser.v2.model;

import com.fasterxml.jackson.databind.JsonNode;
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
    root.addContent(getKey(), JiraParserUtils.stripJiraFormatting(value));
  }

  @Override
  public String toString() {
    return "TextField{" +
        "key='" + getKey() + '\'' +
        ", path='" + path + '\'' +
        '}';
  }
}
