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

package org.symphonyoss.integration.jira.authorization.oauth.v1;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link JiraOAuth1Provider}.
 *
 * Created by campidelli on 26-jul-17.
 */
@RunWith(MockitoJUnitRunner.class)
public class JiraOAuth1ProviderTest {

  @InjectMocks
  JiraOAuth1Provider authProvider;

  @Test
  public void test() {
    authProvider.configure("a", "b", "http://jira.com/", "http://symphony.com/callback");
  }
}
