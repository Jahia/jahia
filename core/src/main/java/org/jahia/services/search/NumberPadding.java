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
