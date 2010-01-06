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
package org.jahia.ajax.gwt.definitions.server;

import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.commons.server.JahiaRemoteService;
import org.jahia.ajax.gwt.helper.ContentDefinitionHelper;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.params.ParamBean;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.registries.ServicesRegistry;
import org.apache.log4j.Logger;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;

import javax.jcr.RepositoryException;
import java.util.*;

/**
 * User: toto
 * Date: Aug 25, 2008
 * Time: 6:26:11 PM
 */
public class JahiaContentDefinitionServiceImpl extends JahiaRemoteService implements JahiaContentDefinitionService {
    private static final transient Logger logger = Logger.getLogger(JahiaContentDefinitionServiceImpl.class);

    private ContentDefinitionHelper contentDefinition;

    public void setContentDefinition(ContentDefinitionHelper contentDefinition) {
        this.contentDefinition = contentDefinition;
    }

    public GWTJahiaNodeType getNodeType(String name) {
        return contentDefinition.getNodeType(name, retrieveParamBean());
    }

    public Map<GWTJahiaNodeType,Map<GWTJahiaNodeType,List<GWTJahiaNode>>> getNodeTypes() {
        return contentDefinition.getNodeTypes(retrieveParamBean());
    }

    public List<GWTJahiaNodeType> getNodeTypes(List<String> names) {
        return contentDefinition.getNodeTypes(names, retrieveParamBean());
    }

    /**
     * Returns a list of node types with name and label populated that are the
     * sub-types of the specified base type.
     * 
     * @param baseType
     *            the node type name to find sub-types
     * @param parentNode
     *            the parent node, where the wizard was called
     * @return a list of node types with name and label populated that are the
     *         sub-types of the specified base type
     */
    public Map<GWTJahiaNodeType, Map<GWTJahiaNodeType, List<GWTJahiaNode>>> getNodeSubtypes(String baseType, GWTJahiaNode parentNode) {
        return contentDefinition.getNodeSubtypes(baseType, parentNode, retrieveParamBean());
    }

    public Map<GWTJahiaNodeType, List<GWTJahiaNode>> getNodeTypeWithReusableComponents(String type) throws GWTJahiaServiceException {
        return contentDefinition.getNodeTypeWithReusableComponents(type, retrieveParamBean());
    }

    public List<GWTJahiaNodeType> getAvailableMixin(GWTJahiaNodeType type) {
        return contentDefinition.getAvailableMixin(type, retrieveParamBean());
    }

    public List<GWTJahiaNodeType> getAvailableMixin(GWTJahiaNode node) {
        ParamBean ctx = retrieveParamBean();
        try {
            JCRNodeWrapper nodeWrapper = ServicesRegistry.getInstance().getJCRStoreService().getSessionFactory().getCurrentUserSession().getNode(node.getPath());
            ctx.setAttribute("contextNode", nodeWrapper);
        } catch (RepositoryException e) {
            logger.error("Cannot get node", e);
        }
        return contentDefinition.getAvailableMixin(getNodeType(node.getNodeTypes().iterator().next()), ctx);
    }
}
