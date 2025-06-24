/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.i18n.Messages;
import org.owasp.encoder.Encode;
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

        final PrintWriter printWriter = response.getWriter();
        final JahiaUser user = (JahiaUser) request.getSession().getAttribute(Constants.SESSION_USER);
        if (JahiaUserManagerService.isGuest(user)) {
            printWriter.write("READONLY\n");
            return;
        }

        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        SettingsBean settingsBean = SettingsBean.getInstance();
        upload.setFileCountMax(settingsBean.getJahiaFileUploadCountMax());
        final long fileSizeLimit = settingsBean.getJahiaFileUploadMaxSize();
        upload.setHeaderEncoding("UTF-8");
        Map<String, FileItem> uploads = new HashMap<String, FileItem>();
        LinkedHashSet<FileItem> fileItems = new LinkedHashSet<>();
        try {
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
                    fileItems.add(fileItem);
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

                    if ("asyncupload".equals(fileItem.getFieldName())) {
                        String name = fileItem.getName();
                        if (name.trim().length() > 0) {
                            uploads.put(extractFileName(name, uploads), fileItem);
                        }
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

            response.setContentType("text/html");
            for (FileItem fileItem : uploads.values()) {
                storeUploadedFile(request.getSession().getId(), fileItem);
                printWriter.write("<html><body>");
                String itemName = Encode.forHtmlAttribute(fileItem.getName());
                printWriter.write("<div id=\"uploaded\" key=\"" + itemName + "\" name=\"" + itemName + "\"></div>\n");
                printWriter.write("</body></html>");
            }
        } finally {
            for (FileItem fileItem : fileItems) {
                fileItem.delete();
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
            logger.warn("File upload exceeding limit of {} bytes", fileSizeLimit, e);
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
