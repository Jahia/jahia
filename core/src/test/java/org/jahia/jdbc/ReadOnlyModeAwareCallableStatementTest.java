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
