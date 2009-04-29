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
