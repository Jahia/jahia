/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
