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
                        if (!p.isDeleted(Integer.MAX_VALUE) && (session.getWorkspace().getWorkflowState() >1  || p.hasActiveEntries())) {
                            initNode(session.getJahiaPageNode(p));
                        }
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
            ProcessingContext processingContext = getProcessingContext();
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
