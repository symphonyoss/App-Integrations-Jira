package org.symphonyoss.integration.webhook.jira.parser.v2.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Field that represents a list of pure text without any markup.
 * Created by rsanchez on 30/03/17.
 */
public class TextArrayField extends MetadataField {

  private String path;

  private String type;

  private String version;

  private TextField text;

  @XmlAttribute
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @XmlAttribute
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @XmlAttribute
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public TextField getText() {
    return text;
  }

  public void setText(TextField text) {
    this.text = text;
  }

  @Override
  public void process(EntityObject root, JsonNode node) {
    List<EntityObject> list = new ArrayList<>();

    JsonNode arrayNode = getResultNode(node, path);

    for (int i = 0; i < arrayNode.size(); i++) {
      String name = arrayNode.get(i).asText(StringUtils.EMPTY);

      if (text.getReplace() != null) {
        name = name.replace(text.getReplace(), StringUtils.EMPTY);
      }

      EntityObject nestedObject = new EntityObject(type, version);
      nestedObject.addContent(text.getKey(), name);

      list.add(nestedObject);
    }

    root.addContent(getKey(), list);
  }

  @Override
  public String toString() {
    return "TextField{" +
        "key='" + getKey() + '\'' +
        ", path='" + path + '\'' +
        '}';
  }
}
