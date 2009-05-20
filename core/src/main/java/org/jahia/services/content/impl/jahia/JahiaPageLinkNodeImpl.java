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

import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.exceptions.JahiaException;
import org.jahia.api.Constants;

import javax.jcr.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 16, 2008
 * Time: 1:43:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPageLinkNodeImpl extends NodeImpl {


    private Node node;
    private ContentPage page;

    public JahiaPageLinkNodeImpl(SessionImpl session, Node node, ExtendedNodeDefinition def, ContentPage page) throws RepositoryException {
        super(session);
        this.node = node;
        this.page = page;

        setDefinition(def);

        switch (page.getPageType(getEntryLoadRequest())) {
            case JahiaPage.TYPE_DIRECT:
                setNodetype(NodeTypeRegistry.getInstance().getNodeType(Constants.JAHIANT_DIRECT_PAGE_LINK));
                break;
            case JahiaPage.TYPE_LINK:
                setNodetype(NodeTypeRegistry.getInstance().getNodeType(Constants.JAHIANT_INTERNAL_PAGE_LINK));
                break;
            case JahiaPage.TYPE_URL:
                setNodetype(NodeTypeRegistry.getInstance().getNodeType(Constants.JAHIANT_EXTERNAL_PAGE_LINK));
                break;
        }
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return node;
    }

    @Override
    protected void initProperties() throws RepositoryException {
        if (properties == null) {
            super.initProperties();

            try {
                switch (page.getPageType(getEntryLoadRequest())) {
                    case JahiaPage.TYPE_DIRECT:
                        initProperty(new PropertyImpl(getSession(),this,
                                nodetype.getPropertyDefinition("j:link"),
                                new ValueImpl(page.getProperty("uuid"), PropertyType.REFERENCE)));
                        break;
                    case JahiaPage.TYPE_LINK:
                        ContentPage linked = ContentPage.getPage(page.getPageLinkID(getProcessingContext()));
                        initProperty(new PropertyImpl(getSession(),this,
                                nodetype.getPropertyDefinition("j:link"),
                                new ValueImpl(linked.getProperty("uuid"), PropertyType.REFERENCE)));
                        break;
                    case JahiaPage.TYPE_URL:
                        initProperty(new PropertyImpl(getSession(),this,
                                nodetype.getPropertyDefinition("j:url"),
                                new ValueImpl(page.getURL(getProcessingContext()), PropertyType.STRING)));
                        break;
                }
            } catch (JahiaException e) {
                throw new RepositoryException(e);
            }

        }
    }
}
