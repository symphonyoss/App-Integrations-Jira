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

package org.symphonyoss.integration.webhook.jira.parser.v2;

import com.fasterxml.jackson.databind.JsonNode;
import org.symphonyoss.integration.webhook.jira.parser.v2.model.EntityObject;

import java.util.Arrays;
import java.util.List;

/**
 * Mock class to simulate the behaviour for {@link MetadataParser} class.
 * Created by rsanchez on 03/04/17.
 */
public class MockMetadataParser extends MetadataParser {

  private static final String MOCK_EVENT = "mockEvent";

  private String templateFile;

  private String metadataFile;

  public MockMetadataParser(String templateFile, String metadataFile) {
    this.templateFile = templateFile;
    this.metadataFile = metadataFile;
  }

  @Override
  protected void preProcessInputData(JsonNode input) {
    // Do nothing
  }

  @Override
  protected void postProcessOutputData(EntityObject output, JsonNode input) {
    // Do nothing
  }

  @Override
  protected String getTemplateFile() {
    return templateFile;
  }

  @Override
  protected String getMetadataFile() {
    return metadataFile;
  }

  @Override
  public List<String> getEvents() {
    return Arrays.asList(MOCK_EVENT);
  }
}
