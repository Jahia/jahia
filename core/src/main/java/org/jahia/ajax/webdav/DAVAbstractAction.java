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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang.StringUtils;
import org.jahia.ajax.AjaxAction;
import org.jahia.engines.filemanager.TableEntry;
import org.jahia.engines.filemanager.URLUtil;
import org.jahia.engines.calendar.CalendarHandler;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.webdav.JahiaWebdavBaseService;
import org.jahia.api.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Abstract Class that simply holds common methods regarding WEBDAV.
 *
 * @author Xavier Lawrence
 */
public abstract class DAVAbstractAction extends AjaxAction {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(DAVAbstractAction.class);

    private static final List excludeList = new ArrayList();
    private static final List imagesExtensions = new ArrayList();
    private static final double MEGA_BYTE = 1024 * 1024;

    protected static final String DISPLAY_PARAM = "display";
    public static final String DIRECTORIES_ONLY = "directories_Only";
    public static final String IMAGES_ONLY = "images_Only";
    public static final String FILES_ONLY = "files_Only";

    // For the listView
    public static final String LIST_VIEW = "listView";
    public static final String ICONS_VIEW = "iconsView";
    public static final String THUMBNAIL_VIEW = "thumbnailView";

    protected static final SimpleDateFormat sdf = new SimpleDateFormat( CalendarHandler.DEFAULT_DATE_FORMAT );

    static {
        excludeList.add("jcr:system");
        excludeList.add("j:system");

        imagesExtensions.add("gif");
        imagesExtensions.add("jpg");
        imagesExtensions.add("jpe");
        imagesExtensions.add("jpeg");
        imagesExtensions.add("png");
        imagesExtensions.add("bmp");
        imagesExtensions.add("tif");
        imagesExtensions.add("tiff");
    }

    protected static final JahiaWebdavBaseService jahiaWebdavBaseService = JahiaWebdavBaseService.getInstance();

    protected void processObject(final JCRNodeWrapper object,
                                 final ProcessingContext jParams,
                                 final Document resp,
                                 final Element root) {
        if (excludeList.contains(object.getName()) || !object.hasPermission(JCRNodeWrapper.READ)) return;
        final Element item = resp.createElement("object");
        final String path = object.getPath();
        item.setAttribute("key", TableEntry.javascriptEncode(path));
        item.setAttribute("id", TableEntry.javascriptEncode(path));
        item.setAttribute("url", URLUtil.URLEncode(path, "UTF-8"));

        final String display = jParams.getParameter(DISPLAY_PARAM);
        //logger.debug("display: " + display);
        if (object.isCollection()) {
            if (object.getPrimaryNodeTypeName().equals(Constants.JAHIANT_VIRTUALSITE) && !object.getName().equals(jParams.getSiteKey())) {
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("processDirectory: " + object.getName() + ", " + object.getPath());
            }
            if (display != null && display.indexOf("actmove")>-1) {
                final List filesToMoveList = (List) jParams.getSessionState().getAttribute("filesToMoveList");
                if (filesToMoveList != null && filesToMoveList.contains(path)) return;
            } else if (display != null && display.indexOf("actcopy")>-1) {
                final List filesToCopyList = (List) jParams.getSessionState().getAttribute("filesToMoveList");
                if (filesToCopyList != null && filesToCopyList.contains(path)) return;
            }
            processDirectory(object, jParams, resp, root, item);
        } else {
            if (display == null || display.indexOf(DIRECTORIES_ONLY) < 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("processFile: " + object.getName() + ", " + object.getPath());
                }
                processFile(object, jParams, resp, root, item, display);
            } else {
                return;
            }
        }
        final String startingPath = (String) jParams.getSessionState().getAttribute(jParams.getSiteID() + "-path");
        if (! hasChildren(object, display != null && display.indexOf(DIRECTORIES_ONLY) > -1) ||
                startingPath != null && path.startsWith(startingPath) && ! path.equals(startingPath) && (display != null && display.indexOf("actmove")>-1)) {
            item.setAttribute("NoChildren", "NoChildren");
        }
        item.setAttribute("props", "props");
        final boolean isWriteable = object.hasPermission(JCRNodeWrapper.WRITE) && object.isWriteable();
        item.setAttribute("canWrite", String.valueOf(isWriteable));
        item.setAttribute("canRead", String.valueOf(object.hasPermission(JCRNodeWrapper.READ)));
        item.setAttribute("info", "info");
        item.setAttribute("lastModif", getFormatedDate(object.getLastModifiedAsDate()));
        item.setAttribute("fileSize", getFileSize(object));
    }

    protected void processFile(final JCRNodeWrapper file,
                               final ProcessingContext jParams,
                               final Document resp,
                               final Element root,
                               final Element item,
                               final String display) {
        final String path = file.getPath();
        final int index = path.lastIndexOf('.');
        final String extension;
        if (index >= 0) {
            extension = path.substring(index + 1).toLowerCase();
            if (logger.isDebugEnabled()) {
                logger.debug("extension: " + extension);
            }
        } else {
            extension = "";
        }

        if (display != null && display.length() > 0) {
            if (display.indexOf(IMAGES_ONLY) > -1) {
                logger.debug("Should only display images");
                if (! imagesExtensions.contains(extension)) return;
            } else if (display.indexOf(FILES_ONLY) > -1) {
            	// display all files except images
                if (imagesExtensions.contains(extension)) return;
            }

            if (display.indexOf('|') != -1
                    && display.indexOf('|') < display.length() - 1) {
                String[] filters = StringUtils.split(display.substring(display
                        .lastIndexOf('|') + 1), ',');
                boolean matched = false;
                for (int i = 0; i < filters.length; i++) {
                    if (FilenameUtils.wildcardMatch(file.getName(), filters[i],
                            IOCase.INSENSITIVE)) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    return;
                }
            }
        }
        final String fileName = file.getName();
        final String realName = path.substring(path.lastIndexOf("/") + 1);
//        if (fileName.equals(realName)) {
            item.setAttribute("title", fileName);
//        } else {
//            item.setAttribute("title", realName + " (" + fileName + ")");
//        }
        item.setAttribute("dir", extension);
        if (file.isLocked()) {
            item.setAttribute("locked", "locked");
        }
        root.appendChild(item);
    }

    protected void processDirectory(final JCRNodeWrapper directory,
                                    final ProcessingContext jParams,
                                    final Document resp,
                                    final Element root,
                                    final Element item) {

        final String name = directory.getName();
        if (name == null || name.length() == 0) {
            item.setAttribute("title", jParams.getServerName());
            item.setAttribute("dir", "root");
        } else {
            String path = directory.getPath();
            if (path.endsWith("/")) {
                path = path.substring(0, path.length()-1);
            }
            final String realName = path.substring(path.lastIndexOf("/") + 1);
//            if (name.equals(realName) ) {
            item.setAttribute("title", name);
//            } else {
//                item.setAttribute("title", realName + " (" + name + ")");
//            }
            item.setAttribute("dir", "dir");
        }
        root.appendChild(item);
    }

    /**
     * Checks if the given FileNode object has children that can be displayed in the given situation or not.
     * @param object The FileNode instance to check children existance from
     * @return true if at least 1 child is found
     */
    protected boolean hasChildren(final JCRNodeWrapper object, final boolean dirOnly) {
        if (object.isCollection()) {
            if (org.jahia.settings.SettingsBean.getInstance().isPreloadFolders()) {
                final List children = object.getChildren();
                if (children == null || children.size() == 0) return false;
                for (int i = 0; i < children.size(); i++) {
                    final JCRNodeWrapper child = (JCRNodeWrapper) children.get(i);
                    if (dirOnly) {
                        if (child.hasPermission(JCRNodeWrapper.READ) && child.isCollection()) return true;
                    } else {
                        if (child.hasPermission(JCRNodeWrapper.READ)) return true;
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     *
     */
    public static String getFileSize(final JCRNodeWrapper curObj) {
        String fileSize = "";
        final StringBuffer buff = new StringBuffer();
        if (curObj != null) {
            if (curObj.isCollection()) return " ";
            final long size = curObj.getFileContent().getContentLength();
            if (size == 0) return fileSize;
            if (size < 1024) {
                buff.append(size).append(" bytes");

            } else if (size >= MEGA_BYTE) {
                final double fileSizeMb = (double)size / MEGA_BYTE;
                final DecimalFormat df = new DecimalFormat("####.##");
                buff.append(df.format(fileSizeMb)).append(" MB");

            } else {
                final double fileSizeKb = (double)size / 1024.0;
                final DecimalFormat df = new DecimalFormat("###.##");
                buff.append(df.format(fileSizeKb)).append(" KB");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("fileSize: " + buff);
            }
            return buff.toString();
        } else {
            return fileSize;
        }
    }

    /**
     *
     */
    public static int getFileSizeInBytes(final JCRNodeWrapper curObj) {
        if (curObj != null) {
            if (curObj.isCollection()) return 0;
            return (int) curObj.getFileContent().getContentLength();
        } else {
            return 0;
        }
    }

    public static String getFormatedString(String value) {
        if (value == null) return "-";
        return value;
    }

    public static SimpleDateFormat getDateFormat() {
        return sdf;
    }

    public static String getFormatedDate(final Date value) {
        if (value == null) return "-";
        return sdf.format(value);
    }

    /**
     *
     */
    public static String getLastModifUser(final JCRNodeWrapper file) {
        if (file == null) return "-";
        final String res = file.getModificationUser();
        if (res == null) return "-";
        return res;
    }

    /**
     *
     */
    public static String getCreator(final JCRNodeWrapper file) {
        if (file == null) return "-";
        final String res = file.getCreationUser();
        if (res == null) return "-";
        return res;
    }
}
