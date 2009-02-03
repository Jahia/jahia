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

 package org.jahia.ajax.webdav;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.engines.filemanager.TableEntry;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRNodeWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Struts AJAX based Action that collects the disabled/enabled fileManager actions for a given WEBDAV object
 *
 * @author Xavier Lawrence
 */
public class GetFileManagerToolbarAfterSelect extends DAVAbstractAction {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(GetFileManagerToolbarAfterSelect.class);

    private static final String NEW_DIR = "newDir";
    private static final String FILE_UPLOAD = "fileUpload";
    private static final String UNZIP = "unzip";
    private static final String MOVE = "move";
    private static final String RENAME = "rename";
    private static final String DELETE = "delete";
    private static final String LOCK_FILE = "lockFile";
    private static final String COPY = "copy";
    private static final String ZIP = "zip";

    public ActionForward execute(final ActionMapping mapping,
                                 final ActionForm form,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws IOException, ServletException {

        try {
            final String path = getXmlNodeValue(request, KEY);
            final ProcessingContext jParams = retrieveProcessingContext(request, response);

            logger.debug("Getting FileManager rights for object: " + path);

            final JCRNodeWrapper df = jahiaWebdavBaseService.getDAVFileAccess(
                    TableEntry.javascriptDecode(path), jParams.getUser());

            final boolean isWriteable = df.hasPermission(JCRNodeWrapper.WRITE) && df.isWriteable();
            final boolean isDir = df.isCollection();

            final Document resp = getNewDocument(request);
            final Element root = resp.createElement("GetFileManagerToolbarAfterSelectResp");
            final Element item = resp.createElement("object");
            if (isDir && isWriteable) {
                item.setAttribute(NEW_DIR, String.valueOf(true));
                item.setAttribute(FILE_UPLOAD, String.valueOf(true));
                item.setAttribute(UNZIP, String.valueOf(true));
            } else {
                item.setAttribute(NEW_DIR, String.valueOf(false));
                item.setAttribute(FILE_UPLOAD, String.valueOf(false));
                item.setAttribute(UNZIP, String.valueOf(false));
            }

            item.setAttribute(MOVE, String.valueOf(false));
            item.setAttribute(RENAME, String.valueOf(false));
            item.setAttribute(DELETE, String.valueOf(false));
            item.setAttribute(LOCK_FILE, String.valueOf(false));
            item.setAttribute(COPY, String.valueOf(false));
            item.setAttribute(ZIP, String.valueOf(false));

            item.setAttribute("writeable", String.valueOf(isWriteable));
            item.setAttribute("isDir", String.valueOf(isDir));

            item.setAttribute(KEY, path);
            root.appendChild(item);
            resp.appendChild(root);
            sendResponse(resp, response);

        } catch (Exception e) {
            handleException(e, request, response);
        }
        return null;
    }
}
