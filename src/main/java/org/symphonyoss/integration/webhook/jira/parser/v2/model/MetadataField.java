package org.symphonyoss.integration.webhook.jira.parser.v2.model;

import com.fasterxml.jackson.databind.JsonNode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Abstract class to be implemented by all different types of field like txt, html, user, etc.
 * Created by rsanchez on 30/03/17.
 */
public abstract class MetadataField {

  private String key;

  private String integrationUser;

  private FilterCondition filter;

  @XmlAttribute
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getIntegrationUser() {
    return integrationUser;
  }

  public void setIntegrationUser(String integrationUser) {
    this.integrationUser = integrationUser;
  }

  @XmlElement(name = "filter")
  public FilterCondition getFilter() {
    return filter;
  }

  public void setFilter(FilterCondition filter) {
    this.filter = filter;
  }

  public abstract void process(EntityObject root, JsonNode node);

  protected JsonNode getResultNode(JsonNode node, String jsonKey) {
    String[] nodeKeys = jsonKey.split("\\.");

    JsonNode resultNode = node;

    for (String key : nodeKeys) {
      resultNode = resultNode.path(key);

      if ((filter != null) && (filter.getArrayPath().equals(key)) && (resultNode.isArray())) {
        String field = filter.getField();
        String expected = filter.getEquals();

        for (JsonNode item : resultNode) {
          if (expected.equals(item.get(field).asText())) {
            resultNode = item;
            break;
          }
        }
      }
    }

    return resultNode;
  }
}
