/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.textextraction;

import java.util.Set;

/**
 * Filter to find files to redo text extractions
 * 
 * @author Benjamin Papez
 */
public class RepositoryFileFilter {
    String workspace;
    Set<String> mimeTypes;
    String path;
    boolean includeDescendants;
    String fileNamePattern;
    

    public RepositoryFileFilter(String workspace, Set<String> mimeTypes, String path,
            boolean includeDescendants, String fileNamePattern) {
        super();
        this.workspace = workspace;
        this.mimeTypes = mimeTypes;
        this.path = path;
        this.includeDescendants = includeDescendants;
        this.fileNamePattern = fileNamePattern;
    }    
    
    /**
     * List of mime types
     * @return list of mime types
     */
    public Set<String> getMimeTypes() {
        return mimeTypes;
    }
    
    /**
     * Path to narrow search
     * @return path to narrow search
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Include descendants of path
     * @return true when descendants of path should be included
     */
    public boolean isIncludeDescendants() {
        return includeDescendants;
    }
    
    /**
     * Pattern for matching the filename
     * @return pattern for matching the filename
     */
    public String getFileNamePattern() {
        return fileNamePattern;
    }

    /**
     * Workspace to be used
     * @return workspace
     */
    public String getWorkspace() {
        return workspace;
    }
}
