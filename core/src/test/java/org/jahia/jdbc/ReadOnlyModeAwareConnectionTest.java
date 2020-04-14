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
