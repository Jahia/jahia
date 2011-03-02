/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

//
//
//  FileUtils
//  EV      19.12.2000
//  MAP     24.01.2002  Files are stored into UTF-8 format.
//

package org.jahia.utils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.FastHashMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;
import org.artofsolving.jodconverter.document.DefaultDocumentFormatRegistry;
import org.artofsolving.jodconverter.document.DocumentFamily;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.jahia.services.SpringContextSingleton;

public final class FileUtils {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(FileUtils.class);

    private static Map<String, String> fileExtensionIcons;
    private static String[] fileExtensionIconsMapping;
    private static DocumentFormatRegistry formatRegistry = new DefaultDocumentFormatRegistry();
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

    public static String[] getFileExtensionIconsMapping() {
        if (null == fileExtensionIconsMapping) {
            // initialize it
            getFileExtensionIcons();
        }
        return fileExtensionIconsMapping;
    }
    
    /***
     * constructor
     * EV    19.12.2000
     *
     */
    private FileUtils () {
        logger.debug("Starting fileUtils");
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

    public static String getExtensionFromMimeType(String mimeType) {
        DocumentFormat df = formatRegistry.getFormatByMediaType(mimeType);
        if (df == null) {
            return null;
        }
        return df.getExtension();
    }

    public static String getExtension(String fileName) {
        return FilenameUtils.getExtension(fileName);
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
		return ResourceUtils.isJarURL(resourceUrl) ? ResourceUtils.getFile(
		        ResourceUtils.extractJarFileURL(resourceUrl)).lastModified() : resource.getFile()
		        .lastModified();
	}
}