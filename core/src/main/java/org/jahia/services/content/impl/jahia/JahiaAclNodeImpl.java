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

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.jahia.exceptions.JahiaException;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;

/**
 * Created by IntelliJ IDEA.
  * User: toto
  * Date: Jul 3, 2008
  * Time: 11:22:59 AM
  * To change this template use File | Settings | File Templates.
  */
 public class JahiaAclNodeImpl extends NodeImpl {
    private int aclId;
    private NodeImpl parent;

    public JahiaAclNodeImpl(SessionImpl session, int aclId, NodeImpl parent) throws RepositoryException {
        super(session);
        setDefinition(NodeTypeRegistry.getInstance().getNodeType("jmix:accessControlled").getChildNodeDefinitionsAsMap().get("j:acl"));
        setNodetype(NodeTypeRegistry.getInstance().getNodeType("jnt:acl"));
        this.aclId = aclId;
        this.parent = parent;
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return parent;
    }

    @Override
    protected void initNodes() throws RepositoryException {
        super.initNodes();
        // todo ace
//        try {
//            JahiaBaseACL acl = new JahiaBaseACL(aclId);
//            Collection c = acl.getACL().getEntries();
//            for (Iterator iterator = c.iterator(); iterator.hasNext();) {
//                JahiaAclEntry jahiaAclEntry = (JahiaAclEntry) iterator.next();
//            }
//        } catch (JahiaException e) {
//
//        }
    }


    @Override
    protected void initProperties() throws RepositoryException {
        if (properties == null) {
            super.initProperties();
            try {
                JahiaBaseACL acl = new JahiaBaseACL(aclId);
                boolean inherit = acl.getInheritance()==1;

                initProperty(new PropertyImpl(getSession(), this,
                        nodetype.getDeclaredPropertyDefinitionsAsMap().get("j:inherit"),
                        new ValueImpl(Boolean.toString(inherit),PropertyType.BOOLEAN)));

            } catch (JahiaException e) {
                throw new RepositoryException(e);
            }

        }

    }
}
