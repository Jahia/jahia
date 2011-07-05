package org.jahia.services.render;

import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
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

//    public void testJsessionIdWithCache() throws Exception {
//        SettingsBean.getInstance().setDisableJsessionIdParameter(false);
//        SettingsBean.getInstance().setJsessionIdParameterName(jsessionid);
//        httpClient.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
//
//        GetMethod displayLoginMethod = new GetMethod("http://localhost:8085"+ Jahia.getContextPath()+"/administration");
//        int result = httpClient.executeMethod(displayLoginMethod);
//        String responseBodyAsString = displayLoginMethod.getResponseBodyAsString();
//
//        Pattern p = Pattern.compile("action=\"([^\"]*)\"");
//        Matcher m = p.matcher(responseBodyAsString);
//        assertTrue (m.find());
//
//        String url = m.group(1);
//
//        p = Pattern.compile("name=\"redirect\" value=\"([^\"]*)\"");
//        m = p.matcher(responseBodyAsString);
//        assertTrue (m.find());
//
//        String redirect = m.group(1);
//        PostMethod login = new PostMethod("http://localhost:8085" + url);
//        login.setParameter("username","root");
//        login.setParameter("password","root1234");
//        login.setParameter("doLogin","true");
//        login.setParameter("redirect",redirect);
//        result = httpClient.executeMethod(login);
//
//        String sessionid = StringUtils.substringAfter(login.getURI().toString(), ";"+jsessionid);
//        assertNotNull("Session id is not in the url !",sessionid);
//
//        GetMethod home = new GetMethod("http://localhost:8085"+ Jahia.getContextPath()+"/cms/render/live/en/users/root.user-home.html;"+jsessionid+sessionid);
//        result = httpClient.executeMethod(home);
//        System.out.println(home.getResponseBodyAsString());
//        System.out.println("ok");
//    }

    private void findJSessionId(boolean removeJsessionId) throws IOException {
        SettingsBean.getInstance().setDisableJsessionIdParameter(removeJsessionId);
        SettingsBean.getInstance().setJsessionIdParameterName(jsessionid);

        GetMethod displayLoginMethod = new GetMethod("http://localhost:8085"+ Jahia.getContextPath()+"/administration");
        httpClient.executeMethod(displayLoginMethod);
        String responseBodyAsString = displayLoginMethod.getResponseBodyAsString();

        Pattern p = Pattern.compile("action=\"([^\"]*)\"");
        Matcher m = p.matcher(responseBodyAsString);
        assertTrue (m.find());

        String url = m.group(1);

        assertEquals("jsession ID is not "+(removeJsessionId?"removed":"present")+" in administration login url.", removeJsessionId, !url.contains("jsessionid"));
    }

}
