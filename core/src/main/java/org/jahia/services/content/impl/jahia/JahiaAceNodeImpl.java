/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.impl.jahia;

import org.apache.log4j.Logger;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.ExtendedNodeType;

import javax.jcr.*;
import javax.jcr.nodetype.NoSuchNodeTypeException;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 14 août 2009
 */
public class JahiaAceNodeImpl extends NodeImpl {
    private transient static Logger logger = Logger.getLogger(JahiaAceNodeImpl.class);
    private final JahiaAclNodeImpl parent;
    private final String principalName;
    private final String aceType;
    private final String privileges;

    public JahiaAceNodeImpl(SessionImpl session, JahiaAclNodeImpl parent, String principalName, String aceType, String privileges) {
        super(session);
        this.parent = parent;
        this.principalName = principalName;
        this.aceType = aceType;
        this.privileges = privileges;
        try {
            final ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType("jnt:ace");
            setDefinition(parent.nodetype.getChildNodeDefinitionsAsMap().get("*"));
            setNodetype(type);
        } catch (NoSuchNodeTypeException e) {
            logger.error(e);
        }
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return parent;
    }

    @Override
    protected void initProperties() throws RepositoryException {
        super.initProperties();
        initProperty(new PropertyImpl(getSession(), this, "j:aceType",
                                                      nodetype.getPropertyDefinitionsAsMap().get("j:aceType"),
                                                      null, new ValueImpl(aceType, PropertyType.STRING)));
        initProperty(new PropertyImpl(getSession(), this, "j:principal",
                                                      nodetype.getPropertyDefinitionsAsMap().get("j:principal"),
                                                      null, new ValueImpl(principalName, PropertyType.STRING)));
        initProperty(new PropertyImpl(getSession(), this, "j:privileges",
                                                      nodetype.getPropertyDefinitionsAsMap().get("j:privileges"),
                                                      null, new ValueImpl(privileges, PropertyType.STRING)));
        initProperty(new PropertyImpl(getSession(), this, "j:protected",
                                                      nodetype.getPropertyDefinitionsAsMap().get("j:protected"),
                                                      null, new ValueImpl(false)));
    }

    @Override
    public String getName() throws RepositoryException {
//        return "GRANT_jcr_read_g_users";
        return aceType+"_"+privileges.replace(":","_")+"_"+principalName.replace(":","_");
    }
}
