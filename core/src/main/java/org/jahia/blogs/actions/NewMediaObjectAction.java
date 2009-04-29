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
package org.jahia.blogs.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.transaction.Status;

import org.apache.log4j.Logger;
import org.jahia.blogs.ServletResources;
import org.jahia.blogs.model.MediaObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.webdav.JahiaWebdavBaseService;

/**
 * Action used to upload a file to the Jahia content repository.
 * Compliant with MetaWeblog API's newMediaObject method.
 *
 * @author Xavier Lawrence
 */
public class NewMediaObjectAction extends AbstractAction {
    
    public static final String WEBDAV_SERVLET = "/webdav";
    
    // log4j logger
    static Logger log = Logger.getLogger(NewMediaObjectAction.class);
    
    private String blogID;
    private Map struct;
    
    /** Creates a new instance of NewMediaObjectAction */
    public NewMediaObjectAction(String blogID, String userName,
            String password, Map struct) {
        
        super.userName = userName;
        super.password = password;
        this.blogID = blogID;
        this.struct = struct;
    }
    
    /**
     * Uploads a media object into Jahia.
     *
     * @return A Map containing the URL of the uploaded object
     */
    public Object execute() throws JahiaException {
        
        // Create commmon resources
        super.init();
        
        ContentPage blogContentPage = super.changePage(Integer.parseInt(blogID));

        // First check that the user is registered to this site.
        final JahiaUser user = super.checkLogin();
        
        // Set the correct page and check write access
        if (!blogContentPage.checkWriteAccess(user)) {
            throw new JahiaException(
                    "You do not have write access to Blog: "+blogID,
                    "You do not have write access to Page: "+blogID,
                    JahiaException.ACL_ERROR,
                    JahiaException.WARNING_SEVERITY);
        }
        
        String dir = "/users/" + user.getUsername() + "/public";
        
        final JCRNodeWrapper dav = JahiaWebdavBaseService.getInstance().
                getDAVFileAccess(dir, user);
        
        try {
            File f = File.createTempFile("blogUpload", ".tmp");
            FileOutputStream fos = new FileOutputStream(f);
            fos.write((byte[]) struct.get(MediaObject.BITS));
            fos.flush();
            fos.close();

            log.debug("blogUpload TmpFile: " + f.getName());

            final String contentType = (String) struct.get(MediaObject.TYPE);
            JCRNodeWrapper newFile = dav.uploadFile((String) struct.get(MediaObject.NAME), new FileInputStream(f), contentType);
            String finalName = null;
            if (newFile != null) {
                finalName = newFile.getName();
            }
                dav.save();

            StringBuffer buffer = new StringBuffer();
            buffer.append(ServletResources.getCurrentRequest().getScheme());
            buffer.append("://");
            buffer.append(ServletResources.getCurrentRequest().getServerName());
            buffer.append(":");
            buffer.append(ServletResources.getCurrentRequest().getServerPort());
            buffer
                    .append(ServletResources.getCurrentRequest()
                            .getContextPath());
            buffer.append(WEBDAV_SERVLET);
            buffer.append(dir);
            buffer.append("/");
            buffer.append(finalName);

            Map result = new HashMap(1);
            result.put(MediaObject.URL, buffer.toString());

            log.debug("Media Object URL is: " + result);
            f.delete();
            return result;

        } catch (RepositoryException e) {
            throw new JahiaException(e.getMessage(), e.getMessage(),
                    JahiaException.APPLICATION_ERROR,
                    JahiaException.ERROR_SEVERITY, e);
        } catch (IOException e) {
            throw new JahiaException(e.getMessage(), e.getMessage(),
                    JahiaException.APPLICATION_ERROR,
                    JahiaException.ERROR_SEVERITY, e);
        } finally {
            if (dav.getTransactionStatus() == Status.STATUS_ACTIVE) {
                try {
                    dav.refresh(false);
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
