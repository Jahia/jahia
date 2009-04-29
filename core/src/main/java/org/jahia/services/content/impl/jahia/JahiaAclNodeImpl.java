/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
        setDefinition(parent.getPrimaryNodeType().getChildNodeDefinitionsAsMap().get("j:acl"));
        setNodetype(NodeTypeRegistry.getInstance().getNodeType("jnt:acl"));
        this.aclId = aclId;
        this.parent = parent;
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return parent;
    }

    @Override
    protected void initNodes() throws RepositoryException {
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
