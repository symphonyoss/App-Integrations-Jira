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

package org.symphonyoss.integration.webhook.jira;

/**
 * This class contains the constants to access data in the JSON payload received from JIRA, as well as constants to
 * build the entity contained in the webhook message dispatched by the JIRA parser.
 *
 * Created by rsanchez on 18/05/16.
 */
public final class JiraParserConstants {

  /**
   * Avoid initialization
   */
  private JiraParserConstants(){}

  public static final String JIRA = "jira";

  public static final String ISSUE_PATH = "issue";

  public static final String FIELD_PATH = "field";

  public static final String FIELDS_PATH = "fields";

  public static final String PRIORITY_PATH = "priority";

  public static final String ISSUETYPE_PATH = "issuetype";

  public static final String PROJECT_PATH = "project";

  public static final String USER_PATH = "user";

  public static final String SUMMARY_PATH = "summary";

  public static final String EMAIL_ADDRESS_PATH = "emailAddress";

  public static final String ASSIGNEE_PATH = "assignee";

  public static final String LABELS_PATH = "labels";

  public static final String KEY_PATH = "key";

  public static final String SELF_PATH = "self";

  public static final String NAME_PATH = "name";

  public static final String COMMENT_PATH = "comment";

  public static final String UPDATE_AUTHOR_PATH = "updateAuthor";

  public static final String BODY_PATH = "body";

  public static final String CHANGELOG_PATH = "changelog";

  public static final String ITEMS_PATH = "items";

  public static final String STATUS_PATH = "status";

  public static final String TOSTRING_PATH = "toString";

  public static final String FROMSTRING_PATH = "fromString";

  public static final String UNKNOWN_PROJECT = "Unknown Project";

  public static final String AUTHOR_PATH = "author";

  public static final String DESCRIPTION_PATH = "description";

  public static final String EPIC_PATH = "epic";

  public static final String EPIC_LINK_PATH = "Epic Link";

  public static final String DISPLAY_NAME_PATH = "displayName";

  public static final String VISIBILITY_PATH = "visibility";

  public static final String PROJECT_ENTITY_FIELD = "project";

  public static final String KEY_ENTITY_FIELD = "key";

  public static final String SUBJECT_ENTITY_FIELD = "subject";

  public static final String TYPE_ENTITY_FIELD = "type";

  public static final String DESCRIPTION_ENTITY_FIELD = "description";

  public static final String LINK_ENTITY_FIELD = "link";

  public static final String PRIORITY_ENTITY_FIELD = "priority";

  public static final String ISSUE_ENTITY_FIELD = "issue";

  public static final String LABELS_ENTITY_FIELD = "labels";

  public static final String EPIC_ENTITY_FIELD = "epic";

  public static final String USER_ENTITY_FIELD = "user";

  public static final String AUTHOR_ENTITY_FIELD = "author";

  public static final String COMMENT_ENTITY_FIELD = "comment";

  public static final String TEXT_ENTITY_FIELD = "text";

}
