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
//  LoadFlags
//  you can combine all those values to do a mask !
//  for example  (BIGTEXT | SMALLTEXT | HTTPLINK)
//  DJ      05.01.2001
//
//

package org.jahia.data.fields;

import java.util.HashMap;
import java.util.Map;


public class LoadFlags {

    public static final int INTEGER = 1;
    public static final int SMALLTEXT = 2;
    public static final int BIGTEXT = 4;
    public static final int DATE = 8;
    public static final int PAGE = 16;
    public static final int FILE = 32;
    //public  static  final   int     IMAGE           = 64;
    //public  static  final   int     AUDIOCLIP       = 128;
    //public  static  final   int     VIDEOCLIP       = 256;
    public static final int APPLICATION = 512;
    public static final int FLOAT = 1024;
    public static final int BOOLEAN = 2048;
    public static final int COLOR = 4096;
    public static final int SMALLTEXT_SHARED_LANG = 8192;

    public static final int NEWSFEED = 65536;
    public static final int DATASOURCE = 2 * 65536;

    public static final int NOTHING = 0;
    public static final int ALL = -1;
    public static final int TEXTS = BIGTEXT | SMALLTEXT | COLOR | SMALLTEXT_SHARED_LANG;

    private static LoadFlags theObject;

    private Map<Integer, Integer> loadFlags;
    
    /**
     * constructor
     */
    protected LoadFlags() {
        loadFlags = new HashMap<Integer, Integer>();
        loadFlags.put(new Integer(FieldTypes.INTEGER), new Integer(INTEGER));
        loadFlags.put(new Integer(FieldTypes.SMALLTEXT), new Integer(SMALLTEXT));
        loadFlags.put(new Integer(FieldTypes.BIGTEXT), new Integer(BIGTEXT));
        loadFlags.put(new Integer(FieldTypes.DATE), new Integer(DATE));
        loadFlags.put(new Integer(FieldTypes.PAGE), new Integer(PAGE));
        loadFlags.put(new Integer(FieldTypes.FILE), new Integer(FILE));
        loadFlags.put(new Integer(FieldTypes.APPLICATION), new Integer(APPLICATION));
        loadFlags.put(new Integer(FieldTypes.FLOAT), new Integer(FLOAT));
        loadFlags.put(new Integer(FieldTypes.BOOLEAN), new Integer(BOOLEAN));
        loadFlags.put(new Integer(FieldTypes.COLOR), new Integer(COLOR));
        loadFlags.put(new Integer(FieldTypes.SMALLTEXT_SHARED_LANG), new Integer(SMALLTEXT_SHARED_LANG));
    } // end constructor

    /**
     * returns a single instance of the object
     */
    public static synchronized LoadFlags getInstance() {
        if (theObject == null) {
            theObject = new LoadFlags();
        }
        return theObject;
    } // end getInstance


    /**
     * gets available field types
     */
    public Map<Integer, Integer> getLoadFlags() {
       return (Map<Integer, Integer>)((HashMap<Integer, Integer>)loadFlags).clone();
    } // end getFieldTypes

}

// end LoadFlags
