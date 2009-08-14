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

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;

import org.jahia.data.JahiaData;
import org.jahia.data.beans.CategoryBean;
import org.jahia.services.categories.Category;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.taglibs.AbstractJahiaTag;

/**
 * <p>Title: Tag that allows to look up categories by property names and
 * values.</p>
 * <p>Description: It is allowed to use "%" characters to do SQL pattern matching
 * in the property name and value. The result is stored in the page context
 * attribute named using the ID parameter and consists of an List of
 * CategoryBean objects.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 *
 *
 * @jsp:tag name="categoriesByProperties" body-content="empty"
 * description="Tag that allows to look up categories by property names and
 * values.
 *
 * <p><attriInfo>It supports the \"%\" characters to do SQL pattern matching
 * in the property name and value. The result is stored in the page context
 * attribute named using the ID parameter and consists of an List of
 * CategoryBean objects.
 *
 * <p>Both the property name and the property value must be set to carry out the search.
 *
 * <p><b>Example :</b>
 *
 * </i> [To Be Completed] </i>
 *
 *
 * </attriInfo>"
 */

@SuppressWarnings("serial")
public class CategoriesByPropertiesTag extends AbstractJahiaTag {

    private String propertyName = null;
    private String propertyValue = null;
    private String propertyNameRef = null;
    private String propertyValueRef = null;

    /**
     * @jsp:attribute name="id" required="false" rtexprvalue="true"
     * description="the name of the pageContext attribute under which to
     * store a corresponding CategoryBeans objects
     * <p><attriInfo>
     * </attriInfo>"
     */

    /**
     * @param propertyName The property name to search for. May include "%"
     * characters for pattern matching
     *
     * @jsp:attribute name="propertyName" required="false" rtexprvalue="true"
     * description="The property name to search for.
     * <p><attriInfo>May include \"%\" characters for pattern matching.
     * </attriInfo>"
     */
    public void setPropertyName (String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * @param propertyValue The property value to search for. May include "%"
     * characters for pattern matching
     *
     * @jsp:attribute name="propertyValue" required="false" rtexprvalue="true"
     * description="The property value to search for.
     * <p><attriInfo>May include \"%\" characters for pattern matching.
     * </attriInfo>"
     */
    public void setPropertyValue (String propertyValue) {
        this.propertyValue = propertyValue;
    }

    /**
     * @param propertyNameRef the Bean name for a String object containing
     * the property name to search for
     *
     * @jsp:attribute name="propertyNameRef" required="false" rtexprvalue="true"
     * description="the Bean name for a String object containing the property name to search for.
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setPropertyNameRef (String propertyNameRef) {
        this.propertyNameRef = propertyNameRef;
    }

    /**
     * @param propertyValueRef the Bean name for a String object containing
     * the property value to search for
     *
     * description="the Bean name for a String object containing the property value to search for.
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setPropertyValueRef (String propertyValueRef) {
        this.propertyValueRef = propertyValueRef;
    }

    public int doStartTag ()
        throws JspException {

        ServletRequest request = pageContext.getRequest();
        JahiaData jData = (JahiaData) request.getAttribute(
            "org.jahia.data.JahiaData");
        JahiaUser p = null;
        if (jData != null) {
            p = jData.getProcessingContext().getUser();
        }

        List<Category> foundCategories = new ArrayList<Category>();
        List<CategoryBean> foundCategoryBeans = new ArrayList<CategoryBean>();

        if (propertyNameRef != null) {
            propertyName = (String) pageContext.findAttribute(propertyNameRef);
        }
        if (propertyValueRef != null) {
            propertyValue = (String) pageContext.findAttribute(propertyValueRef);
        }

        if ( (propertyName == null) || (propertyValue == null)) {
            throw new JspException(
                "Error: either property name or property value is null, aborting");
        }

        foundCategories = Category.findCategoriesByPropNameAndValue(
            propertyName, propertyValue, p);

        for (Category curCategory : foundCategories) {
            CategoryBean curCategoryBean = new CategoryBean(curCategory,
                jData.getProcessingContext());
            foundCategoryBeans.add(curCategoryBean);
        }

        if (getId() != null) {
            pageContext.setAttribute(getId(),
                                     foundCategoryBeans);
        }
        return EVAL_BODY_BUFFERED;
    }

    // loops through the next elements
    public int doAfterBody ()
        throws JspException {
        // gets the current container list
        return SKIP_BODY;
    }

    public int doEndTag ()
        throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        super.doEndTag();
        propertyName = null;
        propertyValue = null;
        propertyNameRef = null;
        propertyValueRef = null;
        return EVAL_PAGE;
    }

}
