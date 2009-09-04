/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.bin;

import org.hsqldb.Server;
import org.hsqldb.DatabaseManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Vector;

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
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        String servletPath = servletContext.getRealPath("");        
        for (String databaseURI : ((Vector<String>)DatabaseManager.getDatabaseURIs())) {
           if (databaseURI.startsWith("file:") && databaseURI.contains(servletPath)) {
               kill("jdbc:hsqldb:" + databaseURI, "sa", "");
               DatabaseManager.getTimer().shutDown();
               servletContext.log("HSQLDB server stopping, waiting...");
               servletContext.log("HSQLDB now properly shutdown.");
               break;
           }
        }
    }

    private void kill(String url, String user, String password) {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
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
