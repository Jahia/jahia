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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

/**
 * Unit testing of {@link CallableStatement} created by {@link ReadOnlyModeAwareConnection}
 *
 * @author cmoitrier
 */
public final class ReadOnlyModeAwareCallableStatementTest extends AbstractReadOnlyModeAwareStatementTest<CallableStatement> {

    @Override
    protected void testForbiddenOperationsInReadOnlyMode(String query, CallableStatement mockedStatement) throws SQLException {
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
    protected void testAllowedOperationsInReadOnlyMode(String query, CallableStatement mockedStatement) throws SQLException {
        super.testAllowedOperationsInReadOnlyMode(query, mockedStatement);

        assertIsAllowed("execute()", query, (st, sql) -> st.execute());
        assertIsAllowed("executeQuery()", query, (st, sql) -> st.executeQuery());

        // verifies that executions are forwarded to the underlying statement
        verify(mockedStatement, times(1)).execute();
        verify(mockedStatement, times(1)).executeQuery();
    }

    @Override
    protected CallableStatement createStatement(Connection connection, String sql) throws SQLException {
        return connection.prepareCall(sql);
    }

    @Override
    protected CallableStatement mockStatement() throws SQLException {
        final CallableStatement mock = mock(CallableStatement.class);
        when(getMockedConnection().prepareCall(anyString())).thenReturn(mock);
        return mock;
    }

}
