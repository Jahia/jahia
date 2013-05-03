/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.taglibs.uicomponents;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.render.RenderContext;
import org.slf4j.Logger;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * Custom facet functions, which are exposed into the template scope.
 *
 * @author Benjamin Papez
 */
public class Functions {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Functions.class);

    public static JCRNodeWrapper getBoundComponent(JCRNodeWrapper currentNode, RenderContext renderContext,
            String property) {
        JCRNodeWrapper boundComponentNode = null;
        try {
            JCRNodeWrapper mainResource = renderContext.getMainResource().getNode();
            if (renderContext.getAjaxResource() != null) {
                mainResource = renderContext.getAjaxResource().getNode();
            }
            if (currentNode.hasProperty(property)) {
                JCRPropertyWrapper boundComponentProp = currentNode.getProperty(property);
                if (boundComponentProp != null) {
                    boundComponentNode = (JCRNodeWrapper) boundComponentProp.getNode();
                }
                if (boundComponentNode != null) {
                    if (boundComponentNode.isNodeType(Constants.JAHIANT_MAINRESOURCE_DISPLAY) ||
                        boundComponentNode.isNodeType("jnt:template")) {
                        boundComponentNode = mainResource;
                    } else if (boundComponentNode.isNodeType(Constants.JAHIANT_AREA)) {
                        String areaName = boundComponentNode.getName();
                        boundComponentNode = mainResource;
                        if (boundComponentNode.hasNode(areaName)) {
                            boundComponentNode = boundComponentNode.getNode(areaName);
                        } else {
                            boundComponentNode = null;
                        }
                    }
                }
            } else {
                boundComponentNode = mainResource;
            }
            if (boundComponentNode != null && !boundComponentNode.getPath().equals(
                    mainResource.getPath())) {
                renderContext.getResourcesStack().peek().getDependencies().add(boundComponentNode.getCanonicalPath());
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return boundComponentNode;
    }
    
    public static String getBoundComponentPath(JCRNodeWrapper currentNode, RenderContext renderContext, String property) {
        Node boundComponentNode = null;
        try {
            if (currentNode.hasProperty(property)) {
                Property boundComponentProp = currentNode.getProperty(property);
                if (boundComponentProp != null) {
                    boundComponentNode = boundComponentProp.getNode();
                }
                if (boundComponentNode != null) {
                    if (boundComponentNode.isNodeType(Constants.JAHIANT_MAINRESOURCE_DISPLAY)
                            || boundComponentNode.isNodeType("jnt:template")) {
                        boundComponentNode = renderContext.getMainResource().getNode();
                    } else if (boundComponentNode.isNodeType(Constants.JAHIANT_AREA)) {
                        String areaName = boundComponentNode.getName();
                        boundComponentNode = renderContext.getMainResource().getNode();
                        return boundComponentNode.getPath() + "/" + areaName;
                    }
                }
            }
            if (boundComponentNode != null) {
                return boundComponentNode.getPath();
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }    
    
    /**
     * @deprecated use {@link #getBoundComponent(JCRNodeWrapper, RenderContext, String)} instead
     */
    @Deprecated
    public static JCRNodeWrapper getBindedComponent(JCRNodeWrapper currentNode, RenderContext renderContext,
                                                    String property) {
        return getBoundComponent(currentNode, renderContext, property);
    }

    /**
     * @deprecated use {@link #getBoundComponentPath(JCRNodeWrapper, RenderContext, String)} instead
     */
    @Deprecated
    public static String getBindedComponentPath(JCRNodeWrapper currentNode, RenderContext renderContext,
                                                String property) {
        return getBoundComponentPath(currentNode, renderContext, property);
    }
}
