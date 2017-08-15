package org.symphonyoss.integration.jira.services;

import static org.symphonyoss.integration.exception.RemoteApiException.COMPONENT;
import static org.symphonyoss.integration.jira.properties.ServiceProperties.APPLICATION_KEY_ERROR;
import static org.symphonyoss.integration.jira.properties.ServiceProperties.INVALID_URL_ERROR;
import static org.symphonyoss.integration.jira.webhook.JiraParserConstants.NAME_PATH;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.GenericData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authorization.AuthorizedIntegration;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Exception;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;
import org.symphonyoss.integration.exception.IntegrationRuntimeException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.ErrorResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Service that handles the assign user to issue on the JIRA API
 * Created by hamitay on 8/15/17.
 */

@Component
public class UserAssignService {

  private static final String PATH_JIRA_API_ASSIGN_ISSUE =
      "/rest/api/latest/issue/%s/assignee";

  @Autowired
  private LogMessageSource logMessage;

  public ResponseEntity assignUserToIssue(String accessToken, String issueKey, String username,
      String jiraIntegrationURL,
      AuthorizedIntegration authIntegration) throws IOException {

    //Validate input
    if (issueKey.isEmpty()) {
      ErrorResponse response = new ErrorResponse();
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    //Jira requisition
    try {
      OAuth1Provider provider = authIntegration.getOAuth1Provider(jiraIntegrationURL);
      URL myselfUrl = new URL(jiraIntegrationURL);
      myselfUrl = new URL(myselfUrl, String.format(PATH_JIRA_API_ASSIGN_ISSUE, issueKey));

      GenericData data = new GenericData();
      data.put(NAME_PATH, username);
      JsonHttpContent content = new JsonHttpContent(new JacksonFactory(), data);

      provider.makeAuthorizedRequest(accessToken, myselfUrl, HttpMethods.PUT, content);
    } catch (OAuth1Exception e) {
      throw new IntegrationRuntimeException(COMPONENT,
          logMessage.getMessage(APPLICATION_KEY_ERROR), e);
    } catch (MalformedURLException e) {
      String errorMessage = logMessage.getMessage(INVALID_URL_ERROR, jiraIntegrationURL);
      throw new RuntimeException(errorMessage, e);
    }

    return ResponseEntity.ok(HttpStatus.OK);
  }

}
