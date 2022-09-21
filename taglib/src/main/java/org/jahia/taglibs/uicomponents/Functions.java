/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

    /**
     * Returns the node which corresponds to the bound component of the provided property in the specified node.
     * 
     * @param currentNode the node to get the bound component for
     * @param renderContext current render context
     * @param property the property name to lookup bound component
     * @return the bound node
     */
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
    
    /**
     * Returns the node path which corresponds to the bound component of the provided property in the specified node.
     * 
     * @param currentNode the node to get the bound component for
     * @param renderContext current render context
     * @param property the property name to lookup bound component
     * @return the bound node path
     */
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
     * Returns the node which corresponds to the bound component of the provided property in the specified node.
     * 
     * @param currentNode the node to get the bound component for
     * @param renderContext current render context
     * @param property the property name to lookup bound component
     * @return the bound node
     * @deprecated use {@link #getBoundComponent(JCRNodeWrapper, RenderContext, String)} instead
     */
    @Deprecated
    public static JCRNodeWrapper getBindedComponent(JCRNodeWrapper currentNode, RenderContext renderContext,
                                                    String property) {
        return getBoundComponent(currentNode, renderContext, property);
    }

    /**
     * Returns the node path which corresponds to the bound component of the provided property in the specified node.
     * 
     * @param currentNode the node to get the bound component for
     * @param renderContext current render context
     * @param property the property name to lookup bound component
     * @return the bound node path
     * @deprecated use {@link #getBoundComponentPath(JCRNodeWrapper, RenderContext, String)} instead
     */
    @Deprecated
    public static String getBindedComponentPath(JCRNodeWrapper currentNode, RenderContext renderContext,
                                                String property) {
        return getBoundComponentPath(currentNode, renderContext, property);
    }
}
