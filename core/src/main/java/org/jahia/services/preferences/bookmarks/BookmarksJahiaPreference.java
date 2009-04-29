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
package org.jahia.services.preferences.bookmarks;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRNodeDecorator;

import javax.jcr.RepositoryException;

/**
 * User: jahia
 * Date: 15 mai 2008
 * Time: 16:24:45
 */
public class BookmarksJahiaPreference extends JCRNodeDecorator {
    public BookmarksJahiaPreference(JCRNodeWrapper node) {
        super(node);
    }

    public String getPageUUID() throws RepositoryException {
        return  getProperty("j:page").getString();
    }

    public void setPageUUID(String pUUID) throws RepositoryException {
        setProperty("j:page", pUUID);
    }

}
