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
 * This class sets all the kind of pages used by Jahia, and also defines some
 * page masks for the page searching algorithma.
 */
public class PageLoadFlags {
    /** Not a page! */
    public static final int NOTHING = 0;

    /**
     * Defines direct Jahia pages. This kind of pages are not pointing on
     * another page.
     */
    public static final int DIRECT = 1;

    /** Defines internal Jahia pages links */
    public static final int INTERNAL = 2;

    /** Defines external, non-Jahia pages, mostly URL from the web */
    public static final int URL = 4;

    /** defines all the pages */
    public static final int ALL = -1;

    /** Defines all the Jahia pages (direct or internal pages) */
    public static final int JAHIA = DIRECT | INTERNAL;

    /** Defines all the link-type pages (internal or URL pages) */
    public static final int LINKS = INTERNAL | URL;
}
