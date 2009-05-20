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
package org.jahia.utils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jahia.params.ProcessingContext;


/**
 * <p>Title: JTree Tools</p>
 * <p>Description: This class offers backend handling methods to handle
 * JTree trees, as well as generate flat views that are useful for HTML
 * output.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class GUITreeTools {

    /**
     * Returns an array list representation ( simplier to work with when generating
     * an html view ) of a tree starting from a given node.
     *
     * @param tree the tree to convert to a flat array list
     * @param node the starting node in the tree
     * @return an List containing DefaultMutableTreeNode objects that
     * represent a flat view of the tree in it's current visible state
     */
    public static List<DefaultMutableTreeNode> getFlatTree(JTree tree, DefaultMutableTreeNode node){
       List<DefaultMutableTreeNode> values = new ArrayList<DefaultMutableTreeNode>();
       if ( node != null ){
           values.add(node);
           if ( tree.isExpanded(new TreePath(node.getPath())) ){
               Enumeration<?> childrens = node.children();
               while( childrens.hasMoreElements() ){
                   DefaultMutableTreeNode childNode =
                           (DefaultMutableTreeNode)childrens.nextElement();
                   List<DefaultMutableTreeNode> childDescendants = getFlatTree(tree,childNode);
                   values.addAll(childDescendants);
               }
           }
       }
       return values;
    }

    /**
     * @param node the node for which to calculate the levels
     * @return an List containing Integer that are the level numbers for
     * which we should render a vertical line to connect a child node to its
     * parent.
     */
    public static List<Integer> getLevelsWithVerticalLine(DefaultMutableTreeNode node){
        List<Integer> values = new ArrayList<Integer>();
        TreeNode[] treeNodes = node.getPath();
        for( int i=0; i<treeNodes.length; i++ ){
            DefaultMutableTreeNode n = (DefaultMutableTreeNode)treeNodes[i];
            DefaultMutableTreeNode parentNode =
                    (DefaultMutableTreeNode)n.getParent();
            if ( parentNode!=null && !n.equals(parentNode.getLastChild()) ){
                values.add(new Integer(i));
            }
        }

        return values;
    }

    /**
     * Update GUI Tree changes. This method looks for two parameters in the
     * request object : guitree and nodeindex. The guitree parameter may have
     * 3 values : expand, expandall and collapse. The nodeindex indicates the
     * node index for which to perform the operation.
     *
     * @param tree the tree to modify according to the parameters set in the
     * request object
     * @param processingContext the request object containing the parameters that
     * indicate which tree modifications should be performed.
     * @todo To be completed
     */
    public static void updateGUITree(JTree tree,
                                     ProcessingContext processingContext){

        String treeOperation = processingContext.getParameter("guitree");
        String nodeIndex = processingContext.getParameter("nodeindex");

        // we set to null because we later only check for null
        if ("".equals(treeOperation)) {
            treeOperation = null;
        }
        if ("".equals(nodeIndex)) {
            nodeIndex = null;
        }

        updateGUITree(tree, treeOperation, nodeIndex);
    }

    public static void updateGUITree(JTree tree,
                                     HttpServletRequest request){

        String treeOperation = request.getParameter("guitree");
        String nodeIndex = request.getParameter("nodeindex");

        // we set to null because we later only check for null
        if ("".equals(treeOperation)) {
            treeOperation = null;
        }
        if ("".equals(nodeIndex)) {
            nodeIndex = null;
        }

        updateGUITree(tree, treeOperation, nodeIndex);
    }

    private static void updateGUITree(JTree tree,
                                     String treeOperation, String nodeIndex){


        if ( tree != null ){

            DefaultMutableTreeNode rootNode =
                    (DefaultMutableTreeNode)tree.getModel().getRoot();
            if ( rootNode != null ){
                List<DefaultMutableTreeNode> nodeList =
                        GUITreeTools.getFlatTree(tree,rootNode);
                DefaultMutableTreeNode node = null;
                if ((treeOperation != null) && (nodeIndex != null)) {
                    node = (DefaultMutableTreeNode)
                           nodeList.get(Integer.parseInt(nodeIndex));
                } else {
                    node = (DefaultMutableTreeNode)
                       tree.getLastSelectedPathComponent();
                    if (node == null) {
                        return;
                    }
                    treeOperation="expand";
                }
                if ( treeOperation.equals("expand") ){
                    tree.expandPath(new TreePath(node.getPath()));
                } else if ( treeOperation.equals("expandall") ){
                    expandAllPath(tree,node);
                } else if ( treeOperation.equals("collapse") ){
                    tree.collapsePath(new TreePath(node.getPath()));
                }
            }
        }
    }

    /**
     * Expands all the paths under a given node.
     * @param tree the tree on which to expand the node and it's children
     * @param node the starting node under which to expand all the paths.
     */
    public static void expandAllPath(JTree tree, DefaultMutableTreeNode node){
        if ( !node.isLeaf() || node.children().hasMoreElements() ){
            tree.expandPath(new TreePath(node.getPath()));
        }
        Enumeration<?> children = node.children();
        DefaultMutableTreeNode childNode = null;
        while( children.hasMoreElements() ){
            childNode = (DefaultMutableTreeNode)children.nextElement();
            expandAllPath(tree,childNode);
        }
    }
}

