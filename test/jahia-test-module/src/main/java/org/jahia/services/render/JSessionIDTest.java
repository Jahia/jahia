package org.jahia.services.render;

import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jahia.bin.Jahia;
import org.jahia.settings.SettingsBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This test case verify that the jsessionid parameter is correctly removed from the URL
 */
public class JSessionIDTest extends TestCase {
    private HttpClient httpClient;
    private String jsessionid;

    @Before
    public void setUp() {
        httpClient = new HttpClient();
        jsessionid = "jsessionid";
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testJsessionIdExists() throws Exception {
        findJSessionId(false);

    }

    @Test
    public void testJsessionIdRemoved() throws Exception {
        findJSessionId(true);
    }

    private void findJSessionId(boolean removeJsessionId) throws IOException {
        SettingsBean.getInstance().setDisableJsessionIdParameter(removeJsessionId);
        SettingsBean.getInstance().setJsessionIdParameterName(jsessionid);

        GetMethod displayLoginMethod = new GetMethod("http://localhost:8080"+ Jahia.getContextPath()+"/administration");
        httpClient.executeMethod(displayLoginMethod);
        String responseBodyAsString = displayLoginMethod.getResponseBodyAsString();

        Pattern p = Pattern.compile("action=\"([^\"]*)\"");
        Matcher m = p.matcher(responseBodyAsString);
        assertTrue (m.find());

        String url = m.group(1);

        assertEquals("jsession ID is not "+(removeJsessionId?"removed":"present")+" in administration login url.", removeJsessionId, !url.contains("jsessionid"));

        p = Pattern.compile("name=\"redirect\" value=\"([^\"]*)\"");
        m = p.matcher(responseBodyAsString);
        assertTrue (m.find());

        String redirect = m.group(1);

        assertEquals("jsession ID is not "+(removeJsessionId?"removed":"present")+" in redirect url.", removeJsessionId, !redirect.contains("jsessionid"));
    }

}
