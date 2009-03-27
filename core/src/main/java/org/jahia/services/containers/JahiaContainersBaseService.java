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

package org.jahia.services.containers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Constraint;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Literal;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.PropertyValue;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModel;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModelFactory;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentContainerListKey;
import org.jahia.content.ContentFieldKey;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.ContentPageKey;
import org.jahia.content.ObjectKey;
import org.jahia.content.events.ContentObjectDeleteEvent;
import org.jahia.data.JahiaDOMObject;
import org.jahia.data.containers.ContainersChangeEventListener;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainerSet;
import org.jahia.data.containers.JahiaContainerStructure;
import org.jahia.data.containers.JahiaContainerSubDefinition;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.fields.JahiaFieldSubDefinition;
import org.jahia.data.fields.LoadFlags;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaAclManager;
import org.jahia.hibernate.manager.JahiaContainerDefinitionManager;
import org.jahia.hibernate.manager.JahiaContainerListManager;
import org.jahia.hibernate.manager.JahiaContainerManager;
import org.jahia.hibernate.manager.JahiaContainerStructureManager;
import org.jahia.hibernate.manager.JahiaFieldsDataManager;
import org.jahia.hibernate.manager.JahiaObjectManager;
import org.jahia.params.ProcessingContext;
import org.jahia.query.filtercreator.FilterCreator;
import org.jahia.query.qom.ChildNodeImpl;
import org.jahia.query.qom.ConstraintItem;
import org.jahia.query.qom.ContainerQueryBuilder;
import org.jahia.query.qom.DescendantNodeImpl;
import org.jahia.query.qom.JahiaQueryObjectModelConstants;
import org.jahia.query.qom.QueryObjectModelImpl;
import org.jahia.registries.JahiaContainerDefinitionsRegistry;
import org.jahia.registries.JahiaFieldDefinitionsRegistry;
import org.jahia.registries.JahiaListenersRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.ACLNotFoundException;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.content.nodetypes.ExtendedItemDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.SelectorType;
import org.jahia.services.fields.ContentField;
import org.jahia.services.metadata.MetadataBaseService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.pages.JahiaPageTemplateService;
import org.jahia.services.search.indexingscheduler.RuleEvaluationContext;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.version.StateModificationContext;
import org.jahia.services.workflow.WorkflowEvent;
import org.jahia.utils.JahiaTools;

public class JahiaContainersBaseService extends JahiaContainersService {

    /**
     * logging
     */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaContainersBaseService.class);

    /**
     * the service unique instance
     */
    private static JahiaContainersBaseService instance;

    // the Container cache name.
    public static final String CONTAINER_CACHE = "ContainerCache";

    // the Container List cache name.
    public static final String CONTAINERLIST_CACHE = "ContainerListCache";

    private JahiaContainerListManager containerListManager;

    private JahiaContainerManager containerManager;
    private JahiaAclManager aclManager;
    private JahiaContainerStructureManager containerStructureManager;
    private JahiaContainerDefinitionManager containerDefinitionManager;
    private JahiaFieldsDataManager fieldsDataManager;
    private JahiaPageTemplateService tplService;

    private JahiaObjectManager jahiaObjectManager;

    public void setContainerListManager(JahiaContainerListManager containerListManager) {
        this.containerListManager = containerListManager;
    }

    public void setContainerManager(JahiaContainerManager containerManager) {
        this.containerManager = containerManager;
    }

    public void setAclManager(JahiaAclManager aclManager) {
        this.aclManager = aclManager;
    }

    public void setContainerStructureManager(JahiaContainerStructureManager containerStructureManager) {
        this.containerStructureManager = containerStructureManager;
    }

    public void setContainerDefinitionManager(JahiaContainerDefinitionManager containerDefinitionManager) {
        this.containerDefinitionManager = containerDefinitionManager;
    }

    public void setFieldsDataManager(JahiaFieldsDataManager fieldsDataManager) {
        this.fieldsDataManager = fieldsDataManager;
    }

    public JahiaObjectManager getJahiaObjectManager() {
        return jahiaObjectManager;
    }

    public void setJahiaObjectManager(JahiaObjectManager jahiaObjectManager) {
        this.jahiaObjectManager = jahiaObjectManager;
    }
    
    public void setTplService(JahiaPageTemplateService tplService) {
        this.tplService = tplService;
    }

    /**
     * Default constructor, creates a new <code>JahiaContainersBaseService</code> instance.
     */
    protected JahiaContainersBaseService() {
    }


    /**
     * inits the service cache and settings
     */
    public void start()
            throws JahiaInitializationException {
    }

    public void stop()
            throws JahiaException {

        // flush the caches
//        containerInfoCache.flush();
//        containerListInfoCache.flush();
    }

    /**
     * Returns the unique instance of the Container Service.
     *
     * @return the unique instance of the Container Service
     */
    public static synchronized JahiaContainersBaseService getInstance() {
        if (instance == null) {
            instance = new JahiaContainersBaseService();
        }
        return instance;
    }

    /**
     * builds the complete container structure for a specific page
     * builds the complete container structure
     * (containerlists->containers->fields/containerlists)
     * for a specific page
     * DO NOT CACHE THIS METHOD (it depends on other caches values) !!
     *
     * @return a List of containerlist IDs
     */
    public JahiaContainerSet buildContainerStructureForPage(ProcessingContext processingContext, JahiaPage page)
            throws JahiaException {

        JahiaContainerSet theSet = new JahiaContainerSet(processingContext, page);

        JahiaPageDefinition def = page.getPageTemplate();
        String typeName = def.getPageType();
        if (typeName != null) {
            for (Map.Entry<String, Integer> containerNamesAndDefId : buildContainerDefinitionsForTemplate(typeName, page.getSiteID(), page.getPageTemplateID(), theSet).entrySet()) {
                theSet.addDeclaredContainer(containerNamesAndDefId.getKey(), containerNamesAndDefId.getValue());
                theSet.getContainerList(containerNamesAndDefId.getKey());
            }
        }

        return theSet;
    } // end buildContainerStructureForPage
    
    public Map<String, Integer> buildContainerDefinitionsForTemplate(String typeName, int siteId, int pageDefID, JahiaContainerSet theSet) throws JahiaException {
        Map<String, Integer> containerNamesAndDefId = new HashMap<String, Integer>();
        try {
            ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(typeName);
            ExtendedNodeDefinition[] nodes = nt.getChildNodeDefinitions();

            for (ExtendedNodeDefinition nodeDef : nodes) {
                if (nodeDef.getDeclaringNodeType().getName().equals(Constants.JAHIANT_PAGE) || nodeDef.getDeclaringNodeType().getName().equals(Constants.JAHIANT_JAHIACONTENT)) {
                    continue;
                }
                String parentCtnType = nodeDef.getDeclaringNodeType().getName() + " " + nodeDef.getName();
                String containerName = nt.getName().replace(':','_') + "_" + nodeDef.getName();
//                String containerName = nodeDef.getDeclaringNodeType().getName().replace(':','_') + "_" + nodeDef.getName();
                if (nodeDef.getRequiredPrimaryTypes()[0].isNodeType(Constants.JAHIANT_CONTAINERLIST)) {
                    nodeDef = nodeDef.getRequiredPrimaryTypes()[0].getDeclaredChildNodeDefinitionsAsMap().get("*");
                }

                StringBuffer defTypes = new StringBuffer();
                if (nodeDef.getRequiredPrimaryTypes().length > 0) {
                    for (int j = 0; j < nodeDef.getRequiredPrimaryTypes().length; j++) {
                        NodeType nodeType = nodeDef.getRequiredPrimaryTypes()[j];
                        if (nodeType != null) {
                            defTypes.append(nodeType.getName());
                            defTypes.append(",");
                        }
                    }
                }
                if (defTypes.length() == 0) {
                    logger.error("Definition for container " + nodeDef.getName() + " not found, skip it");
                } else {
                    String defTypesStr = defTypes.substring(0, defTypes.length() - 1);
                    int defId = declareContainerDefinition(parentCtnType, containerName, defTypesStr, nodeDef.getSelectorOptions(), siteId, pageDefID, theSet);
                    if (defId > 0) {
                        containerNamesAndDefId.put(containerName, defId);
                    }
                }
            }
        } catch (NoSuchNodeTypeException e) {
            logger.error(e);
        }
        return containerNamesAndDefId;
    }
    
    public int declareContainerDefinition(String parentCtnType, String containerName, String containerTypes, Map<String, String> options, int siteId, int pageDefID, JahiaContainerSet theSet) throws JahiaException {
        List<ExtendedNodeType> types = new ArrayList<ExtendedNodeType>();
        List<ExtendedNodeType> mixinTypes = new ArrayList<ExtendedNodeType>();
        List<String> containerFields = new ArrayList<String>();
        Properties containerDefProp = new Properties();
        Map<String, Properties> declaredFieldDefProps = new HashMap<String, Properties>();
        
        ExtendedNodeType baseType = null;
        boolean availableTypesFound = false;
        String[] cts = containerTypes.split(",");
        if (options != null) {
            if (options.containsKey("availableTypes")) {
                String[] available = options.get("availableTypes").split(",");
                for (String s : available) {
                    try {
                        ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(s);
                        if (!nt.isMixin()) {
                            types.add(nt);
                        } else {
                            logger.error("Cannot add type "+s+", is a mixin type");
                        }
                    } catch (NoSuchNodeTypeException e) {
                        logger.warn(e);
                    }
                    availableTypesFound = true;
                }
            }
            if (options.containsKey("addMixin")) {
                String[] available = options.get("addMixin").split(",");
                for (String s : available) {
                    try {
                        ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(s);
                        if (nt.isMixin()) {
                            mixinTypes.add(nt);
                        } else {
                            logger.error("Cannot add type "+s+", is not a mixin type");
                        }
                    } catch (NoSuchNodeTypeException e) {
                        logger.warn(e);
                    }
                }
            }
//            if (options.containsKey("addListMixin")) {
//                String subContainerName = "mixin";
//                declareContainer(parentCtnType, subContainerName, (String)options.get("addListMixin"), -1,-1,
//                        JahiaContainerDefinition.MANDATORY_TYPE+JahiaContainerDefinition.SINGLE_TYPE, new Properties(), new HashMap<String,String>());
//                containerFields.add("@c " + subContainerName);
//            }
        }
        for (String containerType : cts) {
            ExtendedNodeType nt = null;
            try {
                nt = NodeTypeRegistry.getInstance().getNodeType(containerType);
                if (!availableTypesFound) {
                    if (nt.isAbstract()) {
                        ExtendedNodeType[] subs = nt.getSubtypes();
                        for (ExtendedNodeType sub : subs) {
                            if (!sub.getSystemId().equals("system-standard")) {
                                JahiaTemplatesPackage defPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(sub.getSystemId());
                                if (defPackage != null)  {
                                    JahiaTemplatesPackage parentPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(NodeTypeRegistry.getInstance().getNodeType(parentCtnType.split(" ")[0]).getSystemId());
                                    if (parentPackage != null && !parentPackage.getInvertedHierarchy().contains(defPackage.getName())) {
                                        continue;
                                    }
                                }
                            }
                            types.add(sub);
                        }
                    } else {
                        types.add(nt);
                    }
                }
                if (baseType == null) {
                    baseType = nt;
                } else {
                    List<ExtendedNodeType> sups = Arrays.asList(nt.getPrimarySupertypes());
                    while (!sups.contains(baseType)) {
                        baseType = baseType.getPrimarySupertypes()[0];
                    }
                }
            } catch (NoSuchNodeTypeException e) {
                logger.error("Definition " + containerType + " not found, skip container " + containerName);
            }
        }
        if (types.isEmpty()) {
            logger.error("No types found for " + containerName);
            return 0;
        }
        Set<ExtendedItemDefinition> items = new ListOrderedSet();

        if (types.size() > 1) {
            for (ExtendedNodeType currentType : types) {
                items.addAll(currentType.getItems());
            }
            declareField(containerName, "nt:base jcr:primaryType", containerName + "_jcr_primaryType", FieldTypes.SMALLTEXT_SHARED_LANG, new String[] {"type"}, declaredFieldDefProps, siteId, pageDefID, theSet);
            containerFields.add("@f " + containerName + "_jcr_primaryType");
        } else {
            items.addAll(baseType.getItems());
        }
        
        for (ExtendedNodeType mixinType : mixinTypes) {
            items.addAll(mixinType.getItems());
        }        
        
        for (ExtendedItemDefinition ext : items) {
            NodeType dnt = ext.getDeclaringNodeType();
            if (dnt.isMixin() && (dnt.isNodeType(MetadataBaseService.METADATA_TYPE) || dnt.isNodeType(Constants.MIX_CREATED)|| dnt.isNodeType(Constants.MIX_CREATED_BY) || dnt.isNodeType(Constants.MIX_LAST_MODIFIED) ||
                    dnt.isNodeType(Constants.JAHIAMIX_LASTPUBLISHED)|| dnt.isNodeType(Constants.JAHIAMIX_CATEGORIZED)||dnt.isNodeType(Constants.JAHIAMIX_DESCRIPTION)||dnt.isNodeType(Constants.MIX_REFERENCEABLE)) ||
                    !dnt.isMixin() && !dnt.isNodeType(Constants.JAHIANT_CONTAINER) || dnt.getName().equals(Constants.JAHIANT_CONTAINER) || dnt.getName().equals(Constants.JAHIANT_JAHIACONTENT)) {
                continue;
            }

            addItemDefinitionToContainer(containerName, ext, containerFields, declaredFieldDefProps, siteId, pageDefID, theSet);
        }
        List<String> list = baseType.getGroupedItems();
        if (list != null) {
            int i=0;
            for (String s : list) {
                s = s.replace("-","@f "+containerName + "_").replace("+","@c "+containerName + "_");
                if (containerFields.remove(s)) {
                    containerFields.add(i++,s);
                }
            }
        }
        
        return declareContainerDefinition(parentCtnType, containerName, containerFields,
                baseType.getName(), containerDefProp, siteId, pageDefID, theSet);
    }
    
    private void declareField(String containerName, String name, ExtendedItemDefinition itemDef,
            List<String> containerFields, Map<String, Properties> declaredFieldDefProps, int siteId, int pageDefId,
            JahiaContainerSet theSet) throws JahiaException {

        String n = itemDef.getDeclaringNodeType().getName() + " " + itemDef.getName();

        declareField(containerName, n, name, getType(itemDef), new String[] { itemDef.getName() }, declaredFieldDefProps, siteId,
                pageDefId, theSet);

        containerFields.add("@f " + name);
    }
    
    /**
     * @param containerName
     * @param containerType
     * @param fieldName
     * @param fieldTitle
     * @param fieldType
     * @param aliasNames
     * @param boostFactor    the search boost factor, 1.0 by default
     * @param indexableField true by default, false if the field should not be indexed @throws JahiaException
     */
    private void declareField(String containerName,
                             String containerType, 
                             String fieldName,
                             int fieldType,
                             String[] aliasNames,
                             Map<String, Properties> declaredFieldDefProps,
                             int siteId, int pageDefId, JahiaContainerSet theSet)
            throws JahiaException {
        if(Jahia.getThreadParamBean() != null && Jahia.getThreadParamBean().getOperationMode().equals(ProcessingContext.NORMAL))
        return;
        
        /** todo ensure that the data provided by the user has no special chars in it */
        // we check if a field with the same name was not already declared, of if the field has no
        // empty name or title
        boolean isDeclaredField = theSet != null ? theSet.checkDeclaredField(containerName, fieldName) : false;

        //MC: this check was removed as it doesn't seem to have any purpose anymore
        //boolean isJahiaDataDeclaredField = jData.fields().checkDeclared(fieldName);

        // NK : 03.09.2003
        // FIXME, why is this test for since field names are handled differently with container names ????
        //boolean isDeclaredContainer = false;
        boolean isDeclaredContainer = theSet != null ? theSet.checkDeclared(fieldName) : false;

        if (aliasNames != null && aliasNames.length > 0) {
            declareFieldDefProp(declaredFieldDefProps, fieldName, JahiaFieldDefinition.ALIAS_PROP_NAME, JahiaTools.getStringArrayToString(aliasNames, ","));
            for (int i=0; i<aliasNames.length;i++){
                declareFieldDefProp(declaredFieldDefProps, fieldName, JahiaFieldDefinition.ALIAS_PROP_NAME+i, aliasNames[i]);
            }
        }

        // boost Factor
        if (!isDeclaredField &&
                //MC: this check was removed as it doesn't seem to have any purpose anymore
                //(!isJahiaDataDeclaredField) &&
                fieldName.length() != 0 && !isDeclaredContainer) {

            // first, let's check to see if the declared field has already a field definition in the
            // FieldsDefinitionsRegistry (which means also in the jahia_fields_def table)
            JahiaFieldDefinition aDef = JahiaFieldDefinitionsRegistry.
                    getInstance().getDefinition(siteId, fieldName);
            if (aDef != null) {
                // okay, it seems the definition already exists.
                // now has it the same data than in the database (title, type, defaultval) ?

                Map<Object, Object> props = aDef.getProperties();
                Properties declaredProps = (Properties) declaredFieldDefProps.get(fieldName);

                boolean propsHaveChanged = ((props == null && declaredProps != null && !declaredProps.isEmpty()) ||
                        (props != null && !props.isEmpty() && declaredProps == null)
                        || (props != null && declaredProps != null && !props.equals(declaredProps)));
                boolean ctnTypeHasChanged = (containerType != null && !containerType.equals(aDef.getCtnType())) || (containerType == null && aDef.getCtnType() != null);
                if (propsHaveChanged || ctnTypeHasChanged) {
                    if (aDef.getType() != fieldType && aDef.getType() != -1) {
                        boolean someChanges = false;
                        logger.warn("Definition for field " + fieldName + " (in " + containerName + ") has changed. ");
                        if (propsHaveChanged) {
                            logger.warn(" Properties have changed : " + props + " / " + declaredProps);
                            someChanges = true;
                        }
                        if (ctnTypeHasChanged) {
                            logger.warn(" Container type has changed : " + aDef.getCtnType() + " / " + containerType);
                            someChanges = true;
                        }
                        if (someChanges) {
                            if (logger.isDebugEnabled()) {
                                Throwable throwable = (new Throwable());
                                throwable.fillInStackTrace();
                                traceCaller(throwable, "declareField", pageDefId);
                            } else {
                                logger.warn("please activate debug mode on " + JahiaContainerSet.class.getPackage()
                                        + "." + JahiaContainerSet.class.getName()
                                        + " in your log4j.xml configuration to have more info");
                            }
                        }
                        

                        // okay, no alert ahead, the type hasn't been modified
                        // let's synchronize the data between :
                        //  - the template declaration (file)
                        //  - the database declaration (database)
                        //  - the registry declaration (memory)
                        // the synchronizeData method handles all this for us... pfew :)
                        logger.debug("Setting data for pageDef " +
                                pageDefId);
                        aDef.setProperties(declaredFieldDefProps.get(fieldName));
                        aDef.setCtnType(containerType);
                        JahiaFieldDefinitionsRegistry.getInstance().
                                setDefinition(aDef);
                    }
                }
            } else {
                // hell, the definition doesn't exist in the memory !
                // this can mean two things :
                //  - either this is the first time Jahia encounters this template
                //  - or the database data was screwed
                // in any case, we got to :
                //  - add it into the registry (memory)
                //  - add it into the database (database)
                //  - change the values in jData (memory)
                Map<Integer, JahiaFieldSubDefinition> subDefs = new HashMap<Integer, JahiaFieldSubDefinition>();
                subDefs.put(new Integer(pageDefId),
                        new JahiaFieldSubDefinition(0, 0, pageDefId,
                                false));
                aDef = new JahiaFieldDefinition(0,
                        siteId,
                        fieldName, subDefs);
                aDef.setCtnType(containerType);
                aDef.setProperties(declaredFieldDefProps.get(fieldName));

                JahiaFieldDefinitionsRegistry.getInstance().setDefinition(aDef);
            }
            if (theSet != null) {
                theSet.addDeclaredField(containerName, fieldName);
            }

        } else {
            // the field is already declared, or has a null name/title : let the user have it... ;)
            String errorMsg = "";
            if (fieldName.length() == 0) {
                errorMsg = " Field has null name";
            }
            if (isDeclaredField) {
                errorMsg += " Field already declared : " + fieldName;
            }
            //MC: this check was removed as it doesn't seem to have any purpose anymore
            /*if (isJahiaDataDeclaredField) {
                errorMsg += " Field already declared in JahiaData field set : " +
                    fieldName;
            }*/
            if (isDeclaredContainer) {
                errorMsg += " Field name already used by a container : " +
                        fieldName;
            }

            logger.warn(errorMsg);
        }
    } // end declareField

    /**
     * internal method to trace re-definition callers
     * @param t the throwable
     * @param methodName the name of method to trace
     */
    private void traceCaller(Throwable t, String methodName, int pageDefId){
        String requestURI = Jahia.getThreadParamBean().getRequestURI();
        JahiaPageDefinition pageTemplate = null;
        try {
            pageTemplate = tplService.lookupPageTemplate(pageDefId);
        } catch (JahiaException e) {
            logger.warn("Cannot retrieve template", e);
        }
        
        String templateName = pageTemplate != null ? pageTemplate.getName() : String.valueOf(pageDefId);
        String templateSource = pageTemplate != null ? pageTemplate.getSourcePath() : "";
        String templateJspSourceName = templateSource.substring(templateSource.lastIndexOf(File.separator) + 1, templateSource.lastIndexOf("."));

        for (int i = 0; i < t.getStackTrace().length; i++) {
            // trace only if method called is equal to methodName or _jspService(scriptlet)
            if (t.getStackTrace()[i].getFileName().indexOf(templateJspSourceName) != -1
                    &&  (t.getStackTrace()[i].getMethodName().indexOf(methodName)!=-1 || t.getStackTrace()[i].getMethodName().indexOf("_jspService")!=-1 ))
                logger.debug("Field ReDefinition:"
                        + "\nrequested uri:" + requestURI
                        + "\ntemplatename:" + templateName
                        + "\ntemplatepath:" + templateSource
                        + "\n"+t.getStackTrace()[i].getMethodName()
                        + " at line " + t.getStackTrace()[i].getLineNumber()
                        + "\nin file " + t.getStackTrace()[i].getFileName());
        }
    }
    
    
    /**
     * Declare properties for a given field definition ( field name )
     *
     * @param fieldName
     * @param propName
     * @param propValue
     */
    private void declareFieldDefProp(Map<String, Properties> declaredFieldDefProps, String fieldName,
                                    String propName,
                                    String propValue) {
        if (fieldName != null && propName != null && propValue != null) {
            Properties props = (Properties) declaredFieldDefProps.get(fieldName);
            if (props == null) {
                props = new Properties();
            }
            props.setProperty(propName, propValue);
            declaredFieldDefProps.put(fieldName, props);
        }
    }

    
    public static int getType(ExtendedItemDefinition itemDef) {
        if (!itemDef.isNode()) {
            ExtendedPropertyDefinition propDef = (ExtendedPropertyDefinition) itemDef;
            switch (propDef.getRequiredType()) {
                case PropertyType.STRING :
                    switch (propDef.getSelector()) {
                        case SelectorType.RICHTEXT:
                            return FieldTypes.BIGTEXT;
                        case SelectorType.FILE:
                            return FieldTypes.FILE;
                        case SelectorType.CATEGORY:
                            return FieldTypes.CATEGORY;
                        case SelectorType.COLOR:
                            return FieldTypes.COLOR;
                        default:
                            if (propDef.isInternationalized())
                                return FieldTypes.SMALLTEXT;
                            else
                                return FieldTypes.SMALLTEXT_SHARED_LANG;
                    }
                case PropertyType.LONG :
                    return FieldTypes.INTEGER;
                case PropertyType.DOUBLE :
                    return FieldTypes.FLOAT;
                case PropertyType.DATE :
                    return FieldTypes.DATE;
                case PropertyType.BOOLEAN :
                    return FieldTypes.BOOLEAN;
                case PropertyType.REFERENCE :
                    switch (propDef.getSelector()) {
                        case SelectorType.PORTLET:
                            return FieldTypes.APPLICATION;
                    }
            }
        } else {
            ExtendedNodeDefinition nodeDef = (ExtendedNodeDefinition) itemDef;
            if (nodeDef.getRequiredPrimaryTypes()[0].isNodeType(Constants.JAHIANT_PORTLET)) {
                return FieldTypes.APPLICATION;
            } else if (nodeDef.getRequiredPrimaryTypes()[0].isNodeType(Constants.JAHIANT_PAGE_LINK)) {
                return FieldTypes.PAGE;
            }
        }

        logger.warn("Unknown type : "+itemDef.getName());
        return -1;
    }

    /**
     * enables a jahia template programmer to declare a container, specifying
     * scrollable window size and offset for container lists that will grow very
     * large in size.
     *
     * @param containerName          the container name
     * @param containerFields        the fields (or containers) in the container
     * @param containerNodeType
     * @param containerDefProperties a set of properties to be stored with the container definition.
     *                               Be careful because some keywords are reserved, and will be ignored. The following keys may NOT be used
     *                               because they are reserved for internal Jahia use : windowSize, windowOffset, validatorKey, containerBeanName,
     *                               ALIAS_NAME, CONTAINER_LIST_TYPE. This argument may be null if you don't want to define any custom properties
     *                               for the container definition. @throws JahiaException a critical JahiaException if the container has been already declared
     * @throws JahiaException raises a critical JahiaException if one of the containerFields have not been declared
     * @throws JahiaException raises a critical JahiaException if one of the fields has the same name as the container
     *                        <p/>
     *                        In order to let a container include itself, the keyword "_self" in the containeFields must be used.
     */
    private int declareContainerDefinition(String parentCtnType, String containerName, 
                                 List<String> containerFields, 
                                 String containerNodeType, Properties containerDefProperties, int siteId, int pageDefID, JahiaContainerSet theSet)
            throws JahiaException {
        int defId = 0;
        if (Jahia.getThreadParamBean() != null && Jahia.getThreadParamBean().getOperationMode().equals(ProcessingContext.NORMAL))
        return defId;

        /** @todo ensure that the data provided by the user has no special chars in it */
        // check if a container has already been declared with the same name
        // checks if container name and title are not empty
        // checks if a field has not the same name
        if (theSet != null && (theSet.checkDeclared(containerName) || containerName.length() == 0
                || theSet.checkDeclaredField(containerName))) {
            // the container is already declared, or has a null name/title : let
            // the user have it... ;)
            String errorMsg = "Container already declared or has a null name - name : "
                    + containerName;
            logger.error(errorMsg + " -> BAILING OUT");
            throw new JahiaException(errorMsg, errorMsg,
                    JahiaException.TEMPLATE_ERROR,
                    JahiaException.CRITICAL_SEVERITY);
        } else {
            // first, let's check that all the fields in the containerFields
            // exist
            for (int i = 0; i < containerFields.size(); i++) {
                String theName = containerFields.get(i);
                boolean isField = theName.startsWith("@f ");
                boolean isContainer = theName.startsWith("@c ");
                if (isField || isContainer) {
                    theName = theName.substring(3);
                }
                if (theName.equals("_self")) {
                    containerFields.set(i, containerName);
                } else if (theSet != null && !isField && !theSet.checkDeclared(theName) && !isContainer
                        && !theSet.checkDeclaredField(theName)) {

                    // one of the fields or containers in containerFields
                    // doesn't exist !!
                    String errorMsg = "Element not defined in container "
                            + containerName + " : " + theName;
                    logger.error(errorMsg + " -> BAILING OUT");
                    throw new JahiaException(errorMsg, errorMsg,
                            JahiaException.TEMPLATE_ERROR,
                            JahiaException.CRITICAL_SEVERITY);
                }
            }

            // second, let's build the property set of the container
            // definition
            Properties ctnDefProperties = new Properties();
            if (containerDefProperties != null
                    && !containerDefProperties.isEmpty()) {
                ctnDefProperties.putAll(containerDefProperties);
            }

            JahiaContainerDefinitionsRegistry ctnDefRegistry = JahiaContainerDefinitionsRegistry
                    .getInstance();

            // third, let's check to see if the declared container has
            // already a container definition in the
            // ContainersDefinitionsRegistry
            JahiaContainerDefinition aDef = ctnDefRegistry.getDefinition(
                    siteId, containerName);
            // matter in which language it is.
            if (aDef != null) {
                // okay, it seems the definition already exists.
                // now has it the same data than in the database ?
                boolean propertiesHaveChanged = !aDef.getProperties().equals(
                        ctnDefProperties);

                // checks if title changed
                // checks if structure changed
                boolean structureHasChanged = aDef.structureChanged(
                        containerFields);

                boolean nodeTypeHasChanged = (containerNodeType != null && !containerNodeType
                        .equals(aDef.getContainerType()))
                        || (containerNodeType == null && aDef
                                .getContainerType() != null);
                boolean parentTypeHasChanged = (parentCtnType != null && !parentCtnType
                        .equals(aDef.getParentCtnType()))
                        || (parentCtnType == null && aDef.getParentCtnType() != null);
                if (structureHasChanged || propertiesHaveChanged
                        || nodeTypeHasChanged || parentTypeHasChanged) {
                    if (aDef.getStructure().hasNext()) {
                        logger.warn("Definition for container " + containerName
                                + " has changed");
                        if (propertiesHaveChanged) {
                            logger.warn(" Properties have changed : "
                                    + aDef.getProperties() + " / "
                                    + ctnDefProperties);
                        }
                        if (structureHasChanged) {
                            logger.warn(" Structure has changed");
                        }
                        if (nodeTypeHasChanged) {
                            logger.warn(" Node type has changed");
                        }
                        if (parentTypeHasChanged) {
                            logger.warn(" Parent type has changed");
                        }
                    }
                    synchronized (ctnDefRegistry) {
                        aDef.setProperties(ctnDefProperties);
                        // well, data is not the same in the registry and in the
                        // declare() method !
                        // this means the user has changed the container
                        // declaration in the template...
                        aDef.composeStructure(containerFields);
                        aDef.setContainerType(containerNodeType);
                        aDef.setParentCtnType(parentCtnType);
                        // okay, let's synchronize the data between :
                        // - the template declaration (file)
                        // - the database declaration (database)
                        // - the registry declaration (memory)
                        ctnDefRegistry.setDefinition(aDef);
                    }

                    /**
                     * todo we should now reload the container list since a lot of things have changed, including window parameters.
                     */
                    if (propertiesHaveChanged && theSet != null) {
                        logger.debug("Reloading containerList " + containerName
                                + "...");

                        // small hack to avoid doing this everywhere and to
                        // be able to call checkDeclared in the next call...
                        theSet.reloadContainerList(containerName);
                    }
                }
            } else {
                // hell, the definition doesn't exist in the memory !
                // this can mean two things :
                // - either this is the first time Jahia encounters this
                // template
                // - or the database data was screwed
                // in any case, we got to :
                // - add it into the registry (memory)
                // - add it into the database (database)
                // - change the values in jData (memory)

                Map<Integer, JahiaContainerSubDefinition> subDefs = new HashMap<Integer, JahiaContainerSubDefinition>();
                JahiaContainerSubDefinition subDef = new JahiaContainerSubDefinition(
                        0, pageDefID, siteId, null);
                subDefs.put(0, subDef);
                aDef = new JahiaContainerDefinition(0, siteId,
                        containerName, subDefs, containerNodeType);
                // insert new properties.
                aDef.setProperties(ctnDefProperties);
                synchronized (ctnDefRegistry) {
                    ctnDefRegistry.setDefinition(aDef);
                    aDef = ctnDefRegistry.getDefinition(aDef.getID());
                    aDef.composeStructure(containerFields);
                    aDef.setParentCtnType(parentCtnType);
                    ctnDefRegistry.setDefinition(aDef);
                }
            }
            defId = aDef.getID();
        }
        return defId;
    }    
    
    private void addItemDefinitionToContainer(String containerName, ExtendedItemDefinition ext, List<String> containerFields, Map<String, Properties> declaredFieldDefProps, int siteId, int pageDefId, JahiaContainerSet theSet) throws JahiaException {
        if (!ext.isNode()) {
//                if (propDef.isMultiple()) {
//                    String name = containerName + "_" + propDef.getName();
//                    String subContainerName = name + "Container";
//                    declareField(subContainerName, name, propDef);
//                    List subContainerFields = new ArrayList();
//                    subContainerFields.add("@f " + name);
//                    declareContainer(subContainerName, propDef.getName(), subContainerFields,windowSize,windowOffset, null, null, new String[] {propDef.getName()});
//                    containerFields.add("@c " + subContainerName);
//                } else {
                String name = containerName + "_"+ ext.getName();
                declareField(containerName, name, ext, containerFields, declaredFieldDefProps, siteId, pageDefId, theSet);
//                }
        } else {
            ExtendedNodeDefinition nodeDef = (ExtendedNodeDefinition) ext;
            boolean isJahiaField = false;
            StringBuffer defTypes = new StringBuffer();
            ExtendedNodeType[] nodeTypes = nodeDef.getRequiredPrimaryTypes();
            if (nodeTypes.length > 0) {
                if (nodeTypes[0].isNodeType(Constants.JAHIANT_CONTAINERLIST)) {
                    nodeTypes = nodeTypes[0].getChildNodeDefinitionsAsMap().get("*").getRequiredPrimaryTypes();
                }
                for (int i = 0; i < nodeTypes.length; i++) {
                    ExtendedNodeType nodeType = nodeTypes[i];
                    if (nodeType.isNodeType(Constants.JAHIANT_PAGE_LINK) || nodeType.isNodeType(Constants.JAHIANT_PORTLET)) {
                        isJahiaField = true;
                    }
                    if (nodeType != null) {
                        defTypes.append(nodeType.getName());
                        defTypes.append(",");
                    }
                }
            }
            if (isJahiaField) {
                String name = containerName + "_"+ ext.getName();
                declareField(containerName, name, ext, containerFields, declaredFieldDefProps, siteId, pageDefId, theSet);
            } else if (defTypes.length() == 0) {
                logger.error("Definition for subcontainer "+ nodeDef.getName() + " not found, skip it");
            } else {
                String subContainerName = containerName + "_"+ nodeDef.getName();
                String defTypesStr = defTypes.substring(0,defTypes.length()-1);
                declareContainerDefinition(ext.getDeclaringNodeType().getName() + " " + ext.getName(), subContainerName, defTypesStr, nodeDef.getSelectorOptions(), siteId, pageDefId, theSet);
                containerFields.add("@c " + subContainerName);
            }
        }
    }


    /**
     * gets all container definitions ids on a page
     *
     * @param contentPage the page object
     * @return a List of container definition IDs
     */
    public List<JahiaContainerDefinition> getctndefidsInPage(ContentPage contentPage,
                                     EntryLoadRequest entryLoadRequest)
            throws JahiaException {
        return JahiaContainerDefinitionsRegistry.getInstance().
                getDefinitionsInTemplate(contentPage.getPageTemplate(entryLoadRequest));
    } // end getctndefidsInPage

    /**
     * gets all container list ids on a page, by their definition id
     *
     * @param pageID the page id
     * @param defID  the container definition id
     * @return a List of containerlist IDs
     */
    public List<Integer> getContainerListIDs(int pageID, int defID,
                                      EntryLoadRequest loadVersion)
            throws JahiaException {
        return new ArrayList<Integer>(containerListManager.getContainerListIdsByDefinition(pageID, defID, loadVersion));
    } // end getContainerListIDs

    /**
     * gets all container list ids on a page depending on the load request
     * DO NOT CACHE THIS METHOD (it depends on other caches values) !!
     *
     * @return a List of containerlist IDs
     */
    public List<Integer> getContainerListIDsInPage(ContentPage contentPage,
                                            EntryLoadRequest entryLoadRequest)
            throws JahiaException {
        List<Integer> listIDs = new ArrayList<Integer>();
        for (JahiaContainerDefinition theDefinition : getctndefidsInPage(contentPage,
                entryLoadRequest)) {
            listIDs.addAll(getContainerListIDs(contentPage.getID(),
                    theDefinition.getID(),
                    entryLoadRequest));
        }
        return listIDs;
    } // end getContainerListIDsInPage

    /**
     * gets all container ids in a container list. This list uses stored
     * ranks to order its values
     *
     * @param listID the container list id
     * @return a List of container IDs
     *         <p/>
     *         todo we might want to cache these values in memory in order to improve performance.
     *         laisser encore un moment pour pol parske il le vaut bien
     */
    // laisser encore un moment pour pol parske il le vaut bien
    public List<Integer> getctnidsInList(int listID)
            throws JahiaException {
        return getctnidsInList(listID, EntryLoadRequest.CURRENT);
    } // end getctnidsInList

    public List<Integer> getctnidsInList(int listID, EntryLoadRequest loadVersion)
            throws JahiaException {
        return containerManager.getContainerIdsInContainerList(listID, loadVersion, false);
    } // end getctnidsInList

    /**
     * Gets all container ids in a container list with ordering on a given field.
     * If the fieldName is null, not ordering applied
     *
     * @param listID    the container list id
     * @param fieldName the fieldname on which to filter
     * @param asc       Asc. or desc. ordering ( true = asc )
     * @return a List of container IDs
     *         <p/>
     *         todo we might want to cache these values in memory in order to
     *         improve performance.
     */
    public List<Integer> getctnidsInList(int listID, String fieldName, boolean asc,
                                  EntryLoadRequest loadVersion)
            throws JahiaException {
        if (fieldName == null || fieldName.trim().equals("")) {
            return new ArrayList<Integer>(containerManager.getContainerIdsInContainerList(listID, loadVersion, false));
        }
        return new ArrayList<Integer>(containerManager.getContainerIdsInContainerListSortedByFieldValue(listID, fieldName,
                asc, loadVersion));
    } // end getctnidsInList

    /**
     * gets all container ids for a given site. This list uses stored
     * ranks to order its values
     *
     * @param siteID the given site id.
     * @return List a List of container IDs
     */
    public List<Integer> getCtnIds(int siteID)
            throws JahiaException {
        return new ArrayList<Integer>(containerManager.getAllContainerFromSite(siteID));
    }

    /**
     * gets all container ids of the system.
     *
     * @return List a List of container IDs
     */
    public List<Integer> getCtnIds()
            throws JahiaException {
        return new ArrayList<Integer>(containerManager.getAllContainersIds());
    }

    /**
     * gets all container ids for a given entryLoadRequest
     *
     * @param loadVersion
     * @return
     * @throws JahiaException
     */
    public List<Integer> getCtnIds(EntryLoadRequest loadVersion)
            throws JahiaException {
        return new ArrayList<Integer>(containerManager.getContainerIdsInContainerList(-1, loadVersion, true));
    }

    /**
     * Correct version to sort a containerList without any sort defined
     *
     */
    public List<Integer> getCtnIds(BitSet ids, EntryLoadRequest loadRequest) throws JahiaException {
        return new ArrayList<Integer>(containerManager.getAllContainersIds(ids, loadRequest));
    }

    /**
     * gets all container list ids in a container.
     *
     * @param ctnID the container list id
     * @return a List of container IDs
     *         <p/>
     *         todo we might want to cache these values in memory in order to
     *         improve performance.
     */
    public List<Integer> getCtnListIDsInContainer(int ctnID)
            throws JahiaException {
        return new ArrayList<Integer>(containerListManager.getContainerListIdsInContainer(ctnID));
    }

    /**
     * gets all field ids in a container
     *
     * @param ctnid the container id
     * @return a List of field IDs
     */
    public List<Integer> getFieldIDsInContainer(int ctnid)
            throws JahiaException {
        return getFieldIDsInContainer(ctnid, null);
    } // end getFieldIDsInContainer

    public List<Integer> getFieldIDsInContainer(int ctnid,
                                         EntryLoadRequest loadVersion)
            throws JahiaException {
        return new ArrayList<Integer>(fieldsDataManager.getFieldsIdsInContainer(ctnid, loadVersion));
    } // end getFieldIDsInContainer

    public List<Object[]> getFieldIDsAndTypesInContainer(int ctnid,
                                                 EntryLoadRequest loadVersion)
            throws JahiaException {
        return new ArrayList<Object[]>(fieldsDataManager.getFieldsIdsAndTypesInContainer(ctnid, loadVersion));
    } // end getFieldIDsInContainer

    /**
     * gets a container list id by its container list name and page id
     *
     * @param containerName the container name
     * @param pageID        the page ID
     * @return a container list id
     */
    public int getContainerListID(String containerName, int pageID)
            throws JahiaException {
        return containerListManager.getIdByPageIdAndDefinitionName(containerName, pageID);
    } // end getContainerListID

    /**
     * gets all container definition ids in Jahia
     *
     * @return a List of container definition IDs
     */
    public List<Integer> getAllContainerDefinitionIDs()
            throws JahiaException {
        return new ArrayList<Integer>(containerDefinitionManager.getAllContainerDefinitionIds());
    } // end getAllContainerDefinitionIDs

    /**
     * loads a container info, by its container id
     * NO RIGHTS CHECKS ! see loadContainer(ctnid,LoadFlags,jParams) for that.
     *
     * @param ctnid the container id
     * @return a JahiaContainer object, without field values
     * @see org.jahia.data.containers.JahiaContainer
     */
    public JahiaContainer loadContainerInfo(int ctnid)
            throws JahiaException {
        return loadContainerInfo(ctnid, EntryLoadRequest.CURRENT);
    }

    public JahiaContainer loadContainerInfo(int ctnid,
                                            EntryLoadRequest loadVersion)
            throws JahiaException {
        logger.debug("Attempting to load container " + ctnid +
                " , EntryLoadRequest=" + loadVersion);
        return containerManager.loadContainer(ctnid, loadVersion);

    } // end loadContainerInfo

    /**
     * loads a container by its container id
     * loads a container info and fields by its container id, but not dependant containerlists
     * this method cannot load request-specific values (i.e. applications);
     * see the loadContainer( ctnid, loadFlag, jParams) for that.
     * NO RIGHTS CHECKS ! see loadContainer(ctnid,LoadFlags,jParams) for that.
     * DO NOT CACHE THIS METHOD (it depends on other caches values) !!
     *
     * @param ctnid    the container id
     * @param loadFlag the loadFlag
     * @see org.jahia.data.containers.JahiaContainer
     * @see org.jahia.data.fields.LoadFlags
     */

//    public JahiaContainer loadContainer(int ctnid, int loadFlag)
//            throws JahiaException {
//        return loadContainer(ctnid, loadFlag, null);
//    } // end loadContainer
    public JahiaContainer loadContainer(int ctnid, int loadFlag,
                                        ProcessingContext jParams)
            throws JahiaException {
        return loadContainer(ctnid, loadFlag, jParams,
                jParams.getEntryLoadRequest());
    }

    /**
     * loads a container by its container id
     * loads a container info and fields by its container id, but not dependant containerlists
     * this method can load request-specific values (i.e. applications);
     * DO NOT CACHE THIS METHOD (it depends on other caches values) !!
     *
     * @param ctnid    the container id
     * @param loadFlag the loadFlag
     * @param jParams  the ProcessingContext object, containing request and response
     * @see org.jahia.data.containers.JahiaContainer
     * @see org.jahia.data.fields.LoadFlags
     *      DJ 29.01.2001 - added ACL checks
     *      if there are no read rights -> return an empty container with -1 as ID.
     */
    // DJ 29.01.2001 - added ACL checks
    // if there are no read rights -> return an empty container with -1 as ID.
    public JahiaContainer loadContainer(int ctnid, int loadFlag,
                                        ProcessingContext jParams,
                                        EntryLoadRequest loadVersion)
            throws JahiaException {
        return loadContainer(ctnid, loadFlag, jParams, loadVersion,
                new HashMap<Integer, List<Integer>>(),
                new HashMap<Integer, List<Integer>>(),
                new HashMap<Integer, List<Integer>>());
    }

    /**
     * @param ctnid
     * @param loadFlag
     * @param jParams
     * @param loadVersion
     * @param cachedFieldsInContainer
     * @return
     * @throws JahiaException
     */
    // the Map cachedFieldsInContainer is here if we already have a Map
    // with containerID as key, and a List of fieldIDs in object, see buildContainerStructure.
    public JahiaContainer loadContainer(int ctnid, int loadFlag,
                                        ProcessingContext jParams,
                                        EntryLoadRequest loadVersion,
                                        Map<Integer, List<Integer>> cachedFieldsInContainer,
                                        Map<Integer, List<Integer>> cachedContainersFromContainerLists,
                                        Map<Integer, List<Integer>> cachedContainerListsFromContainers)
            throws JahiaException {
        // loads container info
        JahiaContainer theContainer = loadContainerInfo(ctnid, loadVersion);
        if (theContainer == null) {
            return theContainer;
        }

        ContainerFactoryProxy cFactory =
                new ContainerFactoryProxy(loadFlag,
                        jParams,
                        loadVersion,
                        cachedFieldsInContainer,
                        cachedContainersFromContainerLists,
                        cachedContainerListsFromContainers);

        theContainer.setFactoryProxy(cFactory);

        // loads container fields
        // check for correct rights on container
        if (jParams != null) { // no jParams, can't check for rights
            JahiaUser currentUser = jParams.getUser();
            if (currentUser != null) {
                logger.debug("loadContainer(): checking rights...");
                // if the user has no read rights, return the container with no fields.
                if (!theContainer.checkReadAccess(currentUser) ||
                        (!theContainer.getContentContainer().hasActiveEntries() && !theContainer.checkWriteAccess(currentUser))) {
                    logger.debug("loadContainer(): NO read rights! -> returning null");
                    theContainer.setID(-1); // special flag to say the container is not loaded
                    return theContainer;
                }
                logger.debug("loadContainer(): read rights OK");
            } else {
                throw new JahiaException("No user present !",
                        "No current user defined in the params in loadField() method.",
                        JahiaException.USER_ERROR, JahiaException.ERROR_SEVERITY);
            }
        }
        // end check rights on container

        return theContainer;
    } // end loadContainer

    /**
     * Load a list of containers
     *
     * @param ctnids
     * @param loadFlag
     * @param jParams
     * @param loadVersion
     * @return
     * @throws JahiaException
     */
    public List<JahiaContainer> loadContainers(List<Integer> ctnids, int loadFlag,
                               ProcessingContext jParams,
                               EntryLoadRequest loadVersion)
            throws JahiaException {
        List<JahiaContainer> results = new ArrayList<JahiaContainer>();
        if (ctnids == null) {
            return null;
        }
        if (ctnids.size() == 0) {
            return results;
        }
        // orders the ids
        SortedSet<Integer> sortedIds = new TreeSet<Integer>(ctnids);
        Map<Integer, JahiaContainer> containers = loadContainerInfos(sortedIds, loadVersion);
        int size = ctnids.size();
        Integer id = null;
        JahiaContainer container = null;
        for (int i = 0; i < size; i++) {
            id = (Integer) ctnids.get(i);
            container = (JahiaContainer) containers.get(id);
            if (container != null) {
                ContainerFactoryProxy cFactory =
                        new ContainerFactoryProxy(loadFlag,
                                jParams,
                                loadVersion,
                                null,
                                null,
                                null);

                container.setFactoryProxy(cFactory);

                // loads container fields
                // check for correct rights on container
                if (jParams != null) { // no jParams, can't check for rights
                    JahiaUser currentUser = jParams.getUser();
                    if (currentUser != null) {
                        logger.debug("loadContainer(): checking rights...");
                        // if the user has no read rights, return the container with no fields.
                        if (!container.checkReadAccess(currentUser) ||
                                (!container.getContentContainer().hasActiveEntries() && !container.checkWriteAccess(currentUser))) {
                            logger.debug("loadContainer(): NO read rights! -> returning null");
                            container.setID(-1); // special flag to say the container is not loaded
                        }
                        logger.debug("loadContainer(): read rights OK");
                    } else {
                        throw new JahiaException("No user present !",
                                "No current user defined in the params in loadField() method.",
                                JahiaException.USER_ERROR, JahiaException.ERROR_SEVERITY);
                    }
                }
                // end check rights on container
                results.add(container);
            }
        }
        return results;
    }

    public Map<Integer, JahiaContainer> loadContainerInfos(Collection<Integer> sortedIds,
                                  EntryLoadRequest loadVersion)
            throws JahiaException {
        return containerManager.loadContainers(sortedIds, loadVersion);

    } // end loadContainerInfo

    /**
     * saves the container info
     * saves the container info
     * if id=0, assigns a new id to container and creates it in datasource
     * if listid=0, assigns a new listid to container and creates it in datasource
     *
     * @param theContainer a JahiaContainer object
     * @param parentID     parent container id
     * @param parentAclID  the Acl parent ID
     * @param        jParams                the current ProcessingContext
     * @see org.jahia.data.containers.JahiaContainer
     *      DJ          29.01.01            added ACL check
     *      Note : the new ACLs are created here and not in the engine.
     *      EV          26.02.2001          added parentAclID - acl is no more a dirty hack ;)
     */
    //      DJ          29.01.01            added ACL check
    //                                      Note : the new ACLs are created here and not in the engine.
    //      EV          26.02.2001          added parentAclID - acl is no more a dirty hack ;)
    //
    public void saveContainerInfo(JahiaContainer theContainer,
                                  int parentID,
                                  int parentAclID,
                                  ProcessingContext jParams)
            throws JahiaException {
        // start check for correct rights.
        if (jParams != null) { // no jParams, can't check for rights
            JahiaUser currentUser = jParams.getUser();
            if (currentUser != null) {
                logger.debug("saveContainerInfo(): checking rights...");
                // if the user has no write rights, exit method.
                if (theContainer.getID() != 0 && !theContainer.checkWriteAccess(currentUser)) {
                    logger.debug("saveContainerInfo(): NO write rights! -> don't save");
                    return;
                }
                logger.debug("saveContainerInfo(): write rights OK");
            } else {
                throw new JahiaException("No user present !",
                        "No current user defined in the params in saveContainerInfo() method.",
                        JahiaException.USER_ERROR, JahiaException.ERROR_SEVERITY);
            }
        }
        // end check rights.

        JahiaSaveVersion saveVersion = ServicesRegistry.getInstance().
                getJahiaVersionService().
                getSiteSaveVersion(theContainer.
                        getJahiaID());

        boolean isNew = false;

        if (theContainer.getID() == 0) {
            isNew = true;
            // container not yet exists -> we have to give him an ID !
            int listID = theContainer.getListID();

            /** todo this code was commented out because it caused problems
             * with sub-containers lists that were always loading the first
             * sub-container list.
             */

            /*
            if (listID == 0) {
                // try to load from db again
                listID = ServicesRegistry.getInstance()
                           .getJahiaContainersService().getContainerListID(
                           theContainer.getDefinition().getName(),
                           theContainer.getObjectKey());
            }
            */
            if (listID <= 0) {
                // container is not yet in a list -> we have to create a list !
                JahiaContainerList newList = new JahiaContainerList(0, parentID,
                        theContainer.getPageID(),
                        theContainer.getctndefid(), 0);
                saveContainerListInfo(newList, parentAclID, jParams);

                //create default acl for container list edit view field
                Iterator<JahiaField> fList = theContainer.getFields();
                while (fList.hasNext()) {
                    JahiaField aField = (JahiaField) fList.next();
                    /*
                         aField = ServicesRegistry.getInstance().
                         getJahiaFieldService().loadField( aField.getID(), LoadFlags.ALL, jParams );
                     */
                    JahiaFieldDefinition theDef = aField.getDefinition();
                    if (theDef != null) {

                        // create the ACL object...
//                        JahiaBaseACL newAcl = null;
//                        newAcl = new JahiaBaseACL(parentAclID);
//                        newAcl.create(newList.getAclID());
                        newList.setProperty("view_field_acl_" + theDef.getName(),
                                String.valueOf(parentAclID));
                    }
                }
                listID = newList.getID();
                // FIXME : saved twite ????
                /*
                saveContainerListInfo(newList, parentAclID);
                */
            }
            theContainer.setListID(listID);

            // let's load the parent ContainerList to get its ACL ID
            JahiaContainerList tempList = loadContainerListInfo(theContainer.getListID());
            int clAclID = 0;
            if (tempList != null) {
                clAclID = tempList.getAclID();
                logger.debug(">Container herited ACL from the ContainerList, OK<");
            }

//            // Start Create a new ACL object for the new container.
//            JahiaBaseACL acl = new JahiaBaseACL(clAclID);

            // create a new object by specifying the parent ACL ID (a containerList)
//            if (!acl.create(clAclID)) {
//                String message =
//                        "Could not create an ACL object for a new container.";
//                logger.debug(message + " -> Stop container creation!");
//                throw new JahiaException("JahiaContainersBaseService",
//                        message,
//                        JahiaException.ACL_ERROR,
//                        JahiaException.CRITICAL_SEVERITY);
//            } else {
//                logger.debug("ACL [" + acl.getID() +
//                        "] has just been created! (Container)");
//            }
            // End Create ACL
            theContainer.setAclID(clAclID);

            containerManager.saveContainer(theContainer, saveVersion);
//            if (!saveVersion.isStaging()) {
//                containerInfoCache.put(
//                        getCacheContainerOrContainerListEntryKey(theContainer.getID(), saveVersion.getWorkflowState()),
//                        theContainer);

//            }

            ContentContainer container = ContentContainer.getContainer(theContainer.getID());
            if (container != null) {
                ContainersChangeEventListener listener = (
                        ContainersChangeEventListener) JahiaListenersRegistry.
                        getInstance()
                        .getListenerByClassName(ContainersChangeEventListener.class.getName());
                if (listener != null) {
                    listener.notifyChange(container, ContainersChangeEventListener.CONTAINER_ADDED);
                }
            }
            JahiaEvent objectCreatedEvent = new JahiaEvent(this, jParams, container);
            ServicesRegistry.getInstance().getJahiaEventService().fireContentObjectCreated(objectCreatedEvent);
        } else {
            if (theContainer.isChanged()) {
                // container already exists -> just need to update
                JahiaContainer tmpContainer = (JahiaContainer) theContainer.clone();

                containerManager.updateContainer(tmpContainer, saveVersion);

                ContentContainer container = ContentContainer.getContainer(theContainer.getID());
                JahiaEvent objectCreatedEvent = new JahiaEvent(this, jParams, container);
                ServicesRegistry.getInstance().getJahiaEventService()
                        .fireContentObjectUpdated(objectCreatedEvent);
            }
        }

        // update cache
        if (theContainer != null) {
//            containerInfoCache.remove(getCacheContainerOrContainerListStagingEntryKey(theContainer.getID()));
            ContentContainer.invalidateContainerCache(theContainer.getID());
//            c_utils.invalidateCtnIdsByCtnListCache(theContainer.getListID());
        }

        if (theContainer.isChanged()) {
            WorkflowEvent theEvent = new WorkflowEvent(this, theContainer.getContentContainer(), jParams.getUser(),
                    ContentField.SHARED_LANGUAGE, false);
            theEvent.setNew(isNew);
            ServicesRegistry.getInstance().getJahiaEventService().fireObjectChanged(theEvent);
        }

    } // end saveContainerInfo

    /**
     * saves the container info and fields
     * saves the container info and fields, but not the dependant container lists
     * if id=0, assigns a new id to container and creates it in datasource
     * if listid=0, assigns a new listid to container and creates it in datasource
     *
     * @param theContainer a JahiaContainer object
     * @see org.jahia.data.containers.JahiaContainer
     */
    public void saveContainer(JahiaContainer theContainer,
                              int containerParentID,
                              ProcessingContext jParams)
            throws JahiaException {
        // gets the parent acl ID
        int parentAclID = 0;
        int listID = theContainer.getListID();
        // if the container is included in an existing list,
        // just get the container list acl id
        if (listID != 0) {
            JahiaContainerList theList = this.loadContainerListInfo(listID);
            if (theList != null) {
                parentAclID = theList.getAclID();
            }
        }
        // if parent acl id is still 0, then if the container is included in
        // a parent container, just get the parent container acl id
        if (parentAclID == 0) {
            if (containerParentID != 0) {
                JahiaContainer parentContainer = this.loadContainerInfo(containerParentID, EntryLoadRequest.CURRENT);
                if (parentContainer != null) {
                    parentAclID = parentContainer.getAclID();
                }
            }
        }
        // if parent acl id is still 0, then simply get the page acl id
        if (parentAclID == 0) {
            parentAclID = jParams.getPage().getAclID();
        }
        // end of getting parent acl id

        // save the container list info
        ServicesRegistry.getInstance().getJahiaContainersService().
                saveContainerInfo(theContainer, containerParentID, parentAclID,
                        jParams);

        // save fields, one by one
        Iterator<JahiaField> fields = theContainer.getFields();
        while (fields.hasNext()) {
            // gets the field
            JahiaField containerField = (JahiaField) fields.next();
            // ensures that the field points to its including container
            containerField.setctnid(theContainer.getID());
            // saves the field
            containerField.save(jParams);
        }
    } // end saveContainer

    /**
     * deletes the container info, fields and sublists
     * deletes the container info, fields and sublists
     *
     * @param ctnid   the container id
     * @param jParams the request parameters
     * @see org.jahia.data.containers.JahiaContainer
     *      DJ 29.01.01 - added ACL check
     */
    // DJ 29.01.01 - added ACL check
    public synchronized void deleteContainer(int ctnid, ProcessingContext jParams)
            throws JahiaException {
        // loads container info
        EntryLoadRequest loadRequest = new EntryLoadRequest(EntryLoadRequest.
                STAGING_WORKFLOW_STATE, 0,
                EntryLoadRequest.STAGED.getLocales(), true);
        JahiaContainer theContainer = loadContainer(ctnid, LoadFlags.NOTHING,
                jParams, loadRequest);

        // get current site save version
        JahiaSaveVersion saveVersion = ServicesRegistry.getInstance().
                getJahiaVersionService().
                getSiteSaveVersion(theContainer.
                        getJahiaID());

        // start check for correct rights.
        if (jParams != null) {
            JahiaUser currentUser = jParams.getUser();
            if (currentUser != null) {
                logger.debug("deleteContainer(): checking rights...");
                // if the user has no write rights, exit method.
                if (!theContainer.checkWriteAccess(currentUser)) {
                    logger.debug("deleteContainer(): NO write rights! -> don't delete");
                    return;
                }
                logger.debug("deleteContainer(): write rights OK");
            } else {
                throw new JahiaException("No user present !",
                        "No current user defined in the params in deleteContainer() method.",
                        JahiaException.USER_ERROR, JahiaException.ERROR_SEVERITY);
            }
        }
        // end check rights.

        // deletes container fields - even if no write rights in the fields
        Iterator<JahiaField> theFields = theContainer.getFields();

        while (theFields.hasNext()) {

            JahiaField theField = (JahiaField) theFields.next();
            ContentField contentField = ContentField.getField(theField.getID());
            for (String curLanguageCode : contentField.getLanguagesStates().keySet()) {
                /**
                 * todo FIXME normally we shouldn't create the activation
                 * context here but take it as a parameter in order to
                 * correctly detect loops, but this means that the
                 * deleteContainer signature would have to change.
                 */
                Set<String> languageCodes = new HashSet<String>();
                languageCodes.add(curLanguageCode);
                contentField.markLanguageForDeletion(jParams.getUser(),
                        curLanguageCode,
                        new StateModificationContext(
                                new ContentFieldKey(theField.getID()),
                                languageCodes, true));
            }
        }

        //deletes sub container lists - even if no write rights in the lists
        Iterator<JahiaContainerList> theLists = theContainer.getContainerLists();
        while (theLists.hasNext()) {
            JahiaContainerList theContainerList = (JahiaContainerList) theLists.
                    next();
            //logger.debug( "Deleting container list id " + theContainerList.getID() );
            deleteContainerList(theContainerList.getID(), jParams);
        }

        // deletes ACL
        /*        try {
                    // temporary we don't remove the ACL anymore, because of versioning & staging
                    // JahiaBaseACL theACL = new JahiaBaseACL (theContainer.getAclID());
                    // theACL.delete ();
                }
                catch (ACLNotFoundException ex) {
                    JahiaException je = new JahiaException ("", "Could not find the ACL ["+Integer.toString (theContainer.getAclID())+
             "] while removing field ["+Integer.toString(ctnid)+"]",
             JahiaException.ACL_ERROR, JahiaException.WARNING);
                }*/

        // deletes container info

        containerManager.deleteContainer(theContainer.getID(), saveVersion);

        // we only delete it from cache if it's not the staged version
        if (!saveVersion.isStaging()) {
//            containerInfoCache.remove(this.getCacheContainerOrContainerListActiveEntryKey(containerParam.intValue()));
//            containerInfoCache.remove(this.getCacheContainerOrContainerListStagingEntryKey(containerParam.intValue()));
            ContentContainer.invalidateContainerCache(theContainer.getID());
//        c_utils.invalidateCtnIdsByCtnListCache(theContainer.getListID());
        }

        ObjectKey key = new ContentContainerKey(ctnid);
        ContentObjectDeleteEvent jahiaEvent = new ContentObjectDeleteEvent(key, theContainer.getJahiaID(), jParams);
        ServicesRegistry.getInstance().getJahiaEventService().fireContentObjectDelete(jahiaEvent);

        // handled by previous event
        //ServicesRegistry.getInstance().getJahiaSearchService().removeContentObject(key);

    } // end deleteContainer

    public ActivationTestResults isContainerValidForActivation(Set<String> languageCodes,
                                                               int id, JahiaUser user, JahiaSaveVersion saveVersion,
                                                               ProcessingContext jParams,
                                                               StateModificationContext stateModifContext)
            throws JahiaException {
        ActivationTestResults activationResults = new ActivationTestResults();

        return activationResults;
    }

    public ActivationTestResults activateStagedContainer(Set<String> languageCodes,
                                                         int id, JahiaUser user, JahiaSaveVersion saveVersion,
                                                         ProcessingContext jParams, StateModificationContext stateModifContext)
            throws JahiaException {
        ContentContainer contentContainer = ContentContainer.getContainer(id);

        boolean stateModified = false;
        if (contentContainer.willBeCompletelyDeleted(null, languageCodes)) {
            stateModified = true;
            stateModifContext.pushAllLanguages(true);
        }

        Set<String> activateLanguageCodes = new HashSet<String>(languageCodes);
        if (stateModifContext.isAllLanguages()) {
            activateLanguageCodes.addAll(contentContainer.getStagingLanguages(true));
        }

        ActivationTestResults activationResults =
                isContainerValidForActivation(activateLanguageCodes,
                        id,
                        user,
                        saveVersion,
                        jParams,
                        stateModifContext);

        activationResults.merge(contentContainer.isPickedValidForActivation(languageCodes, stateModifContext));

        // fire event
        JahiaEvent theEvent = new JahiaEvent(saveVersion, jParams, contentContainer);
        ServicesRegistry.getInstance().getJahiaEventService()
                .fireBeforeContainerActivation(theEvent);

        containerManager.validateStagedContainer(id, saveVersion);
        logger.debug("Container " + id + " has just been validated.");

        RuleEvaluationContext ctx = new RuleEvaluationContext(contentContainer.getObjectKey(),
                contentContainer, jParams, user);
        ServicesRegistry.getInstance().getJahiaSearchService().indexContainer(id, user, false, true, ctx);

        // invalidate corresponding cache entry
        ContentContainer.invalidateContainerCache(id);

        if (stateModified) {
            stateModifContext.popAllLanguages();
        }

        return activationResults;

    }

    /**
     * Validate the containers of the page to which the user has admin AND write access
     * i.e. now Staged containers are Active.
     *
     * @param saveVersion it must contain the right versionID and staging/versioning
     *                    info of the current site
     * @return true if all the containers where successfully validated.
     */
    public ActivationTestResults activateStagedContainers(Set<String> languageCodes,
                                                          int pageID,
                                                          JahiaUser user,
                                                          JahiaSaveVersion saveVersion,
                                                          ProcessingContext jParams,
                                                          StateModificationContext stateModifContext)
            throws JahiaException {
        ActivationTestResults activationResults = new ActivationTestResults();

        activationResults.merge(areContainersValidForActivation(languageCodes,
                pageID, user, saveVersion, jParams, stateModifContext));
        if (activationResults.getStatus() ==
                ActivationTestResults.FAILED_OPERATION_STATUS) {
            return activationResults;
        }

        List<Integer> ctnIDs = new ArrayList<Integer>(containerManager.getStagedContainerInPage(pageID));
        // for each container, we check if the user has write+admin access to it,
        // if so we can validate it
        for (int id : ctnIDs) {
            // Added this check here because of Page Move issue
            // If this container contains a page currently moved but not
            // activated, we won't to activate this container
            ActivationTestResults curTestResults = isContainerValidForActivation(languageCodes, id, user, saveVersion, jParams,
                    stateModifContext);

            if (curTestResults.getStatus() == ActivationTestResults.FAILED_OPERATION_STATUS) {
                logger.debug("Container " + id + " is not valid for activation. testResults="
                        + curTestResults.toString());
                continue;
            }
            activationResults.merge(activateStagedContainer(languageCodes, id,
                    user, saveVersion, jParams, stateModifContext));
        }
        return activationResults;
    }

    public ActivationTestResults areContainersValidForActivation(Set<String> languageCodes,
                                                                 int pageID,
                                                                 JahiaUser user,
                                                                 JahiaSaveVersion saveVersion,
                                                                 ProcessingContext jParams,
                                                                 StateModificationContext stateModifContext)
            throws JahiaException {
        ActivationTestResults activationTestResults = new ActivationTestResults();
        List<Integer> ctnIDs = new ArrayList<Integer>(containerListManager.getOnlyStagedContainerIdsInPage(pageID));
        // for each container, we check if the user has write+admin access to it,
        // if so we can validate it
        for (int id : ctnIDs) {
            ActivationTestResults curTestResults = isContainerValidForActivation(languageCodes, id, user, saveVersion, jParams,
                    stateModifContext);

            if (curTestResults.getStatus() != ActivationTestResults.COMPLETED_OPERATION_STATUS) {
                logger.debug(
                        "Container " + id + " is not valid for activation. testResults=" + curTestResults.toString());
            }

            if (curTestResults.getStatus() == ActivationTestResults.FAILED_OPERATION_STATUS) {
                // a container may fail. It will simply not be validated, but the
                // total result for ALL the containers should be partial.
                curTestResults.setStatus(ActivationTestResults.PARTIAL_OPERATION_STATUS);
                curTestResults.moveErrorsToWarnings();
            }

            activationTestResults.merge(curTestResults);
        }
        return activationTestResults;

    }

    public List<Integer> getSubContainerListIDs(int containerID, EntryLoadRequest request) {
        return containerListManager.getSubContainerListIDs(containerID, request);
    }

    /**
     * loads a container list info without its containers
     * loads container list info, but not dependant fields and container lists
     * no rights check.
     *
     * @param containerListID the container list id
     * @see org.jahia.data.containers.JahiaContainerList
     */
    public JahiaContainerList loadContainerListInfo(int containerListID)
            throws JahiaException {
        return loadContainerListInfo(containerListID, EntryLoadRequest.CURRENT);
    } // end loadContainerListInfo

    public JahiaContainerList loadContainerListInfo(int containerListID,
                                                    EntryLoadRequest loadVersion)
            throws JahiaException {
        logger.debug("Loading containerList " + containerListID + " version=" +
                loadVersion);

        JahiaContainerList result = containerListManager.loadContainerList(containerListID, loadVersion);
        if (result != null) {
            return (JahiaContainerList) result.clone();
        }
        return result;
    } // end loadContainerListInfo

    /**
     * loads a container list
     * loads container list info and containers, but not dependant container lists
     * this method cannot load request-specific values (i.e. applications);
     * see the loadContainerList( containerListID, loadFlag, jParams) for that.
     * DO NOT CACHE THIS METHOD (it depends on other caches values) !!
     *
     * @param containerListID the container list id
     * @param loadFlag        the loadFlag
     * @see org.jahia.data.containers.JahiaContainerList
     * @see org.jahia.data.fields.LoadFlags
     */
    public JahiaContainerList loadContainerList(int containerListID,
                                                int loadFlag)
            throws JahiaException {
        return loadContainerList(containerListID, loadFlag, null);
    } // end loadContainerList

    /**
     * loads a container list
     * loads container list info and containers, but not dependant container lists
     * this method can load request-specific values (i.e. applications).
     * DO NOT CACHE THIS METHOD (it depends on other caches values) !!
     *
     * @param containerListID the container list id
     * @param loadFlag        the loadFlag
     * @param jParams         the ProcessingContext object, containing request and response
     * @see org.jahia.data.containers.JahiaContainerList
     * @see org.jahia.data.fields.LoadFlags
     *      DJ 29.01.01 - added ACL rights
     *      SH 19.01.02 - added Scrollable container support
     */
    // DJ 29.01.01 - added ACL rights
    // SH 19.01.02 - added Scrollable container support
    public JahiaContainerList loadContainerList(int containerListID,
            int loadFlag, ProcessingContext jParams) throws JahiaException {
        return loadContainerList(containerListID, loadFlag, jParams,
                jParams != null ? jParams.getEntryLoadRequest() : null,
                new HashMap<Integer, List<Integer>>(),
                new HashMap<Integer, List<Integer>>(),
                new HashMap<Integer, List<Integer>>());
    }

    public JahiaContainerList loadContainerList(int containerListID,
            int loadFlag, ProcessingContext jParams,
            EntryLoadRequest loadVersion) throws JahiaException {
        return loadContainerList(containerListID, loadFlag, jParams,
                loadVersion, new HashMap<Integer, List<Integer>>(),
                new HashMap<Integer, List<Integer>>(),
                new HashMap<Integer, List<Integer>>());
    }

    public JahiaContainerList loadContainerList(int containerListID,
                                                int loadFlag,
                                                ProcessingContext jParams,
                                                EntryLoadRequest loadVersion,
                                                Map<Integer, List<Integer>> cachedFieldsInContainer,
                                                Map<Integer, List<Integer>> cachedContainersFromContainerLists,
                                                Map<Integer, List<Integer>> cachedContainerListsFromContainers)
            throws JahiaException {

        logger.debug("Starting...[" + containerListID + "]");

        return loadContainerList(containerListID, null, loadFlag, jParams, loadVersion,
                cachedFieldsInContainer, cachedContainersFromContainerLists,
                cachedContainerListsFromContainers);
    }


    /**
     * Loads a set of containers for a given container list
     * Loads container list info and containers, but not dependant container lists
     * this method can load request-specific values (i.e. applications).
     * DO NOT CACHE THIS METHOD (it depends on other caches values) !!
     *
     * @param containerListID the container list id
     * @param ctnids          List of container ids to load. (not used)
     * @param loadFlag        the loadFlag
     * @param jParams         the ProcessingContext object, containing request and response
     * @see org.jahia.data.containers.JahiaContainerList
     * @see org.jahia.data.fields.LoadFlags
     */
    public JahiaContainerList loadContainerList(int containerListID,
                                                List<Integer> ctnids,
                                                int loadFlag,
                                                ProcessingContext jParams,
                                                EntryLoadRequest loadVersion,
                                                Map<Integer, List<Integer>> cachedFieldsInContainer,
                                                Map<Integer, List<Integer>> cachedContainersFromContainerLists,
                                                Map<Integer, List<Integer>> cachedContainerListsFromContainers)
            throws JahiaException {
        logger.debug("Starting for ctnlist [" + containerListID + "]");

        // loads container list info
        JahiaContainerList theContainerList = loadContainerListInfo(containerListID, loadVersion);

        if (theContainerList == null) {
            throw new JahiaException("Error while loading content",
                    "JahiaContainersBaseService.loadContainerList> couldn't find container list ID " +
                            Integer.toString(containerListID) + " in database",
                    JahiaException.USER_ERROR, JahiaException.ERROR_SEVERITY);
        }

        // start check for correct rights.
        if (jParams != null) { // no jParams, can't check for rights
            JahiaUser currentUser = jParams.getUser();
            if (currentUser != null) {
                logger.debug("loadContainerList(): checking rights...");
                // if the user has no read rights, return an empty list.
                if (!theContainerList.checkReadAccess(currentUser)) {
                    logger.debug("loadContainerList(): NO read rights! -> returning empty list");
                    theContainerList.setIsContainersLoaded(true);
                    return theContainerList;
                }
                logger.debug("loadContainerList(): read rights OK");

//                final SessionState session = jParams.getSessionState();
//                if (session != null) {
//                    if (session.getAttribute("getSorteredAndFilteredCtnIds" + containerListID) == null) {
//                        final List idsForSession = getctnidsInList(containerListID, loadVersion);
//                        session.setAttribute("getSorteredAndFilteredCtnIds" + containerListID, idsForSession);
//                    }
//                }

            } else {
                throw new JahiaException("No user present !",
                        "No current user defined in the params in loadContainerList() method.",
                        JahiaException.USER_ERROR, JahiaException.ERROR_SEVERITY);
            }
        }
        ContainerListFactoryProxy cListFactory =
                new ContainerListFactoryProxy(loadFlag,
                        jParams,
                        loadVersion,
                        cachedFieldsInContainer,
                        cachedContainersFromContainerLists,
                        cachedContainerListsFromContainers);

        theContainerList.setFactoryProxy(cListFactory);
        return theContainerList;

    } // end loadContainerList

    /**
     * saves a container list info
     * saves container list info, but not dependant fields and container lists
     * if id=0, attributes a new id to container list and creates it in datasource
     *
     * @param theContainerList a JahiaContainerList object
     * @param parentAclID      the Acl parent ID
     * @see org.jahia.data.containers.JahiaContainerList
     *      EV          26.02.2001          added parentAclID
     * @deprecated use the method with the ProcessingContext
     */
    //      EV          26.02.2001          added parentAclID
    public void saveContainerListInfo(JahiaContainerList
            theContainerList, int parentAclID)
            throws JahiaException {
        saveContainerListInfo(theContainerList, parentAclID, null);
    } // end saveContainerListInfo

    /**
     * saves a container list info
     * saves container list info, but not dependant fields and container lists
     * if id=0, attributes a new id to container list and creates it in datasource
     *
     * @param theContainerList a JahiaContainerList object
     * @param parentAclID      the Acl parent ID
     * @see org.jahia.data.containers.JahiaContainerList
     *      EV          26.02.2001          added parentAclID
     */
    //      EV          26.02.2001          added parentAclID
    public void saveContainerListInfo(JahiaContainerList
            theContainerList, int parentAclID, ProcessingContext jParams)
            throws JahiaException {
        if (theContainerList.getAclID() == 0) {
            // Start Create a new ACL object for the new container.
            theContainerList.setAclID(parentAclID);
        }
        // to get the JahiaSaveVersion, we need to load the page info at 1st, to know to what site the
        // container list belongs to.
        // throws a JahiaException if the page can't be found
        ContentPage thePage = ServicesRegistry.getInstance().
                getJahiaPageService().lookupContentPage(theContainerList.getPageID(), true);
        JahiaSaveVersion saveVersion = ServicesRegistry.getInstance().
                getJahiaVersionService().
                getSiteSaveVersion(thePage.getJahiaID());
        boolean isNew = false;
        if (theContainerList.getID() == 0) {
            isNew = true;
            containerListManager.createContainerList(theContainerList, saveVersion, thePage.getSiteID());
            this.containerDefinitionManager.invalidateContainerDefinitionInTemplate(thePage.getPageTemplate(jParams).getID());
            if (theContainerList.getID() > 0) {
                theContainerList.getContentContainerList();
                ContentContainerList cList = ContentContainerList.getContainerList(theContainerList.getID());
                JahiaEvent objectCreatedEvent = new JahiaEvent(this, jParams, cList);
                ServicesRegistry.getInstance().getJahiaEventService().fireContentObjectCreated(objectCreatedEvent);
            }
        } else {
            containerListManager.updateContainerList(theContainerList, saveVersion);
            ContentContainerList cList = ContentContainerList.getContainerList(theContainerList.getID());
            JahiaEvent objectCreatedEvent = new JahiaEvent(this, jParams, cList);
            ServicesRegistry.getInstance().getJahiaEventService().fireContentObjectUpdated(objectCreatedEvent);
        }

        invalidateContainerListFromCache(theContainerList.getID());

//        JahiaContainerUtilsDB.getInstance().invalidateCtnIdsByCtnListCache(theContainerList.getID());
//        JahiaContainerUtilsDB.getInstance().invalidateSubCtnListIDsByCtnCache(theContainerList.getParentEntryID());

        if (jParams != null) {
            WorkflowEvent theEvent = new WorkflowEvent(this, theContainerList.getContentContainerList(), jParams.getUser(),
                    ContentField.SHARED_LANGUAGE, false);
            theEvent.setNew(isNew);
            ServicesRegistry.getInstance().getJahiaEventService().fireObjectChanged(theEvent);
        }

    } // end saveContainerListInfo

    /**
     * deletes a container list info
     * deletes a container list info, but not dependant containers
     * see deleteContainerList for that
     *
     * @param listID the container list id to delete
     */
    private synchronized void deleteContainerListInfo(int listID,
                                                      JahiaSaveVersion saveVersion)
            throws JahiaException {

//        ObjectKey key = ContentContainerListKey.getChildInstance(String.valueOf(listID));
        containerListManager.deleteContainerList(listID, saveVersion);

        // handled by previous event
        //ServicesRegistry.getInstance().getJahiaSearchService().removeContentObject(key);


        this.invalidateContainerListFromCache(listID);
    } // end deleteContainerListInfo

    /**
     * deletes a container list info
     * deletes a container list info, including dependant containers
     * see deleteContainerList for that
     *
     * @param listID the container list id to delete
     */
    public synchronized void deleteContainerList(int listID, ProcessingContext jParams)
            throws JahiaException {
        // No ACL check implemented, because when you delete a page you want to
        // delete every containerlist / container it contains, even if you don't have
        // the specific rights to do so.

        // gets list object
        JahiaContainerList theList = loadContainerListInfo(listID,
                EntryLoadRequest.STAGED);

        // to get the JahiaSaveVersion, we need to load the page info at 1st, to know to what site the
        // container list belongs to.
        // throws a JahiaException if the page can't be found
        ContentPage thePage = ServicesRegistry.getInstance().
                getJahiaPageService().lookupContentPage(theList.getPageID(), true);
        JahiaSaveVersion saveVersion = ServicesRegistry.getInstance().
                getJahiaVersionService().
                getSiteSaveVersion(thePage.getJahiaID());
        EntryLoadRequest loadVersion = ServicesRegistry.getInstance().
                getJahiaVersionService().
                isStagingEnabled(thePage.getJahiaID()) ?
                EntryLoadRequest.STAGED :
                EntryLoadRequest.CURRENT;

        // deletes list info
        deleteContainerListInfo(listID, saveVersion);

//        containerListInfoCache.remove(getCacheContainerOrContainerListStagingEntryKey(listID));

        // deletes all containers of container list
        List<Integer> cIDs = getctnidsInList(listID, loadVersion);
        for (int i = 0; i < cIDs.size(); i++) {
            int ctnid = ((Integer) cIDs.get(i)).intValue();
            deleteContainer(ctnid, jParams);
        }

        ObjectKey key = new ContentContainerListKey(listID);
        ContentObjectDeleteEvent jahiaEvent = new ContentObjectDeleteEvent(key, theList.getDefinition().getJahiaID(),
                jParams);
        ServicesRegistry.getInstance().getJahiaEventService().fireContentObjectDelete(jahiaEvent);

//        JahiaContainerUtilsDB.getInstance().invalidateSubCtnListIDsByCtnCache(theList.getParentEntryID());

        // deletes ACL (remporary removed because of versioning
        /*        try {
                    if (theList != null) {
             JahiaBaseACL theACL = new JahiaBaseACL (theList.getAclID());
                        theACL.delete ();
                    }
                }
                catch (ACLNotFoundException ex) {
                    JahiaException je = new JahiaException ("", "Could not find the ACL ["+Integer.toString (theList.getAclID())+
             "] while removing field ["+Integer.toString(listID)+"]",
             JahiaException.ACL_ERROR, JahiaException.WARNING);
                }*/
    } // end deleteContainerList

    /**
     * Validate the container lists of the page to which the user has admin AND write access
     * i.e. now Staged containers are Active.
     *
     * @param saveVersion it must contain the right versionID and staging/versioning
     *                    info of the current site
     */
    public ActivationTestResults activateStagedContainerLists(Set<String> languageCodes,
                                                              int pageID, JahiaUser user,
                                                              JahiaSaveVersion saveVersion,
                                                              StateModificationContext stateModifContext)
            throws JahiaException {
        ActivationTestResults activationResults = new ActivationTestResults();


        activationResults.merge(areContainerListsValidForActivation(languageCodes, pageID, user, saveVersion,
                stateModifContext));
        if (activationResults.getStatus() ==
                ActivationTestResults.FAILED_OPERATION_STATUS) {
            return activationResults;
        }

        // for each container, we check if the user has write+admin access to it,
        // if so we can validate it
        for (int id : containerListManager.getOnlyStagedContainerIdsInPage(pageID)) {
            JahiaContainerList theContainerList = loadContainerListInfo(id,
                    EntryLoadRequest.STAGED);
            if (theContainerList.checkAdminAccess(user) &&
                    theContainerList.checkWriteAccess(user)) {
                // yes user has access, we can validate this containerlist
                containerListManager.validateStagedContainerList(id, saveVersion);

                ServicesRegistry.getInstance().getJahiaSearchService().indexContainerList(id, user);

                // update cache
//                containerListInfoCache.remove(getCacheContainerOrContainerListStagingEntryKey(id));
//                JahiaContainerUtilsDB.getInstance().invalidateSubCtnListIDsByCtnCache(theContainerList.getParentEntryID());

                logger.debug("VALIDATION CONTAINER LIST #" + id);
            }
        }
        return activationResults;
    }

    /**
     * @param languageCodes
     * @param containerListID
     * @param user
     * @param saveVersion
     * @param jParams
     * @param stateModifContext
     * @return
     * @throws JahiaException
     */
    public ActivationTestResults activateStagedContainerList(Set<String> languageCodes,
                                                             int containerListID, JahiaUser user,
                                                             JahiaSaveVersion saveVersion,
                                                             ProcessingContext jParams, StateModificationContext stateModifContext)
            throws JahiaException {

        ActivationTestResults activationResults = new ActivationTestResults();


        activationResults.merge(isContainerListValidForActivation(languageCodes, containerListID, user, saveVersion,
                stateModifContext));
        activationResults.merge(
                ContentContainerList.getContainerList(containerListID).isPickedValidForActivation(languageCodes, stateModifContext));

        if (activationResults.getStatus() ==
                ActivationTestResults.FAILED_OPERATION_STATUS) {
            return activationResults;
        }
        containerListManager.validateStagedContainerList(containerListID, saveVersion);

        ServicesRegistry.getInstance().getJahiaSearchService().indexContainerList(containerListID, user);

        logger.debug("VALIDATION CONTAINER LIST #" + containerListID);
        return activationResults;
    }


    public ActivationTestResults areContainerListsValidForActivation(Set<String> languageCodes,
                                                                     int pageID,
                                                                     JahiaUser user,
                                                                     JahiaSaveVersion saveVersion,
                                                                     StateModificationContext stateModifContext)
            throws JahiaException {
        // containers lists are for the moment always ready for activation
        return new ActivationTestResults();
    }

    public ActivationTestResults isContainerListValidForActivation(Set<String> languageCodes,
                                                                   int containerListID,
                                                                   JahiaUser user,
                                                                   JahiaSaveVersion saveVersion,
                                                                   StateModificationContext stateModifContext)
            throws JahiaException {
        // containers lists are for the moment always ready for activation
        return new ActivationTestResults();
    }


    /**
     * loads a container definition by its id
     *
     * @param definitionID the container definition id
     * @return a JahiaContainerDefinition object
     * @see org.jahia.data.containers.JahiaContainerDefinition
     * @see org.jahia.data.containers.JahiaContainerStructure
     */
    public JahiaContainerDefinition loadContainerDefinition(int definitionID)
            throws JahiaException {
        JahiaContainerDefinition result = containerDefinitionManager.loadContainerDefinition(definitionID);
        return result;
    } // end fullyLoadContainerDefinition

    /**
     * Load a container definition by it's site ID and it's definition name
     *
     * @param siteID         the site identifier on which to retrieve the definition
     * @param definitionName the unique name for the definition
     * @return a JahiaContainerDefinition if found
     * @throws JahiaException in case there was a problem communicating with
     *                        the database
     */
    public JahiaContainerDefinition loadContainerDefinition(int siteID, String definitionName)
            throws JahiaException {
        JahiaContainerDefinition result = containerDefinitionManager.loadContainerDefinition(siteID, definitionName);
        return result;
    } // end fullyLoadContainerDefinition


    /**
     * saves a container definition
     * if id=0, assigns a new id to the definition and creates it in the datasource
     *
     * @param theDefinition the JahiaContainerDefinition to save
     * @see org.jahia.data.containers.JahiaContainerDefinition
     * @see org.jahia.data.containers.JahiaContainerStructure
     */
    public synchronized void saveContainerDefinition(JahiaContainerDefinition
            theDefinition)
            throws JahiaException {
        if (theDefinition.getID() == 0) {
            containerDefinitionManager.createContainerDefinition(theDefinition);
        } else {
            containerDefinitionManager.updateContainerDefinition(theDefinition);
        }
    } // end saveContainerDefinition

    /**
     * deletes a container definition
     *
     * @param definitionID the JahiaContainerDefinition to delete
     * @see org.jahia.data.containers.JahiaContainerDefinition
     * @see org.jahia.data.containers.JahiaContainerStructure
     */
    public synchronized void deleteContainerDefinition(int definitionID)
            throws JahiaException {
        containerDefinitionManager.deleteContainerDefinition(definitionID);
        JahiaContainerDefinitionsRegistry.getInstance().removeContainerDefinition(definitionID);

    } // end deleteContainerDefinition


    public synchronized List<JahiaContainerDefinition> loadContainerDefinitionInTemplate(int templateID) {
        return containerDefinitionManager.loadContainerDefinitionInTemplate(templateID);
    }

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all containers of a site
     *
     * @param siteID the site identification number
     */
    public JahiaDOMObject getContainersAsDOM(int siteID)
            throws JahiaException {

//        return c_containers.getContainersAsDOM (siteID);
        return null;
    }

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all container lists of a site
     *
     * @param siteID
     */
    public JahiaDOMObject getContainerListsAsDOM(int siteID)
            throws JahiaException {

//        return c_lists.getContainerListsAsDOM(siteID);
        return null;
    }

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all container lists props of a site
     *
     * @param siteID the site identification number
     */
    public JahiaDOMObject getContainerListPropsAsDOM(int siteID)
            throws JahiaException {

//        return c_lists_props.getPropertiesAsDOM(siteID);
        return null;
    }

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all container def of a site
     *
     * @param siteID the site identification number
     */
    public JahiaDOMObject getContainerDefsAsDOM(int siteID)
            throws JahiaException {

//        return c_defs.getContainerDefsAsDOM(siteID);
        return null;
    }

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all container def prop of a site
     *
     * @param siteID the site identification number
     */
    public JahiaDOMObject getContainerDefPropsAsDOM(int siteID)
            throws JahiaException {

//        return c_defs.getContainerDefPropsAsDOM(siteID);
        return null;
    }

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all containers extented properties of a site
     *
     * @param siteID the site identification number
     */
    public JahiaDOMObject getContainerExtendedPropsAsDOM(int siteID)
            throws JahiaException {
//        return c_defs_props.getPropertiesAsDOM(siteID);
        return null;
    }

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all container structure of a site
     *
     * @param siteID the site identification number
     */
    public JahiaDOMObject getContainerStructsAsDOM(int siteID)
            throws JahiaException {
        return null;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns a List of all Acl ID used by container for a site
     * Used for site extraction
     *
     * @param siteID the site identification number
     */
    public List<Integer> getAclIDs(int siteID)
            throws JahiaException {

        return new ArrayList<Integer>(aclManager.findAllContainerAclsIdInSite(siteID));

    }

    //--------------------------------------------------------------------------
    /**
     * Returns a List of all ctn lists fields ( field def ) Acl ID for a site
     * Used for site extraction
     *
     * @param siteID the site identification number
     */
    public List<Integer> getCtnListFieldAclIDs(int siteID)
            throws JahiaException {

//        return c_lists_props.getCtnListFieldACLs(siteID);
        return null;
    }

    public SortedSet<Integer> getAllPageTopLevelContainerListIDs(int pageID,
                                                        EntryLoadRequest loadRequest)
            throws JahiaException {
        return new TreeSet<Integer>(containerListManager.getPageTopLevelContainerListIDs(pageID, loadRequest));
    }

    public SortedSet<ContentObjectEntryState> getContainerListInPageEntryStates(int pageID)
            throws JahiaException {
        SortedSet<ContentObjectEntryState> entryStates = new TreeSet<ContentObjectEntryState>();
        Set<Integer> containerListIDs = new TreeSet<Integer>(containerListManager.getAllPageTopLevelContainerListIDs(pageID));
        for (Integer curContainerListID : containerListIDs) {
            entryStates.addAll(getContainerListEntryStates(curContainerListID.
                    intValue()));
        }
        return entryStates;
    }

    private SortedSet<ContentObjectEntryState> getContainerListEntryStates(int listID)
            throws JahiaException {
        SortedSet<ContentObjectEntryState> entryStates = new TreeSet<ContentObjectEntryState>();
        /**
         * todo FIXME the following will not work if we request a listID of
         * a versioned container list. We must make this work for any workflow
         * state.
         */

//        JahiaContainerList theList = null;
//        EntryLoadRequest loadRequest = EntryLoadRequest.STAGED;
//        try {
//            theList = loadContainerListInfo(listID, loadRequest);
//        } catch (JahiaException je) {
//            // error while loading the container in staged mode, let's try to
//            // load it from a deleted entry state.
//            loadRequest = EntryLoadRequest.DELETED;
//            // try to load once again
//            theList = loadContainerListInfo(listID, loadRequest);
//        }

        List<Integer> containerIDs = containerManager.getContainerIdsInContainerList(listID, null, false);
        for (int ctnid : containerIDs) {
            entryStates.addAll(getContainerEntryStates(ctnid));
        }

        return entryStates;
    }

    private SortedSet<ContentObjectEntryState> getContainerEntryStates(int id)
            throws JahiaException {
        SortedSet<ContentObjectEntryState> entryStates = new TreeSet<ContentObjectEntryState>();

        /**
         * todo FIXME the following will not work if we request a ID of
         * a versioned container. We must make this work for any workflow
         * state.
         */
        JahiaContainer theContainer = null;
        EntryLoadRequest loadRequest = EntryLoadRequest.STAGED;
        try {
            theContainer = loadContainerInfo(id, loadRequest);
        } catch (JahiaException je) {
            // error while loading the container in staged mode, let's try to
            // load it from a deleted entry state.
            loadRequest = EntryLoadRequest.DELETED;
            // try to load once again
            theContainer = loadContainerInfo(id, loadRequest);
        }

        // we must now check to see if this container has fields that
        // don't exist in an active version.

        // we might want to cache the next field id retrieval code ?
        List<Integer> fieldIDs = fieldsDataManager.getFieldsIdsInContainer(id, null);
        for (int fieldID : fieldIDs) {
            ContentField currentField = ContentField.getField(fieldID);
            entryStates.addAll(currentField.getEntryStates());
        }

        // now let's check that the case of subcontainer lists. If they cannot
        // be marked for deletion, neither can this container.
        Iterator<JahiaContainerList> subContainerListsEnum = theContainer.getContainerLists();
        while (subContainerListsEnum.hasNext()) {
            JahiaContainerList theContainerList = 
                    subContainerListsEnum.
                            next();
            entryStates.addAll(getContainerListEntryStates(theContainerList.
                    getID()));
        }

        return entryStates;
    }

    public boolean markPageContainerListsLanguageForDeletion(int pageID,
                                                             JahiaUser user,
                                                             String languageCode,
                                                             StateModificationContext stateModifContext)
            throws JahiaException {
        Collection<Integer> containerIDs = containerListManager.getAllPageContainerListIDs(pageID);
        boolean allDeleted = true;
        for (Integer curContainerListID : containerIDs) {
            boolean succeeded = markContainerListLanguageForDeletion(curContainerListID.
                    intValue(), user, languageCode, stateModifContext);
            if (!succeeded) {
                allDeleted = false;
            }
        }
        return allDeleted;
    }

    public synchronized boolean markContainerListLanguageForDeletion(int listID, JahiaUser user,
                                                                     String languageCode,
                                                                     StateModificationContext
                                                                             stateModifContext)
            throws JahiaException {

        // gets list object
        JahiaContainerList theList = loadContainerListInfo(listID,
                EntryLoadRequest.STAGED);
        if (theList == null) return true; //list already deleted or does not exist so why continue
        ContentContainerList contentContainerList = theList.getContentContainerList();

        /**
         * NK:
         * Testing if the container list will be completely deleted has no meaning,
         * because it is SHARED in nature.
         * Instead, we should state that :
         *       a container list will be completely deleted only if its parent Page
         *       is going to be completely deleted
         */
        boolean stateModified = false;
        if (contentContainerList.getParentContainerID() != 0 || contentContainerList.getPageID() != 0) {
            if (contentContainerList.willBeCompletelyDeleted(languageCode, null)) {
                stateModified = true;
                stateModifContext.pushAllLanguages(true);
            }
        }

        // mark all containers in this list for deletion if possible, otherwise
        // abort as soon as possible.
        boolean allDeleted = false;
        List<Locale> locales = new ArrayList<Locale>();
        locales.add(org.jahia.utils.LanguageCodeConverters.languageCodeToLocale(languageCode));
        EntryLoadRequest loadRequest = new EntryLoadRequest(EntryLoadRequest.
                STAGING_WORKFLOW_STATE, 0, locales, true);
        for (int ctnid : getctnidsInList(listID, loadRequest)) {
            boolean succeeded = markContainerLanguageForDeletion(ctnid, user,
                    languageCode, stateModifContext);
            if (!succeeded) {
                allDeleted = false;
            }
        }

        if (contentContainerList.willAllChildsBeCompletelyDeleted(user, languageCode, null)) {

            JahiaEvent theEvent = new JahiaEvent(this, null, contentContainerList);
            ServicesRegistry.getInstance().getJahiaEventService().fireBeforeStagingContentIsDeleted(theEvent);

            JahiaSaveVersion saveVersion = new JahiaSaveVersion(true, true);
            containerListManager.deleteContainerList(listID, saveVersion);
            allDeleted = true;
        }

        if (stateModified) {
            stateModifContext.popAllLanguages();
        }

        this.invalidateContainerListFromCache(listID);

        ServicesRegistry.getInstance().getJahiaSearchService().indexContainerList(listID, user);

        WorkflowEvent theEvent = new WorkflowEvent(this, contentContainerList, user, languageCode, true);
        ServicesRegistry.getInstance().getJahiaEventService().fireObjectChanged(theEvent);

        return allDeleted;
    }

    public boolean markContainerLanguageForDeletion(int id, JahiaUser user, String languageCode,
                                                    StateModificationContext stateModifContext)
            throws JahiaException {
        ContentContainer contentContainer = ContentContainer.getContainer(id);

        if (contentContainer == null) {
            return false;
        }


//        int siteId = contentContainer.getSiteID();
        /**
         * NK:
         * Testing if the container will be completely deleted has no meaning,
         * because it is SHARED in nature.
         * Instead, we should state that :
         *       a container will be completely deleted only if its container list
         *       ( that is the parent page ) is going to be completely deleted
         */
        boolean stateModified = false;
        if (contentContainer.willBeCompletelyDeleted(languageCode, null)) {
            stateModified = true;
            stateModifContext.pushAllLanguages(true);
        }

//        ContentContainer originalContentContainer = contentContainer;

        // start check for correct rights.
        if (user != null) {
            logger.debug("checking rights...");
            // if the user has no write rights, exit method.
            if (!contentContainer.checkWriteAccess(user)) {
                logger.debug("NO write rights! -> don't delete");
                if (stateModified) {
                    stateModifContext.popAllLanguages();
                }
                return false;
            }
            logger.debug("write rights OK");
        } else {
            throw new JahiaException("No user present !",
                    "No current user defined in the params in deleteContainer() method.",
                    JahiaException.USER_ERROR, JahiaException.ERROR_SEVERITY);
        }
        // we must now check to see if this container has fields that
        // don't exist in an active version.

        // we might want to cache the next field id retrieval code ?
        List<Locale> locales = new ArrayList<Locale>();
        locales.add(org.jahia.utils.LanguageCodeConverters.languageCodeToLocale(languageCode));
        EntryLoadRequest loadRequest = new EntryLoadRequest(EntryLoadRequest.
                STAGING_WORKFLOW_STATE, 0, locales, true);

        // now let's check that the case of subcontainer lists. If they cannot
        // be marked for deletion, neither can this container.
        for (ContentObject curChild : contentContainer.getChilds(user, loadRequest)) {
            if (curChild instanceof ContentContainerList) {
                ContentContainerList subContainerList = (ContentContainerList) curChild;
                subContainerList.markLanguageForDeletion(user, languageCode, stateModifContext);
            } else if (curChild instanceof ContentField) {
                ContentField currentField = (ContentField) curChild;
                currentField.markLanguageForDeletion(user, languageCode, stateModifContext);
            }
        }

        // We only delete the container if all its lang are going to be deleted
        // and no childs exist
        if (stateModified && stateModifContext.isAllLanguages()
                && contentContainer.willAllChildsBeCompletelyDeleted(user,
                languageCode, null)) {

            JahiaEvent theEvent = new JahiaEvent(this, null, contentContainer);
            ServicesRegistry.getInstance().getJahiaEventService().fireBeforeStagingContentIsDeleted(theEvent);

            // in case it is really deleted
            RuleEvaluationContext ctx = new RuleEvaluationContext(contentContainer.getObjectKey(),
                    contentContainer, Jahia.getThreadParamBean(), user);
            if (!contentContainer.hasActiveEntries()){
                ServicesRegistry.getInstance().getJahiaSearchService().removeContentObject(contentContainer, user, ctx);
            }
            JahiaSaveVersion saveVersion = new JahiaSaveVersion(true, true);
            logger.debug("delete container " + id);
            containerManager.deleteContainer(contentContainer.getID(), saveVersion);
            ContentContainer.invalidateContainerCache(contentContainer.getID());
            contentContainer = ContentContainer.getContainer(contentContainer.getID());
            if (contentContainer != null){
                ServicesRegistry.getInstance().getJahiaSearchService().indexContainer(contentContainer.getID(),
                        user, ctx);
            }
        } else {
            // we have to create a staged entry to be able to active mark for deleted on this container's fields
            if (contentContainer.getStagingLanguages(false, true).isEmpty()) {
                // if no staging or nor marked for delete exists
                Iterator<ContentObjectEntryState> iterator = contentContainer
                        .getActiveAndStagingEntryStates().iterator();
                if (iterator.hasNext()) {
                    ContentObjectEntryState fromEntryState =
                            iterator.next();
                    ContentObjectEntryState toEntryState =
                            new ContentObjectEntryState(ContentObjectEntryState
                                    .WORKFLOW_STATE_START_STAGING, 0,
                                    ContentObject.SHARED_LANGUAGE);
                    contentContainer.copyEntry(fromEntryState, toEntryState);
                    ContentContainer.invalidateContainerCache(contentContainer.getID());
                }
            }
        }
        if (stateModified) {
            stateModifContext.popAllLanguages();
        }

        // handled by the event bellow
        if (contentContainer != null) {
            WorkflowEvent theEvent = new WorkflowEvent(this, contentContainer, user, languageCode, true);
            ServicesRegistry.getInstance().getJahiaEventService().fireObjectChanged(theEvent);
        }

        return true;
    }

    public void purgeContainer(int id)
            throws JahiaException {

        /**
         * todo FIXME the following will not work if we request a ID of
         * a versioned container. We must make this work for any workflow
         * state.
         */
        JahiaContainer theContainer = null;
        EntryLoadRequest loadRequest = EntryLoadRequest.STAGED;
        try {
            theContainer = loadContainerInfo(id, loadRequest);
        } catch (JahiaException je) {
            // error while loading the container in staged mode, let's try to
            // load it from a deleted entry state.
            loadRequest = EntryLoadRequest.DELETED;
            // try to load once again
            theContainer = loadContainerInfo(id, loadRequest);
        }

        // we must now check to see if this container has fields that
        // don't exist in an active version.

        // we might want to cache the next field id retrieval code ?
        for (int fieldID : fieldsDataManager.getFieldsIdsInContainer(id, null)) {
            ContentField currentField = ContentField.getField(fieldID);
            currentField.purge();
        }

        // now let's check that the case of subcontainer lists. If they cannot
        // be marked for deletion, neither can this container.
        Iterator<JahiaContainerList> subContainerListsEnum = theContainer.getContainerLists();
        while (subContainerListsEnum.hasNext()) {
            JahiaContainerList theContainerList = 
                    subContainerListsEnum.
                            next();
            purgeContainerList(theContainerList.getID());
        }

        // if we got here it means that the container is ready to be deleted
        // so we can mark it to be deleted.

        // we should now mark the sub container lists too...

        JahiaBaseACL acl = theContainer.getACL();
        if (!theContainer.getContentContainer().isAclSameAsParent()) {
            acl.delete();
        }

        containerManager.purgeContainer(theContainer.getID());
        // we only delete it from cache if it's not the staged version
//        containerInfoCache.remove(this.getCacheContainerOrContainerListActiveEntryKey(containerIDInt.intValue()));
//        containerInfoCache.remove(this.getCacheContainerOrContainerListStagingEntryKey(containerIDInt.intValue()));
        ContentContainer.invalidateContainerCache(theContainer.getID());
//        this.c_utils.invalidateCtnIdsByCtnListCache(theContainer.getListID());
    }

    public void purgeContainerList(int listID)
            throws JahiaException {
        // gets list object
        /**
         * todo FIXME the following will not work if we request a listID of
         * a versioned container list. We must make this work for any workflow
         * state.
         */
        JahiaContainerList theList = loadContainerListInfo(listID,
                EntryLoadRequest.STAGED);

        // mark all containers in this list for deletion if possible, otherwise
        // abort as soon as possible.
        for (int ctnid : containerManager.getContainerIdsInContainerList(listID, null, false)) {
            purgeContainer(ctnid);
        }

        JahiaBaseACL acl = theList.getACL();
        if (!theList.getContentContainerList().isAclSameAsParent()) {
            acl.delete();
        }

        // we must now remove all the field def ACL properties.
        ContentPage sourceContentPage = ServicesRegistry.getInstance().
                getJahiaPageService().
                lookupContentPage(theList.
                        getPageID(), true);
        if (sourceContentPage != null) {
            Iterator<JahiaContainerStructure> structure = theList.getDefinition().getStructure(
                    JahiaContainerStructure.JAHIA_FIELD);
            while (structure.hasNext()) {
                JahiaContainerStructure theStruct =
                        structure.next();
                JahiaFieldDefinition theDef =
                        (JahiaFieldDefinition) theStruct.getObjectDef();
                String val = theList.getProperty("view_field_acl_" +
                        theDef.getName());
                if (val != null) {
                    try {
                        int aclID = Integer.parseInt(val);
                        JahiaBaseACL theACL = null;
                        try {
                            theACL = new JahiaBaseACL(aclID);
                            theACL.delete();
                        } catch (ACLNotFoundException ex) {
                            logger.debug("Error loading ACL", ex);
                        } catch (JahiaException ex) {
                            logger.debug("Error loading ACL", ex);
                        }
                    } catch (Exception t) {
                    }
                }
            }
        }

        // now let's delete the database related data.
        containerListManager.purgeContainerList(listID);

        this.invalidateContainerListFromCache(listID);
    }

    public void purgePageContainerLists(int pageID)
            throws JahiaException {
        Set<Integer> containerListIDs = new TreeSet<Integer>(containerListManager.getAllPageTopLevelContainerListIDs(pageID));

        for (Integer curContainerListID : containerListIDs) {
            purgeContainerList(curContainerListID.intValue());
        }
    }

    /**
     * Returns true if container-definition is a sub-definition
     *
     * @param containerDefinitionID an integer specifying the container
     *                              definition for which to find the parent sub definitions.
     * @return true if container definition is a sub-definition
     * @throws JahiaException if there was an error while communicating with the
     *                        database
     */
    public boolean hasContainerDefinitionParents(int containerDefinitionID)
            throws JahiaException {
        return containerStructureManager.hasContainerDefinitionParents(containerDefinitionID);
    }

    /**
     * Retrieves all the container list IDs in a site that correspond to the
     * passed name. Note that the structure of these lists may be different
     * because they may have originated in different templates !
     *
     * @param siteID      the site for which to retrieve the container lists by name
     * @param name        the name (definition name) for which to retrieve the
     *                    container lists in the site.
     * @param loadRequest the EntryLoadRequest for which to retrieve the
     *                    container list IDs. Maybe null if we want to get absolutely EVERY
     *                    container list regardless of status/version, etc...
     * @return a SortedSet containing Integer that are subContainerDefinitionIDs
     *         that contain the container definition we are looking for.
     * @throws JahiaException if there was an error while communicating with the
     *                        database
     */
    public SortedSet<Integer> getSiteTopLevelContainerListsIDsByName(int siteID,
                                                            String name,
                                                            EntryLoadRequest loadRequest)
            throws JahiaException {
        JahiaContainerDefinition jahiaContainerDefinition = containerDefinitionManager.loadContainerDefinition(siteID,
                name);
        if (jahiaContainerDefinition != null) {
            return new TreeSet<Integer>(containerListManager.getTopLevelContainerListIDsByDefinitionID(
                    jahiaContainerDefinition.getID(), loadRequest));
        } else {
            return new TreeSet<Integer>();
        }
    }

    /**
     * Retrieves all the properties for a given container
     *
     * @param containerID the identifier of the container whose properties
     *                    we want to retrieve from the database
     * @return a Properties object that contains all the properties that are
     *         available for this container in the database
     * @throws JahiaException generated if there were problems executing the
     *                        query or communicating with the database.
     */
    public Map<Object, Object> getContainerProperties(int containerID)
            throws JahiaException {
        return containerManager.getProperties(containerID);
    }

    public Map<Object, Object> getContainerListProperties(int containerListID)
            throws JahiaException {
        return containerListManager.getProperties(containerListID);
    }

    /**
     * Saves a whole set of properties in the database for the specified
     * container.
     *
     * @param containerID         the container whose properties we are serializing
     *                            in the database.
     * @param jahiaID             the site id
     * @param containerProperties the Properties object that contains all
     *                            the properties to save in the database. Only what is passed here will
     *                            exist in the database. If the database contained properties that aren't
     *                            in this Map they will be deleted.
     * @throws JahiaException generated if there were problems executing the
     *                        query or communicating with the database.
     */
    public void setContainerProperties(int containerID,
                                       int jahiaID,
                                       Map<Object, Object> containerProperties)
            throws JahiaException {
        containerManager.setProperties(containerID, jahiaID, containerProperties);
    }

    public void setContainerListProperties(int containerListID,
                                           int jahiaID,
                                           Map<Object, Object> containerProperties)
            throws JahiaException {
        containerListManager.setProperties(containerListID, jahiaID, containerProperties);
    }

    /**
     * Removes all container infos from the cache if it was present in the cache. If not,
     * this does nothing.
     *
     * @param containerID the identifier for the container to try to remove from the
     *                    cache.
     */
    public void invalidateContainerFromCache(int containerID) {
//        synchronized (containerInfoCache) {
//            containerInfoCache.remove(getCacheContainerOrContainerListActiveEntryKey(containerID));
//            containerInfoCache.remove(getCacheContainerOrContainerListStagingEntryKey(containerID));
//        }
    }

    /**
     * Removes all container list from the cache if it was present in the cache. If not,
     * this does nothing.
     *
     * @param containerListID the identifier for the container list to try to remove from the
     *                        cache.
     */
    public void invalidateContainerListFromCache(int containerListID) {
//        synchronized (containerInfoCache) {
//            containerListInfoCache.remove(getCacheContainerOrContainerListActiveEntryKey(containerListID));
//            containerListInfoCache.remove(getCacheContainerOrContainerListStagingEntryKey(containerListID));
//        }
    }

    public int getContainerListID(String containerListName, int pageID, int containerParentID) throws JahiaException {
        if (containerParentID == 0)
            return getContainerListID(containerListName, pageID);
        return containerListManager.getIdByPageIdAndDefinitionNameAndParentId(containerListName, pageID, containerParentID);
    }

    public void saveContainerRankInfo(JahiaContainer c, ProcessingContext jParams) throws JahiaException {
        if (c.isChanged()) {
            // container already exists -> just need to update
            JahiaContainer tmpContainer = (JahiaContainer) c.clone();
            JahiaSaveVersion saveVersion = ServicesRegistry.getInstance().
                    getJahiaVersionService().getSiteSaveVersion(c.getJahiaID());
            containerManager.updateContainer(tmpContainer, saveVersion);

            ContentContainer container = ContentContainer.getContainer(c.getID());
            JahiaEvent objectCreatedEvent = new JahiaEvent(this, jParams, container);
            ServicesRegistry.getInstance().getJahiaEventService().fireContentObjectUpdated(objectCreatedEvent);
        }
        WorkflowEvent theEvent = new WorkflowEvent(this, c.getContentContainer(), jParams.getUser(),
                ContentField.SHARED_LANGUAGE, false);
        ServicesRegistry.getInstance().getJahiaEventService().fireObjectChanged(theEvent);
    }


    public int getContainerParentListId(int id, EntryLoadRequest req) {
        return containerManager.getParentContainerListId(id, req);
    }

    public ContentObjectKey getListParentObjectKey(int id, EntryLoadRequest req) {
        int[] i = containerListManager.getParentIds(id, req);
        if (i[1] > 0) {
            return new ContentContainerKey(i[1]);
        } else if (i[0] > 0) {
            return new ContentPageKey(i[0]);
        } else {
            return null;
        }
    }

    public List<ContentContainer> findContainersByPropertyNameAndValue(String name, String value) throws JahiaException {
        List<ContentContainer> r = new ArrayList<ContentContainer>();
        for (Integer integer : containerManager.findContainerIdByPropertyNameAndValue(name, value)) {
            r.add(ContentContainer.getContainer(integer.intValue()));
        }
        return r;
    }

    public List<Object[]> getContainerPropertiesByName(String name) {
        return containerManager.getContainerPropertiesByName(name);
    }

    public List<ContentContainerList> findContainerListsByPropertyNameAndValue(String name, String value) throws JahiaException {
        List<ContentContainerList> r = new ArrayList<ContentContainerList>();
        for (Integer integer : containerListManager.findContainerListIdByPropertyNameAndValue(name, value)) {
            r.add(ContentContainerList.getContainerList(integer.intValue()));
        }
        return r;
    }

    public List<Object[]> getContainerListPropertiesByName(String name) {
        return containerListManager.getContainerListPropertiesByName(name);
    }

    public List<Integer> getContainerIDsOnPagesHavingAcls(Set<Integer> pageIDs, Set<Integer> aclIDs) {
        return containerManager.getContainerIDsOnPagesHavingAcls(pageIDs, aclIDs);
    }

    public List<Integer> getContainerIDsHavingAcls(Set<Integer> aclIDs) {
        return containerManager.getContainerIDsHavingAcls(aclIDs);
    }

    public List<Integer> getContainerListIDsOnPagesHavingAcls(Set<Integer> pageIDs, Set<Integer> aclIDs) {
        return containerListManager.getContainerListIDsOnPagesHavingAcls(pageIDs, aclIDs);
    }

    public List<Integer> getContainerListIDsHavingAcls(Set<Integer> aclIDs) {
        return containerListManager.getContainerListIDsHavingAcls(aclIDs);
    }

    /**
     * Returns a list of JahiaContainerDefinition's names using given type
     * @param type
     * @return
     */
    public List<String> getContainerDefinitionNamesWithType(String type){
        Set<String> types = new HashSet<String>(1);
        types.add(type);
        return this.containerDefinitionManager.getContainerDefinitionNamesWithType(types);
    }
    
    /**
     * Returns a list of JahiaContainerDefinition's names using given types.
     * @param types
     * @return
     */
    public List<String> getContainerDefinitionNamesWithType(Set<String> types){
        return this.containerDefinitionManager.getContainerDefinitionNamesWithType(types);
    }    

    /**
     * Create a ContainerQueryBean from a given QueryObjectModel
     *
     * @param queryModel
     * @param queryContextCtnID
     * @param jParams
     * @throws JahiaException
     * @return
     */
    public ContainerQueryBean createContainerQueryBean(QueryObjectModelImpl queryModel, int queryContextCtnID,
                                                       Properties parameters,
                                                       ProcessingContext jParams) throws JahiaException {

        if ( queryModel == null ){
            return null;
        }
        boolean queryModelWithSelectorOnly = (queryModel.getConstraint()==null
                && queryModel.getOrderings()==null);

        ContainerQueryBuilder builder = new ContainerQueryBuilder(jParams,new HashMap<String, Value>());
        ContainerQueryContext queryContext = ContainerQueryContext.getQueryContext(queryModel,
                queryContextCtnID,parameters,jParams);
        ContainerQueryBean queryBean = builder.getContainerQueryBean(queryModel,queryContext);
        if (queryBean != null
                && queryBean.getQueryContext().isSiteLevelQuery()
                && (queryBean.getFilter() == null
                        && queryBean.getSearcher() == null && queryBean
                        .getSorter() == null)) {
            boolean isJustOneSiteChildOrDescendantNodeConstraint = isJustOneSiteChildOrDescendantNodeConstraint(builder.getRootConstraint(), queryContext);
            if (queryModelWithSelectorOnly || isJustOneSiteChildOrDescendantNodeConstraint) {
                QueryObjectModelFactory queryFactory = queryModel.getQueryFactory();
                Constraint pathConstraint = null;
                if (isJustOneSiteChildOrDescendantNodeConstraint) {
                    pathConstraint = builder.getRootConstraint().getConstraint();
                }
                // create a new query object model with a constraint when the Query Object Model only contains Selector
                // but no Constraint and no Sorter
                ValueFactory valueFactory = ServicesRegistry.getInstance()
                        .getQueryService().getValueFactory();
                List<String> definitions = queryBean.getQueryContext()
                        .getContainerDefinitionsIncludingType(false);
                if (definitions != null && !definitions.isEmpty()) {
                    String[] definitionsAr = (String[]) definitions
                            .toArray(new String[] {});
                    String definitionNamesStr = JahiaTools
                            .getStringArrayToString(definitionsAr, ",");
                    Value val = valueFactory.createValue(definitionNamesStr);
                    try {
                        Literal literal = queryFactory.literal(val);
                        PropertyValue prop = queryFactory
                                .propertyValue(FilterCreator.CONTENT_DEFINITION_NAME);
                        Constraint constraint = queryFactory
                                .comparison(
                                        prop,
                                        JahiaQueryObjectModelConstants.OPERATOR_EQUAL_TO,
                                        literal);
                        if (pathConstraint != null) {
                            constraint = queryFactory.and(pathConstraint, constraint);
                        }
                        QueryObjectModel queryObjectModel = queryFactory
                                .createQuery(queryModel.getSource(),
                                        constraint, queryModel
                                                .getOrderings(), queryModel
                                                .getColumns());
                        queryContext = ContainerQueryContext.getQueryContext((QueryObjectModelImpl) queryObjectModel,
                                queryContextCtnID,parameters,jParams);
                        queryBean = builder.getContainerQueryBean(
                                (QueryObjectModelImpl) queryObjectModel,
                                queryContext);
                    } catch (Throwable t) {
                        throw new JahiaException(
                                "Failed creating ContainerQueryBean",
                                "Failed creating ContainerQueryBean",
                                JahiaException.APPLICATION_ERROR,
                                JahiaException.ERROR_SEVERITY, t);
                    }
                }
            }
        }
        return queryBean;
    }
    
    protected boolean isJustOneSiteChildOrDescendantNodeConstraint(
            ConstraintItem constraint, ContainerQueryContext queryContext) {
        boolean justOneSiteDescendantNode = false;
        if (constraint != null
                && (constraint.getConstraint() instanceof DescendantNodeImpl || constraint
                        .getConstraint() instanceof ChildNodeImpl)
                && (constraint.getChildConstraintItems() == null
                        || constraint.getChildConstraintItems().size() == 0 || constraint
                        .getChildConstraintItems().size() == 1
                        && constraint.equals(constraint
                                .getChildConstraintItems().get(0)))) {
            if (queryContext.isSiteLevelQuery()) {
                justOneSiteDescendantNode = true;
            }
        }
        return justOneSiteDescendantNode;
    }
    /**
     * Returns a map of acl ids for the given list of ctnIds
     *
     * @param ctnIds
     * @return
     */
    public Map<Integer, Integer> getContainerACLIDs(List<Integer> ctnIds){
        return this.containerManager.getContainerACLIDs(ctnIds);
    }

    /**
     * Returns the acl ID for a given Container ID
     * @param ctnID
     * @return
     */
    public int getContainerACLID(int ctnID) {
        return this.containerManager.getContainerACLID(ctnID);
    }

    public Map<String, String> getVersions(int site) {
        Map<String, String> m = containerManager.getVersions(site);
        m.putAll(containerListManager.getVersions(site));
        return m;
    }



} // end JahiaContainersBaseService
