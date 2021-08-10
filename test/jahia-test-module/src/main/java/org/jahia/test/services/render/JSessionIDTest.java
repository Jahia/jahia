/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.test.services.render;

import org.apache.commons.lang.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.cookie.IgnoreCookieSpecFactory;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jahia.bin.Jahia;
import org.jahia.settings.SettingsBean;
import org.jahia.test.JahiaTestCase;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This test case verify that the jsessionid parameter is correctly removed from the URL
 */
public class JSessionIDTest extends JahiaTestCase {
    private static Logger logger = LoggerFactory.getLogger(JSessionIDTest.class);
    private static boolean isJsessionIdActive;
    private CloseableHttpClient httpClient;
    private String jsessionid;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        isJsessionIdActive = SettingsBean.getInstance().isDisableJsessionIdParameter();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        SettingsBean.getInstance().setDisableJsessionIdParameter(isJsessionIdActive);
    }

    @Before
    public void setUp() {
        httpClient = HttpClients.custom().setDefaultCookieSpecRegistry(name -> new IgnoreCookieSpecFactory()).build();
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

        HttpGet displayLoginMethod = new HttpGet(getBaseServerURL() + Jahia.getContextPath() + "/start");
        try (CloseableHttpResponse httpResponse = httpClient.execute(displayLoginMethod)) {
            assertEquals("Method failed: " + httpResponse.getCode(), HttpStatus.SC_UNAUTHORIZED, httpResponse.getCode());

            String responseBodyAsString = EntityUtils.toString(httpResponse.getEntity());

            Pattern p = Pattern.compile("action=\"([^\"]*)\"");
            Matcher m = p.matcher(responseBodyAsString);
            assertTrue(m.find());

            String url = m.group(1);
            if (!removeJsessionId && logger.isInfoEnabled()) {
                String unencodedUrl = getBaseServerURL() + Jahia.getContextPath() + "/start";
                logger.info("Unencoded URL: {}", unencodedUrl);
                logger.info("Encoded redirect URL: {}", getResponse().encodeRedirectURL(unencodedUrl));
                logger.info("Encoded URL: {}", getResponse().encodeURL(unencodedUrl));
            }

            assertEquals("jsession ID is not " + (removeJsessionId ? "removed" : "present") + " in administration login url:" + url, removeJsessionId,
                    !StringUtils.containsIgnoreCase(url, jsessionid));
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

}
