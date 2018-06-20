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
