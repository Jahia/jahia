/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
