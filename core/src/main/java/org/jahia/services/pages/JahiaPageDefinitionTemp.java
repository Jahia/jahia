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
