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

package org.jahia.bin;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.PathNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.transform.DocumentConverterService;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.files.FileUpload;
import org.springframework.web.servlet.ModelAndView;

/**
 * Performs conversion of the submitted document into specified format.
 * 
 * @author Fabrice Cantegrel
 * @author Sergiy Shyrkov
 */
public class DocumentConverter extends JahiaController {

    private static Logger logger = LoggerFactory.getLogger(DocumentConverter.class);

    private DocumentConverterService converterService;

    private SettingsBean settingsBean;
    
    private boolean requireAuthenticatedUser = true;

    private ModelAndView handleGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String workspace = null;
        String nodePath = null;
        String targetFileExtension = null;

        String pathInfo = request.getPathInfo();
        // we expect the format:
        // /<workspace>/<file-node-path>.<target-file-extension>
        if (pathInfo != null && pathInfo.length() > 1) {
            pathInfo = StringUtils.substringAfter(pathInfo.substring(1), "/");
            workspace = StringUtils.substringBefore(pathInfo, "/");
            nodePath = StringUtils.substringAfter(pathInfo, workspace);
            if (nodePath.contains(".")) {
                targetFileExtension = StringUtils.substringAfterLast(nodePath, ".");
                nodePath = StringUtils.substringBeforeLast(nodePath, ".");
            } else {
                nodePath = null;
            }
        }
        // check required parameters
        if (!JCRContentUtils.isValidWorkspace(workspace) || StringUtils.isEmpty(nodePath)
                || StringUtils.isEmpty(targetFileExtension)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Expected data not found in the request."
                    + " Please check the documentation of the Jahia Document Converter Service for more details.");
            return null;
        }
        // check target format
        String targetFormat = converterService.getMimeType(targetFileExtension);
        if (targetFormat == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Cannot lookup MIME type that corresponds to file extension '" + targetFileExtension + "'");
            return null;
        }

        InputStream is = null;
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace);
            JCRNodeWrapper node = session.getNode(nodePath);
            if (node.isNodeType("nt:file")) {
                response.setContentType(targetFormat);
                response.setHeader("Content-Disposition", "attachment; filename=\""
                        + StringUtils.substringBeforeLast(node.getName(), ".") + "." + targetFileExtension + "\"");
                is = node.getFileContent().downloadFile();
                converterService.convert(is, converterService.getMimeType(FilenameUtils.getExtension(node.getName())),
                        response.getOutputStream(), targetFormat);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Path should correspond to a file node");
            }
        } catch (PathNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            IOUtils.closeQuietly(is);
        }

        return null;
    }

    private ModelAndView handlePost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        FileUpload fu = new FileUpload(request, settingsBean.getTmpContentDiskPath(), Integer.MAX_VALUE);
        if (fu.getFileItems().size() == 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No file was submitted");
            return null;
        }

        // take the first one
        DiskFileItem inputFile = fu.getFileItems().values().iterator().next();
        InputStream stream = null;
        String returnedMimeType = fu.getParameterValues("mimeType") != null ? fu.getParameterValues("mimeType")[0]
                : null;
        if (returnedMimeType == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter mimeType");
            return null;
        }

        try {
            ServletOutputStream outputStream = response.getOutputStream();

            stream = inputFile.getInputStream();
            // return a file
            response.setContentType(returnedMimeType);
            response.setHeader("Content-Disposition", "attachment; filename=\""
                    + FilenameUtils.getBaseName(inputFile.getName()) + "."
                    + converterService.getExtension(returnedMimeType) + "\"");

            converterService.convert(stream, inputFile.getContentType(), outputStream, returnedMimeType);

            try {
                outputStream.flush();
            } finally {
                outputStream.close();
            }
        } catch (Exception e) {
            logger
                    .error("Error converting uploaded file " + inputFile.getFieldName() + ". Cause: " + e.getMessage(),
                            e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Exception occurred: " + e.getMessage());
        } finally {
            IOUtils.closeQuietly(stream);
            fu.disposeItems();
        }

        return null;
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (requireAuthenticatedUser && isUserGuest()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "The service is available only for authenticated users.");
            return null;
        }

        if (!converterService.isEnabled()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Conversion service is not enabled.");
            return null;
        }
        
        ModelAndView result = null;

        if (ServletFileUpload.isMultipartContent(request) || "get".equalsIgnoreCase(request.getMethod())) {
            long startTime = System.currentTimeMillis();
            try {
                result = ServletFileUpload.isMultipartContent(request) ? handlePost(request,
                        response) : handleGet(request, response);
            } finally {
                if (logger.isInfoEnabled()) {
                    StringBuilder sb = new StringBuilder(100);
                    sb.append("Converted [").append(request.getRequestURI());
                    JCRSessionFactory jcrSessionFactory = JCRSessionFactory.getInstance();
                    if (jcrSessionFactory.getCurrentUser() != null) {
                        sb.append("] user=[").append(
                                jcrSessionFactory.getCurrentUser().getUsername());
                    }
                    sb.append("] ip=[").append(request.getRemoteAddr()).append("] sessionID=[")
                            .append(request.getSession().getId()).append("] in [")
                            .append(System.currentTimeMillis() - startTime).append("ms]");
                    logger.info(sb.toString());
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Multipart data is not found in the POST request");
            return null;
        }

        return result;
    }

    /**
     * @param converterService the converterService to set
     */
    public void setConverterService(DocumentConverterService converterService) {
        this.converterService = converterService;
    }

    /**
     * @param settingsBean the settingsBean to set
     */
    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }
    
    public static String getPath() {
        return "/cms/convert";
    }

    public void setRequireAuthenticatedUser(boolean requireAuthenticatedUser) {
        this.requireAuthenticatedUser = requireAuthenticatedUser;
    }

}
