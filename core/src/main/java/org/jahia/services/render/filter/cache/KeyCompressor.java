/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.render.filter.cache;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

/**
 * Class of utility method for compressing/decompressing cache key.
 */

public class KeyCompressor {
    protected transient static Logger logger = org.slf4j.LoggerFactory.getLogger(KeyCompressor.class);

    /**
     * Encode a cacheKey.
     * @param inputString The key to encode
     * @return the encoded key as a Base-64 url safe string
     */
    public static String encodeKey(String inputString) {
        if (StringUtils.isEmpty(inputString)) {
            return inputString;
        }
//        // Compress the bytes
//        byte[] output = new byte[4096];
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
//        Deflater compresser = new Deflater(Deflater.BEST_SPEED);
//        try {
//            compresser.setInput(inputString.getBytes("UTF-8"));
//            compresser.finish();
//            int compressedDataLength;
//            do {
//                compressedDataLength = compresser.deflate(output);
//                if (compressedDataLength > 0) {
//                    outputStream.write(output, 0, compressedDataLength);
//                }
//            } while (compressedDataLength > 0);
//            return Base64.encodeBase64URLSafeString(outputStream.toByteArray());
//        } catch (UnsupportedEncodingException e) {
//            logger.warn("Not able to encode dependency: " + inputString, e);
//        } finally {
//            compresser.end();
//        }

        return inputString;
    }

    /**
     * Decode a base-64 url safe cache key.
     *
     * @param inputString encoded key.
     * @return decoded key
     */
    public static String decodeKey(String inputString) {
//        if (StringUtils.isEmpty(inputString)) {
            return inputString;
//        }
//        byte[] input = Base64.decodeBase64(inputString);
//        // Decompress the bytes
//        StringBuilder outputString = new StringBuilder();
//        try {
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048);
//            InflaterOutputStream inflaterOutputStream = new InflaterOutputStream(outputStream);
//            inflaterOutputStream.write(input, 0, input.length);
//            outputString.append(outputStream.toString("UTF-8"));
//        } catch (UnsupportedEncodingException e) {
//            logger.warn("Not able to decode dependency: " + inputString, e);
//        } catch (IOException e) {
//            logger.warn("Not able to encode dependency: " + inputString, e);
//        }
//        return outputString.toString();
    }
}