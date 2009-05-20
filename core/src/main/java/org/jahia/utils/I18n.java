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
package org.jahia.utils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;


/**
 *
 * @author  Khue Nguyen
 */

public class I18n {

    static Map w1252ToISO;
    static {
        w1252ToISO = new HashMap();
        w1252ToISO.put(new Character('\u0080'), "Euro");
        w1252ToISO.put(new Character('\u0082'), ",");
        w1252ToISO.put(new Character('\u0083'), "f");
        w1252ToISO.put(new Character('\u0085'), "...");
        w1252ToISO.put(new Character('\u0088'), "^");
        w1252ToISO.put(new Character('\u008B'), "<");
        w1252ToISO.put(new Character('\u008C'), "OE");
        w1252ToISO.put(new Character('\u0091'), "'");
        w1252ToISO.put(new Character('\u0092'), "'");
        w1252ToISO.put(new Character('\u0093'), "\"");
        w1252ToISO.put(new Character('\u0094'), "\"");
        w1252ToISO.put(new Character('\u0095'), ".");
        w1252ToISO.put(new Character('\u0096'), "-");
        w1252ToISO.put(new Character('\u0097'), "-");
        w1252ToISO.put(new Character('\u0098'), "~");
        w1252ToISO.put(new Character('\u009B'), ">");
        w1252ToISO.put(new Character('\u009C'), "oe");
    }

    /**
     * Convert a request parameter value to java unicode string
     */
    public static String getText(HttpServletRequest request, String paramName ) {

        if ( paramName == null )
            return null;

        String value = request.getParameter(paramName);
        if ( value == null )
            return null;

        try{
            value = new String(value.getBytes(), request.getCharacterEncoding());
        }catch(java.io.UnsupportedEncodingException ex){
            System.err.println(ex);
        }catch ( NullPointerException nex ){
            System.err.println(nex);
        }
        return value;
    }

    public static String windows1252ToISO(String windows1252encoded) {
        StringBuffer transcodedValue = new StringBuffer(windows1252encoded.length());
        for (int i=0; i < windows1252encoded.length(); i++) {
            char curChar = windows1252encoded.charAt(i);
            if ((curChar >= 128) && (curChar <= 159)) {
                // this range of characters is unused in ISO-8859-1 but
                // is used by Windows Central Europe encoding, so we can
                // detect it and transcode some of it's characters.
                Character charIndex = new Character(curChar);
                String replacementStr = (String) w1252ToISO.get(charIndex);
                if (replacementStr != null) {
                    transcodedValue.append(replacementStr);
                }
            } else {
                transcodedValue.append(curChar);
            }
        }
        return transcodedValue.toString();
    }
}

