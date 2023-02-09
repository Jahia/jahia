/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
//
//
//  FileUtils
//  EV      19.12.2000
//  MAP     24.01.2002  Files are stored into UTF-8 format.
//

package org.jahia.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections.FastHashMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFamily;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.SpringContextSingleton;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.*;

public final class FileUtils {

    @SuppressWarnings("unchecked")
    private static class Holder {
        static final Map<String, String> fileExtensionIcons;
        static final Map<String, String> fileMimeTypeIcons;
        static final String[] fileExtensionIconsMapping;
        static {
            Map<String, String> icons = (Map<String, String>) SpringContextSingleton.getBean("fileExtensionIcons");
            FastHashMap mappings = new FastHashMap(icons);
            mappings.setFast(true);
            String[] jsMappings = new String[2];
            jsMappings[0] = new StringBuilder(512).append("\"")
                    .append(StringUtils.join(mappings.keySet().iterator(), "\", \"")).append("\"").toString();
            jsMappings[1] = new StringBuilder(512).append("\"")
                    .append(StringUtils.join(mappings.values().iterator(), "\", \"")).append("\"").toString();
            fileExtensionIconsMapping = jsMappings;
            fileExtensionIcons = mappings;
            fileMimeTypeIcons = new FastHashMap((Map<String, String>) SpringContextSingleton.getBean("fileMimeTypeIcons"));
        }
    }

    private static DocumentFormatRegistry formatRegistry = DefaultDocumentFormatRegistry.getInstance();

    /**
     * Calculates the MD5 digest from the content of the supplied input stream. The supplied stream is closed after reading it.
     *
     * @param is the resource input stream
     * @return the MD5 digest (as a HEX string) from the content of the supplied input stream
     */
    public static String calculateDigest(InputStream is) {
        try (DigestInputStream digestInputStream = toDigestInputStream(
                (is instanceof BufferedInputStream) ? is : new BufferedInputStream(is))) {
            byte[] b = new byte[1024 * 8];
            int read = 0;
            while (read != -1) {
                read = digestInputStream.read(b);
            }
            return Hex.encodeHexString(digestInputStream.getMessageDigest().digest());
        } catch (IOException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    /**
     * Cleans a directory without deleting it, considering also named exclusions.
     *
     * @param directory directory to clean
     * @param filter the file filter to consider
     * @throws IOException in case cleaning is unsuccessful
     * @see org.apache.commons.io.FileUtils#cleanDirectory(File)
     */
    public static void cleanDirectory(File directory, FileFilter filter) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        File[] files = directory.listFiles();
        if (files == null) { // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }

        if (files.length == 0) {
            return;
        }

        IOException exception = null;
        for (File file : files) {
            if (filter != null && !filter.accept(file)) {
                continue;
            }
            try {
                org.apache.commons.io.FileUtils.forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * Returns the content of the specified {@link Resource} as a string.
     *
     * @param resource
     *            the resource to be read
     * @return the content of the specified {@link Resource} as a string
     * @throws IOException
     *             in case of an I/O error
     */
    public static String getContent(Resource resource) throws IOException {
        String content = null;
        InputStream is = null;
        try {
            is = resource.getInputStream();
            content = IOUtils.toString(is);
        } finally {
            IOUtils.closeQuietly(is);
        }

        return content;
    }

    public static String getExtension(String fileName) {
        return FilenameUtils.getExtension(fileName);
    }

    public static String getExtensionFromMimeType(String mimeType) {
        DocumentFormat df = formatRegistry.getFormatByMediaType(mimeType);
        if (df == null) {
            return null;
        }
        return df.getExtension();
    }

    private static Map<String, String> getFileExtensionIcons() {
        return Holder.fileExtensionIcons;
    }

    private static Map<String, String> getFileMimeTypeIcons() {
        return Holder.fileMimeTypeIcons;
    }

    public static String[] getFileExtensionIconsMapping() {
        return Arrays.copyOf(Holder.fileExtensionIconsMapping, Holder.fileExtensionIconsMapping.length);
    }

    /**
     * Return file icon for the given file name, based on the extension
     * @param fileName the name of the file
     * @return the icon name
     */
    public static String getFileIcon(String fileName) {
        String ext = "unknown";
        if (StringUtils.isNotEmpty(fileName)) {
            int index = FilenameUtils.indexOfExtension(fileName);
            if (index != -1) {
                ext = fileName.substring(index + 1);
            } else {
                ext = fileName;
            }
            ext = ext.toLowerCase();
        }
        Map<String, String> mappings = getFileExtensionIcons();
        if (mappings == null) {
            return "file";
        }

        String icon = mappings.get(ext);

        return icon != null ? icon : mappings.get("unknown");
    }

    /**
     * Return file icon for the given mime type
     * @param mimeType the mime type of the file
     * @return the icon name
     */
    public static String getFileIconFromMimetype(String mimeType) {
        if (StringUtils.isNotEmpty(mimeType)) {
            String mappedMimeTypeIcon = getFileMimeTypeIcons().get(mimeType);

            // look if exact mimetype is defined
            if (mappedMimeTypeIcon != null) {
                return mappedMimeTypeIcon;
            }

            // look in generic mimetype with wildcard
            for (Map.Entry<String, String> mappedMimeTypeEntry : getFileMimeTypeIcons().entrySet()) {
                if (mappedMimeTypeEntry.getKey().endsWith("*") &&
                        mimeType.startsWith(StringUtils.substringBeforeLast(mappedMimeTypeEntry.getKey(), "*"))) {
                    return mappedMimeTypeEntry.getValue();
                }
            }
        }

        // not found
        return getFileMimeTypeIcons().get("unknown");
    }

    /**
     * Returns the last modified date of the specified resource.
     *
     * @param resource
     *            resource to check the last modified date on
     * @return the last modified date of the specified resource
     * @throws IOException
     *             in case of an I/O error
     */
    public static long getLastModified(Resource resource) throws IOException {
        URL resourceUrl = resource.getURL();
        return ResourceUtils.isJarURL(resourceUrl) ? ResourceUtils.getFile(ResourceUtils.extractJarFileURL(resourceUrl)).lastModified() : resource.lastModified();
    }

    public static List<DocumentFormat> getPossibleFormats() {
        Set<DocumentFormat> map = new LinkedHashSet<DocumentFormat>();
        Set<DocumentFormat> formatSet = formatRegistry.getOutputFormats(DocumentFamily.TEXT);
        map.addAll(formatSet);
        formatSet = formatRegistry.getOutputFormats(DocumentFamily.SPREADSHEET);
        map.addAll(formatSet);
        formatSet = formatRegistry.getOutputFormats(DocumentFamily.PRESENTATION);
        map.addAll(formatSet);
        formatSet = formatRegistry.getOutputFormats(DocumentFamily.DRAWING);
        map.addAll(formatSet);
        List<DocumentFormat> list = new ArrayList<DocumentFormat>(map);
        Collections.sort(list, new Comparator<DocumentFormat>() {
            public int compare(DocumentFormat o1, DocumentFormat o2) {
                return o1.getExtension().compareTo(o2.getExtension());
            }
        });
        return list;
    }

    /**
     * Returns a human-readable representation of the file size (number of bytes).
     *
     * @param bytes
     *            the file size in bytes
     * @return a human-readable representation of the file size (number of bytes)
     */
    public static String humanReadableByteCount(long bytes) {
        return humanReadableByteCount(bytes, false);
    }

    /**
     * Returns a human-readable representation of the file size (number of bytes).
     *
     * @param bytes
     *            the file size in bytes
     * @param withDetails
     *            if true the full display view is used, which also includes the byte count
     * @return a human-readable representation of the file size (number of bytes)
     */
    public static String humanReadableByteCount(long bytes, boolean withDetails) {
        if (bytes < org.apache.commons.io.FileUtils.ONE_KB) {
            return bytes + " bytes";
        }

        StringBuilder display = new StringBuilder();

        long divider = org.apache.commons.io.FileUtils.ONE_KB;
        if (bytes / org.apache.commons.io.FileUtils.ONE_GB > 0) {
            divider = org.apache.commons.io.FileUtils.ONE_GB;
            display.append(" GB");
        } else if (bytes / org.apache.commons.io.FileUtils.ONE_MB > 0) {
            divider = org.apache.commons.io.FileUtils.ONE_MB;
            display.append(" MB");
        } else {
            display.append(" KB");
        }

        display.insert(0, new DecimalFormat("###,###,###,###,###,###,###.##").format((double) bytes / (double) divider));
        if (withDetails) {
            display.append(" (").append(new DecimalFormat("###,###,###,###,###,###,###").format(bytes)).append(" bytes)");
        }

        return display.toString();
    }

    /**
     * Moves the content of the directory to the specified one considering the filter.
     * @param srcDir the source directory to move content from
     * @param destDir the target directory
     * @param filter a filter for inclusions
     * @throws IOException in case of an I/O errors
     */
    public static void moveDirectoryContentToDirectory(File srcDir, File destDir, FileFilter filter)
            throws IOException {
        File[] files = srcDir.listFiles();
        if (files != null && files.length > 0) {
            for (File f : files) {
                if (filter == null || filter.accept(f)) {
                    org.apache.commons.io.FileUtils.moveToDirectory(f, destDir, true);
                }
            }
        }
    }

    private static DigestInputStream toDigestInputStream(InputStream is) {
        try {
            return new DigestInputStream(is, MessageDigest.getInstance("MD5"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private FileUtils () {
        super();
    }
}
