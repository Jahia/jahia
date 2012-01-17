/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.search;

import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;

import javax.jcr.RepositoryException;

/**
 * File and folder search result item, used as a view object in JSP templates.
 * 
 * @author Sergiy Shyrkov
 */
public class FileHit extends JCRNodeHit {

    /**
     * Initializes an instance of this class.
     * 
     * @param node search result item to be wrapped
     * @param context
     */
    public FileHit(JCRNodeWrapper node, RenderContext context) {
        super(node, context);
    }

    /**
     * Returns the folder path for this hit.
     * 
     * @return the folder path for this hit
     */
    public String getFolderPath() {
        return isFolder() ? getPath() : FilenameUtils.getFullPathNoEndSeparator(getPath());
    }

    /**
     * Returns an icon name that corresponds to the current item. Mapping
     * between file extensions and icons is configured in the
     * <code>applicationcontext-basejahiaconfig.xml</code> file.
     * 
     * @return an icon name that corresponds to the current item
     */
    public String getIconType() {
        Map<String, String> types = getIconTypes();
        String extension = isFolder() ? "dir" : FilenameUtils.getExtension(getName());
        String icon = StringUtils.isNotEmpty(extension) ? types.get(extension.toLowerCase()) : null;

        return icon != null ? icon : types.get("unknown");
    }

    private Map<String, String> getIconTypes() {
        return JCRContentUtils.getInstance().getFileExtensionIcons();
    }

    public String getLink() {
        return resource.getUrl();
    }

    /**
     * Returns the resource content length in bytes if applicable.
     *
     * @return resource content length in bytes if applicable
     */
    public long getContentLength() {
        return resource.getFileContent().getContentLength();
    }

    /**
     * Returns the resource content length in kilobytes if applicable.
     *
     * @return resource content length in kilobytes if applicable
     */
    public long getContentLengthKb() {
        long length = getContentLength();
        return (long) (length > 0 ? length / 1000f : 0);
    }

    public String getContentType() {
        return resource.getFileContent().getContentType();
    }    

    /**
     * Returns <code>true</code> if this search hit represents a folder.
     * 
     * @return <code>true</code> if this search hit represents a folder
     */
    public boolean isFolder() {
        try {
            return resource.isNodeType("nt:folder");
        } catch (RepositoryException e) {
            return false;
        }
    }
}
