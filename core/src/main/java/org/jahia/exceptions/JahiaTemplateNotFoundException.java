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
// $Id$
//


package org.jahia.exceptions;


/**
 * This exception is raised when a page template object is requested and is not
 * present in the database.
 *
 * @author  Fulco Houkes
 * @version 1.1
 *
 * @see     JahiaException
 */
public class JahiaTemplateNotFoundException extends JahiaException
{
    private int    mPageTemplateID = -1;
    private String mPageTemplateName = null;

    //-------------------------------------------------------------------------
    /** Default constructor
     *
     * @param   templateID
     *      The page tempate ID.
     */
    public JahiaTemplateNotFoundException (int templateID)
    {
        super ("The page template could not be found",
               "The page template ["+templateID+"] could not be found",
               LOCK_ERROR, ERROR_SEVERITY);

        mPageTemplateID = templateID;
    }

    //-------------------------------------------------------------------------
    /** Default constructor
     *
     * @param   templateName
     *      The page tempalte name.
     */
    public JahiaTemplateNotFoundException (String templateName)
    {
        super ("The page template could not be found",
               "The page template ["+templateName+"] could not be found",
               LOCK_ERROR, ERROR_SEVERITY);

        mPageTemplateName = templateName;
    }


    //-------------------------------------------------------------------------
    /** Return the page template ID
     *
     * @return
     *      Return the page template ID. Return -1 if the template was not
     *      accessed by it's ID. In this case, the method
     *      {@link #getPageTemplateName() getPageTemplateName()} will
     *      return the page template name.
     */
    public final int getPageTemplateID () {
        return mPageTemplateID;
    }

    //-------------------------------------------------------------------------
    /** Return the page template name.
     *
     * @return
     *      Return the page template name. Return null if the template was not
     *      accessed by it's name, in this case the method
     *      {@link #getPageTemplateID() getPageTemplateID()} will return the
     *      page template ID.
     */
    public final String getPageTemplateName () {
        return mPageTemplateName;
    }
}
