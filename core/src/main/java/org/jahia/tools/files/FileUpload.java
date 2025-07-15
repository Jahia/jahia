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
 package org.jahia.tools.files;

/*
 * FileUpload
 * Copyright (c) 2000 Jahia Ltd  All rights reserved.
 *
 */

import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.FileUploadBase.FileUploadIOException;
import org.apache.commons.fileupload.FileUploadBase.IOFileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Class FileUpload.
 * Class to handle fileupload
 * It's essentially a wrapper for the Multipart Request class
 * of the com.oreilly.servlet.Multipart class
 *
 * @author Khue ng
 * @version 1.0
 */
public class FileUpload {
    public static final String FILEUPLOAD_ATTRIBUTE = "fileUpload";

    private static final org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger (FileUpload.class);
    private static final String UTF_8 = "UTF-8";

    private Map<String, List<String>> params;
    private Map<String, String> paramsContentType;
    private Map<String, DiskFileItem> files;
    private Map<String, DiskFileItem> filesByFieldName;

    private final HttpServletRequest req;

    private String savePath = "";
    private final String encoding;
    private boolean allowsFiles = true;

    /**
     * Constructor
     *
     * @param req
     * @param savePath the path where files should be saved
     * @param fileMaxSize the max size of file to upload
     * @param allowsFiles allows files to be uploaded or not (true by default)
     */
    public FileUpload(final HttpServletRequest req,
                      final String savePath,
                      final int fileMaxSize,
                      boolean allowsFiles)
            throws IOException {

        this.req = req;
        this.savePath = savePath;
        this.encoding = UTF_8;
        this.allowsFiles = allowsFiles;
        init();
    }

    /**
     * Constructor
     *
     * @param req
     * @param savePath the path where files should be saved
     * @param fileMaxSize the max size of file to upload
     */
    public FileUpload(final HttpServletRequest req,
                      final String savePath,
                      final int fileMaxSize)
        throws IOException {

        this.req = req;
        this.savePath = savePath;
        this.encoding = UTF_8;
        init();
    }

    /**
     * Constructor
     *
     * @param req
     * @param savePath    the path where files should be saved
     * @param fileMaxSize the max size of file to upload
     * @param charset The charset to use to decode the values (default = UTF-8)
     */
    public FileUpload(final HttpServletRequest req,
                      final String savePath,
                      final int fileMaxSize,
                      final String charset)
            throws IOException {

        this.req = req;
        this.savePath = savePath;
        if (charset == null) {
            this.encoding = UTF_8;
        } else {
            this.encoding = charset;
        }
        init();
    }

    /**
     * Init the MultiPartReq object if it's actually null
     *
     * @exception IOException
     */
    protected void init ()
        throws IOException {

        params = new HashMap<>();
        paramsContentType = new HashMap<>();
        files = new HashMap<>();
        filesByFieldName = new HashMap<>();

        parseQueryString();

        if (checkSavePath(savePath)) {
            try {
                final ServletFileUpload upload = new ServletFileUpload();
                upload.setFileCountMax(SettingsBean.getInstance().getJahiaFileUploadCountMax());
                FileItemIterator iter = upload.getItemIterator(req);
                DiskFileItemFactory factory = null;
                while (iter.hasNext()) {
                    factory = prepareUploadFileItem(iter.next(), factory, JCRSessionFactory.getInstance().getCurrentUser());
                }
            } catch (FileUploadException ioe) {
                throw new IOException(ioe.getMessage(), ioe);
            }
        } else {
            logger.error(
                    "FileUpload::init storage path does not exists or can write");
            throw new IOException(
                    "FileUpload::init storage path does not exists or cannot write");
        }
    }

    private DiskFileItemFactory prepareUploadFileItem(FileItemStream item, DiskFileItemFactory factory, JahiaUser user) throws FileUploadException, IOException {
        try (InputStream stream = item.openStream()) {
            if (item.isFormField()) {
                final String name = item.getFieldName();
                final List<String> v;
                if (params.containsKey(name)) {
                    v = params.get(name);
                } else {
                    v = new ArrayList<>();
                    params.put(name, v);
                }
                v.add(Streams.asString(stream, encoding));
                paramsContentType.put(name, item.getContentType());
            } else {
                if (!allowsFiles) {
                    logger.warn("File upload rejected user=[{}], enable debug log level for more details", user.getUsername());
                    if (logger.isDebugEnabled()) {
                        logger.debug("File uploaded rejected user=[{}] formField=[{}] fileName=[{}], requestURI=[{}]", user.getUsername(), item.getFieldName(), item.getName(), req.getRequestURI());
                    }
                    return factory;
                }
                if (factory == null) {
                    factory = new DiskFileItemFactory();
                    factory.setSizeThreshold(1);
                    factory.setRepository(new File(savePath));
                }
                DiskFileItem fileItem = (DiskFileItem) factory.createItem(item.getFieldName(), item.getContentType(),
                        item.isFormField(), item.getName());
                try {
                    Streams.copy(stream, fileItem.getOutputStream(), true);
                } catch (FileUploadIOException e) {
                    throw (FileUploadException) e.getCause();
                } catch (IOException e) {
                    throw new IOFileUploadException(
                            "Processing of " + FileUploadBase.MULTIPART_FORM_DATA + " request failed. " + e.getMessage(), e);
                }
                final FileItemHeaders fih = item.getHeaders();
                fileItem.setHeaders(fih);
                if (fileItem.getSize() > 0) {
                    String tmpFileName = fileItem.getStoreLocation().getName();
                    files.put(fileItem.getStoreLocation().getName(), fileItem);
                    filesByFieldName.put(fileItem.getFieldName(), fileItem);
                    logger.info("File uploaded user=[{}] tmpFile=[{}]", user.getUsername(), tmpFileName);
                    if (logger.isDebugEnabled()) {
                        logger.debug("File uploaded user=[{}] tmpFile=[{}] formField=[{}] fileName=[{}], requestURI=[{}]",
                                user.getUsername(), tmpFileName, item.getFieldName(), item.getName(), req.getRequestURI());
                    }
                }
            }
        }
        return factory;
    }

    /**
     * Return an Iterator of parameters name
     *
     * @return an Iterator of parameters name
     */
    public Set<String> getParameterNames () {
        return params.keySet();
    }

    public Map<String, List<String>> getParameterMap () {
        return params;
    }

    public void setParameterMap(Map<String, List<String>> params) {
        this.params = params;
    }

    /**
     * Return the values of a parameter
     *
     * @param paramName the name of the parameter
     * @return the values of a parameter as a String Array
     */
    public String[] getParameterValues (final String paramName) {
        final List<String> list = params.get(paramName);
        if (list == null) {
            return null;
        }
        String[] res = new String[list.size()];
        list.toArray(res);
        return res;
    }

    /**
     * Return the content type of a parameter
     *
     * @param paramName the name of the parameter
     * @return the values of a parameter as a String Array
     */
    public String getParameterContentType (final String paramName) {
        return paramsContentType.get(paramName);
    }

    /**
     * Return an Iterator of parameters name
     *
     * @return an Iterator of parameters name
     */
    public Set<String> getFileNames () {
        return files.keySet();
    }

    public File getFile(String n) {
        return files.get(n).getStoreLocation();
    }

    /**
	 * Returns the map of all {@link FileUpload} objects keyed by the form field
	 * name.
	 *
	 * @return the map of all {@link FileUpload} objects keyed by the form field
	 *         name
	 */
	public Map<String, DiskFileItem> getFileItems() {
		return filesByFieldName;
	}

    public String getFileSystemName(String n) {
        return files.get(n).getName();
    }

    public String getFormFieldName(String n) {
        return files.get(n).getFieldName();
    }

    public String getFileContentType(String n) {
        return files.get(n).getContentType();
    }

    public void markFilesAsConsumed() {
        files = null;
        filesByFieldName = null;
    }

    /**
     * Method checkSavePath
     * Check if the path where to save files is valid
     *
     * @param path the relative path where to save files to
     * @return true if this directory exists, and can be write to
     */
    protected boolean checkSavePath (String path) {

        if (path != null && (path.length() > 0)) {

            if (logger.isDebugEnabled()) {
                logger.debug("path is {}", path);
            }
            File tmpFile = new File(path);
            if (tmpFile.isDirectory() && tmpFile.canWrite()) {
                savePath = path;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void disposeItems() {
        if (getFileItems() == null) {
            return;
        }
        for (DiskFileItem item : getFileItems().values()) {
            if (item != null) {
                try {
                    item.delete();
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        logger.warn("Unable to delete the uploaded file item " + item.getName(), e);
                    } else {
                        logger.warn("Unable to delete the uploaded file item {}", item.getName());
                    }
                }
            }
        }
    }

    /**
     * Parse a query String and create an Map of parameter/value
     *
     */
    protected void parseQueryString () {

        if (logger.isDebugEnabled()) {
            logger.debug(req.getQueryString());
        }

        if (req.getQueryString() == null) {
            return;
        }

        final StringTokenizer tokenizer = new StringTokenizer(req.getQueryString(),
                "&");
        while (tokenizer.hasMoreTokens()) {
            final String param = tokenizer.nextToken();
            int pos = param.indexOf('=');
            if (pos > 0) {
                final String name = param.substring(0, pos);
                final String value = param.substring(pos + 1, param.length());
                final List<String> v ;
                if (params.containsKey(name)) {
                    v = params.get(name);
                } else {
                    v = new ArrayList<>();
                    params.put(name,v);
                }
                v.add(value);
            }
        }
    }

}
