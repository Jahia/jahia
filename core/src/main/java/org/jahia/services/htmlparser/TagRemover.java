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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * <p>Title: Html DOM Visitor, removing tags</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class TagRemover implements HtmlDOMVisitor{

    private Set tagsToRemove = new HashSet();

    public TagRemover(){}

    /**
     * let the visitor initiate itself
     *
     * @param siteId , current site, if -1, use default settings
     * @return
     */
    public void init(int siteId){
    }

    public void addTag(String tag){
        if ( tag != null && !this.tagsToRemove.contains(tag) ){
            this.tagsToRemove.add(tag);
        }
    }

    public void addTags(String[] tags){
        if ( tags != null ){
            int size = tags.length;
            for ( int i=0; i<size; i++ ){
                this.addTag(tags[i]);
            }
        }
    }

    /**
     * Remove all unwanted markups
     *
     * @param doc
     * @return
     */
    public Document parseDOM(Document doc){
        if ( doc != null && this.tagsToRemove.size()>0 ){
            removeMarkups(doc.getDocumentElement(),this.tagsToRemove);
        }
        return doc;
    }

    /**
     * Filter out unwanted markups
     *
     * @param node, the stating node
     * @param markups, the set of the names of the markups to remove
     */
    private void removeMarkups(Node node, Set markups){

        if ( markups == null || node == null ){
            return;
        }

        boolean toRemove = matchMarkup(node.getNodeName(),markups);
        Node parentNode = node.getParentNode();

        NodeList childs = node.getChildNodes();
        List al = new ArrayList();

        int count = childs.getLength();
        for ( int i=0 ; i<count ; i++ ){
            Node child = (Node)childs.item(i);
            al.add(child);
        }
        for ( int i=0; i<count ; i++ ){
            Node child = (Node)al.get(i);
            removeMarkups(child,markups);
        }

        if ( toRemove ){
            parentNode.removeChild(node);
        } else{
            Node refNode = node;
            while ( matchMarkup(parentNode.getNodeName(),markups) ){
                refNode = parentNode;
                parentNode = parentNode.getParentNode();
            }

            if ( !refNode.equals(node) ){
                parentNode.insertBefore(node,refNode);
            }
        }
    }

    private boolean matchMarkup(String name, Set markups){
        if ( name == null || markups == null || markups.size()==0 ){
            return false;
        }
        Iterator iterator = markups.iterator();
        String tag = null;
        while ( iterator.hasNext() ){
            tag = (String)iterator.next();
            if ( tag.toLowerCase().equals(name.toLowerCase()) ){
                return true;
            }
        }
        return false;
    }
}
