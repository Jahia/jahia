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

import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.fields.ContentField;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.content.ContentObject;

import javax.jcr.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 9, 2009
 * Time: 1:34:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowStateNodeImpl extends NodeImpl {
    private JahiaContentNodeImpl parent;

    protected ContentObject object;

    public WorkflowStateNodeImpl(SessionImpl session, JahiaContentNodeImpl parent) throws RepositoryException {
        super(session);
        setDefinition(NodeTypeRegistry.getInstance().getNodeType("jmix:i18n").getChildNodeDefinitionsAsMap().get("j:translation"));
        setNodetype(NodeTypeRegistry.getInstance().getNodeType("jnt:translation"));
        this.parent = parent;
        this.object = parent.getContentObject();
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return parent;
    }

    @Override
    protected void initNodes() throws RepositoryException {
        super.initNodes();
    }

    @Override
    protected void initProperties() throws RepositoryException {
        super.initProperties();
    }

}
