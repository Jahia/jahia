/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.content.nodetypes;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Locale;

import javax.jcr.RepositoryException;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.test.JahiaTestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Integration test for the default values of node properties.
 *
 * @author Sergiy Shyrkov
 */
public class NodePropertyDefaultValueTest extends JahiaTestCase {

    //private static final String SITE_KEY = "defValuesTestSite";
    private static final String SITE_KEY = JahiaSitesService.SYSTEM_SITE_KEY;

    private static final String FOLDER_PATH = "/sites/" + SITE_KEY + "/contents";

    private static final String NODE_NAME = "defaultValuesTest";

    private static final String NODE_PATH = FOLDER_PATH + "/" + NODE_NAME;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        //TestHelper.createSite(SITE_KEY, ImmutableSet.of("en", "de_DE", "fr_FR"), ImmutableSet.of("en"), false);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        //TestHelper.deleteSite(SITE_KEY);
    }

    private JCRSessionWrapper englishSession;
    private JCRSessionWrapper frenchSession;
    private JCRSessionWrapper germanSession;

    @Before
    public void setUp() throws RepositoryException {
        refreshSession();
        if (englishSession.nodeExists(NODE_PATH)) {
            englishSession.getNode(NODE_PATH).remove();
            englishSession.save();
        }
    }

    @After
    public void tearDown() throws RepositoryException {
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testDefaultValues() throws Exception {
        Calendar now = Calendar.getInstance();
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        englishSession.getNode(FOLDER_PATH).addNode(NODE_NAME, "test:dynamicValuesTest");
        englishSession.save();
        englishSession.logout();
        refreshSession();

        checkPropertyValues(now, tomorrow, "");
    }

    @Test
    public void testMixinDefaultValues() throws Exception {
        Calendar now = Calendar.getInstance();
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        JCRNodeWrapper node = englishSession.getNode(FOLDER_PATH).addNode(NODE_NAME, "test:emptyContent");
        node.addMixin("test:dynamicValuesTestMixin");
        englishSession.save();
        englishSession.logout();
        refreshSession();

        checkPropertyValues(now, tomorrow, "Mixin");
    }

    private void checkPropertyValues(Calendar now, Calendar tomorrow, String propNameSuffix) throws RepositoryException {
        JCRNodeWrapper node = englishSession.getNode(NODE_PATH);

        // check if the properties were created
        assertFalse(node.hasProperty("propertyTextNotAutoCreated" + propNameSuffix));
        assertFalse(node.hasProperty("propertyTextNotAutoCreatedI18n" + propNameSuffix));
        assertTrue(node.hasProperty("propertyText" + propNameSuffix));
        assertTrue(node.hasProperty("propertyTextI18n" + propNameSuffix));
        assertTrue(node.hasProperty("propertyLong" + propNameSuffix));
        assertTrue(node.hasProperty("propertyDouble" + propNameSuffix));
        assertTrue(node.hasProperty("propertyDate" + propNameSuffix));
        assertTrue(node.hasProperty("propertyNow" + propNameSuffix));
        assertTrue(node.hasProperty("propertyTomorrow" + propNameSuffix));
        assertTrue(node.hasProperty("propertyCurrentUser" + propNameSuffix));
        assertTrue(node.hasProperty("propertyResourceBundleShared" + propNameSuffix));
        assertTrue(node.hasProperty("propertyResourceBundleI18n" + propNameSuffix));

        // check values for node properties
        assertEquals("Text plain", node.getProperty("propertyText" + propNameSuffix).getString());
        assertEquals("Text i18n", node.getProperty("propertyTextI18n" + propNameSuffix).getString());
        assertEquals(12, node.getProperty("propertyLong" + propNameSuffix).getLong());
        assertEquals(12.28, node.getProperty("propertyDouble" + propNameSuffix).getDouble(), 0.01);

        Calendar date = node.getProperty("propertyDate" + propNameSuffix).getDate();
        assertNotNull(date);
        assertEquals(1979, date.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, date.get(Calendar.MONTH));
        assertEquals(9, date.get(Calendar.DAY_OF_MONTH));

        Calendar dateNow = node.getProperty("propertyNow" + propNameSuffix).getDate();
        assertNotNull(dateNow);
        assertEquals(now.get(Calendar.YEAR), dateNow.get(Calendar.YEAR));
        assertEquals(now.get(Calendar.MONTH), dateNow.get(Calendar.MONTH));
        assertEquals(now.get(Calendar.DAY_OF_MONTH), dateNow.get(Calendar.DAY_OF_MONTH));

        Calendar dateTomorrow = node.getProperty("propertyTomorrow" + propNameSuffix).getDate();
        assertNotNull(dateTomorrow);
        assertEquals(tomorrow.get(Calendar.YEAR), dateTomorrow.get(Calendar.YEAR));
        assertEquals(tomorrow.get(Calendar.MONTH), dateTomorrow.get(Calendar.MONTH));
        assertEquals(tomorrow.get(Calendar.DAY_OF_MONTH), dateTomorrow.get(Calendar.DAY_OF_MONTH));

        assertEquals(englishSession.getUserID(), node.getProperty("propertyCurrentUser" + propNameSuffix).getString());
        assertEquals("Test value 1", node.getProperty("propertyResourceBundleShared" + propNameSuffix).getString());
        assertEquals("Test value 2", node.getProperty("propertyResourceBundleI18n" + propNameSuffix).getString());

        // test I18n french and german should have been auto created too
        JCRNodeWrapper frenchNode = frenchSession.getNode(node.getPath());
        JCRNodeWrapper germanNode = germanSession.getNode(node.getPath());

        assertFalse(frenchNode.hasProperty("propertyTextNotAutoCreatedI18n" + propNameSuffix));
        assertFalse(germanNode.hasProperty("propertyTextNotAutoCreatedI18n" + propNameSuffix));

        assertEquals("Text i18n", frenchNode.getProperty("propertyTextI18n" + propNameSuffix).getString());
        assertEquals("Text i18n", germanNode.getProperty("propertyTextI18n" + propNameSuffix).getString());

        assertEquals("Test value 1", germanNode.getProperty("propertyResourceBundleShared" + propNameSuffix).getString());
        assertEquals("Test value 1", frenchNode.getProperty("propertyResourceBundleShared" + propNameSuffix).getString());

        assertEquals("Valeur de test 2", frenchNode.getProperty("propertyResourceBundleI18n" + propNameSuffix).getString());
        assertEquals("Testwert 2", germanNode.getProperty("propertyResourceBundleI18n" + propNameSuffix).getString());
    }

    @Test
    public void testEnglishDefaultValues() throws Exception {
        createNode(englishSession);
        JCRNodeWrapper node = englishSession.getNode(NODE_PATH);
        assertEquals("Test value 2", node.getProperty("propertyResourceBundleI18n").getString());
    }

    @Test
    public void testFrenchDefaultValues() throws Exception {
        createNode(frenchSession);
        JCRNodeWrapper node = frenchSession.getNode(NODE_PATH);
        assertEquals("Valeur de test 2", node.getProperty("propertyResourceBundleI18n").getString());
    }

    @Test
    public void testGermanDefaultValues() throws Exception {
        createNode(germanSession);
        JCRNodeWrapper node = germanSession.getNode(NODE_PATH);
        assertEquals("Testwert 2", node.getProperty("propertyResourceBundleI18n").getString());
    }

    private void createNode(JCRSessionWrapper session) throws Exception {
        session.getNode(FOLDER_PATH).addNode(NODE_NAME, "test:dynamicValuesTest");
        session.save();
    }

    private void refreshSession() throws RepositoryException {
        englishSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        frenchSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.FRENCH);
        germanSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.GERMAN);
    }
}
