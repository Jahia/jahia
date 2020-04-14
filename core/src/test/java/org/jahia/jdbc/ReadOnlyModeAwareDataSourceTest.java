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
