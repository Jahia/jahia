package org.jahia.services.render.filter.cache;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.Deflater;
import java.util.zip.InflaterOutputStream;

public class KeyCompressor {
    protected transient static Logger logger = org.slf4j.LoggerFactory.getLogger(KeyCompressor.class);
    public static String encodeKey(String inputString) {
        if (StringUtils.isEmpty(inputString)) {
            return inputString;
        }
        // Compress the bytes
        byte[] output = new byte[4096];
        Deflater compresser = new Deflater(Deflater.BEST_SPEED);
        try {
            compresser.setInput(inputString.getBytes("UTF-8"));
            compresser.finish();
            int compressedDataLength = compresser.deflate(output);
            byte[] copy = new byte[compressedDataLength];
            System.arraycopy(output, 0, copy, 0, Math.min(output.length, compressedDataLength));
            return Base64.encodeBase64URLSafeString(copy);
        } catch (UnsupportedEncodingException e) {
            logger.warn("Not able to encode dependency: " + inputString, e);
        }

        return inputString;
    }

    /**
     * Decode facet filter URL parameter
     *
     * @param inputString enocded facet filter URL query parameter
     * @return decoded facet filter parameter
     */
    public static String decodeKey(String inputString) {
        if (StringUtils.isEmpty(inputString)) {
            return inputString;
        }
        byte[] input = Base64.decodeBase64(inputString);
        // Decompress the bytes
        StringBuilder outputString = new StringBuilder();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048);
            InflaterOutputStream inflaterOutputStream = new InflaterOutputStream(outputStream);
            inflaterOutputStream.write(input, 0, input.length);
            outputString.append(outputStream.toString("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.warn("Not able to decode dependency: " + inputString, e);
        } catch (IOException e) {
            logger.warn("Not able to encode dependency: " + inputString, e);
        }
        return outputString.toString();
    }
}