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

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.services.categories.Category;
import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.apache.log4j.Logger;

import javax.servlet.ServletRequest;
import java.util.StringTokenizer;

/**
 *
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class DisplayCategoryTitleTag extends AbstractJahiaTag {

    private static transient final Logger logger = Logger.getLogger(DisplayCategoryTitleTag.class);

    private String categoryKeys;
    private String valueID;

    public void setCategoryKeys(String categoryKeys) {
        this.categoryKeys = categoryKeys;
    }

    public void setValueID(String valueID) {
        this.valueID = valueID;
    }

    public int doStartTag() {
        if (categoryKeys != null && categoryKeys.length() > 0) {
            try {
                final ServletRequest request = pageContext.getRequest();
                final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
                final ProcessingContext jParams = jData.getProcessingContext();
                final StringBuffer result = new StringBuffer();
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

                if (valueID != null && valueID.length() > 0) {
                    pageContext.setAttribute(valueID, result.toString());
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
        categoryKeys = null;
        valueID = null;
        return EVAL_PAGE;
    }
}
