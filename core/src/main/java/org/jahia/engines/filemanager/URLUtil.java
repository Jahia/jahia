/*
 * $Header$
 * $Revision: 208507 $
 * $Date: 2005-02-15 11:45:14 +0100 (Tue, 15 Feb 2005) $
 *
 * ====================================================================
 *
 * Copyright 1999-2002 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package org.jahia.engines.filemanager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.jahia.engines.calendar.CalendarHandler;


/**
 * General purpose request parsing and encoding utility methods.
 *
 * @version $Revision: 208507 $ $Date: 2005-02-15 11:45:14 +0100 (Tue, 15 Feb 2005) $
 */

public final class URLUtil {


    private static final transient Logger logger = Logger.getLogger(URLUtil.class);
    
    // -------------------------------------------------------------- Variables


    /**
     * The DateFormat to use for generating readable dates in cookies.
     */
    private static SimpleDateFormat format =
        new SimpleDateFormat(CalendarHandler.DEFAULT_DATE_FORMAT);


    /**
     * Array containing the safe characters set.
     */
    private static BitSet safeCharacters;


    private static final char[] hexadecimal =
    {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
     'A', 'B', 'C', 'D', 'E', 'F'};


    // ----------------------------------------------------- Static Initializer


    static {

        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        safeCharacters = new BitSet(256);
        int i;
        for (i = 'a'; i <= 'z'; i++) {
            safeCharacters.set(i);
        }
        for (i = 'A'; i <= 'Z'; i++) {
            safeCharacters.set(i);
        }
        for (i = '0'; i <= '9'; i++) {
            safeCharacters.set(i);
        }
        safeCharacters.set('-');
        safeCharacters.set('_');
        safeCharacters.set('.');
        safeCharacters.set('*');
        safeCharacters.set('/');
        safeCharacters.set('+');
        safeCharacters.set('@');
        safeCharacters.set('$');
        safeCharacters.set(',');
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Decode and return the specified URL-encoded String.
     * When the byte array is converted to a string, the system default
     * character encoding is used...  This may be different than some other
     * servers.
     *
     * @param str The url-encoded string
     *
     * @exception IllegalArgumentException if a '%' character is not followed
     * by a valid 2-digit hexadecimal number
     * @deprecated
     */
    public static String URLDecode(String str) {

        return URLDecode(str, null);

    }


    /**
     * Decode and return the specified URL-encoded String.
     *
     * @param str The url-encoded string
     * @param enc The encoding to use; if null, the default encoding is used
     * @exception IllegalArgumentException if a '%' character is not followed
     * by a valid 2-digit hexadecimal number
     * @deprecated
     */
    public static String URLDecode(String str, String enc) {

        if (str == null)
            return (null);

        // FIXME: replace all byte[] by char[]
        byte[] bytes;
        if (enc==null) {
            bytes=str.getBytes();
        }
        else {
            try {
                bytes=str.getBytes(enc);
            }
            catch (UnsupportedEncodingException ex) {
                bytes=str.getBytes();
            }
        }

        return URLDecode(bytes, enc);
    }


    /**
     * Decode and return the specified URL-encoded byte array.
     *
     * @param bytes The url-encoded byte array of an URL path.
     * @exception IllegalArgumentException if a '%' character is not followed
     * by a valid 2-digit hexadecimal number
     */
    public static String URLDecode(byte[] bytes) {
        return URLDecode(bytes, null);
    }


    /**
     * Decode and return the specified URL-encoded byte array.
     *
     * @param bytes The url-encoded byte array of an URL path.
     * @param enc The encoding to use; if null, the default encoding is used
     * @exception IllegalArgumentException if a '%' character is not followed
     * by a valid 2-digit hexadecimal number
     */
    public static String URLDecode(byte[] bytes, String enc) {

        if (bytes == null)
            return (null);

        int len = bytes.length;
        int ix = 0;
        int ox = 0;
        while (ix < len) {
            byte b = bytes[ix++];     // Get byte to test
            if (b == '%') {
                b = (byte) ((convertHexDigit(bytes[ix++]) << 4)
                            + convertHexDigit(bytes[ix++]));
            }
            bytes[ox++] = b;
        }
        if (enc != null) {
            try {
                return new String(bytes, 0, ox, enc);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return new String(bytes, 0, ox);

    }


    /**
     * Convert a byte character value to hexidecimal digit value.
     *
     * @param b the character value byte
     */
    private static byte convertHexDigit( byte b ) {
        if ((b >= '0') && (b <= '9')) return (byte)(b - '0');
        if ((b >= 'a') && (b <= 'f')) return (byte)(b - 'a' + 10);
        if ((b >= 'A') && (b <= 'F')) return (byte)(b - 'A' + 10);
        return 0;
    }


    /**
     * URL rewriter for URL paths.
     *
     * @param path Path which has to be rewiten
     */
    public static String URLEncode(String path, String enc) {

        /**
         * Note: This code portion is very similar to URLEncoder.encode.
         * Unfortunately, there is no way to specify to the URLEncoder which
         * characters should be encoded. Here, ' ' should be encoded as "%20"
         * and '/' shouldn't be encoded. '+' should not be encoded in pathes too.
         */

        int maxBytesPerChar = 10;
        StringBuffer rewrittenPath = new StringBuffer(path.length());
        ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);
        OutputStreamWriter writer = null;
        try {
            // FIXME: Use the same encoding as the one specified above
            writer = new OutputStreamWriter(buf, enc);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            writer = new OutputStreamWriter(buf);
        }

        for (int i = 0; i < path.length(); i++) {
            int c = path.charAt(i);
            if (safeCharacters.get(c)) {
                rewrittenPath.append((char)c);
            } else {
                // convert to external encoding before hex conversion
                try {
                    writer.write(c);
                    writer.flush();
                } catch(IOException e) {
                    buf.reset();
                    continue;
                }
                byte[] ba = buf.toByteArray();
                for (int j = 0; j < ba.length; j++) {
                    // Converting each byte in the buffer
                    byte toEncode = ba[j];
                    rewrittenPath.append('%');
                    int low = (toEncode & 0x0f);
                    int high = ((toEncode & 0xf0) >> 4);
                    rewrittenPath.append(hexadecimal[high]);
                    rewrittenPath.append(hexadecimal[low]);
                }
                buf.reset();
            }
        }

        return rewrittenPath.toString();

    }
}


