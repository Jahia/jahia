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
package org.jahia.bin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.hsqldb.DatabaseManager;
import org.hsqldb.Server;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 3, 2008
 * Time: 3:03:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class HypersonicManager {
    private  org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(HypersonicManager.class);

    String args[] = new String[10];
    Thread serverThread;
    Server hsqldbServer;
    String dbLocation = "/WEB-INF/var/dbdata/hsqldbjahia";
    String dbPortStr = "9001";
    String dbSilent = "true";
    String dbTrace = "false";

    public HypersonicManager() {
    }

    public void startup(String path) {

        hsqldbServer = new Server();
        hsqldbServer.setDatabaseName(0,"hsqldbjahia");
        hsqldbServer.setDatabasePath(0, "file:" + path);
        hsqldbServer.setAddress("127.0.0.1");
        hsqldbServer.setNoSystemExit(true);
        hsqldbServer.setPort(Integer.parseInt(dbPortStr));
        hsqldbServer.setTrace(Boolean.valueOf(dbTrace).booleanValue());
        hsqldbServer.setSilent(Boolean.valueOf(dbSilent).booleanValue());
        logger.info("Starting HSQLDB server");
        hsqldbServer.start();

        // now let's wait until the database is ready.
        logger.info("Waiting for database to start...");
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

    public void stop() {
        logger.info("HSQLDB server stopping, waiting...");
        kill(Integer.parseInt(dbPortStr), "sa", "");
        DatabaseManager.getTimer().shutDown();
        logger.info("HSQLDB now properly shutdown.");
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
