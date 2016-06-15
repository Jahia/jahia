/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.content.server;

import com.google.gwt.safehtml.shared.UriUtils;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadBase.FileUploadIOException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.LimitedInputStream;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.helper.VersioningHelper;
import org.jahia.ajax.gwt.helper.ZipHelper;
import org.jahia.api.Constants;
import org.jahia.bin.SessionNamedDataStorage;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRVersionService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

/**
 * File upload servlet to handle requests from GWT upload form.
 *
 * @author rfelden
 * @version 2 avr. 2008 - 16:51:39
 */
public class GWTFileManagerUploadServlet extends HttpServlet {

    private static final int OK = 0;
    private static final int EXISTS = 1;
    private static final int READONLY = 2;
    private static final int BAD_LOCATION = 3;
    private static final int UNKNOWN_ERROR = 9;

    private static final Logger logger = LoggerFactory.getLogger(GWTFileManagerUploadServlet.class);
    private static final long serialVersionUID = 1048509772346464862L;

    private volatile SessionNamedDataStorage<UploadedPendingFile> fileStorage;

    private static long getContentLength(FileItemHeaders pHeaders) {
        try {
            return Long.parseLong(pHeaders.getHeader(FileUploadBase.CONTENT_LENGTH));
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        SettingsBean settingsBean = SettingsBean.getInstance();
        final long fileSizeLimit = settingsBean.getJahiaFileUploadMaxSize();
        upload.setHeaderEncoding("UTF-8");
        Map<String, FileItem> uploads = new HashMap<String, FileItem>();
        String location = null;
        String type = null;
        boolean unzip = false;
        response.setContentType("text/plain; charset=" + settingsBean.getCharacterEncoding());
        final PrintWriter printWriter = response.getWriter();
        try {
            FileItemIterator itemIterator = upload.getItemIterator(request);
            FileSizeLimitExceededException sizeLimitExceededException = null;
            while (itemIterator.hasNext()) {
                final FileItemStream item = itemIterator.next();
                if (sizeLimitExceededException != null) {
                    continue;
                }
                FileItem fileItem = factory.createItem(item.getFieldName(), item.getContentType(), item.isFormField(),
                        item.getName());
                long contentLength = getContentLength(item.getHeaders());

                // If we have a content length in the header we can use it
                if (fileSizeLimit > 0 && contentLength != -1L && contentLength > fileSizeLimit) {
                    throw new FileSizeLimitExceededException("The field " + item.getFieldName()
                            + " exceeds its maximum permitted size of " + fileSizeLimit + " bytes.", contentLength,
                            fileSizeLimit);
                }
                InputStream itemStream = item.openStream();

                InputStream limitedInputStream = null;
                try {
                    limitedInputStream = fileSizeLimit > 0 ? new LimitedInputStream(itemStream, fileSizeLimit) {

                        @Override
                        protected void raiseError(long pSizeMax, long pCount) throws IOException {
                            throw new FileUploadIOException(new FileSizeLimitExceededException("The field "
                                    + item.getFieldName() + " exceeds its maximum permitted size of " + fileSizeLimit
                                    + " bytes.", pCount, pSizeMax));
                        }
                    } : itemStream;

                    Streams.copy(limitedInputStream, fileItem.getOutputStream(), true);
                } catch (FileUploadIOException e) {
                    if (e.getCause() instanceof FileSizeLimitExceededException) {
                        if (sizeLimitExceededException == null) {
                            sizeLimitExceededException = (FileSizeLimitExceededException) e.getCause();
                        }
                    } else {
                        throw e;
                    }
                } finally {
                    IOUtils.closeQuietly(limitedInputStream);
                }

                if ("unzip".equals(fileItem.getFieldName())) {
                    unzip = true;
                } else if ("uploadLocation".equals(fileItem.getFieldName())) {
                    location = fileItem.getString("UTF-8");
                } else if ("asyncupload".equals(fileItem.getFieldName())) {
                    String name = fileItem.getName();
                    if (name.trim().length() > 0) {
                        uploads.put(extractFileName(name, uploads), fileItem);
                    }
                    type = "async";
                } else if (!fileItem.isFormField() && fileItem.getFieldName().startsWith("uploadedFile")) {
                    String name = fileItem.getName();
                    if (name.trim().length() > 0) {
                        uploads.put(extractFileName(name, uploads), fileItem);
                    }
                    type = "sync";
                }
            }
            if (sizeLimitExceededException != null) {
                throw sizeLimitExceededException;
            }
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            printWriter.write("UPLOAD-SIZE-ISSUE: " + getSizeLimitErrorMessage(fileSizeLimit, e, request) + "\n");
            return;
        } catch (FileUploadIOException e) {
            if (e.getCause() != null && (e.getCause() instanceof FileSizeLimitExceededException)) {
                printWriter.write("UPLOAD-SIZE-ISSUE: " + getSizeLimitErrorMessage(fileSizeLimit, (FileSizeLimitExceededException) e.getCause(), request) + "\n");
            } else {
                logger.error("UPLOAD-ISSUE", e);
                printWriter.write("UPLOAD-ISSUE: " + e.getLocalizedMessage() + "\n");
            }
            return;
        } catch (FileUploadException e) {
            logger.error("UPLOAD-ISSUE", e);
            printWriter.write("UPLOAD-ISSUE: " + e.getLocalizedMessage() + "\n");
            return;
        }

        if (type == null || type.equals("sync")) {
            response.setContentType("text/plain");

            final JahiaUser user = (JahiaUser) request.getSession().getAttribute(Constants.SESSION_USER);

            final List<String> pathsToUnzip = new ArrayList<String>();
            for (String fileName : uploads.keySet()) {
                final FileItem fileItem = uploads.get(fileName);
                try {
                    StringBuilder name = new StringBuilder(fileName);
                    final int saveResult = saveToJcr(user, fileItem, location, name);
                    switch (saveResult) {
                        case OK:
                            if (unzip && fileName.toLowerCase().endsWith(".zip")) {
                                pathsToUnzip.add(new StringBuilder(location).append("/").append(name.toString()).toString());
                            }
                            printWriter.write("OK: " + UriUtils.encode(name.toString()) + "\n");
                            break;
                        case EXISTS:
                            storeUploadedFile(request.getSession().getId(), fileItem);
                            printWriter.write("EXISTS: " + UriUtils.encode(fileItem.getFieldName()) + " " + UriUtils.encode(fileItem.getName()) + " " + UriUtils.encode(fileName) + "\n");
                            break;
                        case READONLY:
                            printWriter.write("READONLY: " + UriUtils.encode(fileItem.getFieldName()) + "\n");
                            break;
                        default:
                            printWriter.write("UPLOAD-FAILED: " + UriUtils.encode(fileItem.getFieldName()) + "\n");
                            break;
                    }
                } catch (IOException e) {
                    logger.error("Upload failed for file \n", e);
                } finally {
                    fileItem.delete();
                }
            }

            // direct blocking unzip
            if (unzip && pathsToUnzip.size() > 0) {
                try {
                    ZipHelper zip = ZipHelper.getInstance();
                    //todo : in which workspace do we upload ?
                    zip.unzip(pathsToUnzip, true, JCRSessionFactory.getInstance().getCurrentUserSession(), (Locale) request.getSession().getAttribute(Constants.SESSION_UI_LOCALE));
                } catch (RepositoryException e) {
                    logger.error("Auto-unzipping failed", e);
                } catch (GWTJahiaServiceException e) {
                    logger.error("Auto-unzipping failed", e);
                }
            }
        } else {
            response.setContentType("text/html");
            for (FileItem fileItem : uploads.values()) {
                storeUploadedFile(request.getSession().getId(), fileItem);
                printWriter.write("<html><body>");
                printWriter.write("<div id=\"uploaded\" key=\"" + fileItem.getName() + "\" name=\"" + fileItem.getName() + "\"></div>\n");
                printWriter.write("</body></html>");
            }
        }
    }

    private void storeUploadedFile(String sessionID, final FileItem fileItem) {
        try {
            final InputStream contentStream = new BufferedInputStream(fileItem.getInputStream());
            try {
                getFileStorage().put(sessionID, fileItem.getName(), new UploadedPendingFile() {

                    @Override
                    public String getContentType() {
                        return fileItem.getContentType();
                    }

                    @Override
                    public InputStream getContentStream() {
                        return contentStream;
                    }

                    @Override
                    public void close() {
                    }
                });
            } finally {
                contentStream.close();
            }
        } catch (IOException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    private String getSizeLimitErrorMessage(final long fileSizeLimit, FileSizeLimitExceededException e, HttpServletRequest request) {
        Locale locale = (Locale) request.getSession().getAttribute(Constants.SESSION_UI_LOCALE);
        if (locale == null) {
            locale = (Locale) request.getSession().getAttribute(Constants.SESSION_LOCALE);
        }
        if (logger.isDebugEnabled()) {
            logger.warn("File upload exceeding limit of " + fileSizeLimit + " bytes", e);
        } else {
            logger.warn("File upload exceeding limit of {} bytes", fileSizeLimit);
        }
        return Messages.getInternalWithArguments("fileSizeError.label", "File upload exceeding limit of {0} bytes", locale, fileSizeLimit);
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

    private int saveToJcr(JahiaUser user, FileItem item, String location, StringBuilder name) throws IOException {

        String filename = name.toString();
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
                    locationFolder.getSession().checkout(locationFolder);
                }
                JCRNodeWrapper node = locationFolder.uploadFile(filename, is, JCRContentUtils.getMimeType(filename, item.getContentType()));
                node.getSession().save();
                if (!node.getName().equals(filename)) {
                    name.delete(0, name.length());
                    name.append(node.getName());
                }
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

    @SuppressWarnings("unchecked")
    private SessionNamedDataStorage<UploadedPendingFile> getFileStorage() {
        if (fileStorage != null) {
            return fileStorage;
        }
        synchronized (this) {
            if (fileStorage != null) {
                return fileStorage;
            }
            ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
            fileStorage = (SessionNamedDataStorage<UploadedPendingFile>) context.getBean("UploadedPendingFileStorage");
            return fileStorage;
        }
    }
}
