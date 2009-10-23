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
package org.jahia.taglibs.query;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspException;

import org.apache.taglibs.standard.tag.common.fmt.ParamSupport;
import org.jahia.taglibs.utility.ParamParent;

@SuppressWarnings("serial")
public class CreateFacetFilterTag extends ContainerQueryTag implements ParamParent {

    private String facetName;
    private String facetBeanId;    
    private String facetValueBeanId;    
    private String propertyName;
    private String valueTitle;    
    private List<Object> params = new ArrayList<Object>();    
    
    public int doEndTag() throws JspException {
       
        int result = super.doEndTag();

        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        propertyName = null;
        facetName = null;
        facetBeanId = null;        
        facetValueBeanId = null;        
        valueTitle = null;
        params = new ArrayList<Object>();
        return result;
    }
    
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getValueTitle() {
        return valueTitle;
    }

    public void setValueTitle(String valueTitle) {
        this.valueTitle = valueTitle;
    }

    public String getFacetName() {
        return facetName;
    }

    public void setFacetName(String facetName) {
        this.facetName = facetName;
    }

    public String getFacetBeanId() {
        return facetBeanId;
    }

    public void setFacetBeanId(String facetBeanId) {
        this.facetBeanId = facetBeanId;
    }
    
    /**
     * Adds an argument (for parametric replacement) to this tag's message.
     *
     * @see ParamSupport
     */
    public void addParam(Object arg) {
        params.add(arg);
    }

    public void setFacetValueBeanId(String facetValueBeanId) {
        this.facetValueBeanId = facetValueBeanId;
    }

    public String getFacetValueBeanId() {
        return facetValueBeanId;
    }
}
