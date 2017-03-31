package org.symphonyoss.integration.webhook.jira.parser.v2.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Field that represents an URL composed by a set of text fields.
 * Created by rsanchez on 30/03/17.
 */
public class UrlField extends MetadataField {

  private String path;

  private List<TextField> fields;

  @XmlAttribute
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @XmlElement(name = "text")
  public List<TextField> getFields() {
    return fields;
  }

  public void setFields(
      List<TextField> fields) {
    this.fields = fields;
  }

  @Override
  public void process(EntityObject root, JsonNode node) {
    JsonNode resultNode = getResultNode(node, path);
    String value = resultNode.asText(StringUtils.EMPTY);

    String baseUrl = getBaseUrl(value);

    if (StringUtils.isNotEmpty(baseUrl)) {
      StringBuilder result = new StringBuilder();

      for (TextField field : fields) {
        if (field.getValue() != null) {
          result.append(field.getValue());
          result.append("/");
        } else {
          JsonNode fieldNode = field.getResultNode(node, field.getPath());
          String fieldValue = fieldNode.asText(StringUtils.EMPTY);

          if (StringUtils.isNotEmpty(fieldValue)) {
            result.append(fieldValue);
            result.append("/");
          }
        }
      }

      if (StringUtils.isNotEmpty(result)) {
        result.insert(0, baseUrl + "/");
        root.addContent(getKey(), result);
      }
    }
  }

  private String getBaseUrl(String selfUrl) {
    try {
      URL url = new URL(selfUrl);

      StringBuilder baseUrl = new StringBuilder();

      baseUrl.append(url.getProtocol());
      baseUrl.append("://");
      baseUrl.append(url.getHost());

      if (url.getPort() != -1) {
        baseUrl.append(":");
        baseUrl.append(url.getPort());
      }

      return baseUrl.toString();
    } catch (MalformedURLException e) {
      return StringUtils.EMPTY;
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
