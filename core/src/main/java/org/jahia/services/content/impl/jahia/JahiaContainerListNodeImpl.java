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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
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
