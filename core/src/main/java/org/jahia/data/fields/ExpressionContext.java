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
import org.jahia.utils.DateUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.bin.Jahia;
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
                new SimpleDateFormat(DateUtils.DEFAULT_DATETIME_FORMAT)));
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