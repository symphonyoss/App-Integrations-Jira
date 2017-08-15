package org.symphonyoss.integration.jira.services;

import static org.symphonyoss.integration.exception.RemoteApiException.COMPONENT;
import static org.symphonyoss.integration.jira.api.JiraApiResourceConstants.ISSUE_KEY;
import static org.symphonyoss.integration.jira.properties.ServiceProperties.APPLICATION_KEY_ERROR;
import static org.symphonyoss.integration.jira.properties.ServiceProperties.MISSING_FIELD;
import static org.symphonyoss.integration.jira.webhook.JiraParserConstants.NAME_PATH;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.GenericData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Exception;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1HttpRequestException;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;
import org.symphonyoss.integration.exception.IntegrationRuntimeException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.ErrorResponse;

import java.io.IOException;
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
      URL integrationURL, OAuth1Provider provider) throws IOException {

    //Validate input
    if (issueKey.isEmpty()) {
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

    } catch (OAuth1Exception e) {
      throw new IntegrationRuntimeException(COMPONENT,
          logMessage.getMessage(APPLICATION_KEY_ERROR), e);
    } catch (OAuth1HttpRequestException e) {
      throw new IntegrationRuntimeException(COMPONENT,
          logMessage.getMessage(APPLICATION_KEY_ERROR), e);
    }

    return ResponseEntity.ok(HttpStatus.OK);
  }

}
