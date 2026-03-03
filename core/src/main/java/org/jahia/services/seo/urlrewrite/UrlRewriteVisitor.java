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
package org.jahia.services.seo.urlrewrite;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.interceptor.url.SrcSetURLReplacer;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter.ModeCondition;
import org.jahia.services.render.filter.HtmlTagAttributeTraverser.HtmlTagAttributeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A Jahia HTML Tag attribute visitor for applying URL rewriting rules (using {@link UrlRewriteService}).
 *
 * @author Sergiy Shyrkov
 */
public class UrlRewriteVisitor implements HtmlTagAttributeVisitor {
    private static Logger logger = LoggerFactory.getLogger(UrlRewriteVisitor.class);
    private static final String REQUEST_ATTRIBUTE_TAG_NAME = UrlRewriteVisitor.class.getName() + ".tagName";
    private static final String REQUEST_ATTRIBUTE_TAG_ATTRIBUTE_NAME = UrlRewriteVisitor.class.getName() + ".tagAttributeName";

    private String[] applyOnModes;

    private UrlRewriteService urlRewriteService;

    private boolean preconditionsMatch(String attrValue, RenderContext context) {
        if (StringUtils.isEmpty(attrValue)) {
            return false;
        }

        if (applyOnModes != null && applyOnModes.length > 0) {
            for (String mode : applyOnModes) {
                if (ModeCondition.matches(context, mode)) {
                    return true;
                }
            }
            return false;
        }

        return true;
    }

    public void setApplyOnModes(String[] applyOnModes) {
        this.applyOnModes = applyOnModes;
    }

    /**
     * Applies the configured URL rewriting rules to the URL value.
     *
     * @see org.jahia.services.render.filter.HtmlTagAttributeTraverser.HtmlTagAttributeVisitor#visit(String, org.jahia.services.render.RenderContext, String, String, org.jahia.services.render.Resource)
     */
    public String visit(final String attrValue, RenderContext context, String tagName, String attrName, Resource resource) {
        String value = attrValue;
        if (preconditionsMatch(attrValue, context)) {
            long timer = System.currentTimeMillis();
            try {
                String tagNameLowerCase = tagName == null ? null : tagName.toLowerCase();
                String attrNameLowerCase = attrName == null ? null : attrName.toLowerCase();
                context.getRequest().setAttribute(REQUEST_ATTRIBUTE_TAG_NAME, tagNameLowerCase);
                context.getRequest().setAttribute(REQUEST_ATTRIBUTE_TAG_ATTRIBUTE_NAME, attrNameLowerCase);
                if (StringUtils.equals(SrcSetURLReplacer.IMG, tagNameLowerCase) && StringUtils.endsWith(attrNameLowerCase,
                        SrcSetURLReplacer.IMG_SRCSET_ATTR)) {
                    // Process srcset attribute: parse entries, rewrite URLs, rebuild srcset string
                    // Example: "/files/img-320.jpg 320w, /files/img-640.jpg 640w"
                    //       => "https://cdn.com/files/img-320.jpg 320w, https://cdn.com/files/img-640.jpg 640w"
                    value =
                            // Step 1: Parse srcset into individual entries (URL + descriptor pairs)
                            // "/files/img-320.jpg 320w" => SrcsetEntry("/files/img-320.jpg", "320w")
                            Arrays.stream(SrcSetURLReplacer.parseSrcsetEntries(value))
                                    // Step 2: Rewrite each URL and preserve its descriptor
                                    // "/files/img-320.jpg" => "https://cdn.com/files/img-320.jpg"
                                    .map(entry -> rewriteSrcsetEntry(context, entry))
                                    // Step 3: Rebuild the srcset string by joining entries with comma separator
                                    // ["https://cdn.com/files/img-320.jpg 320w", "https://cdn.com/files/img-640.jpg 640w"]
                                    // => "https://cdn.com/files/img-320.jpg 320w, https://cdn.com/files/img-640.jpg 640w"
                                    .collect(Collectors.joining(", "));
                } else {
                    value = urlRewriteService.rewriteOutbound(attrValue, context.getRequest(), context.getResponse());
                }
            } catch (Exception e) {
                logger.error("Error rewriting URL value " + attrValue + " Skipped rewriting.", e);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Rewriting URL {} into {} took {} ms", attrValue, value, System.currentTimeMillis() - timer);
            }
        }
        return value;
    }

    private String rewriteSrcsetEntry(RenderContext context, SrcSetURLReplacer.SrcsetEntry entry) {
        try {
            String rewrittenUrl = urlRewriteService.rewriteOutbound(entry.getUrl(), context.getRequest(), context.getResponse());
            return StringUtils.isNotEmpty(entry.getDescriptor()) ? rewrittenUrl + " " + entry.getDescriptor() : rewrittenUrl;
        } catch (Exception e) {
            throw new RuntimeException("Error rewriting URL: " + entry.getUrl(), e);
        }
    }

    public void setUrlRewriteService(UrlRewriteService urlRewriteService) {
        this.urlRewriteService = urlRewriteService;
    }
}
