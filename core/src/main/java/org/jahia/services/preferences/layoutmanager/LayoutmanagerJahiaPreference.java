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
package org.jahia.services.preferences.layoutmanager;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRLayoutNode;
import org.jahia.services.content.decorator.JCRNodeDecorator;
import org.jahia.services.content.decorator.JCRLayoutItemNode;

import javax.jcr.RepositoryException;
import javax.jcr.PathNotFoundException;
import java.util.List;

/**
 * User: jahia
 * Date: 26 mars 2008
 * Time: 10:05:34
 */
public class LayoutmanagerJahiaPreference extends JCRNodeDecorator {
    public static String PROVIDER_TYPE = "layoutmanager";

    public LayoutmanagerJahiaPreference(JCRNodeWrapper node) {
        super(node);
    }

    public JCRLayoutNode getLayoutNode() throws RepositoryException {
        try {
            return new JCRLayoutNode((JCRNodeWrapper) getNode("j:layout"));
        } catch (PathNotFoundException e) {
            return new JCRLayoutNode(addNode("j:layout", "jnt:layout"));
        }
    }

    public List<JCRLayoutItemNode> getLayoutItems() throws RepositoryException {
        return getLayoutNode().getLayoutItems();
    }


    public void setPage(String page) throws RepositoryException {
        getLayoutNode().setPage(page);
        setProperty("j:page", page);
    }

    public boolean isLiveDraggable() throws RepositoryException {
        return getLayoutNode().isLiveDraggable();
    }

    public void setLiveDraggable(boolean liveDraggable) throws RepositoryException {
        getLayoutNode().setLiveEditable(liveDraggable);
    }

    public boolean isLiveEditable() throws RepositoryException {
        return getLayoutNode().isLiveEditable();
    }

    public void setLiveEditable(boolean liveEditable) throws RepositoryException {
        getLayoutNode().setLiveEditable(liveEditable);
    }

    public long getNbColumns() throws RepositoryException {
        return getLayoutNode().getNbColumns();
    }

    public void setNbColumns(long nbColumns) throws RepositoryException {
        getLayoutNode().setNbColumns(nbColumns);
    }

    public String getPage() throws RepositoryException {
        return getProperty("j:page").getString();
    }


}
