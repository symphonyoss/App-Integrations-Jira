package org.symphonyoss.integration.jira.service;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Exception;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1HttpRequestException;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;
import org.symphonyoss.integration.exception.IntegrationRuntimeException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.ErrorResponse;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by alexandre-silva-daitan on 15/08/17.
 */
public class SearchAssignableUsersService {

  @Autowired
  private LogMessageSource logMessage;

  public ResponseEntity searchAssingablesUsers(String accessToken, OAuth1Provider provider,
      URL myselfUrl, String component) {
    HttpResponse response = null;
    try {
      provider.makeAuthorizedRequest(accessToken, myselfUrl, HttpMethods.GET, null);

    } catch (OAuth1Exception e) {
      throw new IntegrationRuntimeException(component,
          logMessage.getMessage("integration.jira.private.key.validation"), e);
    } catch (OAuth1HttpRequestException e) {
      if (e.getCode() == HttpStatus.NOT_FOUND.value()) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setMessage(e.getLocalizedMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
      }
    }
    return ResponseEntity.ok(HttpStatus.OK);
  }
}
