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
