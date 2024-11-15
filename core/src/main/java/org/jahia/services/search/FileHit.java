/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
