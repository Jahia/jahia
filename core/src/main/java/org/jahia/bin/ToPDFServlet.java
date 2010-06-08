/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

import static org.jahia.api.Constants.LIVE_WORKSPACE;

import java.io.InputStream;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.transform.DocumentConverterService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Performs conversion of the submitted document into specified format.
 *
 * @author Cédric Mailleux
 */
public class ToPDFServlet extends HttpServlet implements Controller {

    private static Logger logger = Logger.getLogger(ToPDFServlet.class);

    private DocumentConverterService converterService;

    private String defaultWorkspace = LIVE_WORKSPACE;
    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.web.servlet.mvc.Controller#handleRequest(javax.servlet
     * .http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!converterService.isEnabled()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Conversion service is not enabled.");
            return null;
        }

        String path = StringUtils.substringAfter(request.getPathInfo().substring(1), "/");
        String workspace = StringUtils.defaultIfEmpty(StringUtils.substringBefore(path, "/"), defaultWorkspace);
        String nodePath = request.getParameter("path");
        String id = request.getParameter("id");
        InputStream is = null; 
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace);
            JCRNodeWrapper node = id != null ? session.getNodeByIdentifier(id) : session.getNode(nodePath);
            if(node.isNodeType("nt:file")) {
                response.setContentType(converterService.getMimeType("pdf"));
                response.setHeader("Content-Disposition", "attachment; filename=\""
                        + StringUtils.substringBeforeLast(node.getName(), ".") + ".pdf\"");
                is = node.getFileContent().downloadFile();
                converterService.convert(is,FilenameUtils.getExtension(node.getName()),response.getOutputStream(),converterService.getMimeType("pdf"));
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Path should be a file");
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(),e);
        } finally {
            IOUtils.closeQuietly(is);
        }

        return null;
    }

    /**
     * @param converterService the converterService to set
     */
    public void setConverterService(DocumentConverterService converterService) {
        this.converterService = converterService;
    }

    public static String getToPDFServletPath() {
        return "/cms/toPDF";
    }
}