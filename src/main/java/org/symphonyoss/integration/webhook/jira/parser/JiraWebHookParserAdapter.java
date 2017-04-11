package org.symphonyoss.integration.webhook.jira.parser;

import com.fasterxml.jackson.databind.JsonNode;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;
import org.symphonyoss.integration.webhook.parser.WebHookParser;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Adapt the interface {@link WebHookParser} to {@link JiraParser}
 * Created by rsanchez on 11/04/17.
 */
public class JiraWebHookParserAdapter implements WebHookParser {

  private JiraParser parser;

  public JiraWebHookParserAdapter(JiraParser parser) {
    this.parser = parser;
  }

  @Override
  public List<String> getEvents() {
    return parser.getEvents();
  }

  @Override
  public Message parse(WebHookPayload payload) throws WebHookParseException {
    try {
      JsonNode rootNode = JsonUtils.readTree(payload.getBody());
      Map<String, String> parameters = payload.getParameters();

      return parser.parse(parameters, rootNode);
    } catch (IOException e) {
      throw new JiraParserException(
          "Something went wrong while trying to convert your message to the expected format", e);
    }
  }

}
