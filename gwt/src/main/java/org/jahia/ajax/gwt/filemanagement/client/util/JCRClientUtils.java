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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.filemanagement.client.util;

import java.util.Map;
import java.util.HashMap;

/**
 * User: rfelden
 * Date: 12 déc. 2008 - 11:03:23
 */
public final class JCRClientUtils {

    public static final String FOLDER_NODETYPES = "nt:folder,jnt:mountPoint";
    public static final String FILE_NODETYPES = "nt:file,nt:folder,jnt:mountPoint,jnt:portlet";
    public static final String PORTLET_NODETYPES = "nt:folder,jnt:mountPoint,jnt:portlet";
    public static final String GARBAGE_NODETYPES = "jnt:jahiacontent,jnt:systemRoot,jnt:jahiaVirtualsite" ;

    public final static String MY_REPOSITORY = "myRepository" ;
    public final static String MY_EXTERNAL_REPOSITORY = "myExternalRepository" ;
    public final static String SHARED_REPOSITORY = "sharedRepository" ;
    public final static String WEBSITE_REPOSITORY = "websiteRepository" ;

    public final static String ALL_FILES = "files" ;

    public final static String MY_MASHUP_REPOSITORY = "myMashupRepository" ;
    public final static String SHARED_MASHUP_REPOSITORY = "sharedMashupRepository" ;
    public final static String WEBSITE_MASHUP_REPOSITORY = "websiteMashupRepository" ;

    public final static String ALL_MASHUPS = "mashups" ;

    public final static String GLOBAL_REPOSITORY = "globalRepository" ;

    public final static Map<String, String> REPO_LABELS = new HashMap<String, String>() ;

    static {
        REPO_LABELS.put(WEBSITE_REPOSITORY, "Current site repository") ;
        REPO_LABELS.put(SHARED_REPOSITORY, "Shared repository") ;
        REPO_LABELS.put(MY_EXTERNAL_REPOSITORY, "My external repository") ;
        REPO_LABELS.put(MY_REPOSITORY, "My repository") ;
        REPO_LABELS.put(MY_MASHUP_REPOSITORY,  "My mashups repository" );
        REPO_LABELS.put(SHARED_MASHUP_REPOSITORY, "Shared mashups repository") ;
        REPO_LABELS.put(WEBSITE_MASHUP_REPOSITORY, "Current site mashups repository") ;
        REPO_LABELS.put(GLOBAL_REPOSITORY, "Global repository") ;
    }

    public static String cleanUpFilename(String name) {
        name = name.replaceAll("([\\*:/\\\\<>|?\"])"," ");
        name = name.trim();
        return name;
    }

}
