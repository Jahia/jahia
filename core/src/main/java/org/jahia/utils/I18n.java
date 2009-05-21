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

