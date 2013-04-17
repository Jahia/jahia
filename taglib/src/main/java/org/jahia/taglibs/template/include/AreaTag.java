/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.template.include;

import org.apache.commons.lang.StringUtils;
import org.apache.taglibs.standard.tag.common.core.ParamParent;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ConstraintsHelper;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.Template;
import org.slf4j.Logger;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.*;

/**
 * Handler for the &lt;template:module/&gt; tag, used to render content objects.
 * User: toto
 * Date: May 14, 2009
 * Time: 7:18:15 PM
 */
public class AreaTag extends ModuleTag implements ParamParent {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(AreaTag.class);

    private String areaType = "jnt:contentList";

    private String moduleType = "area";

    private String mockupStyle;

    private Integer level;

    private Template templateNode;

    private boolean areaAsSubNode;

    private String conflictsWith = null;
    
    public void setAreaType(String areaType) {
        this.areaType = areaType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    public void setMockupStyle(String mockupStyle) {
        this.mockupStyle = mockupStyle;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public void setAreaAsSubNode(boolean areaAsSubNode) {
        this.areaAsSubNode = areaAsSubNode;
    }

    @Override
    protected String getModuleType(RenderContext renderContext) throws RepositoryException {
        return moduleType;
    }

    protected void missingResource(RenderContext renderContext, Resource resource)
            throws RepositoryException, IOException {
        if (renderContext.isEditMode() && checkStudioLock(renderContext, node)) {
            try {
                constraints = ConstraintsHelper
                        .getConstraints(Arrays.asList(NodeTypeRegistry.getInstance().getNodeType(areaType)), null);
            } catch (RepositoryException e) {
                logger.error("Error when getting list constraints", e);
            }

            String areaPath = path;
            if (!path.startsWith("/")) {
                if (areaAsSubNode && resource.getNode().getPath().startsWith(renderContext.getMainResource().getNode().getPath())) {
                    areaPath = resource.getNode().getPath() + "/" + path;
                } else {
                    areaPath = renderContext.getMainResource().getNode().getPath() + "/" + path;
                }
            }

            String additionalParameters = "missingList=\"true\"";
            if (conflictsWith != null) {
                additionalParameters += " conflictsWith=\"" + conflictsWith + "\"";
            }
            if (renderContext.getEditModeConfigName().equals("contributemode")) {
                JCRNodeWrapper contributeNode = (JCRNodeWrapper) renderContext.getRequest().getAttribute("areaListResource");
                if (contributeNode == null || !contributeNode.hasProperty("j:contributeTypes")) {
                    additionalParameters += " editable=\"false\"";
                }
            }
            if (!StringUtils.isEmpty(mockupStyle)) {
                additionalParameters += " mockupStyle=\"" + mockupStyle + "\"";
            }
            additionalParameters += " areaHolder=\""+resource.getNode().getIdentifier()+"\"";
            printModuleStart(getModuleType(renderContext), areaPath, null, null, additionalParameters);
            if (getBodyContent() != null) {
                getPreviousOut().write(getBodyContent().getString());
            }
            printModuleEnd();
        }
    }

    protected String getConfiguration() {
        return Resource.CONFIGURATION_WRAPPEDCONTENT;
    }

    @Override protected boolean canEdit(RenderContext renderContext) {
        if (path != null) {
            boolean stillInWrapper = false;
            return renderContext.isEditMode() && editable && !stillInWrapper &&
                    renderContext.getRequest().getAttribute("inArea") == null;
        } else {
            return super.canEdit(renderContext);
        }
    }

    protected void findNode(RenderContext renderContext, Resource currentResource) throws IOException {
        Resource mainResource = renderContext.getMainResource();

        if (renderContext.isAjaxRequest() && renderContext.getAjaxResource() != null) {
            mainResource = renderContext.getAjaxResource();
        }
        renderContext.getRequest().removeAttribute("skipWrapper");
        renderContext.getRequest().removeAttribute("inArea");
        pageContext.setAttribute("org.jahia.emptyArea",Boolean.TRUE, PageContext.PAGE_SCOPE);
        try {
            // path is null in main resource display
            Template t = (Template) renderContext.getRequest().getAttribute("previousTemplate");
            templateNode = t;

            if ("absoluteArea".equals(moduleType)) {
                // No more areas in an absolute area
                renderContext.getRequest().setAttribute("previousTemplate", null);
                JCRNodeWrapper main = null;
                try {
                    main = renderContext.getMainResource().getNode();
                    if (level != null && main.getDepth() >= level + 3) {
                        node = (JCRNodeWrapper) main.getAncestor(level + 3);
                    } else if (level == null) {
                        node = renderContext.getSite().getHome();
                    } else {
                        return;
                    }
                    if (node == null) {
                        return;
                    }
                    if(logger.isDebugEnabled()) {
                        logger.debug("Looking for absolute area "+path+", will be searched in node "+ node.getPath() +
                                     " saved template = "+templateNode.serialize()+", previousTemplate set to null");
                    }
                    node = node.getNode(path);
                    pageContext.setAttribute("org.jahia.emptyArea",Boolean.FALSE, PageContext.PAGE_SCOPE);
                } catch (RepositoryException e) {
                    if (node != null) {
                        path = node.getPath() + "/" + path;
                    }
                    node = null;
                    missingResource(renderContext, currentResource);
                } finally {
                    if (node == null && logger.isDebugEnabled()) {
                        if (level == null) {
                            logger.debug(
                                    "Cannot get a node {}, relative to the home page of site {}"
                                            + " for main resource {}",
                                    new String[] {
                                            path,
                                            main != null && main.getResolveSite() != null ? main.getResolveSite().getPath() : null,
                                            main != null ? main.getPath() : null });
                        } else {
                            logger.debug(
                                    "Cannot get a node {}, with level {} for main resource {}",
                                    new String[] { path, String.valueOf(level), main != null ? main.getPath() : null });
                        }
                    }
                }
            } else if (path != null) {
                if (!path.startsWith("/")) {
                    List<JCRNodeWrapper> nodes = new ArrayList<JCRNodeWrapper>();
                    if (t != null) {
                        for (Template currentTemplate : t.getNextTemplates()) {
                            nodes.add(0,
                                    mainResource.getNode().getSession().getNodeByIdentifier(currentTemplate.getNode()));
                        }
                    }
                    nodes.add(mainResource.getNode());
                    boolean isCurrentResource = false;
                    if (areaAsSubNode) {
                        nodes.add(0,currentResource.getNode());
                        isCurrentResource = true;
                    }
                    boolean found = false;
                    boolean notMainResource = false;

                    Set<String> allPaths = new HashSet<String>();
                    for (Resource resource : renderContext.getResourcesStack()) {
                        allPaths.add(resource.getNode().getPath());
                    }

                    for (JCRNodeWrapper node : nodes) {
                        if (!path.equals("*") && node.hasNode(path) && !allPaths.contains(node.getPath()+"/"+path)) {
                            notMainResource = mainResource.getNode() != node && !node.getPath().startsWith(renderContext.getMainResource().getNode().getPath());
                            this.node = node.getNode(path);
                            if (currentResource.getNode().getParent().getPath().equals(this.node.getPath())) {
                                this.node = null;
                            } else {
                                // now let's check if the content node matches the areaType. If not it means we have a
                                // conflict with another content created outside of the content of the area (DEVMINEFI-223)
                                if (!this.node.isNodeType(areaType)) {
                                    conflictsWith = this.node.getPath();
                                    found = false;
                                    this.node = null;
                                    break;
                                } else {
                                    found = true;
                                    pageContext.setAttribute("org.jahia.emptyArea",Boolean.FALSE, PageContext.PAGE_SCOPE);
                                    break;
                                }
                            }
                        }
                        if (t != null && !isCurrentResource) {
                            t = t.getNext();
                        }
                        isCurrentResource = false;
                    }
                    renderContext.getRequest().setAttribute("previousTemplate", t);
                    if(logger.isDebugEnabled()) {
                        String tempNS = (templateNode!=null)?templateNode.serialize():null;
                        String prevNS = (t!=null)?t.serialize():null;
                        logger.debug("Looking for local area "+path+", will be searched in node "+ (node!=null?node.getPath():null) +
                                     " saved template = "+tempNS+", previousTemplate set to "+prevNS);
                    }
                    boolean templateEdit = mainResource.getModuleParams().containsKey("templateEdit") &&mainResource.getModuleParams().get("templateEdit").equals(node.getParent().getIdentifier());
                    if (notMainResource && !templateEdit) {
                        renderContext.getRequest().setAttribute("inArea", Boolean.TRUE);
                    }
                    if (!found) {
                        missingResource(renderContext, currentResource);
                    }
                } else if (path.startsWith("/")) {
                    JCRSessionWrapper session = mainResource.getNode().getSession();

                    // No more areas in an absolute area
                    renderContext.getRequest().setAttribute("previousTemplate", null);
                    if(logger.isDebugEnabled()) {
                        logger.debug("Looking for absolute area "+path+", will be searched in node "+ node.getPath() +
                                     " saved template = "+templateNode.serialize()+", previousTemplate set to null");
                    }
                    try {
                        node = (JCRNodeWrapper) session.getItem(path);
                        pageContext.setAttribute("org.jahia.emptyArea",Boolean.FALSE, PageContext.PAGE_SCOPE);
                    } catch (PathNotFoundException e) {
                        missingResource(renderContext, currentResource);
                    }
                }
                renderContext.getRequest().setAttribute("skipWrapper", Boolean.TRUE);
            } else {
                renderContext.getRequest().setAttribute("previousTemplate", null);
                renderContext.getRequest().removeAttribute("skipWrapper");
                node = mainResource.getNode();
                pageContext.setAttribute("org.jahia.emptyArea",Boolean.FALSE, PageContext.PAGE_SCOPE);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        if (node == null && logger.isDebugEnabled()) {
            logger.debug("Can not find the area node for path " + path + " with templates " + templateNode +
                         "rendercontext " + renderContext + " main resource " + mainResource +
                         " current resource " + currentResource);
        }
    }

    @Override public int doEndTag() throws JspException {
        Object o = pageContext.getRequest().getAttribute("inArea");
        try {
            return super.doEndTag();
        } finally {
            pageContext.getRequest().setAttribute("previousTemplate", templateNode);
            if(logger.isDebugEnabled()) {
                        logger.debug("Restoring previous template "+templateNode.serialize());
                    }
            templateNode = null;
            level = null;
            areaAsSubNode = false;
            conflictsWith = null;
            pageContext.getRequest().setAttribute("inArea", o);

        }
    }

    @Override
    protected void render(RenderContext renderContext, Resource resource) throws IOException {
        if (canEdit(renderContext) || !isEmptyArea() || path == null) {
            super.render(renderContext, resource);
        }
    }

    protected  boolean isEmptyArea() {
        for (String s : constraints.split(" ")) {
            if (!JCRContentUtils.getChildrenOfType(node, s).isEmpty()) {
                return false;
            }
        }
        return true;
    }

}
