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

package org.jahia.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Locale;

public class JahiaString {

    private final static QuotedPrintableCodec QP_CODEC = new QuotedPrintableCodec();

    public static String adjustStringSize(String str, int size) {
        if (str == null) // FIXNE : Don't like this null return.
            return null;
        if (str.length() > size) {
            return str.substring(0, size - 2) + "..";
        } else {
            StringBuffer emtpyStr = new StringBuffer();
            for (int i = 0; i < size - str.length(); i++) {
                emtpyStr.append(" ");
            }
            return str + emtpyStr;
        }
    }

    // This method help to support get proper string like Chinese
    public static String getProperStr(String str, Locale locale) {
        return str;
    }

    public static String generateRandomString(int length) {
        SecureRandom randomGen = new SecureRandom();
        StringBuffer result = new StringBuffer();
        int count = 0;
        while (count < length) {
            int randomSel = randomGen.nextInt(3);
            int randomInt = randomGen.nextInt(26);
            char randomChar = '0';
            switch (randomSel) {
                case 0: randomChar = (char) (((int)'A') + randomInt); break;
                case 1: randomChar = (char) (((int)'a') + randomInt); break;
                case 2: randomChar = (char) (((int)'0') + (randomInt % 10)); break;
            }
            result.append(randomChar);
            count++;
        }
        return result.toString();
    }

    /**
     * Encodes the given value into QUOTEE-PRINTABLE using the given charset
     *
     * @param value   The Value to encode
     * @param charSet The charSet to use
     * @return The encoded value in a String object
     */
    public synchronized static String encodeToQP(final String value, final String charSet) throws UnsupportedEncodingException {
        return QP_CODEC.encode(value, charSet);
    }

    /**
     * Decodes a value encoded in QUOTED-PRINTABLE using the given charset
     *
     * @param value   The value to decode
     * @param charSet The given charset
     * @return The decoded value in a String object
     */
    public synchronized static String decodeQP(final String value, final String charSet)
            throws DecoderException, UnsupportedEncodingException, IOException {
        return QP_CODEC.decode(value, charSet);
    }
}
