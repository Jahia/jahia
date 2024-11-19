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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.interceptor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.StartTagType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.sites.SitesSettings;

/**
 * Filters out unwanted HTML elements from the rich text property values before saving.
 *
 * @author Sergiy Shyrkov
 */
public class HtmlFilteringInterceptor extends BaseInterceptor {

    private static Logger logger = LoggerFactory.getLogger(HtmlFilteringInterceptor.class);

    private static Set<String> convertToTagSet(String tags) {
        if (StringUtils.isEmpty(tags)) {
            return null;
        }

        Set<String> tagSet = new HashSet<String>();
        for (String tag : StringUtils.split(tags, " ,")) {
            String toBeFiltered = tag.trim().toLowerCase();
            if (toBeFiltered.length() > 0) {
                tagSet.add(toBeFiltered);
            }
        }

        return tagSet;
    }

    /**
     * Filters out configured "unwanted" HTML tags and returns the modified content. If no modifications needs to be done, returns the
     * original content.
     *
     * @param content
     *            the content to be modified
     * @param filteredTags
     *            the set of tags to be filtered out
     * @param removeContentBetweenTags
     *            if set to <code>true</code> the content between the start and end tag elements will be also removed from the output;
     *            otherwise only start and end tags themselves are removed.
     * @return filtered out content or the original one if no modifications needs to be done
     */
    protected static String filterTags(String content, Set<String> filteredTags, boolean removeContentBetweenTags) {
        if (filteredTags.isEmpty()) {
            return content;
        }

        long timer = System.currentTimeMillis();
        boolean modified = false;

        Source src = new Source(content);
        OutputDocument out = new OutputDocument(src);
        for (String filteredTagName : filteredTags) {
            for (StartTag startTag : src.getAllStartTags(filteredTagName)) {
                if (startTag.getTagType() == StartTagType.NORMAL) {
                    Element element = startTag.getElement();
                    EndTag endTag = element.getEndTag();
                    if (removeContentBetweenTags && endTag != null) {
                        out.remove(element);
                    } else {
                        out.remove(startTag);
                        if (endTag != null) {
                            out.remove(endTag);
                        }
                    }
                    modified = true;
                }
            }
        }
        String result = modified ? out.toString() : content;

        if (logger.isDebugEnabled()) {
            logger.debug("Filter HTML tags took " + (System.currentTimeMillis() - timer) + " ms");
        }

        return result;
    }
    private boolean considerSiteSettingsForFiltering;

    private Set<String> filteredTags = Collections.emptySet();

    private boolean removeContentBetweenTags;

    @Override
    public Value beforeSetValue(JCRNodeWrapper node, String name,
            ExtendedPropertyDefinition definition, Value originalValue)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {

        String content = originalValue.getString();
        if (StringUtils.isEmpty(content) || !content.contains("<")) {
            if (logger.isDebugEnabled()) {
                logger.debug("The value does not contain any HTML tags. Skip filtering.");
            }
            return originalValue;
        }

        Set<String> tags = filteredTags;
        boolean doFiltering = false;
        if (considerSiteSettingsForFiltering && node.getResolveSite().isHtmlMarkupFilteringEnabled()) {
            JCRSiteNode resolveSite = node.getResolveSite();
            if (resolveSite != null && resolveSite.hasProperty(SitesSettings.HTML_MARKUP_FILTERING_ENABLED)) {
                tags = convertToTagSet(resolveSite.hasProperty(
                        SitesSettings.HTML_MARKUP_FILTERING_TAGS) ? resolveSite
                        .getProperty(SitesSettings.HTML_MARKUP_FILTERING_TAGS).getString() : null);
                if (tags != null && !tags.isEmpty()) {
                    doFiltering = true;
                }
            }
        } else if (filteredTags != null && !filteredTags.isEmpty()) {
            doFiltering = true;
        }

        if (!doFiltering) {
            return originalValue;
        }

        Value modifiedValue = originalValue;

        if (logger.isDebugEnabled()) {
            logger.debug("Performing HTML tag filtering for " + node.getPath() + "/" + name);
            if (logger.isTraceEnabled()) {
                logger.trace("Original value: " + content);
            }
        }

        String result = filterTags(content, tags, removeContentBetweenTags);
        if (result != content && !result.equals(content)) {
            modifiedValue = node.getSession().getValueFactory().createValue(result);
            if (logger.isDebugEnabled()) {
                logger.debug("Done filtering of \"unwanted\" HTML tags.");
                if (logger.isTraceEnabled()) {
                    logger.trace("Modified value: " + result);
                }
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("The value does not contain HTML tags that needs to be removed. The content remains unchanged.");
            }
        }

        return modifiedValue;
    }

    @Override
    public Value[] beforeSetValues(JCRNodeWrapper node, String name,
            ExtendedPropertyDefinition definition, Value[] originalValues)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        Value[] res = new Value[originalValues.length];

        for (int i = 0; i < originalValues.length; i++) {
            Value originalValue = originalValues[i];
            res[i] = beforeSetValue(node, name, definition, originalValue);
        }
        return res;
    }

    public void setConsiderSiteSettingsForFiltering(boolean considerSiteSettingsForFiltering) {
        this.considerSiteSettingsForFiltering = considerSiteSettingsForFiltering;
    }

    public void setFilteredTags(String tagsToFilter) {
        this.filteredTags = convertToTagSet(tagsToFilter);

        if (this.filteredTags == null || this.filteredTags.isEmpty()) {
            logger.info("No HTML tag filtering configured. Interceptor will be disabled.");
        } else {
            logger.info("HTML tag filtering configured fo tags: " + tagsToFilter);
        }
    }

    /**
     * if set to <code>true</code> the content between the start and end tag elements will be also removed from the output; otherwise only
     * start and end tags themselves are removed.
     *
     * @param removeContentBetweenTags
     *            do we remove the content between tag elements?
     */
    public void setRemoveContentBetweenTags(boolean removeContentBetweenTags) {
        this.removeContentBetweenTags = removeContentBetweenTags;
    }

}
