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

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.message.MessageMLVersion;

/**
 * Parser factory for the MessageML v2.
 * Created by rsanchez on 21/03/17.
 */
@Component
public class V2ParserFactory implements ParserFactory {

  @Override
  public boolean accept(MessageMLVersion version) {
    return MessageMLVersion.V2.equals(version);
  }

  @Override
  public void onConfigChange(IntegrationSettings settings) {
    // Do nothing
  }

  @Override
  public JiraParser getParser(JsonNode node) {
    // FIXME To be implemented APP-622
    throw new NotImplementedException("Parser not implemented");
  }

}
