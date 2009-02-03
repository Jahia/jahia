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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.bin;

import org.hsqldb.Server;
import org.hsqldb.DatabaseManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

public class HypersonicLauncherListener implements ServletContextListener {

    String args[] = new String[10];
    Thread serverThread;
    Server hsqldbServer;
    String dbLocation;
    String dbPortStr;
    String dbSilent;
    String dbTrace;
    String jahiaConfigFileName;
    boolean startHSQLServer = false;

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        jahiaConfigFileName = servletContext.getInitParameter("jahia.config");
        servletContext.log("jahia.config=" + jahiaConfigFileName);
        String realJahiaConfigFileName = servletContext.getRealPath(jahiaConfigFileName);
        File jahiaConfigFile = new File(realJahiaConfigFileName);
        if (jahiaConfigFile.exists()) {
            Properties jahiaConfigProps = new Properties();
            try {
                jahiaConfigProps.load(new FileInputStream(jahiaConfigFile));
                String startHSQLServerStr = jahiaConfigProps.getProperty("db_starthsqlserver");
                if (startHSQLServerStr == null) {
                    startHSQLServerStr = "false";
                }
                startHSQLServer = Boolean.valueOf(startHSQLServerStr).booleanValue();
                if (!startHSQLServer) {
                    servletContext.log("HSQL server will not be started.");
                    return;
                }

            } catch (FileNotFoundException fnfe) {
                servletContext.log("Problem finding file " + jahiaConfigFileName, fnfe);
            } catch (IOException ioe) {
                servletContext.log("Problem while loading file " + jahiaConfigFileName, ioe);
            }
        }

        dbLocation = servletContext.getInitParameter("hsqldb.location");
        dbPortStr = servletContext.getInitParameter("hsqldb.port");
        dbSilent = servletContext.getInitParameter("hsqldb.silent");
        dbTrace = servletContext.getInitParameter("hsqldb.trace");

        servletContext.log("dbLocation=" + dbLocation);
        String realPath = servletContext.getRealPath(dbLocation);
        servletContext.log("realPath = " + realPath);

        hsqldbServer = new Server();
        hsqldbServer.setDatabaseName(0,"hsqldbjahia");
        hsqldbServer.setDatabasePath(0, "file:" + realPath);
        hsqldbServer.setAddress("127.0.0.1");
        //hsqldbServer.setLogWriter(null);
        //hsqldbServer.setErrWriter(null);
        hsqldbServer.setNoSystemExit(true);
        hsqldbServer.setPort(Integer.parseInt(dbPortStr));
        hsqldbServer.setTrace(Boolean.valueOf(dbTrace).booleanValue());
        hsqldbServer.setSilent(Boolean.valueOf(dbSilent).booleanValue());
        servletContext.log("Starting HSQLDB server");
        hsqldbServer.start();

        startHSQLServer = true;

        // now let's wait until the database is ready.
        servletContext.log("Waiting for database to start...");
        boolean starting = true;
        while (starting) {
            Thread.yield();
            try {
                Class.forName("org.hsqldb.jdbcDriver");
                String url = "jdbc:hsqldb:hsql://127.0.0.1:" + dbPortStr+"/hsqldbjahia";
                Connection con = DriverManager.getConnection(url, "sa", "");
                String sql = "COMMIT";
                Statement stmt = con.createStatement();
                stmt.executeUpdate(sql);
                stmt.close();
                starting = false;
            } catch (Exception e) {
                starting = false;
            }
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if (startHSQLServer) {
            ServletContext servletContext = servletContextEvent.getServletContext();
            kill(Integer.parseInt(dbPortStr), "sa", "");
            DatabaseManager.getTimer().shutDown();
            servletContext.log("HSQLDB server stopping, waiting...");
//            hsqldbServer.shutdown();
//            while (hsqldbServer.getState() != ServerConstants.SERVER_STATE_SHUTDOWN) {
//                // loop
//            }
            servletContext.log("HSQLDB now properly shutdown.");
            startHSQLServer = false;
        }
    }

    private void kill(int port, String user, String password) {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            String url = "jdbc:hsqldb:hsql://127.0.0.1:" + port + "/hsqldbjahia";
            Connection con = DriverManager.getConnection(url, user, password);
            String sql = "SHUTDOWN";
            Statement stmt = con.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            boolean stopping = true;
            while (stopping) {
                Thread.yield();
                try {
                    Class.forName("org.hsqldb.jdbcDriver");
                    sql = "COMMIT";
                    stmt = con.createStatement();
                    stmt.executeUpdate(sql);
                    stmt.close();
                    stopping = true;
                } catch (Exception e) {
                    stopping = false;
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

}
