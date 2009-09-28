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
package org.jahia.services.preferences.toolbar;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRNodeDecorator;

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
