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

package org.jahia.data.containers;

import org.jahia.content.PropertiesInterface;
import org.jahia.data.fields.JahiaContentFieldFacade;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaContainerDefinitionsRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.ACLResourceInterface;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.containers.ContainerFactoryProxy;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.utils.LanguageCodeConverters;

import java.io.Serializable;
import java.util.*;

public class JahiaContainer implements Serializable, Cloneable, ACLResourceInterface, PropertiesInterface {

    private static final long serialVersionUID = -1475638714225472196L;

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JahiaContainer.class);

    private int ID;
    private int jahiaID;
    private int pageID;
    private int listID;
    private int rank;
    private int ctndefid;
    private int aclID;
    private int versionID;
    private int workflowState;
    private String languageCode = "";

    private boolean     isContainerListsLoaded = false;
    private boolean     isFieldsLoaded = false;
    private boolean     isChanged = true;

    private int contextualContainerListID;

    private Properties  ctnProperties = null;

    /**
     * @associates JahiaField
     */
    private List<JahiaField> fields = new ArrayList<JahiaField>();

    /**
     * @associates JahiaContainerList
     */
    private List<JahiaContainerList> containerLists = new ArrayList<JahiaContainerList>();

    private Map<String, Object> children = new HashMap<String, Object>();

    private ContainerFactoryProxy cFactoryProxy;

    // no-parameter constructor required for JavaBean pattern compliance so
    // that these objects may be used with JSPs
    public JahiaContainer() {
        this.workflowState = EntryLoadRequest.STAGING_WORKFLOW_STATE;
    }

    /***
     * constructor
     * EV    24.11.2000
     *
     */
    public JahiaContainer (int ID,
                           int jahiaID,
                           int pageID,
                           int listID,
                           int rank,
                           int aclID,
                           int ctndefid,
                           int versionID,
                           int workflowState) {
        this.ID = ID;
        this.jahiaID = jahiaID;
        this.pageID = pageID;
        this.listID = listID;
        this.rank = rank;
        this.aclID = aclID;
        this.ctndefid = ctndefid;
        this.versionID = versionID;
        this.workflowState = workflowState;

    } // end constructor

    /**
     * Set the containerFactory used to control how this container's will be loaded
     *
     */
    public void setFactoryProxy(ContainerFactoryProxy cFactoryProxy){
        this.cFactoryProxy = cFactoryProxy;
    }

    /***
     * returns the container id
     * EV    24.11.2000
     *
     */
    public final int getID () {
        return ID;
    }

    public final int getJahiaID () {
        return jahiaID;
    }
    /**
     * returns the id of the site which contains the container.
     * @return int
     */
    public final int getSiteID () {
        return jahiaID;
    } //FIXME_MULTISITE Hollis jahiaID or siteID ?

    /**
     * returns the id of the page which contains the container
     * @return int
     */
    public final int getPageID () {
        return pageID;
    }

    public final int getListID () {
        return listID;
    }
    /**
     * returns the container rank.
     * @return int
     */
    public final int getRank () {
        return rank;
    }

    public final int getctndefid () {
        return ctndefid;
    }

    public final int getAclID () {
        ContentContainer container = getContentContainer();
        if (container != null) {
            return container.getAclID();
        }
        return aclID;
    }

    public final int getVersionID () {
        return versionID;
    }

    /**
     * This is the id of the container list from which this container is returned.
     * This container list may be different (a container used to aggregate containers from other container list
     * using Filters in exemple ) than the real unique parent container list of this container.
     *
     * @return
     */
    public int getContextualContainerListID() {
        return contextualContainerListID;
    }

    public void setContextualContainerListID(int contextualContainerListID) {
        this.contextualContainerListID = contextualContainerListID;
    }

    public void setVersionID (int versionID) {
        this.versionID = versionID;
    }

    public final int getWorkflowState () {
        return workflowState;
    }

    public void setWorkflowState (int wf) {
        this.workflowState = wf;
    }

    public final String getLanguageCode () {
        return languageCode;
    }

    public final JahiaBaseACL getACL () {
        JahiaBaseACL acl = null;
        try {
            acl = JahiaBaseACL.getACL(getAclID());
//            acl = new JahiaBaseACL(getAclID());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return acl;
    }
    /**
     * returns the field list.
     * @return Enumeration
     */
    public Iterator<JahiaField> getFields () {
        checkProxy(ContainerFactoryProxy.LOAD_FIELDS);
        return fields.iterator();
    }

    public int getNbFields () {
        checkProxy(ContainerFactoryProxy.LOAD_FIELDS);
        return fields.size();
    }

    public Iterator<JahiaContainerList> getContainerLists () {
        checkProxy(ContainerFactoryProxy.LOAD_SUBCONTAINER_LISTS);
        return containerLists.iterator();
    }

    public void setID (int ID) {
        this.ID = ID;
    }

    public void setctndefid (int ctndefid) {
        if (this.ctndefid == ctndefid) {
            return;
        }
        this.ctndefid = ctndefid;
        this.isChanged = true;
    }

    public void setAclID (int aclID) {
        this.aclID = aclID;
        if (this.aclID == aclID) {
            return;
        }
        this.isChanged = true;
    }

    public void setListID (int listID) {
        if (this.listID == listID) {
            return;
        }
        this.listID = listID;
        this.isChanged = true;
    }

    public void setRank (int rank) {
        if (this.rank == rank) {
            return;
        }
        this.rank = rank;
        this.isChanged = true;
    }

    public void setLanguageCode (String languageCode) {
        this.languageCode = languageCode;
    }

    public boolean isContainerListsLoaded(){
        return this.isContainerListsLoaded;
    }

    public boolean isFieldsLoaded(){
        return this.isFieldsLoaded;
    }

    public boolean isChanged() {
        return isChanged;
    }

    /**
     * @param val
     */
    public void setContainerListsLoaded ( boolean val ){
        this.isContainerListsLoaded = val;
    }

    /**
     *
     * @param val
     */
    public void setFieldsLoaded ( boolean val ){
        this.isFieldsLoaded = val;
    }

    public void setChanged(boolean changed) {
        isChanged = changed;
    }

    /**
     * Empty the list of fields
     */
    public void clearFields(){
        if ( this.fields != null && !this.fields.isEmpty()){
            for (JahiaField f : fields){
                try {
                    this.children.remove(f.getDefinition().getName());
                } catch ( JahiaException je ){
                    logger.debug("Exception",je);
                }
            }
        }
        this.fields = new ArrayList<JahiaField>();
    }

    /**
     * Empty the list of sub container lists
     */
    public void clearContainerLists(){
        if ( this.containerLists != null && !this.containerLists.isEmpty() ){
            for (JahiaContainerList cList : containerLists){
                try {
                    this.children.remove(cList.getDefinition().getName());
                } catch ( JahiaException je ){
                    logger.debug("Exception",je);
                }
            }
        }
        this.containerLists = new ArrayList<JahiaContainerList>();
    }

    // end accessor methods

    //-------------------------------------------------------------------------
    /**
     * Clone
     */
    public Object clone () {
        JahiaContainer container = new JahiaContainer(ID, jahiaID, pageID,
            listID, rank, aclID, ctndefid, versionID, workflowState);
        container.setLanguageCode(languageCode);
        container.setChanged(isChanged());
        return container;
    }

    //-------------------------------------------------------------------------
    /***
     * getDefinition
     *
     */
    public JahiaContainerDefinition getDefinition ()
        throws JahiaException {
        JahiaContainerDefinition theDef = JahiaContainerDefinitionsRegistry.
                                          getInstance(
            ).getDefinition(ctndefid);
        if (theDef != null) {
            return theDef;
        } else {
            String msg = "JahiaContainer definition " + ctndefid +
                         " not found in definition registry !";
            throw new JahiaException("Synchronisation error in database",
                                     msg, JahiaException.DATABASE_ERROR,
                                     JahiaException.CRITICAL_SEVERITY);
        }
    } // end getDefinition

    //-------------------------------------------------------------------------
    /**
     * get a field value
     *
     * @param        fieldName       the field name
     * @exception   JahiaException
     *      throws a critical jahia exception if field not found
     */
    public String getFieldValue( String fieldName )
    throws JahiaException
    {
        return getFieldValue(fieldName,false,null);
    }

    //-------------------------------------------------------------------------
    /**
     * get a field value
     *
     * @param fieldName
     * @param allowDiffVersionHighlight
     * @return
     * @exception   JahiaException
     *      throws a critical jahia exception if field not found
     */
    public String getFieldValue( String fieldName ,
                                 boolean allowDiffVersionHighlight,
                                 ProcessingContext jParams)
    throws JahiaException
    {
        checkProxy(ContainerFactoryProxy.LOAD_FIELDS);
        JahiaField theField = null;
        for (JahiaField aField : fields) {
            if (aField.getDefinition().getName().equals(fieldName) || aField.getDefinition().getName().equals(getDefinition().getName() + "_" + fieldName)) {
                theField = aField;
                break;
            }
        }
        if ( theField != null ){
            if ( !allowDiffVersionHighlight ){
                return theField.getValue();
            } else {
                return theField.getHighLightDiffValue(jParams);
            }
        }
        return null;
    }

    //-------------------------------------------------------------------------
    /**
     * get a field value
     *
     * @param        fieldName       the field name
     * @param        defaultValue
     * @exception   JahiaException
     *      throws a critical jahia exception if field not found
     */
    public String getFieldValue( String fieldName ,
                                 String defaultValue ,
                                 boolean allowDiffVersionHighlight ,
                                 ProcessingContext jParams )
    throws JahiaException
    {
        String value =
                getFieldValue(fieldName,allowDiffVersionHighlight,jParams);
        if ( value == null ){
            return defaultValue;
        }
        return value;
    }

    //-------------------------------------------------------------------------
    /**
     * get a field value
     *
     * @param        fieldName       the field name
     * @param        defaultValue
     * @exception   JahiaException
     *      throws a critical jahia exception if field not found
     */
    public String getFieldValue( String fieldName , String defaultValue )
    throws JahiaException
    {
        String value = getFieldValue(fieldName);
        if ( value == null ){
            return defaultValue;
        }
        return value;
    }

    //-------------------------------------------------------------------------
    /**
     * get a field value
     *
     * @param        fieldName       the field name
     * @exception   JahiaException
     *      throws a critical jahia exception if field not found
     */
    public Object getFieldObject (String fieldName)
            throws JahiaException {
        checkProxy(ContainerFactoryProxy.LOAD_FIELDS);
        if (ctndefid != 0) {
            String s = getDefinition().getName() + "_";
            if (!fieldName.startsWith(s)) {
                fieldName = s + fieldName;
            }
        }
        JahiaField theField = (JahiaField) children.get(fieldName);
        if (theField != null) {
            return theField.getObject();
        }
        return null;
    }

    //-------------------------------------------------------------------------
    /***
     * get a field by its fieldname
     * (returns the JahiaField object of name �fieldName" contained in this container)
     *
     * @param        fieldName       the field name
     *
     * @exception   JahiaException
     *      throws a critical jahia exception if field not found
     *
     */
    public JahiaField getField (String fieldName)
            throws JahiaException {
        checkProxy(ContainerFactoryProxy.LOAD_FIELDS);
        if (ctndefid != 0) {
            String s = getDefinition().getName() + "_";
            if (!fieldName.startsWith(s)) {
                fieldName = s + fieldName;
            }
        }
        JahiaField theField = (JahiaField) children.get(fieldName);
        return theField;
    } // end getField

    public JahiaField getFieldByName(String fieldName)
        throws JahiaException {
        return getField(fieldName);
    }

    //-------------------------------------------------------------------------
    /***
     * get a field by its id
     * (returns the JahiaField object of id "fieldID" contained in this container)
     * @param        fieldID         the field ID
     *
     * @exception   JahiaException
     *      throws a critical jahia exception if field not found
     *
     */
    public JahiaField getField (int fieldID)
        throws JahiaException {
        checkProxy(ContainerFactoryProxy.LOAD_FIELDS);
        JahiaField theField = null;
        for (JahiaField aField : fields) {
            if (aField.getID() == fieldID) {
                theField = aField;
                break;
            }
        }
        if (theField == null) {
            String name = "<no container definition>";
            if ( this.getctndefid()>0 ){
                name = getDefinition().getName();
            }
            String msg = "Field " + fieldID + " cannot be found in " + name;

            JahiaException je = new JahiaException(msg,
                "JahiaContainer : " + msg,
                JahiaException.TEMPLATE_ERROR, JahiaException.CRITICAL_SEVERITY);
            logger.debug(msg, je);
            
            throw je;            
        }
        return theField;
    } // end getField

    //-------------------------------------------------------------------------
    /***
     * setField
     *
     */
    public  void setField (JahiaField theField)
        throws JahiaException {
        checkProxy(ContainerFactoryProxy.LOAD_FIELDS);
        for (ListIterator<JahiaField> it = fields.listIterator(); it.hasNext(); ) {
            JahiaField aField = it.next();
            if (aField.getID() == theField.getID()) {
                it.set(theField);
                children.put(theField.getDefinition().getName(), theField);
            }
        }
    } // end addField

    //-------------------------------------------------------------------------
    /***
     * addField
     * EV    27.12.2000
     *
     */
    public  void addField (JahiaField theField) {
        if (theField != null) {
            try {
                if ( theField.getDefinition().getItemDefinition() != null ) {
                    fields.add(theField);
                    children.put(theField.getDefinition().getName(), theField);
                }
            } catch (JahiaException je) {
                logger.error("Error while inserting field " + theField.getID() +
                             " into container children map", je);
            } catch (NullPointerException npe) {
                logger.error("Error while inserting field " + theField.getID() +
                             " into container children map", npe);
            }
        }
    } // end addField

    //-------------------------------------------------------------------------
    /***
     * addField
     * EV    27.12.2000
     *
     */
    public void addFields(List<JahiaField> theFields) {
        if (theFields != null) {
            for (JahiaField theField : theFields) {
                try {
                    if ( theField.getDefinition().getItemDefinition() != null ) {
                        fields.add(theField);                        
                        children.put(theField.getDefinition().getName(), theField);
                    }
                } catch (JahiaException je) {
                    logger.error(
                            "Error while inserting field " + theField.getID()
                                    + " into container children map", je);
                } catch (NullPointerException npe) {
                    logger.error(
                            "Error while inserting field " + theField.getID()
                                    + " into container children map", npe);
                }
            }
        }
    } // end addFields    
    
    //-------------------------------------------------------------------------
    /***
     * getQueryContainerList
     * EV    27.12.2000
     *
     */
    public JahiaContainerList getContainerList (String listName)
        throws JahiaException {
        checkProxy(ContainerFactoryProxy.LOAD_SUBCONTAINER_LISTS);
        if (logger.isDebugEnabled()) {
        logger.debug("looking for child container list: " + listName);
        }
        String s = getDefinition().getName() + "_";
        if (!listName.startsWith(s)) {
            listName = s + listName;
        } else {
            logger.error("---------------> listname already prefixed = " + listName + " / "+s);
        }
        JahiaContainerList theList = (JahiaContainerList) children.get(listName);
        if (theList == null) {
            JahiaContainerDefinition theDef = JahiaContainerDefinitionsRegistry.getInstance().getDefinition(this.jahiaID, listName);
            if (theDef == null) {
                logger.error("Cannot find definition "+listName);
            } else {
                theList = new JahiaContainerList(0, this.getID(), this.getPageID(),
                        theDef.getID(), 0);
            }
        }
        return theList;
    } // end getQueryContainerList

    public JahiaContainerList getContainerList (JahiaContainerDefinition def)
        throws JahiaException {
        checkProxy(ContainerFactoryProxy.LOAD_SUBCONTAINER_LISTS);
        String listName = def.getName();
        JahiaContainerList theList = (JahiaContainerList) children.get(listName);
        if (theList == null) {
            theList = new JahiaContainerList(0, this.getID(), this.getPageID(),
                    def.getID(), 0);
        }
        return theList;
    } // end getQueryContainerList

    //-------------------------------------------------------------------------
    /***
     * addContainerList
     * EV    27.12.20007
     *
     */
    public  void addContainerList (JahiaContainerList theList)
        throws JahiaException {
        checkProxy(ContainerFactoryProxy.LOAD_SUBCONTAINER_LISTS);
        if (theList != null) {
            containerLists.add(theList);
            try {
                children.put(theList.getDefinition().getName(), theList);
            } catch (JahiaException je) {
                logger.error("Error while inserting container list " +
                             theList.getID() +
                             " into container children map", je);
            } catch (NullPointerException npe) {
                logger.error("Error while inserting container list " +
                             theList.getID() +
                             " into container children map", npe);
            }
        }
    } // end addList

    //-------------------------------------------------------------------------
    /***
         * Order the fields in the exact order in which they are declared in template.
     * Create new declared fields for old containers.
     *
     * NK    04.06.2002
     *
     * @param   jParams     reference to the parameter bean
     */
    public void fieldsStructureCheck (ProcessingContext jParams, boolean createMissingFieldsInDB)
        throws JahiaException {

        checkProxy(ContainerFactoryProxy.LOAD_FIELDS);
        if (this.languageCode == null || this.languageCode.trim().equals("")) {
            throw new JahiaException(
                "Not a valid Language Code ( empty Str or null )",
                "Not a valid Language Code ( empty Str or null )",
                JahiaException.DATA_ERROR, JahiaException.CRITICAL_SEVERITY);
        }
        // let's order fields as they appear in template declaration !
        List<String> fieldOrder = new ArrayList<String>();
        List<JahiaField> orderedFields = new ArrayList<JahiaField>();
        Map<String, JahiaFieldDefinition> fieldDefs = new HashMap<String, JahiaFieldDefinition>();

        ContentPage sourcePage = ContentPage.getPage(this.getPageID());
        if (sourcePage != null && this.fields != null) {
            int pageDefID = sourcePage.getPageTemplateID(jParams);
            Iterator<JahiaContainerStructure> structure = this.getDefinition().getStructure(
                    JahiaContainerStructure.JAHIA_FIELD);
            while (structure.hasNext()) {
                JahiaContainerStructure theStruct =
                    structure.next();
                JahiaFieldDefinition theDef =
                    (JahiaFieldDefinition) theStruct.getObjectDef();
                fieldOrder.add(theDef.getName());
                orderedFields.add(null); //fake element
                fieldDefs.put(theDef.getName(), theDef);
            }

            for (JahiaField field : fields) {
                String fieldDefName = field.getDefinition().getName();
                int fieldPos = fieldOrder.indexOf(fieldDefName);
                if (fieldPos != -1) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("JahiaContainer.fieldsStructureCheck : Field "
                                        + fieldDefName
                                        + " has pos : "
                                        + fieldPos);
                    }
                    orderedFields.set(fieldPos, field);
                    children.put(fieldDefName, field);
                    fieldDefs.remove(fieldDefName);
                } else {
                    // seems that we encountered a field for which the definition has benn removed from the container declaration
                    // so we ignore this field
                }
            }

            // We don't want to create new field if they already exist but in another
            // language than this container current language code.
            // so there is some extra check
            if ( this.getID()>0 ){
                List<Integer> fieldIds = ServicesRegistry.getInstance().
                    getJahiaContainersService().getFieldIDsInContainer(this.
                    getID());
                List<Locale> locales = new ArrayList<Locale>();
                locales.add(LanguageCodeConverters.languageCodeToLocale(this.getLanguageCode()));
                EntryLoadRequest loadRequest =
                    new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,
                                         0,locales);
                for ( Integer fieldId : fieldIds){
                    boolean found = false;
                    for (JahiaField f : fields) {
                        if ( f.getID() == fieldId.intValue() ){
                            found = true;
                        }
                    }
                    if ( !found ){
                        JahiaContentFieldFacade fFacade =
                            new JahiaContentFieldFacade(fieldId.intValue(),
                                    LoadFlags.ALL,
                                    jParams,
                                    locales,
                                    true);
                         JahiaField field = fFacade.getField(loadRequest,true);
                         String fieldDefName = field.getDefinition().getName();                         
                         int fieldPos = fieldOrder.indexOf(fieldDefName);
                         if (fieldPos != -1) {
                             if (logger.isDebugEnabled()) {
                                logger.debug("JahiaContainer.fieldsStructureCheck : Field "
                                                + fieldDefName
                                                + " has pos : "
                                                + fieldPos);
                            }
                             orderedFields.set(fieldPos, field);
                             children.put(fieldDefName, field);
                             fieldDefs.remove(fieldDefName);
                         }
                    }
                }
            }

            // We check here if the container declaration has changed and if we have to create new
            // declared field for this container.
            for (JahiaFieldDefinition fieldDef : fieldDefs.values()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("JahiaContainer.fieldsStructureCheck : Field "
                            + fieldDef.getName()
                            + " is missing, we have to create a new one ");
                }
                int fieldType = fieldDef.getType();

                int connectType = 0;
                int rank = 0;
                int aclID = 0;
                String fieldValue = fieldDef.getDefaultValue();
                JahiaSaveVersion saveVersion = ServicesRegistry.getInstance().
                                               getJahiaVersionService().
                                               getSiteSaveVersion(this.
                    getJahiaID());
                JahiaField field = ServicesRegistry.getInstance().
                        getJahiaFieldService().
                        createJahiaField(0, this.getJahiaID(), this.getPageID(),
                                         this.getID(), fieldDef.getID(),
                                         fieldType,
                                         connectType, fieldValue, rank, aclID,
                                         saveVersion.getVersionID(),
                                         saveVersion.getWorkflowState(),
                                         this.languageCode);
                if (field != null) {
                    // save the field
                    if(createMissingFieldsInDB)
                    ServicesRegistry.getInstance().getJahiaFieldService().
                        saveField(field, this.getAclID(), jParams);

                    int fieldPos = fieldOrder.indexOf(fieldDef.getName());
                    if (logger.isDebugEnabled()) {
                        logger.debug("JahiaContainer.fieldsStructureCheck : Field "
                                        + fieldDef.getName()
                                        + " has pos : "
                                        + fieldPos);
                    }
                    orderedFields.set(fieldPos, field);
                    children.put(fieldDef.getName(), field);
                }
            }
            this.fields = orderedFields;
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Check if the user has read access for the specified container. Read access means
     * the user can display container data (he needs to have the rights for each field/
     * containerlist too)
     *
     * @param    user    Reference to the user.
     *
         * @return   Return true if the user has read access for the specified container,
     *           or false in any other case.
     */
    public final boolean checkReadAccess (JahiaUser user) {
        return checkAccess(user, JahiaBaseACL.READ_RIGHTS);
    }

    //-------------------------------------------------------------------------
    /**
     * Check if the user has Write access for the specified container. Write access means
     * updating container data data (he needs to have the rights for each field/containerlist too)
     *
     * @param    user    Reference to the user.
     *
     * @return   Return true if the user has write access for the specified container,
     *           or false in any other case.
     */
    public final boolean checkWriteAccess (JahiaUser user) {
        return checkAccess(user, JahiaBaseACL.WRITE_RIGHTS);
    }

    //-------------------------------------------------------------------------
    /**
     * Check if the user has Admin access for the specified container. Admin access means
     * setting container rights (he needs to have the rights for each field/containerlist too)
     *
     * @param    user    Reference to the user.
     *
     * @return   Return true if the user has admin access for the specified container,
     *           or false in any other case.
     */
    public final boolean checkAdminAccess (JahiaUser user) {
        return checkAccess(user, JahiaBaseACL.ADMIN_RIGHTS);
    }

    //-------------------------------------------------------------------------
    private boolean checkAccess (JahiaUser user, int permission) {
        if (user == null) {
            return false;
        }

        boolean result = false;
        try {
            // Try to instantiate the ACL.
            JahiaBaseACL containerACL = getACL();
            if (containerACL != null) {
                // Test the access rights
                result = containerACL.getPermission(user, permission);
            }
        } catch (JahiaException ex) {
            logger.debug("Problem getting ACL on container.", ex);
        }

        return result;
    }

    /**
     * Try to get the content object from the Jahia container
     *
     * @return The content container if success, otherwise null.
     */
    public ContentContainer getContentContainer() {
        ContentContainer contentContainer = null;
        try {
             contentContainer = ContentContainer.getContainer(ID);
        } catch (JahiaException je) {
            logger.debug(je);
        }
        return contentContainer;
    }

    public void setProperties(Properties newProperties) {
        ctnProperties = newProperties;
    }

    public Properties getProperties() {
        if (ctnProperties == null) {
            try {
                Properties tempProperties = new Properties();
                for (Map.Entry<String, String> entry : ServicesRegistry.getInstance()
                        .getJahiaContainersService().getContainerProperties(
                                getID()).entrySet()) {
                    if (entry.getValue() != null)
                        tempProperties.put(entry.getKey(), entry.getValue());
                }
                ctnProperties = tempProperties;
            } catch (JahiaException e) {
                throw new RuntimeException(e);
            }
        }
        return ctnProperties;
    }

    public String getProperty(String propertyName) {
        return (getProperties() != null ? getProperties().getProperty(propertyName) : null);
        }

    public void setProperty(String propertyName, String propertyValue) {
        if (getProperties() == null) {
            ctnProperties = new Properties();
        }    
        getProperties().setProperty(propertyName, propertyValue);
    }

    private void checkProxy(int loadFlag){
        if ( this.cFactoryProxy != null ){
            this.cFactoryProxy.load(this,loadFlag);
        }
    }

    // sort the fields, based on their order attribute
    public void sortFieldByOrderAttribute(){
        List<JahiaField> list = new ArrayList<JahiaField>(fields);
        Collections.sort(list);
        fields = new ArrayList<JahiaField>(list);
    }
        
    public void fieldsStructureCheck(ProcessingContext jParams) throws JahiaException{
        fieldsStructureCheck(jParams,true);
    }
} // end JahiaContainer
