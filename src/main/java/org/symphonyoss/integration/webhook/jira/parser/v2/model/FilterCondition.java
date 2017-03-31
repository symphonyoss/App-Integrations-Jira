package org.symphonyoss.integration.webhook.jira.parser.v2.model;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Metadata field used to filter an array of objects according to the specific value (tag <equals>) in the path.
 * Created by rsanchez on 31/03/17.
 */
public class FilterCondition {

  private String arrayPath;

  private String field;

  private String equals;

  @XmlAttribute
  public String getArrayPath() {
    return arrayPath;
  }

  public void setArrayPath(String arrayPath) {
    this.arrayPath = arrayPath;
  }

  @XmlAttribute
  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  @XmlAttribute
  public String getEquals() {
    return equals;
  }

  public void setEquals(String equals) {
    this.equals = equals;
  }
}
