package org.symphonyoss.integration.webhook.jira.parser;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.symphony.api.pod.client.ApiException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.mockito.Mock;
import org.symphonyoss.integration.IntegrationAtlas;
import org.symphonyoss.integration.service.UserService;

import java.io.File;
import java.io.IOException;

/**
 * Created by rsanchez on 22/07/16.
 */
@Ignore("not a test per se")
public class JiraParserTest {

  @Mock
  protected IntegrationAtlas integrationAtlas;

  @Mock
  protected UserService userService;

  @Before
  public void setup() throws ApiException {
    when(userService.getUserByEmail(anyString(), anyString())).thenReturn(createEntityUser());

  }

  private org.symphonyoss.integration.entity.model.User createEntityUser() {
    org.symphonyoss.integration.entity.model.User user =
        new org.symphonyoss.integration.entity.model.User();
    user.setId(7627861918843L);
    user.setUserName("test");
    user.setEmailAddress("test@symphony.com");
    user.setDisplayName("Test User");
    return user;
  }

  public String readFile(String fileName) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String expected =
        FileUtils.readFileToString(new File(classLoader.getResource(fileName).getPath()));
    return expected = expected.replaceAll("\n", "");
  }

}
