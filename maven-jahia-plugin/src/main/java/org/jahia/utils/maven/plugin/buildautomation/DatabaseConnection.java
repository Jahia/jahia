/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.utils.maven.plugin.buildautomation;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * desc:  It's a class used only by JahiaInstallation and
 * JahiaAdministration (they only have rights to establish direct connections
 * to a database and not via the connection pooling). This class mainly
 * provides you to open test a database connection and to execute sql queries.
 * All methods returns an <code>int</code>, and not ResultSet or anything
 * else. The principe is to check only status of connections, queries, etc.
 *
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author Alexandre Kraft
 * @version 1.0
 */
public class DatabaseConnection {


    private Statement theStatement;
    private Connection theConnection;

    /**
     * Default constructor.
     */
    public DatabaseConnection () {
        // do nothing :o)
    } // end constructor

    /**
     * Test a database connection, using settings defined by the user via
     * some inputs like db url, driver, script, etc. The method to test
     * used is to open the connection, try to create a table test in the
     * database and to drop this table. It also check if the user has
     * choosed the script for sqlserver and he have an access database, or,
     * on the opposite, if he have an access database and he choose the
     * sqlserver script.
     * An UTF-8 compliance test can be made by setting to true the
     * corresponding argument (see the configuration wizard).
     * The last test executed in this method is to check
     * if the database selected has already some jahia data inside.
     *
     * @param   script    The database script selected filename.
     * @param   driver    The JDBC driver for the database.
     * @param   url       The url where JDBC can found the database.
     * @param   username  The username required to access the database.
     * @param   password  The password required to access the database.
     * @param   runtimeSQL       The first line of the database script.
     * @param   checkUTF8Compliance  Enabled the UTF-8 test database.
     * @return  An Map containing two Booleans (one for the connection
     *          test and one for the data inside check (<code>true</code> if
     *          a test generate error(s), otherwise the return value is
     *          <code>false</code>) and a String (it's the error message, if
     *          checks don't generate error(s), the String is simply empty.
     */
    public Map databaseTest (String script,
                                 String driver,
                                 String url,
                                 String username,
                                 String password,
                                 String runtimeSQL,
                                 boolean checkUTF8Compliance,
                                 boolean createTables) {
        Map hashMap = new HashMap();
        hashMap.put("testDatabaseTablesAlreadyExists", Boolean.FALSE);
        hashMap.put("testDatabaseConnectionError", Boolean.FALSE);
        hashMap.put("testDatabaseConnectionMessage", "");

        // test to open a database connection...

        int testStatus = 2;

        try {
            databaseOpen(driver, url, username, password);
            testStatus = 0;
        } catch (ClassNotFoundException cnfe) {
            hashMap.put("testDatabaseConnectionError", Boolean.TRUE); // the connection generate error(s).
            hashMap.put("testDatabaseConnectionMessage",
                        "Driver class not found: " + driver +
                        cnfe.getLocalizedMessage());
            testStatus = 1;
        } catch (SQLException sqle) {
            hashMap.put("testDatabaseConnectionError", Boolean.TRUE); // the connection generate error(s).
            hashMap.put("testDatabaseConnectionMessage",
                        "Error while connecting to the database:" +
                        sqle.getLocalizedMessage());
            testStatus = 2;
        }

        // make other test only if the connection opened successfully...
        if (testStatus == 0) {
            // get the first table name in the script...
            String lowerCaseLine = runtimeSQL.toLowerCase();
            String runtimeTableName = "";
            int tableNamePos = lowerCaseLine.indexOf("create table");
            if (tableNamePos != -1) {
                runtimeTableName = runtimeSQL.substring("create table".length() +
                    tableNamePos,
                    runtimeSQL.indexOf("(")).trim();
            }
            String dbProductName = "";

            // in first, drop the table by security. if the table exists the results can be false...

            if (createTables) {
                databaseQuery("DROP TABLE " + runtimeTableName, true);

                // okay it's the time to test the *talk* by creating the table and dropping it...
                testStatus = databaseQuery(runtimeSQL.toString(), false);
            }

            // check the UTF-8 compliance only if the table was created successfully.
            if ((testStatus == 0) && (checkUTF8Compliance)){
                // Create 'random' string with Latin, Cyrillic and Chinese characters
                // and insert them to the database.
                // My chinese knowledges are limited but I think that '\u8bed\u8a00'
                // characters mean 'language'   :-)
                String testField = "Latin : Ma\u00ffliss \u00e9\u00e0\u00fc\u00f6\u00a7\u00a3 / Cyrillic : \u0419\u0416 / Chinese : \u8bed\u8a00";
                // FIXME : For coding simplicity 'testfield' is hardcoded here. :(
                try {
                    PreparedStatement insertStatement = theConnection.prepareStatement("INSERT INTO " + runtimeTableName + "(testfield) VALUES(?)");
                    insertStatement.setString(1, testField);
                    insertStatement.execute();
                    ResultSet rs = theStatement.executeQuery("SELECT testfield FROM jahia_db_test");
                    if (rs.next()) {
                        String testFieldResult = rs.getString("testfield");
                        if (!testFieldResult.equals(testField)) {
                            testStatus++;
                            hashMap.put("testDatabaseConnectionError", Boolean.TRUE);
                            hashMap.put("testDatabaseConnectionMessage", "This database doesn't seem to support extended charsets");
                            return hashMap;
                        }
                    }
                } catch (SQLException sqle) {
                    testStatus++;

                    hashMap.put("testDatabaseConnectionError", Boolean.TRUE); // the connection generate error(s).
                    hashMap.put("testDatabaseConnectionMessage", "This database doesn't seem to support extended charsets");
                }
            }

            if (createTables) {
                testStatus += databaseQuery("DROP TABLE " + runtimeTableName, false);
            }

            // last tests...
            if (testStatus == 0) {
                try {
                    DatabaseMetaData dbMetaData = theConnection.getMetaData();
                    dbProductName = dbMetaData.getDatabaseProductName().trim();

                } catch (SQLException sqle) { // cannot get the metadata from this database (or the product name)...
                }

                // check if the user has selected sqlserver or access and if it's really this database...
                if (script.equals("sqlserver.script") &&
                    !dbProductName.equals("Microsoft SQL Server")) {
                    testStatus = 1;
                } else if (script.equals("msaccess.script") &&
                           !dbProductName.equals("ACCESS")) {
                    testStatus = 1;
                } else {
                    // FIXME : Is this test still necessary ?
                    // 'testDatabaseTablesAlreadyExists' entry is never used...
                    if (databaseQuery("SELECT * FROM " + runtimeTableName, true) ==
                        0) { // check if the database is already jahia-ified :o)
                        hashMap.put("testDatabaseTablesAlreadyExists",
                                    Boolean.TRUE);
                    }
                }
            }
        }

        // okay all tests executed...
        if (testStatus == 0) {

            hashMap.put("testDatabaseConnectionError", Boolean.FALSE);
        } else {
            Boolean hasError = (Boolean)hashMap.get("testDatabaseConnectionError");
            if ( ((hasError != null) && (hasError.booleanValue() == false)) ||
                 (hasError == null)
                 ){
                hashMap.put("testDatabaseConnectionError", Boolean.TRUE);
                hashMap.put("testDatabaseConnectionMessage",
                            "Can't talk with the database. Check your settings.");
            }
        }

        return hashMap;
    } // end databaseTest

    /**
     * Open a database connection, using settings defined by the user like
     * the database driver, the url, username and password.
     *
     * @param   driver    The JDBC driver for the database.
     * @param   url       The url where JDBC can found the database.
     * @param   username  The username required to access the database.
     * @param   password  The password required to access the database.
     * @return  <code>0</code> if the connection is open, <code>1</code> if the
     *          driver class is not found, or <code>2</code> if the connection
     *          cannot be opened.
     */
    public void databaseOpen (String driver,
                              String url,
                              String username,
                              String password)
        throws ClassNotFoundException, SQLException {
        // always close a possibly old connection before open a new...
        databaseClose();

        // try to open a database connection...
        Class.forName(driver);
        theConnection = DriverManager.getConnection(url, username, password);
        theStatement = theConnection.createStatement();
    } // end databaseOpen

    /**
     * Execute a SQL query. Be careful, this method don't return an ResultSet.
     *
     * @param   sqlCode     The sql query you want to execute.
     * @return  <code>0</code> if the query generates no error(s) or <code>1</code>
     *          if there is an exception.
     */
    public int databaseQuery (String sqlCode, boolean quietErrors) {
        try {
            theStatement.execute(sqlCode);
            return 0;
        } catch (Exception e) {
            if (!quietErrors) {

            }
            return 1;
        }
    } // end databaseQuery

    /**
     * Execute a SQL query. Be careful, this method don't return an ResultSet.
     *
     * @param   sqlCode     The sql query you want to execute.
     *
     * @throws  Exception   Propagate any exceptions
     */
    public void query (String sqlCode)
        throws Exception {
        theStatement.execute(sqlCode);
    } // end query

    public void queryPreparedStatement(String sqlCode, Object[] params)
        throws Exception {
        PreparedStatement ps = theConnection.prepareStatement(sqlCode);
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i+1,params[i]);
        }
        ps.execute();
    } // end query

    /**
     * Close the current database connection. If the connection statement do
     * not exists, the exception is simply catched. There is no problem about
     * this and completely transparent for the user.
     */
    public void databaseClose () {
        try {
            theStatement.close();
        } catch (SQLException sqle) {
        } catch (NullPointerException sqle) {
        }
    } // end databaseClose

    /**
     * Get the connection of this instance of the class.
     *
     * @return  The connection of this instance of the class
     */
    public Connection getConnection () {
        return theConnection;
    } // end getConnection

    /**
     * Get the statement of this instance of the class.
     *
     * @return  The statement of this instance of the class
     */
    public Statement getStatement () {
        return theStatement;
    } // end getStatement

} // end DatabaseConnection
