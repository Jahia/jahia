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
