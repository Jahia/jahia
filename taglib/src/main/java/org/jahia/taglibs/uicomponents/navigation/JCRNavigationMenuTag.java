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
package org.jahia.taglibs.uicomponents.navigation;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.jahia.engines.calendar.CalendarHandler;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.jcr.node.JCRTagUtils;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Set;


@SuppressWarnings("serial")
public class JCRNavigationMenuTag extends AbstractJahiaTag {

    private static transient final Category logger = Logger.getLogger(JCRNavigationMenuTag.class);
    private String kind = null;
    private int startLevel = -1;
    private int maxDepth = -1;
    private boolean expandOnlyPageInPath = true;
    private boolean onlyTop = false;
    private boolean display = true;
    private String var;
    private JCRNodeWrapper node;

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setStartLevel(int startLevel) {
        this.startLevel = startLevel;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void setExpandOnlyPageInPath(boolean expandOnlyPageInPath) {
        this.expandOnlyPageInPath = expandOnlyPageInPath;
    }

    public void setOnlyTop(boolean onlyTop) {
        this.onlyTop = onlyTop;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public String getVar() {
        return var;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    private static SimpleDateFormat dateFormat = new SimpleDateFormat(CalendarHandler.DEFAULT_DATE_FORMAT);

    public int doStartTag() throws JspException {

        try {
            Set<NavMenuItemBean> navMenuUItemsBean = new LinkedHashSet<NavMenuItemBean>();
            settings();
            if (node != null) {
                JCRNodeWrapper siteNode = node.getParent();
                while (!siteNode.isNodeType("jnt:virtualsite")) {
                    siteNode = siteNode.getParent();
                }
                generateMenuAsFlatList(siteNode, 0, navMenuUItemsBean, 0, null, siteNode.getPath());
                pageContext.setAttribute(var, navMenuUItemsBean);
            }
            return EVAL_BODY_BUFFERED;

        } catch (IOException e) {
            logger.error("IOException rendering the menu", e);
        } catch (RepositoryException e) {
            logger.error("Error while rendering the navigation menu tag", e);
        }
        return SKIP_BODY;
    }

    /**
     * Prepare tag environment depending on some tag attributes.
     *
     * @throws org.jahia.exceptions.JahiaException
     *          if start level was not specified and container list retrieval failed
     * @throws javax.servlet.jsp.JspTagException
     *          tag exception
     */
    private void settings() throws JspTagException {

        // set various parameters according to the menu kind
        if (kind != null) {

            // top tabs configuration
            if ("topTabs".equals(kind)) {
                if (cssClassName == null) {
                    cssClassName = "topTabs"; // default name
                }
                onlyTop = true;
                if (startLevel == -1) {
                    startLevel = 1; // default start level
                }
            } else if ("sideMenu".equals(kind)) {
                if (cssClassName == null) {
                    cssClassName = "sideMenu";
                }
                if (startLevel == -1) {
                    startLevel = 2; // default start level
                }
            }
        }
    }

    /**
     * Recursive method to go through pages hierarchy using a specific container list (attribute containerListName)
     * to fill a set (attribute navMenuItemsBean) containing all entries of the menu.
     *
     * @param startNode        the start node for recursion
     * @param level            the current depth level
     * @param navMenuItemsBean Set of navMenuItemBean containing informations of current menu item.
     * @param loopIt           index of current iteration
     * @param parentItem       parent item in subtree
     * @param sitePath         the sitepath of the current iste
     * @throws java.io.IOException           JSP writer exception
     * @throws javax.jcr.RepositoryException In case of JCR error
     */
    private void generateMenuAsFlatList(JCRNodeWrapper startNode, int level, Set<NavMenuItemBean> navMenuItemsBean,
                                        int loopIt, NavMenuItemBean parentItem, String sitePath)
            throws IOException, RepositoryException {

        if (node == null) {
            logger.error("Incorrect node : " + node);
            // throw new IllegalArgumentException("attribute pageID cannot be < 1 (is " + pageId + ")");
            return;
        }

        boolean begin = true;
//        final NodeIterator iterator = JCRTagUtils.getNodes(startNode, "jnt:page");
        final NodeIterator iterator = startNode.getNodes();

        // if the list empty, add a navMenuItem for the action menu
        if (maxDepth == -1 || level <= startLevel + maxDepth) {
            int itemCount = 0;
            while (iterator.hasNext()) {
                JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) iterator.nextNode();
                if (!nodeWrapper.isNodeType("jnt:page")) {
                    continue;
                }
                NavMenuItemBean navMenuItemBean = new NavMenuItemBean();
                navMenuItemBean.setNode(nodeWrapper);
                navMenuItemBean.setParentItem(parentItem);
                itemCount++;
                navMenuItemBean.setItemCount(itemCount);
                logger.debug("level = " + level);
                // Set level
                navMenuItemBean.setLevel(level);
                // set class = "selected" on the link
                // First container
                if (begin) {
                    navMenuItemBean.setFirstInLevel(true);
                    begin = false;
                }
                // Last container
                if (!iterator.hasNext()) {
                    navMenuItemBean.setLastInLevel(true);
                }
                boolean isInPath = true;
                if (level >= startLevel) {
                    final String path = nodeWrapper.getPath().replaceAll(sitePath + "/", "");
                    final String currentPath = this.node.getPath().replaceAll(sitePath + "/", "");
                    final String[] pathElement = path.split("/");
                    final String[] currentPathElement = currentPath.split("/");
                    try {
                        isInPath = startLevel == 0 || pathElement[startLevel - 1].equals(currentPathElement[startLevel - 1]);

                        if (isInPath) {
                            if (currentPath.equals(path)) {
                                navMenuItemBean.setSelected(true);
                            } else {
                                for (int i = 0; i < pathElement.length; i++) {
                                    String s = pathElement[i];
                                    if(i < currentPathElement.length && s.equals(currentPathElement[i])) {
                                        navMenuItemBean.setInPath(true);
                                    } else {
                                        navMenuItemBean.setInPath(false);
                                        break;
                                    }
                                }
                            }
                            navMenuItemsBean.add(navMenuItemBean);
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        
                    }
                }

                if (!onlyTop && (!expandOnlyPageInPath || isInPath)) {
                    generateMenuAsFlatList(nodeWrapper, level + 1, navMenuItemsBean, loopIt + 1, navMenuItemBean,
                                           sitePath);
                }
            }
        }
    }

    public int doEndTag() throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        kind = null;
        expandOnlyPageInPath = true;
        onlyTop = false;
        startLevel = -1;
        maxDepth = -1;
        bodyContent = null;
        super.resetState();
        return EVAL_PAGE;
    }

    public class NavMenuItemBean implements Comparable<NavMenuItemBean> {
        private String separator = "";
        private int level = 0;
        private boolean firstInLevel = false;
        private boolean lastInLevel = false;
        private boolean inPath = false;
        private boolean selected = false;
        private int itemCount = 0;
        private NavMenuItemBean parentItem = null;
        private JCRNodeWrapper node = null;

        public String getSeparator() {
            return separator;
        }

        public void setSeparator(String separator) {
            this.separator = separator;
        }

        public int getItemCount() {
            return itemCount;
        }

        public void setItemCount(int itemCount) {
            this.itemCount = itemCount;
        }

        public boolean isFirstInLevel() {
            return firstInLevel;
        }

        public void setFirstInLevel(boolean firstInLevel) {
            this.firstInLevel = firstInLevel;
        }

        public boolean isInPath() {
            return inPath;
        }

        public void setInPath(boolean inPath) {
            this.inPath = inPath;
        }

        public boolean isLastInLevel() {
            return lastInLevel;
        }

        public void setLastInLevel(boolean lastInLevel) {
            this.lastInLevel = lastInLevel;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public NavMenuItemBean getParentItem() {
            return parentItem;
        }

        public void setParentItem(NavMenuItemBean parentItem) {
            this.parentItem = parentItem;
        }

        public JCRNodeWrapper getNode() {
            return node;
        }

        public void setNode(JCRNodeWrapper node) {
            this.node = node;
        }

        public NavMenuItemBean() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof NavMenuItemBean)) {
                return false;
            }

            NavMenuItemBean that = (NavMenuItemBean) o;

            try {
                return getNode().getIdentifier().equals(that.getNode().getIdentifier());
            } catch (RepositoryException e) {
                return true;
            }
        }

        @Override
        public int hashCode() {
            try {
                return node.getIdentifier().hashCode();
            } catch (RepositoryException e) {
                return 0;
            }
        }

        public int compareTo(NavMenuItemBean navMenuB) throws ClassCastException {
            try {
                return getNode().getProperty("jcr:title").getString().compareTo(navMenuB.getNode().getProperty("jcr:title").getString());
            } catch (RepositoryException e) {
                return -1;
            }
        }
    }
}