/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.test.services.content;

import static org.junit.Assert.*;

import org.jahia.services.content.decorator.JCRSiteNode;
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.junit.*;

import com.google.common.collect.Sets;

import javax.jcr.PathNotFoundException;

import java.io.IOException;
import java.util.Locale;

/**
 * Regroups tests that test multi-lingual features of Jahia.
 *
 * @author loom
 *         Date: Jan 27, 2010
 *         Time: 2:16:51 PM
 */
public class MultiLanguageTest extends JahiaTestCase {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(MultiLanguageTest.class);
    private final static String TESTSITE_NAME = "jcrMultiLanguageTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    private boolean isTextPresentInResponse(String relativeUrl, String text) throws IOException {
        return getAsText(relativeUrl).contains(text);
    }

    @BeforeClass
    public static void oneTimeSetup() throws Exception {
        JahiaSite site = TestHelper.createSite(TESTSITE_NAME, Sets.newHashSet(Locale.ENGLISH.toString(), Locale.FRENCH.toString()), null, true);
        Assert.assertNotNull(site);
    }

    //@Test
    @Test
    public void testFallBackLanguage() throws Exception {
        JCRPublicationService jcrService = ServicesRegistry.getInstance()
                .getJCRPublicationService();
        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(TESTSITE_NAME);
        site = ((JCRSiteNode)JCRSessionFactory.getInstance().getCurrentUserSession().getNode(site.getJCRLocalPath()));

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
        site = ((JCRSiteNode)JCRSessionFactory.getInstance().getCurrentUserSession().getNode(site.getJCRLocalPath()));

        site.setMixLanguagesActive(false);
        ServicesRegistry.getInstance().getJahiaSitesService().updateSystemSitePermissions(site);

        frenchEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.FRENCH);
        frenchTextNode = frenchEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/text1");
        frenchTextPropertyValue = frenchTextNode.getPropertyAsString("text");
        assertNull("English text node should not be available in edit workspace when mixed language is de-activated.", frenchTextPropertyValue);

        site.setMixLanguagesActive(true);
        ServicesRegistry.getInstance().getJahiaSitesService().updateSystemSitePermissions(site);

        // Now let's do the checks on the live sessions

        JCRSessionWrapper frenchLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.FRENCH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRNodeWrapper frenchLiveTextNode = frenchLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/text1");
        String frenchLiveTextPropertyValue = frenchLiveTextNode.getPropertyAsString("text");

        assertEquals("English text node should be available in live workspace when mixed language is activated.", "English text", frenchLiveTextPropertyValue);

        // now let's test without mix language active.
        JCRSessionFactory.getInstance().closeAllSessions();
        site = ((JCRSiteNode)JCRSessionFactory.getInstance().getCurrentUserSession().getNode(site.getJCRLocalPath()));

        site.setMixLanguagesActive(false);
        ServicesRegistry.getInstance().getJahiaSitesService().updateSystemSitePermissions(site);

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

        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(TESTSITE_NAME);
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
        frenchEditSession.getNode(SITECONTENT_ROOT_NODE + "/home").setProperty("jcr:title","page");
        frenchEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/listA/textInvalidLanguage").setProperty("text", frenchText);

        frenchEditSession.save();

        jcrService.publishByMainId(stageRootNode.getIdentifier());

        JCRNodeWrapper englishLiveSessionNode = englishLiveSession.getNode(textNode1.getPath());
        String string = englishLiveSessionNode.getProperty("text").getValue().getString();
        assertEquals(string,englishText);

        assertEquals(frenchText, sf.getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.FRENCH, defLocale).getNode(SITECONTENT_ROOT_NODE + "/home/listA/textInvalidLanguage").getProperty("text").getValue().getString());

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


    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite(TESTSITE_NAME);
    }


}
