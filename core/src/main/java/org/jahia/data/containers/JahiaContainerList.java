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
//  JahiaContainerList
//  EV      27.12.2000
//

package org.jahia.data.containers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jahia.exceptions.JahiaException;
import org.jahia.registries.JahiaContainerDefinitionsRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.ACLNotFoundException;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.containers.ContainerListFactoryProxy;
import org.jahia.services.containers.ContainerQueryBean;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.usermanager.JahiaUser;

/**
 * <p>Title: A JahiaContainerList is a list of JahiaContainer objects</p>
 * <p>Description: The JahiaContainerList object managed a list of
 * JahiaContainer object, that themselves contain JahiaField objects.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Eric Vassalli
 * @version 1.0
 */

public class JahiaContainerList implements Cloneable, Serializable {

    private static final long serialVersionUID = 7805330035791855293L;

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaContainerList.class);

    private int     ID;
    private int     parentEntryID;
    private int     pageID;
    private int     ctndefid;
    private int     aclID;
    private int     fullDataSize = 0;

    private int     maxSize = Integer.MAX_VALUE;

    private boolean isContainersLoaded = false; // by default the container list is loaded without its containers.

    private Properties  ctnListProperties = null;

    private JahiaContainerListPagination ctnListPagination;

    private ContainerQueryBean queryBean;

    /**
     * @associates JahiaContainer
     */
    private List<JahiaContainer>  containers = new ArrayList<JahiaContainer>();

    private ContainerListFactoryProxy cListFactoryProxy;

     //-------------------------------------------------------------------------
    public JahiaContainerList(  int     ID,
                                int     parentEntryID,
                                int     pageID,
                                int     ctndefid,
                                int     aclID )
    {
        this.ID             = ID;
        this.parentEntryID  = parentEntryID;
        this.pageID         = pageID;
        this.ctndefid = ctndefid;
        this.aclID          = aclID;
    } // end constructor

    /**
     * Set the containerListFactory used to control how this containerlist's
     * containers will be loaded.
     *
     * @param containerFactory
     */
    public void setFactoryProxy(ContainerListFactoryProxy cListFactoryProxy){
        this.cListFactoryProxy = cListFactoryProxy;
    }
    
    public ContainerListFactoryProxy getFactoryProxy() {
        return cListFactoryProxy;
    }

    public JahiaContainerList() {
    }

    //-------------------------------------------------------------------------
    public  int     getID()             {   return ID;                      }
    public  int     getParentEntryID()  {   return parentEntryID;           }
    public  int     getPageID()         {   return pageID;                  }
    public  int     getctndefid() 		{   return ctndefid;          		}

    public  int getAclID()		{
        ContentContainerList list = getContentContainerList();
        if (list != null) {
            return list.getAclID();
        }
        return aclID;
    }

    /**
     * Return true if this container list is loaded with its containers
     *
     * @author NK
     */
    public  boolean isContainersLoaded()
    {
        return isContainersLoaded;
    }

    public  JahiaContainerListPagination getCtnListPagination() {
        return getCtnListPagination(true);
    }

    public  JahiaContainerListPagination getCtnListPagination(boolean loadContent) {
        if ( loadContent ){
            checkProxy();
        }
        return ctnListPagination;
    }

    public final JahiaBaseACL getACL()		{
        JahiaBaseACL acl = null;
        try {
            acl = JahiaBaseACL.getACL(getAclID());
        } catch ( Exception e ) {
            logger.error(e.getMessage(), e);
        }
        return acl;
    }

    public  void    setID( int ID )     {   this.ID = ID;                   }
    public  void    setAclID( int aclID ) {   this.aclID = aclID;           }
    public  void    setParentEntryID(int parentEntryID)  { this.parentEntryID = parentEntryID; }

    public  void    setCtnListPagination( int windowSize, int windowOffset )
    {
        this.ctnListPagination = new JahiaContainerListPagination(this.getFullSize(),windowSize,windowOffset);
    }

    public  void    setCtnListPagination( JahiaContainerListPagination cListPagination )
    {
        this.ctnListPagination = cListPagination;
    }

    /**
     * By default the container list is loaded without its containers.
     * Set this state to true to indeicate you loaded its containers.
     *
     * @author NK
     */
    public void setIsContainersLoaded( boolean value )
    {
        this.isContainersLoaded = value;
    }


    // end accessor methods



    //-------------------------------------------------------------------------
    /**
     * returns the  index nth container in the container list
     */
    public JahiaContainer getContainer( int index ) throws JahiaException
    {
        checkProxy();
        if (index < containers.size()) {
            return (JahiaContainer) containers.get(index);
        } else {
            String errorMsg = "Error in JahiaContainerList : trying to get entry " + index + " for container " + getDefinition().getName();
            logger.error(errorMsg + " -> BAILING OUT");
            throw new JahiaException(   "Error in database synchronisation",
                                        errorMsg, JahiaException.DATABASE_ERROR, JahiaException.CRITICAL_SEVERITY );
        }
    } // end getContainer



    /**
     * @return an Iterator of the containers in the container list.
     */
    public Iterator<JahiaContainer> getContainers()
    {
        checkProxy();
        List<JahiaContainer> tempList = new ArrayList<JahiaContainer>(containers);
        return tempList.iterator();
    } // end getContainers


    /**
     * @return the List of containers.
     */
    public List<JahiaContainer> getContainersList()
    {
        checkProxy();
        return this.containers;
    }

    /**
     * Returns the size of the container list (ie the number of elements in
     * the list). This now returns only the size that has been loaded. For
     * real full size of data set, see the full size call below.
     * @returns an integer specifying the number of elements in the container
     * list in memory for the current view.
     */
    public int size() {
        checkProxy();
        if ( containers != null )
        {
            return containers.size();
        }
        return 0;
    }

    /**
     * Sets the full size of the container list stored in the database, not
     * just in memory.
     * @param fullDataSize an integer containing a value of the full data size.
     */
    public void setFullSize(int fullDataSize) {
        if (fullDataSize >= 0) {
            this.fullDataSize = fullDataSize;
        }
    }

    /**
     * Returns the full size of the data set in the datasource. This function
     * has been added because of the introduction of scrollable container lists
     * which load only the set for the view.
     * @return the number of elements of this containerList in the database
     */
    public int getFullSize() {
        checkProxy();
        return this.fullDataSize;
    }

    /***
        * getDefinition
        *
        */
    public JahiaContainerDefinition getDefinition()
    throws JahiaException
    {
        JahiaContainerDefinition theDef = JahiaContainerDefinitionsRegistry.getInstance().getDefinition( ctndefid );
        if (theDef != null) {
            return theDef;
        } else {
            String msg = "JahiaContainer definition " + ctndefid + " not found in definition registry !";
            throw new JahiaException( "Synchronisation error in database",
                                        msg, JahiaException.DATABASE_ERROR, JahiaException.CRITICAL_SEVERITY );
        }
    } // end getDefinition

    public ContainerQueryBean getQueryBean() {
        return queryBean;
    }

    public void setQueryBean(ContainerQueryBean queryBean) {
        this.queryBean = queryBean;
        this.isContainersLoaded = false;
    }

    /***
        * adds a container to the containerList
        * @author EV    27.12.2000
        *
        */
    public void addContainer( JahiaContainer theContainer )
    {
        checkProxy();
        /*
        int insertPos = containers.size();
        for (int i=0; i < containers.size(); i++) {
            JahiaContainer aContainer = (JahiaContainer)containers.elementAt(i);
            if (theContainer.getRank() < aContainer.getRank()) {
                insertPos = i;
                break;
            }
            aContainer = null;
        }
        containers.insertElementAt( theContainer, insertPos );
        */
        theContainer.setContextualContainerListID(this.getID());
        containers.add(theContainer);

    } // end addContainer


    //-------------------------------------------------------------------------
    /**
     * Returns true if the acl set at container list level allow a field of a given
     * field def name to be editable or not
     *
     * @param String fieldDefName
     * @param JahiaUser the user
     * @return boolean true if the acl return true , false else
     * @author Khue Nguyen
     */
    public final boolean isFieldEditable(String fieldDefName, JahiaUser user){
        boolean result = false;
        if ( fieldDefName == null || user == null )
            return false;

        String val = this.getProperty("view_field_acl_"+fieldDefName);
        if ( val != null ){
            try {
                int aclID = Integer.parseInt(val);
                JahiaBaseACL theACL = null;
                try {
                    theACL = new JahiaBaseACL (aclID);
                }
                catch (ACLNotFoundException ex) {
                }
                catch (JahiaException ex) {
                }
                return theACL.getPermission(user,JahiaBaseACL.WRITE_RIGHTS);
            } catch ( Exception t ){
            }
        }
        return result;
    }

    //-------------------------------------------------------------------------
    /**
     * Returns true if the acl set at container list level allow a field of a given
     * field def name to be visible ( READ permission )
     *
     * @param String fieldDefName
     * @param JahiaUser the user
     * @return boolean true if the acl return true , false else
     * @author Khue Nguyen
     */
    public final boolean isFieldReadable(String fieldDefName, JahiaUser user){
        if ( fieldDefName == null || user == null )
            return false;

        String val = this.getProperty("view_field_acl_"+fieldDefName);
        if ( val != null ){
            try {
                int aclID = Integer.parseInt(val);
                JahiaBaseACL theACL = null;
                theACL = new JahiaBaseACL (aclID);
                return theACL.getPermission(user,JahiaBaseACL.READ_RIGHTS);
            } catch ( Exception t ){
                // One acl defined but there's an error assume false for security reason
                logger.error("error dureing guessing of readable field ",t);
                return false;
            }
        }
        // NO Acl defined so assume true
        return true;
    }


    //-------------------------------------------------------------------------
    /**
     * Check if the user has read access for the specified containerList. Read access means
     * the user can display containerList data.
     *
     * @param    user    Reference to the user.
     *
     * @return   Return true if the user has read access for the specified containerList,
     *           or false in any other case.
     */
    public final boolean checkReadAccess (JahiaUser user)
    {
        return checkAccess (user, JahiaBaseACL.READ_RIGHTS);
    }

    //-------------------------------------------------------------------------
    /**
     * Check if the user has Write access for the specified container. Write access means
     * the user can add/delete/update containers in the containerList.
     *
     * @param    user    Reference to the user.
     *
     * @return   Return true if the user has write access for the specified containerList,
     *           or false in any other case.
     */
    public final boolean checkWriteAccess (JahiaUser user)
    {
        return checkAccess (user, JahiaBaseACL.WRITE_RIGHTS);
    }


    //-------------------------------------------------------------------------
    /**
     * Check if the user has Admin access for the specified container. Admin access means
     * the user can set rights on the containerList.
     *
     * @param    user    Reference to the user.
     *
     * @return   Return true if the user has admin access for the specified containerList,
     *           or false in any other case.
     */
    public final boolean checkAdminAccess (JahiaUser user)
    {
        return checkAccess (user, JahiaBaseACL.ADMIN_RIGHTS);
    }


    //-------------------------------------------------------------------------
    private boolean checkAccess (JahiaUser user, int permission)
    {
        if (user == null) {
            return false;
        }

        // NK :Fake container list with acl id = -1 are not yet created in storage
        //     and their ACL are therefore fake too, so we return true
        if ( aclID == 0 )
            return true;

        boolean result = false;
        try {
            // Try to instanciate the ACL.
            JahiaBaseACL containerListACL = getACL();
            if (containerListACL != null) {
                // Test the access rights
                result = containerListACL.getPermission (user, permission);
            }
        } catch (JahiaException ex) {
            logger.debug("Problem getting ACL on container list.", ex);
        }

        return result;
    }


    public void setProperties(Properties newProperties) {
        this.ctnListProperties = newProperties;
    }

    public Properties getProperties() {
        if (ctnListProperties == null) {
            try {
                Properties tempProperties = new Properties();
                for (Map.Entry<Object, Object> entry : ServicesRegistry.getInstance()
                        .getJahiaContainersService()
                        .getContainerListProperties(getID()).entrySet()) {
                    if (entry.getValue() != null)
                        tempProperties.put(entry.getKey(), entry.getValue());
                }
                ctnListProperties = tempProperties;
            } catch (JahiaException e) {
                throw new RuntimeException(e);
            }
        }
        return ctnListProperties;        
    }

    public String getProperty(String propertyName) {
        return (getProperties() != null ? getProperties().getProperty(propertyName) : null);
    }

    public void setProperty(String propertyName, String propertyValue) {
        if (getProperties() == null) {
             ctnListProperties = new Properties();
        }
        getProperties().setProperty(propertyName, propertyValue);
    }

    /**
     * Merge a set of properties passed in parameters with the internal
     * properties set. Returns true if the resulting set of internal properties
     * must be serialized.

     * @param newProperties
     * @return true if the result of the merge is a different set of properties
     * that needs to be serialized.
     */
    public boolean mergeProperties(Properties newProperties) {
        boolean mustSave = false;
        Iterator<?> newPropKeys = newProperties.keySet().iterator();
        while (newPropKeys.hasNext()) {
            String curNewPropName = (String) newPropKeys.next();
            String curNewPropValue = newProperties.getProperty(curNewPropName);
            if (getProperties().containsKey(curNewPropName)) {
                String internalPropValue = getProperties().getProperty(curNewPropName);
                if (!internalPropValue.equals(curNewPropValue)) {
                    // properties are not equals, lets set it.
                    getProperties().setProperty(curNewPropName, curNewPropValue);
                    mustSave = true;
                }
            } else {
                // this is a new property.
                getProperties().setProperty(curNewPropName, curNewPropValue);
                mustSave = true;
            }
        }
        return mustSave;
    }

    /**
     * Try to get the content object from the Jahia container list
     *
     * @return The content container list if success, otherwise null.
     */
    public ContentContainerList getContentContainerList() {
        ContentContainerList contentContainerList = null;
        try {
            if (ID != 0) {
                contentContainerList = ContentContainerList.getContainerList(ID);
            } else {
                // this case can happen if we are handling a "fake"
                // container list created to generate the add container URL,
                // etc...
                return null;
            }
        } catch (JahiaException je) {
            logger.error(
                "Error while trying to retrieve JahiaContainerList from JahiaContainerList",
                je);
        }
        return contentContainerList;
    }

    /**
     * Returns the max size allowed
     *
     * @return
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Set the max size allowed
     * 
     * @param maxSize
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Return jcr path "/siteKey/pageKey"
     *
     * @param context
     * @return
     * @throws JahiaException
     */

//    public String getJCRPath(ProcessingContext context) throws JahiaException {
//        if (this.getID()==-1){
//            return "";
//        }
//        try {
//            String path = ContentContainerListKey.getInstance(ContentContainerListKey.CONTAINERLIST_TYPE +
//                ContentContainerListKey.KEY_SEPARATOR + this.getID()).toString();
//            ContentPage contentPage = ContentPage.getPage(this.getPageID());
//            String parentPath = contentPage.getJCRPath(context);
//            return parentPath + "/" + ServicesRegistry.getInstance().getQueryService()
//                    .getNameFactory().create(Name.NS_DEFAULT_URI,path);
//        } catch ( Exception e ){
//            throw new JahiaException("Error creating JCRPath for container " + this.getID(),
//                "Error creating JCRPath for container " + this.getID(),JahiaException.APPLICATION_ERROR,
//                    JahiaException.ERROR_SEVERITY, e);
//        }
//    }

    //-------------------------------------------------------------------------
    /**
     * Clone
     */
    public Object clone () {
        JahiaContainerList containerList =
            new JahiaContainerList(this.ID,this.parentEntryID,this.pageID,
                                   this.ctndefid,this.aclID);
        if ( this.ctnListProperties != null ){
            containerList.setProperties( (Properties)this.ctnListProperties.
                                        clone());
        }
        return containerList;
    }

    private void checkProxy(){
        if ( this.cListFactoryProxy != null ){
            this.cListFactoryProxy.load(this);
        }
    }

    public void clearContainers(){
        this.containers = new ArrayList<JahiaContainer>();
    }
}
