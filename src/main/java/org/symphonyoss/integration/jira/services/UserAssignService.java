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

package org.symphonyoss.integration.jira.services;

import static org.symphonyoss.integration.exception.RemoteApiException.COMPONENT;
import static org.symphonyoss.integration.jira.api.JiraApiResourceConstants.ISSUE_KEY;
import static org.symphonyoss.integration.jira.properties.ServiceProperties.APPLICATION_KEY_ERROR;
import static org.symphonyoss.integration.jira.properties.ServiceProperties.ISSUEKEY_NOT_FOUND;
import static org.symphonyoss.integration.jira.properties.ServiceProperties.MISSING_FIELD;
import static org.symphonyoss.integration.jira.properties.ServiceProperties.USERNAME_INVALID;
import static org.symphonyoss.integration.jira.webhook.JiraParserConstants.NAME_PATH;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.GenericData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Exception;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1HttpRequestException;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;
import org.symphonyoss.integration.jira.exception.JiraAuthorizationException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.ErrorResponse;

import java.net.URL;

/**
 * Service that handles the assign user to issue on the JIRA API
 * Created by hamitay on 8/15/17.
 */
@Component
public class UserAssignService {

  @Autowired
  private LogMessageSource logMessage;

  public ResponseEntity assignUserToIssue(String accessToken, String issueKey, String username,
      URL integrationURL, OAuth1Provider provider) {

    //Validate input
    if (StringUtils.isEmpty(issueKey)) {
      ErrorResponse response = new ErrorResponse();
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      response.setMessage(logMessage.getMessage(MISSING_FIELD, ISSUE_KEY));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    //Jira requisition
    try {

      GenericData data = new GenericData();
      data.put(NAME_PATH, username);
      JsonHttpContent content = new JsonHttpContent(new JacksonFactory(), data);
      provider.makeAuthorizedRequest(accessToken, integrationURL, HttpMethods.PUT, content);

    } catch (OAuth1HttpRequestException e) {
      if (e.getCode() == HttpStatus.NOT_FOUND.value()) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setMessage(logMessage.getMessage(ISSUEKEY_NOT_FOUND, issueKey));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
      }
      if (e.getCode() == HttpStatus.BAD_REQUEST.value()) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage(logMessage.getMessage(USERNAME_INVALID, username));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
      }
    } catch (OAuth1Exception e) {
      throw new JiraAuthorizationException(COMPONENT,
          logMessage.getMessage(APPLICATION_KEY_ERROR), e);
    }

    return ResponseEntity.ok(HttpStatus.OK);
  }

}
