/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.content.server;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.bin.Jahia;
import org.jahia.ajax.gwt.content.server.helper.FileManagerWorker;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.jcr.RepositoryException;
import java.io.*;
import java.util.*;
import java.text.MessageFormat;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 2 avr. 2008 - 16:51:39
 */
public class GWTFileManagerUploadServlet extends HttpServlet {
    public static final int OK = 0;
    public static final int EXISTS = 1;
    public static final int READONLY = 2;
    public static final int BAD_LOCATION = 3;
    public static final int UNKNOWN_ERROR = 9;

    private static Logger logger = Logger.getLogger(GWTFileManagerUploadServlet.class) ;
    private JCRStoreService jcr = ServicesRegistry.getInstance().getJCRStoreService() ;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.debug("Entered GWT upload servlet") ;

        FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(Jahia.getSettings().getJahiaFileUploadMaxSize());
        upload.setHeaderEncoding("UTF-8");
        Map<String, FileItem> uploads = new HashMap<String, FileItem>() ;
        String location = null ;
        String type = null;
        boolean unzip = false ;

        final PrintWriter printWriter = response.getWriter();
        try {
			List<FileItem> items = upload.parseRequest(request) ;
            for (FileItem item : items) {
                if ("unzip".equals(item.getFieldName())) {
                    unzip = true ;
                } else if ("uploadLocation".equals(item.getFieldName())) {
                    location = item.getString("UTF-8") ;
                } else if ("asyncupload".equals(item.getFieldName())) {
                    String name = item.getName() ;
                    if (name.trim().length() > 0) {
                        uploads.put(extractFileName(name), item) ;
                    }
                    type = "async";
                } else if (!item.isFormField() && item.getFieldName().startsWith("uploadedFile")) {
                    String name = item.getName() ;
                    if (name.trim().length() > 0) {
                        uploads.put(extractFileName(name), item) ;
                    }
                    type = "sync";
                }
            }
		} catch (FileUploadBase.SizeLimitExceededException e) {
            Locale locale = (Locale) request.getSession().getAttribute(ParamBean.SESSION_LOCALE);
            String locMsg = null ;
            try {
                ResourceBundle res = ResourceBundle.getBundle(JahiaResourceBundle.JAHIA_INTERNAL_RESOURCES, locale) ;
                locMsg = MessageFormat.format(res.getString("org.jahia.engines.filemanager.Filemanager_Engine.fileSizeError.label"),
                                              Jahia.getSettings().getJahiaFileUploadMaxSize()) ;
            } catch (Exception ex) {
                logger.debug("Error while using default engine resource bundle (" + JahiaResourceBundle.JAHIA_INTERNAL_RESOURCES + ") with locale " + locale, ex);
            }
            if (locMsg == null) {
                locMsg = "File upload exceeding limit of " + Jahia.getSettings().getJahiaFileUploadMaxSize() + " bytes" ;
            }
            logger.error(locMsg, e) ;
            printWriter.write("UPLOAD-ISSUE: "+locMsg+"\n");
			return ;
        } catch (FileUploadException e) {
            logger.error("UPLOAD-ISSUE", e) ;
            printWriter.write("UPLOAD-ISSUE"+"\n");
			return;
        }

        if (type == null || type.equals("sync")) {
            response.setContentType("text/plain");

            final JahiaUser user = (JahiaUser) request.getSession().getAttribute(ParamBean.SESSION_USER);

            boolean failed = false ;
            final List<String> pathsToUnzip = new ArrayList<String>() ;
            for (String filename: uploads.keySet()) {
                try  {
                    final FileItem item = uploads.get(filename);
                    final int i = writeToDisk(user, item, location, filename);
                    switch (i) {
                        case OK:
                            if (unzip && filename.toLowerCase().endsWith(".zip")) {
                                pathsToUnzip.add(new StringBuilder(location).append("/").append(filename).toString()) ;
                            }
                            printWriter.write("OK: " + filename+"\n");
                            break;
                        case EXISTS:
                            File f = File.createTempFile("upload", ".tmp");
                            IOUtils.copy(item.getInputStream(), new FileOutputStream(f));
                            asyncItems.put(f.getName(), new Item(item.getContentType(), item.getSize(), f));

                            printWriter.write("EXISTS: " + item.getFieldName() + " " + f.getName() + " " + filename + "\n");
                            break;
                        case READONLY:
                            printWriter.write("READONLY: " +  item.getFieldName()+"\n");
                            break;
                        default:
                            printWriter.write("UPLOAD-FAILED: " +  item.getFieldName()+"\n");
                            break;
                    }
                }  catch (IOException e) {
                    logger.error("Upload failed for file \n", e) ;
                    failed = true ;
                }
            }

            // direct blocking unzip
            if (unzip && pathsToUnzip.size() > 0) {
                try {
                    FileManagerWorker.unzip(pathsToUnzip, true, user);
                } catch (GWTJahiaServiceException e) {
                    logger.error("Auto-unzipping failed", e);
                }
            }
            // unzip archives in another thread (do not block/interrupt post response)
            /*new Thread() {
                    @Override
                    public void run() {
                        try {
                            FileManagerWorker.unzip(pathsToUnzip, true, user);
                        } catch (GWTJahiaServiceException e) {
                            logger.error("Auto-unzipping failed", e);
                        }
                    }
                }.start();*/
//            logger.debug("UPLOAD-SUCCEEDED") ;
//            printWriter.write("OK");
        } else {
            response.setContentType("text/html");

            for (FileItem fileItem : uploads.values()) {
                printWriter.write("<html><body>");
                File f = File.createTempFile("upload", ".tmp");
                IOUtils.copy(fileItem.getInputStream(), new FileOutputStream(f));
                printWriter.write("<div id=\"uploaded\" key=\""+f.getName() + "\" name=\""+fileItem.getName()+"\"></div>\n");
                printWriter.write("</body></html>");
                asyncItems.put(f.getName(), new Item(fileItem.getContentType(), fileItem.getSize(), f));
            }
        }
	}

    private String extractFileName(String rawFileName) {
        if (rawFileName.indexOf("\\") >= 0) {
            return rawFileName.substring(rawFileName.lastIndexOf("\\")+1) ;
        } else if (rawFileName.indexOf("/") >= 0) {
            return rawFileName.substring(rawFileName.lastIndexOf("/")+1) ;
        } else {
            return rawFileName ;
        }
    }

    private int writeToDisk(JahiaUser user, FileItem item, String location, String filename) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("item : " + item);
            logger.debug("destination : " + location);
            logger.debug("filename : " + filename);
            logger.debug("size : " + item.getSize()) ;
        }
        if (item == null || location == null || filename == null) {
            return UNKNOWN_ERROR ;
        }


        JCRNodeWrapper locationFolder;
        try {
            locationFolder = jcr.getThreadSession(user).getNode(location);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            return BAD_LOCATION;
        }

        Exception ex = locationFolder.getException() ;
        if (ex != null) {
            logger.error("Exception building the node", ex) ;
        }

        if (!locationFolder.isWriteable()) {
            logger.debug("destination is not writable for user " + user.getName()) ;
            return READONLY;
        }
        JCRNodeWrapper result ;
        try {
            if (locationFolder.hasNode(filename)) {
                return EXISTS;
            }
            InputStream is = item.getInputStream() ;
            result = locationFolder.uploadFile(filename, is, item.getContentType());
            is.close() ;
            locationFolder.saveSession();
        } catch (RepositoryException e) {
            logger.error("exception ",e) ;
            return UNKNOWN_ERROR;
        }
        return result.isValid() ? OK : UNKNOWN_ERROR ;
    }

    private static Map<String, Item> asyncItems = new HashMap<String, Item>();

    public static class Item {
        public String contentType;
        public long length;
        public FileInputStream file;

        Item(String contentType, long length, final File file) throws FileNotFoundException {
            this.contentType = contentType;
            this.length = length;
            this.file = new FileInputStream(file) {
                @Override
                public void close() throws IOException {
                    super.close();
                    file.delete();
                }
            };
        }
    }

    public static Item getItem(String key) {
        return asyncItems.get(key);
    }
}
