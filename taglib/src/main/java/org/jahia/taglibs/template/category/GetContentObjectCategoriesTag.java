/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
