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

 package org.jahia.data.fields;


public class JahiaFieldDefinitionProperties {

    /**
     * Optional JSP output to be used by update fields ( and containers ) engines
     * in place of default ones
     */
    public static final String FIELD_UPDATE_JSP_FILE_PROP = "field_update_jsp_file_prop";
    
    /**
     * This property can be used for SmallText and SharedSmallText fields. Set the value 
     * to true, if you want to display a multi-line input field instead of a single-line.
     */
    public static final String FIELD_MULTILINE_SMALLTEXT_PROP = "field_multiline_smalltext_prop";    

    /**
     * This property can be used fnor BigText fields. You can set the ID of a stylesheet and 
     * a style definition description configured in htmleditors_config.xml 
     */
    public static final String FIELD_STYLESHEET_ID_PROP = "field_stylesheet_id_prop";
    
    /**
     * Optional page selection filter for page fields with a filtered page view when 
     * linking or moving pages.
     */
    public static final String PAGE_SELECTION_FILTER_PROP = "page_selection_filter_prop";        


    public static final String COLOR_PICKER_PROP = "color_picker";
}
