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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

/**
 * Unit testing of {@link PreparedStatement} created by {@link ReadOnlyModeAwareConnection}
 *
 * @author cmoitrier
 */
public final class ReadOnlyModeAwarePreparedStatementTest extends AbstractReadOnlyModeAwareStatementTest<PreparedStatement> {

    @Override
    protected void testForbiddenOperationsInReadOnlyMode(String query, PreparedStatement mockedStatement) throws SQLException {
        super.testForbiddenOperationsInReadOnlyMode(query, mockedStatement);

        assertIsForbidden("execute()", query, (st, sql) -> st.execute());
        assertIsForbidden("executeQuery()", query, (st, sql) -> st.executeQuery());
        assertIsForbidden("executeUpdate()", query, (st, sql) -> st.executeUpdate());

        // verifies that executions are not forwarded to the underlying statement
        verify(mockedStatement, never()).execute();
        verify(mockedStatement, never()).executeQuery();
        verify(mockedStatement, never()).executeUpdate();
    }

    @Override
    protected void testAllowedOperationsInReadOnlyMode(String query, PreparedStatement mockedStatement) throws SQLException {
        super.testAllowedOperationsInReadOnlyMode(query, mockedStatement);

        assertIsAllowed("execute()", query, (st, sql) -> st.execute());
        assertIsAllowed("executeQuery()", query, (st, sql) -> st.executeQuery());

        // verifies that executions are forwarded to the underlying statement
        verify(mockedStatement, times(1)).execute();
        verify(mockedStatement, times(1)).executeQuery();
    }

    @Override
    protected PreparedStatement createStatement(Connection connection, String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    @Override
    protected PreparedStatement mockStatement() throws SQLException {
        final PreparedStatement mock = mock(PreparedStatement.class);
        when(getMockedConnection().prepareStatement(anyString())).thenReturn(mock);
        return mock;
    }

}
