/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.uicomponents;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.render.RenderContext;
import org.slf4j.Logger;

import javax.jcr.ItemNotFoundException;
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
            boundComponentNode = getBoundJcrNodeWrapper(currentNode, renderContext, property, boundComponentNode);
        } catch (ItemNotFoundException e) {
            logger.debug(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return boundComponentNode;
    }
    
    public static String getBoundComponentPath(JCRNodeWrapper currentNode, RenderContext renderContext, String property) {
        JCRNodeWrapper boundComponentNode = null;
        try {
            boundComponentNode = getBoundJcrNodeWrapper(currentNode, renderContext, property, boundComponentNode);
            if (boundComponentNode != null) {
                return boundComponentNode.getPath();
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private static JCRNodeWrapper getBoundJcrNodeWrapper(JCRNodeWrapper currentNode, RenderContext renderContext, String property, JCRNodeWrapper boundComponentNode) throws RepositoryException {
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
        return boundComponentNode;
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
