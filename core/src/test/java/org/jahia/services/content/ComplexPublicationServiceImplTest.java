/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.utils.TestHelper;
import org.junit.*;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.util.*;

import static org.junit.Assert.*;

public class ComplexPublicationServiceImplTest extends AbstractJUnitTest {
    private static final transient Logger logger = org.slf4j.LoggerFactory
            .getLogger(ComplexPublicationServiceImplTest.class);

    private static JahiaSite site;
    private static ComplexPublicationService complexPublicationService;
    private static JCRPublicationService publicationService;

    private final static String TESTSITE_NAME = "ComplexPublicationServiceImplTestSite";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private final static String INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE = "English text";
    private final static String INITIAL_FRENCH_TEXT_NODE_PROPERTY_VALUE = "French text";

    private JCRNodeWrapper testHomeEdit;

    private JCRSessionWrapper englishEditSession;
    private JCRSessionWrapper frenchEditSession;
    private JCRSessionWrapper systemEditSession;


    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        site = TestHelper.createSite(TESTSITE_NAME, new HashSet<String>(Arrays.asList("en", "fr")),
                Collections.singleton("en"), false);
        assertNotNull(site);

        complexPublicationService = (ComplexPublicationService) SpringContextSingleton.getInstance().getContext().getBean("ComplexPublicationService");
        publicationService = ServicesRegistry.getInstance().getJCRPublicationService();
    }

    @Override
    public void afterClassSetup() throws Exception {
        super.afterClassSetup();
        TestHelper.deleteSite(TESTSITE_NAME);
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Before
    public void setUp() {
        try {
            getCleanSession();
            JCRNodeWrapper englishEditSiteHomeNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home");
            testHomeEdit = englishEditSiteHomeNode.addNode("test" + System.currentTimeMillis(), Constants.JAHIANT_PAGE);
            testHomeEdit.setProperty(Constants.JCR_TITLE, "Test page");
            testHomeEdit.setProperty("j:templateName", "simple");
            englishEditSession.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            fail("Cannot setUp test: " + e.getMessage());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
        JCRSessionFactory.getInstance().closeAllSessions();
    }


    @Test
    public void getFullUnpublicationInfos() throws RepositoryException {
        getCleanSession();
        // given a published node with 2 translations
        JCRNodeWrapper enContent = englishEditSession.getNode(testHomeEdit.getPath()).addNode("testNode", "jnt:text");
        enContent.setProperty("text", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        englishEditSession.save();
        frenchEditSession.getNode(enContent.getPath()).setProperty("text", INITIAL_FRENCH_TEXT_NODE_PROPERTY_VALUE);
        frenchEditSession.save();

        // When I publish the content in all languages.
        publicationService.publishByMainId(enContent.getIdentifier());

        // Then the info in english should have one entry with no shared node and translation node set
        ResultInfo result = new ResultInfo(enContent.getI18N(Locale.ENGLISH).getIdentifier(), null);
        checkPublicationInfos(Collections.singletonList(enContent.getIdentifier()), Arrays.asList(Locale.ENGLISH.toString()), systemEditSession, Collections.singletonList(result));

        // Then the info in all languages should contain 2 results
        ResultInfo result1 = new ResultInfo(enContent.getI18N(Locale.ENGLISH).getIdentifier(), null);
        ResultInfo result2 = new ResultInfo(enContent.getI18N(Locale.FRENCH).getIdentifier(), null);
        checkPublicationInfos(Collections.singletonList(enContent.getIdentifier()), Arrays.asList(Locale.ENGLISH.toString(), Locale.FRENCH.toString()), systemEditSession, Arrays.asList(result1, result2));


        // When the node is unpublished in english
        publicationService.unpublish(Collections.singletonList(enContent.getI18N(Locale.ENGLISH).getIdentifier()));

        // Then info in french should have one entry with no shared node and translation node set
        result = new ResultInfo(enContent.getI18N(Locale.FRENCH).getIdentifier(), null);
        checkPublicationInfos(Collections.singletonList(enContent.getIdentifier()), Arrays.asList(Locale.FRENCH.toString()), systemEditSession, Collections.singletonList(result));

        // When querying info in english, Then the result should be empty
        checkPublicationInfos(Collections.singletonList(enContent.getIdentifier()), Arrays.asList(Locale.ENGLISH.toString()), systemEditSession, Collections.emptyList());

        // Then the info in all languages should contain 1 result
        checkPublicationInfos(Collections.singletonList(enContent.getIdentifier()), Arrays.asList(Locale.ENGLISH.toString(), Locale.FRENCH.toString()), systemEditSession, Arrays.asList(result2));

    }

    @Test
    public void getFullUnpublicationInfosOnNonI18n() throws RepositoryException {
        getCleanSession();
        // Given a published non i18n content
        JCRNodeWrapper content = englishEditSession.getNode(testHomeEdit.getPath()).addNode("testReference", "jnt:contentReference");
        englishEditSession.save();
        publicationService.publishByMainId(content.getIdentifier());

        // When querying the unpublication info in english, Then we should have one entry with no shared node
        ResultInfo resultRef = new ResultInfo(null, content.getIdentifier());
        checkPublicationInfos(Collections.singletonList(content.getIdentifier()), Arrays.asList(Locale.ENGLISH.toString()), systemEditSession, Collections.singletonList(resultRef));

        // When querying the unpublication info in all languages, Then we should have two entries with no shared node
        checkPublicationInfos(Collections.singletonList(content.getIdentifier()), Arrays.asList(Locale.ENGLISH.toString(), Locale.FRENCH.toString()), systemEditSession, Arrays.asList(resultRef, resultRef));
    }

    @Test
    public void getFullUnpublicationInfosOnBothI18nAndNotI18n() throws RepositoryException {
        getCleanSession();
        // Given a published non i18n content
        JCRNodeWrapper content = englishEditSession.getNode(testHomeEdit.getPath()).addNode("testReference", "jnt:contentReference");
        englishEditSession.save();
        publicationService.publishByMainId(content.getIdentifier());

        // given a published node with 2 translations
        JCRNodeWrapper enContent = englishEditSession.getNode(testHomeEdit.getPath()).addNode("testNode", "jnt:text");
        enContent.setProperty("text", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        englishEditSession.save();
        frenchEditSession.getNode(enContent.getPath()).setProperty("text", INITIAL_FRENCH_TEXT_NODE_PROPERTY_VALUE);
        frenchEditSession.save();
        publicationService.publishByMainId(enContent.getIdentifier());

        // When querying the unpublication info in english for both nodes, Then we should have two entries
        ResultInfo i18nResult = new ResultInfo(enContent.getI18N(Locale.ENGLISH).getIdentifier(), null);
        ResultInfo result = new ResultInfo(null, content.getIdentifier());
        checkPublicationInfos(Arrays.asList(enContent.getIdentifier(), content.getIdentifier()),
                Collections.singletonList(Locale.ENGLISH.toString()),
                systemEditSession,
                Arrays.asList(i18nResult, result));
    }

    @Test
    public void getFullUnpublicationInfosOnList() throws RepositoryException {

        getCleanSession();
        // given a published list with 2 contents in english only,
        JCRNodeWrapper list = englishEditSession.getNode(testHomeEdit.getPath()).addNode("list", "jnt:contentList");
        JCRNodeWrapper c1 = list.addNode("testNode", "jnt:text");
        c1.setProperty("text", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        JCRNodeWrapper c2 = list.addNode("testNode1", "jnt:text");
        c2.setProperty("text", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        englishEditSession.save();
        publicationService.publishByMainId(list.getIdentifier());

        // When querying info in french, Then the result should be empty
        checkPublicationInfos(Collections.singletonList(list.getIdentifier()), Collections.singletonList(Locale.FRENCH.toString()), systemEditSession, Collections.emptyList());

        // Then the info in all languages should contain 2 results
        ResultInfo r1 = new ResultInfo(null, list.getIdentifier());
        ResultInfo r2 = new ResultInfo(c1.getI18N(Locale.ENGLISH).getIdentifier(), null);
        ResultInfo r3 = new ResultInfo(c2.getI18N(Locale.ENGLISH).getIdentifier(), null);

        // When querying info in english, Then the result should be empty
        checkPublicationInfos(Collections.singletonList(list.getIdentifier()), Collections.singletonList(Locale.ENGLISH.toString()), systemEditSession, Arrays.asList(r1, r2, r3));

        // Then the info in all languages should the same
        checkPublicationInfos(Collections.singletonList(list.getIdentifier()), Arrays.asList(Locale.ENGLISH.toString(), Locale.FRENCH.toString()), systemEditSession, Arrays.asList(r1, r2, r3));

        // Given I set amd publish a content in french
        frenchEditSession.getNode(c1.getPath()).setProperty("text", INITIAL_FRENCH_TEXT_NODE_PROPERTY_VALUE);
        frenchEditSession.save();
        publicationService.publishByMainId(c1.getIdentifier());

        // When check in french
        ResultInfo r4 = new ResultInfo(c1.getI18N(Locale.FRENCH).getIdentifier(), null);
        checkPublicationInfos(Collections.singletonList(list.getIdentifier()), Collections.singletonList(Locale.FRENCH.toString()), systemEditSession, Collections.singletonList(r4));
        // When check in english
        checkPublicationInfos(Collections.singletonList(list.getIdentifier()), Collections.singletonList(Locale.ENGLISH.toString()), systemEditSession, Arrays.asList(r2, r3));
        // When check in all languages

        checkPublicationInfos(Collections.singletonList(list.getIdentifier()), Arrays.asList(Locale.ENGLISH.toString(), Locale.FRENCH.toString()), systemEditSession, Arrays.asList(r1, r2, r3, r1, r4));
    }


    private void checkPublicationInfos(List<String> identifiers, List<String> locales, JCRSessionWrapper session, List<ResultInfo> results) throws RepositoryException {
        Collection<ComplexPublicationService.FullPublicationInfo> infos = complexPublicationService.getFullUnpublicationInfos(identifiers, locales, true, session);

        // Then I receive only one publication info
        assertEquals("we were expecting " + infos.size() + " publication info", results.size(), infos.size());
        if (!infos.isEmpty()) {
            int i = 0;
            for (ComplexPublicationService.FullPublicationInfo info : infos) {
                // Then the info contains i18n content
                assertEquals("translated node is not set", info.getTranslationNodeIdentifier(), results.get(i).i18nUUID);
                // Then the info contains no shared node
                assertEquals("Shared node is null", info.getNodeIdentifier(), results.get(i).SharedUUID);
                i++;
            }
        }
    }

    private void getCleanSession() throws RepositoryException {
        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        sessionFactory.closeAllSessions();
        englishEditSession = sessionFactory.getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        frenchEditSession = sessionFactory.getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.FRENCH);
        systemEditSession = sessionFactory.getCurrentSystemSession(Constants.EDIT_WORKSPACE, null, null);
    }

    class ResultInfo {
        String i18nUUID;
        String SharedUUID;

        public ResultInfo(String i18nUUID, String sharedUUID) {
            this.i18nUUID = i18nUUID;
            SharedUUID = sharedUUID;
        }
    }
}
