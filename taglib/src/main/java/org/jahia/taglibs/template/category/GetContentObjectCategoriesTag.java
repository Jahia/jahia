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

import org.jahia.content.ObjectKey;
import org.jahia.services.categories.Category;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.data.beans.CategoryBean;

import javax.servlet.jsp.PageContext;
import java.util.Set;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class GetContentObjectCategoriesTag extends AbstractJahiaTag {

    private static final transient org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(GetContentObjectCategoriesTag.class);

    private String objectKey;
    private String valueID;
    private boolean asSet;

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public void setValueID(String valueID) {
        this.valueID = valueID;
    }

    public void setAsSet(boolean asSet) {
        this.asSet = asSet;
    }

    public int doStartTag() {
        if (valueID != null && valueID.length() > 0) {
            pageContext.removeAttribute(valueID, PageContext.PAGE_SCOPE);
        }
        try {
            final ObjectKey key = ObjectKey.getInstance(objectKey);
            final Set<Category> categories = Category.getObjectCategories(key);
            if (asSet) {
                if (valueID != null && valueID.length() > 0 && categories != null) {
                    final Set<CategoryBean> categoryBeans = CategoryBean.getCategoryBeans(categories);
                    pageContext.setAttribute(valueID, categoryBeans);
                }
            } else {
                final StringBuffer objectCategories = new StringBuffer();
                for (final Category curCategory : categories) {
                    if (objectCategories.length() > 0) {
                        objectCategories.append("$$$");
                    }                    
                    objectCategories.append(curCategory.getKey());
                }
                if (valueID != null && valueID.length() > 0) {
                    if (objectCategories.length() > 0) {
                        pageContext.setAttribute(valueID, objectCategories.toString());
                    }
                }
            }

        } catch (Exception je) {
            logger.error("Error while getting object categories", je);
        }

        return SKIP_BODY;
    }

    public int doEndTag() {
        objectKey = null;
        valueID = null;
        asSet = false;
        return EVAL_PAGE;
    }
}
