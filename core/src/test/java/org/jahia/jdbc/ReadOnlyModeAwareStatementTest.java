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

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit testing of {@link Statement} created by {@link ReadOnlyModeAwareConnection}
 *
 * @author cmoitrier
 */
public final class ReadOnlyModeAwareStatementTest {

    // A list of queries not allowed in read-only mode
    private static final List<String> WRITE_QUERIES = Collections.unmodifiableList(Arrays.asList(
            "insert into whatever (a, b, c) values ('x', 'y', 'z)",
            "delete * from whatever",
            "update whatever set a = 'z'"
    ));

    // A list of queries always allowed
    private static final List<String> READ_QUERIES = Collections.unmodifiableList(Arrays.asList(
            "select * from whatever"
    ));

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private ReadOnlyModeAwareConnection.ReadOnlyModeStatus readOnlyModeStatus;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Connection mockedConnection;

    @Test
    public void testCannotWriteToDatabaseInReadOnlyMode() throws SQLException {
        when(readOnlyModeStatus.isReadOnlyEnabled()).thenReturn(true);

        final Statement mockedStatement = mock(Statement.class);
        when(mockedConnection.createStatement()).thenReturn(mockedStatement);

        final ReadOnlyModeAwareConnection cnx = new ReadOnlyModeAwareConnection(mockedConnection, readOnlyModeStatus);

        assertCannotWrite("execute(String)", sql -> cnx.createStatement().execute(sql));
        assertCannotWrite("execute(String, int)", sql -> cnx.createStatement().execute(sql, 0));
        assertCannotWrite("execute(String, int[])", sql -> cnx.createStatement().execute(sql, new int[0]));
        assertCannotWrite("execute(String, String[])", sql -> cnx.createStatement().execute(sql, new String[0]));
        assertCannotWrite("executeQuery(String)", sql -> cnx.createStatement().executeQuery(sql));
        assertCannotWrite("executeUpdate(String)", sql -> cnx.createStatement().executeUpdate(sql));
        assertCannotWrite("executeUpdate(String, int)", sql -> cnx.createStatement().executeUpdate(sql, 0));
        assertCannotWrite("executeUpdate(String, int[]", sql -> cnx.createStatement().executeUpdate(sql, new int[0]));
        assertCannotWrite("executeUpdate(String, String[]", sql -> cnx.createStatement().executeUpdate(sql, new String[0]));
        assertCannotWrite("executeLargeUpdate(String)", sql -> cnx.createStatement().executeLargeUpdate(sql));
        assertCannotWrite("executeLargeUpdate(String, int)", sql -> cnx.createStatement().executeLargeUpdate(sql, 0));
        assertCannotWrite("executeLargeUpdate(String, int[])", sql -> cnx.createStatement().executeLargeUpdate(sql, new int[0]));
        assertCannotWrite("executeLargeUpdate(String, String[])", sql -> cnx.createStatement().executeLargeUpdate(sql, new String[0]));
        assertCannotWrite("executeBatch()", sql -> {
            Statement st = cnx.createStatement();
            st.addBatch("select * from whatever");
            st.addBatch(sql);
            st.executeBatch();
        });
        assertCannotWrite("executeLargeBatch()", sql -> {
            Statement st = cnx.createStatement();
            st.addBatch("select * from whatever");
            st.addBatch(sql);
            st.executeLargeBatch();
        });

        // verify that query execution is not forwarded to the underlying statement
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

    @Test
    public void testCanReadFromDatabaseInReadOnlyMode() {
        when(readOnlyModeStatus.isReadOnlyEnabled()).thenReturn(true);

        final ReadOnlyModeAwareConnection cnx = new ReadOnlyModeAwareConnection(mockedConnection, readOnlyModeStatus);

        assertCanRead("execute(String)", sql -> cnx.createStatement().execute(sql));
        assertCanRead("execute(String, int)", sql -> cnx.createStatement().execute(sql, 0));
        assertCanRead("execute(String, int[])", sql -> cnx.createStatement().execute(sql, new int[0]));
        assertCanRead("execute(String, String[]", sql -> cnx.createStatement().execute(sql, new String[0]));
        assertCanRead("executeQuery(String)", sql -> cnx.createStatement().executeQuery(sql));
        assertCanRead("executeBatch()", sql -> {
            Statement st = cnx.createStatement();
            st.addBatch(sql);
            st.executeBatch();
        });
        assertCanRead("executeLargeBatch()", sql -> {
            Statement st = cnx.createStatement();
            st.addBatch(sql);
            st.executeLargeBatch();
        });
    }

    private static void assertCannotWrite(String description, QueryHandler handler) {
        WRITE_QUERIES.stream().forEach(query -> assertIsForbidden(description, handler, query));
    }

    private static void assertCanRead(String description, QueryHandler handler) {
        READ_QUERIES.stream().forEach(query -> assertIsAllowed(description, handler, query));
    }

    private static void assertIsForbidden(String description, QueryHandler handler, String query) {
        try {
            handler.execute(query);
            fail(String.format("[%s][%s] Query execution should have been prevented", description, query));
        } catch (SQLException e) {
            if (!isQueryForbiddenInReadOnlyMode(e)) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void assertIsAllowed(String description, QueryHandler handler, String query) {
        try {
            handler.execute(query);
        } catch (SQLException e) {
            if (isQueryForbiddenInReadOnlyMode(e)) {
                fail(String.format("[%s][%s] Query should be allowed", description, query));
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean isQueryForbiddenInReadOnlyMode(SQLException e) {
        return "DataSource is in read-only mode".equals(e.getMessage());
    }

    @FunctionalInterface
    private interface QueryHandler {
        void execute(String sql) throws SQLException;
    }

}
