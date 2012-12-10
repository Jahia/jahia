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

package org.jahia.services.content;

import java.io.IOException;
import java.util.Locale;

import javax.jcr.PathNotFoundException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.drools.util.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import com.google.common.collect.Sets;

/**
 * Regroups tests that test multi-lingual features of Jahia.
 *
 * @author loom
 *         Date: Jan 27, 2010
 *         Time: 2:16:51 PM
 */
public class MultiLanguageTest extends JahiaTestCase {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(MultiLanguageTest.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "jcrMultiLanguageTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private HttpClient client;

    private boolean isTextPresentInResponse(String relativeUrl, String text) {
        String body = StringUtils.EMPTY;
        GetMethod getMethod = new GetMethod(getBaseServerURL() + Jahia.getContextPath()
                + relativeUrl);
        try {
            int responseCode = client.executeMethod(getMethod);
            assertEquals("Response code is not OK: " + responseCode, 200, responseCode);
            body = getMethod.getResponseBodyAsString();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            getMethod.releaseConnection();
        }
        
        return body.contains(text);
    }
    
    @Before
    public void setUp() throws Exception {
        site = TestHelper.createSite(TESTSITE_NAME, Sets.newHashSet(Locale.ENGLISH.toString(), Locale.FRENCH.toString()), null, true);
        Assert.assertNotNull(site);
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        session.save();
    }

    @Test
    public void testFallBackLanguage() throws Exception {
        JCRPublicationService jcrService = ServicesRegistry.getInstance()
                .getJCRPublicationService();

        String defaultLanguage = site.getDefaultLanguage();

        JCRSessionWrapper englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRSessionWrapper englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRNodeWrapper stageRootNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE);
        englishLiveSession.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper stageNode = (JCRNodeWrapper) stageRootNode.getNode("home");

        JCRNodeWrapper textNode1 = stageNode.addNode("text1", "jnt:text");
        textNode1.setProperty("text", "English text");

        englishEditSession.save();

        jcrService.publishByMainId(stageNode.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, Sets.newHashSet(Locale.ENGLISH.toString(), Locale.FRENCH.toString()),
                false, null);

        JCRSessionWrapper frenchEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.FRENCH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRNodeWrapper frenchTextNode = frenchEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/text1");
        String frenchTextPropertyValue = frenchTextNode.getProperty("text").getValue().getString();

        assertEquals("English text node should be available in edit workspace when mixed language is activated.", "English text", frenchTextPropertyValue);

        // now let's test without mix language active.
        JCRSessionFactory.getInstance().closeAllSessions();
        site.setMixLanguagesActive(false);
        ServicesRegistry.getInstance().getJahiaSitesService().updateSite(site);

        frenchEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.FRENCH);
        frenchTextNode = frenchEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/text1");
        frenchTextPropertyValue = frenchTextNode.getPropertyAsString("text");
        assertNull("English text node should not be available in edit workspace when mixed language is de-activated.", frenchTextPropertyValue);

        site.setMixLanguagesActive(true);
        ServicesRegistry.getInstance().getJahiaSitesService().updateSite(site);

        // Now let's do the checks on the live sessions

        JCRSessionWrapper frenchLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.FRENCH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRNodeWrapper frenchLiveTextNode = frenchLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/text1");
        String frenchLiveTextPropertyValue = frenchLiveTextNode.getPropertyAsString("text");

        assertEquals("English text node should be available in live workspace when mixed language is activated.", "English text", frenchLiveTextPropertyValue);

        // now let's test without mix language active.
        JCRSessionFactory.getInstance().closeAllSessions();
        site.setMixLanguagesActive(false);
        ServicesRegistry.getInstance().getJahiaSitesService().updateSite(site);

        frenchLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.FRENCH);
        try {
            frenchLiveTextNode = frenchLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/text1");
            assertTrue("English text node should not be available in live workspace when mixed language is not activated.",frenchLiveTextNode!=null);
        } catch (PathNotFoundException e) {
            logger.info("This exception was expected as English text node should not be available in live workspace when mixed language is not activated.",e);
        }

    }

    @Test
    public void testLanguageInvalidity() throws Exception {
        JCRPublicationService jcrService = ServicesRegistry.getInstance()
                .getJCRPublicationService();

        String defaultLanguage = site.getDefaultLanguage();

        JCRSessionFactory sf = jcrService.getSessionFactory();
        Locale defLocale = LanguageCodeConverters.languageCodeToLocale(defaultLanguage);
        JCRSessionWrapper englishEditSession = sf.getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, defLocale);
        JCRSessionWrapper englishLiveSession = sf.getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH, defLocale);
        JCRNodeWrapper stageRootNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE);
        englishLiveSession.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper stageNode = (JCRNodeWrapper) stageRootNode.getNode("home").getNode("listA");

        JCRNodeWrapper textNode1 = stageNode.addNode("textInvalidLanguage", "jnt:text");
        final String englishText = "English text";
        textNode1.setProperty("text", englishText);

        englishEditSession.save();
        
        JCRSessionWrapper frenchEditSession = sf.getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.FRENCH, defLocale);
        
        final String frenchText = "French text";
        frenchEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/listA/textInvalidLanguage").setProperty("text", frenchText);
        
        frenchEditSession.save();

        jcrService.publishByMainId(stageRootNode.getIdentifier());

        JCRNodeWrapper englishLiveSessionNode = englishLiveSession.getNode(textNode1.getPath());
        String string = englishLiveSessionNode.getProperty("text").getValue().getString();
        assertEquals(string,englishText);

        assertEquals(frenchText, sf.getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.FRENCH, defLocale).getNode(SITECONTENT_ROOT_NODE + "/home/listA/textInvalidLanguage").getProperty("text").getValue().getString());
        
        client = new HttpClient();

        // Validate that EN and FR content is visible in live
        String url = "/cms/render/live/en/sites/" + TESTSITE_NAME + "/home.html";
        assertTrue("Not found expected value (" + englishText + ") in response body for url: " + url, isTextPresentInResponse(url, englishText));
        // and once again for the cached page
        assertTrue("Not found expected value (" + englishText + ") in response body for url: " + url, isTextPresentInResponse(url, englishText));
        url = "/cms/render/live/fr/sites/" + TESTSITE_NAME + "/home.html";
        assertTrue("Not found expected value (" + frenchText + ") in response body for url: " + url, isTextPresentInResponse(url, frenchText));
        // and once again for the cached page
        assertTrue("Not found expected value (" + frenchText + ") in response body for url: " + url, isTextPresentInResponse(url, frenchText));
        
        // deactivate FR 
        textNode1.setProperty("j:invalidLanguages", new String[] { "fr" });
        englishEditSession.save();
        
        // publish inactivation
        jcrService.publishByMainId(stageRootNode.getIdentifier());

        JCRSessionFactory.getInstance().closeAllSessions();
        assertEquals(englishText, sf.getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH, defLocale).getNode(SITECONTENT_ROOT_NODE + "/home/listA/textInvalidLanguage").getProperty("text").getString());
        assertFalse("French node should not be available in live",  sf.getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.FRENCH, defLocale).nodeExists(SITECONTENT_ROOT_NODE + "/home/listA/textInvalidLanguage"));
        
        // Validate that EN content is still visible in live
        url = "/cms/render/live/en/sites/" + TESTSITE_NAME + "/home.html";
        assertTrue("Not found expected value (" + englishText + ") in response body for url: " + url, isTextPresentInResponse(url, englishText));
        // and once again for the cached page
        assertTrue("Not found expected value (" + englishText + ") in response body for url: " + url, isTextPresentInResponse(url, englishText));

        // Validate that FR content is NOT visible in live
        url = "/cms/render/live/fr/sites/" + TESTSITE_NAME + "/home.html";
        assertFalse("Found unexpected value (" + frenchText + ") in response body for url: " + url, isTextPresentInResponse(url, frenchText));
        // and once again for the cached page
        assertFalse("Found unexpected value (" + frenchText + ") in response body for url: " + url, isTextPresentInResponse(url, frenchText));

        englishEditSession = sf.getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, defLocale);
        // activate FR 
        englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/listA/textInvalidLanguage").getProperty("j:invalidLanguages").remove();
        englishEditSession.save();
        
        // publish activation
        jcrService.publishByMainId(stageRootNode.getIdentifier());

        JCRSessionFactory.getInstance().closeAllSessions();
        assertEquals(englishText, sf.getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH, defLocale).getNode(SITECONTENT_ROOT_NODE + "/home/listA/textInvalidLanguage").getProperty("text").getString());
        assertEquals(frenchText, sf.getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.FRENCH, defLocale).getNode(SITECONTENT_ROOT_NODE + "/home/listA/textInvalidLanguage").getProperty("text").getString());
        
        // Validate that EN and FR are visible in live
        url = "/cms/render/live/en/sites/" + TESTSITE_NAME + "/home.html";
        assertTrue("Not found expected value (" + englishText + ") in response body for url: " + url, isTextPresentInResponse(url, englishText));
        // and once again for the cached page
        assertTrue("Not found expected value (" + englishText + ") in response body for url: " + url, isTextPresentInResponse(url, englishText));
        url = "/cms/render/live/fr/sites/" + TESTSITE_NAME + "/home.html";
        assertTrue("Not found expected value (" + frenchText + ") in response body for url: " + url, isTextPresentInResponse(url, frenchText));
        // and once again for the cached page
        assertTrue("Not found expected value (" + frenchText + ") in response body for url: " + url, isTextPresentInResponse(url, frenchText));
    }

    @After
    public void tearDown() throws Exception {
        TestHelper.deleteSite(TESTSITE_NAME);
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        session.save();
        JCRSessionFactory.getInstance().closeAllSessions();
    }


}
