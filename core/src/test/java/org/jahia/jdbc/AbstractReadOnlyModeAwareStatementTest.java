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

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Base class for unit testing of read-only mode aware {@link Statement},
 * {@link java.sql.PreparedStatement} and {@link java.sql.CallableStatement}.
 *
 * @author cmoitrier
 */
@RunWith(JUnitParamsRunner.class)
public abstract class AbstractReadOnlyModeAwareStatementTest<S extends Statement> {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private ReadOnlyModeAwareConnection.ReadOnlyModeStatus readOnlyModeStatus;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Connection mockedConnection;

    private ReadOnlyModeAwareConnection connection;

    @Before
    public void before() {
        this.connection = new ReadOnlyModeAwareConnection(mockedConnection, readOnlyModeStatus);
    }

    @Test
    @TestCaseName("{method}({params})")
    @Parameters({
            "delete * from whatever",
            "insert into whatever (a\\, b\\, c) values ('x'\\, 'y'\\, 'z)",
            "update whatever set a = 'z'"
    })
    public final void testCannotWriteToDatabaseInReadOnlyMode(String query) throws SQLException {
        final S mockedStatement = mockStatement();
        testForbiddenOperationsInReadOnlyMode(query, mockedStatement);
    }

    @Test
    @TestCaseName("{method}({params})")
    @Parameters({
            "select * from whatever"
    })
    public final void testCanReadFromDatabaseInReadOnlyMode(String query) throws SQLException {
        final S mockedStatement = mockStatement();
        testAllowedOperationsInReadOnlyMode(query, mockedStatement);
    }

    protected void testForbiddenOperationsInReadOnlyMode(String query, S mockedStatement) throws SQLException {
        when(readOnlyModeStatus.isReadOnlyEnabled()).thenReturn(true);

        assertIsForbidden("execute(String)", query, (st, sql) -> st.execute(sql));
        assertIsForbidden("execute(String, int)", query, (st, sql) -> st.execute(sql, 0));
        assertIsForbidden("execute(String, int[])", query, (st, sql) -> st.execute(sql, new int[0]));
        assertIsForbidden("execute(String, String[])", query, (st, sql) -> st.execute(sql, new String[0]));
        assertIsForbidden("executeQuery(String)", query, (st, sql) -> st.executeQuery(sql));
        assertIsForbidden("executeUpdate(String)", query, (st, sql) -> st.executeUpdate(sql));
        assertIsForbidden("executeUpdate(String, int)", query, (st, sql) -> st.executeUpdate(sql, 0));
        assertIsForbidden("executeUpdate(String, int[]", query, (st, sql) -> st.executeUpdate(sql, new int[0]));
        assertIsForbidden("executeUpdate(String, String[]", query, (st, sql) -> st.executeUpdate(sql, new String[0]));
        assertIsForbidden("executeLargeUpdate(String)", query, (st, sql) -> st.executeLargeUpdate(sql));
        assertIsForbidden("executeLargeUpdate(String, int)", query, (st, sql) -> st.executeLargeUpdate(sql, 0));
        assertIsForbidden("executeLargeUpdate(String, int[])", query, (st, sql) -> st.executeLargeUpdate(sql, new int[0]));
        assertIsForbidden("executeLargeUpdate(String, String[])", query, (st, sql) -> st.executeLargeUpdate(sql, new String[0]));
        assertIsForbidden("executeBatch()", query, (st, sql) -> {
            st.addBatch("select * from whatever");
            st.addBatch(sql);
            st.executeBatch();
        });
        assertIsForbidden("executeLargeBatch()", query, (st, sql) -> {
            st.addBatch("select * from whatever");
            st.addBatch(sql);
            st.executeLargeBatch();
        });

        // verifies that executions are not forwarded to the underlying statement
        verify(mockedStatement, never()).execute(anyString());
        verify(mockedStatement, never()).execute(anyString(), anyInt());
        verify(mockedStatement, never()).execute(anyString(), any(int[].class));
        verify(mockedStatement, never()).execute(anyString(), any(String[].class));
        verify(mockedStatement, never()).executeQuery(anyString());
        verify(mockedStatement, never()).executeUpdate(anyString());
        verify(mockedStatement, never()).executeUpdate(anyString(), anyInt());
        verify(mockedStatement, never()).executeUpdate(anyString(), any(int[].class));
        verify(mockedStatement, never()).executeUpdate(anyString(), any(String[].class));
        verify(mockedStatement, never()).executeLargeUpdate(anyString());
        verify(mockedStatement, never()).executeLargeUpdate(anyString(), anyInt());
        verify(mockedStatement, never()).executeLargeUpdate(anyString(), any(int[].class));
        verify(mockedStatement, never()).executeLargeUpdate(anyString(), any(String[].class));
        verify(mockedStatement, never()).executeBatch();
        verify(mockedStatement, never()).executeLargeBatch();
    }

    protected void testAllowedOperationsInReadOnlyMode(String query, S mockedStatement) throws SQLException {
        when(readOnlyModeStatus.isReadOnlyEnabled()).thenReturn(true);

        assertIsAllowed("execute(String)", query, (st, sql) -> st.execute(sql));
        assertIsAllowed("execute(String, int)", query, (st, sql) -> st.execute(sql, 0));
        assertIsAllowed("execute(String, int[])", query, (st, sql) -> st.execute(sql, new int[0]));
        assertIsAllowed("execute(String, String[]", query, (st, sql) -> st.execute(sql, new String[0]));
        assertIsAllowed("executeQuery(String)", query, (st, sql) -> st.executeQuery(sql));
        assertIsAllowed("executeBatch()", query, (st, sql) -> {
            st.addBatch(sql);
            st.executeBatch();
        });
        assertIsAllowed("executeLargeBatch()", query, (st, sql) -> {
            st.addBatch(sql);
            st.executeLargeBatch();
        });

        // verifies that executions are forwarded to the underlying statement
        verify(mockedStatement, times(1)).execute(query);
        verify(mockedStatement, times(1)).execute(query, 0);
        verify(mockedStatement, times(1)).execute(query, new int[0]);
        verify(mockedStatement, times(1)).execute(query, new String[0]);
        verify(mockedStatement, times(1)).executeQuery(query);
        verify(mockedStatement, times(1)).executeBatch();
        verify(mockedStatement, times(1)).executeLargeBatch();
        verify(mockedStatement, times(2)).addBatch(query);
    }

    protected abstract S createStatement(Connection connection, String sql) throws SQLException;

    protected abstract S mockStatement() throws SQLException;

    protected final Connection getMockedConnection() {
        return mockedConnection;
    }

    protected final void assertIsForbidden(String description, String query, QueryHandler<S> handler) throws SQLException {
        final S statement = createStatement(connection, query);
        try {
            handler.execute(statement, query);
            fail(String.format("Query execution should have been prevented: %s", description));
        } catch (SQLException e) {
            if (!isQueryForbiddenInReadOnlyMode(e)) {
                throw new RuntimeException(e);
            }
        }
    }

    protected final void assertIsAllowed(String description, String query, QueryHandler<S> handler) throws SQLException {
        final S statement = createStatement(connection, query);
        try {
            handler.execute(statement, query);
        } catch (SQLException e) {
            if (isQueryForbiddenInReadOnlyMode(e)) {
                fail(String.format("Query should be allowed: %s", description));
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private static final boolean isQueryForbiddenInReadOnlyMode(SQLException e) {
        return "DataSource is in read-only mode".equals(e.getMessage());
    }

    @FunctionalInterface
    protected interface QueryHandler<S extends Statement> {
        void execute(S statement, String query) throws SQLException;
    }

}
