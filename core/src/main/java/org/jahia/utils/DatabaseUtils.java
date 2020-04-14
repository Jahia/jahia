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
package org.jahia.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang.StringUtils;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.jahia.commons.DatabaseScripts;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.SpringContextSingleton;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

/**
 * Database related utility tool.
 *
 * @author Sergiy Shyrkov
 */
public final class DatabaseUtils {

    public static enum DatabaseType {
        derby, mssql, mysql, oracle, postgresql, mariadb;
    }

    public static final String TEST_TABLE = "jahia_nodetypes_provider";

    private static volatile DatabaseType dbType;
    private static volatile DataSource ds;

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtils.class);

    private static final Comparator<Resource> SCRIPT_RESOURCE_COMPARATOR = new Comparator<Resource>() {

        @Override
        public int compare(Resource resource1, Resource resource2) {
            try {
                return resource1.getFile().getCanonicalPath().compareTo(resource2.getFile().getCanonicalPath());
            } catch (IOException e) {
                throw new JahiaRuntimeException(e);
            }
        }
    };

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

    public static void executeScript(Resource sqlScript) throws SQLException, IOException {
        executeScript(new InputStreamReader(sqlScript.getInputStream(), Charsets.UTF_8));
    }

    public static void executeScript(Reader sqlScript) throws SQLException, IOException {
        executeStatements(DatabaseScripts.getScriptStatements(sqlScript));
    }

    public static void executeStatements(List<String> statements) throws SQLException {
        logger.info("Executing {} statement(s)...", statements.size());
        long startTime = System.currentTimeMillis();

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

            logger.info("Done executing {} statement(s) in {} ms", statements.size(),
                    System.currentTimeMillis() - startTime);
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

    /**
     * Check whether necessary database structures like tables/indices/triggers are present;
     * currently we simply check if the {@link #TEST_TABLE} table is present and assume everything else is present too if so.
     *
     * @return Whether necessary database structures are present
     */
    public static boolean isDatabaseStructureInitialized() {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    stmt.executeQuery("select count(*) from " + TEST_TABLE);
                } catch (SQLException e) {
                    return false;
                }
                return true;
            }
        } catch (SQLException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    /**
     * Create all the database structures like tables/indices/triggers.
     *
     * @param varDir The "var" directory of the DX installation
     * @param applicationContext The DX application context
     */
    public static void initializeDatabaseStructure(String varDir, ApplicationContext applicationContext) {
        try {
            Resource[] scripts = getScripts(varDir + "/db/sql/schema", applicationContext);
            for (Resource script : scripts) {
                String path = script.getFile().getCanonicalPath();
                logger.info("Excecuting {}...", path);
                long startAt = System.currentTimeMillis();
                executeScript(script);
                logger.info("Finished excecuting {} in {} ms", path, (System.currentTimeMillis() - startAt));
            }
        } catch (IOException | SQLException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    /**
     * Get all SQL scripts located within a base directory, as a resources.
     *
     * @param basePath The absolute path of the base directory to look for SQL script in
     * @param applicationContext The DX application context
     * @return All SQL scripts located within the base directory (along with sub-directories), as a resources, sorted by absolute script file path
     * @throws IOException in case of an error when retrieving SQL scripts
     */
    public static Resource[] getScripts(String basePath, ApplicationContext applicationContext) throws IOException {
        String folder = "file:" + basePath + '/' + getDatabaseType();
        if (applicationContext.getResource(folder).exists()) {
            Resource[] scripts = applicationContext.getResources(folder + "/**/*.sql");
            Arrays.sort(scripts, SCRIPT_RESOURCE_COMPARATOR);
            return scripts;
        } else {
            return new Resource[] {};
        }
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
