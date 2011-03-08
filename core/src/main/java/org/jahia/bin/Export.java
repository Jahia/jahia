/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.utils.WebUtils;
import org.jdom.JDOMException;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.SAXException;

import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jahia.api.Constants.LIVE_WORKSPACE;

/**
 * Content export handler.
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 2 avr. 2010
 */
public class Export extends JahiaController implements ServletContextAware {

    public static final String CLEANUP = "cleanup";

    private String defaultWorkspace = LIVE_WORKSPACE;

    private String cleanupXsl;
    private String templatesCleanupXsl;

    /**
     * Process the request and return a ModelAndView object which the DispatcherServlet
     * will render. A <code>null</code> return value is not an error: It indicates that
     * this object completed request processing itself, thus there is no ModelAndView
     * to render.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @return a ModelAndView to render, or <code>null</code> if handled directly
     * @throws Exception in case of errors
     */
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            checkUserAuthorized();
            
            String method = request.getMethod();
            if (method.equals("POST")) {
                doPost(request, response);
            } else if (method.equals("GET")) {
                doGet(request, response);
            }

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (JahiaUnauthorizedException ue) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ue.getMessage());
        } catch (Exception e) {
            DefaultErrorHandler.getInstance().handle(e, request, response);
        }
        
        return null;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException, JahiaException, RepositoryException, SAXException, JDOMException {
        String path = StringUtils.substringAfter(request.getPathInfo().substring(1), "/");
        String workspace = StringUtils.defaultIfEmpty(StringUtils.substringBefore(path, "/"), defaultWorkspace);
        String[] strings = ("/" + StringUtils.substringAfter(path, "/")).split("\\.");
        String nodePath = strings[0];
        String exportFormat = strings[1];
        //make sure this file is not cached by the client (or a proxy middleman)
        WebUtils.setNoCacheHeaders(resp);

        Map<String, Object> params = getParams(request);

        OutputStream outputStream = resp.getOutputStream();
            ImportExportService ie = ServicesRegistry.getInstance().getImportExportService();

            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace);
            JCRNodeWrapper node = session.getNode(nodePath);
            JCRNodeWrapper exportRoot = null;
            if (request.getParameter("root") != null) {
                exportRoot = session.getNode(request.getParameter("root"));
            }
            if ("xml".equals(exportFormat)) {
                resp.setContentType("text/xml");
                if ("template".equals(request.getParameter(CLEANUP))) {
                    params.put(ImportExportService.XSL_PATH,templatesCleanupXsl);
                } else if ("simple".equals(request.getParameter(CLEANUP))) {
                    params.put(ImportExportService.XSL_PATH,cleanupXsl);
                }
                ie.exportNode(node, exportRoot, outputStream, params);
            } else if ("zip".equals(exportFormat)) {
                resp.setContentType("application/zip");
                if ("template".equals(request.getParameter(CLEANUP))) {
                    params.put(ImportExportService.XSL_PATH, templatesCleanupXsl);
                } else if ("simple".equals(request.getParameter(CLEANUP))) {
                    params.put(ImportExportService.XSL_PATH, cleanupXsl);
                }
                ie.exportZip(node, exportRoot, outputStream, params);
                outputStream.close();
            }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException, JahiaException, RepositoryException, SAXException, JDOMException {
        Map<String, Object> params = getParams(request);

        OutputStream outputStream = resp.getOutputStream();
            ImportExportService ie = ServicesRegistry.getInstance().getImportExportService();

            if ("all".equals(request.getParameter("exportformat"))) {
                if (!getCurrentUser().isRoot()) {
                    throw new JahiaUnauthorizedException("Only root user can perform export of all content");
                }

                resp.setContentType("application/zip");
                params.put(ImportExportService.INCLUDE_ALL_FILES, Boolean.TRUE);
                params.put(ImportExportService.INCLUDE_TEMPLATES, Boolean.TRUE);
                params.put(ImportExportService.INCLUDE_SITE_INFOS, Boolean.TRUE);
                params.put(ImportExportService.INCLUDE_DEFINITIONS, Boolean.TRUE);
                params.put(ImportExportService.VIEW_WORKFLOW, Boolean.TRUE);
                params.put(ImportExportService.XSL_PATH, cleanupXsl);

                ie.exportAll(outputStream, params);
                outputStream.close();
            } else if ("site".equals(request.getParameter("exportformat"))) {
                if (!getCurrentUser().isRoot()) {
                    throw new JahiaUnauthorizedException("Only root user can perform export of a site");
                }

                List<JahiaSite> sites = new ArrayList<JahiaSite>();
                String[] sitekeys = request.getParameterValues("sitebox");
                if (sitekeys != null) {
                    for (String sitekey : sitekeys) {
                        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(sitekey);
                        sites.add(site);
                    }
                }

                if (sites.isEmpty()) {
                    JahiaAdministration.doRedirect(request, resp, request.getSession(),
                                                   JahiaAdministration.JSP_PATH + "no_sites_selected.jsp");
                } else {
                    resp.setContentType("application/zip");
                    params.put(ImportExportService.INCLUDE_ALL_FILES, Boolean.TRUE);
                    params.put(ImportExportService.INCLUDE_TEMPLATES, Boolean.TRUE);
                    params.put(ImportExportService.INCLUDE_SITE_INFOS, Boolean.TRUE);
                    params.put(ImportExportService.INCLUDE_DEFINITIONS, Boolean.TRUE);
                    params.put(ImportExportService.VIEW_WORKFLOW, Boolean.TRUE);
                    params.put(ImportExportService.XSL_PATH, cleanupXsl);

                    ie.exportSites(outputStream, params, sites);
                    outputStream.close();
                }
            }
    }

    private Map<String, Object> getParams(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();

        params.put(ImportExportService.VIEW_CONTENT, !"false".equals(request.getParameter("viewContent")));
        params.put(ImportExportService.VIEW_VERSION, "true".equals(request.getParameter("viewVersion")));
        params.put(ImportExportService.VIEW_ACL, !"false".equals(request.getParameter("viewAcl")));
        params.put(ImportExportService.VIEW_METADATA, !"false".equals(request.getParameter("viewMetadata")));
        params.put(ImportExportService.VIEW_JAHIALINKS, !"false".equals(request.getParameter("viewLinks")));
        params.put(ImportExportService.VIEW_WORKFLOW, "true".equals(request.getParameter("viewWorkflow")));
        return params;
    }

    public static String getExportServletPath() {
        // TODO move this into configuration
        return "/cms/export";
    }

    public void setServletContext(ServletContext servletContext) {
        cleanupXsl = servletContext.getRealPath("/WEB-INF/etc/repository/export/" + "cleanup.xsl");
        templatesCleanupXsl = servletContext.getRealPath("/WEB-INF/etc/repository/export/"
                + "templatesCleanup.xsl");
    }
}
