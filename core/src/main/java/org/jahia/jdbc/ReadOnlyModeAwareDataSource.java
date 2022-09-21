/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
