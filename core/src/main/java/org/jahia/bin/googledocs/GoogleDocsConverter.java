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
import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.googledocs.GoogleDocsService;

import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListEntry.MediaType;
import com.google.gdata.util.ServiceException;

/**
 * Performs conversion of documents into specified format using Google Data API.
 * 
 * @author Sergiy Shyrkov
 */
public class GoogleDocsConverter extends GoogleDocsController {

    private static final long serialVersionUID = 6685883011235304632L;
    
    private static Logger logger = LoggerFactory.getLogger(GoogleDocsConverter.class);

    public static String getPath() {
        return "/cms/gconvert";
    }

    @Override
    protected void doAction(DocumentListEntry document, GoogleDocsService docsService, JCRNodeWrapper node,
            JCRSessionWrapper session, HttpServletRequest request, HttpServletResponse response)
            throws MalformedURLException, IOException, ServiceException {

        try {
        String fileName = node.getName();
        String exportFormat = request.getParameter("exportFormat");
        response.setContentType(MediaType.valueOf(exportFormat.toUpperCase()).getMimeType());
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + StringUtils.substringBeforeLast(fileName, ".") + "." + exportFormat + "\"");
        docsService.export(document, response.getOutputStream(), exportFormat);
        } finally {
                if (document != null) {
                    try {
                        docsService.delete(document);
                    } catch (Exception e) {
                        logger.warn("Unable to remove document " + document.getTitle(), e);
                    }
                }
        }
    }

    @Override
    protected boolean preconditionsMatched(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!super.preconditionsMatched(request, response)) {
            return false;
        }

        String exportFormat = request.getParameter("exportFormat");
        if (StringUtils.isEmpty(exportFormat)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Expected parameter 'exportFormat' is not found in the request.");
            return false;
        }

        try {
            MediaType.valueOf(exportFormat.toUpperCase());
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported export format '" + exportFormat + "'.");
            return false;
        }

        return true;
    }
}
