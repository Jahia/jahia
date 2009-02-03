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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.engines.filemanager.TableEntry;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRNodeWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Struts AJAX based Action that collects the disabled/enabled fileManager actions for a given WEBDAV object
 *
 * @author Xavier Lawrence
 */
public class GetFileManagerToolBar extends DAVAbstractAction {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(GetFileManagerToolBar.class);

    public static final String TOOLBAR_VALUES = "toolbarValues";

    private static final String CHECKED_ITEMS = "checkedItems";
    private static final String CHECKED = "checked";
    private static final String NEW_DIR = "newDir";
    private static final String FILE_UPLOAD = "fileUpload";
    private static final String UNZIP = "unzip";
    private static final String MOVE = "move";
    private static final String RENAME = "rename";
    private static final String DELETE = "delete";
    private static final String LOCK_FILE = "lockFile";
    private static final String COPY = "copy";
    private static final String ZIP = "zip";

    private static final String SHARED = "/shared";
    private static final String GROUPS = "/groups";
    private static final String USERS = "/users";

    private static final List excludeList = new ArrayList(3);

    static {
        excludeList.add(SHARED);
        excludeList.add(GROUPS);
        excludeList.add(USERS);
    }

    public ActionForward execute(final ActionMapping mapping,
                                 final ActionForm form,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws IOException, ServletException {
        try {
            final String[] values = getMultipleXmlNodeValue(request, KEY, CHECKED);
            final String path = values[0];
            final boolean checked = Boolean.valueOf(values[1]).booleanValue();
            final ProcessingContext jParams = retrieveProcessingContext(request, response);

            if (logger.isDebugEnabled()) {
                logger.debug("Getting FileManager rights for object: " + path + "-" + checked);
            }

            final JCRNodeWrapper df = jahiaWebdavBaseService.getDAVFileAccess(
                    TableEntry.javascriptDecode(path), jParams.getUser());

            final boolean isReadable = df.hasPermission(JCRNodeWrapper.READ);
            final boolean isWriteable = df.hasPermission(JCRNodeWrapper.WRITE) && df.isWriteable() && ! excludeList.contains(path) &&
                    ! path.endsWith("/public") && ! path.endsWith("/private");
            final boolean isLocked = df.isLocked();
            final boolean isUnlockable = isLocked && (df.getLockOwner().equals(jParams.getUser().getUsername())
                    || jParams.getUser().isAdminMember(jParams.getSiteID()));
            final boolean isDir = df.isCollection();

            final HttpSession session = request.getSession();
            Map toolbarValues = (Map) session.getAttribute(TOOLBAR_VALUES);
            if (toolbarValues == null) {
                toolbarValues = new HashMap();
                toolbarValues.put(NEW_DIR, Boolean.TRUE);
                toolbarValues.put(FILE_UPLOAD, Boolean.TRUE);
                toolbarValues.put(MOVE, Boolean.TRUE);
                toolbarValues.put(RENAME, Boolean.TRUE);
                toolbarValues.put(DELETE, Boolean.TRUE);
                toolbarValues.put(LOCK_FILE, Boolean.TRUE);
                toolbarValues.put(COPY, Boolean.TRUE);
                toolbarValues.put(ZIP, Boolean.TRUE);
                toolbarValues.put(UNZIP, Boolean.TRUE);
                toolbarValues.put(CHECKED_ITEMS, new ArrayList());
            }

            final Document resp = getNewDocument(request);
            final Element root = resp.createElement("GetFileManagerToolBarResp");
            final Element item = resp.createElement("object");
            if (checked) {
                final List vect = (List) toolbarValues.get(CHECKED_ITEMS);
                if (! vect.contains(path)) vect.add(path);
                if (! isDir && session.getAttribute("selectedFile") == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("put selectedFile: " + df.getPath());
                    }
                    session.setAttribute("selectedFile", df.getPath());
                }

                if (isDir && isWriteable) {
                    item.setAttribute(NEW_DIR, String.valueOf(getValue(toolbarValues, NEW_DIR)));
                    item.setAttribute(FILE_UPLOAD, String.valueOf(getValue(toolbarValues, FILE_UPLOAD)));
                    item.setAttribute(UNZIP, String.valueOf(getValue(toolbarValues, UNZIP)));
                } else {
                    item.setAttribute(NEW_DIR, "false");
                    toolbarValues.put(NEW_DIR, Boolean.FALSE);
                    item.setAttribute(FILE_UPLOAD, "false");
                    toolbarValues.put(FILE_UPLOAD, Boolean.FALSE);
                    item.setAttribute(UNZIP, "false");
                    toolbarValues.put(UNZIP, Boolean.FALSE);
                }
                if (isDir || (isLocked && !isUnlockable)) {
                    item.setAttribute(LOCK_FILE, "false");
                    toolbarValues.put(LOCK_FILE, Boolean.FALSE);
                } else {
                    item.setAttribute(LOCK_FILE, String.valueOf(getValue(toolbarValues, LOCK_FILE)));
                }
                if (isWriteable && !isLocked) {
                    item.setAttribute(MOVE, String.valueOf(getValue(toolbarValues, MOVE)));
                    final int size = ((List) toolbarValues.get(CHECKED_ITEMS)).size();
                    item.setAttribute(RENAME, String.valueOf(getValue(toolbarValues, RENAME) && size < 2));

                    item.setAttribute(DELETE, String.valueOf(getValue(toolbarValues, DELETE)));

                    if (isDir) {
                        item.setAttribute(LOCK_FILE, String.valueOf(false));
                        toolbarValues.put(LOCK_FILE, Boolean.valueOf(false));
                    } else {
                        item.setAttribute(LOCK_FILE, String.valueOf(getValue(toolbarValues, LOCK_FILE)));
                    }
                } else {
                    item.setAttribute(MOVE, "false");
                    toolbarValues.put(MOVE, Boolean.FALSE);
                    item.setAttribute(RENAME, "false");
                    toolbarValues.put(RENAME, Boolean.FALSE);
                    item.setAttribute(DELETE, "false");
                    toolbarValues.put(DELETE, Boolean.FALSE);
                }

                item.setAttribute("writeable", String.valueOf(isWriteable));
                item.setAttribute("isDir", String.valueOf(isDir));

                if (isReadable) {
                    item.setAttribute(COPY, String.valueOf(getValue(toolbarValues, COPY)));
                    if (isWriteable) {
                        item.setAttribute(ZIP, String.valueOf(getValue(toolbarValues, ZIP)));
                    } else {
                        item.setAttribute(ZIP, "false");
                        toolbarValues.put(ZIP, Boolean.FALSE);
                    }
                } else {
                    item.setAttribute(COPY, "false");
                    toolbarValues.put(COPY, Boolean.FALSE);
                    item.setAttribute(ZIP, "false");
                    toolbarValues.put(ZIP, Boolean.FALSE);
                }

            } else {
                if (!isDir && df.getPath().equals(session.getAttribute("selectedFile"))) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("removed selectedFile: " + df.getPath());
                    }
                    session.removeAttribute("selectedFile");
                }

                final List vect = (List) toolbarValues.get(CHECKED_ITEMS);
                vect.remove(path);

                boolean newDir = true;
                boolean fileUpload = true;
                boolean move = true;
                boolean rename = true;
                boolean delete = true;
                boolean lockFile = true;
                boolean copy = true;
                boolean zip = true;
                boolean writeable = true;
                boolean isDirectory = true;
                boolean unzip = true;

                for (int i = 0; i < vect.size(); i++) {
                    final JCRNodeWrapper df2 = jahiaWebdavBaseService.getDAVFileAccess(
                            TableEntry.javascriptDecode((String) vect.get(i)), jParams.getUser());
                    final boolean isReadable2 = df2.hasPermission(JCRNodeWrapper.READ);
                    final String path2 = df2.getPath();
                    final boolean isWriteable2 = df2.hasPermission(JCRNodeWrapper.WRITE) && df2.isWriteable() && ! excludeList.contains(path2) &&
                            ! path2.endsWith("/public") && ! path2.endsWith("/private");
                    final boolean isLocked2 = df2.isLocked();
                    final boolean isUnlockable2 = isLocked2 && (df2.getLockOwner().equals(jParams.getUser().getUsername())
                    || jParams.getUser().isAdminMember(jParams.getSiteID()));
                    final boolean isDir2 = df2.isCollection();


                    if (isDir2 && isWriteable2) {} else {
                        newDir = false;
                        fileUpload = false;
                        unzip = false;
                    }

                    if (isDir2 || (isLocked2 && !isUnlockable2)) {
                        lockFile = false;
                    }
                    if (isWriteable2 && !isLocked2) {
                        if (vect.size() > 1) {
                            rename = false;
                        }
                    } else {
                        move = false;
                        rename = false;
                        delete = false;
                    }

                    if (isReadable2) {} else {
                        copy = false;
                        zip = false;
                    }

                    writeable = writeable && isWriteable2;
                    isDirectory = isDirectory && isDir2;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("checked items: " + vect);
                }
                if (vect.size() == 0) {
                    newDir = false;
                    fileUpload = false;
                    move = false;
                    rename = false;
                    delete = false;
                    lockFile = false;
                    copy = false;
                    zip = false;
                    writeable = false;
                    isDirectory = false;
                    unzip = false;
                    session.removeAttribute(TOOLBAR_VALUES);
                    toolbarValues.clear();
                    toolbarValues.put(NEW_DIR, Boolean.TRUE);
                    toolbarValues.put(FILE_UPLOAD, Boolean.TRUE);
                    toolbarValues.put(MOVE, Boolean.TRUE);
                    toolbarValues.put(RENAME, Boolean.TRUE);
                    toolbarValues.put(DELETE, Boolean.TRUE);
                    toolbarValues.put(LOCK_FILE, Boolean.TRUE);
                    toolbarValues.put(COPY, Boolean.TRUE);
                    toolbarValues.put(ZIP, Boolean.TRUE);
                    toolbarValues.put(UNZIP, Boolean.TRUE);
                    toolbarValues.put(CHECKED_ITEMS, new ArrayList());
                }

                item.setAttribute(NEW_DIR, String.valueOf(newDir));
                item.setAttribute(FILE_UPLOAD, String.valueOf(fileUpload));
                item.setAttribute(UNZIP, String.valueOf(unzip));
                item.setAttribute(MOVE, String.valueOf(move));
                item.setAttribute(RENAME, String.valueOf(rename));
                item.setAttribute(DELETE, String.valueOf(delete));
                item.setAttribute(LOCK_FILE, String.valueOf(lockFile));
                item.setAttribute(COPY, String.valueOf(copy));
                item.setAttribute(ZIP, String.valueOf(zip));

                item.setAttribute("writeable", String.valueOf(writeable));
                if (vect.size() > 0) {
                    item.setAttribute("isDir", String.valueOf(isDirectory));
                }
            }

            item.setAttribute(KEY, path);
            root.appendChild(item);
            resp.appendChild(root);
            session.setAttribute(TOOLBAR_VALUES, toolbarValues);
            sendResponse(resp, response);

        } catch (Exception e) {
            handleException(e, request, response);
        }
        return null;
    }

    protected boolean getValue(final Map table, final String key) {
        return ((Boolean) table.get(key)).booleanValue();
    }
}
