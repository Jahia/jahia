/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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

import org.apache.commons.collections.FastHashMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artofsolving.jodconverter.document.DefaultDocumentFormatRegistry;
import org.artofsolving.jodconverter.document.DocumentFamily;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.jahia.services.SpringContextSingleton;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

public final class FileUtils {

    private static Map<String, String> fileExtensionIcons;
    
    private static String[] fileExtensionIconsMapping;
    
    private static DocumentFormatRegistry formatRegistry = new DefaultDocumentFormatRegistry();
    
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
    
    @SuppressWarnings("unchecked")
    private static Map<String, String> getFileExtensionIcons() {
        if (fileExtensionIcons == null) {
            synchronized (FileUtils.class) {
                if (fileExtensionIcons == null) {
                    SpringContextSingleton ctxHolder = SpringContextSingleton
                            .getInstance();
                    if (ctxHolder.isInitialized()) {
                        Map<String, String> icons = (Map<String, String>) ctxHolder
                                .getContext().getBean("fileExtensionIcons");
                        FastHashMap mappings = new FastHashMap(icons);
                        mappings.setFast(true);
                        String[] jsMappings = new String[2];
                        jsMappings[0] = new StringBuilder(512).append("\"")
                                .append(
                                        StringUtils.join(mappings.keySet()
                                                .iterator(), "\", \"")).append(
                                        "\"").toString();
                        jsMappings[1] = new StringBuilder(512).append("\"")
                                .append(
                                        StringUtils.join(mappings.values()
                                                .iterator(), "\", \"")).append(
                                        "\"").toString();
                        fileExtensionIconsMapping = jsMappings;
                        fileExtensionIcons = mappings;
                    }
                }
            }
        }

        return fileExtensionIcons;
    }

    public static String[] getFileExtensionIconsMapping() {
        if (null == fileExtensionIconsMapping) {
            // initialize it
            getFileExtensionIcons();
        }
        return fileExtensionIconsMapping;
    }

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

    public static String getFileIconFromMimetype(String mimeType) {
        DocumentFormat df = formatRegistry.getFormatByMediaType(mimeType);
        if (df == null) {
            return null;
        }
        Map<String, String> mappings = getFileExtensionIcons();
        if (mappings == null) {
            return "file";
        }

        String icon = mappings.get(df.getExtension());

        return icon != null ? icon : mappings.get("unknown");
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

    private FileUtils () {
        super();
    }
}