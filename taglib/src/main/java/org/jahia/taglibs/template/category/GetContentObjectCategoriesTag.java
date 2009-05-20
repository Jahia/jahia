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
import org.jahia.taglibs.ValueJahiaTag;
import org.jahia.data.beans.CategoryBean;

import javax.servlet.jsp.PageContext;
import java.util.Set;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class GetContentObjectCategoriesTag extends ValueJahiaTag {

    private static final transient org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(GetContentObjectCategoriesTag.class);

    private String objectKey;
    private boolean asSet;

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public void setAsSet(boolean asSet) {
        this.asSet = asSet;
    }

    public int doStartTag() {
        if (getVar() != null) {
            pageContext.removeAttribute(getVar(), PageContext.PAGE_SCOPE);
        }
        if (getValueID() != null) {
            pageContext.removeAttribute(getValueID(), PageContext.PAGE_SCOPE);
        }
        try {
            final ObjectKey key = ObjectKey.getInstance(objectKey);
            final Set<Category> categories = Category.getObjectCategories(key);
            if (asSet) {
                if (categories != null && (getVar() != null || getValueID() != null)) {
                    final Set<CategoryBean> categoryBeans = CategoryBean.getCategoryBeans(categories);
                    if (getVar() != null) {
                        pageContext.setAttribute(getVar(), categoryBeans);
                    }
                    if (getValueID() != null) {
                        pageContext.setAttribute(getValueID(), categoryBeans);
                    }
                }
            } else {
                final StringBuilder objectCategories = new StringBuilder();
                for (final Category curCategory : categories) {
                    if (objectCategories.length() > 0) {
                        objectCategories.append("$$$");
                    }                    
                    objectCategories.append(curCategory.getKey());
                }
                if (objectCategories.length() > 0) {
                    if (getVar() != null) {
                        pageContext.setAttribute(getVar(), objectCategories.toString());
                    }
                    if (getValueID() != null) {
                        pageContext.setAttribute(getValueID(), objectCategories.toString());
                }
                }
            }

        } catch (Exception je) {
            logger.error("Error while getting object categories", je);
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
        objectKey = null;
        asSet = false;
    }

}
