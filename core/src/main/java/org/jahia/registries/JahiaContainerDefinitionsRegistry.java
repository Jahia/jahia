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
//  JahiaContainerDefinitionsRegistry

package org.jahia.registries;

import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.containers.JahiaContainerSet;
import org.jahia.data.containers.JahiaContainerSubDefinition;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.fields.JahiaFieldSubDefinition;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.cache.CacheListener;
import org.jahia.services.cache.CacheService;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.content.nodetypes.*;
import org.jahia.services.metadata.MetadataBaseService;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.pages.JahiaPageTemplateService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.utils.JahiaTools;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jcr.PropertyType;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;

public class JahiaContainerDefinitionsRegistry implements CacheListener {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JahiaContainerDefinitionsRegistry.class);

    private static JahiaContainerDefinitionsRegistry theObject = null;

    public static final String CONTAINER_DEFINITION_BY_ID_CACHE = "ContainerDefinitionByID";
    public static final String CONTAINER_DEFINITION_BY_SITE_AND_NAME_CACHE =
            "ContainerDefinitionsBySiteAndName";
//    private Cache containerDefByID;
//    private Cache containerDefBySiteIDAndName;
    @SuppressWarnings("unused")
    private CacheService cacheService;
    private JahiaContainersService containersService;
    private JahiaPageTemplateService tplService;
    private JahiaTemplateManagerService templateManagerService;

    private boolean initialized = false;

    private JahiaContainerDefinitionsRegistry () {
    } // end constructor

    /***
     * registry accessor
     * @return       the registry object
     *
     */
    public static synchronized JahiaContainerDefinitionsRegistry getInstance () {
        if (theObject == null) {
            theObject = new JahiaContainerDefinitionsRegistry();
        }
        return theObject;
    } // end getInstance
    
    /***
     * registry accessor
     * @return       the registry object
     *
     */
    public static synchronized JahiaContainerDefinitionsRegistry getInstance (JahiaContainersService containersService, JahiaTemplateManagerService templateManagerService) {
        if (theObject == null) {
            theObject = new JahiaContainerDefinitionsRegistry();
        }
        if (theObject != null && (theObject.containersService == null || theObject.templateManagerService == null)) {
            theObject.containersService = containersService;
            theObject.templateManagerService = templateManagerService;
        }
        return theObject;
    } // end getInstance

    /***
     * calls the loadAllDefinitions method
     *
     */
    public void init ()
        throws JahiaException {
        if (!initialized) {
            logger.debug("Starting ContainerDefinitions Registry");
            /*try {
            containerDefByID = ServicesRegistry.getInstance().getCacheService().createCacheInstance(CONTAINER_DEFINITION_BY_ID_CACHE);
            containerDefByID.registerListener(this);


            containerDefBySiteIDAndName = ServicesRegistry.getInstance().getCacheService().createCacheInstance(CONTAINER_DEFINITION_BY_SITE_AND_NAME_CACHE);
            containerDefBySiteIDAndName.registerListener(this);

        } catch (JahiaException je) {
            logger.error(
                "Error while creating caches for JahiaContainerDefinition registry.", je);
        }*/
            loadAllDefinitions();
            initialized = true;
        }
    } // end init

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setContainersService(JahiaContainersService containersService) {
        this.containersService = containersService;
    }
    public JahiaContainersService getContainersService() {
        return containersService;
    }

    /***
     * loads all container definitions from the database
     *
     * calls load_container_definitions in JahaiContainersDBService
     *
     */
    private void loadAllDefinitions ()
        throws JahiaException {
        getContainersService().getAllContainerDefinitionIDs();

//        for (int i = 0; i < defIDs.size(); i++) {
//            JahiaContainerDefinition currentDefinition =
//                    containersService.loadContainerDefinition (((Integer)defIDs.elementAt(i)).intValue());

            /*containerDefByID.put(new Integer(currentDefinition.getID()),
                                 currentDefinition);
            containerDefBySiteIDAndName.put(buildCacheKey(currentDefinition.
                getName(), currentDefinition.getJahiaID()), currentDefinition);*/
//        }
    }

    private JahiaContainerDefinition loadDefinitionByID (int defID)
        throws JahiaException {
        JahiaContainerDefinition currentDefinition = getContainersService().loadContainerDefinition(defID);
        if (currentDefinition != null) {
            /*containerDefByID.put(new Integer(currentDefinition.getID()),
                                 currentDefinition);
            containerDefBySiteIDAndName.put(buildCacheKey(currentDefinition.
                getName(), currentDefinition.getJahiaID()), currentDefinition);*/
        }
        return currentDefinition;
    }

    private JahiaContainerDefinition loadDefinitionBySiteIDAndName (int siteID,
        String definitionName)
        throws JahiaException {
        JahiaContainerDefinition currentDefinition = null;
    
        if (getContainersService() != null) {
        currentDefinition = getContainersService().loadContainerDefinition(siteID, definitionName);
        if (currentDefinition != null) {
            /*containerDefByID.put(new Integer(currentDefinition.getID()),
                                 currentDefinition);
            containerDefBySiteIDAndName.put(buildCacheKey(currentDefinition.
                getName(), currentDefinition.getJahiaID()), currentDefinition);*/
        }
        }
        return currentDefinition;
    }

    /***
     * gets a definition in the registry through its definition ID
     *
     * @param        defID           the definition ID
     * @return       a JahiaContainerDefinition object; null if not found
     * @see          org.jahia.data.containers.JahiaContainerDefinition
     *
     * @exception JahiaException   raises a critical JahiaException if no definition found
     *
     */
    public JahiaContainerDefinition getDefinition (int defID)
        throws JahiaException {
//        synchronized (containerDefByID) {
            JahiaContainerDefinition currentDef =null;
            currentDef = loadDefinitionByID(defID);
            if (currentDef == null) {
                logger.debug("Couldn't find container definition for ID=" +defID);
            }
            return currentDef;
//        }
    } // end getDefinition

    /***
     * gets a definition in the registry through its page definition id and container name
     *
     * @param		siteID			the site id
     * @param        containerName   the container name
     * @return       a JahiaContainerDefinition object; null if not found
     * @see          org.jahia.data.containers.JahiaContainerDefinition
     *
     *
     */
    public synchronized JahiaContainerDefinition getDefinition (int siteID,
        String containerName)
        throws JahiaException {
//        synchronized (containerDefBySiteIDAndName) {
            JahiaContainerDefinition currentDef = null;
            currentDef = loadDefinitionBySiteIDAndName(siteID, containerName);
            if (currentDef == null) {
                logger.debug("Couldn't find container definition for siteID=" +
                             siteID + " and name=" + containerName);
            }
            return currentDef;
//        }
    } // end getDefinition

    /***
     * gets all definitions of a page template in the registry
     *
     * @return       a JahiaContainerDefinition object; null if not found
     * @see          org.jahia.data.containers.JahiaContainerDefinition
     *
     *
     */
    public List<JahiaContainerDefinition> getDefinitionsInTemplate (JahiaPageDefinition pageDefinition) {
        List<JahiaContainerDefinition> containerDefinitions = getContainersService().loadContainerDefinitionInTemplate(0);
        List<JahiaContainerDefinition> theDefs = new ArrayList<JahiaContainerDefinition>();
        for (JahiaContainerDefinition def : containerDefinitions) {
            String pageDefName = pageDefinition.getPageType().replace(':', '_');
            if (def.getName().startsWith(pageDefName)) {
                theDefs.add(def);
            }
        }
        return theDefs;
    } // end getDefinitionsInTemplate

    /***
     * sets a definition in the registry, and synchronizes it with the database
     *
     * @param        theContainerDef the JahiaContainerDefinition object to set
     * @see          org.jahia.data.containers.JahiaContainerDefinition
     *
     */
    public void setDefinition (JahiaContainerDefinition
                                            theContainerDef)
        throws JahiaException {
        getContainersService().saveContainerDefinition(theContainerDef);
    } // end setDefinition

    /***
     * remove a Container Definition
     */
    public void removeContainerDefinition (int ctnDefID)
        throws JahiaException {
    } // end removeContainerDefinition

    /**
     * This method is called each time the cache flushes its items.
     *
     * @param cacheName the name of the cache which flushed its items.
     */
    public void onCacheFlush(String cacheName) {
       /* if (CONTAINER_DEFINITION_BY_ID_CACHE.equals(cacheName)) {
            containerDefBySiteIDAndName.flush(false);

        } else if (CONTAINER_DEFINITION_BY_SITE_AND_NAME_CACHE.equals(cacheName)) {
            containerDefByID.flush(false);
        }*/

        try {
            loadAllDefinitions();

        } catch (JahiaException e) {
            logger.warn("Could not reload the Field Definitions.", e);
        }
    }

    public Map<String, Integer> buildContainerDefinitionsForTemplate(String typeName, int siteId, int pageDefID, JahiaContainerSet theSet) throws JahiaException {
        Map<String, Integer> containerNamesAndDefId = new HashMap<String, Integer>();
        try {
            ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(typeName);
            ExtendedNodeDefinition[] nodes = nt.getChildNodeDefinitions();

            for (ExtendedNodeDefinition nodeDef : nodes) {
                final ExtendedNodeType dnt = nodeDef.getDeclaringNodeType();
                if (dnt.getName().equals(Constants.JAHIANT_PAGE) || dnt.getName().equals(Constants.JAHIANT_JAHIACONTENT) || !nodeDef.isJahiaContentItem() ) {
                    continue;
                }
                String parentCtnType = dnt.getName() + " " + nodeDef.getName();
                String containerName = nt.getName().replace(':','_') + "_" + nodeDef.getName();
//                String containerName = nodeDef.getDeclaringNodeType().getName().replace(':','_') + "_" + nodeDef.getName();
                ExtendedNodeType[] requiredPrimaryTypes = nodeDef.getRequiredPrimaryTypes();
                if (requiredPrimaryTypes[0].isNodeType(Constants.JAHIANT_CONTAINERLIST) || requiredPrimaryTypes[0].isNodeType(Constants.JAHIANT_CONTENTLIST)) {
                    nodeDef = requiredPrimaryTypes[0].getDeclaredUnstructuredChildNodeDefinitions().values().iterator().next();
                    requiredPrimaryTypes = nodeDef.getRequiredPrimaryTypes(); 
                }

                StringBuilder defTypes = new StringBuilder();
                if (requiredPrimaryTypes.length > 0) {
                    for (int j = 0; j < requiredPrimaryTypes.length; j++) {
                        NodeType nodeType = requiredPrimaryTypes[j];
                        if (nodeType != null) {
                            defTypes.append(nodeType.getName());
                            defTypes.append(",");
                        }
                    }
                }
                if (defTypes.length() == 0) {
                    logger.error("Definition for container " + nodeDef.getName() + " not found, skip it");
                } else {
                    String defTypesStr = defTypes.deleteCharAt(defTypes.length() - 1).toString();
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
                                JahiaTemplatesPackage defPackage = templateManagerService.getTemplatePackage(sub.getSystemId());
                                if (defPackage != null)  {
                                    JahiaTemplatesPackage parentPackage = templateManagerService.getTemplatePackage(NodeTypeRegistry.getInstance().getNodeType(parentCtnType.split(" ")[0]).getSystemId());
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
        Set<ExtendedItemDefinition> items = new LinkedHashSet<ExtendedItemDefinition>();

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
            if (!ext.isJahiaContentItem()) {
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
     * @param fieldType
     * @param aliasNames
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
                    if (aDef.getType() != fieldType) {
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
            if (tplService != null) {
                pageTemplate = tplService.lookupPageTemplate(pageDefId);
            }
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
                case PropertyType.NAME :
                    switch (propDef.getSelector()) {
                        case SelectorType.RICHTEXT:
                            return FieldTypes.BIGTEXT;
                        case SelectorType.FILEPICKER:
                        case SelectorType.FILEUPLOAD:
                            return FieldTypes.FILE;
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
                case ExtendedPropertyType.WEAKREFERENCE :
                case PropertyType.REFERENCE :
                    switch (propDef.getSelector()) {
                        case SelectorType.FILEPICKER:
                        case SelectorType.FILEUPLOAD:
                            return FieldTypes.FILE;
                        case SelectorType.PORTLET:
                            return FieldTypes.APPLICATION;
                        case SelectorType.CATEGORY:
                            return FieldTypes.CATEGORY;
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
                if (nodeTypes[0].isNodeType(Constants.JAHIANT_CONTAINERLIST)||nodeTypes[0].isNodeType(Constants.JAHIANT_CONTENTLIST)) {
                    nodeTypes = nodeTypes[0].getUnstructuredChildNodeDefinitions().values().iterator().next().getRequiredPrimaryTypes();
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
            } else if (nodeDef.isJahiaContentItem()) {
                String subContainerName = containerName + "_"+ nodeDef.getName();
                String defTypesStr = defTypes.substring(0,defTypes.length()-1);
                declareContainerDefinition(ext.getDeclaringNodeType().getName() + " " + ext.getName(), subContainerName, defTypesStr, nodeDef.getSelectorOptions(), siteId, pageDefId, theSet);
                containerFields.add("@c " + subContainerName);
            }
        }
    }

    public void setTplService(JahiaPageTemplateService tplService) {
        this.tplService = tplService;
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }    
}