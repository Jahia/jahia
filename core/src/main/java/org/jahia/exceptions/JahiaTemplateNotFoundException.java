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
