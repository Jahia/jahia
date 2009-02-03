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

package org.jahia.services.pages;


/**
 * This interface holds constants related to the pages.
 *
 * @author Fulco Houkes
 * @version 1.0
 */
public interface PageInfoInterface {

    //---------------------------------
    // The following constants define the different kind of pages present
    // inside of Jahia. A TYPE_DIRECT page is a real page holding renderable
    // content, while the TYPE_LINK and TYPE_URL are references on other pages.
    // Respectively the TYPE_LINK and TYPE_URL type are links to an internal
    // jahia page and a remote URL, which is usually a non jahia page.


    /** Real page type */
    public static final int TYPE_DIRECT = 0;

    /** Link type page */
    public static final int TYPE_LINK = 1;

    /** Remote URL page type */
    public static final int TYPE_URL = 2;

    /** Array holding the constant type names */
    public static final String[] PAGE_TYPE_NAMES = {"TYPE_DIRECT", "TYPE_LINK", "TYPE_URL"};



    //---------------------------------
    // page default link values.


    /** Default remote URL value */
    public static final String NO_REMOTE_URL = "http://";

    /**
     * Page tree hierarchy termination value. <b>No real page should have this
     * page ID!</b>
     */
    public static final int TERMINATION_PAGE_ID = -1;

}
