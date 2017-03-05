/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.utils;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.jahia.commons.DatabaseScripts;
import org.jahia.services.SpringContextSingleton;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database related utility tool.
 *
 * @author Sergiy Shyrkov
 */
public final class DatabaseUtils {

    public static enum DatabaseType {
        derby, mssql, mysql, oracle, postgresql;
    }

    private static volatile DatabaseType dbType;
    private static volatile DataSource ds;

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtils.class);

    public static void closeQuietly(Object closable) {
        if (closable == null) {
            return;
        }
        try {
            if (closable instanceof Connection) {
                ((Connection) closable).close();
            } else if (closable instanceof Statement) {
                ((Statement) closable).close();
            } else if (closable instanceof ResultSet) {
                ((ResultSet) closable).close();
            } else if (closable instanceof ScrollableResults) {
                ((ScrollableResults) closable).close();
            } else if (closable instanceof Session) {
                ((Session) closable).close();
            } else if (closable instanceof StatelessSession) {
                ((StatelessSession) closable).close();
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.warn(e.getMessage(), e);
            } else {
                logger.warn(e.getMessage());
            }

        }
    }

    public static void executeScript(Reader sqlScript) throws SQLException, IOException {
        executeStatements(DatabaseScripts.getScriptStatements(sqlScript));
    }

    public static void executeStatements(List<String> statements) throws SQLException {
        Connection conn = null;
        try {
            conn = getDatasource().getConnection();
            DatabaseScripts.executeStatements(statements, conn);
            if (!conn.getAutoCommit()) {
                conn.commit();
            }
        } catch (SQLException e) {
            if (conn != null && !conn.getAutoCommit()) {
                conn.rollback();
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    public static int executeUpdate(String query) throws SQLException {
        int result = 0;
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getDatasource().getConnection();
            stmt = conn.createStatement();
            result = stmt.executeUpdate(query);
            if (!conn.getAutoCommit()) {
                conn.commit();
            }
        } catch (SQLException e) {
            if (conn != null && !conn.getAutoCommit()) {
                conn.rollback();
            }
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        return result;
    }

    public static DatabaseType getDatabaseType() {
        if (dbType == null) {
            synchronized (DatabaseUtils.class) {
                if (dbType == null) {
                    dbType = DatabaseType.valueOf(StringUtils.substringBefore(
                            StringUtils.substringBefore(SettingsBean.getInstance().getPropertiesFile().getProperty("db_script")
                                    .trim(), "."), "_"));
                }
            }
        }
        return dbType;
    }

    public static DataSource getDatasource() {
        if (ds == null) {
            synchronized (DatabaseUtils.class) {
                if (ds == null) {
                    ds = (DataSource) SpringContextSingleton.getBean("dataSource");
                }
            }
        }
        return ds;
    }

    public static ScrollMode getFirstSupportedScrollMode(ScrollMode fallback, ScrollMode... scrollModesToTest) {

        ScrollMode supportedMode = null;
        Connection conn = null;
        try {
            conn = getDatasource().getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            for (ScrollMode scrollMode : scrollModesToTest) {
                if (metaData.supportsResultSetType(scrollMode.toResultSetType())) {
                    supportedMode = scrollMode;
                    break;
                }
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.warn("Unlable to check supported scrollable resultset type. Cause: " + e.getMessage(), e);
            } else {
                logger.warn("Unlable to check supported scrollable resultset type. Cause: " + e.getMessage());
            }
        } finally {
            closeQuietly(conn);
        }

        return supportedMode != null ? supportedMode : fallback;

    }

    public static SessionFactory getHibernateSessionFactory() {
        return (SessionFactory) SpringContextSingleton.getBean("sessionFactory");
    }

    public static void setDatasource(DataSource ds) {
        DatabaseUtils.ds = ds;
    }

    private DatabaseUtils() {
        super();
    }
}
