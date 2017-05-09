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
package org.symphonyoss.integration.webhook.jira.parser.utils;

import static org.symphonyoss.integration.messageml.MessageMLFormatConstants.MESSAGEML_END;
import static org.symphonyoss.integration.messageml.MessageMLFormatConstants.MESSAGEML_START;

import com.fasterxml.jackson.databind.JsonNode;
import org.symphonyoss.integration.json.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by apimentel on 28/04/17.
 */
public class FileUtils {
  public static JsonNode readJsonFromFile(String filename) throws IOException {
    ClassLoader classLoader = FileUtils.class.getClassLoader();
    return JsonUtils.readTree(classLoader.getResourceAsStream(filename));
  }

  public static String readMessageMLFile(String fileName) throws IOException {
    String expected = readFile(fileName).replaceAll("\n", "");
    return MESSAGEML_START + expected + MESSAGEML_END;
  }

  public static String readFile(String fileName) throws IOException {
    ClassLoader classLoader = FileUtils.class.getClassLoader();
    String file =
        org.apache.commons.io.FileUtils.readFileToString(
            new File(classLoader.getResource(fileName).getPath()), Charset
                .defaultCharset());
    return file;
  }
}
