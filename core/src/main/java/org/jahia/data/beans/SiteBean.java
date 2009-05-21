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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.fields.LoadFlags;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SiteLanguageMapping;
import org.jahia.services.sites.SiteLanguageSettings;

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
    }

    public SiteBean(JahiaSite jahiaSite, ProcessingContext processingContext) {
        this.jahiaSite = jahiaSite;
        this.processingContext = processingContext;
    }

    public JahiaSite getJahiaSite() {
        return jahiaSite;
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

    public PageBean getPageByStringID(String pageIDStr) {
        return getPage(Integer.parseInt(pageIDStr));
    }

    /**
     * Optimized method to retrieve all the container lists by a name in a
     * Jahia site.
     * WARNING : This method does *NOT* give access to sub container lists
     * if they exist. Use the getAllContainerLists method if you need access
     * to sub container lists.
     *
     * @param name String
     * @return List
     * @todo FIXME complete object model loading to include sub container list
     * access
     */
    public List<ContainerListBean> getLightContainerLists(String name) {
        List<ContainerListBean> containerLists = new ArrayList<ContainerListBean>();
        try {
            Set<Integer> containerListIDs = ServicesRegistry.getInstance().
                    getJahiaContainersService().
                    getSiteTopLevelContainerListsIDsByName(
                            getId(), name, processingContext.getEntryLoadRequest());
            Iterator<Integer> containerListIDIter = containerListIDs.iterator();
            while (containerListIDIter.hasNext()) {
                Integer curContainerListID = (Integer) containerListIDIter.next();
                JahiaContainerList curContainerList = ServicesRegistry
                        .getInstance()
                        .getJahiaContainersService()
                        .loadContainerList(
                                curContainerListID.intValue(),
                                LoadFlags.ALL,
                                processingContext,
                                processingContext.getEntryLoadRequest(),
                                null,
                                null, null);

                ContainerListBean containerListBean = new ContainerListBean(curContainerList, processingContext);
                containerLists.add(containerListBean);
            }
            return containerLists;
        } catch (JahiaException je) {
            logger.error("Error while retrieving site top level container lists with name " + name + ":", je);
            return null;
        }
    }

    public List<ContainerListBean> getAllContainerLists(String name) {
        List<ContainerListBean> containerLists = new ArrayList<ContainerListBean>();
        try {
            Set<Integer> containerListIDs = ServicesRegistry.getInstance().
                    getJahiaContainersService().
                    getSiteTopLevelContainerListsIDsByName(
                            getId(), name, processingContext.getEntryLoadRequest());
            Iterator<Integer> containerListIDIter = containerListIDs.iterator();
            while (containerListIDIter.hasNext()) {
                Integer curContainerListID = (Integer) containerListIDIter.next();
                JahiaContainerList curContainerList = ServicesRegistry.getInstance().getJahiaContainersService()
                        .loadContainerList(curContainerListID.intValue(),
                                LoadFlags.ALL, processingContext, processingContext.getEntryLoadRequest(), null, null, null);
                ContainerListBean containerListBean = new ContainerListBean(curContainerList, processingContext);
                containerLists.add(containerListBean);
            }
            return containerLists;
        } catch (JahiaException je) {
            logger.error("Error while retrieving site top level container lists with name " + name + ":", je);
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

    public int getDefaultTemplateID() {
        return jahiaSite.getDefaultTemplateID();
    }

    public String getDescription() {
        return jahiaSite.getDescr();
    }

    public int getGroupDefaultHomepageDef() {
        return jahiaSite.getGroupDefaultHomepageDef();
    }

    public int getGroupDefaultHomepageDefActiveState() {
        return jahiaSite.getGroupDefaultHomepageDefActiveState();
    }

    public int getGroupDefaultHomepageDefAtCreationOnly() {
        return jahiaSite.getGroupDefaultHomepageDefAtCreationOnly();
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

    public List<SiteLanguageMapping> getLanguageMappings() {
        try {
            return jahiaSite.getLanguageMappings();
        } catch (JahiaException je) {
            logger.error("Error while retrieving language mappings for site " + getId() + ":", je);
            return null;
        }
    }

    public List<SiteLanguageSettings> getLanguageSettings() {
        try {
            return jahiaSite.getLanguageSettings();
        } catch (JahiaException je) {
            logger.error("Error while retrieving language settings for site " + getId() + ":", je);
            return null;
        }
    }

    public String[] getActiveLanguageCodes() {
        List<String> codes = new LinkedList<String>();
        try {
            for (SiteLanguageSettings lang : jahiaSite
                    .getLanguageSettings(true)) {
                codes.add(lang.getCode());
            }
        } catch (JahiaException je) {
            logger.error("Error while retrieving language settings for site "
                    + getId(), je);
            return null;
        }
        String[] languages = new String[codes.size()];
        return codes.toArray(languages);
    }

    public String getServerName() {
        return jahiaSite.getServerName();
    }

    public String getSiteKey() {
        return jahiaSite.getSiteKey();
    }

    public String getSiteName() {
        return jahiaSite.getTitle();
    }

    public String getTemplateFolder() {
        return jahiaSite.getTemplateFolder();
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

    public boolean isTemplatesAutoDeployMode() {
        return jahiaSite.getTemplatesAutoDeployMode();
    }

    public String getTitle() {
        return jahiaSite.getTitle();
    }

    public int getUserDefaultHomepageDef() {
        return jahiaSite.getUserDefaultHomepageDef();
    }

    public int getUserDefaultHomepageDefActiveState() {
        return jahiaSite.getUserDefaultHomepageDefActiveState();
    }

    public int getUserDefaultHomepageDefAtCreationOnly() {
        return jahiaSite.getUserDefaultHomepageDefAtCreationOnly();
    }

    public boolean isWebAppsAutoDeployMode() {
        return jahiaSite.getWebAppsAutoDeployMode();
    }

    public boolean isActive() {
        return jahiaSite.isActive();
    }

    public boolean isMixLanguagesActive() {
        return jahiaSite.isMixLanguagesActive();
    }

    public boolean isStagingEnabled() {
        return jahiaSite.isStagingEnabled();
    }

    public boolean isVersioningEnabled() {
        return jahiaSite.isVersioningEnabled();
    }

    public String getJCRPath() throws JahiaException {
        return jahiaSite.getJCRPath(this.processingContext);
    }
    
   public String getExternalUrl() {
       return processingContext.getSiteURL(jahiaSite, -1, false, true, false);
   }
   
}