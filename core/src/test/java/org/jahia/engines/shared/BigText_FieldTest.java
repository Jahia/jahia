/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.engines.shared;

import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;
import org.jahia.data.fields.JahiaBigTextField;
import org.jahia.params.ProcessingContext;

/**
 * JUnit based test
 *
 * @author Xavier Lawrence
 */
public class BigText_FieldTest extends TestCase {
    
    private static Logger logger = Logger.getLogger(BigText_FieldTest.class);
    private String content;
    
    public BigText_FieldTest(String testName) {
        super(testName);
    }
    
    /** Test data to "copy-paste" in the engine:
        hello ### ### ###### <br/>
        <a href='2'>the link (1)</a> <br />
        <a href='2'> the link (2)</a> <br/>
        <a href='2' >the link (3)</a> <br/>
        <a href='2' > the link (4) </a> <br/>
        <a href='pid/2'>the link (5)</a> <br/>
        <a href='/pid/2'>the link (6)</a> <br/>
        <a href='lang/en/pid/2'>the link (7)</a> <br/>
        <a href='/lang/en/pid/2'>the link (8)</a> <br/>
        <a href='/site/mySite/pid/2'>the link (9)</a> <br/>
        <a href='site/mySite/lang/en/pid/2'>the link (10)</a> <br/>
        <a href='/site/mySite/lang/en/pid/2'>the link (11)</a> <br/>
        <a href='jahia/Jahia/site/mySite/lang/fr/pid/2'>the link (12)</a> <br/>
        <a href='/jahia/Jahia/site/mySite/lang/fr/pid/2'>the link (13)</a> <br/>
        <a href='http://localhost:8080/jahia/Jahia/site/mySite/lang/en/pid/2'>the link (14)</a><br/>
        <a href='http://localhost:8080/jahia/Jahia/site/mySite/lang/en/pid/2'>the link (15)</a><br/>
        <a href='/jahia/Jahia/site/mySite/lang/en/op/edit/pid/2'>the link (16)</a><br/>
        <a href='/jahia/Jahia/site/mySite/lang/en/op/edit/pid/2?matrix=1116589713765'>the link (17)</a><br/>
        <a href='/jahia/Jahia/site/mySite/lang/en/op/edit/pid/2;jsessionid=1234546754756'>the link (18)</a><br/>
        <a href='/jahia/Jahia/pid/2?entryid=34?matrix=1116589713765' target='_blank'>the link (19)</a><br/>
        <a href='ftp://ftp.jahia.org/logo.gif'>the link (20)</a><br/>
        Link a) <a href="/jahia/Jahia/myKey?vf_partnerUrl=http%3A%2F%2Fgoogle.de">Games by Name</a><br />
        Link b) <a href="/jahia/Jahia/pid/2?vf_partnerUrl=http%3A%2F%2Fgoogle.de">Games by ID</a><br />
        <a href='http://www.jahia.org/javadoc'>the link (21)</a><br/>
        <img height='123' src='/webdav/root/img.gif' width='123' />  <br/>
        <img height='123' src="/webdav/root/img.gif" width="123" />  <br/>
        <img height='123' src='/jahia/webdav/root/img.gif' width='123' />   <br/>
        <img height='123' src="/jahia/webdav/root/img.gif" width="123" />  <br/>
        <a href='http://www.epfl.ch/jahia/Jahia/op/edit/pid/23?matrix=1116589713765'>the link (22)</a>    <br/>
        <a href='3'>the link (23)</a> <br/>
        <a href='site/mySite/myKey'>the link (24)</a> <br/>
        <a href='jahia/Jahia/site/mySite/myKey'>the link (25)</a> <br/>
        <a href='/myKey'>the link (26)</a> <br/>
        <a href='/site/mySite/myKey'>the link (27)</a> <br/>
        <a href='/jahia/Jahia/site/mySite/myKey'>the link (28)</a> <br/>
        <a href='http://localhost:8080/jahia/Jahia/site/mySite/op/edit/myKey'>the link (29)</a> <br/>
        <a href=\"http://localhost:8080/jahia/Jahia/site/mySite/op/edit/myKey\">the link (30)</a> <br/>
     */
    protected void setUp() throws Exception {
        content = "\n" +
                "<a href='2'>the link (1)</a>\n" +
                "<a href='2'> the link (2)</a>\n" +
                "<a href='2' >the link (3)</a>\n" +
                "<a href='2' > the link (4) </a>\n" +
                "<a href='pid/2'>the link (5)</a>\n" +
                "<a href='/pid/2'>the link (6)</a>\n" +
                "<a href='lang/en/pid/2'>the link (7)</a>\n" +
                "<a href='/lang/en/pid/2'>the link (8)</a>\n" +
                "<a href='/site/mySite/pid/2'>the link (9)</a>\n" +
                "<a href='site/mySite/lang/en/pid/2'>the link (10)</a>\n" +
                "<a href='/site/mySite/lang/en/pid/2'>the link (11)</a>\n" +
                "<a href='jahia/Jahia/site/mySite/lang/fr/pid/2'>the link (12)</a>\n" +
                "<a href='/jahia/Jahia/site/mySite/lang/fr/pid/2'>the link (13)</a>\n" +
                "<a href='http://localhost:8080/jahia/Jahia/site/mySite/lang/en/pid/2'>the link (14)</a>\n" +
                "<a href='http://localhost:8080/jahia/Jahia/site/mySite/lang/en/pid/2'>the link (15)</a>\n" +
                "<a href='/jahia/Jahia/site/mySite/lang/en/op/edit/pid/2'>the link (16)</a>\n" +
                "<a href='/jahia/Jahia/site/mySite/lang/en/op/edit/pid/2?matrix=1116589713765'>the link (17)</a>\n" +
                "<a href='/jahia/Jahia/site/mySite/lang/en/op/edit/pid/2;jsessionid=1234546754756'>the link (18)</a>\n" +
                "<a href='/jahia/Jahia/pid/2?entryid=34?matrix=1116589713765' target=\"_blank\">the link (19)</a>\n" +
                "<a href='ftp://ftp.jahia.org/logo.gif'>the link (20)</a>\n" +
                "<a href='http://www.jahia.org/javadoc'>the link (21)</a>\n" +
                "<img height='123' src='/webdav/root/img.gif' width='123'/>\n" +
                "<img height='123' src=\"/webdav/root/img.gif\" width='123' />\n" +
                "<img height='123' src='/jahia/webdav/root/img.gif' width='123'/>\n" +
                "<img height='123' src=\"/jahia/webdav/root/img.gif\" width='123' />\n" +
                "<a href='http://www.epfl.ch/jahia/Jahia/op/edit/pid/23?matrix=1116589713765'>the link (22)</a>\n";
    }
    
    public static Test suite() {
        final TestSuite suite = new TestSuite(BigText_FieldTest.class);
        return suite;
    }
    
    /**
     * Test of cleanUpHardCodedLinks method, of class org.jahia.data.fields.JahiaBigTextField.
     */
    public void testCleanUpHardCodedLinks() throws Exception {
        logger.info("testCleanUpHardCodedLinks");
        
        try {
            //HttpServletRequest request = new HttpServletRequestWrapper(
            //        new HttpServletRequestImpl());
            final ProcessingContext processingContext = new ProcessingContext();
            processingContext.setContextPath("/jahia");
            processingContext.setServletPath("/Jahia");
            processingContext.setServerName("localhost");
            processingContext.setServerPort(8080);
            processingContext.setScheme("http");
            processingContext.setOpMode(ProcessingContext.EDIT);

            final Integer id = new Integer(1);
                        
            final JahiaBigTextField field = new JahiaBigTextField( id,
                    id,
                    id,
                    id,
                    id,
                    id,
                    id,
                    "",
                    id,
                    id,
                    id,
                    id,
                    "en");
            
            String res = field.cleanUpHardCodedLinks(content, processingContext, Locale.getDefault(), "");
            
            logger.info("RawValue: "+res);
            logger.info("InternalLinks: "+field.getInternalLinks());

            final String rewritten = JahiaBigTextField.rewriteURLs(res, processingContext);
            logger.info("rewritten: " + rewritten);
            
            assertTrue(res.indexOf("?matrix") < 0 &&
                    res.indexOf(";jsessionid") < 0 &&
                    res.indexOf("/op/") < 0 &&
                    res.indexOf("/site/") < 0 &&
                    res.indexOf("/lang/") > -1 &&
                    res.indexOf(HttpServletRequestImpl.SERVER_NAME) < 0);                        
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     *
     */
    class HttpServletRequestImpl implements HttpServletRequest {
        
        public static final String SERVER_NAME = "localhost";
        public static final String LOCAL_ADDR = "192.168.2.197";
        
        public String getAuthType() { return ""; }
        
        public Cookie[] getCookies() { return null; }
        
        public StringBuffer getRequestURL() { return null; }
        
        public java.security.Principal getUserPrincipal() { return null; }
        
        public long getDateHeader(String name){ return 0; }
        
        public boolean isUserInRole(String name) { return false; }
        
        public String getContextPath() { return "/jahia"; }
        
        public String getHeader(String name){ return ""; }
        
        public Enumeration getHeaders(String name) { return null; }
        
        public Enumeration getHeaderNames() { return null; }
        
        public int getIntHeader(String name) { return -1; }
        
        public String getMethod() { return ""; }
        
        public String getPathInfo() { return ""; }
        
        public String getPathTranslated() { return ""; }
        
        public String getQueryString() { return ""; }
        
        public String getRemoteUser() { return ""; }
        
        public String getRequestedSessionId() { return ""; }
        
        public String getRequestURI() { return ""; }
        
        public String getServletPath() { return "/Jahia"; }
        
        public HttpSession getSession(boolean create) { return null; }
        
        public HttpSession getSession() { return null; }
        
        public boolean isRequestedSessionIdValid() { return false; }
        
        public boolean isRequestedSessionIdFromCookie() { return false; }
        
        public boolean isRequestedSessionIdFromURL() { return false; }
        
        public boolean isRequestedSessionIdFromUrl() { return false; }
        
        public Object getAttribute(String name) { return ""; }
        
        public Enumeration getAttributeNames() { return null; }
        
        public String getCharacterEncoding() { return ""; }
        
        public void setCharacterEncoding(String env)
        throws java.io.UnsupportedEncodingException {}
        
        public int getContentLength() { return 0; }
        
        public String getContentType() { return ""; }
        
        public ServletInputStream getInputStream() throws java.io.IOException { return null; }
        
        public String getParameter(String name) { return ""; }
        
        public Enumeration getParameterNames() { return null; }
        
        public String[] getParameterValues(String name) { return null; }
        
        public java.util.Map getParameterMap() { return null; }
        
        public String getProtocol() { return "HTTP/1.1"; }
        
        public String getScheme() { return "http"; }
        
        public String getServerName() { return SERVER_NAME; }
        
        public int getServerPort() { return 8080; }
        
        public java.io.BufferedReader getReader() throws java.io.IOException { return null; }
        
        public String getRemoteAddr() { return ""; }
        
        public String getRemoteHost() { return ""; }
        
        public void setAttribute(String name, Object o) {}
        
        public void removeAttribute(String name) {}
        
        public Locale getLocale() { return new Locale("en"); }
        
        public Enumeration getLocales() { return null; }
        
        public boolean isSecure() { return false; }
        
        public RequestDispatcher getRequestDispatcher(String path) { return null; }
        
        public String getRealPath(String path) { return ""; }
        
        public int getRemotePort() { return 1234; }
        
        public String getLocalName() { return ""; }
        
        public String getLocalAddr() { return LOCAL_ADDR; }
        
        public int getLocalPort() { return 5678; }
    }
}
