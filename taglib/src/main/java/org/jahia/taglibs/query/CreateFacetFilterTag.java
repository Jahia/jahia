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
package org.jahia.taglibs.query;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.taglibs.standard.tag.common.fmt.ParamSupport;
import org.jahia.data.JahiaData;
import org.jahia.data.containers.JahiaContainerSet;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContainerQueryContext;
import org.jahia.services.search.facets.FacetBean;
import org.jahia.services.search.facets.FacetValueBean;
import org.jahia.services.search.facets.JahiaFacetingService;
import org.jahia.taglibs.utility.ParamParent;
import org.jahia.utils.i18n.ResourceBundleMarker;

@SuppressWarnings("serial")
public class CreateFacetFilterTag extends ContainerQueryTag implements ParamParent {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(CreateFacetFilterTag.class);

    private String facetName;
    private String facetBeanId;    
    private String facetValueBeanId;    
    private String propertyName;
    private String valueTitle;    
    private List<Object> params = new ArrayList<Object>();    
    
    public int doEndTag() throws JspException {
        processFaceting();
        
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
    
    private void processFaceting () {
        final ServletRequest request = pageContext.getRequest();
        final JahiaData jData = (JahiaData) request
                .getAttribute("org.jahia.data.JahiaData");
        final ProcessingContext jParams = jData.getProcessingContext();
        try {
            JahiaFacetingService facetingService = ServicesRegistry.getInstance().getJahiaFacetingService();
            FacetBean facetBean = null;
            List<FacetValueBean> createdFacetValue = null;
            if (getFacetValueBeanId() != null) {
                createdFacetValue = (List<FacetValueBean>)pageContext.findAttribute(getFacetValueBeanId());
                if (createdFacetValue == null) {
                    createdFacetValue = new ArrayList<FacetValueBean>();                
                }
            }
           
            if (getPropertyName() != null) {
                ContainerQueryContext context = getQueryBean(jData).getQueryContext();
                if (context.getContainerDefinitionNames().isEmpty() && getTargetContainerListName() != null) {
                    context.setContainerDefinitionType(JahiaContainerSet.resolveNodeType(getTargetContainerListName(), jParams.getPage()
                            .getPageTemplateID(), 0, jParams));
                    context.getContainerDefinitionsIncludingType(true);
                }
                facetBean = facetingService.createFacetFilter(getFacetName(),
                    getPropertyName(), getValueTitle(), context, jParams,
                    createdFacetValue);
            } else if (getQueryBean(jData) != null && getQueryBean(jData).getFilter() != null) {
                if (getValueTitle().indexOf(" ") == -1) {
                    setValueTitle(ResourceBundleMarker.drawMarker(
                            getResourceBundle(), getValueTitle(),
                            getValueTitle()));
                }
                facetBean = facetingService.createFacetFilter(getFacetName(),
                        getValueTitle(), params.toArray(),
                        getQueryBean(jData).getFilter(), getQueryBean(jData)
                                .getQueryContext(), jParams, createdFacetValue);
            }
            if (getFacetValueBeanId() != null) {
                pageContext.setAttribute(getFacetValueBeanId(), createdFacetValue, PageContext.REQUEST_SCOPE);
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
