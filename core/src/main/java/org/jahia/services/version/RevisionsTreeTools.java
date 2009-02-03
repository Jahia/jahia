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
