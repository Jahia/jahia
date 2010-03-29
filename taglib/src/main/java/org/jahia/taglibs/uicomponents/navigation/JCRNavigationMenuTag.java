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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.render.Resource;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class JCRNavigationMenuTag extends AbstractJahiaTag {

    private static final long serialVersionUID = 8195958771697928329L;
    private static transient final Logger logger = Logger.getLogger(JCRNavigationMenuTag.class);
    private String kind = null;
    private int startLevel = Integer.MIN_VALUE;
    private int maxDepth = Integer.MIN_VALUE;
    private boolean expandOnlyPageInPath = true;
    private boolean onlyTop = false;
    private boolean display = true;
    private boolean relativeToCurrentNode = false;    
    private String var;
    private JCRNodeWrapper node;
    private JCRNodeWrapper menuNode;    
    
    private Set<String> tagsToFilterBy = null;
    private Set<String> categoriesToFilterBy = null;
    private Set<String> typesToFilterBy = null;    

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

    public int doStartTag() throws JspException {

        try {
            Set<NavMenuItemBean> navMenuUItemsBeans = new LinkedHashSet<NavMenuItemBean>();
            settings();
            JCRNodeWrapper baseNode = node;
            int realStartLevel = startLevel;           
            if (isRelativeToCurrentNode() && baseNode != null) {
                baseNode = baseNode.getParent();
            }
            while (baseNode != null && !baseNode.isNodeType("jnt:virtualsite")
                    && (!isRelativeToCurrentNode() || realStartLevel < 0)) {
                baseNode = baseNode.getParent();
                if (isRelativeToCurrentNode()) {
                    realStartLevel++;
                }
            }
            if (realStartLevel < 0) {
                realStartLevel = 0;
            }

            generateMenuAsFlatList(realStartLevel, baseNode, 1,
                    navMenuUItemsBeans, null, baseNode != null ? baseNode.getPath() : null);
            if (StringUtils.isNotEmpty(var)) {
                pageContext.setAttribute(var, navMenuUItemsBeans);
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
                if (startLevel == Integer.MIN_VALUE) {
                    startLevel = 1; // default start level
                }
            } else if ("sideMenu".equals(kind)) {
                if (cssClassName == null) {
                    cssClassName = "sideMenu";
                }
                if (startLevel == Integer.MIN_VALUE) {
                    startLevel = 2; // default start level
                }
            }
        }
        
        if (getMenuNode() != null) {
            if (getTypesToFilterBy() == null) {
                Set<String> types = new HashSet<String>();
                try {
                    JCRPropertyWrapper property = getMenuNode().hasProperty(
                            Constants.ALLOWED_TYPES) ? getMenuNode()
                            .getProperty(Constants.ALLOWED_TYPES) : null;
                    if (property != null && property.getValues().length > 0) {
                        for (Value type : property.getValues()) {
                            types.add(type.getString());
                        }
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
                setTypesToFilterBy(types);
            }
            if (getTagsToFilterBy() == null) {
                try {
                    JCRPropertyWrapper property = getMenuNode().hasProperty(
                            Constants.TAGS) ? getMenuNode().getProperty(
                            Constants.TAGS) : null;

                    if (property != null && property.getValues().length > 0) {
                        Set<String> tags = new HashSet<String>(property
                                .getValues().length);

                        for (Value tag : property.getValues()) {
                            tags.add(tag.getString());
                        }
                        setTagsToFilterBy(tags);
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (getCategoriesToFilterBy() == null) {
                try {
                    JCRPropertyWrapper property = getMenuNode().hasProperty(
                            Constants.DEFAULT_CATEGORY) ? getMenuNode().getProperty(
                            Constants.DEFAULT_CATEGORY) : null;

                    if (property != null && property.getValues().length > 0) {
                        Set<String> categories = new HashSet<String>(property
                                .getValues().length);

                        for (Value category : property.getValues()) {
                            categories.add(category.getString());
                        }
                        setCategoriesToFilterBy(categories);
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        if (CollectionUtils.isEmpty(getTypesToFilterBy())) {
            Set<String> types = new HashSet<String>();
            types.add(Constants.JAHIANT_PAGE);
            setTypesToFilterBy(types);
        }
    }

    /**
     * Recursive method to go through pages hierarchy using a specific container list (attribute containerListName)
     * to fill a set (attribute navMenuItemsBean) containing all entries of the menu.
     *
     * @param realStartLevel   the start level relative to the base node
     * @param currentNode        the start node for recursion
     * @param level            the current depth level
     * @param navMenuItemsBean Set of navMenuItemBean containing informations of current menu item.
     * @param parentItem       parent item in subtree
     * @param basePath         the basepath of the current site/node
     * @throws java.io.IOException           JSP writer exception
     * @throws javax.jcr.RepositoryException In case of JCR error
     */
    private void generateMenuAsFlatList(final int realStartLevel, final JCRNodeWrapper currentNode, final int level, final Set<NavMenuItemBean> navMenuItemsBean,
                                        final NavMenuItemBean parentItem, final String basePath)
            throws IOException, RepositoryException {
        if (currentNode == null) {
            logger.error("Incorrect node : " + currentNode);
            return;
        }
        Resource res = (Resource) pageContext.getRequest().getAttribute("currentResource");
        
        boolean begin = true;

        final NodeIterator iterator = currentNode.getNodes();

        // if the list empty, add a navMenuItem for the action menu
        if (maxDepth == Integer.MIN_VALUE || level <= realStartLevel + maxDepth) {
            final String currentPath = node.getPath().replaceAll(
                    basePath + "/", "");
            int itemCount = 0;
            NavMenuItemBean navMenuItemBean = null;
            while (iterator.hasNext()) {
                JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) iterator
                        .nextNode();
                navMenuItemBean = new NavMenuItemBean();
                navMenuItemBean.setNode(nodeWrapper);
                navMenuItemBean.setParentItem(parentItem);

                boolean isInPath = true;
                if (level > realStartLevel) {
                    if (!CollectionUtils.isEmpty(getTypesToFilterBy())) {
                        boolean found = false;
                        for (String typeValue : getTypesToFilterBy()) {
                            if (nodeWrapper.isNodeType(typeValue)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            continue;
                        }
                    }
                    if (!CollectionUtils.isEmpty(getTagsToFilterBy())) {
                        boolean found = false;
                        for (String filterTag : getTagsToFilterBy()) {
                            JCRPropertyWrapper property = nodeWrapper
                                    .hasProperty(Constants.TAGS) ? nodeWrapper
                                    .getProperty(Constants.TAGS) : null;
                            if (property != null) {
                                for (Value setTag : property.getValues()) {
                                    if (setTag.getString().equals(filterTag)) {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!found) {
                            continue;
                        }
                    }
                    if (!CollectionUtils.isEmpty(getCategoriesToFilterBy())) {
                        boolean found = false;
                        for (String filterCategory : getCategoriesToFilterBy()) {
                            JCRPropertyWrapper property = nodeWrapper
                                    .hasProperty(Constants.DEFAULT_CATEGORY) ? nodeWrapper
                                    .getProperty(Constants.DEFAULT_CATEGORY)
                                    : null;
                            if (property != null) {
                                for (Value setCategory : property.getValues()) {
                                    if (setCategory.getString().equals(
                                            filterCategory)) {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!found) {
                            continue;
                        }
                    }
                    navMenuItemBean.setItemCount(++itemCount);
                    // First container
                    if (begin) {
                        navMenuItemBean.setFirstInLevel(true);
                        begin = false;
                    }
                    res.getDependencies().add(nodeWrapper);

                    if (logger.isDebugEnabled()) {
                        logger.debug("level = " + level);
                    }
                    // Set level
                    navMenuItemBean.setLevel(level - realStartLevel);

                    final String path = nodeWrapper.getPath().replaceAll(
                            basePath + "/", "");
                    final String[] pathElement = path.split("/");
                    final String[] currentPathElement = currentPath.split("/");
                    try {
                        isInPath = realStartLevel == 0
                                || pathElement[realStartLevel - 1]
                                        .equals(currentPathElement[realStartLevel - 1]);

                        if (isInPath) {
                            if (currentPath.equals(path)) {
                                navMenuItemBean.setSelected(true);
                            } else {
                                for (int i = 0; i < pathElement.length; i++) {
                                    String s = pathElement[i];
                                    if (i < currentPathElement.length
                                            && s.equals(currentPathElement[i])) {
                                        navMenuItemBean.setInPath(true);
                                    } else {
                                        navMenuItemBean.setInPath(false);
                                        break;
                                    }
                                }
                            }
                            navMenuItemsBean.add(navMenuItemBean);
                            if (parentItem != null) {
                                parentItem.setHasChildren(true);
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        logger.warn(e.getMessage(), e);
                    }
                }

                if (!onlyTop && (!expandOnlyPageInPath || isInPath)) {
                    generateMenuAsFlatList(realStartLevel, nodeWrapper,
                            level + 1, navMenuItemsBean, 
                            navMenuItemBean, basePath);
                }
            }
            // Last container
            if (navMenuItemBean != null) {
                navMenuItemBean.setLastInLevel(true);
            }
        }
    }

    public int doEndTag() throws JspException {
        resetState();
        return EVAL_PAGE;
    }

    @Override
    protected void resetState() {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        kind = null;
        expandOnlyPageInPath = true;
        onlyTop = false;
        startLevel = Integer.MIN_VALUE;
        maxDepth = Integer.MIN_VALUE;
        node = null;
        menuNode = null;
        tagsToFilterBy = null;
        categoriesToFilterBy = null;
        typesToFilterBy = null;        
        bodyContent = null;
        super.resetState();
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
        private boolean hasChildren;
        
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

        /**
         * @return the hasChildren
         */
        public boolean isHasChildren() {
            return hasChildren;
        }

        /**
         * @param hasChildren the hasChildren to set
         */
        public void setHasChildren(boolean hasChildren) {
            this.hasChildren = hasChildren;
        }
    }

    public boolean isRelativeToCurrentNode() {
        return relativeToCurrentNode;
    }

    public void setRelativeToCurrentNode(boolean relativeToCurrentNode) {
        this.relativeToCurrentNode = relativeToCurrentNode;
    }

    public Set<String> getTagsToFilterBy() {
        return tagsToFilterBy;
    }

    public void setTagsToFilterBy(Set<String> tagsToFilterBy) {
        this.tagsToFilterBy = tagsToFilterBy;
    }

    public Set<String> getCategoriesToFilterBy() {
        return categoriesToFilterBy;
    }

    public void setCategoriesToFilterBy(Set<String> categoriesToFilterBy) {
        this.categoriesToFilterBy = categoriesToFilterBy;
    }

    public Set<String> getTypesToFilterBy() {
        return typesToFilterBy;
    }

    public void setTypesToFilterBy(Set<String> typesToFilterBy) {
        this.typesToFilterBy = typesToFilterBy;
    }

    public JCRNodeWrapper getMenuNode() {
        return menuNode;
    }

    public void setMenuNode(JCRNodeWrapper menuNode) {
        this.menuNode = menuNode;
    }
}