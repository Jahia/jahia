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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.uicomponents.tree;

import org.jahia.data.beans.RequestBean;
import org.jahia.params.ProcessingContext;
import org.jahia.resourcebundle.JahiaResourceBundle;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.urls.QueryMapURL;
import org.jahia.utils.GUITreeTools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * <p>Title: HTML Tree control</p>
 * <p>Description: This HTML-specific tag handles the display AND handling of
 * a tree control. It includes all the logic for session-handling, looping,
 * selection. It should be generic enough to be used in any kind of
 * application, for any kind of user objects.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 * @jsp:tag name="tree" body-content="JSP"
 * description="Displays and handles an HTML tree control.
 * <p/>
 * <p><attriInfo>This HTML-specific tag handles the display AND handling of
 * a tree control. It includes all the logic for session-handling, looping,
 * selection. It should be generic enough to be used in any kind of
 * application, for any kind of user objects.
 * <p/>
 * <p/>
 * <p><b>Example 1 :</b>
 * <p/>
 * <p/>
 * &lt;content:category key=\"root\" id=\"rootCategory\" subTreeID=\"categoryTree\" /&gt;  <br>
 * <p/>
 * &lt;jahiaHtml:tree treeName=\"categoryTree\" userObjectID=\"userObject\" actionURL=\"&lt;%=actionURL%&gt;\" <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;                        nodeIndexID=\"nodeIndex\" selectionParamName=\"selectednode\" selectedNodeIndexID=\"selectedNodeIndex\" <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;                        selectionURLID=\"selectionURL\" selectedUserObjectID=\"selectedUserObject\"&gt; <br>
 * &nbsp;&nbsp;           &lt;% <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;               Object nodeInfo = pageContext.findAttribute(\"userObject\"); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;                Integer nodeIndexInt = (Integer) pageContext.findAttribute(\"nodeIndex\"); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;                int nodeIndex = nodeIndexInt.intValue(); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;                String selectionURL = (String) pageContext.findAttribute(\"selectionURL\"); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;                Integer selectedNodeIndexInt = (Integer) pageContext.findAttribute(\"selectedNodeIndex\"); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;                int selectedNodeIndex = selectedNodeIndexInt.intValue(); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;                Category curCategory = (Category) nodeInfo; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;                String catDisplay = curCategory.getTitle(jData.params().getLocale()); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;                if (catDisplay == null) { <br>
 * &nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;                    catDisplay = \"(key=\" + curCategory.getKey() + \")\"; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;                 } <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;                if (nodeIndex == selectedNodeIndex) { %&gt;  <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;                   &lt;b&gt;&lt;%= catDisplay %&gt;&lt;/b&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;             &lt;% } else { %&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;                  &lt;a href=\"&lt;%=selectionURL %&gt;\"&gt;&lt;%= catDisplay %&gt;&lt;/a&gt; <br>
 * &nbsp;&nbsp;           &lt;% } <br>
 * &nbsp;&nbsp;           %&gt; <br>
 * &lt;/jahiaHtml:tree&gt; <br>
 * <p/>
 * <p><b>Example 2 :</b>
 * <p/>
 * <p/>
 * &lt;content:category key=\"root\" id=\"rootCategory\" subTreeID=\"categoryTree\" /&gt; <br>
 * <p/>
 * &lt;jahiaHtml:tree treeName=\"categoryTree\" userObjectID=\"userObject\" actionURL=\"&lt;%=actionURL%&gt;\" <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;                nodeIndexID=\"nodeIndex\" selectionParamName=\"selectednode\" selectedNodeIndexID=\"selectedNodeIndex\" <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;                selectionURLID=\"selectionURL\" selectedUserObjectID=\"selectedUserObject\"&gt; <br>
 * &nbsp;&nbsp;   &lt;% <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;        Object nodeInfo = pageContext.findAttribute(\"userObject\"); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;        Integer nodeIndexInt = (Integer) pageContext.findAttribute(\"nodeIndex\"); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;        int nodeIndex = nodeIndexInt.intValue(); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;        String selectionURL = (String) pageContext.findAttribute(\"selectionURL\"); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;        Integer selectedNodeIndexInt = (Integer) pageContext.findAttribute(\"selectedNodeIndex\"); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;        int selectedNodeIndex = selectedNodeIndexInt.intValue(); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;        Category curCategory = (Category) nodeInfo; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;        String catDisplay = curCategory.getTitle(jData.params().getLocale()); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;        if (catDisplay == null) { <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;            catDisplay = \"(key=\" + curCategory.getKey() + \")\"; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;        }  <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;        if (nodeIndex == selectedNodeIndex) { %&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;           &lt;b&gt;&lt;%= catDisplay %&gt;&lt;/b&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;     &lt;% } else { %&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;           &lt;a href=\"&lt;%=selectionURL %&gt;\"&gt;&lt;%= catDisplay %&gt;&lt;/a&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;     &lt;% } <br>
 * &nbsp;&nbsp;   %&gt; <br>
 * &lt;/jahiaHtml:tree&gt; <br>
 * <p/>
 * </attriInfo>"
 */

public class TreeTag extends AbstractJahiaTag {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(TreeTag.class);

    private String treeName = null;
    private Iterator listIterator = null;
    private Object userObject = null;
    private boolean initialized = false;
    private JTree tree = null;
    private List flatTree = null;
    private String userObjectID = null;
    private int nodeCounter = 0;
    private String actionURL = null;
    private String selectionParamName = null;
    private String nodeIndexID = null;
    private String selectedUserObjectID = null;
    private String selectedNodeIndexID = null;
    private int selectedNodeIndex = 0;
    private String selectionURLID = null;
    private String vLineIcon = "vLineIcon";
    private String lineNodeIcon = "lineNodeIcon";
    private String lastNodeIcon = "lastNodeIcon";
    private String spacerIcon = "org.jahia.pix.image";
    private String expandIcon = "plusNodeIcon";
    private String collapseIcon = "minusNodeIcon";
    private String expandAllIcon = "expandAllNodeIcon";

    public TreeTag() {
    }

    /**
     * Sets the name of the pageContext attribute that contains the JTree
     * instance we will operate on.
     *
     * @param treeName the name of the pageContext attribute that contains
     *                 the JTree instance
     * @jsp:attribute name="treeName" required="true" rtexprvalue="true"
     * description="Sets the name of the pageContext attribute that contains the
     * <a href='http://java.sun.com/j2se/1.4.2/docs/api/javax/swing/JTree.html' target='tagFrame'>JTree</a>
     * instance we will operate on.
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setTreeName(String treeName) {
        this.treeName = treeName;
    }

    /**
     * Sets the name of the pageContext attribute that will be used to store
     * the current UserObject in the current iteration over the visible tree
     * nodes
     *
     * @param userObjectID the name of the pageContext attribute for the
     *                     current UserObject
     * @jsp:attribute name="userObjectID" required="true" rtexprvalue="true"
     * description="Sets the name of the pageContext attribute that will be used to store
     * the current UserObject in the current iteration over the visible tree
     * nodes.
     * <p/>
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setUserObjectID(String userObjectID) {
        this.userObjectID = userObjectID;
    }

    /**
     * Sets the base action URL that is used to generate URLs that will
     * perform tree display modifications.
     *
     * @param actionURL the base URL to use
     * @jsp:attribute name="actionURL" required="false" rtexprvalue="true"
     * description="Sets the base action URL that is used to generate URLs that will
     * perform tree display modifications.
     * <p/>
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setActionURL(String actionURL) {
        this.actionURL = actionURL;
    }

    /**
     * Sets the name of the query string URL parameter that is used to
     * specify the currently selected tree node. If this property is never set,
     * the default name "selectednode" will be used.
     *
     * @param selectionParamName the name of the URL query string parameter
     *                           name to be used for the selected node index
     * @jsp:attribute name="selectionParamName" required="false" rtexprvalue="true"
     * description="Sets the name of the query string URL parameter that is used to
     * specify the currently selected tree node.
     * <p/>
     * <p><attriInfo>If this property is never set, the default name \"selectednode\" will be used.
     * </attriInfo>"
     */
    public void setSelectionParamName(String selectionParamName) {
        this.selectionParamName = selectionParamName;
    }

    /**
     * Sets the name of the pageContext attribute to use to store the current
     * node index.
     *
     * @param nodeIndexID the name of the pageContext attribute
     * @jsp:attribute name="nodeIndexID" required="false" rtexprvalue="true"
     * description="Sets the name of the pageContext attribute to use to store the current
     * node index.
     * <p/>
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setNodeIndexID(String nodeIndexID) {
        this.nodeIndexID = nodeIndexID;
    }

    /**
     * Sets the name of the pageContext attribute to use to store the selected
     * UserObject instance
     *
     * @param selectedUserObjectID the name of the pageContext attribute
     * @jsp:attribute name="selectedUserObjectID" required="false" rtexprvalue="true"
     * description="Sets the name of the pageContext attribute to use to store the selected
     * UserObject instance.
     * <p/>
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setSelectedUserObjectID(String selectedUserObjectID) {
        this.selectedUserObjectID = selectedUserObjectID;
    }

    /**
     * Sets the name of the pageContext attribute to use to store the selected
     * node index Integer
     *
     * @param selectedNodeIndexID the name of the pageContext attribute
     * @jsp:attribute name="selectedNodeIndexID" required="false" rtexprvalue="true"
     * description="Sets the name of the pageContext attribute to use to store the selected
     * node index Integer.
     * <p/>
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setSelectedNodeIndexID(String selectedNodeIndexID) {
        this.selectedNodeIndexID = selectedNodeIndexID;
    }

    /**
     * Sets the name of the pageContext attribute to use to store the URL
     * that selects the current node in the iteration
     *
     * @param selectionURLID the name of the pageContext attribute
     * @jsp:attribute name="selectionURLID" required="false" rtexprvalue="true"
     * description="Sets the name of the pageContext attribute to use to store the URL
     * that selects the current node in the iteration.
     * <p/>
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setSelectionURLID(String selectionURLID) {
        this.selectionURLID = selectionURLID;
    }

    /**
     * Sets the name of the key for the image to use for vertical lines.
     * If not set defaults to a value of "vLineIcon".
     *
     * @param vLineIcon the common resource key name
     * @jsp:attribute name="vLineIcon" required="false" rtexprvalue="true"
     * description="Sets the name of the key for the image to use for vertical lines. .
     * <p/>
     * <p><attriInfo>If not set defaults to a value of \"vLineIcon\".
     * </attriInfo>"
     */
    public void setVLineIcon(String vLineIcon) {
        this.vLineIcon = vLineIcon;
    }

    /**
     * Sets the name of the key for the image to use for horizontal lines.
     * If not set defaults to a value of "lineNodeIcon".
     *
     * @param lineNodeIcon the common resource key name
     * @jsp:attribute name="lineNodeIcon" required="false" rtexprvalue="true"
     * description="Sets the name of the key for the image to use for horizontal lines.
     * <p/>
     * <p><attriInfo>If not set defaults to a value of \"lineNodeIcon\".
     * </attriInfo>"
     */
    public void setLineNodeIcon(String lineNodeIcon) {
        this.lineNodeIcon = lineNodeIcon;
    }

    /**
     * Sets the name of the key for the image to use for the last node on a
     * node line.
     * If not set defaults to a value of "lastNodeIcon".
     *
     * @param lastNodeIcon the common resource key name
     * @jsp:attribute name="lastNodeIcon" required="false" rtexprvalue="true"
     * description="Sets the name of the key for the image to use for the last node on a
     * node line.
     * <p/>
     * <p><attriInfo>If not set defaults to a value of \"lastNodeIcon\".
     * </attriInfo>"
     */
    public void setLastNodeIcon(String lastNodeIcon) {
        this.lastNodeIcon = lastNodeIcon;
    }

    /**
     * Sets the name of the key for the image used as a spacer (usually a
     * 1x1 GIF image).
     * If not set defaults to a value of "org.jahia.pix.image".
     *
     * @param spacerIcon the common resource key name
     * @jsp:attribute name="spacerIcon" required="false" rtexprvalue="true"
     * description="Sets the name of the key for the image used as a spacer (usually a
     * 1x1 GIF image).
     * <p/>
     * <p><attriInfo>If not set defaults to a value of \"org.jahia.pix.image\".
     * </attriInfo>"
     */
    public void setSpacerIcon(String spacerIcon) {
        this.spacerIcon = spacerIcon;
    }

    /**
     * Sets the name of the key for the image to be used to expand a node.
     * If not set defaults to a value of "plusNodeIcon";
     *
     * @param expandIcon the common resource key name;
     * @jsp:attribute name="expandIcon" required="false" rtexprvalue="true"
     * description="Sets the name of the key for the image to be used to expand a node.
     * <p/>
     * <p><attriInfo>If not set defaults to a value of \"plusNodeIcon\"
     * </attriInfo>"
     */
    public void setExpandIcon(String expandIcon) {
        this.expandIcon = expandIcon;
    }

    /**
     * Sets the name of the key for the image to be used to collapse a node.
     * If not set defaults to a value of "minusNodeIcon";
     *
     * @param collapseIcon the common resource key name
     * @jsp:attribute name="collapseIcon" required="false" rtexprvalue="true"
     * description="Sets the name of the key for the image to be used to collapse a node.
     * <p/>
     * <p><attriInfo>If not set defaults to a value of \"minusNodeIcon\"
     * </attriInfo>"
     */
    public void setCollapseIcon(String collapseIcon) {
        this.collapseIcon = collapseIcon;
    }

    /**
     * Sets the name of the key for the image to be used to expand all the
     * nodes below a certain node.
     * If not set defaults to a value of "expandAllNodeIcon";
     *
     * @param expandAllIcon the common resource key name
     * @jsp:attribute name="expandAllIcon" required="false" rtexprvalue="true"
     * description="Sets the name of the key for the image to be used to expand all the
     * nodes below a certain node.
     * <p/>
     * <p><attriInfo>If not set defaults to a value of \"expandAllNodeIcon\"
     * </attriInfo>"
     */
    public void setExpandAllIcon(String expandAllIcon) {
        this.expandAllIcon = expandAllIcon;
    }

    private void init() {
        tree = (JTree) pageContext.findAttribute(treeName);

        if (tree == null) {
            return;
        }

        nodeCounter = 0;

        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        String targetTreeName = request.getParameter("targettree");

        selectedNodeIndex = 0;
        DefaultMutableTreeNode rootNode =
                (DefaultMutableTreeNode) tree.getModel().getRoot();
        DefaultMutableTreeNode selectedNode = rootNode;

        if (targetTreeName != null) {
            if (targetTreeName.equals(treeName)) {
                // before we modify the tree, we must handle the currently
                // selected node.
                if (selectionParamName == null) {
                    selectionParamName = "selectednode";
                }
                String selectedNodeIndexStr = request.getParameter(
                        selectionParamName);
                if (selectedNodeIndexStr != null) {
                    selectedNodeIndex = Integer.parseInt(selectedNodeIndexStr);
                } else {
                    // no selection found in URL
                }

                TreePath selectedNodePath = tree.getSelectionPath();
                if (selectedNodeIndexStr != null) {
                    List nodeList =
                            GUITreeTools.getFlatTree(tree, rootNode);
                    if (selectedNodeIndex > (nodeList.size() - 1)) {
                        selectedNodeIndex = 0;
                    }
                    selectedNode
                            = (DefaultMutableTreeNode)
                            nodeList.get(selectedNodeIndex);
                    selectedNodePath = new TreePath(selectedNode.getPath());
                    tree.clearSelection();
                    tree.setSelectionPath(selectedNodePath);
                } else {
                    selectedNode = (DefaultMutableTreeNode) tree.
                            getLastSelectedPathComponent();
                    if (selectedNode != null) {
                        flatTree = GUITreeTools.getFlatTree(tree, rootNode);
                        selectedNodeIndex = flatTree.indexOf(selectedNode);
                    }
                }

                GUITreeTools.updateGUITree(tree, request);

                // now we must test if the selectedNode is still visible. If
                // it's not we must adjust the selection to a visible node
                // (preferably a parent node of the currently selected node).
                if (!tree.isVisible(selectedNodePath)) {
                    TreePath currentPath = selectedNodePath.getParentPath();
                    while ((currentPath != null) &&
                            (!tree.isVisible(currentPath))) {
                        currentPath = currentPath.getParentPath();
                    }
                    if (currentPath != null) {
                        // now that we found a visible parent, we must figure
                        // out it's index.
                        selectedNode = (DefaultMutableTreeNode) currentPath.
                                getLastPathComponent();
                        selectedNodePath = currentPath;
                        tree.clearSelection();
                        tree.setSelectionPath(selectedNodePath);

                        flatTree = GUITreeTools.getFlatTree(tree, rootNode);
                        selectedNodeIndex = flatTree.indexOf(selectedNode);
                    }
                }

            }
        }

        if (selectedUserObjectID != null) {
            pageContext.setAttribute(selectedUserObjectID,
                    selectedNode.getUserObject());
        }

        if (selectedNodeIndexID != null) {
            pageContext.setAttribute(selectedNodeIndexID,
                    new Integer(selectedNodeIndex));
        }

        if (rootNode != null) {
            flatTree = GUITreeTools.getFlatTree(tree, rootNode);
        }
        listIterator = flatTree.iterator();

        if (actionURL == null) {
            actionURL = ((HttpServletRequest) pageContext.getRequest()).getRequestURI();
        }

        initialized = true;
    }

    public int doStartTag() {

        logger.debug("startTag");

        // first let's see if we are handling a previous call or if we need
        // to create the tree for the first time.

        JspWriter out = pageContext.getOut();
        if (!initialized) {
            init();
        }

        if (tree == null) {
            return SKIP_BODY;
        }

        try {
            if (!listIterator.hasNext()) {
                return SKIP_BODY;
            } else {
                out.println("<!-- begin tree control -->");
                out.println(
                        "<table class=\"text\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
            }

            startNextNode(out, actionURL);

        } catch (IOException ioe) {
            logger.error("Error:", ioe);
        }

        return EVAL_BODY_BUFFERED;
    }

    public int doAfterBody() {

        logger.debug("afterBody");

        JspWriter out = bodyContent.getEnclosingWriter();
        try {
            bodyContent.writeOut(out);
            bodyContent.clearBody();

            out.println("</td>");
            out.println("<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>");
            out.println("</tr>");

            if (!listIterator.hasNext()) {
                out.println("</table>");
                out.println("<!-- end tree control -->");
                return SKIP_BODY;
            } else {
                startNextNode(out, actionURL);
            }

        } catch (IOException ioe) {
            logger.error("Error:", ioe);
        }

        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag()
            throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        treeName = null;
        listIterator = null;
        userObject = null;
        initialized = false;
        tree = null;
        flatTree = null;
        userObjectID = null;
        nodeCounter = 0;
        actionURL = null;
        selectionParamName = null;
        nodeIndexID = null;
        selectedUserObjectID = null;
        selectedNodeIndexID = null;
        selectedNodeIndex = 0;
        selectionURLID = null;
        vLineIcon = "vLineIcon";
        lineNodeIcon = "lineNodeIcon";
        lastNodeIcon = "lastNodeIcon";
        spacerIcon = "org.jahia.pix.image";
        expandIcon = "plusNodeIcon";
        collapseIcon = "minusNodeIcon";
        expandAllIcon = "expandAllNodeIcon";
        return EVAL_PAGE;
    }

    private void startNextNode(JspWriter out, String actionURL)
            throws IOException {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) listIterator.
                next();

        out.println("<tr class=\"sitemap1\">");
        out.println("  <td>&nbsp;</td>");
        out.print("  <td class=\"text\" nowrap>");

        List verticalLineCells = GUITreeTools.
                getLevelsWithVerticalLine(node);
        int nodeLevel = node.getLevel();
        for (int level = 0; level < nodeLevel; level++) {
            DefaultMutableTreeNode parentNode =
                    (DefaultMutableTreeNode) node.getParent();
            boolean isLastChild = (parentNode != null &&
                    node.equals(parentNode.getLastChild()));

            if (level < nodeLevel - 1) {
                if (verticalLineCells.contains(new Integer(level + 1))) {
                    displayIcon(vLineIcon, "", null, null, null, null,
                            "absmiddle", out);
                } else {
                    displayIcon(spacerIcon, "", null, null, new Integer(14),
                            new Integer(0), "absmiddle", out);
                }
            } else {
                if (!isLastChild) {
                    displayIcon(lineNodeIcon, "", null, null, null, null,
                            "absmiddle", out);
                } else if (isLastChild || node.isLeaf()) {
                    displayIcon(lastNodeIcon, "", null, null, null, null,
                            "absmiddle", out);
                } else {
                    displayIcon(spacerIcon, "", null, null, new Integer(14),
                            new Integer(0), "absmiddle", out);
                }
            }
        }

        QueryMapURL treeActionURL = new QueryMapURL(actionURL);
        treeActionURL.setQueryParameter("targettree", treeName);
        treeActionURL.setQueryParameter("nodeindex",
                Integer.toString(nodeCounter));
        treeActionURL.setQueryParameter(selectionParamName,
                Integer.toString(selectedNodeIndex));
        if (!node.isLeaf() && tree.isExpanded(new TreePath(node.getPath()))) {
            treeActionURL.setQueryParameter("guitree", "collapse");
            out.print("<a href=\"" + treeActionURL.toString() + "\">");
            displayIcon(collapseIcon, JahiaResourceBundle.getCommonResource(
                    "org.jahia.taglibs.html.controls.TreeTag.collapse.label",
                    getProcessingContext()), null, null, null, null, "absmiddle", out);
            out.print("</a>");
        } else if (!node.isLeaf()) {
            treeActionURL.setQueryParameter("guitree", "expandall");
            out.print("<a href=\"" + treeActionURL.toString() + "\">");
            displayIcon(expandAllIcon,
                    JahiaResourceBundle.
                            getCommonResource(
                            "org.jahia.taglibs.html.controls.TreeTag.expandAll.label",
                            getProcessingContext()), null, null, null, null,
                    "absmiddle", out);
            out.print("</a>");
            treeActionURL.setQueryParameter("guitree", "expand");
            out.print("<a href=\"" + treeActionURL.toString() + "\">");
            displayIcon(expandIcon,
                    JahiaResourceBundle.getCommonResource(
                            "org.jahia.taglibs.html.controls.TreeTag.expand.label",
                            getProcessingContext()), null, null, null, null, "absmiddle",
                    out);
            out.print("</a>");
        } else {
            displayIcon("org.jahia.pix.image", "", null, null, new Integer(14),
                    new Integer(0), "absmiddle", out);
        }

        userObject = node.getUserObject();

        pageContext.setAttribute(userObjectID, userObject);

        if (nodeIndexID != null) {
            pageContext.setAttribute(nodeIndexID, new Integer(nodeCounter));
        }

        if (selectionURLID != null) {
            treeActionURL.setQueryParameter("guitree", "expand");
            treeActionURL.setQueryParameter(selectionParamName,
                    Integer.toString(nodeCounter));
            pageContext.setAttribute(selectionURLID, treeActionURL.toString());
        }

        nodeCounter++;
    }

    private void displayIcon(String src, String alt, String altKey,
                             String bundleName,
                             Integer width, Integer height,
                             String align, JspWriter out)
            throws IOException {

        // now let's resolve the alt text if resource bundle keys are being used.
        if (altKey != null) {
            alt = JahiaResourceBundle.getResource(bundleName, altKey,
                    getProcessingContext().getLocale(),
                    getProcessingContext());
        }

        StringBuffer str = new StringBuffer();
        // Resolve file name
        String imagePath = JahiaResourceBundle.getUrlPathCommonResource(
                src, getProcessingContext());
        if (imagePath == null) {
            str.append("<!-- couldn't find resource with key " + src + " -->");
        } else {
            // Write image HTML tag
            str.append("<img alt=\"");
            str.append(alt);
            str.append("\" border=\"0\" src=\"");
            str.append(imagePath);
            str.append("\"");
            if (height != null) {
                str.append(" height=\"");
                str.append(height.intValue());
                str.append("\"");
            }
            if (width != null) {
                str.append(" width=\"");
                str.append(width.intValue());
                str.append("\"");
            }
            if (align != null) {
                str.append(" align=\"");
                str.append(align);
                str.append("\"");
            }
            str.append(">");
        }
        out.print(str.toString());

    }

}
