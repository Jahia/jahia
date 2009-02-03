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

//
//  FieldTypes
//  EV      18.11.2000
//
//

package org.jahia.data.fields;

import java.util.HashMap;
import java.util.Map;

public class FieldTypes {

    private static final FieldTypes instance = new FieldTypes();

    public static final int INTEGER = 1;
    public static final int SMALLTEXT = 2;
    public static final int BIGTEXT = 3;
    public static final int DATE = 4;
    public static final int PAGE = 5;
    public static final int FILE = 6;
    // public  static  final   int     IMAGE           = 7;
    // public  static  final   int     AUDIOCLIP       = 8;
    // public  static  final   int     VIDEOCLIP       = 9;
    public static final int APPLICATION = 10;
    public static final int FLOAT = 11;
    public static final int BOOLEAN = 12;
    public static final int COLOR = 13;

    public static final int CATEGORY = 14;
    public static final int SMALLTEXT_SHARED_LANG = 20;


    public static final String typeName[] = {
            "Undefined",
            "Integer",
            "Small text",
            "Big text",
            "Date",
            "Page",
            "File",
            "Image",
            "Audio clip",
            "Video clip",
            "Application",
            "Float",
            "Boolean",
            "Color",
            "Category",
            "",
            "",
            "",
            "",
            "",
            "Small Text Shared Lang"};

    private final Map<String, Integer> fieldTypes = new HashMap<String, Integer>();
    private final Map<Integer, String> fieldClassNames = new HashMap<Integer, String>();

    /**
     * constructor
     */
    protected FieldTypes() {
        fieldTypes.put("Integer", new Integer(INTEGER));
        fieldTypes.put("SmallText", new Integer(SMALLTEXT));
        fieldTypes.put("BigText", new Integer(BIGTEXT));
        fieldTypes.put("Date", new Integer(DATE));
        fieldTypes.put("Page", new Integer(PAGE));
        fieldTypes.put("File", new Integer(FILE));
        fieldTypes.put("Application", new Integer(APPLICATION));
        fieldTypes.put("Float", new Integer(FLOAT));
        fieldTypes.put("Boolean", new Integer(BOOLEAN));
        fieldTypes.put("Color", new Integer(COLOR));
        fieldTypes.put("Category", new Integer(CATEGORY));
        fieldTypes.put("SharedSmallText", new Integer(SMALLTEXT_SHARED_LANG));

        fieldClassNames.put(new Integer(INTEGER), "org.jahia.data.fields.JahiaIntegerField");
        fieldClassNames.put(new Integer(SMALLTEXT), "org.jahia.data.fields.JahiaSmallTextField");
        fieldClassNames.put(new Integer(BIGTEXT), "org.jahia.data.fields.JahiaBigTextField");
        fieldClassNames.put(new Integer(PAGE), "org.jahia.data.fields.JahiaPageField");
        fieldClassNames.put(new Integer(FILE), "org.jahia.data.fields.JahiaFileFieldWrapper");
        fieldClassNames.put(new Integer(APPLICATION), "org.jahia.data.fields.JahiaApplicationField");
        fieldClassNames.put(new Integer(FLOAT), "org.jahia.data.fields.JahiaFloatField");
        fieldClassNames.put(new Integer(BOOLEAN), "org.jahia.data.fields.JahiaBooleanField");
        fieldClassNames.put(new Integer(DATE), "org.jahia.data.fields.JahiaDateField");
        fieldClassNames.put(new Integer(COLOR), "org.jahia.data.fields.JahiaColorField");
        fieldClassNames.put(new Integer(CATEGORY), "org.jahia.data.fields.JahiaCategoryField");
        fieldClassNames.put(new Integer(SMALLTEXT_SHARED_LANG), "org.jahia.data.fields.JahiaSmallTextSharedLangField");

    } // end constructor


    /**
     * returns a single instance of the object
     */
    public static FieldTypes getInstance() {
        return instance;
    } // end getInstance


    /**
     * gets available field types
     */
    public Map<String, Integer> getFieldTypes() {
        return fieldTypes;
    } // end getFieldTypes


    /**
     * gets fieldClassName
     */
    public Map<Integer, String> getFieldClassNames() {
        return fieldClassNames;
    } // end getFieldTypes

    public static String getIconClassName(final int type, final boolean small) {
        final String className;
        switch (type) {
            case FieldTypes.INTEGER:
                className = "number_type";
                break;

            case FieldTypes.SMALLTEXT:
                className = "small_type";
                break;

            case FieldTypes.BIGTEXT:
                className = "big_type";
                break;

            case FieldTypes.DATE:
                className = "date_type";
                break;

            case FieldTypes.PAGE:
                className = "page_type";
                break;

            case FieldTypes.FILE:
                className = "file_type";
                break;

            case FieldTypes.APPLICATION:
                className = "app_type";
                break;

            case FieldTypes.FLOAT:
                className = "number_type";
                break;

            case FieldTypes.BOOLEAN:
                className = "boolean_type";
                break;

            case FieldTypes.COLOR:
                className = "color_type";
                break;

            case FieldTypes.CATEGORY:
                className = "category_type";
                break;

            case FieldTypes.SMALLTEXT_SHARED_LANG:
                className = "small_shared_type";
                break;

            default:
                className = "undefined_type";
                break;
        }
        if (small)
            return className;
        else
            return className + "_big";
    }



} // end FieldTypes
