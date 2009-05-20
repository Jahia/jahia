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
//
// 08.05.2001 NK implements Comparator interface to be sortable
// 22.01.2001 NK added page def ACL
//

package org.jahia.services.pages;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentObject;
import org.jahia.content.PageDefinitionKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.ACLNotFoundException;
import org.jahia.services.acl.ACLResourceInterface;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;


/**
 * This class is used to keep the page definitions information stored
 * in the database. Each time a page definition is loaded it's information
 * are stored into the cache using this class. <br/>
 * <br/>
 * This class knows how to save it's content, but it's not an automatic process
 * to avoid one database access for each little change made to the page data.
 * Therefore a call to the {@link #commitChanges() commitChanges()} method is
 * needed to save the update information to the database.
 *
 * @author Eric Vassalli
 * @author Fulco Houkes
 * @author Khue Nguyen
 * @version 2.0
 */
public class JahiaPageDefinition extends ContentDefinition implements Comparator<JahiaPageDefinition>,
        ACLResourceInterface, Serializable {

    private static final long serialVersionUID = -8064307695096958287L;

    public static final String DEF_SOURCE_PATH = "<no source path>";
    
    private static Logger logger = Logger.getLogger(JahiaPageDefinition.class);

    static {
        ContentDefinition.registerType (PageDefinitionKey.PAGE_TYPE,
                JahiaPageDefinition.class.getName ());
    }

    // Properties names
    public static final String ACLID_PROP = "acl_id";
    public static final String DESCRIPTION_PROP = "description";

    private int mID;
    private int mSiteID;
    private String mName;
    private String mSourcePath;         // JSP source path
    private boolean mAvailable;            // Available or not for a user in the administration manager
    private String mImage;
    private String pageType;

    private int mTempSiteID;
    private String mTempName;
    private boolean mTempAvailable;
    private String mTempImage;
    private String mTempPageType;

    private Map<String, String> mProps = new HashMap<String, String> ();

    private boolean mDataChanged;
    
    private String displayName;
    
    private String description;

    //-------------------------------------------------------------------------
    /**
     * Default constructor.
     *
     * @param      ID          Unique identification number of the page definition.
     * @param      siteID     Jahia site unique identification number
     * @param   name        Name of the page definition.
     * @param   sourcePath  Source path of the page definition
     * @param  isAvailable Not available to users if false
     * @param   image		A thumbnail
     * @param pageType
     */
    public JahiaPageDefinition(int ID, int siteID, String name,
                               String sourcePath, boolean isAvailable,
                               String image, String pageType) {
        super (new PageDefinitionKey (ID));
        mID = ID;
        mSiteID = siteID;

        if (name == null) {
            name = "Untitled-" + ID;
        }
        mName = name;

        if (sourcePath == null) {
            sourcePath = DEF_SOURCE_PATH;
        }
        mSourcePath = sourcePath;

        mAvailable = isAvailable;

        if (image == null) {
            image = "";
        }
        mImage = image;

        this.pageType = pageType;

        // temp variables.
        mTempSiteID = siteID;
        mTempName = name;
        mTempAvailable = isAvailable;
        mTempImage = image;
        mTempPageType = pageType;
        mDataChanged = false;
    }

    /**
     * @param IDInType
     *
     * @return
     */
    public static ContentDefinition getChildInstance (String IDInType) {

        try {
            return ServicesRegistry.getInstance ().getJahiaPageTemplateService ()
                    .lookupPageTemplate (Integer.parseInt (IDInType));
        } catch (JahiaException je) {
            logger.debug ("Error retrieving ContentDefinition instance for id : " + IDInType,
                    je);
        }
        return null;
    }

    //-------------------------------------------------------------------------
    /**
     * Commit all the previous changes to the database.
     *
     * @throws JahiaException Throws a JahiaException if the data could not
     *                        be saved successfully into the database.
     */
    public void commitChanges (JahiaPageTemplateService service)
            throws JahiaException {
        // commit only if the data has really changed
        if (mDataChanged) {

            mSiteID = mTempSiteID;
            mName = mTempName;
            mAvailable = mTempAvailable;
            mImage = mTempImage;
            pageType = mTempPageType;
            service.updatePageTemplate (this);
            mDataChanged = false;
        }
    }

    public void commitChanges ()
            throws JahiaException {
        // commit only if the data has really changed
        if (mDataChanged) {

            mSiteID = mTempSiteID;
            mName = mTempName;
            mAvailable = mTempAvailable;
            mImage = mTempImage;
            pageType = mTempPageType;
            ServicesRegistry.getInstance().getJahiaPageTemplateService().updatePageTemplate (this);
            mDataChanged = false;
        }
    }


    public void disableChanges () {
        if (mDataChanged) {
            mTempSiteID = mSiteID;
            mTempName = mName;
            mTempAvailable = mAvailable;
            mTempImage = mImage;
            mTempPageType = pageType;
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Return the page definition unique identification number.
     *
     * @return The identification number.
     */
    public final int getID () {
        return mID;
    }

    public final void setID(int mID) {
        this.mID = mID;
        this.setObjectKey(new PageDefinitionKey(this.mID));
    }
    //-------------------------------------------------------------------------
    /**
     * Get the image path
     *
     * @return String path to the image.
     */
    public String getImage () {
        return mImage;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the site unique identification number.
     *
     * @return The site identification number.
     */
    public final int getJahiaID () {
        return mSiteID;
    }


    //--------------------------------------------------------------------------
    /**
     * Return the page definition name.
     *
     * @return The page definition name.
     */
    public final String getName () {
        return mName;
    }

    /**
     * @param contentObject
     * @param entryState
     *
     * @return
     */
    public String getTitle (ContentObject contentObject,
                            ContentObjectEntryState entryState) {
        try {
            return ((ContentPage) contentObject)
                    .getTitle (new EntryLoadRequest (entryState));
        } catch (Exception t) {
            logger.debug ("Exception retrieving Definition Title", t);
        }
        return null;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the JSP file associated with the page definition.
     *
     * @return The source path.
     */
    public final String getSourcePath () {
        return mSourcePath;
    }


    //-------------------------------------------------------------------------
    /**
     * Get the Available status
     *
     * @return Return true if the page definition is available or false if
     *         it's not.
     */
    public final boolean isAvailable () {
        return mAvailable;
    }

    //-------------------------------------------------------------------------
    /**
     * Set the image's path
     *
     * @param value The new image path.
     */
    public void setImage (String value) {
        mTempImage = value;
        mDataChanged = true;
    }


    //-------------------------------------------------------------------------
    /**
     * Set the Jahia ID.
     *
     * @param value The new Jahia ID.
     */
    public void setJahiaID (int value) {
        mTempSiteID = value;
        mDataChanged = true;
    }


    //-------------------------------------------------------------------------
    /**
     * Set the page definition name.
     *
     * @param value The new page definition name.
     */
    public void setName (String value) {
        mTempName = value;
        mDataChanged = true;
    }

    //-------------------------------------------------------------------------
    /**
     * Set the source path.
     *
     * @param value The new source path.
     */
    public void setSourcePath (String value) {
        mSourcePath = value;
    }

    //-------------------------------------------------------------------------
    /**
     * Set the available status.
     *
     * @param value Set the new available status. True the Definition page
     *              will be available, false it won't.
     */
    public void setAvailable (boolean value) {
        mTempAvailable = value;
        mDataChanged = true;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the description of the template.
     *
     * @return Return the description.
     */
    public final String getDescription () {
        return description;
    }    
    
    /**
     * Set the description.
     *
     * @param id the description
     */
    public void setDescription (String description) {
        this.description = description;
    }
    
    /**
     * Return the ACL object.
     *
     * @return Return the ACL.
     */
    public final JahiaBaseACL getACL () {

        if (mProps == null)
            return null;

        String value = (String) mProps.get(ACLID_PROP);
        if (value == null)
            return null;

        JahiaBaseACL acl = null;
        try {
            acl = new JahiaBaseACL (Integer.parseInt (value));
        } catch (ACLNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return acl;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the ACL id.
     *
     * @return int the ACL id.
     */
    public final int getAclID () {

        if (mProps == null)
            return -1;

        String value = (String) mProps.get(ACLID_PROP);
        if (value == null)
            return -1;

        JahiaBaseACL acl = null;
        try {
            acl = new JahiaBaseACL (Integer.parseInt (value));
            return acl.getID ();
        } catch (ACLNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return -1;
    }

    //-------------------------------------------------------------------------
    /**
     * Set the acl id.
     *
     * @param id the acl id
     */
    protected final void setACL (int id) {
        if (mProps == null) {
            logger.debug ("ACL cannot be set because properties is null");
            return;
        }
        mProps.put(ACLID_PROP, String.valueOf (id));
        mDataChanged = true;
    }
    
    //-------------------------------------------------------------------------
    /**
     * Compare between two objects, sort by their name
     *
     * @param c1
     * @param c2
     *
     */
    public int compare (JahiaPageDefinition c1, JahiaPageDefinition c2) throws ClassCastException {

        return (c1.getName().toLowerCase ()
                .compareTo (c2.getName ().toLowerCase ()));

    }




    //--------------------------------------------------------------------------
    /**
     * Set the properties
     *
     */
    public void setProperties (Map<String, String> props) {
        mProps = props;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the properties
     *
     * @return Properties the properties
     */
    public Map<String, String> getProperties () {
        return mProps;
    }

    //--------------------------------------------------------------------------
    /**
     * Return a property as String
     *
     * @param name , the property name, null if not found
     */
    public String getProperty (String name) {
        if (mProps != null) {
            return (String) mProps.get(name);
        } else {
            return null;
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Set a property
     *
     * @param name , the property name
     * @param value , the property value as String
     */
    public void setProperty (String name, String value) {
        if (mProps != null) {
            mProps.put(name, value);
        }
    }

    /**
     * Returns the template display name.
     * 
     * @return the template display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the display name.
     * 
     * @param displayName
     *            the value of the display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPageType() {
        return pageType;
    }

    public void setPageType(String pageType) {
        this.mTempPageType = pageType;
        this.mDataChanged = true;
    }

    public String getPrimaryType() {
        return getPageType();
    }
}