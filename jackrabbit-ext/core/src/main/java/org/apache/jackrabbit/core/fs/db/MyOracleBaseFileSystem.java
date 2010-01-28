///**
// * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
// * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
// *
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License
// * as published by the Free Software Foundation; either version 2
// * of the License, or (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
// *
// * As a special exception to the terms and conditions of version 2.0 of
// * the GPL (or any later version), you may redistribute this Program in connection
// * with Free/Libre and Open Source Software ("FLOSS") applications as described
// * in Jahia's FLOSS exception. You should have received a copy of the text
// * describing the FLOSS exception, and it is also available here:
// * http://www.jahia.com/license
// *
// * Commercial and Supported Versions of the program
// * Alternatively, commercial and supported versions of the program may be used
// * in accordance with the terms contained in a separate written agreement
// * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
// * for your use, please contact the sales department at sales@jahia.com.
// */
//package org.apache.jackrabbit.core.fs.db;
//
//import java.io.BufferedReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.sql.DatabaseMetaData;
//import java.sql.ResultSet;
//import java.sql.Statement;
//
//import javax.jcr.RepositoryException;
//
//import org.apache.commons.io.IOUtils;
//import org.apache.jackrabbit.util.Text;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * This code is part of the patch submitted in :
// * https://issues.apache.org/jira/browse/JCR-1525
// * but not yet applied to the Jackrabbit source code. Once it will have
// * been included, we should remove the associated patches.
// *
// * <code>OracleBaseFileSystem</code> is a JDBC-based <code>FileSystem</code>
// * base implementation for Jackrabbit that persists file system entries in an
// * Oracle database.
// * <p/>
// * It is configured through the following properties:
// * <ul>
// * <li><code>driver</code>: the FQN name of the JDBC driver class
// * (default: <code>"oracle.jdbc.OracleDriver"</code>)</li>
// * <li><code>schema</code>: type of schema to be used
// * (default: <code>"oracle"</code>)</li>
// * <li><code>url</code>: the database url (e.g.
// * <code>"jdbc:oracle:thin:@[host]:[port]:[sid]"</code>)</li>
// * <li><code>user</code>: the database user</li>
// * <li><code>password</code>: the user's password</li>
// * <li><code>schemaObjectPrefix</code>: prefix to be prepended to schema objects</li>
// * <li><code>tableSpace</code>: the tablespace to use</li>
// * </ul>
// * See also {@link DbFileSystem}.
// * <p/>
// * The following is a fragment from a sample configuration:
// * <pre>
// *   &lt;FileSystem class="org.apache.jackrabbit.core.fs.db.Oracle*FileSystem"&gt;
// *       &lt;param name="url" value="jdbc:oracle:thin:@127.0.0.1:1521:orcl"/&gt;
// *       &lt;param name="user" value="scott"/&gt;
// *       &lt;param name="password" value="tiger"/&gt;
// *       &lt;param name="schemaObjectPrefix" value="rep_"/&gt;
// *       &lt;param name="tableSpace" value="default"/&gt;
// *  &lt;/FileSystem&gt;
// * </pre>
// */
//public class MyOracleBaseFileSystem extends DbFileSystem {
//    /**
//     * Logger instance
//     */
//    private static Logger log = LoggerFactory.getLogger(MyOracleBaseFileSystem.class);
//
//    /** the variable for the Oracle table space */
//    public static final String TABLE_SPACE_VARIABLE = "${tableSpace}";
//
//    /** the Oracle table space to use */
//    protected String tableSpace;
//
//    /**
//     * Creates a new <code>OracleBaseFileSystem</code> instance.
//     */
//    protected MyOracleBaseFileSystem() {
//        // preset some attributes to reasonable defaults
//        schema = "oracle";
//        driver = "oracle.jdbc.OracleDriver";
//        schemaObjectPrefix = "";
//        initialized = false;
//    }
//
//    /**
//     * Returns the configured Oracle table space.
//     * @return the configured Oracle table space.
//     */
//    public String getTableSpace() {
//        return tableSpace;
//    }
//
//    /**
//     * Sets the Oracle table space.
//     * @param tableSpace the Oracle table space.
//     */
//    public void setTableSpace(String tableSpace) {
//        if (tableSpace != null) {
//            this.tableSpace = tableSpace.trim();
//        } else {
//            this.tableSpace = null;
//        }
//    }
//
//    /**
//     * {@inheritDoc}
//     * <p/>
//     * Overridden in order to support multiple oracle schemas. Note that schema
//     * names in Oracle correspond to the username of the connection. See
//     * http://issues.apache.org/jira/browse/JCR-582
//     *
//     * @throws Exception if an error occurs
//     */
//    protected void checkSchema() throws Exception {
//        DatabaseMetaData metaData = con.getMetaData();
//        String tableName = schemaObjectPrefix + "FSENTRY";
//        if (metaData.storesLowerCaseIdentifiers()) {
//            tableName = tableName.toLowerCase();
//        } else if (metaData.storesUpperCaseIdentifiers()) {
//            tableName = tableName.toUpperCase();
//        }
//        String userName = metaData.getUserName();
//
//        ResultSet rs = metaData.getTables(null, userName, tableName, null);
//        boolean schemaExists;
//        try {
//            schemaExists = rs.next();
//        } finally {
//            rs.close();
//        }
//
//        if (!schemaExists) {
//            // read ddl from resources
//            InputStream in = OracleFileSystem.class.getResourceAsStream(schema + ".ddl");
//            if (in == null) {
//                String msg = "Configuration error: unknown schema '" + schema + "'";
//                log.debug(msg);
//                throw new RepositoryException(msg);
//            }
//            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//            Statement stmt = getDataSource().getConnection().createStatement();
//            try {
//                String sql = reader.readLine();
//                while (sql != null) {
//                    // Skip comments and empty lines
//                    if (!sql.startsWith("#") && sql.length() > 0) {
//                        // replace prefix variable
//                        sql = Text.replace(sql, SCHEMA_OBJECT_PREFIX_VARIABLE, schemaObjectPrefix);
//
//                        // set the tablespace if it is defined
//                        String tspace;
//                        if (tableSpace == null || "".equals(tableSpace)) {
//                            tspace = "";
//                        } else {
//                            tspace = "tablespace " + tableSpace;
//                        }
//                        sql = Text.replace(sql, TABLE_SPACE_VARIABLE, tspace).trim();
//
//                        // execute sql stmt
//                        stmt.executeUpdate(sql);
//                    }
//                    // read next sql stmt
//                    sql = reader.readLine();
//                }
//            } finally {
//                IOUtils.closeQuietly(in);
//                closeStatement(stmt);
//            }
//        }
//    }
//
//    /**
//     * Builds the SQL statements
//     * <p/>
//     * Since Oracle treats emtpy strings and BLOBs as null values the SQL
//     * statements had to be adapated accordingly. The following changes were
//     * necessary:
//     * <ul>
//     * <li>The distinction between file and folder entries is based on
//     * FSENTRY_LENGTH being null/not null rather than FSENTRY_DATA being
//     * null/not null because FSENTRY_DATA of a 0-length (i.e. empty) file is
//     * null in Oracle.</li>
//     * <li>Folder entries: Since the root folder has an empty name (which would
//     * be null in Oracle), an empty name is automatically converted and treated
//     * as " ".</li>
//     * </ul>
//     */
//    protected void buildSQLStatements() {
//        insertFileSQL = "insert into "
//                + schemaObjectPrefix + "FSENTRY "
//                + "(FSENTRY_PATH, FSENTRY_NAME, FSENTRY_DATA, "
//                + "FSENTRY_LASTMOD, FSENTRY_LENGTH) "
//                + "values (?, ?, ?, ?, ?)";
//
//        insertFolderSQL = "insert into "
//                + schemaObjectPrefix + "FSENTRY "
//                + "(FSENTRY_PATH, FSENTRY_NAME, FSENTRY_LASTMOD, FSENTRY_LENGTH) "
//                + "values (?, nvl(?, ' '), ?, null)";
//
//        updateDataSQL = "update "
//                + schemaObjectPrefix + "FSENTRY "
//                + "set FSENTRY_DATA = ?, FSENTRY_LASTMOD = ?, FSENTRY_LENGTH = ? "
//                + "where FSENTRY_PATH = ? and FSENTRY_NAME = ? "
//                + "and FSENTRY_LENGTH is not null";
//
//        updateLastModifiedSQL = "update "
//                + schemaObjectPrefix + "FSENTRY set FSENTRY_LASTMOD = ? "
//                + "where FSENTRY_PATH = ? and FSENTRY_NAME = ? "
//                + "and FSENTRY_LENGTH is not null";
//
//        selectExistSQL = "select 1 from "
//                + schemaObjectPrefix + "FSENTRY where FSENTRY_PATH = ? "
//                + "and FSENTRY_NAME = nvl(?, ' ')";
//
//        selectFileExistSQL = "select 1 from "
//                + schemaObjectPrefix + "FSENTRY where FSENTRY_PATH = ? "
//                + "and FSENTRY_NAME = ? and FSENTRY_LENGTH is not null";
//
//        selectFolderExistSQL = "select 1 from "
//                + schemaObjectPrefix + "FSENTRY where FSENTRY_PATH = ? "
//                + "and FSENTRY_NAME = nvl(?, ' ') and FSENTRY_LENGTH is null";
//
//        selectFileNamesSQL = "select FSENTRY_NAME from "
//                + schemaObjectPrefix + "FSENTRY where FSENTRY_PATH = ? "
//                + "and FSENTRY_LENGTH is not null";
//
//        selectFolderNamesSQL = "select FSENTRY_NAME from "
//                + schemaObjectPrefix + "FSENTRY where FSENTRY_PATH = ? "
//                + "and FSENTRY_NAME != ' ' "
//                + "and FSENTRY_LENGTH is null";
//
//        selectFileAndFolderNamesSQL = "select FSENTRY_NAME from "
//                + schemaObjectPrefix + "FSENTRY where FSENTRY_PATH = ? "
//                + "and FSENTRY_NAME != ' '";
//
//        selectChildCountSQL = "select count(FSENTRY_NAME) from "
//                + schemaObjectPrefix + "FSENTRY where FSENTRY_PATH = ?  "
//                + "and FSENTRY_NAME != ' '";
//
//        selectDataSQL = "select nvl(FSENTRY_DATA, empty_blob()) from "
//                + schemaObjectPrefix + "FSENTRY where FSENTRY_PATH = ? "
//                + "and FSENTRY_NAME = ? and FSENTRY_LENGTH is not null";
//
//        selectLastModifiedSQL = "select FSENTRY_LASTMOD from "
//                + schemaObjectPrefix + "FSENTRY where FSENTRY_PATH = ? "
//                + "and FSENTRY_NAME = nvl(?, ' ')";
//
//        selectLengthSQL = "select nvl(FSENTRY_LENGTH, 0) from "
//                + schemaObjectPrefix + "FSENTRY where FSENTRY_PATH = ? "
//                + "and FSENTRY_NAME = ? and FSENTRY_LENGTH is not null";
//
//        deleteFileSQL = "delete from "
//                + schemaObjectPrefix + "FSENTRY where FSENTRY_PATH = ? "
//                + "and FSENTRY_NAME = ? and FSENTRY_LENGTH is not null";
//
//        deleteFolderSQL = "delete from "
//                + schemaObjectPrefix + "FSENTRY where "
//                + "(FSENTRY_PATH = ? and FSENTRY_NAME = nvl(?, ' ') and FSENTRY_LENGTH is null) "
//                + "or (FSENTRY_PATH = ?) "
//                + "or (FSENTRY_PATH like ?) ";
//
//        copyFileSQL = "insert into "
//                + schemaObjectPrefix + "FSENTRY "
//                + "(FSENTRY_PATH, FSENTRY_NAME, FSENTRY_DATA, "
//                + "FSENTRY_LASTMOD, FSENTRY_LENGTH) "
//                + "select ?, ?, FSENTRY_DATA, "
//                + "FSENTRY_LASTMOD, FSENTRY_LENGTH from "
//                + schemaObjectPrefix + "FSENTRY where FSENTRY_PATH = ? "
//                + "and FSENTRY_NAME = ? and FSENTRY_LENGTH is not null";
//
//        copyFilesSQL = "insert into "
//                + schemaObjectPrefix + "FSENTRY "
//                + "(FSENTRY_PATH, FSENTRY_NAME, FSENTRY_DATA, "
//                + "FSENTRY_LASTMOD, FSENTRY_LENGTH) "
//                + "select ?, FSENTRY_NAME, FSENTRY_DATA, "
//                + "FSENTRY_LASTMOD, FSENTRY_LENGTH from "
//                + schemaObjectPrefix + "FSENTRY where FSENTRY_PATH = ? "
//                + "and FSENTRY_LENGTH is not null";
//    }
//}
