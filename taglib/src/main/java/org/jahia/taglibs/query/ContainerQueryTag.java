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

import java.io.IOException;
import java.util.Properties;

import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelConstants;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.jahia.data.JahiaData;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainerSet;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.query.qom.JahiaQueryObjectModelConstants;
import org.jahia.query.qom.QueryObjectModelImpl;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContainerQueryBean;
import org.jahia.taglibs.template.containerlist.ContainerListTag;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 22 oct. 2007
 * Time: 12:47:54
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class ContainerQueryTag extends QueryDefinitionTag  {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ContainerQueryTag.class);

    private String queryObjectModelBeanName;

    private String targetContainerListName;

    private String targetContainerListID;

    private JahiaContainerList targetContainerList;

    private ContainerQueryBean queryBean;

    private String queryBeanID;

    public String getQueryBeanID() {
        return queryBeanID;
    }

    public void setQueryBeanID(String queryBeanID) {
        this.queryBeanID = queryBeanID;
    }

    public String getTargetContainerListName() {
        return targetContainerListName;
    }

    public void setTargetContainerListName(String targetContainerListName) {
        this.targetContainerListName = targetContainerListName;
    }

    public String getTargetContainerListID() {
        return targetContainerListID;
    }

    public void setTargetContainerListID(String targetContainerListID) {
        this.targetContainerListID = targetContainerListID;
    }

    public String getQueryObjectModelBeanName() {
        return queryObjectModelBeanName;
    }

    public void setQueryObjectModelBeanName(String queryObjectModelBeanName) {
        this.queryObjectModelBeanName = queryObjectModelBeanName;
    }

    public ContainerQueryBean getQueryBean (JahiaData jData) throws JahiaException {
        if (this.queryBean != null){
            return this.queryBean;
        }
        int targetCtnListID = 0;
        ProcessingContext jParams = jData.getProcessingContext();
        try {
            if ( this.targetContainerList != null ){
                targetCtnListID = this.targetContainerList.getID();
            }
            if ( this.targetContainerListID != null && !"".equals(this.targetContainerListID.trim()) ){
                targetCtnListID = Integer.parseInt(this.targetContainerListID);
            } else if (this.targetContainerListName != null && !"".equals(this.targetContainerListName.trim())) {
                targetCtnListID = ServicesRegistry.getInstance().getJahiaContainersService().getContainerListID(
                        JahiaContainerSet.resolveContainerName(targetContainerListName, jParams.getPage()
                                .getPageTemplateID(), 0, jParams), jParams.getPageID());
                if (targetCtnListID == 0) {
                    return null;
                }
            }
        } catch ( Exception t ){
            logger.debug("Error retrieving the target container list", t);
            return null;
        }

        int queryContextCtnID = targetCtnListID;
        ContainerListTag cListTag = (ContainerListTag) findAncestorWithClass(
                this, ContainerListTag.class);
        if (cListTag != null) {
            try {
                if (this.qomBuilder.getOrderings().size() == 0) {
                    JahiaFieldDefinition sortFieldDef = cListTag
                            .getSortFieldDefinition(cListTag.getSortByField(),
                                    cListTag.getSortByMetaData());
                    if (sortFieldDef != null) {
                        boolean numberValue = sortFieldDef.getType() == FieldTypes.INTEGER
                                || sortFieldDef.getType() == FieldTypes.FLOAT ? true
                                : false;
                        String order = cListTag.getSortOrder() != null
                                && cListTag.getSortOrder().trim().toLowerCase()
                                        .startsWith("desc") ? String
                                .valueOf(QueryObjectModelConstants.JCR_ORDER_DESCENDING)
                                : String
                                        .valueOf(QueryObjectModelConstants.JCR_ORDER_ASCENDING);

                        String
                                .valueOf(QueryObjectModelConstants.JCR_ORDER_DESCENDING);
                        addOrdering(sortFieldDef.getName(), numberValue, null,
                                sortFieldDef.getIsMetadata(), null, order, true, null);
                    }
                }
            } catch (RepositoryException e) {
                logger.warn("Unable to set container list sorting with field: "
                        + cListTag.getSortByField() + " "
                        + cListTag.getSortByMetaData(), e);
            }
        }
        
        QueryObjectModel queryModel = null;
        if (this.getQueryObjectModelBeanName()==null){
            queryModel = this.getQueryObjectModel();
        } else {
            queryModel = (QueryObjectModel)pageContext.getAttribute(this.getQueryObjectModelBeanName(),
                    PageContext.REQUEST_SCOPE);
            if (queryModel == null) {
                return null;
            }
        }
        QueryObjectModelImpl modelImpl = (QueryObjectModelImpl) queryModel;

        if (cListTag != null){        
            if (!modelImpl.getProperties().contains(
                    JahiaQueryObjectModelConstants.SEARCH_MAX_HITS)
                    && cListTag.getMaxSize() > 0
                    && cListTag.getMaxSize() < Integer.MAX_VALUE) {
                modelImpl.getProperties().put(
                        JahiaQueryObjectModelConstants.SEARCH_MAX_HITS,
                        String.valueOf(cListTag.getMaxSize()));

            }
        }
        queryBean = ServicesRegistry.getInstance().getJahiaContainersService()
                .createContainerQueryBean(modelImpl, queryContextCtnID,
                        new Properties(), jParams);
        return queryBean;
    }

    public int doStartTag ()  throws JspException {
        int eval = super.doStartTag();

        // check if we are inside a ContainerListTag
        ContainerListTag containerListTag = (ContainerListTag)findAncestorWithClass(this, ContainerListTag.class);
        if (containerListTag != null){
            this.targetContainerList = containerListTag.getContainerList();
            if ( this.targetContainerList != null ){
                this.targetContainerListID = String.valueOf(this.targetContainerList.getID());
            }
        }

        return eval;
    }

    // Body is evaluated one time, so just writes it on standard output
    public int doAfterBody () {
        int result = super.doAfterBody();

        if ( this.getQueryObjectModelBeanName() != null && getId() != null) {
            pageContext.removeAttribute(getId(),PageContext.REQUEST_SCOPE);
        }
        try {
            bodyContent.writeOut(bodyContent.getEnclosingWriter());
        } catch (IOException ioe) {
            logger.error("Error:", ioe);
        }
        return result;
    }

    public int doEndTag ()
        throws JspException {

        try {
            this.getQueryBean(getJData());
        } catch ( Exception t ){
            logger.warn("Exception occured when creating the ContainerQueryBean",t);
            throw new JspException(t);
        }

        if ( this.targetContainerList != null ){
            this.targetContainerList.setQueryBean(this.queryBean);
        }
        if (getQueryBeanID() != null) {
            pageContext.setAttribute(getQueryBeanID(), this.queryBean, PageContext.REQUEST_SCOPE);
        }

        int result = super.doEndTag();

        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        queryBeanID = null;
        targetContainerList = null;
        queryObjectModelBeanName = null;
        targetContainerListName = null;
        targetContainerListID = null;
        queryBean = null;
        
        return result;
    }
}
