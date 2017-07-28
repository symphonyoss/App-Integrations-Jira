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

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.logging.LogMessageSource;

/**
 * Unit tests for {@link JiraOAuth1Provider}.
 *
 * Created by campidelli on 26-jul-17.
 */
@RunWith(MockitoJUnitRunner.class)
public class JiraOAuth1ProviderTest {

  static final String BASE_URL = "http://jira.atlassian.com";
  static final String CALLBACK_URL = "http://myapplication.com/callback";
  static final String CONSUMER_KEY = "OauthKey";
  static final String PRIVATE_KEY = "MIICeQIBADANBgkqhkiG9w0BAQEFAASCAmMwggJfAgEAAoGBAN8wcS"
      + "F5AE7sL30p2mnM0X3T1OZy4BDfxucZTYdYmg99vqv6uVQyjc4zKOHRiwnCh2GwatT4jBfoQfWx6VUmvcxKHuZwcVCH"
      + "F/u/Vw85wsMDpD4pBglpX1GsFlfSQe1E115X7mHD7tHlkQHvtVplf5BmYxM6G2EljBmiRRQq4OLbAgMBAAECgYEAxu"
      + "54h6tAWRgvo9IgOVk0CIE9LEKL8L5knStybQbOGqyrvMJ3WdLNjlMPR2fsE8DtxmbmcfkvdUexMvtmzF0BoWDvJgqn"
      + "GaUr9l0gZfGCR0ir2PBJ7V9OOJz5ug4ExLz6S9WNV6RdtXOSXSbNG3/L+56tocA05JpZrZaUfK43V0ECQQDyjkokOr"
      + "k54DwdnSH86V2bXn+RlzAyumhfGKJpC7pbeZgcSJtkbV9RslEr+TcVuuJyHZGeWtPEStl1BaKnvRLxAkEA649aVUD1"
      + "b9Cly+Q2l7KbgDjny5k/Ezw7JK3hjYEKQrHjgkMejOuKSkeRz2imWD8PLoJ01GgMXLIiu+F1lb06iwJBAI7NJuldiV"
      + "+BnOLyd+gmnG20nPZiRIYZKQmTv0qJFRZ16A/+zz25Br1adl+lQcERXfBBaFIKt1KBnrU+tBx9PIECQQCLquG6rttX"
      + "wvSrIdMkuufsbNEzLNfzRcEjjF2yExLMXMEymS1iDL5gMHNJ8RjANhOAViWDU3YQ+CYUFCgt8pblAkEAhM5ky54f3U"
      + "ViEO29UyWv2ZNaZPd17bSr8HAo/lxXyju4TRNRB3vIq79lMNalX5HKHlI9EST7xXLh110xXRH9/Q\\=\\=";

  @Mock
  private LogMessageSource logMessage;

  @InjectMocks
  private JiraOAuth1Provider authProvider;

  @Test
  public void testConfigure() {
    assertNull(authProvider.getConsumerKey());
    assertNull(authProvider.getPrivateKey());
    assertNull(authProvider.getAuthorizationCallbackUrl());
    assertNull(authProvider.getRequestTemporaryTokenUrl());
    assertNull(authProvider.getAuthorizeTemporaryTokenUrl());
    assertNull(authProvider.getRequestAccessTokenUrl());

    authProvider.configure(CONSUMER_KEY, PRIVATE_KEY, BASE_URL, CALLBACK_URL);

    assertEquals(CONSUMER_KEY, authProvider.getConsumerKey());
    assertEquals(PRIVATE_KEY, authProvider.getPrivateKey());
    assertEquals(CALLBACK_URL, authProvider.getAuthorizationCallbackUrl().toString());
    assertTrue(authProvider.getRequestTemporaryTokenUrl().toString().contains(BASE_URL));
    assertTrue(authProvider.getAuthorizeTemporaryTokenUrl().toString().contains(BASE_URL));
    assertTrue(authProvider.getRequestAccessTokenUrl().toString().contains(BASE_URL));
  }

  @Test(expected = JiraOAuth1Exception.class)
  public void testConfigureMalformedBaseURL() {
    authProvider.configure(CONSUMER_KEY, PRIVATE_KEY, "?", CALLBACK_URL);
  }

  @Test(expected = JiraOAuth1Exception.class)
  public void testConfigureMalformedCallbackURL() {
    authProvider.configure(CONSUMER_KEY, PRIVATE_KEY, BASE_URL, "?");
  }
}