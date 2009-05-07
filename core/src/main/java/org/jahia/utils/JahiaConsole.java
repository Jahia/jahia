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
/* JAHIA                                                            */

/* Class Name   :   JahiaConsole                                    */

/* Function     :   Manages Jahia console messages                  */

/* Created      :   08-10-2000                                      */

/* Author       :   Eric Vassalli                                   */

/* Interface    :                                                   */

/*      print( String )     : prints a message in console           */

/*      println( String )   : prints a message in console with \n   */

/*      startup()           : displays cool startup message :)      */

/*                                         Copyright 2002 Jahia Ltd */

/********************************************************************/

package org.jahia.utils;

import javax.servlet.GenericServlet;

public class JahiaConsole {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaConsole.class);

    /**
     * Constants for logging levels {
     */
    public static final int DEFAULT_LOGGING_LEVEL = 3;
    public static final int MAX_LOGGING_LEVEL = 10;
    public static final int CONSOLE_LOGGING_LEVEL = 9;
    /**
     * }
     */

    /**
     * constructor
     * EV    08.10.2000
     */
    private JahiaConsole() {
        println("JahiaConsole", "***** Starting Jahia Console");
    } // end constructor


    public static void setServlet(GenericServlet servletRef) {
        // do nothing;
    }

    public static void setLoggingLevel(int level) {
        // do nothing;
    }

    /**
     * print
     * EV    08.10.2000
     */
    public static void print(String origin, String msg) {
        logger.debug(origin + " > " + msg);
    }


    /**
     * println
     * EV    08.10.2000
     */
    public static void println(String origin, String msg) {
        logger.debug(origin + "> " + msg);
    }

    /**
     * Small utility function to print stack trace on the Jahia console.
     *
     * @param origin a String representing the origin of the message. Recommended
     *               format is class.method
     * @param t      the exception whose stack trace will be dumped into the Jahia
     *               Console.
     * @author Serge Huber.
     */
    public static void printe(String origin, Throwable t) {
        logger.debug(origin, t);
    }

    /**
     * Prints a message on the console.
     * THIS METHOD SHOULD BE CALLED ONLY IF YOU WANT YOUR MESSAGE TO BE DISPLAYED IN THE
     * RELEASE VERSION OF JAHIA.  Don't abuse ;-)
     */
    public static synchronized void finalPrintln(String origin, String msg) {
        logger.info(origin + "> " + msg);
    }

    public static synchronized void finalPrint(String origin, String msg) {
        logger.info(origin + "> " + msg);
    }


    /**
     * startup
     * EV    08.10.2000
     */
    public static void startup(int buildNumber) {
        String msg = "";
        msg += "***********************************\n";
        msg += "   Starting Jahia - Build " + buildNumber + "\n";
        msg += "       \"Today's a great day ! \"\n";
        msg += "***********************************\n";
        JahiaConsole.println("JahiaConsole.startup", "\n\n" + msg + "\n");
        println("Jahia", "***** Starting Jahia *****");
    }


    /**
     * startupWithTrust
     * AK    20.01.2001
     */
    public static void startupWithTrust(int buildNumber) {
        Integer buildNumberInteger = new Integer(buildNumber);
        String buildString = buildNumberInteger.toString();
        StringBuilder buildBuffer = new StringBuilder();

        for (int i = 0; i < buildString.length(); i++) {
            buildBuffer.append(" ");
            buildBuffer.append(buildString.substring(i, i + 1));
        }

        StringBuilder msg = new StringBuilder(512);
        msg
                .append(
                        "\n\n\n\n"
                                + "                                     ____.\n"
                                + "                         __/\\ ______|    |__/\\.     _______\n"
                                + "              __   .____|    |       \\   |    +----+       \\\n"
                                + "      _______|  /--|    |    |    -   \\  _    |    :    -   \\_________\n"
                                + "     \\\\______: :---|    :    :           |    :    |         \\________>\n"
                                + "             |__\\---\\_____________:______:    :____|____:_____\\\n"
                                + "                                        /_____|\n"
                                + "\n"
                                + "      . . . s t a r t i n g   j a h i a   b u i l d  ")
                .append(buildBuffer.toString())
                .append(
                        " . . .\n"
                                + "\n\n"
                                + "   Copyright 2002-2009 - Jahia Ltd. http://www.jahia.org - All Rights Reserved\n"
                                + "\n\n"
                                + " *******************************************************************************\n"
                                + " * The contents of this software, or the files included with this software,    *\n"
                                + " * are subject to the Jahia Sustainable Enterprise License - JSEL.             *\n"
                                + " * You may not use this software except in compliance with the license. You    *\n"
                                + " * may obtain a copy of the license at http://www.jahia.org/license. See the   *\n"
                                + " * license for the rights, obligations and limitations governing use of the    *\n"
                                + " * contents of the software.                                                   *\n"
                                + " *******************************************************************************\n"
                                + "\n\n");

        System.out.println (msg.toString());
        System.out.flush();
    }

}
