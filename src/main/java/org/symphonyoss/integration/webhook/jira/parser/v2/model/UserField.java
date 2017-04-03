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

import static org.symphonyoss.integration.entity.model.EntityConstants.DISPLAY_NAME_ENTITY_FIELD;
import static org.symphonyoss.integration.entity.model.EntityConstants.EMAIL_ADDRESS_ENTITY_FIELD;
import static org.symphonyoss.integration.entity.model.EntityConstants.USERNAME_ENTITY_FIELD;
import static org.symphonyoss.integration.entity.model.EntityConstants.USER_ID;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.service.UserService;
import org.symphonyoss.integration.utils.ApplicationContextUtils;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Field that represents an user information.
 * Created by rsanchez on 30/03/17.
 */
public class UserField extends MetadataField {

  private String email;

  private String displayName;

  @XmlAttribute
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @XmlAttribute
  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public void process(EntityObject root, JsonNode node) {
    JsonNode emailAddress = getResultNode(node, email);

    User user = null;
    UserService userService = ApplicationContextUtils.getBean(UserService.class);

    if (userService != null) {
      user = userService.getUserByEmail(getIntegrationUser(), emailAddress.asText(StringUtils.EMPTY).trim());
    }

    if ((user == null) || (user.getId() == null)) {
      JsonNode displayNameNode = getResultNode(node, displayName);
      root.addContent(DISPLAY_NAME_ENTITY_FIELD, displayNameNode.asText(StringUtils.EMPTY));
    } else {
      root.addContent(USER_ID, user.getId());
      root.addContent(EMAIL_ADDRESS_ENTITY_FIELD, user.getEmailAddress());
      root.addContent(USERNAME_ENTITY_FIELD, user.getUsername());
      root.addContent(DISPLAY_NAME_ENTITY_FIELD, user.getDisplayName());
    }
  }

  @Override
  public String toString() {
    return "UserField{" +
        "email='" + email + '\'' +
        ", displayName='" + displayName + '\'' +
        '}';
  }
}
