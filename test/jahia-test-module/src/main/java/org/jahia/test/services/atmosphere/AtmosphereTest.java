/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.test.services.atmosphere;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.Response;

import org.apache.commons.httpclient.HttpStatus;
import org.atmosphere.cpr.HeaderConfig;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * @author rincevent
 * @since JAHIA 6.7
 *        Created : 01/10/12
 */
public class AtmosphereTest extends JahiaTestCase {
    private transient static Logger logger = LoggerFactory.getLogger(AtmosphereTest.class);
    private String urlTarget = getBaseServerURL() + Jahia.getContextPath()+"/atmosphere/alert/testChannel";

    @Test
    public void testHeaderBroadcasterCache() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        logger.info("{}: running test: testHeaderBroadcasterCache", getClass().getSimpleName());

        final CountDownLatch latch = new CountDownLatch(1);
        long t1 = System.currentTimeMillis();
        Builder builder = new Builder();
        builder.setConnectionTimeoutInMs(Integer.MAX_VALUE);
        builder.setRequestTimeoutInMs(Integer.MAX_VALUE);        
        AsyncHttpClient c = new AsyncHttpClient(builder.build());
        try {
            // Suspend
            c.preparePost(urlTarget).addParameter("message", "cacheme").execute().get();

            // Broadcast
            c.preparePost(urlTarget).addParameter("message", "cachememe").execute().get();

            //Suspend
            Response r = c.prepareGet(urlTarget).addHeader(HeaderConfig.X_CACHE_DATE, String.valueOf(t1)).execute(new AsyncCompletionHandler<Response>() {

                @Override
                public Response onCompleted(Response r) throws Exception {
                    try {
                        return r;
                    } finally {
                        latch.countDown();
                    }
                }
            }).get();

            try {
                latch.await(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                fail(e.getMessage());
            }

            assertNotNull(r);
            assertEquals(HttpStatus.SC_OK, r.getStatusCode());
            assertEquals(r.getResponseBody(), "cacheme\ncachememe\n");
        } catch (Exception e) {
            logger.error("test failed", e);
            fail(e.getMessage());
        }

        c.close();
    }


    private static JahiaSite site;
    private static JCRPublicationService jcrService;

    private final static String TESTSITE_NAME = "jcrAtmosphereTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private final static String INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE = "English text";

    private JCRSessionWrapper englishEditSession;
    private JCRSessionWrapper englishLiveSession;

    private JCRNodeWrapper testHomeEdit;

    private void getCleanSession() throws RepositoryException {
        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        sessionFactory.closeAllSessions();
        englishEditSession = sessionFactory.getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        englishLiveSession = sessionFactory.getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);
    }

    @BeforeClass
    public static void setUpClass() {
        try {
            site = TestHelper.createSite(TESTSITE_NAME, new HashSet<String>(Arrays.asList("en", "fr")),
                    Collections.singleton("en"), false);
            assertNotNull(site);

            jcrService = ServicesRegistry.getInstance()
                    .getJCRPublicationService();

        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }
    }

    @AfterClass
    public static void tearDownClass() {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Before
    public void setUp() {
        try {
            getCleanSession();
            JCRNodeWrapper englishEditSiteHomeNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home");
            testHomeEdit = englishEditSiteHomeNode.addNode("test"+System.currentTimeMillis(), "jnt:page");
            testHomeEdit.setProperty("jcr:title", "Test page");
            testHomeEdit.setProperty("j:templateName", "simple");            
            englishEditSession.save();
            jcrService.publishByMainId(testHomeEdit.getIdentifier());
        } catch (RepositoryException e) {
            fail("Cannot setup test" + e.getMessage());
        }
    }

    @After
    public void tearDown() {
        try {
            getCleanSession();
            englishEditSession.getNodeByIdentifier(testHomeEdit.getIdentifier()).remove();
            englishEditSession.save();
            englishLiveSession.getNodeByIdentifier(testHomeEdit.getIdentifier()).remove();
            englishLiveSession.save();
        } catch (RepositoryException e) {
            fail("Cannot remove nodes");
        }
    }

    @Test
    public void testNodePublish() throws RepositoryException {
        TestHelper.createList(testHomeEdit, "contentList1", 4, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        final Value[] values = testHomeEdit.getResolveSite().getProperty("j:installedModules").getValues();
        Value[] values1 = new Value[values.length+1];
        System.arraycopy(values,0,values1,0,values.length);
        values1[values.length] = JCRValueFactoryImpl.getInstance().createValue("atmosphere:6.7.0.0-SNAPSHOT");
        testHomeEdit.getResolveSite().setProperty("j:installedModules",values1);
        englishEditSession.save();
        final CountDownLatch latch = new CountDownLatch(1);
        AsyncHttpClient c = new AsyncHttpClient();
        try {

            //Suspend
            final AtomicReference<Response> response = new AtomicReference<Response>();
            c.prepareGet(getBaseServerURL() + Jahia.getContextPath()+"/atmosphere/sites/jcrAtmosphereTest-en").execute(new AsyncCompletionHandler<Response>() {

                @Override
                public Response onCompleted(Response r) throws Exception {
                    try {
                        response.set(r);
                        return r;
                    } finally {
                        latch.countDown();
                    }
                }
            });

            Thread.sleep(2500);

            jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);
            try {
                latch.await(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                fail(e.getMessage());
            }
            Response r = response.get();
            assertNotNull(r);
            assertEquals(HttpStatus.SC_OK, r.getStatusCode());
            assertTrue(r.getResponseBody().contains("\"name\":\"contentList1_text0\""));
        } catch (Exception e) {
            logger.error("test failed", e);
            fail(e.getMessage());
        }
        c.close();
    }
}
