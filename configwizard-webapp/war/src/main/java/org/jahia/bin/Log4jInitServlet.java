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
