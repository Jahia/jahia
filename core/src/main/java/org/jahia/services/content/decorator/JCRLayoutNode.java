/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
        jcrLayoutItemNode.setPortlet(portletNode);
        jcrLayoutItemNode.setColumnIndex(column);
        jcrLayoutItemNode.setRowIndex(row);
        jcrLayoutItemNode.setStatus(status);
        return jcrLayoutItemNode;
    }
}
