/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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

import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.StartTagType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.sites.SitesSettings;

/**
 * Filters out unwanted HTML elements from the rich text property values before
 * saving.
 * 
 * @author Sergiy Shyrkov
 */
public class HtmlFilteringInterceptor extends RichTextInterceptor {

	private static Logger logger = LoggerFactory.getLogger(HtmlFilteringInterceptor.class);

	/**
	 * Filters out configured "unwanted" HTML tags and returns the modified
	 * content. If no modifications needs to be done, returns the original
	 * content.
	 * 
	 * @param content
	 *            the content to be modified
	 * @param filteredTags
	 *            the set of tags to be filtered out
	 * @return filtered out content or the original one if no modifications
	 *         needs to be done
	 */
	protected static String filterTags(String content, Set<String> filteredTags) {
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
					EndTag endTag = startTag.getElement().getEndTag();
					out.remove(startTag);
					if (endTag != null) {
						out.remove(endTag);
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

	private Set<String> filteredTags = Collections.emptySet();
	
	private boolean considerSiteSettingsForFiltering;

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
	    if (considerSiteSettingsForFiltering) {
	        if (node.getResolveSite().hasProperty(SitesSettings.HTML_MARKUP_FILTERING_ENABLED)) {
                tags = convertToTagSet(node.getResolveSite().hasProperty(
                        SitesSettings.HTML_MARKUP_FILTERING_TAGS) ? node.getResolveSite()
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

        String result = filterTags(content, tags);
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

    public void setFilteredTags(String tagsToFilter) {
        this.filteredTags = convertToTagSet(tagsToFilter);

        if (this.filteredTags == null || this.filteredTags.isEmpty()) {
            logger.info("No HTML tag filtering configured. Interceptor will be disabled.");
        } else {
            logger.info("HTML tag filtering configured fo tags: " + tagsToFilter);
        }
    }

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

    public void setConsiderSiteSettingsForFiltering(boolean considerSiteSettingsForFiltering) {
        this.considerSiteSettingsForFiltering = considerSiteSettingsForFiltering;
    }

}
