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

package org.jahia.taglibs.query;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.jahia.data.JahiaData;
import org.jahia.data.containers.JahiaContainerSet;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContainerQueryContext;
import org.jahia.services.search.facets.FacetBean;
import org.jahia.services.search.facets.JahiaFacetingService;

@SuppressWarnings("serial")
public class CreateFacetFilterTag extends ContainerQueryTag {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(CreateFacetFilterTag.class);

    private String facetName;
    private String facetBeanId;    
    private String propertyName;
    private String valueTitle;    
    
    public int doEndTag() throws JspException {
        processFaceting();
        
        int result = super.doEndTag();

        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        propertyName = null;
        facetName = null;
        facetBeanId = null;        
        valueTitle = null;
        return result;
    }
    
    private void processFaceting () {
        final ServletRequest request = pageContext.getRequest();
        final JahiaData jData = (JahiaData) request
                .getAttribute("org.jahia.data.JahiaData");
        final ProcessingContext jParams = jData.getProcessingContext();
        try {
            JahiaFacetingService facetingService = ServicesRegistry.getInstance().getJahiaFacetingService();
            FacetBean facetBean = null;
            if (getPropertyName() != null) {
                ContainerQueryContext context = getQueryBean(jData).getQueryContext();
                if (context.getContainerDefinitionNames().isEmpty() && getTargetContainerListName() != null) {
                    context.setContainerDefinitionType(JahiaContainerSet.resolveNodeType(getTargetContainerListName(), jParams.getPage()
                            .getPageTemplateID(), 0, jParams));
                    context.getContainerDefinitionsIncludingType(true);
                }
                facetBean = facetingService.createFacetFilter(getFacetName(), getPropertyName(), context, jParams);
            } else if (getQueryBean(jData) != null && getQueryBean(jData).getFilter() != null) {
                facetBean = facetingService.createFacetFilter(getFacetName(), getValueTitle(), getQueryBean(jData).getFilter(), getQueryBean(jData)
                        .getQueryContext(), jParams);
            }
            if (getFacetBeanId() != null) {
                pageContext.setAttribute(getFacetBeanId(), facetBean, PageContext.REQUEST_SCOPE);
            }
            if (getBodyContent() != null) {
                bodyContent.writeOut(bodyContent.getEnclosingWriter());
            }
        } catch (Exception e) {
            logger.error("Error", e);
        }

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
}
