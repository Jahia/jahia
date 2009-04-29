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
package org.jahia.services.preferences.layoutmanager;

import org.jahia.services.preferences.JahiaPreference;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRLayoutNode;
import org.jahia.services.content.JCRLayoutItemNode;
import org.jahia.services.content.JCRNodeDecorator;

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
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(LayoutmanagerJahiaPreference.class);

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
