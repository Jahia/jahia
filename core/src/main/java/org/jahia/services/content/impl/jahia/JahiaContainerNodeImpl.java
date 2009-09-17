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

import org.apache.log4j.Logger;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.containers.JahiaContainerStructure;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.fields.ContentField;
import org.jahia.services.content.nodetypes.*;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.api.Constants;
import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentObject;

import javax.jcr.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 17 d�c. 2007
 * Time: 16:00:02
 * To change this template use File | Settings | File Templates.
 */
public class JahiaContainerNodeImpl extends JahiaContentNodeImpl {
    
    private static final transient Logger logger = Logger.getLogger(JahiaContainerNodeImpl.class);

    private ContentContainer contentContainer;
//    private int count = -1;

    public JahiaContainerNodeImpl(SessionImpl session, ContentContainer contentContainer) throws RepositoryException {
        super(session, contentContainer);

        try {
            JahiaContainerDefinition def = (JahiaContainerDefinition) ContentDefinition.getContentDefinitionInstance(object.getDefinitionKey(EntryLoadRequest.STAGED));
            setDefinition(def.getNodeDefinition());
            setNodetype(NodeTypeRegistry.getInstance().getNodeType(def.getContainerType()));
            this.contentContainer = contentContainer;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    public String getName() throws RepositoryException {
        return getName(contentContainer);
    }

    public static String getName(ContentContainer contentContainer) {
        try {
            String s = contentContainer.getProperty("containerKey");
            if (s != null) {
                return s;
            }
        } catch (JahiaException e) {
            e.printStackTrace();
        }
//        return contentContainer.getObjectKey().toString();
        return "ctn"+ contentContainer.getID();
    }

    protected void initFields() throws RepositoryException {
        if (fields == null) {
            super.initFields();

            try {
                List<? extends ContentObject> childs = contentContainer.getChilds(null, getEntryLoadRequest(), JahiaContainerStructure.JAHIA_FIELD);

                for (Iterator<? extends ContentObject> iterator = childs.iterator(); iterator.hasNext();) {
                    ContentField contentField = (ContentField) iterator.next();
                    if (contentField != null) {
                        initFieldItem(contentField);
                    }
                }
            } catch (JahiaException e) {
                throw new RepositoryException(e);
            }
        }
    }

    protected void initNodes() throws RepositoryException {
        if (nodes == null ) {
            super.initNodes();
            ProcessingContext processingContext = getProcessingContext();
            final List<Integer> containerListIDs = ServicesRegistry.getInstance().getJahiaContainersService().getSubContainerListIDs(contentContainer.getID(), getEntryLoadRequest());
            try {
                for (final Integer curContainerListID : containerListIDs) {
                    ContentContainerList l = ContentContainerList.getContainerList(curContainerListID);
                    initNode(session.getJahiaContainerListNode(l));
                }
            } catch (JahiaException e) {
                throw new RepositoryException("Error loading container", e);
            }
        }
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        try {       
            ProcessingContext processingContext = getProcessingContext();
            return session.getJahiaContainerListNode((ContentContainerList) contentContainer.getParent(getEntryLoadRequest()));
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public ExtendedNodeType getPrimaryNodeType() throws RepositoryException {
        return NodeTypeRegistry.getInstance().getNodeType(getProperty(Constants.JCR_PRIMARYTYPE).getString());
    }

    public ExtendedNodeType[] getMixinNodeTypes() throws RepositoryException {
        try {
            JahiaContainerDefinition def = (JahiaContainerDefinition) ContentDefinition.getContentDefinitionInstance(object.getDefinitionKey(EntryLoadRequest.STAGED));
            List<ExtendedNodeType> list = new ArrayList<ExtendedNodeType>(mixin);
            list.addAll(def.getMixinNodeTypes());
            return list.toArray(new ExtendedNodeType[list.size()]);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


}
