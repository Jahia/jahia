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

package org.jahia.engines.search;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jcr.Property;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.data.beans.LookupBaseBean;
import org.jahia.data.search.JahiaSearchHitInterface;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.utils.FileUtils;

/**
 * File and folder search result item, used as a view object in JSP templates.
 * 
 * @author Sergiy Shyrkov
 */
public class FileHit extends AbstractHit {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(FileHit.class);

    private static final Map<String, String> ICONS_TYPES;

    // TODO move this mapping to some configuration resource
    static {
        ICONS_TYPES = new HashMap<String, String>();

        ICONS_TYPES.put("unknown", "file");

        ICONS_TYPES.put("txt", "txt");
        ICONS_TYPES.put("text", "txt");

        ICONS_TYPES.put("html", "html");
        ICONS_TYPES.put("htm", "html");

        ICONS_TYPES.put("pdf", "pdf");

        ICONS_TYPES.put("doc", "doc");
        ICONS_TYPES.put("ppt", "ppt");
        ICONS_TYPES.put("xls", "xls");

        ICONS_TYPES.put("jar", "zip");
        ICONS_TYPES.put("zip", "zip");
        ICONS_TYPES.put("rar", "rar");
        ICONS_TYPES.put("tar", "zip");
        ICONS_TYPES.put("tgz", "zip");

        ICONS_TYPES.put("mp3", "sound");
        ICONS_TYPES.put("wav", "sound");
        ICONS_TYPES.put("mid", "sound");
        ICONS_TYPES.put("midi", "sound");

        ICONS_TYPES.put("bmp", "img");
        ICONS_TYPES.put("gif", "img");
        ICONS_TYPES.put("ico", "img");
        ICONS_TYPES.put("jpeg", "img");
        ICONS_TYPES.put("jpg", "img");
        ICONS_TYPES.put("png", "img");
        ICONS_TYPES.put("tif", "img");
        ICONS_TYPES.put("tiff", "img");

        ICONS_TYPES.put("avi", "video");
        ICONS_TYPES.put("mpeg", "video");
        ICONS_TYPES.put("mpa", "video");
        ICONS_TYPES.put("mpe", "video");
        ICONS_TYPES.put("mpg", "video");
        ICONS_TYPES.put("mov", "video");

        ICONS_TYPES.put("exe", "exe");
    }

    private JCRNodeWrapper node;

    private Map<String, String> propertiesFacade;

    private String content;

    /**
     * Initializes an instance of this class.
     * 
     * @param searchHit
     *            search result item to be wrapped
     * @param processingContext
     *            current processing context object
     */
    public FileHit(JahiaSearchHitInterface searchHit,
            ProcessingContext processingContext) {
        super(searchHit);
        this.node = (JCRNodeWrapper) searchHit.getObject();
        this.propertiesFacade = new LookupBaseBean<String, String>() {
            private Map<String, String> properties;

            @Override
            public Set<Map.Entry<String, String>> entrySet() {
                return getProperties().entrySet();
            }

            @Override
            public String get(Object key) {
                return node.getPropertyAsString(String.valueOf(key));
            }

            private Map<String, String> getProperties() {
                if (null == properties) {
                    properties = node.getPropertiesAsString();
                }
                return properties;
            }

            @Override
            public Set<String> keySet() {
                return getProperties().keySet();
            }

            @Override
            public int size() {
                return getProperties().size();
            }

            @Override
            public Collection<String> values() {
                return getProperties().values();
            }
        };
    }

    /**
     * Returns the resource content length in bytes if applicable.
     * 
     * @return resource content length in bytes if applicable
     */
    public long getContentLength() {
        return node.getFileContent().getContentLength();
    }

    public String getContentType() {
        return node.getFileContent().getContentType();
    }

    public Date getCreated() {
        return node.getCreationDateAsDate();
    }

    public String getCreatedBy() {
        return node.getCreationUser();
    }

    public String getFolderPath() {
        return node.isCollection() ? node.getPath() : FilenameUtils
                .getFullPathNoEndSeparator(node.getPath());
    }

    public String getIconType() {
        String extension = FilenameUtils.getExtension(node.getName());
        String icon = StringUtils.isNotEmpty(extension) ? ICONS_TYPES
                .get(extension.toLowerCase()) : null;

        return icon != null ? icon : ICONS_TYPES.get("unknown");
    }

    public Date getLastModified() {
        return node.getLastModifiedAsDate();
    }

    public String getLastModifiedBy() {
        return node.getModificationUser();
    }

    public String getLink() {
        return node.getUrl();
    }

    public String getName() {
        return node.getName();
    }

    public String getPath() {
        return node.getPath();
    }

    public Map<String, String> getProperties() {
        return propertiesFacade;
    }

    public JCRNodeWrapper getRawHit() {
        return node;
    }

    public int getSizeKb() {
        int sizeInKb = (int) (getContentLength() / 1000f);
        return sizeInKb > 0 ? sizeInKb : 1;
    }

    public String getSummary() {
        String summary = searchHit.getTeaser();

        if (summary == null || summary.length() == 0) {
            summary = getContent();
            if (summary != null) {
                if (summary.length() > 175) {
                    return summary.substring(0, 175);
                }
                return summary;
            } else {
                summary = node.getName();
            }
        }
        return summary;
    }

    /**
     * Returns the hit text content.
     *
     * @return the hit text content
     */
    public String getContent() {
        if (content != null){
            return content;
        }
        if (!node.isFile() || node.getFileContent().isImage()){
            content = "";
            return content;
        } else {
            try {
                if (node.getRealNode().hasNode(Constants.JCR_CONTENT)){
                    if (node.getRealNode().getNode(Constants.JCR_CONTENT).hasProperty(Constants.EXTRACTED_TEXT)){
                        Property property = node.getRealNode().getNode(Constants.JCR_CONTENT)
                                .getProperty(Constants.EXTRACTED_TEXT);
                        content = FileUtils.readerToString(new InputStreamReader(property.getStream()));
                    }
                }
            } catch ( Throwable t ){
                logger.debug(t);
            }
        }
        if (content == null){
            content = "";
        }
        return content;
    }

    public String getTitle() {
        return node.getName();
    }

    public Type getType() {
        return isFolder() ? Type.FOLDER : Type.FILE;
    }

    public boolean isFolder() {
        return node.isCollection();
    }
}
