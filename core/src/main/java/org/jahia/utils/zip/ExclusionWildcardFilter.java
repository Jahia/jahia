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
package org.jahia.utils.zip;

import java.util.List;

import org.apache.commons.io.FilenameUtils;

/**
 * Wildcard based exclusion filter for ZIP archive entries.
 *
 * @author Sergiy Shyrkov
 */
public class ExclusionWildcardFilter implements PathFilter {

    private String[] excludedResources;

    public ExclusionWildcardFilter(List<String> excludedResources) {
        super();
        this.excludedResources = excludedResources.toArray(new String[0]);
    }

    public ExclusionWildcardFilter(String... excludedResources) {
        super();
        this.excludedResources = excludedResources;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jahia.utils.zip.PathFilter#accept(java.lang.String)
     */
    public boolean accept(String path) {
        if (excludedResources == null || excludedResources.length == 0) {
            return true;
        }

        boolean accept = true;
        for (String excludePattern : excludedResources) {
            if (FilenameUtils.wildcardMatch(path, excludePattern)) {
                accept = false;
                break;
            }
        }

        return accept;
    }

}
