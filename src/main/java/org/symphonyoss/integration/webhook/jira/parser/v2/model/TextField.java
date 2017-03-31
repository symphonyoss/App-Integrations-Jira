package org.symphonyoss.integration.webhook.jira.parser.v2.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Field that represents a pure text without any markup.
 * Created by rsanchez on 30/03/17.
 */
public class TextField extends MetadataField {

  private String path;

  private String value;

  private String replace;

  private boolean optional;

  @XmlAttribute
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @XmlAttribute
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @XmlAttribute
  public String getReplace() {
    return replace;
  }

  public void setReplace(String replace) {
    this.replace = replace;
  }

  @XmlAttribute
  public boolean isOptional() {
    return optional;
  }

  public void setOptional(boolean optional) {
    this.optional = optional;
  }

  @Override
  public void process(EntityObject root, JsonNode node) {
    JsonNode resultNode = getResultNode(node, path);
    String value = resultNode.asText(StringUtils.EMPTY);

    if (replace != null) {
      value = value.replace(replace, StringUtils.EMPTY);
    }

    if ((!optional) || (StringUtils.isNotEmpty(value))) {
      root.addContent(getKey(), value);
    }
  }

  @Override
  public String toString() {
    return "TextField{" +
        "key='" + getKey() + '\'' +
        ", path='" + path + '\'' +
        '}';
  }
}
