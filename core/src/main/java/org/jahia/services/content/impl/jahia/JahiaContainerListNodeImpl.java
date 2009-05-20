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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.apache.log4j.Logger;
import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentObject;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.containers.JahiaContentContainerFacade;
import org.jahia.data.events.JahiaEvent;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaContainerDefinitionsRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 24, 2008
 * Time: 6:18:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class JahiaContainerListNodeImpl extends JahiaContentNodeImpl {

    private static final transient Logger logger = Logger.getLogger(JahiaContainerListNodeImpl.class);

    private ContentContainerList contentContainerList;
    private int count = -1;

    public JahiaContainerListNodeImpl(SessionImpl session, ContentContainerList contentContainerList) throws RepositoryException {
        super(session, contentContainerList);

        try {
            JahiaContainerDefinition def = (JahiaContainerDefinition) ContentDefinition.getContentDefinitionInstance(object.getDefinitionKey(EntryLoadRequest.STAGED));
            setDefinition(def.getContainerListNodeDefinition());
            setNodetype(NodeTypeRegistry.getInstance().getNodeType(def.getContainerListNodeType()));
            this.contentContainerList = contentContainerList;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    protected void initNodes() throws RepositoryException {
        if (nodes == null ) {
            super.initNodes();

            ProcessingContext processingContext = getProcessingContext();
            try {
                List conts = contentContainerList.getChilds(processingContext.getUser(),getEntryLoadRequest());

                for (Iterator iterator = conts.iterator(); iterator.hasNext();) {
                    ContentContainer cont = (ContentContainer) iterator.next();
                    initNode(session.getJahiaContainerNode(cont));
                }
            } catch (JahiaException e) {
                throw new RepositoryException("Error loading container", e);
            }
        }
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        try {
            ContentObject contentObject = contentContainerList.getParent(getEntryLoadRequest());
            if (contentObject instanceof ContentContainer) {
                return session.getJahiaContainerNode((ContentContainer) contentObject);
            } else if (contentObject instanceof ContentPage) {
                return session.getJahiaPageNode((ContentPage) contentObject);
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Node addNode(String name, String type) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        try {
            JahiaContainerDefinition def = JahiaContainerDefinitionsRegistry.getInstance().getDefinition(contentContainerList.getDefinitionID(null));
            int pageID = contentContainerList.getPageID();

            if (!Arrays.asList(def.getNodeDefinition().getRequiredPrimaryTypes()).contains(NodeTypeRegistry.getInstance().getNodeType(type))) {
                throw new ConstraintViolationException(type);
            }

            JahiaSite site = session.getRepository().getSitesService().getSite(contentContainerList.getSiteID());
//            JahiaContainerList containerList = contentContainerList.getJahiaContainerList(getProcessingContext(), getProcessingContext().getEntryLoadRequest());
//            ContentContainerList ccl = containerList.getContentContainerList();

            JahiaContentContainerFacade contentContainerFacade = new JahiaContentContainerFacade(0,
                    contentContainerList.getSiteID(),
                    pageID,
                    contentContainerList.getID(),
                    contentContainerList.getDefinitionID(null),
                    0,
                    getProcessingContext(),
                    site.getLanguageSettingsAsLocales (false));
            int parentAclID = contentContainerList.getAclID();
            JahiaContainer container = contentContainerFacade.getContainer(EntryLoadRequest.STAGED,true);

            //todo : delay saveContainerInfo to save method
            ServicesRegistry.getInstance ().getJahiaContainersService ().
                    saveContainerInfo (container, 0, parentAclID, getProcessingContext());

            JahiaEvent theEvent = new JahiaEvent(this, getProcessingContext(), container);
            ServicesRegistry.getInstance().getJahiaEventService().fireAddContainer(theEvent);

            ContentContainer cc = container.getContentContainer();
            cc.setProperty("containerKey", name);
            JahiaContainerNodeImpl res = new JahiaContainerNodeImpl(getSession(), cc);
            res.setNew();
            initNodes();
            initNode(res);
            return res;
        } catch (JahiaException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public ExtendedNodeType[] getMixinNodeTypes() throws RepositoryException {
        try {
            JahiaContainerDefinition def = (JahiaContainerDefinition) ContentDefinition.getContentDefinitionInstance(object.getDefinitionKey(EntryLoadRequest.STAGED));
            List<ExtendedNodeType> list = def.getContainerListMixinNodeTypes();
            return list.toArray(new ExtendedNodeType[list.size()]);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
