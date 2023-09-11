/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.decorator;

import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.List;
import java.util.ArrayList;

/**
 * User: jahia
 * Date: 18 mars 2009
 * Time: 16:38:46
 */
public class JCRLayoutNode extends JCRNodeDecorator {
    public JCRLayoutNode(JCRNodeWrapper node) {
        super(node);
    }

    public List<JCRLayoutItemNode> getLayoutItems() throws RepositoryException {
        List<JCRLayoutItemNode> nodes = new ArrayList<JCRLayoutItemNode>();
        for (NodeIterator iterator = getNodes(); iterator.hasNext();) {
            nodes.add(new JCRLayoutItemNode((JCRNodeWrapper) iterator.nextNode()));
        }

        return nodes;
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
        jcrLayoutItemNode.setColumnIndex(column);
        jcrLayoutItemNode.setRowIndex(row);
        jcrLayoutItemNode.setStatus(status);
        return jcrLayoutItemNode;
    }
}
