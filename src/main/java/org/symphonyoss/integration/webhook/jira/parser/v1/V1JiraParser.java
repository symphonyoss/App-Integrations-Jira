package org.symphonyoss.integration.webhook.jira.parser.v1;

import org.symphonyoss.integration.webhook.jira.parser.JiraParser;

import java.util.List;

/**
 * Parser interface for the MessageML v1.
 * Created by rsanchez on 21/03/17.
 */
public interface V1JiraParser extends JiraParser {

  List<String> getEvents();

  void setJiraUser(String jiraUser);

}
