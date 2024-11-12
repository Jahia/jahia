/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.sql.DataSource;
import java.sql.*;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;

/**
 * Unit testing of {@link ReadOnlyModeAwareDataSource}
 *
 * @author cmoitrier
 */
public final class ReadOnlyModeAwareDataSourceTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSource mockedDataSource;

    @Test
    public void testConnectionsAreReadOnlyModeAware() throws SQLException {
        final ReadOnlyModeAwareDataSource dataSource = new ReadOnlyModeAwareDataSource(mockedDataSource);

        assertIsReadOnlyModeAware(dataSource.getConnection());
        assertIsReadOnlyModeAware(dataSource.getConnection("x", "y"));
    }

    @Test
    public void testEnableReadOnly() throws SQLException {
        final ReadOnlyModeAwareDataSource dataSource = new ReadOnlyModeAwareDataSource(mockedDataSource);
        final Connection cnx1 = dataSource.getConnection();
        final Connection cnx2 = dataSource.getConnection("x", "y");

        dataSource.switchReadOnlyMode(true);

        assertTrue("cnx1 is not read-only", ((ReadOnlyModeAwareConnection) cnx1).status.isReadOnlyEnabled());
        assertTrue("cnx2 is not read-only", ((ReadOnlyModeAwareConnection) cnx2).status.isReadOnlyEnabled());
    }

    @Test
    public void testDisableReadOnly() throws SQLException {
        final ReadOnlyModeAwareDataSource dataSource = new ReadOnlyModeAwareDataSource(mockedDataSource);
        final Connection cnx1 = dataSource.getConnection();
        final Connection cnx2 = dataSource.getConnection("x", "y");

        dataSource.switchReadOnlyMode(false);

        assertFalse("cnx1 is read-only", ((ReadOnlyModeAwareConnection) cnx1).status.isReadOnlyEnabled());
        assertFalse("cnx2 is read-only", ((ReadOnlyModeAwareConnection) cnx2).status.isReadOnlyEnabled());
    }

    private void assertIsReadOnlyModeAware(Connection connection) {
        assertThat(connection, instanceOf(ReadOnlyModeAwareConnection.class));
    }

}
