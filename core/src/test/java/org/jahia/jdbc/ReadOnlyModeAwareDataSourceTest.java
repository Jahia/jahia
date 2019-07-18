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
