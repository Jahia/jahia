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
package org.jahia.services.content.decorator;

import org.jahia.services.content.JCRNodeWrapper;

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
