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
package org.jahia.services.preferences.page;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRNodeDecorator;

import javax.jcr.RepositoryException;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 21 ao�t 2008
 * Time: 10:30:57
 * To change this template use File | Settings | File Templates.
 */
public class PageJahiaPreference extends JCRNodeDecorator {

    public PageJahiaPreference(JCRNodeWrapper node) throws RepositoryException {
        super(node);
    }

    public String getPageUUID() throws RepositoryException {
        return  getProperty("j:page").getString();
    }

    public void setPageUUID(String page) throws RepositoryException {
        setProperty("j:page", page);
    }

    public String getPrefName() throws RepositoryException {
        return getProperty("j:prefName").getString();
    }

    public void setPrefName(String prefName) throws RepositoryException {
        setProperty("j:prefName", prefName);
    }

    public String getPrefValue() throws RepositoryException {
        return getProperty("j:prefValue").getString();
    }

    public void setPrefValue(String prefValue) throws RepositoryException {
        setProperty("j:prefValue",prefValue);
    }

}
