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
package org.jahia.taglibs.template.include;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.common.core.ParamParent;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;

/**
 * Handler for the &lt;template:module/&gt; tag, used to render content objects.
 * User: toto
 * Date: May 14, 2009
 * Time: 7:18:15 PM
 */
public class WrappedContentTag extends ModuleTag implements ParamParent {

    private static Logger logger = Logger.getLogger(WrappedContentTag.class);

    private String areaType = "jnt:contentList";

    public void setAreaType(String areaType) {
        this.areaType = areaType;
    }

    @Override
    protected String getModuleType() throws RepositoryException {
//        if (!path.startsWith("/")) {
        return "area";
//        } else {
//            return "absolutearea";
//        }
    }

    protected void missingResource(RenderContext renderContext, Resource mainResource, Resource resource)
            throws RepositoryException, IOException {
        node = null;
        try {
            if (renderContext.isEditMode()) {
                JCRSessionWrapper session = resource.getNode().getSession();
                if (!path.startsWith("/")) {
                    JCRNodeWrapper nodeWrapper = resource.getNode();
                    if (!nodeWrapper.isLocked()) {
                        if (!nodeWrapper.isCheckedOut()) {
                            nodeWrapper.checkout();
                        }
                        node = nodeWrapper.addNode(path, areaType);
                    }
                } else {
                    // Absolute area
                    JCRNodeWrapper parent = session.getNode(StringUtils.substringBeforeLast(path, "/"));
                    if (!parent.isLocked()) {
                        if (!parent.isCheckedOut()) {
                            parent.checkout();
                        }
                        node = parent.addNode(StringUtils.substringAfterLast(path, "/"), areaType);
                    }
                }
                NodeIterator ni = mainResource.getNode().getNodes();
                while (ni.hasNext()) {
                    JCRNodeWrapper subNode = (JCRNodeWrapper) ni.next();
                    subNode.copy(node.getPath());
                }
                session.save();
            }
        } catch (ConstraintViolationException e) {
            super.missingResource(renderContext, resource);
        } catch (RepositoryException e) {
            logger.error("Cannot create area", e);
        }
    }

    protected String getConfiguration() {
        return Resource.CONFIGURATION_WRAPPEDCONTENT;
    }

    @Override protected boolean canEdit(RenderContext renderContext) {
        if (path != null) {
            boolean stillInWrapper = false;
            return renderContext.isEditMode() && editable && !stillInWrapper;
        } else {
            return super.canEdit(renderContext);
        }
    }

    protected void findNode(RenderContext renderContext, Resource currentResource) throws IOException {
        Resource resource = renderContext.getMainResource();

        if (renderContext.isAjaxRequest() && renderContext.getAjaxResource() != null) {
            resource = renderContext.getAjaxResource();
        }
        node = resource.getNode();
        renderContext.getRequest().removeAttribute("skipWrapper");

        if (path != null) {
            try {
                if (!path.startsWith("/")) {
                    if (!path.equals("*") && node.hasNode(path)) {
                        node = node.getNode(path);
                    } else {
                        missingResource(renderContext, currentResource, resource);
                    }
                } else if (path.startsWith("/")) {
                    JCRSessionWrapper session = node.getSession();
                    try {
                        node = (JCRNodeWrapper) session.getItem(path);
                    } catch (PathNotFoundException e) {
                        missingResource(renderContext, currentResource, resource);
                    }
                }
                renderContext.getRequest().setAttribute("skipWrapper", Boolean.TRUE);
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override protected void printModuleStart(String type, String path, String resolvedTemplate, String scriptInfo)
            throws RepositoryException, IOException {

        if (this.path.startsWith("/")) {
            RenderContext context =
                    (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);
            pageContext.getOut().print("<div jahiatype=\"linkedContentInfo\" linkedNode=\"" +
                    context.getMainResource().getNode().getIdentifier() + "\"" + " node=\"" + node.getIdentifier() +
                    "\" type=\"absolute\">");
        }

        super.printModuleStart(type, path, resolvedTemplate,
                scriptInfo);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override protected void printModuleEnd() throws IOException {
        super.printModuleEnd();

        if (path.startsWith("/")) {
            pageContext.getOut().print("</div>");
        }

    }
}