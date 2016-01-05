/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.notification;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.notification.HtmlExternalizationService;
import org.jahia.test.JahiaTestCase;
import org.junit.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the HTML externalization service.
 * 
 * @author Sergiy Shyrkov
 */
public class HtmlExternalizationServiceTest extends JahiaTestCase {

    private static HtmlExternalizationService service;
    
    private String source;
    private String serverUrl;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        service = (HtmlExternalizationService) SpringContextSingleton.getBean("HtmlExternalizationService");
        assertNotNull("HtmlExternalizationService cannot be retrieved", service);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        service = null;
    }

    @Before
    public void setUp() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("externalize.html");
        assertNotNull("Unable to lookup the externalize.html resource", is);
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer);
        IOUtils.closeQuietly(is);
        source = writer.toString();
        assertTrue("Resource cannot be read or is empty", StringUtils.isNotEmpty(source));
        serverUrl = "http://www.jahia-test.org:"+ getBaseServerURLPort() + Jahia.getContextPath();
    }

    @After
    public void tearDown() {
        source = null;
    }

    @Test
    public void testUrls() throws Exception {
        
        String output = service.externalize(source, serverUrl);
        
        assertTrue("Output is empty", StringUtils.isNotEmpty(output));
        
        Source src = new Source(output);

        // check URLs
        List<StartTag> linkStartTags = src.getAllStartTags(HTMLElementName.A);
        for (StartTag startTag : linkStartTags) {
            String href = startTag.getAttributeValue("href");
            assertTrue("The URL was not rewritten correctly: " + href, href == null || !href.startsWith("/"));
        }
    }

    @Test
    public void testCss() throws Exception {

        String output = service.externalize(source, serverUrl);
        
        assertTrue("Output is empty", StringUtils.isNotEmpty(output));
        
        Source src = new Source(output);

        // check CSS
        List<StartTag> cssStartTags = src.getAllStartTags(HTMLElementName.LINK);
        for (StartTag startTag : cssStartTags) {
            String rel = startTag.getAttributeValue("rel");
            assertTrue("CSS was not inlined correctly " + startTag.getAttributeValue("href"), rel == null || !"stylesheet".equalsIgnoreCase(rel));
        }
    }

    @Test
    public void testJavaScript() throws Exception {
        
        String output = service.externalize(source, serverUrl);
        
        assertTrue("Output is empty", StringUtils.isNotEmpty(output));
        
        Source src = new Source(output);

        // check JavaScript
        List<Element> scriptTags = src.getAllElements(HTMLElementName.SCRIPT);
        assertTrue("Not all script tags were removed. " + scriptTags.size() + " tags remain.", scriptTags.isEmpty());
        
    }

    @Test
    public void testCssUrls() throws Exception {
        
        String output = service.externalize(source, serverUrl);
        
        assertTrue("Output is empty", StringUtils.isNotEmpty(output));
        
        // check CSS URLs
        assertTrue("CSS URLs were not rewritten correctly", output.contains("background: url(\""+serverUrl+"/css/images"));
    }
}
