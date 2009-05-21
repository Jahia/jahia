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
//
package org.jahia.services.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.aopalliance.intercept.Interceptor;
import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentFieldKey;
import org.jahia.content.ObjectKey;
import org.jahia.data.events.JahiaEventListenerInterface;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.fields.JahiaFieldSubDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaFieldsDataManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.JahiaFieldDefinitionsRegistry;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.springframework.context.ApplicationContext;

/**
 * Metadata Service
 *
 * @author Khue Nguyen
 */
public class MetadataBaseService extends MetadataService {

    static private String GET_METADATA_FIELDS_BY_NAME = "SELECT DISTINCT a.comp_id.id FROM JahiaFieldsData a, JahiaFieldsDef b WHERE b.isMetadata=1 AND a.fieldDefinition.id=b.id AND b.name=(:fieldDefName)";
    static private String GET_METADATA_FIELDS_BY_NAME_AND_BY_SITEID = "SELECT DISTINCT a.comp_id.id FROM JahiaFieldsData a, JahiaFieldsDef b WHERE b.isMetadata=1 AND a.siteId=(:siteId) AND a.fieldDefinition.id=b.id AND b.name=(:fieldDefName)";

    static private MetadataBaseService instance = null;

    private Map<String, JahiaFieldDefinition> contentDefinitions = new HashMap<String, JahiaFieldDefinition>();

    private JahiaEventListenerInterface metadataEventListener;
    private JahiaFieldDefinitionsRegistry fieldDefinitionsRegistry;
    public static final String METADATA_TYPE = "jmix:contentmetadata";

    protected MetadataBaseService() {
    }

    public static MetadataBaseService getInstance() {
        if (instance == null) {
            instance = new MetadataBaseService();
        }
        return instance;
    }


    public void start()
    throws JahiaInitializationException {

        try {
            reloadConfigurationFile();
        } catch ( Exception t ){
            throw new  JahiaInitializationException("Error loading initializing service", t);
        }
    }

    public void stop() {}

    public void setFieldDefinitionsRegistry(JahiaFieldDefinitionsRegistry fieldDefinitionsRegistry) {
        this.fieldDefinitionsRegistry = fieldDefinitionsRegistry;
    }

    /**
     * Reload configuration file from disk
     *
     * @throws JahiaException
     */
    public void reloadConfigurationFile() throws java.io.FileNotFoundException,
    JahiaException {

        this.contentDefinitions.clear();

        List<ExtendedNodeType> allTypes = new ArrayList<ExtendedNodeType>();

        ExtendedNodeType metadataType = null;
        try {
            metadataType = NodeTypeRegistry.getInstance().getNodeType(METADATA_TYPE);
        } catch (NoSuchNodeTypeException e) {
            e.printStackTrace();
            return;
        }

        for (ExtendedNodeType nodeType : metadataType.getSupertypes()) {
            allTypes.add(nodeType);
        }
        allTypes.add(metadataType);
        for (ExtendedNodeType type : metadataType.getMixinSubtypes()) {
            allTypes.add(type);
        }

        for (ExtendedNodeType type : allTypes) {
            ExtendedPropertyDefinition[] propDefs = type.getDeclaredPropertyDefinitions();
            for (ExtendedPropertyDefinition propDef : propDefs) {
                String name = propDef.getLocalName();
                JahiaFieldDefinition fieldDef = fieldDefinitionsRegistry.getDefinition(0, name);

                if (fieldDef == null) {
                    // have to create it
                    fieldDef = new JahiaFieldDefinition(0, 0, name, new HashMap<Integer, JahiaFieldSubDefinition>());
                }
                fieldDef.setCtnType(type.getName() + " " + propDef.getName());
                fieldDef.setIsMetadata(true);

                fieldDefinitionsRegistry.setDefinition(fieldDef);

                // register content definition mappings
                if (fieldDef != null) {
                    this.contentDefinitions.put(fieldDef.getName(), fieldDef);
                }
            }
        }
    }

    /**
     * Return the JahiaEventListener used to handle Metadata
     *
     */
    public JahiaEventListenerInterface getMetadataEventListener(){
        return metadataEventListener;
    }

    public void setMetadataEventListener(JahiaEventListenerInterface metadataEventListener) {
        this.metadataEventListener = metadataEventListener;
    }

    /**
     * Add an aopalliance interceptor to the JahiaEventListener
     *
     */
    public void addAOPInterceptor(Interceptor interceptor){
        if ( interceptor == null ){
            return;
        }
//        Advised eventListenerBean = (Advised) metadataEventListener;
        /*if ( eventListenerBean != null ){
            eventListenerBean.addInterceptor(interceptor);
        }*/
    }

    /**
     * Returns an array list of metadata that match this contentDefinition
     *
     * @param contentDefinition ContentDefinition
     * @return boolean
     */
    public List<ObjectKey> getMatchingMetadatas(ContentDefinition contentDefinition){
        List<ObjectKey> result = new ArrayList<ObjectKey>();
        if (contentDefinition == null) {
            return result;
        }
        String type = contentDefinition.getPrimaryType();
        ExtendedNodeType metadataType = null;
        if (type != null) {
            try {
                ExtendedNodeType ent = NodeTypeRegistry.getInstance().getNodeType(type);
                ExtendedNodeType[] st = ent.getSupertypes();
                for (ExtendedNodeType aSt : st) {
                    if (aSt.isNodeType(METADATA_TYPE) && aSt.isMixin()) {
                        metadataType = aSt;
                        break;
                    }
                }
            } catch (NoSuchNodeTypeException e) {
                e.printStackTrace();
            }
        } else {
            try {
                metadataType = NodeTypeRegistry.getInstance().getNodeType(METADATA_TYPE);
            } catch (NoSuchNodeTypeException e) {
                e.printStackTrace();
            }
        }
        if (metadataType != null) {
            ExtendedPropertyDefinition[] propDef = metadataType.getPropertyDefinitions();
            for (ExtendedPropertyDefinition aPropDef : propDef) {
                JahiaFieldDefinition f = contentDefinitions.get(aPropDef.getLocalName());
                result.add(f.getObjectKey());
            }
        }
        return result;
    }

    /**
     * Returns true if the given field definition is declared in metadata config file
     * @param name the metadata name ( jahia field definition name )
     * @return
     */
    public boolean isDeclaredMetadata(String name){
        return ( this.contentDefinitions.get(name) != null );
    }

    /**
     * Create all metadata associations between a ContentDefinition and
     * registered Metadata Definitions
     *
     * @param contentDefinition ContentDefinition
     * @throws JahiaException
     */
    public void assignMetadataToContentDefinition(ContentDefinition contentDefinition)
    throws JahiaException {
    }

    /**
     * Returns an array of ObjectKey that are metadatas
     *
     * @param name String, the metadata name
     * @throws JahiaException
     * @return ArrayList
     */
    public List<ObjectKey> getMetadataByName(String name)
    throws JahiaException {
        return getMetadataByName(name,-1);
    }

    /**
     * Returns an array of ObjectKey that are metadatas of a given site
     *
     * @param name String, the metadata name
     * @param siteId, the site id
     * @throws JahiaException
     * @return ArrayList
     * @throws JahiaException
     */
    public List<ObjectKey> getMetadataByName(String name, int siteId)
    throws JahiaException {
        List<ObjectKey> metadatas = new ArrayList<ObjectKey>();

        if ( name == null ){
            return metadatas;
        }
        try {
            ApplicationContext context = SpringContextSingleton.getInstance()
                    .getContext();
            JahiaFieldsDataManager fieldMgr = (JahiaFieldsDataManager) context
                    .getBean(JahiaFieldsDataManager.class.getName());
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("fieldDefName", name);

            String stmt = null;
            if (siteId > 0) {
                stmt = GET_METADATA_FIELDS_BY_NAME_AND_BY_SITEID;
                parameters.put("siteId", siteId);
            } else {
                stmt = GET_METADATA_FIELDS_BY_NAME;
            }
            List<Integer> queryResult = fieldMgr.<Integer>executeQuery(stmt, parameters);

            for (int fieldId : queryResult) {
                ContentFieldKey key = new ContentFieldKey(fieldId);
                metadatas.add(key);
            }
        } catch (Exception se) {
            throw new JahiaException("Cannot load metadatas with name " + name
                    + "from the database", se.getMessage(),
                    JahiaException.DATABASE_ERROR,
                    JahiaException.ERROR_SEVERITY, se);
        }
        return metadatas;
    }
}

