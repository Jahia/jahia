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
package org.jahia.taglibs.template.category;

import org.jahia.taglibs.ValueJahiaTag;
import org.jahia.services.categories.Category;
import org.jahia.params.ProcessingContext;
import org.apache.log4j.Logger;

import java.util.StringTokenizer;

/**
 *
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class DisplayCategoryTitleTag extends ValueJahiaTag {

    private static transient final Logger logger = Logger.getLogger(DisplayCategoryTitleTag.class);

    private String categoryKeys;
    public void setCategoryKeys(String categoryKeys) {
        this.categoryKeys = categoryKeys;
    }

    public int doStartTag() {
        if (categoryKeys != null && categoryKeys.length() > 0) {
            try {
                final ProcessingContext jParams = getProcessingContext();
                final StringBuilder result = new StringBuilder();
                final StringTokenizer tokenizer = new StringTokenizer(categoryKeys, "$$$");
                while (tokenizer.hasMoreTokens()) {
                    final String key = tokenizer.nextToken();
                    final Category cat = Category.getCategory(key);
                    if (result.length() > 0) result.append(", ");
                    final String title = cat.getTitle(jParams.getLocale());
                    if (title != null && title.length() > 0) {
                        result.append(title);
                    } else {
                        result.append(key);
                    }
                }

                if (getVar() != null || getValueID() != null) {
                    if (getVar() != null) {
                        pageContext.setAttribute(getVar(), result.toString());
                    }
                    if (getValueID() != null) {
                        pageContext.setAttribute(getValueID(), result.toString());
                    }
                } else {
                    pageContext.getOut().print(result.toString());
                }
            } catch (Exception e) {
                logger.error("Error in DisplayCategoryTitleTag", e);
            }
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        resetState();
        return EVAL_PAGE;
    }
    
    @Override
    protected void resetState() {
        super.resetState();
        categoryKeys = null;
    }
}
