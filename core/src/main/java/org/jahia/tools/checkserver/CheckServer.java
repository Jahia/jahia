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
//
//  12.06.2001  POL  First release

package org.jahia.tools.checkserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Calendar;

import org.jahia.utils.properties.PropertiesManager;

public class CheckServer {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(CheckServer.class);

    /** time beetwen 2 tests [second] **/
    static private final int CHECKTIME = 60;

    /** default port **/
    static private final int PORT = 80;

    /** lock file **/
    static private final String LOCKFILE = ".lock";

    /**
     * desc:  This will check if tomcat is running. If not, then restart tomcat
     *
     * Copyright:    Copyright (c) 2002
     * Company:      Jahia Ltd
     *
     * @author Phillippe Vollenweider
     * @version 1.0
     */
    static void main (String args[]) {
        System.out.println("Jahia LifeControl, version 1.0 started");

        // check command line syntax
        if (args.length < 2) {
            logger.info( "Usage: java [-classpath path_to_jahia_classes] org.jahia.tools.checkserver.CheckServer <delay> <path_to_jahia_properties>");
            Runtime.getRuntime().exit(1);
        }

        // check if the lockfile exist
        File lockFile = new File(LOCKFILE);
        if (!lockFile.exists()) {
            try {
                lockFile.createNewFile();
            } catch (IOException ioe) {
                logger.error("Could not create the lock file " +
                                          LOCKFILE + " in current directory", ioe);
                Runtime.getRuntime().exit(1);
            }
        }

        // get checktime var
        String checkTimeStr = args[0];
        int checkTime = CHECKTIME;
        try {
            checkTime = Integer.parseInt(checkTimeStr);
        } catch (NumberFormatException nfe) {
        }
        checkTime *= 1000;

        // get the path to jahia.properties
        String propertiesFile = args[1];
        if (!new File(propertiesFile).exists()) {
            logger.error("Could not open " + propertiesFile);
            Runtime.getRuntime().exit(1);
        }

        PropertiesManager properties = new PropertiesManager(propertiesFile);

        // get properties
        String serverHomeDiskPath = properties.getProperty("serverHomeDiskPath");
        String jahiaCoreHttpPath = properties.getProperty("jahiaCoreHttpPath");
        String jahiaHostHttpPath = properties.getProperty("jahiaHostHttpPath");
        String server = properties.getProperty("server");

        // exit if the server is not tomcat
        if (server.toLowerCase().indexOf("tomcat") == -1) {
            logger.error("This service run only with tomcat server");
            Runtime.getRuntime().exit(1);
        }

        // get the port
        int i = replacePattern(jahiaHostHttpPath, "://", "///").indexOf(":");
        int port = PORT;
        String portStr = "";
        if (i != -1) {
            portStr = jahiaHostHttpPath.substring(i + 1,
                                                  jahiaHostHttpPath.length());
            port = Integer.parseInt(portStr);
        }

        // get jahiaCoreHttpPath
        URL uri = null;
        try {
            uri = new URL(jahiaCoreHttpPath);
        } catch (Exception throwable) {
            return;
        }

        while (true) {
            if (!lockFile.exists()) {
                logger.error("Lockfile not found: exiting");
                Runtime.getRuntime().exit(1);
            }
            logger.info("[" +
                        Calendar.getInstance().getTime().toString() +
                        "] Server status for " + uri.toString() +
                        " : ");
            try {
                // Let's try to get the default web page
                InputStream inputstream = uri.openStream();
                inputstream.close();
                System.out.println("[OK]");

            } catch (Exception throwable1) {
                System.out.println("[FAILED]");

                // check the OS
                String ext = ".bat"; // windows
                if (File.separator.equals("/")) {
                    ext = ".sh"; // other
                }
                // String p = ".." + File.separator;
                String shutdownScript = serverHomeDiskPath + "bin" +
                                        File.separator + "shutdown" + ext;
                String startupScript = serverHomeDiskPath + "bin" +
                                       File.separator + "startup" + ext;

                try {
                    logger.info("   * Shutdown tomcat");
                    logger.info("   * " + shutdownScript);
                    logger.info("   * Waiting for all conections to be closed : ");

                    if (!new File(shutdownScript).exists()) {
                        logger.error("Could not execute " +
                                                shutdownScript);
                        Runtime.getRuntime().exit(1);
                    }
                    try {
                        Runtime.getRuntime().exec(shutdownScript, null,
                                                  new File(serverHomeDiskPath));
                    } catch (IOException ioe) {
                        logger.error("Error:" + ioe.toString(), ioe);
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                    }

                    // Check if all connections are closed
                    boolean allConnectionClosed = false;
                    while (!allConnectionClosed) {
                        try {
                            // If we can open a socket on port PORT, it mean that all connections are closed.
                            ServerSocket s = new ServerSocket(port);
                            s.close();
                            allConnectionClosed = true;
                        } catch (IOException ioe) {
                            // wait for connections to be closed
                            allConnectionClosed = false;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ie) {
                            }
                        }
                    }
                    System.out.println(" [OK]");
                    logger.info("   * Starting tomcat");
                    if (!new File(startupScript).exists()) {
                        logger.error("Could not execute " +
                                                  startupScript);
                        Runtime.getRuntime().exit(1);
                    }
                    logger.info("   * " + startupScript);
                    logger.info("   * waiting for for tomcat to be up : ");
                    try {
                        Runtime.getRuntime().exec(startupScript, null,
                                                  new File(serverHomeDiskPath));
                    } catch (IOException ioe) {
                        System.out.println(ioe);
                    }
                    boolean flag = false;

                    // wait the server to be up
                    while (!flag) {
                        try {
                            InputStream inputstream = uri.openStream();
                            flag = true;
                            inputstream.close();
                        } catch (Exception t) {
                            flag = false;
                        }
                    }
                    System.out.println(" [OK]");

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            } // catch

            try {
                Thread.sleep(checkTime);
            } catch (InterruptedException ie) {
                //
            }

        } // while(true)
    } //main

    static public String replacePattern (String str, String oldToken,
                                         String newToken) {
        if (str == null) {
            return str;
        }
        String result = "";
        int i = str.indexOf(oldToken);
        while (i != -1) {
            result += str.substring(0, i) + newToken;
            str = str.substring(i + oldToken.length(), str.length());
            i = str.indexOf(oldToken);
        }
        return result + str;
    }

}
