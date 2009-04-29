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
// 28.01.2002 NK added page def ACL
//

package org.jahia.services.pages;


/**
 * Can be used to handle every page definition parameters without changing/creating
 * a real object in storage until the user really want to save it.
 *
 * @author Khue Nguyen
 */
public class JahiaPageDefinitionTemp {

    private int mID;
    private int mSiteID;
    private String mName;
    private String mSourcePath;         // JSP source path
    private boolean mAvailable;          // Available or not for a user.
    private String mImage;
    private boolean mIsDefault = false;

    //-------------------------------------------------------------------------
    /**
     * Default constructor.
     *
     * @param int     ID          Unique identification number of the page definition ( dummy ).
     * @param int     jahiaID     Jahia site unique identification number
     * @param String  name        Name of the page definition.
     * @param String  sourcePath  Source path of the page definition
     * @param boolean isAvailable Not available to users if false
     * @param String  image		A thumbnail
     * @param boolean isDefault	true if this template is the default template
     */
    public JahiaPageDefinitionTemp (int ID, int siteID, String name,
                                    String sourcePath, boolean isAvailable,
                                    String image, boolean isDefault) {
        mID = ID;
        mSiteID = siteID;

        if (name == null) {
            name = "";
        }
        mName = name;

        if (sourcePath == null) {
            sourcePath = "";
        }
        mSourcePath = sourcePath;

        mAvailable = isAvailable;

        if (image == null) {
            image = "";
        }
        mImage = image;

        mIsDefault = isDefault;

    }

    //-------------------------------------------------------------------------
    /**
     * Build from an existing page definition
     *
     * @param JahiaPageDefinition template
     * @param boolean             isDefault , true if the template the site's default template.
     */
    public JahiaPageDefinitionTemp (JahiaPageDefinition template, boolean isDefault) {
        if (template == null)
            return;

        mID = template.getID ();
        mSiteID = template.getJahiaID ();
        mName = template.getName ();
        mSourcePath = template.getSourcePath ();
        mAvailable = template.isAvailable ();
        mImage = template.getImage ();
        mIsDefault = isDefault;
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


    //-------------------------------------------------------------------------
    /**
     * Return the page definition name.
     *
     * @return The page definition name.
     */
    public final String getName () {
        return mName;
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
     * Get the Default status
     *
     * @return Return true if the page definition is the site's default template
     *         or false if it's not.
     */
    public final boolean isDefault () {
        return mIsDefault;
    }

    //-------------------------------------------------------------------------
    /**
     * Set the image's path
     *
     * @param value The new image path.
     */
    public void setImage (String value) {
        mImage = value;
    }


    //-------------------------------------------------------------------------
    /**
     * Set the Jahia ID.
     *
     * @param value The new Jahia ID.
     */
    public void setJahiaID (int value) {
        mSiteID = value;
    }


    //-------------------------------------------------------------------------
    /**
     * Set the page definition name.
     *
     * @param value The new page definition name.
     */
    public void setName (String value) {
        mName = value;
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
        mAvailable = value;
    }

    //-------------------------------------------------------------------------
    /**
     * Set the default status.
     *
     * @param value Set the new default status. True the Definition page
     *              will be the default template of the site.
     */
    public void setDefault (boolean value) {
        mIsDefault = value;
    }

} // end JahiaPageDefinitionTemp
