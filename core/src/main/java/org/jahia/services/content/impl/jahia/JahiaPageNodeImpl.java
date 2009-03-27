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

import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.PageProperty;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 17 d�c. 2007
 * Time: 14:58:44
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPageNodeImpl extends JahiaContentNodeImpl {
    
    private static final transient Logger logger = Logger.getLogger(JahiaPageNodeImpl.class);

    private ContentPage contentPage;

    public JahiaPageNodeImpl(SessionImpl session, ContentPage contentPage) throws RepositoryException {
        super(session, contentPage);

        // todo not same definition if rootpage
        setDefinition(NodeTypeRegistry.getInstance().getNodeType(Constants.JAHIANT_PAGE).getDeclaredChildNodeDefinitionsAsMap().get("*"));
        JahiaPageDefinition template = contentPage.getPageTemplate(getProcessingContext());
        if (template == null ) {
            template = contentPage.getPageTemplate(EntryLoadRequest.STAGED);
        }
        setNodetype(NodeTypeRegistry.getInstance().getNodeType(template.getPageType()));

        this.contentPage = contentPage;
    }

    protected void initNodes() throws RepositoryException {
        if (nodes == null) {
            super.initNodes();

            JahiaContainersService jahiaContainersService = ServicesRegistry.getInstance().getJahiaContainersService();
            ProcessingContext processingContext = getProcessingContext();
            try {
                // containers
                final Set<Integer> containerListIDs = jahiaContainersService.
                        getAllPageTopLevelContainerListIDs(
                                contentPage.getID(), getEntryLoadRequest());
                for (final Integer curContainerListID : containerListIDs) {
                    try {
                        ContentContainerList l = ContentContainerList.getContainerList(curContainerListID);
                        initNode(session.getJahiaContainerListNode(l));
                    } catch (RepositoryException e) {
                        logger.error("Cannot get container",e);
                    }
                }

                // subpages
                for (ContentPage p : ServicesRegistry.getInstance().getJahiaPageService().getContentPageChilds(
                        contentPage.getID(), processingContext.getUser(), LoadFlags.ALL, null, true)) {
                    try {
                        initNode(session.getJahiaPageNode(p));
                    } catch (RepositoryException e) {
                        logger.error("Cannot get page",e);
                    }
                }
            } catch (Exception t) {
                throw new RepositoryException("Error loading page ("+ contentPage.getID()+")top level container lists : ", t);
            }
        }
    }

    @Override
    protected void initProperties() throws RepositoryException {
        if (properties == null) {
            super.initProperties();

            ExtendedNodeType pageType = NodeTypeRegistry.getInstance().getNodeType(Constants.JAHIANT_PAGE);

            initProperty(new PropertyImpl(getSession(),this,
                    pageType.getPropertyDefinition("j:pageTitle"),
                    new ValueImpl(contentPage.getTitle(getProcessingContext()), PropertyType.STRING)));

            initProperty(new PropertyImpl(getSession(),this,
                    pageType.getPropertyDefinition("j:template"),
                    new ValueImpl(contentPage.getPageTemplate(getProcessingContext()).getName(), PropertyType.STRING)));

            initProperty(new PropertyImpl(getSession(),this,
                    pageType.getPropertyDefinition("j:pid"),
                    new ValueImpl(""+contentPage.getID(), PropertyType.LONG)));

        }
    }

    public String getName() throws RepositoryException {
        try {
            String s = contentPage.getProperty(PageProperty.PAGE_URL_KEY_PROPNAME);
            if (s != null) {
                return s;
            }
        } catch (JahiaException e) {
            e.printStackTrace();
        }
        return contentPage.getObjectKey().toString();
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        try {
            int i = contentPage.getParentID(getEntryLoadRequest());
            if (i>0) {
                ContentPage page = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(i, true);
                return session.getJahiaPageNode(page);
            } else {
                return session.getJahiaSiteNode(session.getRepository().getSitesService().getSite(contentPage.getJahiaID()));
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
