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
package org.jahia.services.content.impl.jahia;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.PropertyType;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.jahia.content.ContentObject;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.exceptions.JahiaException;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jul 3, 2008
 * Time: 11:27:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class JahiaWorkflowSettingsNodeImpl extends NodeImpl {
    private transient static Logger logger = Logger.getLogger(JahiaWorkflowSettingsNodeImpl.class);
    private ContentObject object;
    private Node parent;

    public JahiaWorkflowSettingsNodeImpl(SessionImpl session, NodeImpl parent, ContentObject object) {
        super(session);
        this.object = object;
        this.parent = parent;
        try {
            setDefinition(NodeTypeRegistry.getInstance().getNodeType("jmix:workflowed").getChildNodeDefinitionsAsMap().get("j:workflow"));
            setNodetype(NodeTypeRegistry.getInstance().getNodeType("jnt:workflow"));
        } catch (NoSuchNodeTypeException e) {
            logger.error(e);
        }
    }

    public Node getParent() {
        return parent;
    }

    @Override
    protected void initProperties() throws RepositoryException {
        if (properties == null) {
            super.initProperties();
            String type = "";
            try {
                final WorkflowService service = ServicesRegistry.getInstance().getWorkflowService();
                final int inheritedMode = service.getWorkflowMode(object);
                switch (inheritedMode) {
                    case WorkflowService.INACTIVE : type = "no-workflow";
                        break;
                    case WorkflowService.INHERITED : type = "inherited";
                        break;
                    case WorkflowService.LINKED : type = "linked";
                        break;
                    case WorkflowService.EXTERNAL : type = service.getExternalWorkflowName(object); 
                }
            } catch (JahiaException e) {
                e.printStackTrace();
            }
            initProperty(new PropertyImpl(getSession(), this, "j:type",
                                                      nodetype.getPropertyDefinitionsAsMap().get("j:type"),
                                                      null, new ValueImpl(type, PropertyType.STRING)));
        }
    }
}
