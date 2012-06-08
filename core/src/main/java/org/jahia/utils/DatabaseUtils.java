/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.lob.ReaderInputStream;
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

    private static DatabaseType dbType;

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

    public static void executeStatements(List<String> statements) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getDatasource().getConnection();
            stmt = conn.createStatement();
            for (String query : statements) {
                try {
                    stmt.execute(query);
                } catch (SQLException e) {
                    String lcLine = query.toLowerCase();
                    if (!lcLine.contains("drop table") && !lcLine.contains("drop trigger")
                            && !lcLine.contains("drop sequence")) {
                        logger.error(
                                "Unable to execute query: " + query + ". Cause: " + e.getMessage(),
                                e);
                        throw e;
                    }
                }
            }
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
            dbType = DatabaseType.valueOf(StringUtils.substringBefore(
                    StringUtils.substringBefore(SettingsBean.getInstance().getPropertiesFile()
                            .getProperty("db_script").trim(), "."), "_"));
        }
        return dbType;
    }

    public static DataSource getDatasource() {
        return (DataSource) SpringContextSingleton.getBean("dataSource");
    }

    public static SessionFactory getHibernateSessionFactory() {
        return (SessionFactory) SpringContextSingleton.getBean("sessionFactory");
    }

    public static List<String> getScriptStatements(File scriptFile) throws IOException {
        return getScriptStatements(new BufferedInputStream(new FileInputStream(scriptFile)));
    }

    public static List<String> getScriptStatements(InputStream is) throws IOException {
        List<String> statements = new LinkedList<String>();
        try {
            for (String line : IOUtils.readLines(is)) {
                if (StringUtils.isBlank(line) || line.trim().startsWith("#")
                        || line.trim().startsWith("--")) {
                    continue;
                }
                line = line.trim();
                if (line.endsWith(";") && !line.endsWith("end;")) {
                    line = line.substring(0, line.length() - 1);
                }
                statements.add(line);
            }
        } finally {
            IOUtils.closeQuietly(is);
        }

        return statements;
    }

    public static List<String> getScriptStatements(String scriptContent) throws IOException {
        return getScriptStatements(new ReaderInputStream(new StringReader(scriptContent)));
    }

    private DatabaseUtils() {
        super();
    }
}
