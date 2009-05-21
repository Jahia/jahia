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
