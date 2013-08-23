/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.utils.StringResponseWrapper;
import org.osgi.framework.Bundle;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Helps to precompile JSPs of a WebApp. The Servlet performs 3 actions depending on the passed params: - if jsp_name param is passed, the
 * servlet tries to forward to the JSP with the passed name - if compile_type=all is passed, the servlet tries to forward to all found JSPs
 * and generates a report HTML output - if compile_type=modules is passed, the servlet tries to forward to all found module JSPs and
 * generates a report HTML output - if compile_type=site is passed, the servlet tries to forward to all found module JSPs of a site and
 * generates a report HTML output - if no special param is passed, the servlet generates a page with links for the above described purposes
 */
public class JspPrecompileServlet extends HttpServlet {
    private static final long serialVersionUID = 7291760429380775493L;
    private static final String JSP_NAME_PARAM = "jsp_name";
    private static final String COMPILE_TYPE_PARAM = "compile_type";

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
            precompileJsps(Arrays.asList(jspName),aRequest, aResponse);
        } else if ("all".equals(compileType)) {
            // precompile all JSPs and generate report
            precompileJsps(searchForAllJsps(), aRequest, aResponse);
        } else if ("modules".equals(compileType)) {
            // precompile all JSPs and generate report
            precompileJsps(searchForBundleJsps(), aRequest, aResponse);
        } else if ("module".equals(compileType)) {
            // precompile JSPs of a module
            precompileJsps(
                    searchForBundleJsps(FrameworkService.getBundleContext().getBundle(
                            Long.parseLong(aRequest.getParameter("id")))), aRequest, aResponse);
        } else if ("non-modules".equals(compileType)) {
            // precompile all JSPs not in modules and generate report
            precompileJsps(searchForJsps(""), aRequest, aResponse);
        } else {
            // generate output with links for compile all and all JSPs
            PrintWriter out = aResponse.getWriter();
            List<String> foundJsps = searchForAllJsps();

            aResponse.setContentType("text/html;charset=ISO-8859-1");
            
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
            out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
            out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
            out.println("<head>");
            out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
            out.println("<meta http-equiv=\"expires\" content=\"0\" />");
            out.print("<link rel=\"stylesheet\" href=\"");
            out.print(aResponse.encodeURL(aRequest.getContextPath() + "/tools/tools.css"));
            out.println("\" type=\"text/css\" />");
            out.println("<title>JSP compilation</title>");
            out.println("</head>");
            out.println("<body>");

            out.println("<h1>JSP compilation</h1>");
            out.print("<p>Found <strong>");
            out.print(foundJsps.size());
            out.println("</strong> JSPs</p>");
            
            out.println("<h2>Pre-compile:</h2>");
            out.println("<ul>");
            out.print("<li><a target=\"_blank\" href=\"");

            long now = System.currentTimeMillis();

            String url = aResponse.encodeURL(aRequest.getContextPath()
                    + aRequest.getServletPath() + "?" + COMPILE_TYPE_PARAM
                    + "=all&timestamp=" + now + "&" + MAGIC_TOMCAT_PARAM);

            out.print(url);
            out.println("\">all</a></li>");
            
            out.print("<li><a target=\"_blank\" href=\"");

            url = aResponse.encodeURL(aRequest.getContextPath()
                    + aRequest.getServletPath() + "?" + COMPILE_TYPE_PARAM
                    + "=non-modules&timestamp=" + now + "&" + MAGIC_TOMCAT_PARAM);

            out.print(url);
            out.println("\">non-modules</a></li>");
            
            out.print("<li><a target=\"_blank\" href=\"");

            url = aResponse.encodeURL(aRequest.getContextPath()
                    + aRequest.getServletPath() + "?" + COMPILE_TYPE_PARAM
                    + "=modules&timestamp=" + now + "&" + MAGIC_TOMCAT_PARAM);

            out.print(url);
            out.print("\">all modules</a></li>");
            
            out.println("</ul>");
            out.println("<h2>Modules:</h2>");
            
            out.println("<ul>");
            Map<String, Long> moduleBundles = new TreeMap<String, Long>();
            for (Bundle bundle : FrameworkService.getBundleContext().getBundles()) {
                if (BundleUtils.isJahiaModuleBundle(bundle)) {
                    Enumeration<?> en = bundle.findEntries("/", "*.jsp", true);
                    if (en != null && en.hasMoreElements()) {
                        moduleBundles.put(bundle.getSymbolicName(), bundle.getBundleId());
                    }
                }
            }
            for (Map.Entry<String, Long> entry : moduleBundles.entrySet()) {
                out.print("<li><a target=\"_blank\" href=\"");

                url = aResponse.encodeURL(aRequest.getContextPath() + aRequest.getServletPath() + "?"
                        + COMPILE_TYPE_PARAM + "=module&id=" + entry.getValue() + "&timestamp=" + now + "&"
                        + MAGIC_TOMCAT_PARAM);

                out.print(url);
                out.print("\">" + entry.getKey() + "</a></li>");
            }

            out.println("</ul><br/>");

            out.println("<h2>All JSPs:</h2>");
            listFiles(out, aRequest.getContextPath(),
                    aRequest.getServletPath(), foundJsps, aResponse, now);

            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Searches for Files with extension JSP in the whole web app directory.
     *
     * @return List of context relative JSP names (Strings)
     */
    private List<String> searchForAllJsps() {
        List<String> list = searchForJsps("");
        list.addAll(searchForBundleJsps());
        return list;
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

    private List<String> searchForBundleJsps() {
        List<String> foundJsps = new ArrayList<String>();
        for (Bundle bundle : FrameworkService.getBundleContext().getBundles()) {
            foundJsps.addAll(searchForBundleJsps(bundle));
        }
        return foundJsps;
    }

    private List<String> searchForBundleJsps(Bundle bundle) {
        List<String> foundJsps = new ArrayList<String>();
        Enumeration<?> en = bundle.findEntries("/","*.jsp",true);
        if (en != null) {
            while (en.hasMoreElements()) {
                URL url  = (URL) en.nextElement();
                foundJsps.add("modules/"+bundle.getSymbolicName()+url.getPath());
            }
        }
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

        List < String > buggyJsps = new ArrayList<String>();
        int i = 1;
        for (final String jspPath : foundJsps) {
            try {
                StringResponseWrapper responseWrapper = new StringResponseWrapper(aResponse);
                compile(jspPath, aRequest, responseWrapper, i);
                System.out.println(" OK.");
            } catch (Exception e) {
                e.printStackTrace();
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
            out.print("Precompile failed for following <strong>" + buggyJsps.size()
                    + "</strong> JSPs:<br/>\r\n");
            listFiles(out, aRequest.getContextPath(),
                    aRequest.getServletPath(), buggyJsps, aResponse, System
                            .currentTimeMillis());
        }
        out.println("</body>" + "</html>");
    }

    private void compile(final String jspPath, final HttpServletRequest aRequest, HttpServletResponse aResponse, int i) throws Exception {
        RequestDispatcher rd = aRequest.getRequestDispatcher("/" + jspPath);
        System.out.print("Compiling (" + i + ") " + jspPath + "...");
        rd.include(aRequest, aResponse);
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
            anOut.print("<a target=\"_blank\" href=\"");
            String url = null;

            // create link to JspPrecompileServlet with jsp_name param
            url = aContextPath + aServletPath + "?" + JSP_NAME_PARAM + "="
                    + jspPath + "&" + MAGIC_TOMCAT_PARAM;

            url = url + "&now=" + now;
            anOut.print(aResponse.encodeURL(url));

            anOut.print("\">");
            anOut.print(jspPath);
            anOut.println("</a><br/>");
        }
    }
}