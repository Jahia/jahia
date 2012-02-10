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

package org.jahia.ajax.gwt.content.server;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.helper.VersioningHelper;
import org.jahia.ajax.gwt.helper.ZipHelper;
import org.jahia.bin.Jahia;
import org.jahia.params.ParamBean;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRVersionService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;

/**
 * File upload servlet to handle requests from GWT upload form.
 *
 * @author rfelden
 * @version 2 avr. 2008 - 16:51:39
 */
public class GWTFileManagerUploadServlet extends HttpServlet implements HttpSessionListener {
    public static final int OK = 0;
    public static final int EXISTS = 1;
    public static final int READONLY = 2;
    public static final int BAD_LOCATION = 3;
    public static final int UNKNOWN_ERROR = 9;

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(GWTFileManagerUploadServlet.class);

    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(Jahia.getSettings().getJahiaFileUploadMaxSize());
        upload.setHeaderEncoding("UTF-8");
        Map<String, FileItem> uploads = new HashMap<String, FileItem>();
        String location = null;
        String type = null;
        boolean unzip = false;

        final PrintWriter printWriter = response.getWriter();
        try {
            List<FileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
                if ("unzip".equals(item.getFieldName())) {
                    unzip = true;
                } else if ("uploadLocation".equals(item.getFieldName())) {
                    location = item.getString("UTF-8");
                } else if ("asyncupload".equals(item.getFieldName())) {
                    String name = item.getName();
                    if (name.trim().length() > 0) {
                        uploads.put(extractFileName(name, uploads), item);
                    }
                    type = "async";
                } else if (!item.isFormField() && item.getFieldName().startsWith("uploadedFile")) {
                    String name = item.getName();
                    if (name.trim().length() > 0) {
                        uploads.put(extractFileName(name, uploads), item);
                    }
                    type = "sync";
                }
            }
        } catch (FileUploadBase.SizeLimitExceededException e) {
            Locale locale = (Locale) request.getSession().getAttribute(ParamBean.SESSION_LOCALE);
            String locMsg = null;
            try {
                String msg = JahiaResourceBundle.getJahiaInternalResource("fileSizeError.label", locale);
                locMsg = MessageFormat.format(msg,
                        Jahia.getSettings().getJahiaFileUploadMaxSize());
            } catch (Exception ex) {
                logger.debug("Error while using default engine resource bundle (internal) with locale " + locale, ex);
            }
            if (locMsg == null) {
                locMsg = "File upload exceeding limit of " + Jahia.getSettings().getJahiaFileUploadMaxSize() + " bytes";
            }
            logger.error(locMsg, e);
            printWriter.write("UPLOAD-ISSUE: " + locMsg + "\n");
            return;
        } catch (FileUploadException e) {
            logger.error("UPLOAD-ISSUE", e);
            printWriter.write("UPLOAD-ISSUE" + "\n");
            return;
        }

        if (type == null || type.equals("sync")) {
            response.setContentType("text/plain");

            final JahiaUser user = (JahiaUser) request.getSession().getAttribute(ParamBean.SESSION_USER);

            final List<String> pathsToUnzip = new ArrayList<String>();
            for (String filename : uploads.keySet()) {
                final FileItem item = uploads.get(filename);
                try {
                    final int i = writeToDisk(user, item, location, filename);
                    switch (i) {
                        case OK:
                            if (unzip && filename.toLowerCase().endsWith(".zip")) {
                                pathsToUnzip.add(new StringBuilder(location).append("/").append(filename).toString());
                            }
                            printWriter.write("OK: " + filename + "\n");
                            break;
                        case EXISTS:
                            File f = File.createTempFile("upload", null);
                            InputStream is = item.getInputStream();
                            OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
                            try {
                                IOUtils.copy(is, os);
                            } finally {
                                IOUtils.closeQuietly(os);
                                IOUtils.closeQuietly(is);
                            }
                            asyncItems.put(f.getName(), new Item(item.getName(), item.getContentType(), item.getSize(), f, request.getSession().getId()));

                            printWriter.write("EXISTS: " + item.getFieldName() + " " + f.getName() + " " + filename + "\n");
                            break;
                        case READONLY:
                            printWriter.write("READONLY: " + item.getFieldName() + "\n");
                            break;
                        default:
                            printWriter.write("UPLOAD-FAILED: " + item.getFieldName() + "\n");
                            break;
                    }
                } catch (IOException e) {
                    logger.error("Upload failed for file \n", e);
                } finally {
                    item.delete();
                }
            }

            // direct blocking unzip
            if (unzip && pathsToUnzip.size() > 0) {
                try {
                    ZipHelper zip = ZipHelper.getInstance();
                    //todo : in which workspace do we upload ?
                    zip.unzip(pathsToUnzip, true, JCRSessionFactory.getInstance().getCurrentUserSession(),(Locale) request.getSession().getAttribute(ParamBean.SESSION_UI_LOCALE));
                } catch (RepositoryException e) {
                    logger.error("Auto-unzipping failed", e);
                } catch (GWTJahiaServiceException e) {
                    logger.error("Auto-unzipping failed", e);
                }
            }
        } else {
            response.setContentType("text/html");

            for (FileItem fileItem : uploads.values()) {
                printWriter.write("<html><body>");
                File f = File.createTempFile("upload", ".tmp");
                InputStream is = fileItem.getInputStream();
                OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
                try {
                    IOUtils.copy(is, os);
                } finally {
                    IOUtils.closeQuietly(os);
                    IOUtils.closeQuietly(is);
                }
                printWriter.write("<div id=\"uploaded\" key=\"" + f.getName() + "\" name=\"" + fileItem.getName() + "\"></div>\n");
                printWriter.write("</body></html>");
                asyncItems.put(f.getName(), new Item(fileItem.getName(), fileItem.getContentType(), fileItem.getSize(), f, request.getSession().getId()));
            }
        }
    }


    private String extractFileName(String rawFileName, Map<String, FileItem> uploads) {
        String basename;
        if (rawFileName.indexOf("\\") >= 0) {
            basename = rawFileName.substring(rawFileName.lastIndexOf("\\") + 1);
        } else if (rawFileName.indexOf("/") >= 0) {
            basename = rawFileName.substring(rawFileName.lastIndexOf("/") + 1);
        } else {
            basename = rawFileName;
        }

        int i = 1;
        String name = basename;
        int dot = basename.lastIndexOf('.');
        String ext = "";
        if (dot > 0) {
            ext = basename.substring(dot);
            basename = basename.substring(0, dot);
        }
        int und = basename.lastIndexOf('-');
        if (und > -1 && basename.substring(und + 1).matches("[0-9]+")) {
            basename = basename.substring(0, und);
        }

        do {
            if (!uploads.containsKey(name)) {
                break;
            } else {
                name = basename + "-" + (i++) + ext;
            }
        } while (true);

        return name;
    }

    private int writeToDisk(JahiaUser user, FileItem item, String location, String filename) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("item : " + item);
            logger.debug("destination : " + location);
            logger.debug("filename : " + filename);
            logger.debug("size : " + item.getSize());
        }
        if (item == null || location == null || filename == null) {
            return UNKNOWN_ERROR;
        }


        JCRNodeWrapper locationFolder;
        try {
            locationFolder = JCRSessionFactory.getInstance().getCurrentUserSession().getNode(location);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            return BAD_LOCATION;
        }

        if (!locationFolder.hasPermission("jcr:addChildNodes") || locationFolder.isLocked()) {
            logger.debug("destination is not writable for user " + user.getName());
            return READONLY;
        }
        try {
            if (locationFolder.hasNode(JCRContentUtils.escapeLocalNodeName(filename))) {
                return EXISTS;
            }
            InputStream is = item.getInputStream();
            try {
                boolean versioningAvailable = false;
                if (locationFolder.getProvider().isVersioningAvailable()) {
                    versioningAvailable = true;
                }
                if (versioningAvailable) {
                    locationFolder.getSession().getWorkspace().getVersionManager().checkout(locationFolder.getPath());
                }
                JCRNodeWrapper node = locationFolder.uploadFile(filename, is, item.getContentType());
                node.save();
                // Handle potential move of the node after save
                node = node.getSession().getNodeByIdentifier(node.getIdentifier());
                if (node.getProvider().isVersioningAvailable()) {
                    node.checkpoint();
                    JCRVersionService.getInstance().addVersionLabel(node, VersioningHelper.getVersionLabel(node.getProperty("jcr:created").getDate().getTime().getTime()));
                }
            } finally {
                IOUtils.closeQuietly(is);
            }
            locationFolder.saveSession();
        } catch (RepositoryException e) {
            logger.error("exception ", e);
            return UNKNOWN_ERROR;
        }
        return OK;
    }

    private static Map<String, Item> asyncItems = new HashMap<String, Item>();

    public static class Item {
        private String contentType;
        private long length;
        private File file;
        private String sessionId;
        private String originalFileName;

        Item(String originalFileName, String contentType, long length, final File file, String sessionId) throws FileNotFoundException {
            this.originalFileName = originalFileName;
            this.contentType = contentType;
            this.length = length;
            this.file = file;
            this.sessionId = sessionId;
        }

        public String getOriginalFileName() {
            return originalFileName;
        }

        public InputStream getStream() throws FileNotFoundException {
            return new BufferedInputStream(new FileInputStream(file));
        }

        public String getContentType() {
            return contentType;
        }

        public long getLength() {
            return length;
        }

        public File getFile() {
            return file;
        }

        public String getSessionId() {
            return sessionId;
        }

        /**
         * Deletes the corresponding file and cleans up its reference.
         */
        public void dispose() {
            if (file != null) {
                asyncItems.remove(file.getName());
                FileUtils.deleteQuietly(file);
            }
        }
    }

    public static Item getItem(String key) {
        return asyncItems.get(key);
    }

    public void sessionCreated(HttpSessionEvent se) {
        // do nothing
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        // clean up items, belonging to this session
        String id = null;
        try {
            id = se.getSession().getId();
        } catch (Exception e) {
            logger.warn("Unable to get ID of the session. Skip cleaning up temporary uploaded files.", e);
        }
        if (id != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Checking temporary uploaded files for session with ID " + id);
            }
            for (Iterator<Map.Entry<String, Item>> iterator = asyncItems.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<String, Item> entry = iterator.next();
                Item item = entry.getValue();
                if (item.sessionId != null && id.equals(item.sessionId)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Removing item " + item.file);
                    }
                    iterator.remove();
                    item.file.delete();
                }
            }
        }
    }
}
