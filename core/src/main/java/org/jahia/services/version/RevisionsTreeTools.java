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
package org.jahia.services.version;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentContainerListKey;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentPageKey;
import org.jahia.content.JahiaObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.fields.ContentField;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.GUITreeTools;

public class RevisionsTreeTools {

    /**
     *
     * @param pageRevisionEntrySet
     * @param user
     * @param loadRequest
     * @param operationMode
     * @return
     * @throws JahiaException
     */
    public static JTree getTreeOfPageRevisions(PageRevisionEntrySet pageRevisionEntrySet,
            JahiaUser user, EntryLoadRequest loadRequest, String operationMode)
    throws JahiaException {

        JTree tree = null;
        try {

            // Root Node
            DefaultMutableTreeNode top =
                    new DefaultMutableTreeNode(
                    JahiaObject.getInstance(pageRevisionEntrySet.getObjectKey()),true);
            DefaultTreeModel treeModel = new DefaultTreeModel(top,true);
            tree = new JTree(treeModel);

            // Add revision to the tree
            Map contentObjectsInTree = new HashMap();
            contentObjectsInTree.put(pageRevisionEntrySet.getObjectKey(),top);

            DefaultMutableTreeNode revNode = null;

            Iterator iterator =
                    pageRevisionEntrySet.getRevisions().iterator();
            RevisionEntry revEntry = null;
            while ( iterator.hasNext() ){

                // retrieve the content object and its parent
                revEntry = (RevisionEntry)iterator.next();
                if ( revEntry.getObjectKey().getType().equals(ContentPageKey.PAGE_TYPE) ){
                    revNode = new DefaultMutableTreeNode(revEntry);
                    top.insert(revNode,0);
                } else {
                    // check if the content object is already added int the tree
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                            contentObjectsInTree.get(revEntry.getObjectKey());
                    if ( node == null ){
                        // add the content object in the tree
                        ContentObject contentObject = (ContentObject)
                                JahiaObject.getInstance(revEntry.getObjectKey());
                        node = addContentObjectInTree(contentObject, tree,
                                contentObjectsInTree,user,loadRequest,operationMode);
                        // automatically expand Field nodes
                        if ( contentObject instanceof ContentField ){
                            GUITreeTools.expandAllPath(tree,node);
                        }
                    }

                    // Add the revision node to the current content object node
                    if ( node != null ){
                        revNode = new DefaultMutableTreeNode(revEntry);
                        if ( revEntry.getObjectKey().getType()
                             .equals(ContentContainerKey.CONTAINER_TYPE) ){
                            node.insert(revNode,0);
                        } else if ( revEntry.getObjectKey().getType()
                                    .equals(ContentContainerListKey.CONTAINERLIST_TYPE) ){
                            node.insert(revNode,0);
                        } else {
                            node.add(revNode);
                        }
                    }
                }
            }
        } catch ( java.lang.ClassNotFoundException cnf ){
            throw new JahiaException("Error creating Page Revisions Tree",
                                     "Error creating Page Revisions Tree",
                                     JahiaException.APPLICATION_ERROR,
                                     JahiaException.APPLICATION_ERROR,cnf);
        }
        return tree;
    }

    /**
     *
     * @param contentObject
     * @param tree
     * @param contentObjectsInTree
     * @return
     */
    private static DefaultMutableTreeNode addContentObjectInTree(ContentObject contentObject,
            JTree tree, Map contentObjectsInTree, JahiaUser user,
            EntryLoadRequest loadRequest, String operationMode) throws JahiaException {

        // check if the content object is already added int the tree
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                contentObjectsInTree.get(contentObject.getObjectKey());
        if ( node == null ){
            node = new DefaultMutableTreeNode(contentObject,true);
            ContentObject parent =
                    contentObject.getParent(user,loadRequest,operationMode);
            DefaultMutableTreeNode parentNode =
                    addContentObjectInTree(parent,tree,contentObjectsInTree,
                    user,loadRequest,operationMode);
            if ( parentNode != null ){
                parentNode.add(node);
            } else {
                // parent node not found, append to root
                // FIXME : unexpected situation
                DefaultMutableTreeNode rootNode =
                        (DefaultMutableTreeNode)tree.getModel().getRoot();
                rootNode.add(node);
            }
        }
        if ( node != null ){
            contentObjectsInTree.put(contentObject.getObjectKey(),node);
        }
        return node;
    }
}
