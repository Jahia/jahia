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

 package org.jahia.data.containers;

import java.util.List;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 28 avr. 2005
 * Time: 11:56:54
 * To change this template use File | Settings | File Templates.
 */
public class NumberFormats {

    public static final String BYTE_FORMAT = "Byte";
    public static final String SHORT_FORMAT = "Short";
    public static final String INTEGER_FORMAT = "Integer";
    public static final String LONG_FORMAT = "Long";
    public static final String FLOAT_FORMAT = "Float";
    public static final String DOUBLE_FORMAT = "Double";

    private static final String[] formats = {BYTE_FORMAT,SHORT_FORMAT,INTEGER_FORMAT,
                                            LONG_FORMAT,FLOAT_FORMAT,DOUBLE_FORMAT};

    private static List<String> formatsList;

    static {
        formatsList = Arrays.asList(formats);
    }

    /**
     * Return true if the format is the one of the above
     *
     * @param format
     * @return
     */
    public static boolean isValidFormat(String format){
        if ( format == null ){
            return false;
        }
        return formatsList.contains(format);
    }

    /**
     *
     * @param v1
     * @param v2
     * @param format
     * @return
     */
    public static int compareNumber(String v1,
                                    String v2,
                                    String format){
       int result = 0;
       if ( BYTE_FORMAT.equals(format) ){
           return compareByte(v1,v2);
       } else if ( SHORT_FORMAT.equals(format) ){
           return compareShort(v1,v2);
       } else if ( INTEGER_FORMAT.equals(format) ){
           return compareInteger(v1,v2);
       } else if ( LONG_FORMAT.equals(format) ){
           return compareLong(v1,v2);
       } else if ( FLOAT_FORMAT.equals(format) ){
           return compareFloat(v1,v2);
       } else if ( DOUBLE_FORMAT.equals(format) ){
           return compareDouble(v1,v2);
       }
       return result;
    }


   public static int compareByte(String v1, String v2){
        byte n1 = 0;
        byte n2 = 0;

        try {
            n1 = Byte.parseByte(v1);
        } catch ( Exception t){
            n1 = Byte.MIN_VALUE;
        }
        try {
            n2 = Byte.parseByte(v2);
        } catch ( Exception t){
            n2 = Byte.MIN_VALUE;
        }
        if ( n1 == n2 ){
            return 0;
        } else if (  n1 > n2 ){
            return 1;
        }
        return -1;
    }

    public static int compareShort(String v1, String v2){
        short n1 = 0;
        short n2 = 0;

        try {
            n1 = Short.parseShort(v1);
        } catch ( Exception t){
            n1 = Short.MIN_VALUE;
        }
        try {
            n2 = Short.parseShort(v2);
        } catch ( Exception t){
            n2 = Short.MIN_VALUE;
        }
        if ( n1 == n2 ){
            return 0;
        } else if (  n1 > n2 ){
            return 1;
        }
        return -1;
    }

    public static int compareInteger(String v1, String v2){
        int n1 = 0;
        int n2 = 0;

        try {
            n1 = Integer.parseInt(v1);
        } catch ( Exception t){
            n1 = Integer.MIN_VALUE;
        }
        try {
            n2 = Integer.parseInt(v2);
        } catch ( Exception t){
            n2 = Integer.MIN_VALUE;
        }
        if ( n1 == n2 ){
            return 0;
        } else if (  n1 > n2 ){
            return 1;
        }
        return -1;
    }

    public static int compareLong(String v1, String v2){
        long n1 = 0;
        long n2 = 0;

        try {
            n1 = Long.parseLong(v1);
        } catch ( Exception t){
            n1 = Long.MIN_VALUE;
        }
        try {
            n2 = Long.parseLong(v2);
        } catch ( Exception t){
            n2 = Long.MIN_VALUE;
        }
        if ( n1 == n2 ){
            return 0;
        } else if (  n1 > n2 ){
            return 1;
        }
        return -1;
    }

    public static int compareFloat(String v1, String v2){
        float n1 = 0;
        float n2 = 0;

        try {
            n1 = Float.parseFloat(v1);
        } catch ( Exception t){
            n1 = Float.MIN_VALUE;
        }
        try {
            n2 = Float.parseFloat(v2);
        } catch ( Exception t){
            n2 = Float.MIN_VALUE;
        }
        if ( n1 == n2 ){
            return 0;
        } else if (  n1 > n2 ){
            return 1;
        }
        return -1;
    }

    public static int compareDouble(String v1, String v2){
        double n1 = 0;
        double n2 = 0;

        try {
            n1 = Double.parseDouble(v1);
        } catch ( Exception t){
            n1 = Double.MIN_VALUE;
        }
        try {
            n2 = Double.parseDouble(v2);
        } catch ( Exception t){
            n2 = Double.MIN_VALUE;
        }
        if ( n1 == n2 ){
            return 0;
        } else if (  n1 > n2 ){
            return 1;
        }
        return -1;
    }

}
