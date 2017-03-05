/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
 package org.jahia.tools.files;

/*
 * FileUpload
 * Copyright (c) 2000 Jahia Ltd  All rights reserved.
 *
 */

import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemHeadersSupport;
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

    private HttpServletRequest req;

    private String savePath = "";
    private String encoding;

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

        params = new HashMap<String, List<String>>();
        paramsContentType = new HashMap<String, String>();
        files = new HashMap<String, DiskFileItem>();
        filesByFieldName = new HashMap<String, DiskFileItem>();

        parseQueryString();

        if (checkSavePath(savePath)) {
            try {
                final ServletFileUpload upload = new ServletFileUpload();
                FileItemIterator iter = upload.getItemIterator(req);
                DiskFileItemFactory factory = null;
                while (iter.hasNext()) {
                    FileItemStream item = iter.next();
                    InputStream stream = item.openStream();
                    if (item.isFormField()) {
                        final String name = item.getFieldName();
                        final List<String> v;
                        if (params.containsKey(name)) {
                            v = params.get(name);
                        } else {
                            v = new ArrayList<String>();
                            params.put(name, v);
                        }
                        v.add(Streams.asString(stream, encoding));
                        paramsContentType.put(name, item.getContentType());
                    } else {
                        if (factory == null) {
                            factory = new DiskFileItemFactory();
                            factory.setSizeThreshold(1);
                            factory.setRepository(new File(savePath));
                        }
                        DiskFileItem fileItem = (DiskFileItem) factory.createItem(
                                item.getFieldName(), item.getContentType(), item.isFormField(),
                                item.getName());
                        try {
                            Streams.copy(item.openStream(), fileItem.getOutputStream(), true);
                        } catch (FileUploadIOException e) {
                            throw (FileUploadException) e.getCause();
                        } catch (IOException e) {
                            throw new IOFileUploadException("Processing of "
                                    + FileUploadBase.MULTIPART_FORM_DATA + " request failed. "
                                    + e.getMessage(), e);
                        }
                        final FileItemHeaders fih = item.getHeaders();
                        fileItem.setHeaders(fih);
                        if (fileItem.getSize() > 0) {
                            files.put(fileItem.getStoreLocation().getName(), fileItem);
                            filesByFieldName.put(fileItem.getFieldName(), fileItem);
                        }
                    }
                }
            } catch (FileUploadException ioe) {
                logger.error("Error while initializing FileUpload class:", ioe);
                throw new IOException(ioe.getMessage());
            }
        } else {
            logger.error(
                    "FileUpload::init storage path does not exists or can write");
            throw new IOException(
                    "FileUpload::init storage path does not exists or cannot write");
        }
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
        final List<String> list = ((List<String>) params.get(paramName));
        if (list == null) return null;
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
                logger.debug("path is " + path);
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
        if (getFileItems() != null) {
            for (DiskFileItem item : getFileItems().values()) {
                if (item != null) {
                    try {
                        item.delete();
                    } catch (Exception e) {
                        if (logger.isDebugEnabled()) {
                            logger.warn(
                                    "Unable to delete the uploaded file item " + item.getName(), e);
                        } else {
                            logger.warn("Unable to delete the uploaded file item {}",
                                    item.getName());
                        }
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
            int pos = param.indexOf("=");
            if (pos > 0) {
                final String name = param.substring(0, pos);
                final String value = param.substring(pos + 1, param.length());
                final List<String> v ;
                if (params.containsKey(name)) {
                    v = (List<String>) params.get(name);
                } else {
                    v = new ArrayList<String>();
                    params.put(name,v);
                }
                v.add(value);
            }
        }
    }

}