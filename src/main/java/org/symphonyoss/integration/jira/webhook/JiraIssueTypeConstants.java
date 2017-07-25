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

package org.symphonyoss.integration.jira.webhook;

/**
 * This class contains the possible issue types, retrieved from Jira's callback payload at
 * /issue/issueType/name
 * Created by apimentel on 19/05/17.
 */
public class JiraIssueTypeConstants {
  public static final String BUG_TYPE = "bug";
  public static final String INCIDENT_TYPE = "incident";
  public static final String SUPPORT_ISSUE_TYPE = "support_issue";
  public static final String INCIDENT_SEVERITY_1_TYPE = "incident_severity_1";

  public static final String EPIC_TYPE = "epic";
  public static final String INCIDENT_SEVERITY_4_TYPE = "incident_severity_4";
  public static final String DOCUMENTATION_TYPE = "documentation";

  public static final String STORY_TYPE = "story";
  public static final String NEW_FEATURE_TYPE = "new_feature";
  public static final String IMPROVEMENT_TYPE = "improvement";
  public static final String CHANGE_REQUEST_TYPE = "change_request";

  public static final String SPIKE_TYPE = "spike";
  public static final String PROBLEM_TYPE = "problem";
  public static final String INCIDENT_SEVERITY_2_TYPE = "incident_severity_2";
  public static final String INCIDENT_SEVERITY_3_TYPE = "incident_severity_3";

  public static final String TASK_TYPE = "task";
}
