/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.vanity;

import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.junit.*;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;

public class VanityUrlServiceTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(VanityUrlServiceTest.class);

    private static final String SITEA = "siteA";

    private static VanityUrlService vanityUrlService;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            vanityUrlService = (VanityUrlService) SpringContextSingleton.getBean("org.jahia.services.seo.jcr.VanityUrlService");

            initSite(SITEA);
        } catch (Exception e) {
            logger.error("Error setting up ValidationTest environment", e);
            Assert.fail();
        }
    }

    @After
    public void tearDown() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            try {
                JCRNodeWrapper page = session.getNode("/sites/" + SITEA + "/home/page1");
                page.getNode("vanityUrlMapping").remove();
                session.save();
                vanityUrlService.flushCaches();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });

        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            TestHelper.deleteSite(SITEA);
        } catch (Exception e) {
            logger.error("Error tearing down ValidationTest environment", e);
        }
    }

    @Test
    public void shouldAddAndUpdateNewVanity() throws Exception {
        VanityUrl vanityUrl = createVanity(true, true, "/test", SITEA, "en");
        saveVanity(vanityUrl);
        vanityUrl = insureVanityExist(vanityUrl);

        vanityUrl.setActive(false);
        saveVanity(vanityUrl);
        vanityUrl = insureVanityExist(vanityUrl);

        vanityUrl.setUrl("/test2");
        saveVanity(vanityUrl);
        vanityUrl = insureVanityExist(vanityUrl);

        vanityUrl.setLanguage("fr");
        saveVanity(vanityUrl);
        vanityUrl = insureVanityExist(vanityUrl);

        vanityUrl.setDefaultMapping(false);
        saveVanity(vanityUrl);
        insureVanityExist(vanityUrl);
    }

    @Test
    public void shouldDisableDefault() throws Exception {
        VanityUrl defaultVanity = createVanity(true, true, "/default", SITEA, "en");
        VanityUrl vanity = createVanity(true, false, "/normal", SITEA, "en");

        saveVanity(defaultVanity);
        saveVanity(vanity);

        defaultVanity = insureVanityExist(defaultVanity);
        vanity = insureVanityExist(vanity);

        vanity.setDefaultMapping(true);
        saveVanity(vanity);

        defaultVanity.setDefaultMapping(false);
        insureVanityExist(defaultVanity);
        insureVanityExist(vanity);
    }

    @Test
    public void shouldDisableDefaultWhenLanguageSwitch() throws Exception {
        VanityUrl frVanity = createVanity(true, true, "/fr", SITEA, "fr");
        VanityUrl enVanity = createVanity(true, true, "/en", SITEA, "en");

        saveVanity(frVanity);
        saveVanity(enVanity);

        frVanity = insureVanityExist(frVanity);
        enVanity = insureVanityExist(enVanity);

        frVanity.setLanguage("en");
        saveVanity(frVanity);

        enVanity.setDefaultMapping(false);
        insureVanityExist(frVanity);
        insureVanityExist(enVanity);
    }

    private static void initSite(String siteKey) throws Exception {
        JahiaSite site = TestHelper.createSite(siteKey, "localhost", TestHelper.WEB_TEMPLATES);
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            try {
                createPage((JCRSiteNode) session.getNode(site.getJCRLocalPath()), "page1");
                session.save();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    private static void insureVanityDoesNotExist(String vanityUrl) throws RepositoryException {
        int foundCount = JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            try {
                return vanityUrlService.findExistingVanityUrls(vanityUrl, SITEA, "default").size();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        assertEquals(0, foundCount);
    }

    private static VanityUrl insureVanityExist(VanityUrl vanityUrlToTest) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            try {
                VanityUrl vanityUrl = vanityUrlService.findExistingVanityUrls(vanityUrlToTest.getUrl(), SITEA, "default").get(0);
                Assert.assertTrue(vanityUrl != null);
                Assert.assertEquals(vanityUrlToTest.isActive(), vanityUrl.isActive());
                Assert.assertEquals(vanityUrlToTest.isDefaultMapping(), vanityUrl.isDefaultMapping());
                Assert.assertEquals(vanityUrlToTest.getLanguage(), vanityUrl.getLanguage());
                Assert.assertEquals(vanityUrlToTest.getUrl(), vanityUrl.getUrl());
                return vanityUrl;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void saveVanity(VanityUrl vanityUrl) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            try {
                JCRNodeWrapper page = session.getNode("/sites/" + SITEA + "/home/page1");
                vanityUrlService.saveVanityUrlMapping(page, vanityUrl);
                session.save();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    private static void saveVanities(List<VanityUrl> vanityUrls, Set<String> updatedLocales) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            try {
                JCRNodeWrapper page = session.getNode("/sites/" + SITEA + "/home/page1");
                vanityUrlService.saveVanityUrlMappings(page, vanityUrls, updatedLocales);
                session.save();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    private static JCRNodeWrapper createPage(JCRSiteNode site, String pageName) throws Exception {
        JCRNodeWrapper page = site.getNode("home").addNode(pageName, "jnt:page");
        page.setProperty("j:templateName", "simple");
        page.setProperty("jcr:title", pageName);
        return page;
    }

    private static VanityUrl createVanity(boolean isActive, boolean isDefault, String url, String site, String language) {
        VanityUrl vanityUrl = new VanityUrl();
        vanityUrl.setActive(isActive);
        vanityUrl.setDefaultMapping(isDefault);
        vanityUrl.setLanguage(language);
        vanityUrl.setSite(site);
        vanityUrl.setUrl(url);
        return vanityUrl;
    }

    @Test
    public void testDefaultFlag() throws Exception {
        // create first vanity
        VanityUrl vanityUrl1 = createVanity(true, true, "/test", SITEA, "en");
        saveVanity(vanityUrl1);
        vanityUrl1 = insureVanityExist(vanityUrl1);

        // create second (non-default) vanity
        VanityUrl vanityUrl2 = createVanity(true, false, "/test2", SITEA, "en");
        saveVanities(Lists.newArrayList(vanityUrl1, vanityUrl2), Sets.newHashSet("en"));

        vanityUrl1 = insureVanityExist(vanityUrl1);
        vanityUrl2 = insureVanityExist(vanityUrl2);

        // create third vanity and set it as default
        VanityUrl vanityUrl3 = createVanity(true, true, "/test3", SITEA, "en");
        vanityUrl1.setDefaultMapping(false);
        saveVanities(Lists.newArrayList(vanityUrl1, vanityUrl2, vanityUrl3), Sets.newHashSet("en"));

        insureVanityExist(vanityUrl1);
        insureVanityExist(vanityUrl2);
        insureVanityExist(vanityUrl3);
    }

    @Test
    public void testDelete() throws Exception {
        // create first vanity
        VanityUrl vanityUrl1 = createVanity(true, true, "/test", SITEA, "en");
        saveVanity(vanityUrl1);
        vanityUrl1 = insureVanityExist(vanityUrl1);

        // create second (non-default) vanity
        VanityUrl vanityUrl2 = createVanity(true, false, "/test2", SITEA, "en");
        saveVanities(Lists.newArrayList(vanityUrl1, vanityUrl2), Sets.newHashSet("en"));

        vanityUrl1 = insureVanityExist(vanityUrl1);
        vanityUrl2 = insureVanityExist(vanityUrl2);
        
        saveVanities(Lists.newArrayList(vanityUrl1), Sets.newHashSet("en"));
        
        insureVanityExist(vanityUrl1);
        insureVanityDoesNotExist(vanityUrl2.getUrl());

        saveVanities(Lists.newArrayList(), Sets.newHashSet("en"));
        
        insureVanityDoesNotExist(vanityUrl1.getUrl());
        insureVanityDoesNotExist(vanityUrl2.getUrl());
    }
}
