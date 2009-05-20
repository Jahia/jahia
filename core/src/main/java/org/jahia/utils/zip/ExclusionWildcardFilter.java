/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.utils.zip;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

/**
 * Wildcard based exclusion filter for ZIP archive entries.
 * 
 * @author Sergiy Shyrkov
 */
public class ExclusionWildcardFilter implements PathFilter {

    private List<String> excludedResources;

    public ExclusionWildcardFilter(List<String> excludedResources) {
        super();
        this.excludedResources = excludedResources;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.utils.zip.PathFilter#accept(java.lang.String)
     */
    public boolean accept(String path) {
        if (excludedResources == null || excludedResources.isEmpty()) {
            return true;
        }

        boolean accept = true;
        for (Iterator<String> iterator = excludedResources.iterator(); iterator
                .hasNext();) {
            String excludePattern = (String) iterator.next();
            if (FilenameUtils.wildcardMatch(path, excludePattern)) {
                accept = false;
                break;
            }
        }

        return accept;
    }

}
