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
