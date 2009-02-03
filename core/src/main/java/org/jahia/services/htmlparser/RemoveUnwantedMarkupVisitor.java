/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

 package org.jahia.services.htmlparser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The DOM visitor for removing specified HTML elements.
 * 
 * @author Khue Nguyen
 */
public class RemoveUnwantedMarkupVisitor implements HtmlDOMVisitor {

    /**
     * Filter out unwanted markups
     * 
     * @param node
     *            , the stating node
     * @param markups
     *            , the set of the names of the markups to remove
     */
    private static void removeMarkups(Node node, Set markups) {

        if (markups == null || node == null) {
            return;
        }

        boolean toRemove = node.getNodeName() != null ? markups.contains(node.getNodeName().toLowerCase()) : false;
        Node parentNode = node.getParentNode();

        NodeList childs = node.getChildNodes();
        List al = new ArrayList();

        int count = childs.getLength();
        for (int i = 0; i < count; i++) {
            Node child = (Node) childs.item(i);
            al.add(child);
        }
        for (int i = 0; i < count; i++) {
            Node child = (Node) al.get(i);
            removeMarkups(child, markups);
        }

        if (toRemove) {
            parentNode.removeChild(node);
        } else {
            Node refNode = node;
            while (parentNode.getNodeName() != null && markups.contains(parentNode.getNodeName().toLowerCase())) {
                refNode = parentNode;
                parentNode = parentNode.getParentNode();
            }

            if (!refNode.equals(node)) {
                parentNode.insertBefore(node, refNode);
            }
        }
    }

    private Set unwantedMarkups = new HashSet();

    public RemoveUnwantedMarkupVisitor(String[] tagsToRemove) {
        CollectionUtils.addAll(unwantedMarkups, tagsToRemove);
    }

    public void init(int siteId) {
        // do nothing
    }

    /**
     * Remove all unwanted markups
     * 
     * @param doc
     * @return
     */
    public Document parseDOM(Document doc) {
        if (doc != null && unwantedMarkups.size() > 0) {
            removeMarkups(doc.getDocumentElement(), this.unwantedMarkups);
        }
        return doc;
    }

}