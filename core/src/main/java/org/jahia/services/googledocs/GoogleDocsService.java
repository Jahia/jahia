/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.googledocs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections.FastHashMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;

import com.google.gdata.client.GoogleAuthTokenFactory.UserToken;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListEntry.MediaType;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.data.docs.FolderEntry;
import com.google.gdata.data.media.MediaSource;
import com.google.gdata.data.media.MediaStreamSource;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.ServiceException;

/**
 * Document service that uses Google Data API to view/edit documents or perform
 * different format conversions.
 * 
 * @author Sergiy Shyrkov
 */
public class GoogleDocsService {

    /**
     * Contains mapping for MIME types and allowed Google Docs export formats.
     * 
     * @author Sergiy Shyrkov
     */
    public static class GoogleDocsExportFormats {

        @SuppressWarnings("unchecked")
        private Map<String, List<String>> exportFormats = new FastHashMap();

        @SuppressWarnings("unchecked")
        private Map<String, String> mimeTypeMappings = new FastHashMap();

        public GoogleDocsExportFormats(Map<String, List<String>> exportFormats, Map<String, String> mimeTypeMappings) {
            super();
            this.exportFormats.putAll(exportFormats);
            ((FastHashMap) this.exportFormats).setFast(true);
            this.mimeTypeMappings.putAll(mimeTypeMappings);
            ((FastHashMap) this.mimeTypeMappings).setFast(true);
        }

        /**
         * Returns the document type (<code>document</code>,
         * <code>presentation</code>, <code>spreadsheet</code> or
         * <code>undefined</code>) depending on the provided MIME type and
         * configured mappings. If the MIME type does not match any document
         * types, <code>undefined</code> type is returned.
         * 
         * @param mimeType the MIME type to lookup mapping for
         * @return the document type (<code>document</code>,
         *         <code>presentation</code>, <code>spreadsheet</code> or
         *         <code>undefined</code>) depending on the provided MIME type
         *         and configured mappings. If the MIME type does not match any
         *         document types, <code>undefined</code> type is returned
         */
        public String getDocumentFormat(String mimeType) {
            if (mimeType == null) {
                return "undefined";
            }

            String type = mimeTypeMappings.get(mimeType);
            if (type == null) {
                for (Map.Entry<String, String> typeMapping : mimeTypeMappings.entrySet()) {
                    if (Pattern.matches(typeMapping.getKey(), mimeType)) {
                        type = typeMapping.getValue();
                        break;
                    }
                }
                if (type == null) {
                    type = "undefined";
                }
                mimeTypeMappings.put(mimeType, type);
            }

            return type;
        }

        public List<String> getExportFormats(String mimeType) {
            List<String> formats = exportFormats.get(getDocumentFormat(mimeType));
            if (formats == null) {
                formats = Collections.emptyList();
            }

            return formats;
        }
    }

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(GoogleDocsService.class);

    private String applicationName = "Jahia-xCM-v6.5";

    private String defaultTargetFolderName = "jahia-xcm";

    private DocsService service;

    protected GoogleDocsService(String userToken) throws AuthenticationException {
        super();
        service = new DocsService(applicationName);
        service.setUserToken(userToken);
    }

    protected GoogleDocsService(String username, String password) throws AuthenticationException {
        super();
        service = new DocsService(applicationName);
        service.setUserCredentials(username, password);
    }

    /**
     * Deletes the specified document from the Google Docs.
     * 
     * @param document the document to delete
     * @throws MalformedURLException in case of a bad document URL
     * @throws IOException in case of I/O errors
     * @throws ServiceException in case of Google Data API errors
     */
    public void delete(DocumentListEntry document) throws MalformedURLException, IOException, ServiceException {
        service.delete(new URL(document.getEditLink().getHref() + "?delete=true"), "*");
    }

    /**
     * Deletes the specified document from the Google Docs.
     * 
     * @param documentId the ID of the document to be deleted
     * @throws MalformedURLException in case of a bad document URL
     * @throws IOException in case of I/O errors
     * @throws ServiceException in case of Google Data API errors
     */
    public void delete(String documentId) throws MalformedURLException, IOException, ServiceException {
        service.delete(new URL("https://docs.google.com/feeds/default/private/full/" + documentId), "*");
    }

    /**
     * Obtains the content stream for the specified resource.
     * 
     * @param exportUrl the URI to use for content export
     * @return the content stream for the specified resource
     * @throws MalformedURLException in case of a bad document URL
     * @throws IOException in case of I/O errors
     * @throws ServiceException in case of Google Data API errors
     */
    public InputStream downloadFile(String exportUrl) throws IOException, MalformedURLException, ServiceException {
        StopWatch watch = new StopWatch();
        watch.start();
        logger.info("Retrieving content for document from: " + exportUrl);

        MediaContent mc = new MediaContent();
        mc.setUri(exportUrl);
        MediaSource ms = service.getMedia(mc);

        InputStream inStream = ms.getInputStream();

        watch.stop();
        logger.info("Got content stream in " + watch.getTime() + " ms");

        return inStream;
    }

    /**
     * Stores the content of the specified resource into the target file.
     * 
     * @param exportUrl the URI to use for content export
     * @param file the target file to store content into
     * @throws MalformedURLException in case of a bad document URL
     * @throws IOException in case of I/O errors
     * @throws ServiceException in case of Google Data API errors
     */
    public void downloadFile(String exportUrl, File file) throws IOException, MalformedURLException, ServiceException {
        FileOutputStream outStream = null;

        try {
            outStream = new FileOutputStream(file);
            downloadFile(exportUrl, outStream);
        } finally {
            IOUtils.closeQuietly(outStream);
        }
    }

    /**
     * Writes the content of the specified resource into the target output
     * stream.
     * 
     * @param exportUrl the URI to use for content export
     * @param out the target output stream to write content into
     * @throws MalformedURLException in case of a bad document URL
     * @throws IOException in case of I/O errors
     * @throws ServiceException in case of Google Data API errors
     */
    public void downloadFile(String exportUrl, OutputStream out) throws IOException, MalformedURLException,
            ServiceException {
        StopWatch watch = new StopWatch();
        watch.start();
        logger.info("Exporting document from: " + exportUrl);

        MediaContent mc = new MediaContent();
        mc.setUri(exportUrl);
        MediaSource ms = service.getMedia(mc);

        InputStream inStream = null;

        try {
            inStream = ms.getInputStream();
            IOUtils.copy(inStream, out);
            out.flush();
        } finally {
            IOUtils.closeQuietly(inStream);
        }

        watch.stop();
        logger.info("Downloaded file in " + watch.getTime() + " ms");
    }

    /**
     * Exports the content of the specified document to a file.
     * 
     * @param uploadedFile the document to be exported
     * @param file the target file to store content into
     * @throws MalformedURLException in case of a bad document URL
     * @throws IOException in case of I/O errors
     * @throws ServiceException in case of Google Data API errors
     */
    public void export(DocumentListEntry uploadedFile, File file) throws MalformedURLException, IOException,
            ServiceException {
        downloadFile(getExportUrl(uploadedFile, FilenameUtils.getExtension(file.getName())), file);
    }

    /**
     * Exports the content of the specified document into a stream.
     * 
     * @param uploadedFile the document to be exported
     * @param out the output stream to write document's content into
     * @param exportFormat the target export format; see {@link MediaType} for
     *            supported formats
     * @throws MalformedURLException in case of a bad document URL
     * @throws IOException in case of I/O errors
     * @throws ServiceException in case of Google Data API errors
     */
    public void export(DocumentListEntry uploadedFile, OutputStream out, String exportFormat)
            throws MalformedURLException, IOException, ServiceException {
        downloadFile(getExportUrl(uploadedFile, exportFormat), out);
    }

    /**
     * Returns the authentication token for current session.
     * 
     * @return the authentication token for current session
     */
    public String getAuthToken() {
        return ((UserToken) service.getAuthTokenFactory().getAuthToken()).getValue();
    }

    /**
     * Returns the default document upload folder in Google Docs.
     * 
     * @return the default document upload folder in Google Docs
     * @throws IOException in case of I/O errors
     * @throws ServiceException in case of Google Data API errors
     */
    protected DocumentListEntry getDefaultTargetFolder() throws IOException, ServiceException {
        StopWatch watch = new StopWatch();
        watch.start();
        DocumentListEntry folder = null;
        DocumentListFeed feed = service.getFeed(new URL("https://docs.google.com/"
                + "feeds/default/private/full/folder%3Aroot/contents/-/folder?title=" + defaultTargetFolderName),
                DocumentListFeed.class);
        List<DocumentListEntry> entries = feed.getEntries();

        if (!entries.isEmpty()) {
            folder = entries.get(0);
        } else {
            DocumentListEntry newEntry = new FolderEntry();
            newEntry.setTitle(new PlainTextConstruct(defaultTargetFolderName));
            folder = service.insert(new URL("https://docs.google.com/feeds/default/private/full/"), newEntry);
        }

        watch.stop();
        logger.info("Getting target folder took " + watch.getTime() + " ms");

        return folder;
    }

    /**
     * Returns the URL of the default document upload folder in Google Docs.
     * 
     * @return the URL of the default document upload folder in Google Docs
     * @throws IOException in case of I/O errors
     * @throws ServiceException in case of Google Data API errors
     */
    public URL getDefaultTargetFolderURL() throws IOException, ServiceException {
        return StringUtils.isNotEmpty(defaultTargetFolderName) ? new URL(((MediaContent) getDefaultTargetFolder()
                .getContent()).getUri()) : new URL("https://docs.google.com/feeds/default/private/full/");
    }

    /**
     * Returns the URL for exporting the content of the specified file.
     * 
     * @param uploadedFile the document to be exported
     * @param exportFormat the target export format; see {@link MediaType} for
     *            supported formats.
     * @return the URL for exporting the content of the specified file
     */
    public String getExportUrl(DocumentListEntry uploadedFile, String exportFormat) {
        StringBuilder buff = new StringBuilder(64);
        buff.append(((MediaContent) uploadedFile.getContent()).getUri());
        buff.append("&exportFormat=").append(exportFormat);
        // If exporting to .csv or .tsv, add the gid parameter to specify
        // which sheet to export
        if ("csv".equals(exportFormat) || "tsv".equals(exportFormat)) {
            // gid=0 will download only the first sheet
            buff.append("&gid=0");
        }
        return buff.toString();
    }

    protected void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    protected void setDefaultTargetFolderName(String targetFolderName) {
        this.defaultTargetFolderName = targetFolderName;
    }

    /**
     * Performs upload of the specified file into Google Docs.
     * 
     * @param file the file to be uploaded
     * @param targetFolderUri the URL of the target upload folder
     * @return the uploaded document entry
     * @throws IOException in case of I/O errors
     * @throws ServiceException in case of Google Data API errors
     */
    protected DocumentListEntry upload(File file, URL targetFolderUri) throws IOException, ServiceException {
        StopWatch watch = new StopWatch();
        watch.start();
        logger.info("Uploading content for: " + file.getName());
        DocumentListEntry newDocument = new DocumentListEntry();
        String mimeType = DocumentListEntry.MediaType.fromFileName(file.getName()).getMimeType();
        newDocument.setFile(file, mimeType);
        newDocument.setTitle(new PlainTextConstruct(file.getName()));

        DocumentListEntry uploadedFile = service.insert(targetFolderUri, newDocument);

        watch.stop();
        logger.info("File " + file + " uploaded in " + watch.getTime() + " ms");

        return uploadedFile;
    }

    /**
     * Performs upload of the provided content into Google Docs.
     * 
     * @param fileStream the content to be uploaded
     * @param documentTitle the target document file name
     * @param mimeType the target MIME type
     * @return the uploaded document entry
     * @throws IOException in case of I/O errors
     * @throws ServiceException in case of Google Data API errors
     */
    public DocumentListEntry upload(InputStream fileStream, String documentTitle, String mimeType) throws IOException,
            ServiceException {
        return upload(fileStream, documentTitle, mimeType, getDefaultTargetFolderURL());
    }

    /**
     * Performs upload of the provided content into Google Docs.
     * 
     * @param fileStream the content to be uploaded
     * @param documentTitle the target document file name
     * @param mimeType the target MIME type
     * @param targetFolderUri the URL of the target upload folder
     * @return the uploaded document entry
     * @throws IOException in case of I/O errors
     * @throws ServiceException in case of Google Data API errors
     */
    protected DocumentListEntry upload(InputStream fileStream, String documentTitle, String mimeType,
            URL targetFolderUri) throws IOException, ServiceException {
        StopWatch watch = new StopWatch();
        watch.start();
        logger.info("Uploading content for: " + documentTitle);

        DocumentListEntry newDocument = new DocumentListEntry();
        newDocument.setTitle(new PlainTextConstruct(documentTitle));

        MediaContent content = new MediaContent();
        content.setMediaSource(new MediaStreamSource(fileStream, mimeType));
        content.setMimeType(new ContentType(mimeType));
        newDocument.setContent(content);

        DocumentListEntry uploadedFile = service.insert(targetFolderUri, newDocument);

        watch.stop();
        logger.info("File " + documentTitle + " uploaded in " + watch.getTime() + " ms");

        return uploadedFile;
    }
}
