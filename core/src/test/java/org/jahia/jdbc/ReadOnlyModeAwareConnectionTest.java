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
package org.jahia.jdbc;

import org.jahia.jdbc.ReadOnlyModeAwareConnection.ReadOnlyAwareStatementHandler;
import org.jahia.jdbc.ReadOnlyModeAwareConnection.ReadOnlyModeStatus;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;

/**
 * Unit testing of {@link ReadOnlyModeAwareConnection}
 *
 * @author cmoitrier
 */
public final class ReadOnlyModeAwareConnectionTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private ReadOnlyModeStatus readOnlyModeStatus;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Connection mockedConnection;

    @Test
    public void testStatementsAreReadOnlyModeAware() throws SQLException {
        final ReadOnlyModeAwareConnection cnx = new ReadOnlyModeAwareConnection(mockedConnection, readOnlyModeStatus);

        assertIsReadOnlyModeAware(cnx.createStatement());
        assertIsReadOnlyModeAware(cnx.createStatement(0, 0));
        assertIsReadOnlyModeAware(cnx.createStatement(0, 0, 0));
    }

    @Test
    public void testPreparedStatementsAreReadOnlyModeAware() throws SQLException {
        final ReadOnlyModeAwareConnection cnx = new ReadOnlyModeAwareConnection(mockedConnection, readOnlyModeStatus);
        final String sql = "select * from whatever";

        assertIsReadOnlyModeAware(cnx.prepareStatement(sql));
        assertIsReadOnlyModeAware(cnx.prepareStatement(sql, 0));
        assertIsReadOnlyModeAware(cnx.prepareStatement(sql, 0, 0));
        assertIsReadOnlyModeAware(cnx.prepareStatement(sql, 0, 0, 0));
        assertIsReadOnlyModeAware(cnx.prepareStatement(sql, new int[0]));
        assertIsReadOnlyModeAware(cnx.prepareStatement(sql, new String[0]));
    }

    @Test
    public void testCallableStatementsAreReadOnlyModeAware() throws SQLException {
        final ReadOnlyModeAwareConnection cnx = new ReadOnlyModeAwareConnection(mockedConnection, readOnlyModeStatus);
        final String sql = "select * from whatever";

        assertIsReadOnlyModeAware(cnx.prepareCall(sql));
        assertIsReadOnlyModeAware(cnx.prepareCall(sql, 0, 0));
        assertIsReadOnlyModeAware(cnx.prepareCall(sql, 0, 0, 0));
    }

    private static void assertIsReadOnlyModeAware(Statement statement) {
        assertTrue("Statement is not a proxy", Proxy.isProxyClass(statement.getClass()));
        assertThat(Proxy.getInvocationHandler(statement), instanceOf(ReadOnlyAwareStatementHandler.class));
    }

}
