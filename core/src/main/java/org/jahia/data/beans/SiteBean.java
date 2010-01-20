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
package org.jahia.data.beans;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.sites.JahiaSite;

import java.util.Set;

/**
 * <p>Title: Site JavaBean compliant JahiaSite facade</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class SiteBean {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(SiteBean.class);

    private JahiaSite jahiaSite;
    private ProcessingContext processingContext;

    private JahiaTemplatesPackage templatePackage;

    public SiteBean() {
        super();
    }

    public SiteBean(JahiaSite jahiaSite, ProcessingContext processingContext) {
        this();
        this.jahiaSite = jahiaSite;
        this.processingContext = processingContext;
    }

    public String[] getActiveLanguageCodes() {
        return jahiaSite.getLanguages().toArray(new String[jahiaSite.getLanguages().size()]);
    }

    public PageBean getPage(int pageID) {
        try {
            ContentPage contentPage = ContentPage.getPage(pageID);
            JahiaPage jahiaPage = contentPage.getPage(processingContext.getEntryLoadRequest(), processingContext.getOperationMode(), processingContext.getUser());
            PageBean pageBean = new PageBean(jahiaPage, processingContext);
            return pageBean;
        } catch (JahiaException je) {
            logger.error("Error while retrieving page " + pageID + " for site + " + getId() + " : ", je);
            return null;
        }
    }

    public int getID() {
        return jahiaSite.getID();
    }

    public int getId() {
        return getID();
    }

    public JahiaBaseACL getACL() {
        return jahiaSite.getACL();
    }

    public int getAclID() {
        return jahiaSite.getAclID();
    }

    public String getDescription() {
        return jahiaSite.getDescr();
    }

    public ContentPage getHomeContentPage() {
        return jahiaSite.getHomeContentPage();
    }

    public PageBean getHomePage() {
        try {
            JahiaPage homeJahiaPage = getHomeContentPage().getPage(processingContext.
                    getEntryLoadRequest(), processingContext.getOperationMode(),
                    processingContext.getUser());
            if (homeJahiaPage != null) {
                return new PageBean(homeJahiaPage, processingContext);
            } else {
                return null;
            }
        } catch (JahiaException je) {
            logger.error("Error while retrieving site home page :", je);
            return null;
        }
    }

    public int getHomepageID() {
        return jahiaSite.getHomePageID();
    }

    public Set<String> getLanguages() {
        return jahiaSite.getLanguages();
    }

    public String getServerName() {
        return jahiaSite.getServerName();
    }

    public String getSiteKey() {
        return jahiaSite.getSiteKey();
    }

    public String getTemplatePackageName() {
        return jahiaSite.getTemplatePackageName();
    }

    public JahiaTemplatesPackage getTemplatePackage() {
        if (null == templatePackage) {
            templatePackage = ServicesRegistry.getInstance()
                    .getJahiaTemplateManagerService().getTemplatePackage(
                            getTemplatePackageName());
        }
        return templatePackage;
    }

    public String getTitle() {
        return jahiaSite.getTitle();
    }

    public String getJCRPath() throws JahiaException {
        return jahiaSite.getJCRPath();
    }
    
   public String getExternalUrl() {
       return processingContext.getSiteURL(jahiaSite, -1, false, true, false);
   }
   
}