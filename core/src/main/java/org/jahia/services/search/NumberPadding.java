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

 package org.jahia.services.search;

import org.apache.commons.lang.StringUtils;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 22 f�vr. 2005
 * Time: 17:26:02
 * To change this template use File | Settings | File Templates.
 */
public class NumberPadding {

    public static String pad(String str){
        String result = str;
        try {
            if ( str != null && !"".equals(str.trim()) && StringUtils.isNumeric(str) ){
                result = StringUtils.leftPad(str,15,'0');
            }
        } catch ( Exception t ){
        }
        return result;
    }

    public static String pad(int i){
        return pad(String.valueOf(i));
    }

    public static String pad(long l){
        return pad(String.valueOf(l));
    }

    public static String unpad(String str){
        String result = str;
        try {
            if ( StringUtils.isNumeric(str) && str.length()>1 ){
                char[] chars = str.toCharArray();
                StringBuffer buffer = new StringBuffer();
                boolean unpadded = false;
                for ( int i=0; i<chars.length-1; i++ ){
                    if ( !unpadded && chars[i] == '0'){
                        continue;
                    } else {
                        buffer.append(chars[i]);
                        unpadded = true;
                    }
                }
                buffer.append(str.charAt(str.length()-1));
                return buffer.toString();
            }
        } catch ( Exception t ){
        }
        return result;
    }

}
