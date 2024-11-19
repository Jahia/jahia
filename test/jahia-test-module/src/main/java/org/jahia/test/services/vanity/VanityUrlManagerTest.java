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
package org.jahia.test.services.vanity;

import com.google.common.collect.Sets;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.ReservedUrlMappingException;
import org.jahia.services.seo.jcr.VanityUrlManager;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.junit.*;

import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class VanityUrlManagerTest {

    private static final String SITE_NAME = "testsite";

    private static VanityUrlManager vanityUrlManager;
    private static JCRPublicationService publicationService;
    private static JahiaSite site;

    @BeforeClass
    public static void setUpClass() throws Exception {
        vanityUrlManager = (VanityUrlManager) SpringContextSingleton.getBean(VanityUrlManager.class.getName());
        publicationService = ServicesRegistry.getInstance().getJCRPublicationService();
        site = TestHelper.createSite(SITE_NAME, "localhost", TestHelper.WEB_TEMPLATES);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        TestHelper.deleteSite(SITE_NAME);
    }

    @After
    public void tearDown() {
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    /**
     * Non-regression test for QA-11406
     *
     * Verifies that saving vanity URls does not lead to inconsistencies between edit and live workspaces that would prevent
     * subsequent calls to {@link VanityUrlManager#saveVanityUrlMappings(JCRNodeWrapper, List, Set, JCRSessionWrapper)}
     * to succeed.
     */
    @Test
    public void testSaveVanitiesMultipleTimesAfterPublication() throws Exception {
        final String language = "en";
        final String pageId = createPage("pageToPublish");

        try {
            final VanityUrl vanityUrl = new VanityUrl("/seo1", SITE_NAME, language, true, true);
            saveVanityUrlMappings(pageId, Arrays.asList(vanityUrl), Sets.newHashSet(language));

            publishSite();

            saveVanityUrlMappings(pageId, getVanityUrls(pageId, language, true), Sets.newHashSet(language));
            saveVanityUrlMappings(pageId, getVanityUrls(pageId, language, true), Sets.newHashSet(language));

        } finally {
            // clean-up: delete the page and un-publish it
            deleteNode(pageId);
            publishSite();
        }
    }

    @Test
    public void testSaveReservedVanityUrls() throws Exception {
        final String language = "en";
        final String pageId = createPage("pageToSetAnInvalidVanity");

        try {
            final VanityUrl vanityUrl = new VanityUrl("/GWT/testregexp", SITE_NAME, language, true, true);
            try {
                saveVanityUrlMappings(pageId, Arrays.asList(vanityUrl), Sets.newHashSet(language));
            } catch (ReservedUrlMappingException e) {
                return;
            }
            Assert.fail("/GWT/testregexp is not a valid vanity url");

        } finally {
            // clean-up: delete the page and un-publish it
            deleteNode(pageId);
        }
    }

    private static String createPage(String pageName) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            JCRSiteNode siteNode = (JCRSiteNode) session.getNode(site.getJCRLocalPath());

            JCRNodeWrapper page = siteNode.getNode("home").addNode(pageName, "jnt:page");
            page.setProperty("j:templateName", "simple");
            page.setProperty("jcr:title", pageName);
            session.save();

            String pagePath = siteNode.getPath() + "/home/" + pageName;
            return session.getNode(pagePath).getIdentifier();
        });
    }

    private static void deleteNode(String nodeId) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
           JCRNodeWrapper node = session.getNodeByIdentifier(nodeId);
           node.remove();
           session.save();
           return null;
        });
    }

    private static void saveVanityUrlMappings(String pageId, List<VanityUrl> vanityUrls, Set<String> updatedLocales) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            JCRNodeWrapper page = session.getNodeByIdentifier(pageId);
            vanityUrlManager.saveVanityUrlMappings(page, vanityUrls, updatedLocales, session);
            session.save();
            return null;
        });
    }

    private static List<VanityUrl> getVanityUrls(String pageId, String languageCode, boolean removeIdentifiers) throws RepositoryException {
        List<VanityUrl> vanityUrls = JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            JCRNodeWrapper page = session.getNodeByIdentifier(pageId);
            return vanityUrlManager.getVanityUrls(page, languageCode, session);
        });
        if (removeIdentifiers) {
            for (VanityUrl vanityUrl : vanityUrls) {
                vanityUrl.setIdentifier(null);
            }
        }
        return vanityUrls;
    }

    private static void publishSite() throws RepositoryException {
        String siteId = JCRTemplate.getInstance().doExecuteWithSystemSession(session ->
                session.getNode(site.getJCRLocalPath()).getIdentifier()
        );
        publicationService.publishByMainId(siteId);
    }

}
