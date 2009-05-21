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
 package org.jahia.urls;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class URICodec {

    private static Logger logger = Logger.getLogger (URICodec.class);

    private static final String hexString = "0123456789ABCDEF";

    public static final String ALPHANUM_CHARS =
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static final String MARK_CHARS = "-_.!~*'()";
    public static final String UNRESERVED_CHARS = ALPHANUM_CHARS + MARK_CHARS;
    /*
     * The following comply to the standard, but allow for the "+" character
     * which causes problems in URIs because it gets translated back to space
     * characters.
    public static final String RESERVED_CHARS =
        ";/?:@&=+$,";
    public static final String AUTHORITY_AUTHORIZEDCHARS = UNRESERVED_CHARS + "$,;:@&=+";
    public static final String PATH_AUTHORIZEDCHARS = UNRESERVED_CHARS + "/;:@&=+$,";
     * so instead we use the following :
     */
    public static final String RESERVED_CHARS =
        ";/?:@&=$,";
    public static final String AUTHORITY_AUTHORIZEDCHARS = UNRESERVED_CHARS + "$,;:@&=";
    public static final String PATH_AUTHORIZEDCHARS = UNRESERVED_CHARS + "/;:@&=$,";

    public static final String QUERY_AUTHORIZEDCHARS = UNRESERVED_CHARS + "=&";
    public static final String FRAGMENT_AUTHORIZEDCHARS = UNRESERVED_CHARS;

    public static final String DEFAULT_AUTHORIZEDCHARS = ALPHANUM_CHARS +
        MARK_CHARS + RESERVED_CHARS;

    // private static String defaultEncoding = "ISO-8859-1";
    private static String defaultEncoding = "UTF-8";
    private static String defaultAuthorizedChars = DEFAULT_AUTHORIZEDCHARS;

    public static String encode(String input, String encoding, String authorizedChars)
        throws java.io.UnsupportedEncodingException {
        if ((input == null) || (encoding == null)) {
            return null;
        }
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < input.length(); i++) {
            String curChar = input.substring(i, i+1);
            if (authorizedChars.indexOf(curChar) == -1) {
                // we found a character that is not authorized, we must encode it.
                // first we must determine if it's a multi-byte character or
                // not.
                byte[] charBytes = curChar.getBytes(encoding);
                for (int j=0; j < charBytes.length; j++) {
                    result.append("%");
                    char hiHex = hexString.charAt((charBytes[j] & 0xFF) >> 4);
                    result.append(hiHex);
                    char lowHex = hexString.charAt(charBytes[j] & 0xF);
                    result.append(lowHex);
                }
            } else {
                result.append(curChar);
            }
        }
        return result.toString();
    }

    public static String encode(String input, String authorizedChars)
        throws java.io.UnsupportedEncodingException {
        return encode(input, defaultEncoding, authorizedChars);
    }

    public static String encode(String input)
        throws java.io.UnsupportedEncodingException {
        return encode(input, defaultEncoding, defaultAuthorizedChars );
    }

    public static String decode(String input, String encoding)
        throws java.io.UnsupportedEncodingException {
        if ((input == null) || (encoding == null)) {
            return null;
        }
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        int currentPos = 0;
        try {
            while ( (input.indexOf("%", currentPos) != -1) &&
                   (currentPos < input.length())) {
                int encodedBytePos = input.indexOf("%", currentPos);
                if (encodedBytePos > currentPos) {
                byteOut.write(input.substring(currentPos, encodedBytePos).
                              getBytes(encoding));
                }
                char hiHexChar = input.charAt(encodedBytePos + 1);
                char lowHexChar = input.charAt(encodedBytePos + 2);
                int hiHex = hexString.indexOf(hiHexChar);
                int lowHex = hexString.indexOf(lowHexChar);
                if ( (hiHex >= 0) && (lowHex >= 0)) {
                    byte curByte = (byte) (( (hiHex << 4) + lowHex) & 0xFF);
                    byteOut.write(curByte);
                    currentPos = encodedBytePos + 3;
                } else {
                    currentPos = encodedBytePos + 1;
                }
            }
            byteOut.write(input.substring(currentPos).getBytes(encoding));
            byteOut.flush();
        } catch (IOException ioe) {
            logger.error("Error while writing to ByteArrayOutputStream", ioe);
        }
        return byteOut.toString(encoding);
    }

    public static String decode(String input)
        throws java.io.UnsupportedEncodingException {
        return decode(input, defaultEncoding);
    }

    static public String getDefaultEncoding() {
        return defaultEncoding;
    }

    static public void setDefaultEncoding(String encoding) {
        defaultEncoding = encoding;
    }

    static public String getDefaultAuthorizedChars () {
        return defaultAuthorizedChars;
    }

    static public void setDefaultAuthorizedChars (String authorizedChars) {
        defaultAuthorizedChars = authorizedChars;
    }

}