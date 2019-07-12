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

import org.jahia.settings.readonlymode.ReadOnlyModeCapable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link DataSource} implementation that prevents execution of queries
 * altering the database when read-only mode is enabled.
 *
 * @author cmoitrier
 */
public final class ReadOnlyModeAwareDataSource extends DelegatingDataSource implements ReadOnlyModeCapable {

    private static final int READ_ONLY_MODE_PRIORITY = -10;
    private static final Logger logger = LoggerFactory.getLogger(ReadOnlyModeAwareDataSource.class);
    private final AtomicBoolean readOnly = new AtomicBoolean(false);

    public ReadOnlyModeAwareDataSource(DataSource targetDataSource) {
        super(targetDataSource);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new ReadOnlyModeAwareConnection(super.getConnection(), () -> readOnly.get());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return new ReadOnlyModeAwareConnection(super.getConnection(username, password), () -> readOnly.get());
    }

    @Override
    public void switchReadOnlyMode(boolean enable) {
        logger.info("{} read-only mode...", (enable ? "Entering" : "Exiting"));
        this.readOnly.set(enable);
        logger.info("Read-only mode is now {}", (enable ? "enabled" : "disabled"));
    }

    @Override
    public int getReadOnlyModePriority() {
        // should be the latest one to switch to read-only and the first one to switch off
        return READ_ONLY_MODE_PRIORITY;
    }

}
