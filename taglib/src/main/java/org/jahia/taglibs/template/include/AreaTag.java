/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.taglibs.template.include;

import org.apache.taglibs.standard.tag.common.core.ParamParent;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ConstraintsHelper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.Template;
import org.slf4j.Logger;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.security.Privilege;
import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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

    private Template templateNode;

    public void setAreaType(String areaType) {
        this.areaType = areaType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    public void setMockupStyle(String mockupStyle) {
        this.mockupStyle = mockupStyle;
    }

    @Override
    protected String getModuleType(RenderContext renderContext) throws RepositoryException {
        return moduleType;
    }

    protected void missingResource(RenderContext renderContext, Resource mainResource, Resource resource)
            throws RepositoryException, IOException {
        if (renderContext.isEditMode()) {
            try {
                constraints = ConstraintsHelper
                        .getConstraints(Arrays.asList(NodeTypeRegistry.getInstance().getNodeType(areaType)));
            } catch (RepositoryException e) {
                logger.error("Error when getting list constraints", e);
            }

            String areaPath = path;
            if (!path.startsWith("/")) {
                areaPath = renderContext.getMainResource().getNode().getPath() + "/" + path;
            }

            String additionalParameters = "missingList=\"true\"";
            if (mockupStyle != null) {
                additionalParameters += " mockupStyle=\"" + mockupStyle + "\"";
            }

            printModuleStart(getModuleType(renderContext), areaPath, null, "No script", additionalParameters);
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
        Resource resource = renderContext.getMainResource();

        if (renderContext.isAjaxRequest() && renderContext.getAjaxResource() != null) {
            resource = renderContext.getAjaxResource();
        }
        renderContext.getRequest().removeAttribute("skipWrapper");
        renderContext.getRequest().removeAttribute("inArea");

        try {
            // path is null in main resource display
            Template t = (Template) renderContext.getRequest().getAttribute("previousTemplate");
            templateNode = t;

            if (path != null) {
                if (currentResource.getNode().isNodeType("jnt:area") && t != null) {
                    // Skip to next node automatically if you're in an area to avoid loop
                    t = t.next;
                }

                if (!path.startsWith("/")) {
                    List<JCRNodeWrapper> nodes = new ArrayList<JCRNodeWrapper>();
                    if (t != null) {
                        for (Template currentTemplate : t.getNextTemplates()) {
                            nodes.add(0,
                                    resource.getNode().getSession().getNodeByIdentifier(currentTemplate.getNode()));
                        }
                    }
                    nodes.add(resource.getNode());

                    boolean found = false;
                    boolean currentMain = false;
                    for (JCRNodeWrapper node : nodes) {
                        if (!path.equals("*") && node.hasNode(path)) {
                            currentMain = resource.getNode() != node;
                            this.node = node.getNode(path);
                            if (currentResource.getNode().getParent().getPath().equals(this.node.getPath())) {
                                this.node = null;
                            } else {
                                found = true;
                                break;
                            }
                        }
                        if (t != null) {
                            t = t.getNext();
                        }
                    }
                    renderContext.getRequest().setAttribute("previousTemplate", t);
                    if (currentMain) {
                        renderContext.getRequest().setAttribute("inArea", Boolean.TRUE);
                    }
                    if (!found) {
                        missingResource(renderContext, currentResource, resource);
                    }
                } else if (path.startsWith("/")) {
                    JCRSessionWrapper session = resource.getNode().getSession();

                    // No more areas in an absolute area
                    renderContext.getRequest().setAttribute("previousTemplate", null);
                    try {
                        node = (JCRNodeWrapper) session.getItem(path);
                    } catch (PathNotFoundException e) {
                        missingResource(renderContext, currentResource, resource);
                    }
                }
                renderContext.getRequest().setAttribute("skipWrapper", Boolean.TRUE);
            } else {
                renderContext.getRequest().removeAttribute("skipWrapper");
                node = resource.getNode();
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override public int doEndTag() throws JspException {
        Object o = pageContext.getRequest().getAttribute("inArea");
        try {
            return super.doEndTag();
        } finally {
            pageContext.getRequest().setAttribute("previousTemplate", templateNode);
            templateNode = null;
            pageContext.getRequest().setAttribute("inArea", o);

        }
    }
}