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
package org.jahia.services.webdav;

import org.jahia.services.content.JCRNodeWrapper;

import javax.transaction.Status;
import javax.jcr.RepositoryException;

/**
 * Adds support for transactional operation on the WebDAV resources.
 * 
 * @author Sergiy Shyrkov
 */
public class WebDAVTemplate {

    private JCRNodeWrapper file;

    /**
     * Initializes an instance of this class.
     * 
     * @param file
     *            the WebDAV file to perform actions
     */
    public WebDAVTemplate(JCRNodeWrapper file) {
        super();
        this.file = file;
    }

    /**
     * Executes the specified action within the transaction scope.
     * 
     * @param action
     *            an action to be performed on the WebDAV resource
     */
    public void transactionalCall(WebDAVCallback action) {
        try {
            boolean ok = action.doInWebDAV(file);
            if (ok) {
                try {
                    file.saveSession();
                } catch (RepositoryException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        } finally {
            if (file.getTransactionStatus() == Status.STATUS_ACTIVE) {
                try {
                    file.refresh(false);
                } catch (RepositoryException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }
}
