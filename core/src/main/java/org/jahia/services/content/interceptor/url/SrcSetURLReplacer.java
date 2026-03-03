/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

    /**
     * Extracts URLs from a srcset attribute value, discarding descriptors.
     * <p>
     * This method parses the srcset attribute and returns only the image URLs,
     * dropping all descriptors (width and pixel density information).
     * </p>
     * <p>
     * Example:
     * <ul>
     *   <li>Input: {@code "image-320.jpg 320w, image-640.jpg 640w"}</li>
     *   <li>Output: {@code ["image-320.jpg", "image-640.jpg"]}</li>
     * </ul>
     * </p>
     *
     * @param srcSet the srcset attribute value
     * @return array of URLs extracted from the srcset
     * @see #parseSrcsetEntries(String)
     * @see SrcsetEntry
     * @deprecated Use {@link #parseSrcsetEntries(String)} instead,
     * which provides more precise extraction of both URLs and descriptors.
     * The descriptor information (width and pixel density) is often needed
     * to properly rebuild the srcset after URL rewriting.
     * <p>
     * Before (loses descriptor information):
     * <pre>
     *             String[] urls = getURLsFromSrcSet(srcSet);
     *             for (String url : urls) {
     *                 // descriptor information is lost
     *             }
     *             </pre>
     * After (preserves descriptor information):
     * <pre>
     *             SrcsetEntry[] entries = parseSrcsetEntries(srcSet);
     *             for (SrcsetEntry entry : entries) {
     *                 String url = entry.getUrl();
     *                 String descriptor = entry.getDescriptor();
     *                 // can now rebuild with original descriptors
     *             }
     *             </pre>
     * </p>
     */
    @Deprecated(since = "8.2.4.0", forRemoval = true)
    public static String[] getURLsFromSrcSet(String srcSet) {
        return Arrays
                .stream(srcSet.split(","))
                .map(String::trim)
                .map(entry -> StringUtils.substringBefore(entry, " "))
                .toArray(String[]::new);
    }

    /**
     * Parses a srcset attribute value and extracts srcset entries with their descriptors.
     * <p>
     * The srcset attribute contains a comma-separated list of entries. Each entry
     * consists of a URL followed by an optional descriptor. The descriptor can be:
     * <ul>
     *   <li>A width descriptor (e.g., "320w") indicating the image's width in pixels</li>
     *   <li>A pixel density descriptor (e.g., "2x") indicating the device pixel ratio</li>
     *   <li>Both descriptors combined (e.g., "320w 2x")</li>
     * </ul>
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code "image-320.jpg 320w, image-640.jpg 640w"} - Width descriptors</li>
     *   <li>{@code "image.jpg 1x, image@2x.jpg 2x"} - Pixel density descriptors</li>
     *   <li>{@code "image-320.jpg 320w 2x"} - Combined width and pixel density descriptor</li>
     *   <li>{@code "image.jpg"} - Single image without descriptor</li>
     * </ul>
     * </p>
     *
     * @param srcset the srcset attribute value
     * @return array of {@link SrcsetEntry} objects containing URLs and descriptors
     */
    public static SrcsetEntry[] parseSrcsetEntries(String srcset) {
        if (StringUtils.isEmpty(srcset)) {
            return new SrcsetEntry[0];
        }

        return Arrays.stream(srcset.split(",")).map(String::trim).map(entry -> {
            String[] parts = entry.split("\\s+", 2);
            String url = parts[0];
            String descriptor = parts.length > 1 ? parts[1] : "";
            return new SrcsetEntry(url, descriptor);
        }).toArray(SrcsetEntry[]::new);
    }

    /**
     * Represents a single entry in a srcset attribute.
     * <p>
     * Each srcset entry consists of:
     * <ul>
     *   <li><b>URL</b>: The path or URL to the image resource</li>
     *   <li><b>Descriptor</b>: An optional descriptor that can be:
     *     <ul>
     *       <li>A width descriptor (e.g., "320w")</li>
     *       <li>A pixel density descriptor (e.g., "2x")</li>
     *       <li>Both descriptors combined (e.g., "320w 2x")</li>
     *     </ul>
     *   </li>
     * </ul>
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code new SrcsetEntry("image-320.jpg", "320w")} - Entry with width descriptor</li>
     *   <li>{@code new SrcsetEntry("image@2x.jpg", "2x")} - Entry with pixel density descriptor</li>
     *   <li>{@code new SrcsetEntry("image-320.jpg", "320w 2x")} - Entry with both descriptors</li>
     *   <li>{@code new SrcsetEntry("image.jpg", "")} - Entry without descriptor</li>
     * </ul>
     * </p>
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img#srcset">MDN Web Docs - srcset attribute</a>
     */
    public static class SrcsetEntry {
        private final String url;
        private final String descriptor;

        public SrcsetEntry(String url, String descriptor) {
            this.url = url;
            this.descriptor = descriptor;
        }

        public String getUrl() {
            return url;
        }

        public String getDescriptor() {
            return descriptor;
        }
    }
}
