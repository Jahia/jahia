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

//
//  JahiaContainerSet
//  EV      01.01.2001      Happy New Year, Folks !
//
package org.jahia.data.containers;

import org.apache.log4j.Logger;
import org.apache.commons.collections.set.ListOrderedSet;
import org.jahia.bin.Jahia;
import org.jahia.data.fields.*;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaContainerDefinitionsRegistry;
import org.jahia.registries.JahiaFieldDefinitionsRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContainerFactory;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.content.nodetypes.*;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.metadata.FieldDefinition;
import org.jahia.services.metadata.MetadataBaseService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.fields.ContentApplicationField;
import org.jahia.services.applications.ApplicationsManagerService;
import org.jahia.utils.JahiaTools;
import org.jahia.resourcebundle.ResourceBundleMarker;
import org.jahia.content.ContentObject;
import org.jahia.content.PageDefinitionKey;
import org.jahia.portlets.JahiaContentPortlet;
import org.jahia.api.Constants;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.File;
import java.util.*;

/**
 * @version $Rev$
 */
public class JahiaContainerSet implements Map {

    /**
     * this container name is used for backward compatibility with
     * the method declareField(String fieldName, String fieldTitle,
     * int fieldType, String defaultValue)
     * without giving any container list name
     * Declaring such field names has the limitation that theses field names
     * should be uniques for all container of the template
     */
    public static final String SHARED_CONTAINER = "@@@SHARED_CONTAINER@@@";

    private static Logger logger = Logger.getLogger(JahiaContainerSet.class);

    private static final String CLASS_NAME = JahiaContainerSet.class.getName();

    /**
     * associates JahiaContainerList
     */
    private Map<String, JahiaContainerList> containerLists;

    /**
     * associates String
     */
    private Set<String> declaredContainers; // list of per-request declared containers

    /**
     * associates String (key = container name/ value = List of field name)
     */
    private Map<String, List<String>> declaredFields;

    /**
     * key = field name / value = Properties
     */
    private Map<String, Properties> declaredFieldDefProps;

    /**
     * This is used to store during a request all the container lists that have
     * been accessed in absolute manner. This will be used to figure out if the
     * absolute references have changed since the last time this template was
     * processed.
     */
    private Set<Integer> absoluteContainerListAccesses = new HashSet<Integer>();


    /**
     * Optional cached containers fields
     */
    private Map<Integer, List<Integer>> cachedFieldsFromContainers;

    /**
     * Optional cached containers in container list
     */
    private Map<Integer, List<Integer>> cachedContainersFromContainerLists;

    /**
     * Optional cached containerlists in containers
     */
    private Map<Integer, List<Integer>> cachedContainerListsFromContainers;

    private JahiaContainersService jahiaContainersService;
    private ProcessingContext processingContext;
    private JahiaPage page;


    private Set<Integer> requestedContainers;
    private Set<Integer> requestedLists;

    //-------------------------------------------------------------------------
    /***
     * constructor
     *
     *
     */
    public JahiaContainerSet(ProcessingContext processingContext, JahiaPage page) {
        this.processingContext = processingContext;
        this.page = page;
        this.containerLists = new HashMap<String, JahiaContainerList>();
        this.declaredContainers = new HashSet<String>();
        this.declaredFields = new HashMap<String, List<String>>();
        this.declaredFieldDefProps = new HashMap<String, Properties>();
        this.cachedFieldsFromContainers = new HashMap<Integer, List<Integer>>();
        this.cachedContainersFromContainerLists = new HashMap<Integer, List<Integer>>();
        this.jahiaContainersService = ServicesRegistry.getInstance().getJahiaContainersService();

        String ids = processingContext.getParameter("getctnid");
        if (ids != null) {
            requestedContainers = new HashSet<Integer>();
            requestedLists = new HashSet<Integer>();
            String[] s = ids.split(",");
            for (int i = 0; i < s.length; i++) {
                String id = s[i];
                ContentContainer cc = (ContentContainer) ContentContainer.getChildInstance(id);
                if (cc != null) {
                    requestedContainers.add(cc.getID());
                    requestedLists.add(cc.getParentContainerListID());
                }
            }
        }
        String listids = processingContext.getParameter("getctnlistid");
        if (listids != null) {
            if (requestedLists == null) {
                requestedLists = new HashSet<Integer>();
            }
            String[] s = listids.split(",");
            for (int i = 0; i < s.length; i++) {
                String id = s[i];
                requestedLists.add(Integer.parseInt(id));
            }
        }
    } // end constructor


    //-------------------------------------------------------------------------
    /***
     * adds a container list to the set.
     *
     * @param        aContainerList  the JahiaContainerList to add
     * @param        listViewId   the container list ID in the tag for supporting 
     *                                    different views of the same list  
     * @see          org.jahia.data.containers.JahiaContainerList
     *
     * @throws org.jahia.exceptions.JahiaException
     */
    private void addContainerList(JahiaContainerList aContainerList, String listViewId) throws JahiaException {
        containerLists.put((listViewId != null
                && listViewId.length() > 0 ? listViewId + "_"
                : "")
                + aContainerList.getDefinition().getName(), aContainerList);
    } // end addContainerList

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
                             String containerType, String fieldName,
                             String fieldTitle,
                             int fieldType,
                             String[] aliasNames,
                             float boostFactor,
                             boolean indexableField,
                             boolean readOnly)
            throws JahiaException {
        if(Jahia.getThreadParamBean().getOperationMode().equals(ProcessingContext.NORMAL))
        return;
        /** todo ensure that the data provided by the user has no special chars in it */
        // we check if a field with the same name was not already declared, of if the field has no
        // empty name or title
        boolean isDeclaredField = checkDeclaredField(containerName, fieldName);

        //MC: this check was removed as it doesn't seem to have any purpose anymore
        //boolean isJahiaDataDeclaredField = jData.fields().checkDeclared(fieldName);

        // NK : 03.09.2003
        // FIXME, why is this test for since field names are handled differently with container names ????
        //boolean isDeclaredContainer = false;
        boolean isDeclaredContainer = checkDeclared(fieldName);

        if (aliasNames != null && aliasNames.length > 0) {
            declareFieldDefProp(fieldName, JahiaFieldDefinition.ALIAS_PROP_NAME, JahiaTools.getStringArrayToString(aliasNames, ","));
            for (int i=0; i<aliasNames.length;i++){
                declareFieldDefProp(fieldName, JahiaFieldDefinition.ALIAS_PROP_NAME+i, aliasNames[i]);
            }
        }

        // boost Factor
        declareFieldDefProp(fieldName, FieldDefinition.SCORE_BOOST, String.valueOf(boostFactor));
        declareFieldDefProp(fieldName, FieldDefinition.INDEXABLE_FIELD, String.valueOf(indexableField));
        declareFieldDefProp(fieldName, FieldDefinition.READ_ONLY, String.valueOf(readOnly));

        if (!isDeclaredField &&
                //MC: this check was removed as it doesn't seem to have any purpose anymore
                //(!isJahiaDataDeclaredField) &&
                fieldName.length() != 0 && fieldTitle.length() != 0 && !isDeclaredContainer) {

            // first, let's check to see if the declared field has already a field definition in the
            // FieldsDefinitionsRegistry (which means also in the jahia_fields_def table)
            JahiaFieldDefinition aDef = JahiaFieldDefinitionsRegistry.
                    getInstance().getDefinition(page.getSiteID(), fieldName);
            int pageDefID = page.getPageTemplateID();
            if (aDef != null) {
                // okay, it seems the definition already exists.
                // now has it the same data than in the database (title, type, defaultval) ?

                Map<Object, Object> props = aDef.getProperties();
                Properties declaredProps = (Properties) this.declaredFieldDefProps.get(fieldName);

                boolean propsHaveChanged = ((props == null && declaredProps != null && !declaredProps.isEmpty()) ||
                        (props != null && !props.isEmpty() && declaredProps == null)
                        || (props != null && declaredProps != null && !props.equals(declaredProps)));
                boolean ctnTypeHasChanged = (containerType != null && !containerType.equals(aDef.getCtnType())) || (containerType == null && aDef.getCtnType() != null);
                if (propsHaveChanged || ctnTypeHasChanged) {
                    // well, data is not the same in the registry and in the declare() method !
                    // this means the user has changed the field declaration in the template...
                    // before doing anything else, we have to be sure that the user didn't change the
                    // field data type, because this could crash the whole application !
                    if (aDef.getType() != fieldType &&
                            aDef.getType() != -1) {
                        // okay, this user think he's smart : he tried to change the data type of
                        // fields already existing...
                        // let's send him a Jahia error to learn him a little lesson
                        String errorMsg =
                                "Cannot change field type because field data already exists : " +
                                        fieldName;
                        logger.error(errorMsg + " -> BAILING OUT");
                        throw new JahiaException(errorMsg, errorMsg,
                                JahiaException.TEMPLATE_ERROR,
                                JahiaException.
                                        CRITICAL_SEVERITY);

                    } else {
                        if (aDef.getType() != -1) {
                            boolean someChanges=false;
                            logger.warn("Definition for field " + fieldTitle + " (in " + containerName + ") has changed. ");
                            if (propsHaveChanged) {
                                logger.warn(" Properties have changed : " + props + " / " + declaredProps);
                                someChanges=true;
                            }
                            if (ctnTypeHasChanged) {
                                logger.warn(" Container type has changed : " + aDef.getCtnType() + " / " + containerType);
                                someChanges=true;
                            }
                            if (someChanges) {
                                if (logger.isDebugEnabled()) {
                                    Throwable throwable = (new Throwable());
                                    throwable.fillInStackTrace();
                                    traceCaller(throwable, "declareField");
                                } else {
                                    logger.warn("please activate debug mode on " + JahiaContainerSet.class.getPackage() + "." + JahiaContainerSet.class.getName() + " in your log4j.xml configuration to have more info");
                                }
                            }
                        }

                        // okay, no alert ahead, the type hasn't been modified
                        // let's synchronize the data between :
                        //  - the template declaration (file)
                        //  - the database declaration (database)
                        //  - the registry declaration (memory)
                        // the synchronizeData method handles all this for us... pfew :)
                        logger.debug("Setting data for pageDef " +
                                pageDefID);
                        aDef.setProperties(this.declaredFieldDefProps.get(fieldName));
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
                subDefs.put(new Integer(pageDefID),
                        new JahiaFieldSubDefinition(0, 0, pageDefID,
                                false));
                aDef = new JahiaFieldDefinition(0,
                        page.getJahiaID(),
                        fieldName, subDefs);
                aDef.setCtnType(containerType);
                aDef.setProperties(this.declaredFieldDefProps.get(fieldName));

                JahiaFieldDefinitionsRegistry.getInstance().setDefinition(aDef);
            }
            List<String> fieldNames = declaredFields.get(containerName);
            if (fieldNames == null) {
                fieldNames = new ArrayList<String>();
            }
            fieldNames.add(fieldName);
            declaredFields.put(containerName, fieldNames);

        } else {
            // the field is already declared, or has a null name/title : let the user have it... ;)
            String errorMsg = "";
            if (fieldName.length() == 0) {
                errorMsg = " Field has null name : " + fieldTitle;
            }
            if (fieldTitle.length() == 0) {
                errorMsg += " Field has null title : " + fieldName;
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
     * Declare properties for a given field definition ( field name )
     *
     * @param fieldName
     * @param propName
     * @param propValue
     */
    private void declareFieldDefProp(String fieldName,
                                    String propName,
                                    String propValue) {
        if (fieldName != null && propName != null && propValue != null) {
            Properties props = (Properties) this.declaredFieldDefProps.get(fieldName);
            if (props == null) {
                props = new Properties();
            }
            props.setProperty(propName, propValue);
            this.declaredFieldDefProps.put(fieldName, props);
        }
    }

    /**
     * enables a jahia template programmer to declare a container, specifying
     * scrollable window size and offset for container lists that will grow very
     * large in size.
     *
     * @param containerName          the container name
     * @param containerTitle         the container title
     * @param containerFields        the fields (or containers) in the container
     * @param windowSize             an integer specifying the size of the
     *                               display window in number of containers. Valid values are >= 1, -1 to
     *                               deactivate this feature
     * @param windowOffset           an integer specifying the offset in the
     *                               list of containers, to be used only when windowSize >= 1. Valid values are
     *                               >= 0, and -1 is used to deactivate this feature
     * @param validatorKey           the key specifying the validator rules set to be
     *                               applied to this container
     * @param containerBeanName      the name of the bean class being wrapped around
     *                               the container facade
     * @param aliasNames             this container's alias ( Container Definition Names )
     *                               Commas separated names without spaces , i.e : containerName1,containerName2,containerName3
     *                               Theses alias state that a container is of same structure as another container of given alias name
     * @param containerListType      @see JahiaContainerDefinition.STANDARD_TYPE, ...
     * @param containerNodeType
     *@param containerDefProperties a set of properties to be stored with the container definition.
     *                               Be careful because some keywords are reserved, and will be ignored. The following keys may NOT be used
     *                               because they are reserved for internal Jahia use : windowSize, windowOffset, validatorKey, containerBeanName,
     *                               ALIAS_NAME, CONTAINER_LIST_TYPE. This argument may be null if you don't want to define any custom properties
     *                               for the container definition. @throws JahiaException a critical JahiaException if the container has been already declared
     * @throws JahiaException raises a critical JahiaException if one of the containerFields have not been declared
     * @throws JahiaException raises a critical JahiaException if one of the fields has the same name as the container
     *                        <p/>
     *                        In order to let a container include itself, the keyword "_self" in the containeFields must be used.
     */
    private void declareContainer(String parentCtnType, String containerName, String containerTitle,
                                 List<String> containerFields, int windowSize,
                                 int windowOffset, String validatorKey,
                                 String containerBeanName,
                                 String[] aliasNames,
                                 int containerListType,
                                 String containerNodeType, Properties containerDefProperties)
            throws JahiaException {
        if(Jahia.getThreadParamBean().getOperationMode().equals(ProcessingContext.NORMAL))
        return;

        /** @todo ensure that the data provided by the user has no special chars in it */
        // check if a container has already been declared with the same name
		// checks if container name and title are not empty
		// checks if a field has not the same name
        if (checkDeclared(containerName) || containerName.length() == 0
				|| containerTitle.length() == 0
				|| this.checkDeclaredField(containerName)) {
            // the container is already declared, or has a null name/title : let
			// the user have it... ;)
            String errorMsg = "Container already declared or has a null name - name : "
                    + containerName + ", title : " + containerTitle;
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
                } else if (!isField && !checkDeclared(theName) && !isContainer
                        && !checkDeclaredField(theName)) {

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
            if (windowSize >= 1) {
                if (windowOffset < 0) {
                    windowOffset = 0;
                }
                ctnDefProperties.setProperty("windowSize", Integer
                        .toString(windowSize));
                ctnDefProperties.setProperty("windowOffset", Integer
                        .toString(windowOffset));

            }

            // added for Apache Validator Support
            if (validatorKey != null && validatorKey.length() > 0) {
                ctnDefProperties.setProperty("validatorKey", validatorKey);
            }
            if (containerBeanName != null && containerBeanName.length() > 0) {
                ctnDefProperties.setProperty("containerBeanName",
                        containerBeanName);
            }

            // save container definition aliasNames
            if (aliasNames != null && aliasNames.length > 0) {
                ctnDefProperties.setProperty(
                        JahiaContainerDefinition.ALIAS_PROP_NAME, JahiaTools
                                .getStringArrayToString(aliasNames, ","));
                for (int i = 0; i < aliasNames.length; i++) {
                    ctnDefProperties.setProperty(
                            JahiaContainerDefinition.ALIAS_PROP_NAME + i,
                            aliasNames[i]);
                }
            }

			// save container list type
            ctnDefProperties.setProperty(
                    JahiaContainerDefinition.CONTAINER_LIST_TYPE_PROPERTY,
                    String.valueOf(containerListType));

            JahiaContainerDefinitionsRegistry ctnDefRegistry = JahiaContainerDefinitionsRegistry
                    .getInstance();

            // third, let's check to see if the declared container has
            // already a container definition in the
            // ContainersDefinitionsRegistry
            JahiaContainerDefinition aDef = ctnDefRegistry.getDefinition(
                    page.getSiteID(), containerName);
            int pageDefID = page.getPageTemplateID(); // get the page template ID no
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
                    if (propertiesHaveChanged) {
                        logger.debug("Reloading containerList " + containerName
                                + "...");

                        // small hack to avoid doing this everywhere and to
                        // be able to call checkDeclared in the next call...
                        declaredContainers.add(containerName);
                        containerLists.remove(containerName);
                        getContainerList(containerName);
                        // hack, will be added below...
                        declaredContainers.remove(containerName);
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
                        0, pageDefID, page.getJahiaID(), null);
                subDefs.put(0, subDef);
                aDef = new JahiaContainerDefinition(0, page.getJahiaID(),
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

                /*
                 * Map subDefs = new HashMap(); JahiaContainerSubDefinition subDef = new JahiaContainerSubDefinition( 0, pageDefID,
                 * containerTitle, null ); subDef.composeStructure( containerFields ); subDefs.put( new Integer(pageDefID), subDef ); aDef =
                 * new JahiaContainerDefinition( 0, jData.params().getPage().getJahiaID(), containerName, subDefs );
                 * JahiaContainerDefinitionsRegistry.getInstance().setDefinition( aDef );
                 */
            }

            // fourth, we declare that the container has been loaded, in
            // order to avoid double definitions
            if (aDef.getID() != 0) {
                // last check that the definition was really created
                declaredContainers.add(containerName);

                // last, we create a new fake container list if the
                // container list is empty
                int clistID = jahiaContainersService.getContainerListID(
                        containerName, page.getID());
                if (clistID == -1) {

                    // if (getQueryContainerList(containerName) == null) {
                    // we create a fake container list, and save it into memory
                    JahiaContainerList fakeContainerList = new JahiaContainerList(
                            0, 0, page.getID(), aDef.getID(), 0);

                    // we can't create a *real* container list in the database
                    // here
                    // since we might be in the case of a sub container list and
                    // these can only be instantiated when the parent container
                    // is created.
                    addContainerList(fakeContainerList, null);
                }
            }
        }
    }     

    public void declareContainer(String parentCtnType, String containerName, String containerTypes, int windowSize, int windowOffset, int containerListType, Properties containerDefProp, Map<String, String> options) throws JahiaException {
        List<ExtendedNodeType> types = new ArrayList<ExtendedNodeType>();
        List<ExtendedNodeType> mixinTypes = new ArrayList<ExtendedNodeType>();

        List<String> containerFields = new ArrayList<String>();

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
            return;
        }
        Set<ExtendedItemDefinition> items = new ListOrderedSet();

        if (types.size() > 1) {
            String title = ResourceBundleMarker.drawMarker(baseType.getResourceBundleId(),"nt_base.jcr_primaryType","jcr_primaryType");
            for (ExtendedNodeType currentType : types) {
                items.addAll(currentType.getItems());
            }
            declareField(containerName, "nt:base jcr:primaryType", containerName + "_jcr_primaryType", title, FieldTypes.SMALLTEXT_SHARED_LANG, new String[] {"type"}, 0.0f, false, false);
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

            addItemDefinitionToContainer(containerName, ext, containerFields);
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
        String v = baseType.getValidator();
        String containerBeanName = null;
        String validatorKey = null;
        if ( v != null ) {
            containerBeanName = v.contains(":") ? v.substring(0,v.indexOf(":")) : null;
            validatorKey = v.contains(":") ? v.substring(v.indexOf(":")+1) : v;
        }
        containerDefProp.put("jcr:primaryType", baseType.getName());
        declareContainer(parentCtnType, containerName, baseType.getResourceBundleMarker(), containerFields,windowSize,windowOffset, validatorKey, containerBeanName, new String[0], containerListType, baseType.getName(), containerDefProp);
    }

    private void addItemDefinitionToContainer(String containerName, ExtendedItemDefinition ext, List<String> containerFields) throws JahiaException {
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
                declareField(containerName, name, ext, containerFields);
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
                declareField(containerName, name, ext, containerFields);
            } else if (defTypes.length() == 0) {
                logger.error("Definition for subcontainer "+ nodeDef.getName() + " not found, skip it");
            } else {
                int type = JahiaContainerDefinition.STANDARD_TYPE;
                ExtendedNodeDefinition ctnDef = nodeDef;
                if (ctnDef.getRequiredPrimaryTypes()[0].isNodeType(Constants.JAHIANT_CONTAINERLIST)) {
                    ctnDef = ctnDef.getRequiredPrimaryTypes()[0].getChildNodeDefinitionsAsMap().get("*");
                }

                if (!ctnDef.allowsSameNameSiblings()) type += JahiaContainerDefinition.SINGLE_TYPE;
                if (ctnDef.isMandatory()) type += JahiaContainerDefinition.MANDATORY_TYPE;
                String subContainerName = containerName + "_"+ nodeDef.getName();

                String defTypesStr = defTypes.substring(0,defTypes.length()-1);
                declareContainer(ext.getDeclaringNodeType().getName() + " " + ext.getName(), subContainerName,defTypesStr, -1,-1, type, new Properties(), nodeDef.getSelectorOptions());
                containerFields.add("@c " + subContainerName);
            }
        }
    }

    private void declareField(String containerName, String name, ExtendedItemDefinition itemDef, List<String> containerFields) throws JahiaException {
        try {
            int type = getType(itemDef);

            if (type == FieldTypes.APPLICATION) {
                Set<String> declared = new HashSet<String>();
                int listId = ServicesRegistry.getInstance().getJahiaContainersService().getContainerListID(containerName, page.getID());
                List<Integer> ctnId = ServicesRegistry.getInstance().getJahiaContainersService().getctnidsInList(listId, EntryLoadRequest.STAGED);
                for (Iterator<Integer> integerIterator = ctnId.iterator(); integerIterator.hasNext();) {
                    int i = integerIterator.next();
                    ContentContainer c = ContentContainer.getContainer(i);
                    for (ContentObject contentObject : c.getChilds(null,null)) {
                        if (contentObject instanceof ContentApplicationField) {
                            ContentApplicationField contentApplicationField = (ContentApplicationField) contentObject;
                            ContentObjectEntryState cos = (ContentObjectEntryState) contentApplicationField.getActiveAndStagingEntryStates().first();
                            String appId = contentApplicationField.getAppID(cos);
                            ApplicationsManagerService applicationManagerService = ServicesRegistry.getInstance().getApplicationsManagerService();
                            EntryPointInstance epi = applicationManagerService.getEntryPointInstance(appId);
                            if (epi != null && epi.getContextName().startsWith(Jahia.getContextPath())) {
                                String defName = epi.getDefName();
                                String portletName = defName.substring(defName.indexOf('.')+1);
                                if (!declared.contains(portletName)) {
                                    String def = JahiaContentPortlet.getContentDefinition(portletName);
                                    if (def != null) {
                                        ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(def);
                                        if (nt != null) {
                                            List<ExtendedItemDefinition> l = nt.getItems();
                                            for (Iterator<ExtendedItemDefinition> extendedItemDefinitionIterator = l.iterator(); extendedItemDefinitionIterator.hasNext();) {
                                                ExtendedItemDefinition extendedItemDefinition = extendedItemDefinitionIterator.next();
                                                if (!extendedItemDefinition.getDeclaringNodeType().getName().equals("jnt:portlet")) {
                                                    addItemDefinitionToContainer(containerName, extendedItemDefinition, containerFields);
                                                }
                                            }
                                        }
                                    }
                                    declared.add(portletName);
                                }
                            }
                        }
                    }
                }
            }

            String title = itemDef.getResourceBundleMarker();

            Map<String, String> extProps = getExtendedProperties(itemDef);
            for (String key : extProps.keySet()) {
                declareFieldDefProp(name, key,extProps.get(key));
            }

            String n = itemDef.getDeclaringNodeType().getName() + " " + itemDef.getName();
            if (!itemDef.isNode()) {
                ExtendedPropertyDefinition propDef = (ExtendedPropertyDefinition) itemDef;
                declareField(containerName, n, name, title, type, new String[] {itemDef.getName()}, (float) propDef.getScoreboost(), propDef.getIndex() != ExtendedPropertyDefinition.INDEXED_NO, itemDef.isProtected());
            } else {
                declareField(containerName, n, name, title, type, new String[] {itemDef.getName()}, 1.f,false, itemDef.isProtected());
            }
        } catch (RepositoryException e) {
            throw new JahiaException("","",0,0,e);
        }
        containerFields.add("@f " + name);
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

    public static Map<String,String> getExtendedProperties(ExtendedItemDefinition itemDef) throws RepositoryException {
        Map<String,String> results = new HashMap<String, String>();

        if (itemDef.isMandatory()) {
            results.put(FieldDefinition.REQUIRED,String.valueOf(itemDef.isMandatory()));
        }
        if (itemDef.isProtected()) {
            results.put(FieldDefinition.READ_ONLY,String.valueOf(itemDef.isProtected()));
        }

        results.put(FieldDefinition.DEFINITION,itemDef.getDeclaringNodeType().getName() + " " +itemDef.getName());

        if (!itemDef.isNode()) {
            ExtendedPropertyDefinition propDef = (ExtendedPropertyDefinition) itemDef;
            Map<String,String> opts = propDef.getSelectorOptions();
            switch (propDef.getSelector()) {
                case SelectorType.RICHTEXT :
                    if (opts.containsKey("stylesheetId")) {
                        results.put(JahiaFieldDefinitionProperties.FIELD_STYLESHEET_ID_PROP, opts.get("stylesheetId"));
                    }
                case SelectorType.SMALLTEXT :
                    if (opts.containsKey("multiline")) {
                        results.put(JahiaFieldDefinitionProperties.FIELD_MULTILINE_SMALLTEXT_PROP, "true");
                    }
                    break;
                case SelectorType.COLOR :
                    results.put(JahiaFieldDefinitionProperties.COLOR_PICKER_PROP, "true");
                    break;
            }
        }
        return results;
    }


    /**
     * enables a jahia template programmer to declare an edit view definition to
     * use for a given container list.
     *
     * @param containerName
	 *            the container name
	 * @param editView ,
	 *            the view definition
	 */
    public void declareContainerEditView(String containerName,
                                         ContainerEditView editView)
            throws JahiaException {

        logger.debug("for container [" + containerName + "]");
        /** todo ensure that the data provided by the user has no special chars in it */

        // check if a container has already been declared with the same name
        if (containerName.length() != 0) {
            // third, let's check to see if the declared container has already a container definition in the
            // ContainersDefinitionsRegistry
            JahiaContainerDefinition aDef = JahiaContainerDefinitionsRegistry
                    .getInstance()
                    .getDefinition(page.
                            getSiteID(), containerName);
            if (aDef != null) {
                aDef.setContainerEditView(editView);
                logger.debug("for container [" + containerName + "] done successfully ");
            }
        }
    }


    //-------------------------------------------------------------------------
    /***
     * checks if a container has already been declared
     * Note : we don't need to call this before a container declaration,
     * this is already done in the declareContainer implementation
     *
     * @param        containerName   the container name
     * @return true if already declared, false if not
     *
     */
    private boolean checkDeclared(String containerName) {
        if(Jahia.getThreadParamBean().getOperationMode().equals(ProcessingContext.NORMAL) && ! declaredContainers.contains(containerName))
        declaredContainers.add(containerName);
        return declaredContainers.contains(containerName);

    } // end checkDeclared

    //-------------------------------------------------------------------------
    /***
     * checks if a container field has already been declared
     * Note : we don't need to do this before a container declaration. This
     * is already done for us in the declareContainer and declareField
     * implementations.
     *
     * @param        containerName   the container name
     * @return true if already declared, false if not
     *
     */
    private boolean checkDeclaredField(String containerName, String fieldName) {

        List<String> fields = declaredFields.get(JahiaContainerSet.SHARED_CONTAINER);
        if (fields != null && fields.contains(fieldName)) {
            return true;
        }
        fields = declaredFields.get(containerName);
        return (fields != null && fields.contains(fieldName));

    } // end checjDeclaredField

    //-------------------------------------------------------------------------
    /***
     * checks if a field name already declared in any containers
     *
     * @param name
     * @return
     */
    private boolean checkDeclaredField(String name) {
        for (List<String> fields : declaredFields.values()) {
            if (fields.contains(name)) {
                return true;
            }
        }
        return false;
    }

    //-------------------------------------------------------------------------
    /***
     * gets the first occurence of a container list
     *
     * @param        containerName   the container name
     * @return a JahiaContainer object
     * @see          org.jahia.data.containers.JahiaContainer
     *
     */
    public JahiaContainer getContainer(String containerName)
            throws JahiaException {
        return getContainer(containerName, 0);
    } // end getContainer

    //-------------------------------------------------------------------------
    /***
     * gets an occurence of a container list
     *
     * @param        containerName   the container name
     * @param        index           the occurence
     * @return a JahiaContainer object
     * @see          org.jahia.data.containers.JahiaContainer
     *
     * @exception JahiaException if container not found
     *
     */
    public JahiaContainer getContainer(String containerName, int index)
            throws JahiaException {

        if (checkDeclared(containerName)) {
            JahiaContainerList theContainerList = (JahiaContainerList)
                    containerLists.get(containerName);
            if (theContainerList != null) {
                JahiaContainer theContainer = theContainerList.getContainer(
                        index);
                if (theContainer != null) {
                    return theContainer;
                } else {
                    String errorMsg = "Container is null : " + containerName +
                            "[" + index + "] -> BAILING OUT";
                    logger.error(errorMsg);
                    throw new JahiaException("Error while returning field " +
                            containerName, errorMsg,
                            JahiaException.TEMPLATE_ERROR,
                            JahiaException.CRITICAL_SEVERITY);
                }
            } else {
                String errorMsg = "Container not declared : " + containerName;
                logger.error(errorMsg + " -> BAILING OUT");
                throw new JahiaException(errorMsg, errorMsg,
                        JahiaException.TEMPLATE_ERROR,
                        JahiaException.CRITICAL_SEVERITY);
            }
        } else {
            String errorMsg = "Container not declared : " + containerName;
            logger.error(errorMsg + " -> BAILING OUT");
            throw new JahiaException(errorMsg, errorMsg,
                    JahiaException.TEMPLATE_ERROR,
                    JahiaException.CRITICAL_SEVERITY);
        }
    } // end getContainer

    //-------------------------------------------------------------------------
    /***
     * gets a container through its ID
     *
     * @param        ctnid     the container ID
     * @return a JahiaContainer object (may be null)
     * @see          org.jahia.data.containers.JahiaContainer
     *
     * @exception JahiaException if container not found
     *
     * !!! Warning : this method can return a null value !!!
     *
     */
    public JahiaContainer getContainer(int ctnid)
            throws JahiaException {

        for (JahiaContainerList aList : containerLists.values()) {
            for (JahiaContainer aContainer : aList.getContainersList()) {
                if (aContainer.getID() == ctnid) {
                    return aContainer;
                }
            }
        }
        return null;
    } // end getContainer

    //-------------------------------------------------------------------------
    /***
     * gets a containerlist through its name
     *
     * @param        containerName   the container list name
     * @return a JahiaContainerList object
     * @see          org.jahia.data.containers.JahiaContainerList
     *
     * @exception JahiaException if container list not found
     *
     */
    public JahiaContainerList getContainerList(String containerName)
            throws JahiaException {
     return getContainerList(containerName, null);   
    }
    
    //-------------------------------------------------------------------------
    /***
     * gets a containerlist through its name
     *
     * @param        containerName   the container list name
     * @param        listViewId   the container list ID in the tag for supporting 
     *                                    different views of the same list 
     * @return a JahiaContainerList object
     * @see          org.jahia.data.containers.JahiaContainerList
     *
     * @exception JahiaException if container list not found
     *
     */
    public JahiaContainerList getContainerList(String containerName, String listViewId)
            throws JahiaException {
        String n = null;
        try {
            n = JahiaPageDefinition.getContentDefinitionInstance(new PageDefinitionKey(page.getPageTemplateID())).getNodeType().getName();
        } catch (Exception e) {
            // fsd
        }
        n = n.replace(':','_')+"_";
        if (!containerName.startsWith(n)) {
            containerName = n + containerName;
        }

//        try {
//            ExtendedNodeType type = JahiaPageDefinition.getContentDefinitionInstance(new PageDefinitionKey(page.getPageTemplateID())).getNodeType();
//            n = type.getChildNodeDefinitionsAsMap().get(containerName).getDeclaringNodeType().getName();
////            n = .getName();
//            n = n.replace(':','_')+"_";
//            if (!containerName.startsWith(n)) {
//                containerName = n + containerName;
//            }
//        } catch (Exception e) {
//            // fsd
//        }

        if (checkDeclared(containerName)) {
            JahiaContainerList theContainerList = (JahiaContainerList)
                    containerLists.get((listViewId != null
                            && listViewId.length() > 0? listViewId + "_"
                                    : "")
                                    + containerName);
            boolean bypass = false;
            if (theContainerList == null) {
                int clistID = jahiaContainersService.getContainerListID(containerName,
                                page.getID());
                if (clistID != -1) {
                    if (requestedLists == null || requestedLists.contains(clistID)) {
                        theContainerList = getContainerList(clistID, page.getID());
                    } else {
                        JahiaContainerDefinition containerDefinition =
                                JahiaContainerDefinitionsRegistry.getInstance().
                                        getDefinition(page.getSiteID(), containerName);
                        theContainerList = new JahiaContainerList(0, 0, page.getID(), containerDefinition.getID(), 0);
                        bypass = true;
                    }
                }
            }
            
            if (theContainerList != null && theContainerList.getID() != 0
                    && !theContainerList.getContentContainerList().hasActiveOrStagingEntries()) {
                // create staged entries
                jahiaContainersService.saveContainerListInfo(
                        theContainerList, theContainerList.getACL().getParentID(),
                        processingContext);
            }

            if ((theContainerList != null && theContainerList.getID() == 0) || theContainerList == null) {
                if (!bypass) {
                JahiaContainerDefinition containerDefinition =
                        JahiaContainerDefinitionsRegistry.getInstance().
                                getDefinition(page.getSiteID(), containerName);
                // just create the container list now
                //
                // we created the container list but we return a containerlist with id==0 !!!!
                // The consequence is that in the template, one can check for id==0 and create the
                // container list again !!!!!!
                    if (containerDefinition != null) {
                        theContainerList = ensureContainerList(containerDefinition,
                                                               page.getID(), 0, listViewId);
                    }
                }
            }

            if (theContainerList != null) {
                /* Load on first access only
                if ( !theContainerList.isContainersLoaded() && theContainerList.getID() != 0){ // not a fake
                    ContainerFactory.getInstance()
                            .fullyLoadContainerList(theContainerList,LoadFlags.ALL,
                            this.jData.getProcessingContext(),usedLoadRequest,
                            this.cachedFieldsFromContainers,
                            this.cachedContainersFromContainerLists,
                            this.cachedContainerListsFromContainers);
                }*/
                addContainerList(theContainerList, listViewId);
            }
            return theContainerList;
        } else {
            String errorMsg = "Container not declared : " + containerName;
            logger.error(errorMsg + " -> BAILING OUT");
            throw new JahiaException(errorMsg, errorMsg,
                    JahiaException.TEMPLATE_ERROR,
                    JahiaException.CRITICAL_SEVERITY);
        }
    } // end getQueryContainerList

    //-------------------------------------------------------------------------
    /***
     * gets a containerlist through its ID
     *
     * @param        listID     the container list ID
     * @return a JahiaContainerList object, may return null
     * @see          org.jahia.data.containers.JahiaContainerList
     *
     */
    public JahiaContainerList getContainerList(int listID) throws JahiaException {
        return getContainerList(listID, null);
    }
    /***
     * gets a containerlist through its ID
     *
     * @param        listID     the container list ID
     * @param        listViewId     the ID in containerList-tag to support multiple views of same list in a page
     * @return a JahiaContainerList object, may return null
     * @see          org.jahia.data.containers.JahiaContainerList
     *
     */
    public JahiaContainerList getContainerList(int listID, String listViewId) throws JahiaException {

        for (Map.Entry<String, JahiaContainerList> entry : containerLists.entrySet()) {
            JahiaContainerList aList = entry.getValue(); 
            if (aList.getID() == listID
                    && (listViewId == null
                            || listViewId.length() == 0 || entry.getKey()
                            .startsWith(listViewId + "_"))) {
                if (!aList.isContainersLoaded()) {
                    // When requesting an archived loadRequest
                    EntryLoadRequest loadRequest =
                            (EntryLoadRequest) processingContext.
                                    getEntryLoadRequest().clone();
                    if (processingContext.showRevisionDiff()) {
                        loadRequest.setWithDeleted(true);
                        loadRequest.setWithMarkedForDeletion(true);
                    } else {
                        loadRequest.setWithDeleted(false);
                        if (processingContext.getOpMode().equals(ProcessingContext.EDIT)) {
                            loadRequest.setWithMarkedForDeletion(org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects());
                        } else {
                            loadRequest.setWithMarkedForDeletion(false);
                        }
                    }
                    ContainerFactory.getInstance()
                            .fullyLoadContainerList(aList, LoadFlags.ALL,
                                    processingContext, loadRequest,
                                    this.cachedFieldsFromContainers,
                                    this.cachedContainersFromContainerLists,
                                    this.cachedContainerListsFromContainers, listViewId);
                }
                return aList;
            }
        }
        return null;
    } // end getQueryContainerList

    //-------------------------------------------------------------------------
    /***
     * gets a container list in another page (absolute page reference by its id)
     *
     * @param        containerName   the container list name
     * @param        pageID          the page ID
     * @return a JahiaContainerList object, or null if nothing found
     * @see          org.jahia.data.containers.JahiaContainerList
     *
     * !!! Warning : this method can return a null value !!!
     * !!! Warning 2 : this method should *NEVER* be called with the name
     * of a sub container list !
     */
    public JahiaContainerList getAbsoluteContainerList(String containerName,
            int pageID) throws JahiaException {
        return getAbsoluteContainerList(containerName, pageID, null);
    }
    
    //-------------------------------------------------------------------------
    /***
     * gets a container list in another page (absolute page reference by its id)
     *
     * @param        containerName   the container list name
     * @param        pageID          the page ID
     * @param        listViewId   the container list ID in the tag for supporting 
     *                                    different views of the same list  
     * @return a JahiaContainerList object, or null if nothing found
     * @see          org.jahia.data.containers.JahiaContainerList
     *
     * !!! Warning : this method can return a null value !!!
     * !!! Warning 2 : this method should *NEVER* be called with the name
     * of a sub container list !
     */
    public JahiaContainerList getAbsoluteContainerList(String containerName,
                                                       int pageID, String listViewId)
            throws JahiaException {

        // quick check for a valid pageID
        if (pageID <= 0) {
            logger.error("Called with invalid pageID of " + pageID +
                    ", returning null container list.");
            throw new JahiaException(
                    "Error while loading absolute container list",
                    "Invalid pageID passed to an absolute container list call : pageID=" +
                            pageID + ", containerName=" + containerName,
                    JahiaException.DATA_ERROR,
                    JahiaException.ERROR_SEVERITY);

        }

        String n = null;
        try {
            n = JahiaPageDefinition.getContentDefinitionInstance(new PageDefinitionKey(ContentPage.getPage(pageID).getPageTemplateID(processingContext.getEntryLoadRequest()))).getNodeType().getName();
        } catch (Exception e) {
            // fsd
        }
        n = n.replace(':','_')+"_";
        if (!containerName.startsWith(n)) {
            containerName = n + containerName;
        }

//        try {
//            ExtendedNodeType type = JahiaPageDefinition.getContentDefinitionInstance(new PageDefinitionKey(ContentPage.getPage(pageID).getPageTemplateID(processingContext.getEntryLoadRequest()))).getNodeType();
//            n = type.getChildNodeDefinitionsAsMap().get(containerName).getDeclaringNodeType().getName();
////            n = type.getName();
//            n = n.replace(':','_')+"_";
//            if (!containerName.startsWith(n)) {
//                containerName = n + containerName;
//            }
//        } catch (Exception e) {
//            // fsd
//        }


        // now let's check if we are accessing a top level container list or
        // not using only the definitions (as the container lists might not
        // yet exist). If it's not a top level container list, throw an
        // exception !
        JahiaContainerDefinition containerDefinition =
                JahiaContainerDefinitionsRegistry.getInstance().
                        getDefinition(page.getSiteID(), containerName);

        if (containerDefinition != null) {
            /**
             * todo possible bug here, should we check the parent sub
             * container definitions for their page ID ? Because container
             * definitions are not local to a page, but sub container definitions
             * are...
             */
            if (org.jahia.settings.SettingsBean.getInstance().isDevelopmentMode() && jahiaContainersService.
                    hasContainerDefinitionParents(containerDefinition.getID())) {
                throw new JahiaException(
                        "Error while loading absolute container list",
                        "Cannot load a sub container list with this method ! " +
                                pageID,
                        JahiaException.DATA_ERROR,
                        JahiaException.ERROR_SEVERITY);
            }
        }
        JahiaContainerList theContainerList = null;
        int containerListID = jahiaContainersService.
                getContainerListID(containerName, pageID);
        if (containerListID != -1) {
            if (requestedLists == null || requestedLists.contains(containerListID)) {
                theContainerList = getContainerList(containerListID, pageID);

                /** what this code does for NK **/
                if (theContainerList == null) {
                    theContainerList = this.getContainerList(containerName);
                    theContainerList.setID(containerListID);
                } else {
                    int containerType = containerDefinition.getContainerListType();
                    if ((containerType & JahiaContainerDefinition.MANDATORY_TYPE) != 0) {
                        ensureMandatoryContainer(theContainerList, containerType);
                    }
                }
            } else {
                theContainerList = new JahiaContainerList(0, 0, processingContext.getPage().getID(), containerDefinition.getID(), 0);

            }
        } else if (containerDefinition != null) {
            ContentPage contentPage = ContentPage.getPage(pageID);
            int pageACLID = contentPage.getAclID();

            JahiaContainerList fakeContainerList = new JahiaContainerList(0, 0, 
                    pageID, containerDefinition.getID(), 0);

            jahiaContainersService.saveContainerListInfo(fakeContainerList, pageACLID, processingContext);
            
            int containerType = containerDefinition.getContainerListType();
            if ((containerType & JahiaContainerDefinition.MANDATORY_TYPE) != 0) {
                ensureMandatoryContainer(fakeContainerList, containerType);
            }
            
            theContainerList = fakeContainerList;
        }

        if (theContainerList != null) {
            if (pageID != this.page.getID()) {
                // let's update cross reference list
                absoluteContainerListAccesses.add(new Integer(theContainerList.getID()));
            } else {
                addContainerList(theContainerList, listViewId);                
            }
        }    
        return theContainerList;
    } // end getAbsoluteContainerList
    
    private JahiaContainerList getContainerList(int containerListID, int pageID){
        JahiaContainerList theContainerList = null;
        try {
            Map<Integer, List<Integer>> currentCachedFieldsFromContainers = this.cachedFieldsFromContainers;
            Map<Integer, List<Integer>> currentCachedContainersFromContainerLists = this.cachedContainersFromContainerLists;
            Map<Integer, List<Integer>> currentCachedContainerListsFromContainers = this.cachedContainerListsFromContainers;
            if ( pageID != this.page.getID()){
                cachedFieldsFromContainers= new HashMap<Integer, List<Integer>>();
                cachedContainersFromContainerLists= new HashMap<Integer, List<Integer>>();
                cachedContainerListsFromContainers= new HashMap<Integer, List<Integer>>();
            }

            EntryLoadRequest ctnLoadRequest = processingContext.
                    getEntryLoadRequest();
            if (ctnLoadRequest.isVersioned()) {
                // does the container exists at this archive date
                theContainerList = jahiaContainersService.loadContainerListInfo(containerListID,
                                ctnLoadRequest);
                if (theContainerList != null && theContainerList.getID()>0){
                    theContainerList = ServicesRegistry.getInstance().getJahiaContainersService().loadContainerList(
                        theContainerList.getID(), LoadFlags.ALL, processingContext, ctnLoadRequest,
                        currentCachedFieldsFromContainers, currentCachedContainersFromContainerLists,
                        currentCachedContainerListsFromContainers);
                }
            }
            if (ctnLoadRequest.isVersioned() && theContainerList == null) {
                // return the active version of the container list but without any containers !
                theContainerList = jahiaContainersService.loadContainerListInfo(containerListID);
                // to avoid reload of containers
                theContainerList.setIsContainersLoaded(true);
            } else {

                // When requesting an archived loadRequest
                EntryLoadRequest loadRequest =
                        (EntryLoadRequest) ctnLoadRequest.clone();
                if (processingContext.showRevisionDiff()) {
                    loadRequest.setWithDeleted(true);
                    loadRequest.setWithMarkedForDeletion(true);
                } else {
                    loadRequest.setWithDeleted(false);
                    if (processingContext.getOpMode().equals(ProcessingContext.EDIT)) {
                        loadRequest.setWithMarkedForDeletion(org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects());
                    } else {
                        loadRequest.setWithMarkedForDeletion(false);
                    }
                }

                if (theContainerList == null) {
                    /*
                     * @fixme : what is the reason to not use lazy load ?
                     * Khue commented that.
                    theContainerList = ContainerFactory.getInstance()
                            .fullyLoadContainerList(containerListID,
                                    LoadFlags.ALL, this.jData.getProcessingContext(),
                                    loadRequest,
                                    currentCachedFieldsFromContainers,
                                    currentCachedContainersFromContainerLists,
                                    currentCachedContainerListsFromContainers);
                    */
                    theContainerList = ServicesRegistry.getInstance().getJahiaContainersService().loadContainerList(
                            containerListID, LoadFlags.ALL, processingContext, loadRequest,
                            currentCachedFieldsFromContainers, currentCachedContainersFromContainerLists,
                            currentCachedContainerListsFromContainers);
                }
                /*
                 * @fixme : what is the reason to not use lazy load ? Khue
                 * commented that. 
                 * else if (!theContainerList.isContainersLoaded()) {
                 * ContainerFactory.getInstance()
                 *     .fullyLoadContainerList(theContainerList, LoadFlags.ALL,
                 *         this.jData.getProcessingContext(), loadRequest,
                 *         currentCachedFieldsFromContainers,
                 *         currentCachedContainersFromContainerLists,
                 *         currentCachedContainerListsFromContainers); }
                 */                
            }
        } catch (final Throwable t) {
            logger.error("Error in getAbsoluteContainerList", t);
        }
        
        return theContainerList;
    }

    /**
     * Retrieves an absolutely referenced container list by its name and page URL key.
     *
     * @param        containerName   the container list name
     * @param        pageUrlKey      the page URL key
     * @return a JahiaContainerList object, or null if nothing found
     * @see          org.jahia.data.containers.JahiaContainerList
     */
    public JahiaContainerList getAbsoluteContainerList(String containerName,
                                                       String pageUrlKey)
            throws JahiaException {
        return getAbsoluteContainerList(containerName, ServicesRegistry
                .getInstance().getJahiaPageService()
                .getPageIDByURLKeyAndSiteID(pageUrlKey,
                        page.getSiteID()), null);
    }       
    
    /**
     * Retrieves an absolutely referenced container list by its name and page URL key.
     *
     * @param        containerName   the container list name
     * @param        pageUrlKey      the page URL key
     * @param        listViewId   the container list ID in the tag for supporting 
     *                                    different views of the same list   
     * @return a JahiaContainerList object, or null if nothing found
     * @see          org.jahia.data.containers.JahiaContainerList
     */
    public JahiaContainerList getAbsoluteContainerList(String containerName,
                                                       String pageUrlKey, String listViewId)
            throws JahiaException {
        return getAbsoluteContainerList(containerName, ServicesRegistry
                .getInstance().getJahiaPageService()
                .getPageIDByURLKeyAndSiteID(pageUrlKey,
                        page.getSiteID()), listViewId);
    }    

    //-------------------------------------------------------------------------
    /***
     * gets a container list in another page (relative page reference by
     * number of levels to go up)
     *
     * @param        containerName   the container list name
     * @param        levelNb         the numbers of level to go up in the
     *                               site structure;
     *                               -1 = Home Page
     * @param        listViewId   the container list ID in the tag for supporting 
     *                                    different views of the same list 
     * @return a JahiaContainerList object, or null if nothing found
     * @see          org.jahia.data.containers.JahiaContainerList
     *
     * !!! Warning : this method can return a null value !!!
     */
    public JahiaContainerList getRelativeContainerList(String containerName,
                                                       int levelNb, String listViewId)
            throws JahiaException {
        logger.debug("into relative....................");
        int pageID = ServicesRegistry.getInstance().getJahiaPageService().
                findPageIDFromLevel(
                        page.getID(), levelNb, processingContext);
        logger.debug(".........page id found : " + pageID);
        if (pageID != -1) {
            return getAbsoluteContainerList(containerName, pageID, listViewId);
        } else {
            return null;
        }
    } // end getRelativeContainerList


    /**
     * Returns a map that contains the current mapping of container list -> page
     * that represent the container lists that have been accessed by pages using
     * the absolute addressing method.
     *
     * @return a Set containing Integer objects that represent the container
     *         list IDs that have been accessed absolutely from the current page.
     */
    public Set<Integer> getAbsoluteContainerListAccesses() {
        return absoluteContainerListAccesses;
    }

    /*
        Map interface implementation methods
     */

    public boolean equals(Object o) {
        return containerLists.equals(o);
    }

    public int hashCode() {
        return containerLists.hashCode();
    }

    public Set<Map.Entry<String, JahiaContainerList>> entrySet() {
        return containerLists.entrySet();
    }

    public Collection<JahiaContainerList> values() {
        return containerLists.values();
    }

    public Set<String> keySet() {
        return containerLists.keySet();
    }

    public void clear()
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "Jahia container lists collection is read-only!");
    }

    public void putAll(Map t)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "Jahia container lists collection is read-only!");
    }

    public Object remove(Object key)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "Jahia container lists collection is read-only!");
    }

    public Object put(Object key,
                      Object value)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "Jahia container lists collection is read-only!");
    }

    public Object get(Object key) {
        return containerLists.get(key);
    }

    public boolean containsValue(Object value) {
        return containerLists.containsValue(value);
    }

    public boolean containsKey(Object key) {
        return containerLists.containsKey(key);
    }

    public boolean isEmpty() {
        return containerLists.isEmpty();
    }

    public int size() {
        return containerLists.size();
    }

    public JahiaContainerList ensureContainerList(JahiaContainerDefinition def, int page, int parent, String listViewId) throws JahiaException {
        synchronized (JahiaContainerSet.CLASS_NAME) {
            int id = -1;
            if (parent == 0) {
                id = jahiaContainersService.getContainerListID(def.getName(), page);
            } else {
                id = jahiaContainersService.getContainerListID(def.getName(), page, parent);
            }
            JahiaContainerList list;
            if (id == -1) {
                int parentAclID = 0;
                if (parent != 0) {
                    parentAclID = jahiaContainersService.
                            loadContainerInfo(parent, processingContext.getEntryLoadRequest()).
                            getAclID();
                } else {
                    parentAclID = this.page.getAclID();
                }
                list = new JahiaContainerList(0, parent, page, def.getID(), 0);
                jahiaContainersService.saveContainerListInfo(list, parentAclID, processingContext);
            } else {
                list = jahiaContainersService.loadContainerListInfo(id, processingContext.getEntryLoadRequest());
            }
            if (list != null) {
                // When requesting an archived loadRequest
                EntryLoadRequest loadRequest = (EntryLoadRequest) processingContext.getEntryLoadRequest().clone();
                if (loadRequest.isVersioned()) {
                    if (processingContext.showRevisionDiff()) {
                        // todo not fully handle so we dont load deleted yet
                        loadRequest.setWithDeleted(true);
                        loadRequest.setWithMarkedForDeletion(true);
                    } else {
                        loadRequest.setWithDeleted(false);
                        if (processingContext.getOpMode().equals(ProcessingContext.EDIT)) {
                            loadRequest.setWithMarkedForDeletion(org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects());
                        } else {
                            loadRequest.setWithMarkedForDeletion(false);
                        }
                    }
                }
                try {
                    list = ContainerFactory.getInstance().fullyLoadContainerList(list.getID(),
                            LoadFlags.ALL,
                            processingContext, loadRequest,
                            this.cachedFieldsFromContainers,
                            this.cachedContainersFromContainerLists,
                            this.cachedContainerListsFromContainers);
                } catch (final Throwable t) {
                   logger.error("Error in getContainerList", t);
                }
                this.addContainerList(list, listViewId);

                ensureMandatoryContainer(list, def.getContainerListType());
            }
            return list;
        }
    }

    private void ensureMandatoryContainer(JahiaContainerList list, int containerType) throws JahiaException{
        if (((containerType & JahiaContainerDefinition.MANDATORY_TYPE) != 0)
                && jahiaContainersService.getctnidsInList(list.getID(), EntryLoadRequest.STAGED).isEmpty()) {

            JahiaContainer container = new JahiaContainer(0,
                    processingContext.getJahiaID(),
                    list.getPageID(),
                    list.getID(),
                    0, /* rank */
                    list.getAclID(),
                    list.getctndefid(),
                    0, 2);
            jahiaContainersService.
                    saveContainer(container, list.getID(), processingContext);
            container.setLanguageCode(processingContext.getLocale().toString());
            container.fieldsStructureCheck(processingContext);
            jahiaContainersService.
                    saveContainer(container, list.getID(), processingContext);
            list.addContainer(container);
        }        
    }
    
    /**
     * internal method to trace re-definition callers
     * @param t the throwable
     * @param methodName the name of method to trace
     */
    private void traceCaller(Throwable t,String methodName){
        String requestURI = Jahia.getThreadParamBean().getRequestURI();
        String templateName = page.getPageTemplate().getName();
        String templateSource = page.getPageTemplate().getSourcePath();
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

    public Set<Integer> getRequestedContainers() {
        return requestedContainers;
    }

    public Set<Integer> getRequestedLists() {
        return requestedLists;
    }
} // end JahiaContainerSet
