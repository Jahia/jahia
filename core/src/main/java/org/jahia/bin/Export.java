/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.bin;

import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.sites.JahiaSite;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.XSLTransformer;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jahia.api.Constants.LIVE_WORKSPACE;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 2 avr. 2010
 */
public class Export extends HttpServlet implements Controller {
    private transient static Logger logger = Logger.getLogger(Export.class);

    private String defaultWorkspace = LIVE_WORKSPACE;
    private static FileCleaningTracker fileCleaningTracker = new FileCleaningTracker();

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

        export(request, response);
        return null;
    }

    private void export(HttpServletRequest request, HttpServletResponse resp) throws Exception {
        String path = StringUtils.substringAfter(request.getPathInfo().substring(1), "/");
        String workspace = StringUtils.defaultIfEmpty(StringUtils.substringBefore(path, "/"), defaultWorkspace);
        String[] strings = ("/" + StringUtils.substringAfter(path, "/")).split("\\.");
        String nodePath = strings[0];
        String exportFormat = strings[1];
        ProcessingContext processingContext = Jahia.createParamBean(request, resp, request.getSession());
        //make sure this file is not cached by the client (or a proxy middleman)
        resp.setHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Cache-Control", "no-cache");

        Map<String, Object> params = new HashMap<String, Object>();

        params.put(ImportExportService.VIEW_CONTENT, !"false".equals(request.getParameter("viewContent")));
        params.put(ImportExportService.VIEW_VERSION, "true".equals(request.getParameter("viewVersion")));
        params.put(ImportExportService.VIEW_ACL, !"false".equals(request.getParameter("viewAcl")));
        params.put(ImportExportService.VIEW_METADATA, !"false".equals(request.getParameter("viewMetadata")));
        params.put(ImportExportService.VIEW_JAHIALINKS, !"false".equals(request.getParameter("viewLinks")));
        params.put(ImportExportService.VIEW_WORKFLOW, "true".equals(request.getParameter("viewWorkflow")));
        params.put(ImportExportService.CLEANUP, "true".equals(request.getParameter(ImportExportService.CLEANUP)));

        OutputStream outputStream = resp.getOutputStream();
        try {
            ImportExportService ie = ServicesRegistry.getInstance().getImportExportService();

            if ("all".equals(request.getParameter("exportformat"))) {
                if (!processingContext.getUser().isRoot()) {
                    resp.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
                    return;
                }

                resp.setContentType("application/zip");
                params.put(ImportExportService.INCLUDE_ALL_FILES, Boolean.TRUE);
                params.put(ImportExportService.INCLUDE_TEMPLATES, Boolean.TRUE);
                params.put(ImportExportService.INCLUDE_SITE_INFOS, Boolean.TRUE);
                params.put(ImportExportService.INCLUDE_DEFINITIONS, Boolean.TRUE);
                params.put(ImportExportService.VIEW_WORKFLOW, Boolean.TRUE);
                params.put(ImportExportService.VIEW_PID, Boolean.TRUE);

                ie.exportAll(outputStream, params, processingContext);
                outputStream.close();
            } else if ("site".equals(processingContext.getParameter("exportformat"))) {
                if (!processingContext.getUser().isRoot()) {
                    resp.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
                    return;
                }

                List<JahiaSite> sites = new ArrayList<JahiaSite>();
                String[] sitekeys = processingContext.getParameterValues("sitebox");
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
                    params.put(ImportExportService.VIEW_PID, Boolean.TRUE);

                    ie.exportSites(outputStream, params, processingContext, sites);
                    outputStream.close();
                }
            } else if ("xml".equals(exportFormat)) {
                resp.setContentType("text/xml");
                if ((Boolean) params.get(ImportExportService.CLEANUP)) {
                    generateCleanedUpXML(request, resp, nodePath);
                } else {
                    getXml(nodePath, outputStream);
                }
            } else if ("zip".equals(exportFormat)) {
                resp.setContentType("application/zip");
                String id = request.getRealPath("/WEB-INF/etc/repository/export/" + "templatesCleanup.xsl");
                params.put(ImportExportService.XSL_PATH, id);
                JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace);
                JCRNodeWrapper node = session.getNode(nodePath);
                ie.exportZip(node, outputStream, params);
                outputStream.close();
            }
        } catch (Exception e) {
            logger.error("Exception during export", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void generateCleanedUpXML(HttpServletRequest request, HttpServletResponse resp, String nodePath)
            throws IOException, RepositoryException, JDOMException {
        OutputStream outputStream;
        String filename = StringUtils.substringAfter(nodePath.substring(1), "/").replace(" ", "_");
        File tempFile = File.createTempFile("exportTemplates-" + filename, "xml");
        outputStream = new DeferredFileOutputStream(1024 * 1024 * 10, tempFile);
        getXml(nodePath, outputStream);
        DeferredFileOutputStream stream = (DeferredFileOutputStream) outputStream;
        InputStream inputStream = new BufferedInputStream(new FileInputStream(stream.getFile()));
        fileCleaningTracker.track(stream.getFile(), inputStream);
        if (stream.isInMemory()) {
            inputStream.close();
            inputStream = new ByteArrayInputStream(stream.getData());
        }
        XSLTransformer xslTransformer = new XSLTransformer(request.getRealPath(
                "/WEB-INF/etc/repository/export/" + "templatesCleanup.xsl"));
        SAXBuilder saxBuilder = new SAXBuilder(false);
        Document document = saxBuilder.build(inputStream);
        Document document1 = xslTransformer.transform(document);
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        xmlOutputter.output(document1, resp.getOutputStream());
        inputStream.close();
    }

    private void getXml(String nodePath, OutputStream outputStream) throws RepositoryException, IOException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        Node node = session.getNode(nodePath);
        session.exportDocumentView(node.getPath(), outputStream, true, false);
        outputStream.close();
    }

    public static String getExportServletPath() {
        // TODO move this into configuration
        return "/cms/export";
    }

    /**
     * Called by the servlet container to indicate to a servlet that the
     * servlet is being taken out of service.  See {@link javax.servlet.Servlet#destroy}.
     */
    @Override
    public void destroy() {
        super.destroy();
        fileCleaningTracker.exitWhenFinished();
    }
}
