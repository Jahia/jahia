/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.publication;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.wip.WIPInfo;
import org.jahia.services.wip.WIPService;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.utils.TestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Integration test for the Work in progress
 * Check publication of content with WIP Status for language
 */
public class WipIT extends AbstractJUnitTest {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(WipIT.class);

    private static JCRPublicationService publicationService;
    private ComplexPublicationService complexPublicationService;
    private static WIPService wipService;

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        publicationService = ServicesRegistry.getInstance().getJCRPublicationService();
        wipService = new WIPService();
        wipService.setPublicationService(publicationService);
        complexPublicationService = (ComplexPublicationService) SpringContextSingleton.getInstance().getContext().getBean("ComplexPublicationService");
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

    @Test
    public void testWIPChildrenAreNotPublishable() throws Exception {
        Map<String, Map<String, JCRSessionWrapper>> sessions = getCleanSessionForLanguages("en");
        JCRSessionWrapper englishEditSession = sessions.get("en").get(Constants.EDIT_WORKSPACE);
        Set<String> siteLanguages = Stream.of("en", "fr", "de").collect(Collectors.toSet());
        JahiaSite site = TestHelper.createSite("exampleSite" , siteLanguages, siteLanguages, false);
        JCRNodeWrapper page = englishEditSession.getNode(site.getJCRLocalPath()).addNode("testPage", "jnt:page");
        page.setProperty("j:templateName", "home");
        page.setProperty(Constants.JCR_TITLE, "testPage");
        englishEditSession.getNode(site.getJCRLocalPath()+ "/testPage").addNode("child1", "jnt:text");
        englishEditSession.getNode(site.getJCRLocalPath() + "/testPage").addNode("child2", "jnt:text");
        englishEditSession.save();
        JCRNodeWrapper siteNode = englishEditSession.getNode(site.getJCRLocalPath());
        Collection<ComplexPublicationService.FullPublicationInfo> infos = complexPublicationService.getFullPublicationInfos(Collections.singletonList(siteNode.getIdentifier()),
                Collections.singletonList("en"), true, englishEditSession);
        assertEquals(6, infos.size());
        assertTrue(infos.stream().anyMatch(e -> e.getNodePath().endsWith("/testPage/child1")));
        assertTrue(infos.stream().anyMatch(e -> e.getNodePath().endsWith("/testPage/child2")));
        wipService.createWipPropertiesOnNewNode(page, new WIPInfo(Constants.WORKINPROGRESS_STATUS_ALLCONTENT, siteLanguages));
        englishEditSession.save();
        Collection<ComplexPublicationService.FullPublicationInfo> infosAfterWIPStatusSet = complexPublicationService.getFullPublicationInfos(Collections.singletonList(siteNode.getIdentifier()),
                Collections.singletonList("en"), true, englishEditSession);

        //Make sure child nodes of WIP node are not getting published
        assertEquals(4, infosAfterWIPStatusSet.size());
        assertFalse(infosAfterWIPStatusSet.stream().anyMatch(e -> e.getNodePath().endsWith("/testPage/child1")));
        assertFalse(infosAfterWIPStatusSet.stream().anyMatch(e -> e.getNodePath().endsWith("/testPage/child2")));
        //check that child node is publishable, if checked separately
        Collection<ComplexPublicationService.FullPublicationInfo> childNodeInfos = complexPublicationService
                .getFullPublicationInfos(Collections.singletonList(englishEditSession.getNode("/sites/exampleSite/testPage/child1").getIdentifier()),
                Collections.singletonList("en"), true, englishEditSession);
        assertEquals(1, childNodeInfos.size());
        TestHelper.deleteSite("exampleSite");
    }

    @Test
    public void testWIPAutoPublish() throws Exception {
        Set<String> languages = Stream.of("en", "fr", "de").collect(Collectors.toSet());
        String siteName = "multipleLanguageSite";
        String testContentName = "i18nContent";
        JahiaSite site = TestHelper.createSite(siteName , languages, languages, false);
        String testContentPath = site.getJCRLocalPath() + "/" + testContentName;

        Map<String, Map<String, JCRSessionWrapper>> sessions = getCleanSessionForLanguages("en", "fr");
        JCRSessionWrapper enEditSession = sessions.get("en").get(Constants.EDIT_WORKSPACE);
        JCRSessionWrapper enLiveSession = sessions.get("en").get(Constants.LIVE_WORKSPACE);
        JCRSessionWrapper frEditSession = sessions.get("fr").get(Constants.EDIT_WORKSPACE);
        JCRSessionWrapper frLiveSession = sessions.get("fr").get(Constants.LIVE_WORKSPACE);

        // add autopublish mixin
        JCRNodeWrapper i18nContent = enEditSession
                .getNode(site.getJCRLocalPath()).addNode(testContentName , "jnt:text");
        i18nContent.addMixin("jmix:autoPublish");
        i18nContent.addMixin("jmix:rbTitle"); // used to have at least one non i18n prop: j:titleKey
        enEditSession.save();

        // test auto publish is working
        setPropertyAndAssertItsAutoPublished(testContentPath, "j:titleKey", enEditSession, enLiveSession, null, "titleKey", true, true);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", enEditSession, enLiveSession, null, "en", true, true);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", frEditSession, frLiveSession, null, "fr", true, true);

        // set WIP for all content, alongs with props modification, content should not be published
        enEditSession.refresh(false);
        i18nContent = enEditSession.getNode(testContentPath);
        final WIPInfo wipInfo = buildWipProperties(Constants.WORKINPROGRESS_STATUS_ALLCONTENT, Collections.emptySet());
        wipService.saveWipPropertiesIfNeeded(i18nContent, wipInfo);
        Assert.assertTrue(isSameWipInfo(i18nContent, wipInfo));
        setPropertyAndAssertItsAutoPublished(testContentPath, "j:titleKey", enEditSession, enLiveSession, "titleKey", "titleKey updated", true, false);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", enEditSession, enLiveSession, "en", "en updated", true, false);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", frEditSession, frLiveSession, "fr", "fr updated", true, false);

        // set WIP only for EN, check that FR and non 18n props are updated directly
        enEditSession.refresh(false);
        i18nContent = enEditSession.getNode(testContentPath);
        final WIPInfo WipInfoEn = buildWipProperties(Constants.WORKINPROGRESS_STATUS_LANG, Collections.singleton("en"));
        wipService.saveWipPropertiesIfNeeded(i18nContent, WipInfoEn);
        Assert.assertTrue(isSameWipInfo(i18nContent, WipInfoEn));
        setPropertyAndAssertItsAutoPublished(testContentPath, "j:titleKey", enEditSession, enLiveSession, "titleKey", "titleKey updated", false, true);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", enEditSession, enLiveSession, "en", "en updated", false, false);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", frEditSession, frLiveSession, "fr", "fr updated", false, true);


        // test that auto publish is now only working for FR and non i18n props
        setPropertyAndAssertItsAutoPublished(testContentPath, "j:titleKey", enEditSession, enLiveSession, "titleKey updated", "titleKey updated 2", true, true);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", enEditSession, enLiveSession, "en", "en updated 2", true, false);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", frEditSession, frLiveSession, "fr updated", "fr updated 2", true, true);

        // now disable WIP completely, en should be published directly
        enEditSession.refresh(false);
        i18nContent = enEditSession.getNode(testContentPath);
        final WIPInfo wipInfoDisabled = buildWipProperties(Constants.WORKINPROGRESS_STATUS_DISABLED, Collections.emptySet());
        wipService.saveWipPropertiesIfNeeded(i18nContent, wipInfoDisabled);
        Assert.assertTrue(isSameWipInfo(i18nContent, wipInfoDisabled));
        setPropertyAndAssertItsAutoPublished(testContentPath, "j:titleKey", enEditSession, enLiveSession, "titleKey updated", "titleKey updated 2", false, true);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", enEditSession, enLiveSession, "en", "en updated 2", false, true);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", frEditSession, frLiveSession, "fr updated", "fr updated 2", false, true);

        // test that now auto publish is back for the complete node for all languages and non i18n props
        setPropertyAndAssertItsAutoPublished(testContentPath, "j:titleKey", enEditSession, enLiveSession, "titleKey updated 2", "titleKey updated 3", true, true);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", enEditSession, enLiveSession, "en updated 2", "en updated 3", true, true);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", frEditSession, frLiveSession, "fr updated 2", "fr updated 3", true, true);

        // test that adding WIP on languages directly is working
        enEditSession.refresh(false);
        i18nContent = enEditSession.getNode(testContentPath);
        final WIPInfo wipInfoFr = buildWipProperties(Constants.WORKINPROGRESS_STATUS_LANG, Collections.singleton("fr"));
        wipService.saveWipPropertiesIfNeeded(i18nContent, wipInfoFr);
        Assert.assertTrue(isSameWipInfo(i18nContent, wipInfoFr));
        setPropertyAndAssertItsAutoPublished(testContentPath, "j:titleKey", enEditSession, enLiveSession, "titleKey updated 3", "titleKey updated 4", true, true);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", enEditSession, enLiveSession, "en updated 3", "en updated 4", true, true);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", frEditSession, frLiveSession, "fr updated 3", "fr updated 4", true, false);

        // test to switch WIP to EN only from WIP FR
        enEditSession.refresh(false);
        i18nContent = enEditSession.getNode(testContentPath);
        wipService.saveWipPropertiesIfNeeded(i18nContent, WipInfoEn);
        Assert.assertTrue(isSameWipInfo(i18nContent, WipInfoEn));
        setPropertyAndAssertItsAutoPublished(testContentPath, "j:titleKey", enEditSession, enLiveSession, "titleKey updated 3", "titleKey updated 4", false, true);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", enEditSession, enLiveSession, "en updated 3", "en updated 4", false, true);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", frEditSession, frLiveSession, "fr updated 3", "fr updated 4", false, true);

        setPropertyAndAssertItsAutoPublished(testContentPath, "j:titleKey", enEditSession, enLiveSession, "titleKey updated 4", "titleKey updated 5", true, true);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", enEditSession, enLiveSession, "en updated 4", "en updated 5", true, false);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", frEditSession, frLiveSession, "fr updated 4", "fr updated 5", true, true);

        // test swich to WIP to all content
        enEditSession.refresh(false);
        i18nContent = enEditSession.getNode(testContentPath);
        wipService.saveWipPropertiesIfNeeded(i18nContent, wipInfo);
        Assert.assertTrue(isSameWipInfo(i18nContent, wipInfo));
        setPropertyAndAssertItsAutoPublished(testContentPath, "j:titleKey", enEditSession, enLiveSession, "titleKey updated 5", "titleKey updated 6", true, false);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", enEditSession, enLiveSession, "en updated 4", "en updated 6", true, false);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", frEditSession, frLiveSession, "fr updated 5", "fr updated 6", true, false);

        // finally disable WIP and test autopublished
        enEditSession.refresh(false);
        i18nContent = enEditSession.getNode(testContentPath);
        wipService.saveWipPropertiesIfNeeded(i18nContent, wipInfoDisabled);
        Assert.assertTrue(isSameWipInfo(i18nContent, wipInfoDisabled));
        setPropertyAndAssertItsAutoPublished(testContentPath, "j:titleKey", enEditSession, enLiveSession, "titleKey updated 5", "titleKey updated 6", false, true);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", enEditSession, enLiveSession, "en updated 4", "en updated 6", false, true);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", frEditSession, frLiveSession, "fr updated 5", "fr updated 6", false, true);

        setPropertyAndAssertItsAutoPublished(testContentPath, "j:titleKey", enEditSession, enLiveSession, "titleKey updated 6", "titleKey updated 7", true, true);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", enEditSession, enLiveSession, "en updated 6", "en updated 7", true, true);
        setPropertyAndAssertItsAutoPublished(testContentPath, "text", frEditSession, frLiveSession, "fr updated 6", "fr updated 7", true, true);
    }

    private void setPropertyAndAssertItsAutoPublished(String nodePath, String propName, JCRSessionWrapper editSesion, JCRSessionWrapper liveSession,
                                                      String oldValue, String newValue, boolean setNewValue, boolean expectAutoPublish) throws RepositoryException {
        editSesion.refresh(false);
        liveSession.refresh(false);

        if (setNewValue) {
            JCRNodeWrapper node = editSesion.getNode(nodePath);
            node.setProperty(propName, newValue);
            editSesion.save();
        }

        JCRNodeWrapper liveNode = liveSession.getNode(nodePath);
        String liveValue = liveNode.hasProperty(propName) ? liveNode.getPropertyAsString(propName) : null;
        assertEquals(expectAutoPublish ? "Prop should be auto published" : "Prop should not be auto published",
                expectAutoPublish ? newValue : oldValue,
                liveValue);
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
        Map<String, Map<String, JCRSessionWrapper>> sessions = getCleanSessionForLanguages("en");
        JCRSessionWrapper englishEditSession = sessions.get("en").get(Constants.EDIT_WORKSPACE);
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
        englishEditSession.save();
        for (String lang : languages) {
            Map<String, Map<String, JCRSessionWrapper>> i18nSessions = getCleanSessionForLanguages(lang);
            JCRSessionWrapper localizedSession = i18nSessions.get(lang).get(Constants.EDIT_WORKSPACE);
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
                Map<String, Map<String, JCRSessionWrapper>> sessions = getCleanSessionForLanguages(language);
                JCRSessionWrapper editSession = sessions.get(language).get(Constants.EDIT_WORKSPACE);

                JCRNodeWrapper node = editSession.getNode(nodePath);
                String path = node.getPath();
                // Set WIP in the current language only
                wipService.saveWipPropertiesIfNeeded(node, buildWipProperties(status, Collections.singleton(language)));
                for (String checkedLanguage : siteLanguages) {
                    // publish the node in each language
                    publicationService.publishByMainId(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, Collections.singleton(checkedLanguage), true, null);
                    Boolean published = expectedResultsByStatus.get(status).get(checkedLanguage);
                    // When WIP Status is by language, result is true (content published) for all but current language
                    if (StringUtils.equals(status, Constants.WORKINPROGRESS_STATUS_LANG)) {
                        published = !StringUtils.equals(language, checkedLanguage);
                    }
                    logger.info("wip status [{} / {}] for node [{}] - [{}] session => content is {}", new String[]{status, language, path, checkedLanguage, published ? "be published" : " NOT be published"});
                    // Validate the publication of the node
                    Map<String, Map<String, JCRSessionWrapper>> checkedSessioms = getCleanSessionForLanguages(checkedLanguage);
                    JCRSessionWrapper liveSession = checkedSessioms.get(checkedLanguage).get(Constants.LIVE_WORKSPACE);

                    assertEquals(liveSession.nodeExists(path), published);
                    // clean up
                    if (liveSession.nodeExists(path)) {
                        liveSession.getNode(path).remove();
                        liveSession.save();
                    }
                }
            }
        }
    }

    private WIPInfo buildWipProperties(String wipStatus, Set<String> wipLanguages) {
        return new WIPInfo(wipStatus, wipLanguages);
    }

    private boolean isSameWipInfo(JCRNodeWrapper node, WIPInfo wipInfo) throws Exception {
        WIPInfo wipOnNode = wipService.getWipInfo(node);
        final Set<String> wipLanguages = wipInfo.getLanguages();
        final Set<String> wipOnNodeLanguages = wipOnNode.getLanguages();
        return (wipOnNode.getStatus().equals(wipInfo.getStatus()) && wipLanguages.containsAll(wipOnNodeLanguages) && wipOnNodeLanguages.containsAll(wipLanguages));
    }

    private Map<String, Map<String, JCRSessionWrapper>> getCleanSessionForLanguages(String ...languages) throws RepositoryException {
        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        sessionFactory.closeAllSessions();
        Map<String, Map<String, JCRSessionWrapper>> result = new HashMap<>();
        for (String language : languages) {
            Map<String, JCRSessionWrapper> sessions = new HashMap<>();
            sessions.put(Constants.EDIT_WORKSPACE, sessionFactory.getCurrentUserSession(Constants.EDIT_WORKSPACE, LocaleUtils.toLocale(language)));
            sessions.put(Constants.LIVE_WORKSPACE, sessionFactory.getCurrentUserSession(Constants.LIVE_WORKSPACE, LocaleUtils.toLocale(language)));
            result.put(language, sessions);
        }
        return result;
    }

}
