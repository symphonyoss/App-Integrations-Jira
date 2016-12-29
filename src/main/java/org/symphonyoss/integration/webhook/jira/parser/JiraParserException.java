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

package org.symphonyoss.integration.webhook.jira.parser;

import org.symphonyoss.integration.webhook.exception.WebHookParseException;

/**
 * Exception to report the failures to validate JIRA messages.
 * Created by rsanchez on 17/05/16.
 */
public class JiraParserException extends WebHookParseException {

  private static final String COMPONENT = "JIRA Webhook Dispatcher";

  public JiraParserException(String message) {
    super(COMPONENT, message);
  }

  public JiraParserException(String message, Exception e) {
    super(COMPONENT, message, e);
  }

}
