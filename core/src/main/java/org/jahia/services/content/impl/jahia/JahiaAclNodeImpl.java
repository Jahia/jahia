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

import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.api.Constants;

import javax.jcr.*;
import java.util.Collection;

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
        try {
            JahiaBaseACL acl = new JahiaBaseACL(aclId);
            Collection<JahiaAclEntry> c = acl.getACL().getEntries();
            for (JahiaAclEntry e : c) {
                if (e != null) {
                    String typeOfPrincipal = e.getComp_id().getType()==1?"u":"g";
                    String principalName;
                    if("u".equals(typeOfPrincipal)) {
                        principalName = typeOfPrincipal +":"+ e.getComp_id().getTarget().split("\\}")[1];
                    } else {
                        principalName = typeOfPrincipal +":"+ e.getComp_id().getTarget().split(":")[0];
                    }
                    if (e.getPermission(JahiaBaseACL.READ_RIGHTS) == JahiaAclEntry.ACL_YES) {
                        initNode(new JahiaAceNodeImpl(getSession(),this,principalName,Constants.GRANT, Constants.JCR_READ_RIGHTS_LIVE));
                        initNode(new JahiaAceNodeImpl(getSession(),this,principalName,Constants.GRANT, Constants.JCR_READ_RIGHTS));
                    }
                    if (e.getPermission(JahiaBaseACL.READ_RIGHTS) == JahiaAclEntry.ACL_NO) {
                        initNode(new JahiaAceNodeImpl(getSession(),this,principalName,Constants.DENY, Constants.JCR_READ_RIGHTS_LIVE));
                        initNode(new JahiaAceNodeImpl(getSession(),this,principalName,Constants.DENY, Constants.JCR_READ_RIGHTS));
                    }
                    if (e.getPermission(JahiaBaseACL.WRITE_RIGHTS) == JahiaAclEntry.ACL_YES) {
                        initNode(new JahiaAceNodeImpl(getSession(),this,principalName,Constants.GRANT, Constants.JCR_WRITE_RIGHTS));
                    }
                    if (e.getPermission(JahiaBaseACL.WRITE_RIGHTS) == JahiaAclEntry.ACL_NO) {
                        initNode(new JahiaAceNodeImpl(getSession(),this,principalName,Constants.DENY, Constants.JCR_WRITE_RIGHTS));
                    }
                    if (e.getPermission(JahiaBaseACL.ADMIN_RIGHTS) == JahiaAclEntry.ACL_YES) {
                        initNode(new JahiaAceNodeImpl(getSession(),this,principalName,Constants.GRANT, Constants.JCR_MODIFYACCESSCONTROL_RIGHTS));
                        initNode(new JahiaAceNodeImpl(getSession(),this,principalName,Constants.GRANT, Constants.JCR_WRITE_RIGHTS_LIVE));
                    }
                    if (e.getPermission(JahiaBaseACL.ADMIN_RIGHTS) == JahiaAclEntry.ACL_NO) {
                        initNode(new JahiaAceNodeImpl(getSession(),this,principalName,Constants.DENY, Constants.JCR_MODIFYACCESSCONTROL_RIGHTS));
                        initNode(new JahiaAceNodeImpl(getSession(),this,principalName,Constants.DENY, Constants.JCR_WRITE_RIGHTS_LIVE));
                    }
                }
            }
        } catch (JahiaException e) {

        }
    }


    @Override
    protected void initProperties() throws RepositoryException {
        if (properties == null) {
            super.initProperties();
            try {
                JahiaBaseACL acl = new JahiaBaseACL(aclId);
                boolean inherit = acl.getInheritance() == 0;

                initProperty(new PropertyImpl(getSession(), this,
                                              nodetype.getDeclaredPropertyDefinitionsAsMap().get("j:inherit"), null,
                                              new ValueImpl(inherit)));

            } catch (JahiaException e) {
                throw new RepositoryException(e);
            }

        }

    }
}
