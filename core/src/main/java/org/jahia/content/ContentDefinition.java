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
package org.jahia.content;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaLinkManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.metadata.MetadataBaseService;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Abstract implementation for Content Definition
 *
 * @author Khue Nguyen
 */
public abstract class ContentDefinition extends JahiaObject implements Serializable {

    private static final long serialVersionUID = 6086558470748000648L;
    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ContentDefinition.class);
    private static JahiaLinkManager linkManager;

    protected ContentDefinition(ObjectKey objectKey) {
        super(objectKey);
    }

    /**
     * No arg constructor required for serialization support.
     */
    protected ContentDefinition() {
    }

    /**
     * This method is reserved for class that derive from this one so that
     * they can update their object key notably when they get an ID assigned
     * from the database when the object is first created.
     * @param objectKey the new object key to set.
     */
    protected void setObjectKey(ObjectKey objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * Instance generator. Build an instance of the appropriate
     * class corresponding to the DefinitionKey passed described.
     *
     * @param objectKey an ContentDefinitionKey instance for the Content Definition we want to retrieve
     * an instance of.
     * @returns a ContentDefinition sub class instance that corresponds to the given
     * object key.
     *
     * @param objectKey , a ContentDefinitionKey subclass
     * @param loadRequest
     * @return
     * @throws ClassNotFoundException
     */
    public static ContentDefinition getContentDefinitionInstance (ObjectKey objectKey)
    throws ClassNotFoundException {
        return (ContentDefinition)getInstanceAsObject(objectKey);
    }

    /**
     * Return the human readable title of this Content Definition for Content Object using this Definition
     *
     * @param contentObject, the contextual Content Object
     *
     * @param contentObject
     * @param entryState
     * @return
     */
    public abstract String getTitle(ContentObject contentObject,
                                    ContentObjectEntryState entryState)
        throws ClassNotFoundException;

    /**
     * @return a String containing the name of the definition, which must be
     * unique in a site.
     */
    public abstract String getName();

    /**
     * Return the human readable title of this Content Definition for Content Object using this Definition
     *
     * @param contentObject, the contextual Content Object
     *
     * @param contentObject
     * @param entryState
     * @return
     */
    public static String getObjectTitle(ContentObject contentObject,
                                  ContentObjectEntryState entryState)
    throws ClassNotFoundException{

        String title = "";
        try {
            ContentDefinition definition =
                       ContentDefinition.getContentDefinitionInstance(
                       contentObject.getDefinitionKey(new EntryLoadRequest(entryState)));
           title = definition.getTitle(contentObject,entryState);
        } catch ( Exception t ){

        }
        return title;
    }

    /**
     * We use this method for lazy retrieval of the linkManager, otherwise
     * we run into chicken and egg problems upon Spring startup.
     * @return
     */
    private static JahiaLinkManager getLinkManager() {
        if (linkManager == null) {
            linkManager = (JahiaLinkManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaLinkManager.class.getName());
        }
        return linkManager;
    }

    /**
     * Return an Array of JahiaObject that have a core "metadata" strutural relationship
     * with this one. @see StructuralRelationship.METADATADEFINITION_LINK
     *
     * @return List of JahiaObject
     */
    public List<? extends JahiaObject> getMetadataDefinitions() throws JahiaException {
        return getMetadataDefinitions(this);
    }

    /**
     * Return an Array of JahiaObject that have a core "metadata" strutural relationship
     * with the given objectKey. @see StructuralRelationship.METADATADEFINITION_LINK
     *
     * @param objectKey ObjectKey
     * @throws JahiaException
     * @return List of JahiaObject
     */
    public static List<? extends JahiaObject> getMetadataDefinitions(ContentDefinition objectKey) throws JahiaException {
        List<JahiaObject> objects = new ArrayList<JahiaObject>();
        List<ObjectKey> fields = MetadataBaseService.getInstance().getMatchingMetadatas(objectKey);
        for (Iterator<ObjectKey> iterator = fields.iterator(); iterator.hasNext();) {
            ContentDefinitionKey contentDefinitionKey = (ContentDefinitionKey) iterator.next();
            try {
                JahiaObject jahiaObject = ContentObject.getInstance(contentDefinitionKey);
                objects.add(jahiaObject);
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return objects;
    }

    /**
     * Return the given JahiaObject that has a core "metadata" strutural relationship
     * with this one and having the given name.
     * @see StructuralRelationship.METADATADEFINITION_LINK
     *
     * @return a JahiaObject
     */
    public JahiaObject getMetadataDefinition(String name) throws JahiaException {
        JahiaObject jahiaObject = null;
        List<ObjectLink> links = getLinkManager().findByTypeAndRightObjectKey(
           StructuralRelationship.METADATADEFINITION_LINK,this.getObjectKey());
        for ( int i=0 ; i<links.size(); i++ ){
            ObjectLink link = (ObjectLink)links.get(i);
            try {
                ContentDefinition contentDefinition = ContentDefinition.
                    getContentDefinitionInstance(link.getLeftObjectKey());
                if (contentDefinition != null
                    && contentDefinition.equals(name)) {
                    return contentDefinition;
                }
            } catch ( ClassNotFoundException cnf ){
                logger.debug(cnf);
            }
        }
        return jahiaObject;
    }

    /**
     * Delete a "metadatadefinition" strutural relationship with this one
     *
     * @param metadataDefinition JahiaObject
     * @throws JahiaException
     */
    public void removeMetadataDefinition(JahiaObject metadataDefinition)
    throws JahiaException {
        removeMetadataDefinition(this,metadataDefinition);
    }

    /**
     * Delete a "metadatadefinition" strutural relationship with this one
     *
     * @param metadataDefinition JahiaObject
     * @throws JahiaException
     */
    public void removeMetadataDefinition(JahiaObject object,
                                         JahiaObject metadataDefinition)
    throws JahiaException {
       List<ObjectLink> links = getLinkManager().findByTypeAndRightObjectKey(
       StructuralRelationship.METADATADEFINITION_LINK,object.getObjectKey());
       for (int i = 0; i < links.size(); i++) {
           ObjectLink link = (ObjectLink) links.get(i);
           if ( link.getLeftObjectKey().equals(metadataDefinition.getObjectKey()) ){
               link.remove();
               break;
           }
       }
    }

    public ExtendedNodeType getNodeType() {
        try {
            return NodeTypeRegistry.getInstance().getNodeType(getPrimaryType());
        } catch (NoSuchNodeTypeException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public String[] getAliasNames(){
        return new String[0];
    }

    public String getPrimaryType() {
        return null;
    }
}
