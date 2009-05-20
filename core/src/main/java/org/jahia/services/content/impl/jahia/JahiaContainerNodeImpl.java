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
        return contentContainer.getObjectKey().toString();
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
            List<ExtendedNodeType> list = def.getMixinNodeTypes();
            return list.toArray(new ExtendedNodeType[list.size()]);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
