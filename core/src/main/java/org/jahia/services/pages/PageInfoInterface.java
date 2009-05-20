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
