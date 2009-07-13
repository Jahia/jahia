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
                                nodetype.getPropertyDefinition("j:link"),null,
                                new ValueImpl(page.getProperty("uuid"), PropertyType.REFERENCE)));
                        break;
                    case JahiaPage.TYPE_LINK:
                        ContentPage linked = ContentPage.getPage(page.getPageLinkID(getProcessingContext()));
                        initProperty(new PropertyImpl(getSession(),this,
                                nodetype.getPropertyDefinition("j:link"),null,
                                new ValueImpl(linked.getProperty("uuid"), PropertyType.REFERENCE)));
                        break;
                    case JahiaPage.TYPE_URL:
                        initProperty(new PropertyImpl(getSession(),this,
                                nodetype.getPropertyDefinition("j:url"),null,
                                new ValueImpl(page.getURL(getProcessingContext()), PropertyType.STRING)));
                        break;
                }
            } catch (JahiaException e) {
                throw new RepositoryException(e);
            }

        }
    }
}
