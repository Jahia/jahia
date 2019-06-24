/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.publication;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.utils.GWTContentUtils;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Integration test for the Work in progress
 * Check publication of content with WIP Status for language
 */
public class WipTest {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(WipTest.class);

    private static JCRPublicationService jcrService;

    @BeforeClass
    public static void setUpClass() {
        jcrService = ServicesRegistry.getInstance().getJCRPublicationService();
    }

    @After
    public void tearDown() {
        JCRSessionFactory.getInstance().closeAllSessions();
    }


    @Test
    public void testWipSiteWithOneLanguage() throws Exception {
        // Given a site with one language
        Set<String> siteLanguages = Collections.singleton("en");
        testWipSiteWithMultipleLanguage(siteLanguages, "oneLanguageSite");
    }

    @Test
    public void testWipSiteWithMultipleLanguage()  throws Exception {
        // Given a site with several languages
        Set<String> siteLanguages = Stream.of("en", "fr", "de").collect(Collectors.toSet());
        testWipSiteWithMultipleLanguage(siteLanguages, "multipleLanguageSite");
    }

    private void testWipSiteWithMultipleLanguage(Set<String> siteLanguages, String siteName) throws Exception {
        JahiaSite site = TestHelper.createSite(siteName , siteLanguages, siteLanguages, false);
        // Given contents in languages
        List<String> contentNodes = createContentNodes(site.getJCRLocalPath(), siteLanguages);
        for (String contentNodePath : contentNodes) {
            checkWipStatusOnNode(contentNodePath, siteLanguages);
        }
        // clean up
        TestHelper.deleteSite(siteName);
    }

    private List<String> createContentNodes(String basePath, Set<String> languages) throws RepositoryException {
        Map<String, JCRSessionWrapper> sessions = getCleanSessionForLanguage("en");
        JCRSessionWrapper englishEditSession = sessions.get(Constants.EDIT_WORKSPACE);
        // create nodes with non i18n content
        JCRNodeWrapper i18nContent = englishEditSession.getNode(basePath).addNode("i18nContent", "jnt:text");

        JCRNodeWrapper nonI18nContent = englishEditSession.getNode(basePath).addNode("nonI18nContent", "jnt:reference");
        nonI18nContent.addMixin("jmix:lastPublished");
        nonI18nContent.setProperty("j:propertyName", "property");

        JCRNodeWrapper mixContent = englishEditSession.getNode(basePath).addNode("mixContent", "jnt:query");
        mixContent.addMixin("jmix:lastPublished");
        mixContent.setProperty("maxItems", 10);

        JCRNodeWrapper mixContentOnlyNonI18n = englishEditSession.getNode(basePath).addNode("mixContentOnlyNonI18n", "jnt:query");
        mixContentOnlyNonI18n.addMixin("jmix:lastPublished");
        mixContentOnlyNonI18n.setProperty("maxItems", 10);

        JCRNodeWrapper mixContentOnlyI18n = englishEditSession.getNode(basePath).addNode("mixContentOnlyI18n", "jnt:query");
        mixContentOnlyI18n.addMixin("jmix:lastPublished");

        // set i18n properties
        englishEditSession.save();
        for (String lang : languages) {
            Map<String, JCRSessionWrapper> i18nSessions = getCleanSessionForLanguage(lang);
            JCRSessionWrapper localizedSession = i18nSessions.get(Constants.EDIT_WORKSPACE);
            localizedSession.getNode(i18nContent.getPath()).setProperty("text", lang + " text");
            localizedSession.getNode(mixContent.getPath()).setProperty("jcr:description", lang + " description");
            localizedSession.getNode(mixContentOnlyI18n.getPath()).setProperty("jcr:description", lang + " description");
            localizedSession.save();
        }
        return  Arrays.asList(i18nContent.getPath(), nonI18nContent.getPath(), mixContent.getPath(), mixContentOnlyNonI18n.getPath(), mixContentOnlyI18n.getPath());
    }

    private void checkWipStatusOnNode(String nodePath, Set<String> siteLanguages) throws RepositoryException {
        Map<String, Map<String, Boolean>> expectedResultsByStatus = new HashMap<>();
        // When WIP status is ALL CONTENT
        Map<String, Boolean> resultAllContent = new HashMap<>();
        for (String language : siteLanguages) {
            // Then the result is always false (no content to publish)
            resultAllContent.put(language, Boolean.FALSE);
        }
        expectedResultsByStatus.put(Constants.WORKINPROGRESS_STATUS_ALLCONTENT, resultAllContent);
        // When WIP status is DISABLED
        Map<String, Boolean> resulDisabledtByLanguage = new HashMap<>();
        for (String language : siteLanguages) {
            // Then the result is always true (content is published)
            resulDisabledtByLanguage.put(language, Boolean.TRUE);
        }
        expectedResultsByStatus.put(Constants.WORKINPROGRESS_STATUS_DISABLED, resulDisabledtByLanguage);
        // When WIP Status is by language, THEN result is true (content published) for all but current language
        Map<String, Boolean> resultByLanguage = new HashMap<>();
        for (String language : siteLanguages) {
            resultByLanguage.put(language, null);
        }
        expectedResultsByStatus.put(Constants.WORKINPROGRESS_STATUS_LANG, resultByLanguage);

        // When I set wip on the node
        for (String status : expectedResultsByStatus.keySet()) {
            for (String language : siteLanguages) {
                Map<String, JCRSessionWrapper>  sessions = getCleanSessionForLanguage(language);
                JCRSessionWrapper editSession = sessions.get(Constants.EDIT_WORKSPACE);

                JCRNodeWrapper node = editSession.getNode(nodePath);
                String path = node.getPath();
                // Set WIP in the current language only
                GWTContentUtils.saveWipPropertiesIfNeeded(node, buildWipProperties(status, Collections.singleton(language)));
                for (String checkedLanguage : siteLanguages) {
                    // publish the node in each language
                    jcrService.publishByMainId(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, Collections.singleton(checkedLanguage), true, null);
                    Boolean published = expectedResultsByStatus.get(status).get(checkedLanguage);
                    // When WIP Status is by language, result is true (content published) for all but current language
                    if (StringUtils.equals(status, Constants.WORKINPROGRESS_STATUS_LANG)) {
                        published = !StringUtils.equals(language, checkedLanguage);
                    }
                    logger.info("wip status [{} / {}] for node [{}] - [{}] session => content is {}", new String[]{status, language, path, checkedLanguage, published ? "be published" : " NOT be published"});
                    // Validate the publication of the node
                    Map<String, JCRSessionWrapper>  checkedSessioms = getCleanSessionForLanguage(checkedLanguage);
                    JCRSessionWrapper liveSession = checkedSessioms.get(Constants.LIVE_WORKSPACE);

                    Assert.assertEquals(liveSession.nodeExists(path), published);
                    // clean up
                    if (liveSession.nodeExists(path)) {
                        liveSession.getNode(path).remove();
                        liveSession.save();
                    }
                }
            }
        }
    }

    private List<GWTJahiaNodeProperty> buildWipProperties(String wipStatus, Set<String> wipLanguages) {
        List<GWTJahiaNodeProperty> properties = new ArrayList<>();
        GWTJahiaNodeProperty wipStatusProp = new GWTJahiaNodeProperty(Constants.WORKINPROGRESS_STATUS, wipStatus);
        GWTJahiaNodeProperty wipLanguagesProp = new GWTJahiaNodeProperty(Constants.WORKINPROGRESS_LANGUAGES,"");
        wipLanguagesProp.setValues(wipLanguages.stream().map(GWTJahiaNodePropertyValue::new).collect(Collectors.toList()));
        properties.add(wipStatusProp);
        properties.add(wipLanguagesProp);
        return properties;
    }


    private Map<String, JCRSessionWrapper> getCleanSessionForLanguage(String language) throws RepositoryException {
        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        sessionFactory.closeAllSessions();
        Map<String, JCRSessionWrapper> sessions = new HashMap<>();
        sessions.put(Constants.EDIT_WORKSPACE, sessionFactory.getCurrentUserSession(Constants.EDIT_WORKSPACE, LocaleUtils.toLocale(language)));
        sessions.put(Constants.LIVE_WORKSPACE, sessionFactory.getCurrentUserSession(Constants.LIVE_WORKSPACE, LocaleUtils.toLocale(language)));
        return sessions;
    }

}
