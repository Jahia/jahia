/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */package org.jahia.bin;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.InputStream;
import java.util.Properties;
import java.lang.RuntimeException;

import org.apache.log4j.xml.*;
import org.apache.log4j.PropertyConfigurator;

/**
 * <p>Title: Log4J initialization servlet</p>
 * <p>Description: This is a servlet that is configured to be initialized
 * when the servlet container startups, so that it may load the configuration
 * for the Log4J logging system</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Serge Huber
 * @version 1.0
 */
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
