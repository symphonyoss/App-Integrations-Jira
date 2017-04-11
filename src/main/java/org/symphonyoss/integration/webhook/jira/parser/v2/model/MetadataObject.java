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

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

/**
 * Model class that represents the tag <object> inside the metadata.
 * Created by rsanchez on 30/03/17.
 */
public class MetadataObject {

  private String id;

  private String type;

  private String version;

  private List<MetadataField> fields;

  private List<MetadataObject> children;

  @XmlAttribute
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  @XmlElement(name = "field")
  public List<MetadataField> getFields() {
    return fields;
  }

  public void setFields(List<MetadataField> fields) {
    this.fields = fields;
  }

  @XmlElement(name = "object")
  public List<MetadataObject> getChildren() {
    return children;
  }

  public void setChildren(List<MetadataObject> children) {
    this.children = children;
  }

  @Override
  public String toString() {
    return "MetadataObject{" +
        "id='" + id + '\'' +
        ", type='" + type + '\'' +
        ", version='" + version + '\'' +
        ", fields=" + fields +
        ", children=" + children +
        '}';
  }
}
