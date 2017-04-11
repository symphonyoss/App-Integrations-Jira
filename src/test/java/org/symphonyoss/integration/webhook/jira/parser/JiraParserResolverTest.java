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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.event.MessageMLVersionUpdatedEventData;
import org.symphonyoss.integration.model.message.MessageMLVersion;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for {@link JiraParserResolver}
 * Created by rsanchez on 23/03/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class JiraParserResolverTest {

  @Spy
  private List<ParserFactory> factories = new ArrayList<>();

  @Spy
  private V1ParserFactory v1Factory;

  @Spy
  private V2ParserFactory v2Factory;

  @InjectMocks
  private JiraParserResolver resolver;

  @Before
  public void setup() {
    factories.add(v1Factory);
    factories.add(v2Factory);
  }

  @Test
  public void testInit() {
    resolver.init();
    assertEquals(v1Factory, resolver.getFactory());
  }

  @Test
  public void testHandleMessageMLV1() {
    MessageMLVersionUpdatedEventData event = new MessageMLVersionUpdatedEventData(MessageMLVersion.V1);
    resolver.handleMessageMLVersionUpdatedEvent(event);

    assertEquals(v1Factory, resolver.getFactory());
  }

  @Test
  public void testHandleMessageMLV2() {
    MessageMLVersionUpdatedEventData event = new MessageMLVersionUpdatedEventData(MessageMLVersion.V2);
    resolver.handleMessageMLVersionUpdatedEvent(event);

    assertEquals(v2Factory, resolver.getFactory());
  }

}
