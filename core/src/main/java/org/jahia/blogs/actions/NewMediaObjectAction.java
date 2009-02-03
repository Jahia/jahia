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
