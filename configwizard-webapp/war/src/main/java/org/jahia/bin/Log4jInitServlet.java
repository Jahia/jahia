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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.InputStream;
import java.util.Properties;
import java.lang.RuntimeException;

import org.apache.log4j.xml.*;
import org.apache.log4j.PropertyConfigurator;

public class Log4jInitServlet extends HttpServlet {

    public void init () {
        String realPath;
        String xmlFile = getInitParameter("log4j-xml-init-file");

        // ------------------------------------
        // if the log4j-init-file is not set, then no point in trying

        getServletContext().log(">> log4j-xml-init-file = " + xmlFile);
        realPath = getServletContext().getRealPath(xmlFile);

        getServletContext().log(">> realPath = " + realPath);

        /*
        if (isLog4JConfigured()) {
            getServletContext().log("Log4J already configured, aborting configuration...");
            return;
        }
        */

        if (realPath != null) {
            try {

                DOMConfigurator.configureAndWatch(realPath);
            } catch (Exception e) {
                getServletContext().log("Exception(DOMConfigurator) : " +
                                        e.getMessage());
                getServletContext().log("Looking for WAR file");
            }

        } else {
            getServletContext().log(
                "Error, couldn't find log4j configuration file " +
                xmlFile);
            getServletContext().log("Looking for WAR file");
        }

        // -------------------------------------

        InputStream input = getServletContext().getResourceAsStream(xmlFile);
        if (input != null) {

            try {
                Properties prop = new Properties();
                prop.load(input);

                PropertyConfigurator.configure(prop);
            } catch (Exception e) {
                getServletContext().log("Exception(PropertyConfigurator) : " +
                                        e.getMessage());
                getServletContext().log(xmlFile.toString() +
                                        " has not been found in WAR file");
                throw new RuntimeException(e.getMessage());
            }

        } else {
            getServletContext().log(xmlFile.toString() +
                                    " has not been found in WAR file");
        }

    }

    public void destroy() {
    }

    public void doGet (HttpServletRequest req, HttpServletResponse res) {
        getServletContext().log(">>---------------------------------------");
        getServletContext().log(">> In org.jahia.bin.Log4jInitServlet     ");
        getServletContext().log(">>---------------------------------------");

    }

}
