/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.remotepublish;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.modules.remotepublish.RemotePublicationService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Unit test for remote publishing
 */
public class SiteTest extends TestCase {
    private static Logger logger = Logger.getLogger(SiteTest.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "jcrRPTest";

    @Override
    protected void setUp() throws Exception {
        try {
            site = TestHelper.createSite(TESTSITE_NAME);
            assertNotNull(site);
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
            JCRSessionFactory.getInstance().closeAllSessions();
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }

    public void testImportExportOfSmallSite() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        final JCRNodeWrapper node = session.getNode("/sites/jcrRPTest/home");
        JCRNodeWrapper page1 = node.addNode("page1", "jnt:page");
        page1.setProperty("jcr:title","Page1");
        JCRNodeWrapper page2 = node.addNode("page2", "jnt:page");
        page2.setProperty("jcr:title","Page2");
        JCRNodeWrapper page3 = node.addNode("page3", "jnt:page");
        page3.setProperty("jcr:title","Page3");
        session.save();
        JCRPublicationService.getInstance().publish("/sites/jcrRPTest/home", Constants.EDIT_WORKSPACE,
                                                    Constants.LIVE_WORKSPACE, null, true);

        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE,
                                                                                              LanguageCodeConverters.languageCodeToLocale(
                                                                                                      site.getDefaultLanguage()));
        JCRNodeWrapper liveSite = liveSession.getNode("/sites/jcrRPTest");

        File tmp = File.createTempFile("remote", ".log.gz");

        RemotePublicationService.getInstance().generateLog(liveSite, null, new FileOutputStream(tmp));
        TestHelper.deleteSite("targetSite");
        TestHelper.createSite("targetSite");
        JCRPublicationService.getInstance().publish("/sites/targetSite/home", Constants.EDIT_WORKSPACE,
                                                    Constants.LIVE_WORKSPACE, null, true);
        RemotePublicationService.getInstance().replayLog(liveSession.getNode("/sites/targetSite"),
                                                         new FileInputStream(tmp));

        liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE,
                                                                                              LanguageCodeConverters.languageCodeToLocale(
                                                                                                      site.getDefaultLanguage()));

        assertNotNull("We should have found /sites/targetSite/home/page1",liveSession.getNode("/sites/targetSite/home/page1"));
        assertNotNull("We should have found /sites/targetSite/home/page2",liveSession.getNode("/sites/targetSite/home/page2"));
        assertNotNull("We should have found /sites/targetSite/home/page3",liveSession.getNode("/sites/targetSite/home/page3"));
    }
}