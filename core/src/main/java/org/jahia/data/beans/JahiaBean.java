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
 package org.jahia.data.beans;

import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.gui.GuiBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.expressions.DateBean;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Main bean for holding Jahia related data, exposed in the request scope.
 *
 * @author Serge Huber
 * @version 1.0
 */

public class JahiaBean {

    private static final transient Logger logger = Logger.getLogger(JahiaBean.class);

    private DateBean dateBean;

    private IncludesBean includesBean;
    
    private PageBean pageBean;
    
    private ProcessingContext processingContext;
    
    private RequestBean requestBean;
    
    private SiteBean siteBean;
    
    private JahiaUser user;

    public JahiaBean(final ProcessingContext ctx) {
        this(ctx, new SiteBean(ctx.getSite(), ctx), ctx.getPage() != null ? new PageBean(ctx.getPage(), ctx) : null, new RequestBean(new GuiBean(ctx), ctx), new DateBean(), ctx.getUser());
    }
    
    public JahiaBean(final ProcessingContext processingContext, SiteBean siteBean, PageBean pageBean, RequestBean requestBean, DateBean dateBean, JahiaUser user) {
        this.processingContext = processingContext;
        this.siteBean = siteBean;
        this.pageBean = pageBean;
        this.requestBean = requestBean;
        this.dateBean=dateBean;
        this.user = user;
        includesBean = new IncludesBean(processingContext);
    }
    
    public DateBean getDate() {
        return dateBean;
    }
    
    public IncludesBean getIncludes() {
        return includesBean;
    }

    public PageBean getPage() {
        return pageBean;
    }

    public ProcessingContext getProcessingContext() {
        return processingContext;
    }

    public RequestBean getRequestInfo() {
        return requestBean;
    }

    public SiteBean getSite() {
        return siteBean;
    }

    public SiteBean getSite(final String name) {
        try {
            final JahiaSite jahiaSite = ServicesRegistry.getInstance().getJahiaSitesService().getSite(name);
            return new SiteBean(jahiaSite, processingContext);
        } catch (JahiaException je) {
            logger.error("Cannot find site " + name + ":", je);
            return null;
        }
    }

    public JahiaUser getUser() {
        return user;
    }
}