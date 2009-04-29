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

/**
 * Action callback for performing WebDAV operations.
 * 
 * @author Sergiy Shyrkov
 */
public interface WebDAVCallback {

    /**
     * This action will be executed by {@link WebDAVTemplate} in order to add
     * additional functionality, like transaction support.
     * 
     * @param file
     *            the WebDAV resource to perform the action on
     * @return <code>true</code> if the action was successfull and the commit
     *         should be made; <code>false</code> - to perform rollback
     */
    boolean doInWebDAV(JCRNodeWrapper file);

}
