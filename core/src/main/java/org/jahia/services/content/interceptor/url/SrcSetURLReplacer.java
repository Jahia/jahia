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
package org.jahia.services.content.interceptor.url;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.interceptor.URLInterceptor;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

/**
 * Special URLReplacer to handle srcset HTML attributes
 * <img src="mon-image.jpg"
 *      srcset="mon-image-320.jpg 320w,
 *              mon-image-640.jpg 640w"
 *      alt="">
 */
public class SrcSetURLReplacer extends BaseURLReplacer {

    public static final String IMG = "img";
    public static final String IMG_SRCSET_ATTR = "srcset";

    /** {@inheritDoc}. */
    @Override
    public boolean canHandle(String tagName, String attrName) {
        return StringUtils.equals(IMG, tagName.toLowerCase()) && StringUtils.endsWith(attrName.toLowerCase(), IMG_SRCSET_ATTR);
    }

    /** {@inheritDoc}. */
    @Override
    public String replaceRefsByPlaceholders(String originalValue, Map<String, Long> newRefs, Map<String, Long> oldRefs, String workspace, Locale locale, JCRNodeWrapper node, ExtendedPropertyDefinition definition) throws RepositoryException {
        if (StringUtils.isNotEmpty(originalValue)) {
            String[] urls = getURLsFromSrcSet(originalValue);

            for (String url : urls) {
                originalValue = StringUtils.replace(originalValue, url, super.replaceRefsByPlaceholders(url, newRefs, oldRefs, workspace, locale, node, definition));
            }
        }
        return originalValue;
    }

    /** {@inheritDoc}. */
    @Override
    public String replacePlaceholdersByRefs(String originalValue, Map<Long, String> refs, String workspaceName, Locale locale, JCRNodeWrapper parent) throws RepositoryException {
        if (StringUtils.isNotEmpty(originalValue)) {
            String[] urls = getURLsFromSrcSet(originalValue);

            for (String url : urls) {
                String replacedUrl = super.replacePlaceholdersByRefs(url, refs, workspaceName, locale, parent);
                originalValue = StringUtils.replace(originalValue, url, "#".equals(replacedUrl) ? URLInterceptor.MISSING_IMAGE : replacedUrl);
            }
        }
        return originalValue;
    }

    public static String[] getURLsFromSrcSet(String srcSet) {
        return Arrays
                .stream(srcSet.split(","))
                .map(String::trim)
                .map(entry -> StringUtils.substringBefore(entry, " "))
                .toArray(String[]::new);
    }
}
