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