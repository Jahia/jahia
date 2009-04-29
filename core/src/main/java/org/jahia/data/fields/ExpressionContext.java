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
package org.jahia.data.fields;

import org.jahia.data.beans.JahiaBean;
import org.jahia.data.beans.PageBean;
import org.jahia.data.beans.RequestBean;
import org.jahia.data.beans.SiteBean;
import org.jahia.gui.GuiBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.expressions.ExpressionContextInterface;
import org.jahia.services.expressions.DateBean;
import org.jahia.services.pages.JahiaPage;
import org.jahia.exceptions.JahiaException;
import org.jahia.bin.Jahia;
import org.jahia.engines.calendar.CalendarHandler;
import org.apache.commons.jexl.JexlContext;

import java.util.Map;
import java.text.SimpleDateFormat;

/**
 * <p>Title: Expression evaluator context</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class ExpressionContext implements ExpressionContextInterface {

    private org.jahia.data.beans.PageBean pageBean;
    private org.jahia.data.beans.SiteBean siteBean;
    private org.jahia.data.beans.RequestBean requestBean;
    private org.jahia.data.beans.JahiaBean jahiaBean;
    private org.jahia.services.usermanager.JahiaUser user;
    private org.jahia.params.ProcessingContext processingContext;

    public ExpressionContext(ProcessingContext processingContext) {
        if (processingContext != null) {
            this.processingContext = processingContext;
            user = processingContext.getUser();
        }
    }

    /**
     * used by subclass for custom initialization.
     *
     * @param jc
     * @throws org.jahia.exceptions.JahiaException
     *
     */
    public void init(JexlContext jc) throws JahiaException {
        final Map<String, Object> vars = jc.getVars();
        vars.put("currentContext", this);
        vars.put("currentPage", getPageBean());
        vars.put("currentSite", getSiteBean());
        vars.put("currentJahia", getJahiaBean());
        vars.put("currentUser", getUser());
        vars.put("currentRequest", getRequestBean());
        vars.put("dateBean", new DateBean(Jahia.getThreadParamBean(),
                new SimpleDateFormat(CalendarHandler.DEFAULT_DATE_FORMAT)));
    }

    public org.jahia.data.beans.PageBean getPageBean() {
        if (processingContext != null && pageBean == null) {
            JahiaPage page = processingContext.getPage();
            if (page == null) {
                return null;
            }
            pageBean = new PageBean(page, processingContext);
        }
        return pageBean;
    }

    public void setPageBean(org.jahia.data.beans.PageBean pageBean) {
        this.pageBean = pageBean;
    }

    public org.jahia.data.beans.SiteBean getSiteBean() {
        if (processingContext != null && siteBean == null) {
            siteBean = new SiteBean(processingContext.getSite(), processingContext);
        }
        return siteBean;
    }

    public void setSiteBean(org.jahia.data.beans.SiteBean siteBean) {
        this.siteBean = siteBean;
    }

    public org.jahia.data.beans.RequestBean getRequestBean() {
        if (processingContext != null && requestBean == null) {
            requestBean = new RequestBean(new GuiBean(processingContext), processingContext);
        }
        return requestBean;
    }

    public void setRequestBean(org.jahia.data.beans.RequestBean requestBean) {
        this.requestBean = requestBean;
    }

    public org.jahia.data.beans.JahiaBean getJahiaBean() {
        if (processingContext != null && jahiaBean == null) {
            jahiaBean = new JahiaBean(processingContext);
        }
        return jahiaBean;
    }

    public void setJahiaBean(org.jahia.data.beans.JahiaBean jahiaBean) {
        this.jahiaBean = jahiaBean;
    }

    public org.jahia.services.usermanager.JahiaUser getUser() {
        return user;
    }

    public void setUser(org.jahia.services.usermanager.JahiaUser user) {
        this.user = user;
    }

    public org.jahia.params.ProcessingContext getParamBean() {
        return processingContext;
    }

    public void setParamBean(org.jahia.params.ProcessingContext processingContext) {
        this.processingContext = processingContext;
    }

}