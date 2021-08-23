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
 *     Copyright (C) 2002-2021 Jahia Solutions Group. All rights reserved.
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
package org.jahia.services.content;

import org.drools.core.command.assertion.AssertEquals;
import org.jahia.api.Constants;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.utils.TestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import java.util.*;

/**
 * JCRNodeWrapperImpl unit test
 */
public class JCRPropertyWrapperImplTest extends AbstractJUnitTest {


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
        calendar1.setTimeInMillis(1000000000001L); // add microsecond when converting to ISO 8601 format
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(2000000000001L);
        Calendar calendar3 = Calendar.getInstance();
        calendar3.setTimeInMillis(3000000000001L);

        /*
         * Create test values with (strings,type) to bypass
         * AbstractValueFactory.createValue() and use EqualsFriendlierValue wrapper
         */
        strings = new Value[]{
                valueFactory.createValue("test1", PropertyType.STRING),
                valueFactory.createValue("test2", PropertyType.STRING),
                valueFactory.createValue("test3", PropertyType.STRING)};
        dates = new Value[]{
                valueFactory.createValue(calendar1.toInstant().toString(), PropertyType.DATE),
                valueFactory.createValue(calendar2.toInstant().toString(), PropertyType.DATE),
                valueFactory.createValue(calendar3.toInstant().toString(), PropertyType.DATE)
        };
        doubles = new Value[]{
                valueFactory.createValue("1.1", PropertyType.DOUBLE),
                valueFactory.createValue("2.2", PropertyType.DOUBLE),
                valueFactory.createValue("3.3", PropertyType.DOUBLE)
        };
        longs = new Value[] {
                valueFactory.createValue("1000000000000", PropertyType.LONG),
                valueFactory.createValue("2000000000000", PropertyType.LONG),
                valueFactory.createValue("3000000000000", PropertyType.LONG)
        };
        booleans = new Value[] {
                valueFactory.createValue("true", PropertyType.BOOLEAN),
                valueFactory.createValue("false", PropertyType.BOOLEAN)
        };
        weakRefs = new Value[]{
                valueFactory.createValue(siteNode, true),
                valueFactory.createValue(siteNode.getSession().getNode("/users"), true),
                valueFactory.createValue(siteNode.getNode("files"), true)
        };
        refs = new Value[]{
                valueFactory.createValue(siteNode),
                valueFactory.createValue(siteNode.getSession().getNode("/users")),
                valueFactory.createValue(siteNode.getNode("files"))
        };
    }

    @After
    public void tearDown() {
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testAddRemoveValues() throws RepositoryException {
        JCRNodeWrapper testNode = siteNode.addNode("testMultipleValuedAPI", "test:canSetProperty");

        testAddRemoveValues(strings, testNode, "StringMultiple");
        testAddRemoveValues(dates, testNode, "DateMultiple");
        testAddRemoveValues(doubles, testNode, "DoubleMultiple");
        testAddRemoveValues(longs, testNode, "LongMultiple");
        testAddRemoveValues(booleans, testNode, "BooleanMultiple");
        testAddRemoveValues(weakRefs, testNode, "WeakReferenceMultiple");
        testAddRemoveValues(refs, testNode, "ReferenceMultiple");
    }

    private void testAddRemoveValues(Value[] values, JCRNodeWrapper testNode, String propName) throws RepositoryException {
        Value[] addValues = values.clone();
        testNode.setProperty(propName, addValues);
        testNode.getSession().save();

        JCRValueWrapper[] savedValues;

        // add values to testNode propName
        savedValues = testNode.getProperty(propName).getValues();
        testValuesArrayEquals(addValues, savedValues);

        // remove one of the values
        List<Value> removeValues = new ArrayList(Arrays.asList(addValues));
        Value removedValue = removeValues.remove(removeValues.size() - 1);
        testNode.getProperty(propName).removeValue(removedValue);
        testNode.getSession().validate();
        testNode.getSession().save();

        // verify value has been removed
        savedValues = testNode.getProperty(propName).getValues();
        Assert.assertEquals(removeValues.size(), savedValues.length);
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
}
