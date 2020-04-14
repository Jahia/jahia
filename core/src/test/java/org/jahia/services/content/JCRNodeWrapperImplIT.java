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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.jcr.*;
import javax.xml.transform.TransformerException;

import org.jahia.api.Constants;
import org.jahia.services.importexport.DocumentViewImportHandler;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.utils.TestHelper;
import org.jahia.utils.StringOutputStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class JCRNodeWrapperImplIT extends AbstractJUnitTest {

    private static final String SITE_NAME = JCRNodeWrapperImplIT.class.getSimpleName();
    private static final Locale LOCALE = Locale.ENGLISH;

    private static String siteKey;

    private JCRNodeWrapper siteNode;
    private Value[] strings;
    private Value[] dates;
    private Value[] doubles;
    private Value[] longs;
    private Value[] booleans;
    private Value[] weakRefs;
    private Value[] refs;

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        siteKey = TestHelper.createSite(SITE_NAME).getSiteKey();
    }

    @Override
    public void afterClassSetup() throws Exception {
        super.afterClassSetup();
        TestHelper.deleteSite(SITE_NAME);
    }

    @Before
    public void setUp() throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, LOCALE);
        siteNode = session.getNode("/sites/" + siteKey);

        ValueFactory valueFactory = siteNode.getSession().getValueFactory();
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(1000000000000L);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(2000000000000L);
        Calendar calendar3 = Calendar.getInstance();
        calendar3.setTimeInMillis(3000000000000L);

        strings = new Value[]{valueFactory.createValue("test1"), valueFactory.createValue("test2"), valueFactory.createValue("test3")};
        dates = new Value[]{valueFactory.createValue(calendar1), valueFactory.createValue(calendar2), valueFactory.createValue(calendar3)};
        doubles = new Value[]{valueFactory.createValue(1.1), valueFactory.createValue(2.2), valueFactory.createValue(3.3)};
        longs = new Value[]{valueFactory.createValue(1000000000000L), valueFactory.createValue(2000000000000L), valueFactory.createValue(3000000000000L)};
        booleans = new Value[]{valueFactory.createValue(true), valueFactory.createValue(false), valueFactory.createValue(true)};
        weakRefs = new Value[]{valueFactory.createValue(siteNode), valueFactory.createValue(siteNode.getSession().getNode("/users")), valueFactory.createValue(siteNode.getNode("files"))};
        refs = new Value[]{valueFactory.createValue(siteNode), valueFactory.createValue(siteNode.getSession().getNode("/users")), valueFactory.createValue(siteNode.getNode("files"))};
    }

    @After
    public void tearDown() {
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testRemoveMixin_mixinTranslationPropertiesShouldBeRemoved() throws RepositoryException {
        addMixinAndProperties("test:i18nMixin", "namedProperty", "unnamedProperty");
        siteNode.removeMixin("test:i18nMixin");
        Node translationNode = getTranslationNode();
        Assert.assertFalse(translationNode.hasProperty("namedProperty"));
        Assert.assertFalse(translationNode.hasProperty("unnamedProperty"));
    }

    @Test
    public void testRemoveMixin_mixinInheritedTranslationPropertiesShouldBeRemoved() throws RepositoryException {
        addMixinAndProperties("test:i18nMixinSubtype", "namedProperty", "unnamedProperty");
        siteNode.removeMixin("test:i18nMixinSubtype");
        Node translationNode = getTranslationNode();
        Assert.assertFalse(translationNode.hasProperty("namedProperty"));
        Assert.assertFalse(translationNode.hasProperty("unnamedProperty"));
    }

    @Test
    public void testRemoveMixin_mixinDuplicateTranslationPropertiesShouldBePreserved() throws RepositoryException {
        addMixinAndProperties("test:i18nMixin", "namedProperty", "unnamedProperty");
        addMixinAndProperties("test:i18nMixinSubtypeDuplicateProperties", "namedProperty", "unnamedProperty");
        siteNode.removeMixin("test:i18nMixin");
        Node translationNode = getTranslationNode();
        Assert.assertTrue(translationNode.hasProperty("namedProperty"));
        Assert.assertTrue(translationNode.hasProperty("unnamedProperty"));
    }

    @Test
    public void testRemoveMixin_translationOwnPropertiesShouldBePreserved() throws RepositoryException {
        addMixinAndProperties("test:i18nMixin", "namedProperty", "unnamedProperty");
        siteNode.removeMixin("test:i18nMixin");
        Node translationNode = getTranslationNode();
        Assert.assertTrue(translationNode.hasProperty(Constants.JCR_PRIMARYTYPE));
        Assert.assertTrue(translationNode.hasProperty(Constants.JCR_ISCHECKEDOUT));
    }

    @Test
    public void testRemoveMixin_translationAmbiguousPropertyShouldBePreserved() throws RepositoryException {
        addMixinAndProperties("test:i18nMixinAmbiguousProperty", Constants.JCR_LANGUAGE);
        siteNode.removeMixin("test:i18nMixinAmbiguousProperty");
        Assert.assertTrue(getTranslationNode().hasProperty(Constants.JCR_LANGUAGE));
    }

    @Test
    public void testMultipleValuedPropsOrderingUsingAPI() throws RepositoryException, TransformerException, SAXException, IOException {
        JCRNodeWrapper testNode = siteNode.addNode("testMultipleValuedAPI", "test:canSetProperty");

        testMultipleValuesOrdering(strings, testNode, "StringMultiple");
        testMultipleValuesOrdering(dates, testNode, "DateMultiple");
        testMultipleValuesOrdering(doubles, testNode, "DoubleMultiple");
        testMultipleValuesOrdering(longs, testNode, "LongMultiple");
        testMultipleValuesOrdering(booleans, testNode, "BooleanMultiple");
        testMultipleValuesOrdering(weakRefs, testNode, "WeakReferenceMultiple");
        testMultipleValuesOrdering(refs, testNode, "ReferenceMultiple");
    }

    @Test
    public void testMultipleValuedPropsOrderingUsingImportExport() throws Exception {
        JCRNodeWrapper testNode = siteNode.addNode("testMultipleValuedImportExport", "test:canSetProperty");

        testNode.setProperty("StringMultiple", strings);
        testNode.setProperty("DateMultiple", dates);
        testNode.setProperty("DoubleMultiple", doubles);
        testNode.setProperty("LongMultiple", longs);
        testNode.setProperty("BooleanMultiple", booleans);
        testNode.setProperty("WeakReferenceMultiple", weakRefs);
        testNode.setProperty("ReferenceMultiple", refs);

        testNode.getSession().save();

        // export the node
        final OutputStream exportOutputStream = new StringOutputStream();
        ImportExportBaseService.getInstance().exportNode(siteNode, siteNode.getSession().getNode("/"), exportOutputStream, new HashMap<>());

        TestHelper.deleteSite(SITE_NAME);

        // import the node
        ImportExportBaseService.getInstance().importXML("/", new ByteArrayInputStream(exportOutputStream.toString().getBytes(StandardCharsets.UTF_8)),
                DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE);

        JCRSessionWrapper sessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, LOCALE);
        sessionWrapper.flushCaches();
        siteNode = sessionWrapper.getNode("/sites/" + siteKey);
        testNode = siteNode.getNode("testMultipleValuedImportExport");

        testValuesArrayEquals(strings, testNode.getProperty("StringMultiple").getValues());
        testValuesArrayEquals(dates, testNode.getProperty("DateMultiple").getValues());
        testValuesArrayEquals(doubles, testNode.getProperty("DoubleMultiple").getValues());
        testValuesArrayEquals(longs, testNode.getProperty("LongMultiple").getValues());
        // TODO following cases are KO: https://jira.jahia.org/browse/QA-12037
        //testValuesArrayEquals(booleans, testNode.getProperty("BooleanMultiple").getValues());
        //testRefsArrayEquals(new String[] {"/sites/" + siteKey, "/users", "/sites/" + siteKey + "/files"}, testNode.getProperty("WeakReferenceMultiple").getValues());
        //testRefsArrayEquals(new String[] {"/sites/" + siteKey, "/users", "/sites/" + siteKey + "/files"}, testNode.getProperty("ReferenceMultiple").getValues());
    }

    private void testMultipleValuesOrdering(Value[] values, JCRNodeWrapper testNode, String propName) throws RepositoryException {
        for (Value value : values) {
            for (Value value1 : values) {
                for (Value value2 : values) {
                    Value[] finalValues = new Value[]{value, value1, value2};
                    testNode.setProperty(propName, finalValues);
                    testNode.getSession().save();

                    JCRValueWrapper[] savedValues = testNode.getProperty(propName).getValues();
                    testValuesArrayEquals(finalValues, savedValues);
                }
            }
        }
    }

    private void testRefsArrayEquals(String[] expectedNodePathValues, JCRValueWrapper[] actualValues) throws RepositoryException {
        for (int i = 0; i < actualValues.length; i++) {
            JCRValueWrapper savedValue = actualValues[i];

            Assert.assertEquals(expectedNodePathValues[i], savedValue.getNode().getPath());
        }
    }

    private void testValuesArrayEquals(Value[] expectedValues, Value[] actualValues) throws RepositoryException {
        for (int i = 0; i < actualValues.length; i++) {
            Value savedValue = actualValues[i];

            switch (savedValue.getType()){
                case PropertyType.REFERENCE:
                case PropertyType.WEAKREFERENCE:
                case PropertyType.STRING:
                    Assert.assertEquals(expectedValues[i].getString(), savedValue.getString());
                    break;
                case PropertyType.DATE:
                    Assert.assertEquals(expectedValues[i].getDate().getTimeInMillis(), savedValue.getDate().getTimeInMillis());
                    break;
                case PropertyType.DOUBLE:
                    Assert.assertEquals(expectedValues[i].getDouble(), savedValue.getDouble(), 0);
                    break;
                case PropertyType.LONG:
                    Assert.assertEquals(expectedValues[i].getLong(), savedValue.getLong());
                    break;
                case PropertyType.BOOLEAN:
                    Assert.assertEquals(expectedValues[i].getBoolean(), savedValue.getBoolean());
                    break;
            }
        }
    }

    private void addMixinAndProperties(String mixinName, String... propertyNames) throws RepositoryException {
        siteNode.addMixin(mixinName);
        for (String propertyName : propertyNames) {
            siteNode.setProperty(propertyName, "property-value");
            assert getTranslationNode().hasProperty(propertyName);
        }
    }

    private Node getTranslationNode() throws RepositoryException {
        return siteNode.getRealNode().getNode(JCRNodeWrapperImpl.getTranslationNodeName(LOCALE));
    }
}
