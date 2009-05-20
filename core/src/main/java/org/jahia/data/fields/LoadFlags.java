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
