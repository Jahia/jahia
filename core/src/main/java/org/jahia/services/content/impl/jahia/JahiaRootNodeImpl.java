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

import java.util.Iterator;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.sites.JahiaSite;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 17 d�c. 2007
 * Time: 10:35:25
 * To change this template use File | Settings | File Templates.
 */
public class JahiaRootNodeImpl extends NodeImpl {

    public JahiaRootNodeImpl(SessionImpl session) throws RepositoryException {
        super(session);
        setNodetype(NodeTypeRegistry.getInstance().getNodeType(Constants.JAHIANT_SYSTEM_ROOT));
    }

    @Override
    protected void initNodes() throws RepositoryException {
        if (nodes == null) {
            super.initNodes();
            try {
            Iterator siteEnum = session.getRepository().getSitesService().getSites();
                while (siteEnum.hasNext()) {
                    JahiaSite curJahiaSite = (JahiaSite) siteEnum.next();

                    initNode(new JahiaSiteNodeImpl(getSession(), curJahiaSite));
                }
            } catch (JahiaException e) {
                throw new RepositoryException("Error retrieving sites", e);
            }
        }
    }

    public String getName() throws RepositoryException {
        return "root";
    }

    @Override
    public String getPath() throws RepositoryException {
        return "/";
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        throw new ItemNotFoundException();
    }

    @Override
    public JahiaSite getSite() {
        return null;
    }
}
