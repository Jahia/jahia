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
package org.jgroups.protocols;

import org.jgroups.annotations.Property;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.util.Util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Jahia specific version of the {@link JDBC_PING} protocol which set auto-commit on the connection to true.
 * 
 * @author Sergiy Shyrkov
 */
@SuppressWarnings("java:S101")
public class JAHIA_JDBC_PING extends JDBC_PING {

    private enum AUTO_COMMIT {
        CHECK, ENFORCE, SKIP;
    }

    public static final short PROTOCOL_ID = (short) 513;

    static {
        ClassConfigurator.addProtocol(PROTOCOL_ID, JAHIA_JDBC_PING.class);
    }

    @Property(description = "Specifies the auto-commit handling on the JDBC connection, when it is obtained from a JNDI datasource and when it returned back. "
            + "If set to 'enforce' (this is the default value) the auto-commit is always set to true. "
            + "The 'check' first checks if the auto-commit is false and only in this case sets it to true. "
            + "The 'skip' value bypasses special handling and leaves auto-commit as it is")
    @SuppressWarnings("java:S116")
    protected String auto_commit = "enforce";

    private AUTO_COMMIT autoCommitMode = AUTO_COMMIT.ENFORCE;

    @Override
    protected void closeConnection(Connection connection) {
        super.closeConnection(ensureAutoCommit(connection));
    }

    private Connection ensureAutoCommit(Connection connection) {
        if (connection == null || AUTO_COMMIT.SKIP == autoCommitMode) {
            return connection;
        }
        try {
            if (AUTO_COMMIT.ENFORCE == autoCommitMode || !connection.getAutoCommit()) {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.warn("Unable to check/set autocommit on connection", e);
            } else {
                log.warn("Unable to check/set autocommit on connection. Cause: " + e.getMessage());
            }
        }

        return connection;
    }

    @Override
    protected Connection getConnection() {
        return ensureAutoCommit(super.getConnection());
    }

    @Override
    public void init() throws Exception {
        super.init();

        if (datasource_jndi_name == null || datasource_jndi_name.trim().length() == 0) {
            autoCommitMode = AUTO_COMMIT.SKIP;
            auto_commit = "skip";
        } else if (auto_commit != null) {
            autoCommitMode = AUTO_COMMIT.valueOf(auto_commit.trim().toUpperCase());
        }
    }

    @Override
    protected byte[] serializeWithoutView(PingData data) {
        final JahiaPingData extendedData = new JahiaPingData(data, System.getProperty("cluster.hazelcast.bindPort"));
        try {
            return Util.streamableToByteBuffer(extendedData);
        } catch (Exception e) {
            log.error("Error", e);
            return new byte[0];
        }
    }
}
