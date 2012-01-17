/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.tools.precompile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Helps to precompile JSPs of a WebApp. The Servlet performs 3 actions depending on the passed params: - if jsp_name param is passed, the
 * servlet tries to forward to the JSP with the passed name - if compile_type=all is passed, the servlet tries to forward to all found JSPs
 * and generates a report HTML output - if compile_type=templates is passed, the servlet tries to forward to all found templates JSPs and
 * generates a report HTML output - if compile_type=site is passed, the servlet tries to forward to all found templates JSPs of a site and
 * generates a report HTML output - if no special param is passed, the servlet generates a page with links for the above described purposes
 */
public class JspPrecompileServlet extends HttpServlet implements Servlet {
    private static final String JSP_NAME_PARAM = "jsp_name";
    private static final String COMPILE_TYPE_PARAM = "compile_type";
    private static final String SITE_KEY_PARAM = "site_key";
    private static final String TEMPLATES_DIR = "modules";

    private static final String MAGIC_TOMCAT_PARAM = "jsp_precompile=true";

    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse)
            throws ServletException, IOException {
        doWork(aRequest, aResponse);
    }

    public void doPost(HttpServletRequest aRequest,
            HttpServletResponse aResponse) throws ServletException, IOException {
        doWork(aRequest, aResponse);
    }

    /**
     * Performs depending on the passed request params the actions mentioned in the class description.
     */
    private void doWork(HttpServletRequest aRequest,
            HttpServletResponse aResponse) throws ServletException, IOException {
        aRequest.getSession(true);

        String jspName = aRequest.getParameter(JSP_NAME_PARAM);
        String compileType = aRequest.getParameter(COMPILE_TYPE_PARAM);
        if (jspName != null) {
            // precompile single JSP
            RequestDispatcher rd = aRequest.getRequestDispatcher("/" + jspName);
            rd.forward(aRequest, aResponse);
        } else if ("all".equals(compileType)) {
            // precompile all JSPs and generate report
            precompileJsps(searchForJsps(), aRequest, aResponse);
        } else if ("templates".equals(compileType)) {
            // precompile all JSPs and generate report
            precompileJsps(searchForJsps(TEMPLATES_DIR), aRequest, aResponse);
        } else if ("site".equals(compileType)) {
            // precompile all JSPs and generate report
            precompileJsps(searchForJsps(TEMPLATES_DIR + "/"
                    + aRequest.getParameter(SITE_KEY_PARAM)), aRequest,
                    aResponse);
        } else {
            // generate output with links for compile all and all JSPs
            PrintWriter out = aResponse.getWriter();
            List<String> foundJsps = searchForJsps();

            aResponse.setContentType("text/html;charset=ISO-8859-1");

            out.print("<html>\r\n" + "<head>"
                    + "<META http-equiv=\"expires\" content=\"0\">"
                    + "<title>JSPs in WebApp</title>" + "</head>\r\n"

                    + "<body>\r\n" + "<b>");
            out.print(new Date().toString());
            out.print("</b><br/>\r\n"

            + "#JSPs in WebApp: ");
            out.print(foundJsps.size());
            out.print("<br>\r\n"

            + "<a target=\"_blank\" href=\"");

            long now = System.currentTimeMillis();

            String url = aResponse.encodeURL(aRequest.getContextPath()
                    + aRequest.getServletPath() + "?" + COMPILE_TYPE_PARAM
                    + "=all&timestamp=" + now + "&" + MAGIC_TOMCAT_PARAM);

            out.print(url);
            out.print("\">precompile all</a><br/>\r\n");
            out.print("<a target=\"_blank\" href=\"");

            url = aResponse.encodeURL(aRequest.getContextPath()
                    + aRequest.getServletPath() + "?" + COMPILE_TYPE_PARAM
                    + "=templates&timestamp=" + now + "&" + MAGIC_TOMCAT_PARAM);

            out.print(url);
            out.print("\">precompile modules</a><br/>\r\n");

            listSites(out, aRequest, aResponse, now);

            listFiles(out, aRequest.getContextPath(),
                    aRequest.getServletPath(), foundJsps, aResponse, now);

            out.print("</body>\r\n" + "</html>");
        }
    }

    /**
     * Searches for Files with extension JSP in the whole web app directory.
     *
     * @return List of context relative JSP names (Strings)
     */
    private List<String> searchForJsps() {
        return searchForJsps("");
    }

    /**
     * Searches for Files with extension JSP in the whole web app directory.
     *
     * @return List of context relative JSP names (Strings)
     */
    private List<String> searchForJsps(String attachPath) {
        String webModulePath = getServletContext().getRealPath("/");
        File jspsDir = new File(webModulePath + attachPath);
        List<String> foundJsps = new ArrayList<String>();
        searchForJsps(webModulePath, jspsDir, foundJsps);
        return foundJsps;
    }

    /**
     * Fills passed List with context relative URLs of found JSPs. If passed dir contains subdirs, the method is called recursive for this
     * subdirs.
     */
    private void searchForJsps(String aWebModulePath, File aDir, List<String> aFoundJsps) {
        File[] files = aDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                // subdir found
                searchForJsps(aWebModulePath, files[i], aFoundJsps);
            } else {
                int extIdx = files[i].getName().lastIndexOf('.');
                if (extIdx != -1
                        && files[i].getName().length() == extIdx + 4 // ... + ".jsp"
                        && files[i].getName().regionMatches(true, extIdx + 1,
                                "jsp", 0, 3)) {
                    // JSP found!
                    String jspPath = files[i].getPath();
                    jspPath = jspPath.substring(aWebModulePath.length());
                    jspPath = jspPath.replace('\\', '/');
                    aFoundJsps.add(jspPath);
                }
            }
        }
    }

    /**
     * Loops through list of all JSP URLs, "includes" each JSP and generates a report HTML response. Progress information is printed to
     * stdout.
     */
    private void precompileJsps(List<String> foundJsps, HttpServletRequest aRequest,
            HttpServletResponse aResponse) throws ServletException, IOException {
        System.out.println("Precompile started...");
        List<String> buggyJsps = new ArrayList<String>();
        int i = 1;
        for (String jspPath : foundJsps) {
            RequestDispatcher rd = aRequest.getRequestDispatcher("/" + jspPath);
            try {
                System.out.print("Compiling (" + i + ") " + jspPath + "...");
                rd.include(aRequest, aResponse);
                System.out.println(" OK.");
            } catch (Exception ex) {
                System.out.println(" ERROR.");
                buggyJsps.add(jspPath);
            }
            aResponse.resetBuffer();
            i++;
        }
        System.out.println("Precompile ended!");
        PrintWriter out = aResponse.getWriter();
        aResponse.setContentType("text/html;charset=ISO-8859-1");
        out.print("<html>" + "<head>"
                + "<META http-equiv=\"expires\" content=\"0\">"
                + "<title>JSP precompile result</title>" + "</head>\r\n"
                + "<body>\r\n" + "<b>");
        out.print(foundJsps.size());
        out.print(" JSPs processed.</b><br/>\r\n");
        if (buggyJsps.size() == 0)
            out.print("No problems found!\r\n");
        else {
            out.print("Precompile failed for following " + buggyJsps.size()
                    + " JSPs:\r\n");
            listFiles(out, aRequest.getContextPath(),
                    aRequest.getServletPath(), buggyJsps, aResponse, System
                            .currentTimeMillis());
        }
        out.println("</body>" + "</html>");
    }

    /**
     * Adds a hyperlinks for each JSP to the output. Each link contains the JSP name. If the JSP is located somewhere below WEB-INF dir, it
     * can not be reached from outside, therefore a link to the servlet is created with a jsp_name param. Tomcat specific jsp_precompile
     * param is also added to each link. Also current timestamp is added to help the browser marking visited links.
     */
    private void listFiles(PrintWriter anOut, String aContextPath,
            String aServletPath, List<String> aFoundJsps,
            HttpServletResponse aResponse, long now) {
        for (String jspPath : aFoundJsps) {
            anOut.print("<br/>");
            anOut.print("<a target=\"_blank\" href=\"");
            String url = null;

            if (jspPath.startsWith("WEB-INF", 1)) {
                // create link to JspPrecompileServlet with jsp_name param
                url = aContextPath + aServletPath + "?" + JSP_NAME_PARAM + "="
                        + jspPath + "&" + MAGIC_TOMCAT_PARAM;
            } else {
                // create direct link to jsp file
                url = aContextPath + "/" + jspPath + "?" + MAGIC_TOMCAT_PARAM;
            }
            url = url + "&now=" + now;
            anOut.print(aResponse.encodeURL(url));

            anOut.print("\">");
            anOut.print(jspPath);
            anOut.println("</a>");
        }
    }

    /**
     * Adds hyperlinks for each site to the output. Each link contains the directory name of the site. Tomcat specific jsp_precompile param
     * is also added to each link. Also current timestamp is added to help the browser marking visited links.
     */
    private void listSites(PrintWriter anOut, HttpServletRequest aRequest,
            HttpServletResponse aResponse, long now) {
        String templatesPath = getServletContext().getRealPath("/");
        templatesPath += templatesPath.endsWith("/") ? TEMPLATES_DIR : "/"+TEMPLATES_DIR;  // Fix for IBM SDK 1.5.0 SR6 (WAS 6.1.0.15)
        File templatesDir = new File(templatesPath);
        if (!templatesDir.exists()) {
            org.slf4j.LoggerFactory.getLogger(getClass()).error("Cannot find templates directory: " + templatesPath);
        }
        else {
            File[] files = templatesDir.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        anOut.print("<a target=\"_blank\" href=\"");
                        String url = aResponse.encodeURL(aRequest.getContextPath()
                                + aRequest.getServletPath() + "?" + COMPILE_TYPE_PARAM
                                + "=site&site_key=" + files[i].getName()
                                + "&timestamp=" + now + "&" + MAGIC_TOMCAT_PARAM);

                        anOut.print(url);
                        anOut.print("\">precompile module: " + files[i].getName()
                                + "</a><br/>\r\n");
                    }
                }
            }
        }
    }
}