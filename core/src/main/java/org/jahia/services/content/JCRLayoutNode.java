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
package org.jahia.services.content;

import javax.jcr.RepositoryException;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 18 mars 2009
 * Time: 16:38:46
 * To change this template use File | Settings | File Templates.
 */
public class JCRLayoutNode extends JCRNodeDecorator {
    public JCRLayoutNode(JCRNodeWrapper node) {
        super(node);
    }

    public List<JCRLayoutItemNode> getLayoutItems() throws RepositoryException {
        List<JCRNodeWrapper> nodeWrappers = getChildren();
        if (nodeWrappers != null) {
            List<JCRLayoutItemNode> nodes = new ArrayList<JCRLayoutItemNode>();
            for (JCRNodeWrapper n : nodeWrappers) {
                nodes.add(new JCRLayoutItemNode(n));
            }
            return nodes;
        }
        return new ArrayList<JCRLayoutItemNode>();
    }

    public boolean isLiveDraggable() throws RepositoryException {
        if (hasProperty("j:liveDraggable")) {
            return getProperty("j:liveDraggable").getBoolean();
        }
        return true;
    }

    public void setLiveDraggable(boolean liveDraggable) throws RepositoryException {
        setProperty("j:liveDraggable", liveDraggable);
    }

    public boolean isLiveEditable() throws RepositoryException {
        if (hasProperty("j:liveEditable")) {
            return getProperty("j:liveEditable").getBoolean();
        }
        return true;
    }

    public void setLiveEditable(boolean liveEditable) throws RepositoryException {
        setProperty("j:liveEditable", liveEditable);
    }

    public long getNbColumns() throws RepositoryException {
        if (hasProperty("j:nbColumns")) {
            return getProperty("j:nbColumns").getLong();
        }
        return 3;
    }

    public void setNbColumns(long nbColumns) throws RepositoryException {
        setProperty("j:nbColumns", nbColumns);
    }

    public String getPage() throws RepositoryException {
        return getProperty("j:page").getString();
    }

    public void setPage(String page) throws RepositoryException {
        setProperty("j:page", page);
    }

    public JCRLayoutItemNode addLayoutItem(JCRNodeWrapper portletNode, int column, int row, String status) throws RepositoryException {
        JCRNodeWrapper jcrNodeWrapper = addNode("j:item", "jnt:layoutItem");
        JCRLayoutItemNode jcrLayoutItemNode = new JCRLayoutItemNode(jcrNodeWrapper);
        jcrLayoutItemNode.setPortlet(portletNode);
        jcrLayoutItemNode.setColumnIndex(column);
        jcrLayoutItemNode.setRowIndex(row);
        jcrLayoutItemNode.setStatus(status);
        return jcrLayoutItemNode;
    }
}
