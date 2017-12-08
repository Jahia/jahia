/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.sites;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.jcr.RepositoryException;

import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test cases for the {@link JahiaSitesService}.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaSitesServiceTest {
    private static Logger logger = LoggerFactory.getLogger(JahiaSitesServiceTest.class);

    private static String originalDefaultSiteKey;

    private static final String SITE_A = "jahiaSitesServiceTestSiteA";

    private static final String SITE_B = "jahiaSitesServiceTestSiteB";

    private static final String SITE_C = "jahiaSitesServiceTestSiteC";

    private static JahiaSitesService siteService;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        siteService = JahiaSitesService.getInstance();
        // keep track of the original default site to restore it after the test
        originalDefaultSiteKey = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<String>() {
            @Override
            public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JahiaSite defSite = siteService.getDefaultSite(session);
                return defSite != null ? defSite.getSiteKey() : null;
            }
        });
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        if (originalDefaultSiteKey != null) {
            // restore original default site
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<String>() {
                @Override
                public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JahiaSite defSite = siteService.getSiteByKey(originalDefaultSiteKey, session);
                    if (defSite != null) {
                        siteService.setDefaultSite(defSite, session);
                        session.save();
                    }
                    return null;
                }
            });
        }
        siteService = null;
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    private JCRSessionWrapper defaultSession;

    private JCRSessionWrapper liveSession;

    private JahiaSite siteA;

    private JahiaSite siteB;

    private JahiaSite siteC;

    private void assertDefaultSite(String expetcedDefSiteKey) throws RepositoryException {
        JahiaSite defSite = siteService.getDefaultSite(liveSession);
        assertNotNull("There is no default site set", defSite);
        assertEquals("The default site is not set to the expected one: " + expetcedDefSiteKey, expetcedDefSiteKey,
                defSite.getSiteKey());
    }

    private void closeSession(JCRSessionWrapper session) {
        if (session != null) {
            try {
                session.logout();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void deleteSite(JahiaSite site, JCRSessionWrapper session) {
        if (site != null) {
            try {
                if (session.nodeExists(site.getJCRLocalPath())) {
                    TestHelper.deleteSite(site.getSiteKey());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void recreateLiveSession() throws RepositoryException {
        closeSession(liveSession);

        liveSession = JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH,
                Locale.ENGLISH);
    }

    @Before
    public void setUp() throws Exception {
        siteA = TestHelper.createSite(SITE_A);
        siteB = TestHelper.createSite(SITE_B);
        siteC = TestHelper.createSite(SITE_C);

        defaultSession = JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.EDIT_WORKSPACE,
                Locale.ENGLISH, Locale.ENGLISH);

        recreateLiveSession();
    }

    @After
    public void tearDown() {
        deleteSite(siteA, defaultSession);
        deleteSite(siteB, defaultSession);
        deleteSite(siteC, defaultSession);

        closeSession(defaultSession);
        closeSession(liveSession);
    }

    @Test
    public void testDefaultSite() throws Exception {
        siteService.setDefaultSite(siteB, defaultSession);
        defaultSession.save();
        recreateLiveSession();
        assertDefaultSite(siteB.getSiteKey());

        siteService.setDefaultSite(siteC, defaultSession);
        defaultSession.save();
        recreateLiveSession();
        assertDefaultSite(siteC.getSiteKey());

        siteService.removeSite(siteC);
        recreateLiveSession();
        assertNotNull(siteService.getDefaultSite(liveSession));

        siteService.setDefaultSite(siteA, defaultSession);
        defaultSession.save();
        recreateLiveSession();
        assertDefaultSite(siteA.getSiteKey());

        siteService.removeSite(siteB);
        recreateLiveSession();
        assertNotNull("There is no default site set", siteService.getDefaultSite(liveSession));
    }

    @Test
    public void testSiteKeyValidity() throws Exception {
        List<JahiaSite> sites = new LinkedList<>();
        JahiaSite site = null;
        try {
            site = TestHelper.createSite("jahiaSitesServiceTestSiteD_a-1");
            assertNotNull(site);
            sites.add(site);

            try {
                // create site with a dot in the key which should not be possible
                site = TestHelper.createSite("jahiaSitesServiceTestSiteD.a");
                sites.add(site);
                fail("Site with a dot in the site key should NOT have been created");
            } catch (JahiaException e) {
                assertTrue(e.getMessage().contains("Site key is not valid"));
            }
            try {
                // create site with a space in the key
                site = TestHelper.createSite("jahiaSitesServiceTestSiteD a");
                sites.add(site);
                fail("Site with a space in the site key should NOT have been created");
            } catch (JahiaException e) {
                assertTrue(e.getMessage().contains("Site key is not valid"));
            }
            try {
                // create site with a non latin characters
                site = TestHelper.createSite("jahiaSitesServiceTestSiteDäöüß");
                sites.add(site);
                fail("Site with a non-Latin charecters in the site key should NOT have been created");
            } catch (JahiaException e) {
                assertTrue(e.getMessage().contains("Site key is not valid"));
            }
        } finally {
            for (JahiaSite s : sites) {
                deleteSite(s, defaultSession);
            }
        }
    }
}
