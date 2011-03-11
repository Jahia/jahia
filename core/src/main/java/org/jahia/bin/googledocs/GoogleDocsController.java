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

package org.jahia.bin.googledocs;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.jcr.PathNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bin.JahiaController;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.googledocs.GoogleDocsService;
import org.jahia.services.googledocs.GoogleDocsServiceFactory;
import org.springframework.web.servlet.ModelAndView;

import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

/**
 * Base controller for using Google Data API.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class GoogleDocsController extends JahiaController {

    private static final long serialVersionUID = 2548972609875824769L;
    private GoogleDocsServiceFactory docsServiceFactory;

    protected abstract void doAction(DocumentListEntry document, GoogleDocsService docsService, JCRNodeWrapper node,
            JCRSessionWrapper session, HttpServletRequest request, HttpServletResponse response) throws Exception;

    protected GoogleDocsService getDocsService(HttpServletRequest request, HttpServletResponse response)
            throws UnsupportedEncodingException, JahiaUnauthorizedException, AuthenticationException {
        return docsServiceFactory.getDocsService(request, response);
    }

    protected ModelAndView handleGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
            ServletException {
        String workspace = null;
        String nodePath = null;

        String pathInfo = request.getPathInfo();
        // we expect the format: /<workspace>/<file-node-path>
        if (pathInfo != null && pathInfo.length() > 1) {
            pathInfo = StringUtils.substringAfter(pathInfo.substring(1), "/");
            workspace = StringUtils.substringBefore(pathInfo, "/");
            nodePath = StringUtils.substringAfter(pathInfo, workspace);
        }
        // check required parameters
        if (StringUtils.isEmpty(workspace) || StringUtils.isEmpty(nodePath)
                || !Constants.EDIT_WORKSPACE.equals(workspace)
                && !Constants.LIVE_WORKSPACE.equals(workspace)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Expected data not found in the request: /<workspace>/<file-node-path>");
            return null;
        }
        try {
            if (!preconditionsMatched(request, response)) {
                return null;
            }

            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace);
            JCRNodeWrapper node = session.getNode(nodePath);
            if (node.isNodeType("nt:file")) {

                GoogleDocsService docsService = getDocsService(request, response);
                if (docsService == null) {
                    return null;
                }

                DocumentListEntry document = uploadFile(node, docsService, request, response);

                doAction(document, docsService, node, session, request, response);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Path should correspond to a file node");
            }
        } catch (PathNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return null;
    }

    protected ModelAndView handlePost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        throw new UnsupportedOperationException();
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView result = null;

        if (ServletFileUpload.isMultipartContent(request)) {
            result = handlePost(request, response);
        } else if ("get".equalsIgnoreCase(request.getMethod())) {
            result = handleGet(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Multipart data is not found in the POST request");
        }

        return result;
    }

    protected boolean preconditionsMatched(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return true;
    }

    public void setDocsServiceFactory(GoogleDocsServiceFactory docsServiceFactory) {
        this.docsServiceFactory = docsServiceFactory;
    }

    private DocumentListEntry uploadFile(JCRNodeWrapper node, GoogleDocsService docsService,
            HttpServletRequest request, HttpServletResponse response) throws IOException, ServiceException {
        DocumentListEntry document = null;
        InputStream is = null;
        String fileName = node.getName();
        try {
            is = node.getFileContent().downloadFile();
            document = docsService.upload(is, fileName, node.getFileContent().getContentType());
        } finally {
            IOUtils.closeQuietly(is);
        }
        return document;
    }

}
