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

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.junit.*;

import com.google.common.collect.Sets;

import javax.jcr.PathNotFoundException;
import java.util.Locale;

/**
 * Regroups tests that test multi-lingual features of Jahia.
 *
 * @author loom
 *         Date: Jan 27, 2010
 *         Time: 2:16:51 PM
 */
public class MultiLanguageTest extends TestCase {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(MultiLanguageTest.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "jcrMultiLanguageTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    @Before
    public void setUp() throws Exception {
        site = TestHelper.createSite(TESTSITE_NAME, Sets.newHashSet(Locale.ENGLISH.toString(), Locale.FRENCH.toString()), null, true);
        Assert.assertNotNull(site);
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        session.save();
    }

    @org.junit.Test
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
        frenchEditSession.logout();
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
        frenchLiveSession.logout();
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

    @After
    public void tearDown() throws Exception {
        TestHelper.deleteSite(TESTSITE_NAME);
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        session.save();
        session.logout();
    }


}
