/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.test.services.vanity;

import com.google.common.collect.Sets;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlManager;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.junit.*;

import javax.jcr.RepositoryException;
import java.util.*;

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
