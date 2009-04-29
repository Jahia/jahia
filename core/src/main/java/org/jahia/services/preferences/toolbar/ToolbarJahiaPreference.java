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
package org.jahia.services.preferences.toolbar;

import org.jahia.services.preferences.JahiaPreference;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRNodeDecorator;

import javax.jcr.RepositoryException;

/**
 * User: jahia
 * Date: 9 avr. 2008
 * Time: 16:08:18
 */
public class ToolbarJahiaPreference extends JCRNodeDecorator {
    public static final String PROVIDER_TYPE = "toolbar";

    public ToolbarJahiaPreference(JCRNodeWrapper node) {
        super(node);
    }

    public String getToolbarName() throws RepositoryException {
        return getProperty("j:toolbarName").getString();
    }

    public void setToolbarName(String name) throws RepositoryException {
        setProperty("j:toolbarName", name);
    }

    public String getType() throws RepositoryException {
        return getProperty("j:type").getString();
    }

    public void setType(String type) throws RepositoryException {
        setProperty("j:type", type);
    }

    public int getState() throws RepositoryException {
        return (int) getProperty("j:state").getLong();
    }

    public void setState(int state) throws RepositoryException {
        setProperty("j:state", state);
    }

    public int getToolbarIndex() throws RepositoryException {
        return (int) getProperty("j:toolbarIndex").getLong();
    }

    public void setToolbarIndex(int index) throws RepositoryException {
        setProperty("j:toolbarIndex", index);
    }

    public int getPositionX() throws RepositoryException {
        return (int) getProperty("j:positionX").getLong();
    }

    public void setPositionX(int positionX) throws RepositoryException {
        setProperty("j:positionX", positionX);
    }

    public int getPositionY() throws RepositoryException {
        return (int) getProperty("j:positionY").getLong();
    }

    public void setPositionY(int positionY) throws RepositoryException {
        setProperty("j:positionY", positionY);
    }

    public boolean isDisplay() throws RepositoryException {
        return getProperty("j:display").getBoolean();
    }

    public void setDisplay(boolean display) throws RepositoryException {
        setProperty("j:display", display);
    }

}
