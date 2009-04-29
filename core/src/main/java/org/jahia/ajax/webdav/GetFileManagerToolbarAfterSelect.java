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
