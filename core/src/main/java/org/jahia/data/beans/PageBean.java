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

import org.jahia.content.ContentObject;
import org.jahia.content.JahiaObject;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaException;
import org.jahia.gui.GuiBean;
import org.jahia.gui.HTMLToolBox;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.JahiaFieldService;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockService;
import org.jahia.services.metadata.CoreMetadataConstant;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.pages.PageProperty;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.utils.InsertionSortedMap;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * <p>Title: Page bean used in views</p>
 * <p>Description: This bean is a facade object that is designed to be used
 * with different view systems such as JSP (or other technologies) templates.
 * It is a little redundant with the JahiaPage object, but the accessors here
 * are fully JavaBean compliant and this way we can keep the "real" objects
 * for compatibility reasons.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber, Xavier Lawrence
 * @version $Id$
 */

public class PageBean extends ContentBean {

    private static final transient Logger logger = Logger.getLogger(PageBean.class);

    public static final String TYPE = "ContentPage";

    private PageBean parent;
    private Map<String, ContainerListBean> containerLists;
    private Map<String, ActionURIBean> actionURIs;
    private boolean completelyLocked = false;
    private boolean independantWorkflowInitialized = false;
    private boolean independantWorkflow = false;
    private boolean groupWorkflowStateInitialized = false;
    private int groupWorkflowState = 0;

    private final ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
    private final WorkflowService workflowService = servicesRegistry.getWorkflowService();
    private final JahiaContainersService jahiaContainersService = servicesRegistry.getJahiaContainersService();
    private final ImportExportService importExportService = servicesRegistry.getImportExportService();
    private final LockService lockRegistry = servicesRegistry.getLockService();

    private JahiaPage jahiaPage;

    static {
        registerType(ContentPage.class.getName(), PageBean.class.getName());
    }

    public PageBean() {
    }

    public PageBean(final JahiaPage jahiaPage, final ProcessingContext processingContext) {
        this.jahiaPage = jahiaPage;
        this.processingContext = processingContext;
    }

    public static AbstractJahiaObjectBean getChildInstance(
            final JahiaObject jahiaObject,
            final ProcessingContext processingContext) {
        final ContentPage contentPage = (ContentPage) jahiaObject;
        try {
            return new PageBean(contentPage.getPage(processingContext.
                    getEntryLoadRequest(), processingContext.getOperationMode(),
                    processingContext.getUser()), processingContext);
        } catch (JahiaException je) {
            logger.error(
                    "Error while converting content container to jahia container",
                    je);
            return null;
        }
    }

    public String getBeanType() {
        return TYPE;
    }

    public PageBean getParentPage() {
        if (parent != null) {
            // parent has already been resolved previously
            return parent;
        }
        final int parentID = jahiaPage.getParentID();
        try {
            final ContentPage parentContentPage = ContentPage.getPage(parentID);
            if (parentContentPage == null) {
                return null;
            }
            final JahiaPage parentJahiaPage = parentContentPage.getPage(processingContext.
                    getEntryLoadRequest(), processingContext.getOperationMode(),
                    processingContext.getUser());
            if (parentJahiaPage == null) {
                return null;
            }
            parent = new PageBean(parentJahiaPage, processingContext);
            return parent;
        } catch (JahiaException je) {
            logger.error("Error while trying to retrieve parent page " +
                    parentID + " for page " + getID() + " : ", je);
            return null;
        }
    }

    public int getID() {
        return jahiaPage.getID();
    }

    public int getPageID() {
        return jahiaPage.getID();
    }

    public String getTitle() {
        return jahiaPage.getTitle();
    }

    public String getHighLightDiffTitle() {
        return jahiaPage.getHighLightDiffTitle(this.processingContext);
    }

    public int getDefinitionID() {
        return jahiaPage.getPageTemplateID();
    }

    public Map<String, ContainerListBean> getContainerLists() {
        /** todo FIXME how to integrate filters and searching with this ?? */
        if (getPageType() != JahiaPage.TYPE_DIRECT) {
            return null;
        }
        if (containerLists != null) {
            return containerLists;
        }
        try {
            containerLists = new HashMap<String, ContainerListBean>();
            final Set<Integer> containerListIDs = jahiaContainersService.
                    getAllPageTopLevelContainerListIDs(
                            getID(), processingContext.getEntryLoadRequest());
            final Iterator<Integer> containerListIDIter = containerListIDs.iterator();
            while (containerListIDIter.hasNext()) {
                final Integer curContainerListID = (Integer) containerListIDIter.next();
                final JahiaContainerList curContainerList = jahiaContainersService.
                        loadContainerList(curContainerListID.intValue(),
                                LoadFlags.ALL, processingContext,
                                processingContext.getEntryLoadRequest(), null, null, null);
                final ContainerListBean containerListBean = new ContainerListBean(
                        curContainerList, processingContext);
                containerLists.put(curContainerList.getDefinition().getName(),
                        containerListBean);
            }
        } catch (JahiaException je) {
            logger.error("Error loading page top level container lists : ", je);
            return null;
        } catch (Exception t) {
            logger.error("Error loading page top level container lists : ", t);
            return null;
        }
        return containerLists;
    }

    public int getAclID() {
        return jahiaPage.getAclID();
    }

    public JahiaBaseACL getACL() {
        return jahiaPage.getACL();
    }

    public int getCounter() {
        return jahiaPage.getCounter();
    }

    public String getCreator() {
        String creator = "";
        try {
            creator = jahiaPage.getContentPage().getMetadataValue(
                    CoreMetadataConstant.CREATOR, processingContext, "");
        } catch (JahiaException e) {
            // do nothing
        }
        return creator;
    }

    public String getDateOfCreation() {
        return jahiaPage.getDoc();
    }

    public int getSiteID() {
        return jahiaPage.getJahiaID();
    }

    public int getTemplateID() {
        return jahiaPage.getPageTemplateID();
    }

    public JahiaPage getJahiaPage() {
        return jahiaPage;
    }

    public int getPageType() {
        return jahiaPage.getPageType();
    }

    /**
     * @deprecated Use getPageType() instead
     */
    public int getType() {
        return jahiaPage.getPageType();
    }

    public String getUrl() {
        try {
            return jahiaPage.getURL(processingContext);
        } catch (JahiaException je) {
            logger.error("Error while generating URL for page " + getID() + ":",
                    je);
            return null;
        }
    }

    public Map<String, Integer> getLanguageStates() {
        return jahiaPage.getLanguagesStates(false);
    }

    public Map<String, Integer> getLanguageStatesWithContent() {
        return jahiaPage.getLanguagesStates(true);
    }

    public List<PageBean> getPath() {
        try {
            final Iterator<ContentPage> pathEnum = jahiaPage.getContentPagePath(processingContext.
                    getOperationMode(), processingContext.getUser());
            final List<PageBean> pathList = new ArrayList<PageBean>();
            while (pathEnum.hasNext()) {
                final ContentPage curJahiaPage = (ContentPage) pathEnum.next();
                final PageBean curPageBean = new PageBean(curJahiaPage.getPage(processingContext), processingContext);
                pathList.add(curPageBean);
            }
            return pathList;
        } catch (JahiaException je) {
            logger.error("Error while retrieving page path for page " + getID() +
                    ":", je);
            return null;
        }
    }

    public List<PageBean> getPathWithLevels(final int levels) {
        try {
            final Iterator<ContentPage> pathEnum = jahiaPage.getContentPagePath(levels, processingContext.
                    getOperationMode(), processingContext.getUser());
            final List<PageBean> pathList = new ArrayList<PageBean>();
            while (pathEnum.hasNext()) {
                final ContentPage curJahiaPage = (ContentPage) pathEnum.next();
                final PageBean curPageBean = new PageBean(curJahiaPage.getPage(processingContext), processingContext);
                pathList.add(curPageBean);
            }
            return pathList;
        } catch (JahiaException je) {
            logger.error("Error while retrieving page path for page " + getID() +
                    ":", je);
            return null;
        }
    }

    public int getLinkID() {
        return jahiaPage.getPageLinkID();
    }

    public JahiaPageDefinition getTemplate() {
        return jahiaPage.getPageTemplate();
    }

    public int getParentID() {
        return jahiaPage.getParentID();
    }

    public String getProperty(final String propertyName) {
        try {
            return jahiaPage.getProperty(propertyName);
        } catch (JahiaException je) {
            logger.error("Error while retrieving property " + propertyName +
                    " for page " + getID() + ":", je);
            return null;
        }
    }

    public String getRemoteURL() {
        return jahiaPage.getRemoteURL();
    }

    public boolean isInCurrentPagePath() {
        try {
            final Iterator<ContentPage> thePath = processingContext.getPage().getContentPagePath(processingContext.
                    getOperationMode(), processingContext.getUser());
            while (thePath.hasNext()) {
                final ContentPage curContentPage = (ContentPage) thePath.next();
                boolean foundTarget = (curContentPage.getID() == processingContext.getPage().getID());
                if (curContentPage.getID() == getID()) {
                    return true;
                }
                if (foundTarget) {
                    break;
                }
            }
        } catch (JahiaException je) {
            logger.error("Error while loading current page path " + processingContext.getPageID() + ":", je);
            return false;
        }
        return false;
    }

    public boolean isCurrentPage() {
        return getID() == processingContext.getPageID();
    }

    public boolean isHomePage() {
        return processingContext.getSite().getHomePageID() == getID();
    }

    public int getLevel() {
        return getPath().size();
    }

    public Map<String, ActionURIBean> getActionURIBeans() {
        if (actionURIs == null) {
            buildActionURIs();
        }
        return actionURIs;
    }

    public boolean isCompletelyLocked() {
        if (actionURIs == null) {
            buildActionURIs();
        }
        return completelyLocked;
    }

    public boolean isPartiallyLocked() {
        if (actionURIs == null) {
            buildActionURIs();
        }
        if (!completelyLocked) {
            boolean partiallyLocked = false;
            for (final Map.Entry<String, ActionURIBean> curActionURIEntry : actionURIs.entrySet()) {
                final ActionURIBean curActionURIBean = curActionURIEntry.getValue();
                if (curActionURIBean.isLocked()) {
                    partiallyLocked = true;
                }
            }
            return partiallyLocked;
        } else {
            return false;
        }
    }

    public boolean isIndependantWorkflow() {
        if (!independantWorkflowInitialized ) {
            try {
                final ContentPage thePage = jahiaPage.getContentPage();
                final int mode = workflowService.getWorkflowMode(thePage);
                independantWorkflow = (mode != WorkflowService.LINKED);
                independantWorkflowInitialized = true;
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return independantWorkflow;
    }

    public int getGroupWorkflowState() {
        if (!groupWorkflowStateInitialized) {
            try {
                final ContentPage thePage = jahiaPage.getContentPage();
                final Map<String, Integer> languagesStates = workflowService.getLanguagesStates(thePage);
                Integer languageState = languagesStates.get(
                        processingContext.getLocale().toString());
                final Integer sharedLanguageState = languagesStates.
                        get(ContentObject.SHARED_LANGUAGE);
                if (languageState != null && languageState.intValue() != -1) {
                    if (sharedLanguageState != null &&
                            languageState.intValue() < sharedLanguageState.intValue()) {
                        languageState = sharedLanguageState;
                    }
                } else if (languageState == null) {
                    languageState = sharedLanguageState;
                }
                groupWorkflowState = (languageState != null) ? languageState.intValue() : 1;
                groupWorkflowStateInitialized = true;
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return groupWorkflowState;
    }

    public boolean isActionURIsEmpty() {
        if (actionURIs == null) {
            buildActionURIs();
        }
        return actionURIs.isEmpty();
    }

    public ContentBean getParent() {
        // we have one case to handle here. If the page is the site home page,
        // it has no parent field.
        if (isHomePage()) {
            return null;
        }

        try {
            final ContentPage contentPage = ContentPage.getPage(getID());
            final ContentField parentContentField = (ContentField) contentPage.
                    getParent(processingContext.getUser(),
                            processingContext.getEntryLoadRequest(), processingContext.getOperationMode());
            if (parentContentField == null) {
                return null;
            }
            final JahiaField parentJahiaField = parentContentField.getJahiaField(
                    processingContext.getEntryLoadRequest());
            if (parentJahiaField == null) {
                return null;
            }
            return new FieldBean(parentJahiaField, processingContext);
        } catch (JahiaException je) {
            logger.error("Error while loading parent field for page " + getID(), je);
        }
        return null;

    }

    private void buildActionURIs() {
        actionURIs = new InsertionSortedMap<String, ActionURIBean>();
        final GuiBean guiBean = new GuiBean(processingContext);
        final HTMLToolBox htmlToolBox = new HTMLToolBox(guiBean, processingContext);
        completelyLocked = true;
        try {
            final String curURL = guiBean.drawPagePropertiesUrl();
            final String curLauncherURI = htmlToolBox.drawPagePropertiesLauncher();
            final ActionURIBean curActionURIBean = new ActionURIBean("update", curURL, curLauncherURI);
            final LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_PAGE_TYPE, processingContext.getPageID());
            final JahiaUser user = processingContext.getUser();
            if (!lockRegistry.isAcquireable(lockKey, user, user.getUserKey())) {
                curActionURIBean.setLocked(true);
            } else {
                completelyLocked = false;
            }
            if (!lockRegistry.canRelease(lockKey, user, user.getUserKey())) {
                curActionURIBean.setReleaseable(true);
            }
            if ((curActionURIBean.getUri() != null) && (!"".equals(curActionURIBean.getUri()))) {
                actionURIs.put(curActionURIBean.getName(), curActionURIBean);
            }

        } catch (JahiaException je) {
            logger.error("Error while retrieving action URI map for page " + getID(), je);
        }
    }

    public boolean isPicker() {
        if (jahiaPage == null) {
            return false;
        }
        try {
            return importExportService.isPicker(jahiaPage.getContentPage());
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public ContentObject getContentObject() {
        return jahiaPage.getContentPage();
    }

    public String getJCRPath() throws JahiaException {
        return this.jahiaPage.getContentPage().getJCRPath(this.processingContext);
    }

    public String getUrlKey() throws JahiaException {
        return jahiaPage.getURLKey();
    }
}
