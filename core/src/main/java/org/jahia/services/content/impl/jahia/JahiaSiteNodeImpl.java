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

import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.api.Constants;

import javax.jcr.*;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 17 d�c. 2007
 * Time: 10:42:56
 * To change this template use File | Settings | File Templates.
 */
public class JahiaSiteNodeImpl extends NodeImpl {

    private JahiaSite jahiaSite;

    public JahiaSiteNodeImpl(SessionImpl session, JahiaSite jahiaSite) throws RepositoryException {
        super(session);
        setDefinition(NodeTypeRegistry.getInstance().getNodeType(Constants.JAHIANT_SYSTEM_ROOT).getDeclaredChildNodeDefinitions()[0]);
        setNodetype(NodeTypeRegistry.getInstance().getNodeType(Constants.JAHIANT_JAHIA_VIRTUALSITE));
        this.jahiaSite = jahiaSite;
    }

    @Override
    protected void initNodes() throws RepositoryException {
        if (nodes == null) {
            super.initNodes();

            ContentPage jahiaPage = jahiaSite.getHomeContentPage();

            try {
                initNode(session.getJahiaPageNode(jahiaPage));
            } catch (ItemNotFoundException e) {
                // no home page
            }
        }
    }

    @Override
    protected void initProperties() throws RepositoryException {
        if (properties == null) {
            super.initProperties();

            initProperty(new PropertyImpl(getSession(), this,
                            nodetype.getDeclaredPropertyDefinitionsAsMap().get("j:name"),
                            new ValueImpl(jahiaSite.getServerName(), PropertyType.STRING)));
        }
    }

    public String getName() throws RepositoryException {
        return jahiaSite.getSiteKey();
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return new JahiaRootNodeImpl(getSession());
    }

    @Override
    public JahiaSite getSite() {
        return jahiaSite;
    }
}
