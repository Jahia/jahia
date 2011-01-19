package org.jahia.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 1/19/11
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class Url {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Url.class);

    /**
     * Encode facet filter URL parameter
     * @param inputString facet filter parameter
     * @return filter encoded for URL query parameter usage
     */

    public static String encodeUrlParam(String inputString) {
        if (StringUtils.isEmpty(inputString)) {
            return inputString;
        }
        // Compress the bytes
        byte[] output = new byte[2048];
        Deflater compresser = new Deflater();
        try {
            compresser.setInput(inputString.getBytes("UTF-8"));
            compresser.finish();
            int compressedDataLength = compresser.deflate(output);
            byte[] copy = new byte[compressedDataLength];
            System.arraycopy(output, 0, copy, 0,
                    Math.min(output.length, compressedDataLength));
            return Base64.encodeBase64URLSafeString(copy);
        } catch (UnsupportedEncodingException e) {
            logger.warn("Not able to encode facet URL: " + inputString, e);
        }

        return inputString;
    }

    /**
     * Decode facet filter URL parameter
     * @param inputString enocded facet filter URL query parameter
     * @return decoded facet filter parameter
     */
    public static String decodeUrlParam(String inputString) {
        if (StringUtils.isEmpty(inputString)) {
            return inputString;
        }
        byte[] input = Base64.decodeBase64(inputString);
        // Decompress the bytes
        Inflater decompresser = new Inflater();
        decompresser.setInput(input, 0, input.length);
        byte[] result = new byte[2048];
        String outputString = "";
        try {
            int resultlength = decompresser.inflate(result);
            decompresser.end();
            outputString = new String(result, 0, resultlength, "UTF-8");
        } catch (DataFormatException e) {
            logger.warn("Not able to decode facet URL: " + inputString, e);
        } catch (UnsupportedEncodingException e) {
            logger.warn("Not able to decode facet URL: " + inputString, e);
        }
        return outputString;
    }
}
