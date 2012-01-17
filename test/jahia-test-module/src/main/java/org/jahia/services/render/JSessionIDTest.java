/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

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
//        GetMethod displayLoginMethod = new GetMethod("http://localhost:8080"+ Jahia.getContextPath()+"/administration");
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
//        PostMethod login = new PostMethod("http://localhost:8080" + url);
//        login.setParameter("username","root");
//        login.setParameter("password","root1234");
//        login.setParameter("doLogin","true");
//        login.setParameter("redirect",redirect);
//        result = httpClient.executeMethod(login);
//
//        String sessionid = StringUtils.substringAfter(login.getURI().toString(), ";"+jsessionid);
//        assertNotNull("Session id is not in the url !",sessionid);
//
//        GetMethod home = new GetMethod("http://localhost:8080"+ Jahia.getContextPath()+"/cms/render/live/en/users/root.user-home.html;"+jsessionid+sessionid);
//        result = httpClient.executeMethod(home);
//        System.out.println(home.getResponseBodyAsString());
//        System.out.println("ok");
//    }

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
    }

}
