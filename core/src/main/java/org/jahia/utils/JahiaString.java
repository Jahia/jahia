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
