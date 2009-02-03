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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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

    private I18nBean i18n;
    
    private I18nBundlesBean i18nBundles;
    
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
        i18nBundles = new I18nBundlesBean(processingContext.getLocale(), siteBean.getTemplatePackage());
        String resourceBundleName = siteBean.getTemplatePackage().getResourceBundleName();
        i18n = (I18nBean) i18nBundles.get(resourceBundleName != null
                && resourceBundleName.length() > 0 ? resourceBundleName
                : "JahiaMessageResources");
    }
    
    public DateBean getDate() {
        return dateBean;
    }
    
    public I18nBean getI18n() {
        return i18n;
    }

    public I18nBundlesBean getI18nBundles() {
        return i18nBundles;
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