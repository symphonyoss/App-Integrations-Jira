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
package org.symphonyoss.integration.jira.exception;

import org.symphonyoss.integration.exception.IntegrationRuntimeException;

/**
 * Unchecked exception thrown to indicate the body path content was not found.
 *
 * Created by alexandre-silva-daitan on 22/08/17.
 */
public class BodyContentNoFoundException extends IntegrationRuntimeException {

  public BodyContentNoFoundException(String serviceName, String message, String solution) {
    super(serviceName, message, solution);
  }
}
