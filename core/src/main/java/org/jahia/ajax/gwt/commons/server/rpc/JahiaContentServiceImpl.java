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
package org.jahia.ajax.gwt.commons.server.rpc;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.service.JahiaContentService;
import org.jahia.ajax.gwt.commons.server.AbstractJahiaGWTServiceImpl;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.*;
import org.jahia.ajax.gwt.utils.JahiaObjectCreator;
import org.jahia.ajax.gwt.utils.JahiaGWTUtils;
import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.data.search.JahiaSearchHit;
import org.jahia.data.beans.ContainerBean;
import org.jahia.data.beans.ContainerListBean;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.LoadFlags;
import org.jahia.engines.EngineMessage;
import org.jahia.engines.EngineMessages;
import org.jahia.engines.search.PagesSearchViewHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.BasicURLGeneratorImpl;
import org.jahia.params.ProcessingContext;
import org.jahia.params.URLGenerator;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.pages.*;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.UserProperties;
import org.jahia.services.usermanager.UserProperty;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.search.SearchHandler;
import org.jahia.services.search.PageSearcher;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.content.ObjectKey;
import org.jahia.content.ContentPageKey;
import org.jahia.content.ContentObject;

import java.util.*;

/**
 * This is the content service, for getting pages, containers and so on...
 */
public class JahiaContentServiceImpl extends AbstractJahiaGWTServiceImpl implements JahiaContentService {

    private static final ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
    private static final Logger logger = Logger.getLogger(JahiaContentServiceImpl.class);

    public GWTJahiaContainerList loadContainerList(GWTJahiaPageContext page, String containerListName) {
        logger.debug("start: " + page);
        // get jahia box
        GWTJahiaContainerList containers = null;

        // retrieve container ParamBean and JahiaData
        ProcessingContext jParams = retrieveParamBean(page);
        JahiaData jData = retrieveJahiaData(page);
        try {
            if (jData == null) {
                logger.error("ParamBean not found");
                return null;
            }

            // get container list id
            int containerListId = getJahiaContainersService().getContainerListID(containerListName, page.getPid());

            // load container list
            JahiaContainerList mainContentContainerList = null;
            // if containerList empty then create it.
            if (containerListId == -1) {
                logger.debug("Page[" + page.getPid() + "] , containerList[" + containerListName + "] ---> create container lis]");
                getJahiaContainersService().saveContainerListInfo(mainContentContainerList, jParams.getContentPage().getAclID(), jParams);
            } else {
                mainContentContainerList = getJahiaContainersService().loadContainerList(containerListId, LoadFlags.ALL, jParams);
            }
            logger.debug("Page[" + page.getPid() + "] , containerList[" + containerListName + "] ---> container list id[" + containerListId + "]");

            // build JahiaBoxes
            containers = createJahiaGWTContainerList(mainContentContainerList);
        } catch (Exception e) {
            logger.error(e, e);
        }
        logger.debug("end");
        return containers;
    }


    private GWTJahiaContainerList createJahiaGWTContainerList(JahiaContainerList containerList) throws Exception {
        // gte ParamBean and JahiaData
        ProcessingContext jParams = getParamBeanRequestAttr();
        if (jParams == null) {
            logger.error("ParamBean not found.");
            return null;
        }
        JahiaData jData = getJahiaDataRequestAttr();
        if (jData == null) {
            logger.error("JahiaData not found.");
            return null;
        }

        // create a containerList bean
        ContainerListBean containerListBean = new ContainerListBean(containerList, jParams);

        // create a gwtContainerList
        GWTJahiaContainerList gwtContainerlist = new GWTJahiaContainerList(containerListBean.getID());
        logger.debug("ContainerList[" + containerListBean.getName() + "] size:" + containerListBean.getSize());

        // set add container launcher
        String addContainerLauncher = jData.gui().drawAddContainerUrl(containerList);
        logger.debug("add Container Launcher url:" + addContainerLauncher);
        gwtContainerlist.setAddContainerLauncher(addContainerLauncher);

        // add gwtContainerlist
        Iterator mainContentContainerEnum = containerList.getContainers();
        containerList.getFullSize();
        while (mainContentContainerEnum.hasNext()) {
            Object o = mainContentContainerEnum.next();
            if (o instanceof org.jahia.data.containers.JahiaContainer) {
                // get container
                JahiaContainer contentContainer = (JahiaContainer) o;

                //create gwtcontainer
                GWTJahiaContainer container = createJahiaGWTContainer(contentContainer);

                // add it to container List
                gwtContainerlist.addContainer(container);
            } else if (o instanceof JahiaContainerList) {
                logger.error("Error: object is not instance of a container");
            }
        }
        return gwtContainerlist;
    }

    private GWTJahiaContainer createJahiaGWTContainer(JahiaContainer jahiaContainer) throws Exception {
        //get ParamBean and JahiaData
        ProcessingContext jParams = getParamBeanRequestAttr();
        if (jParams == null) {
            logger.error("ParamBean not found.");
            return null;
        }
        JahiaData jData = getJahiaDataRequestAttr();
        if (jData == null) {
            logger.error("JahiaData not found.");
            return null;
        }
        // get container bean
        ContainerBean containerBean = new ContainerBean(jahiaContainer, jParams);

        // creata a gwtContainer
        GWTJahiaContainer gwtContainer = new GWTJahiaContainer(containerBean.getID());

        // add delete launcher
        String deleteContainerLauncher = jData.gui().drawDeleteContainerUrl(containerBean.getContentContainer());
        gwtContainer.setDeleteContainerLauncher(deleteContainerLauncher);

        // add edit launcher
        String updateContainerLauncher = jData.gui().drawUpdateContainerUrl(containerBean.getContentContainer());
        gwtContainer.setEditContainerLauncher(updateContainerLauncher);

        // add some properties
        gwtContainer.setFields(new HashMap<String, GWTJahiaField>());
        // to do : get values from gwtContainer properties
        gwtContainer.setColumn(0);
        gwtContainer.setRow(0);
        gwtContainer.setRow(0);

        // add fields
        final Iterator<JahiaField> fields = jahiaContainer.getFields();
        logger.debug("add " + jahiaContainer.getNbFields() + " fields.");
        while (fields.hasNext()) {
            // get current field
            final JahiaField curJahiaField = fields.next();
            final String name = curJahiaField.getDefinition().getName();
            // load it's value;
            curJahiaField.load(LoadFlags.ALL, getParamBeanRequestAttr());

            // added to JahiaContainer
            final String value = curJahiaField.getValue();
            logger.debug("add field [" + name + "," + value + "," + curJahiaField.getObject() + "," + curJahiaField.getRawValue() + "]");
            gwtContainer.addField(name, value);
        }

        // add ContainerList
        final Iterator<JahiaContainerList> containerLists = jahiaContainer.getContainerLists();
        logger.debug("Sub Container List size:");
        while (containerLists.hasNext()) {
            // load gwtContainer list
            JahiaContainerList containerList = containerLists.next();
            int containerListId = containerList.getID();
            String containerListName = containerList.getDefinition().getName();
            logger.debug("Container List Name: " + containerListName);
            JahiaContainerList mainContentContainerList = getJahiaContainersService().loadContainerList(containerListId, LoadFlags.ALL, jParams);

            // build JahiaBoxes
            GWTJahiaContainerList gwtContainers = createJahiaGWTContainerList(mainContentContainerList);
            gwtContainer.addContainerList(containerListName, gwtContainers);
        }

        return gwtContainer;
    }

    public String insertAddContainerHref(GWTJahiaPageContext page) {
        return "http://www.google.com/" + page.getMode() + "/" + page.getPid() + "/" + getRemoteUser();
    }

    public void saveContainerProperty(GWTJahiaPageContext page, int containerId, String propertyName, String propertyValue) {
        logger.debug("- call save container property with values " + containerId + "," + propertyName + "," + propertyValue + "-");
        try {
            org.jahia.data.containers.JahiaContainer container = getJahiaContainersService().loadContainerInfo(containerId);
            if (container != null) {
                container.setProperty(propertyName, propertyValue);
            } else {
                logger.debug("Container with id " + containerId + " not found.");
            }
        } catch (Exception e) {
            logger.error(e, e);
        }

    }

    public List<GWTJahiaUserProperty> getJahiaUserProperties(boolean onlyMySettings) {
        List<GWTJahiaUserProperty> jahiaUserProperties = new ArrayList<GWTJahiaUserProperty>();

        try {
            JahiaUser user = getRemoteJahiaUser();
            UserProperties userProperties = user.getUserProperties();
            UserProperties missingProperties = new UserProperties();
            if (userProperties.size() < UserProperties.DEFAULT_PROPERTIES_NAME.size()) {
                for (String propName : UserProperties.DEFAULT_PROPERTIES_NAME) {
                    if (!userProperties.getProperties().containsKey(propName)) {
                        final UserProperty missingProp;
                        if ("emailNotificationsDisabled".equals(propName)) {
                            missingProp = new UserProperty(propName, "false", false, UserProperty.CHECKBOX);

                        } else if ("preferredLanguage".equals(propName)) {
                            missingProp = new UserProperty(propName, getPreferredLocale(user).toString(), false,
                                    UserProperty.SELECT_BOX);

                        } else {
                            missingProp = new UserProperty(propName, "", false);
                        }
                        missingProperties.setUserProperty(propName, missingProp);
                    }
                }
            }

            // add password
            final UserProperties all = new UserProperties();
            all.putAll(userProperties);
            all.putAll(missingProperties);
            final Iterator<String> propertyNameIterator = all.propertyNameIterator();
            while (propertyNameIterator.hasNext()) {
                // property name
                final String name = propertyNameIterator.next();
                final UserProperty property = all.getUserProperty(name);
                // create the corresponding gwt bean
                final GWTJahiaUserProperty gwtJahiaUserProperty = new GWTJahiaUserProperty();

                // Todo serialize the display attribute in UserProperty to remove this code
                if ("emailNotificationsDisabled".equals(property.getName())) {
                    final GWTJahiaBasicDataBean data = new GWTJahiaBasicDataBean();
                    data.setValue(property.getValue());
                    data.setDisplayName(data.getValue());
                    gwtJahiaUserProperty.setDisplay(UserProperty.CHECKBOX);
                    gwtJahiaUserProperty.setValue(data);

                } else if ("preferredLanguage".equals(property.getName())) {
                    gwtJahiaUserProperty.setDisplay(UserProperty.SELECT_BOX);
                    gwtJahiaUserProperty.setValues(getAvailableBundleLanguageBeans());
                    final Locale loc = LanguageCodeConverters.languageCodeToLocale(property.getValue());
                    gwtJahiaUserProperty.setValue(new GWTJahiaBasicDataBean(loc.toString(), loc.getDisplayName(loc)));

                } else {
                    final GWTJahiaBasicDataBean data = new GWTJahiaBasicDataBean();
                    data.setValue(property.getValue());
                    data.setDisplayName(data.getValue());
                    gwtJahiaUserProperty.setValue(data);
                    gwtJahiaUserProperty.setDisplay(UserProperty.TEXT_FIELD);
                }

                gwtJahiaUserProperty.setRealKey(property.getName());
                if (gwtJahiaUserProperty.isJahiaMySettingsProperty()) {
                    // label from resource bundle
                    gwtJahiaUserProperty.setLabel(getLocaleTemplateRessource("mySettings." + gwtJahiaUserProperty.getKey()));
                } else {
                    gwtJahiaUserProperty.setLabel(gwtJahiaUserProperty.getKey());
                }

                gwtJahiaUserProperty.setReadOnly(property.isReadOnly());
                gwtJahiaUserProperty.setPassword(false);

                // filter by MySettings
                if (onlyMySettings) {
                    // check if it's a customs mySettings prop.
                    boolean isCustomMySettingsProp = name.startsWith(GWTJahiaUserProperty.CUSTOM_USER_PROPERTY_PREFIX);
                    int index = gwtJahiaUserProperty.getJahiaMySettingsPropertyIndex();
                    if (index > -1) {
                        // case of jahiaUserProperties. add it to the 'top' of the list
                        if (jahiaUserProperties.size() > index) {
                            jahiaUserProperties.add(index, gwtJahiaUserProperty);
                        } else {
                            jahiaUserProperties.add(gwtJahiaUserProperty);
                        }
                    } else {
                        // case of customUserProperties. Add it to the 'bottom' of the list
                        if (isCustomMySettingsProp) {
                            jahiaUserProperties.add(gwtJahiaUserProperty);
                        }
                    }

                }
                // get all user properties
                else {
                    jahiaUserProperties.add(gwtJahiaUserProperty);
                }
            }
        } catch (Exception e) {
            logger.error("Can't retrive user properties due to:", e);
        }
        return jahiaUserProperties;
    }


    public GWTJahiaAjaxActionResult updateJahiaUserProperties(List<GWTJahiaUserProperty> newJahiaUserProperties, List<GWTJahiaUserProperty> removeJahiaUserProperties) {
        GWTJahiaAjaxActionResult gwtAjaxActionResult = new GWTJahiaAjaxActionResult();
        JahiaUser user = getRemoteJahiaUser();

        // update user properties
        for (GWTJahiaUserProperty gwtJahiaUserProperty : newJahiaUserProperties) {
            // update password
            if (gwtJahiaUserProperty.isPassword()) {
                String password = gwtJahiaUserProperty.getValue().toString();
                ServicesRegistry registry = ServicesRegistry.getInstance();
                JahiaPasswordPolicyService pwdPolicyService = registry.getJahiaPasswordPolicyService();
                boolean pwdPolicyEnabled = pwdPolicyService.isPolicyEnabled(user);

                // check if password is to short
                if (password != null && password.length() > 0) {
                    if (!pwdPolicyEnabled && password.length() < 6) {
                        gwtAjaxActionResult.addError(getLocaleMessageResource("org.jahia.engines.mysettings.passwordTooShort"));
                    } else {
                        // no prb. with pwd length
                        if (pwdPolicyEnabled) {
                            PolicyEnforcementResult evalResult = pwdPolicyService.enforcePolicyOnPasswordChange(user, password, true);
                            if (!evalResult.isSuccess()) {
                                // password not validated by the pwdPolicyService
                                EngineMessages policyMsgs = evalResult.getEngineMessages();
                                for (EngineMessage errorMessage : policyMsgs.getMessages()) {
                                    gwtAjaxActionResult.addError(getLocaleMessageResource(errorMessage));
                                }
                            } else {
                                // password validated by the pwdPolicyService
                                user.setPassword(password);
                            }
                        } else {
                            // pwdPolicy not activated
                            user.setPassword(password);
                        }
                    }
                } else {
                    logger.debug("newPassword not set--> keep old one.");
                }
            }
            // update other properties
            else {
                UserProperty currentProp = user.getUserProperty(gwtJahiaUserProperty.getRealKey());
                if (currentProp == null) {
                    // add new properties
                    user.setProperty(gwtJahiaUserProperty.getRealKey(), gwtJahiaUserProperty.getValue().toString());
                } else if (!currentProp.getValue().equals(gwtJahiaUserProperty.getValue().getValue())) {
                    // update old properties
                    if (!currentProp.isReadOnly()) {
                        user.setProperty(gwtJahiaUserProperty.getRealKey(), gwtJahiaUserProperty.getValue().toString());
                    } else {
                        logger.debug("property[" + gwtJahiaUserProperty.getRealKey() + "] not update due to 'isReadOnly' flag.");
                    }
                }
            }
        }

        // remove user properties
        for (GWTJahiaUserProperty gwtJahiaUserProperty : removeJahiaUserProperties) {
            UserProperty currentProp = user.getUserProperty(gwtJahiaUserProperty.getRealKey());
            if (currentProp != null) {
                user.removeProperty(gwtJahiaUserProperty.getRealKey());
            }
        }

        return gwtAjaxActionResult;
    }


    public String getContent(GWTJahiaPageContext page, int containerId) {
        return "html of container " + containerId;
    }

    public String getFieldValues(int containerId, String fieldName) {
        try {
            org.jahia.data.containers.JahiaContainer container = getJahiaContainersService().loadContainerInfo(containerId);
            JahiaField field = container.getField(fieldName);
            return field.getValue();
        } catch (Exception e) {
            logger.error(e, e);
        }
        return "dump";
    }

    public GWTJahiaContainer loadContainer(int containerId) {
        try {
            final JahiaContainer container = getJahiaContainersService().loadContainerInfo(containerId);
            return createJahiaGWTContainer(container);
        } catch (Exception e) {
            logger.error("Error loading container", e);
        }
        return null;
    }

    public String getPagePropertyValue(GWTJahiaPageContext page, String propertyName) {
        try {
            Map<String, PageProperty> pageProperties = getJahiaPageService().getPageProperties(page.getPid());
            PageProperty pp = pageProperties.get(propertyName);
            if (pp != null) {
                logger.debug("Property with name " + propertyName + " found. ");
                return pp.getValue();
            }

        } catch (Exception e) {
            logger.error(e, e);
        }
        return null;
    }

    public void updatePagePropertyValue(GWTJahiaPageContext page, String propertyName, String propertyValue) {
        try {
            // retrieve container ParamBean and JahiaData
            ProcessingContext jParams = retrieveParamBean(page);
            JahiaPage jahiaPage = getJahiaPageService().lookupPage(page.getPid(), jParams);
            jahiaPage.setProperty(propertyName, propertyValue);

        } catch (Exception e) {
            logger.error(e, e);
        }
    }

    private JahiaContainersService getJahiaContainersService() {
        return servicesRegistry.getJahiaContainersService();
    }


    private JahiaPageService getJahiaPageService() {
        return servicesRegistry.getJahiaPageService();
    }


    public GWTJahiaPageWrapper getSiteHomePage(int siteId) {
        try {
            final JahiaSite site = servicesRegistry.getJahiaSitesService().getSite(siteId);
            return getJahiaPageWrapper(retrieveParamBean(), site.getHomePage());
        } catch (JahiaException e) {
            logger.error("Error getting site home page", e);
        }
        return null;
    }

    private GWTJahiaPageWrapper getJahiaPageWrapper(JahiaSite site) {
        GWTJahiaPageWrapper wPage = new GWTJahiaPageWrapper();
        wPage.setSiteRoot(true);
        wPage.setPid(0);
        wPage.setParentPid(0);
        wPage.setTitle(site.getSiteKey());
        wPage.setHasChildren(true);
        return wPage;
    }

    private GWTJahiaPageWrapper getJahiaPageWrapper(ProcessingContext jParams, JahiaPage jPage) throws JahiaException {
        GWTJahiaPageWrapper wPage = new GWTJahiaPageWrapper();
        wPage.setTitle(jPage.getTitle());
        if (wPage.getTitle() == null || wPage.getTitle().length() == 0) {
            wPage.setTitle(JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.workflow.display.notitle", jParams.getLocale()));
        }
        wPage.setLocked(jPage.getLanguagesStates(false).containsValue(EntryLoadRequest.WAITING_WORKFLOW_STATE));
        wPage.setPid(jPage.getID());
        wPage.setParentPid(jPage.getParentID());
        URLGenerator u = jParams.getUrlGenerator();
        jParams.setUrlGenerator(new BasicURLGeneratorImpl());
        Locale locale = jParams.getCurrentLocale();
        wPage.setLink(jParams.composeLanguageURL(locale.toString(),jPage.getID()));
        jParams.setUrlGenerator(u);
        wPage.setHasChildren(servicesRegistry.getJahiaPageService().pageHasChildren(jPage.getID(), PageLoadFlags.ALL, jParams.getUser()));
        try {
            wPage.setWorkflowStatus(servicesRegistry.getWorkflowService().getExtendedWorkflowStates(jPage.getContentPage()).get(jParams.getCurrentLocale().toString()));
            wPage.setHasLive(jPage.getContentPage().hasEntries(ContentPage.ACTIVE_PAGE_INFOS, jParams.getCurrentLocale().toString()));
        } catch (Exception e) {
            logger.error("Ccannot get status ",e);
        }
        return wPage;
    }

    public GWTJahiaPageWrapper getPage(int pid) {
        ProcessingContext jParams = retrieveParamBean();

        try {
            JahiaPage page = servicesRegistry.getJahiaPageService().lookupPage(pid, jParams);

            if (page.hasActiveEntries() || (!page.hasActiveEntries() && page.checkWriteAccess(jParams.getUser()))) {
                return getJahiaPageWrapper(jParams, page);
            }

        } catch (JahiaException e) {
            logger.error("could not find children for page " + pid, e);
        }
        return null;
    }

    // todo add rank
    /**
     * Get the subpages for a given jahia page, considering the current user and the mode.
     *
     * @param pid        the pid of the current paqe (to create processing context)
     * @param mode       the current mode (to create processing context)
     * @param parentPage the parent page as a GWT-specific bean (page wrapper)
     * @return a list of pages (page wrapper) containing all visible subpages
     */
    public List<GWTJahiaPageWrapper> getSubPagesForCurrentUser(int pid, String mode, GWTJahiaPageWrapper parentPage) {
        ProcessingContext jParams = retrieveParamBean(pid, mode);
        if (parentPage == null) { // this is the home page case
            JahiaPage jPage = jParams.getSite().getHomePage();
            parentPage = new GWTJahiaPageWrapper();
            parentPage.setTitle(jPage.getTitle());
            parentPage.setPid(jPage.getID());
            parentPage.setParentPid(0);
        }

        List<GWTJahiaPageWrapper> children = new ArrayList<GWTJahiaPageWrapper>();
        try {
            getChildren(parentPage, jParams, children);

        } catch (JahiaException e) {
            logger.error("could not find children for page " + pid, e);
        }
        return children;
    }

    public List<GWTJahiaPageWrapper> getSubPagesForCurrentUser(int parent) {
        return getSubPagesForCurrentUser(getPage(parent));
    }

    public List<GWTJahiaPageWrapper> getSubPagesForCurrentUser(GWTJahiaPageWrapper parentPage) {
        List<GWTJahiaPageWrapper> children = new ArrayList<GWTJahiaPageWrapper>();
        ProcessingContext jParams = retrieveParamBean();

        EntryLoadRequest el = new EntryLoadRequest(jParams.getEntryLoadRequest());
        el.setWorkflowState(EntryLoadRequest.STAGING_WORKFLOW_STATE);
        Locale locale = getEngineLocale();
        el.setFirstLocale(locale.toString());
        jParams.setEntryLoadRequest(el);
        jParams.setCurrentLocale(locale);
        try {
            if (parentPage == null) { // this is the site case
                Iterator<JahiaSite> enu = servicesRegistry.getJahiaSitesService().getSites();
                while (enu.hasNext()) {
                    JahiaSite site = enu.next();
                    if (site.getHomePage(el) == null) {
                        // the home page is not available for current user --> no need to include this site
                        continue;
                    }
                    GWTJahiaPageWrapper wPage = getJahiaPageWrapper(site);
                    children.add(wPage);
                    
                }
            } else if (parentPage.isSiteRoot()) { // this is the home page case
                JahiaPage jPage = servicesRegistry.getJahiaSitesService().getSiteByKey(parentPage.getTitle()).getHomePage(el);
                GWTJahiaPageWrapper wPage = getJahiaPageWrapper(jParams, jPage);
                children.add(wPage);
            } else {
                getChildren(parentPage, jParams, children);
            }

        } catch (JahiaException e) {
            logger.error("could not process page " + (parentPage != null ? parentPage.getPid() : "null"), e);
        }
        return children;
    }

    private void getChildren(GWTJahiaPageWrapper parentPage, ProcessingContext jParams, List<GWTJahiaPageWrapper> children) throws JahiaException {
        List<JahiaPage> childs = servicesRegistry.getJahiaPageService().getPageChilds(parentPage.getPid(), PageLoadFlags.ALL, jParams);

        // sort children
        for (JahiaPage page : childs) {
            if (page.getPageType() == JahiaPage.TYPE_DIRECT) {
                if (page.hasActiveEntries() || (!page.hasActiveEntries() && page.checkWriteAccess(jParams.getUser()))) {
                    GWTJahiaPageWrapper wPage = getJahiaPageWrapper(jParams, page);
                    children.add(wPage);
                }
            }
        }
    }


    /**
     * Retrieve the home page for the current site in order to get the sitemap entry point.
     *
     * @param pid  the pid of the current page (to create processing context)
     * @param mode the current mode (to create processing context)
     * @param rec  choose whether the sitemap should be retrieved in one call or not
     * @return the home page as a GWT specific bean (page wrapper) containing all subpages if specified (arg "rec")
     */
    public GWTJahiaPageWrapper getHomePageForCurrentUser(int pid, String mode, boolean rec) {
        ProcessingContext jParams = retrieveParamBean(pid, mode);
        JahiaPage home = jParams.getSite().getHomePage();
        try {
            GWTJahiaPageWrapper page = getJahiaPageWrapper(retrieveParamBean(), home);
            if (rec && page.hasChildren()) {
                page.setSubpages(getSubPagesForCurrentUserRec(page.getPid(), jParams));
            }
            return page;
        } catch (JahiaException e) {
            logger.error("could not retrieve home page", e);
        }
        return null;
    }

    /**
     * Recursive method to get the complete sitemap in one call
     *
     * @param parentId the root page id
     * @param jParams  processing context
     * @return a list of page fully filled
     */
    private List<GWTJahiaPageWrapper> getSubPagesForCurrentUserRec(int parentId, ProcessingContext jParams) {

        List<GWTJahiaPageWrapper> children = new ArrayList<GWTJahiaPageWrapper>();
        try {
            List<JahiaPage> childs = servicesRegistry.getJahiaPageService().getPageChilds(parentId, PageLoadFlags.ALL, jParams.getUser());

            // sort children
            for (JahiaPage page : childs) {
                if (page.hasActiveEntries() || (!page.hasActiveEntries() && page.checkWriteAccess(jParams.getUser()))) {
                    GWTJahiaPageWrapper wPage = getJahiaPageWrapper(jParams, page);
                    if (wPage.hasChildren()) {
                        wPage.setSubpages(getSubPagesForCurrentUserRec(wPage.getPid(), jParams));
                    }
                    children.add(wPage);
                }
            }

        } catch (JahiaException e) {
            logger.error("could not find children for page " + parentId, e);
        }
        return children;
    }

    private static Locale getPreferredLocale(JahiaUser user) {
        String propValue = user != null ? user.getProperty("preferredLanguage") : null;
        Locale locale = propValue != null ? LanguageCodeConverters.languageCodeToLocale(propValue) : null;

        if (null == locale) {
            // property is not set --> get list of site languages
            List<Locale> siteLocales = Collections.emptyList();
            final JahiaSite site = Jahia.getThreadParamBean().getSite();
            try {
                siteLocales = site.getLanguageSettingsAsLocales(true);
            } catch (JahiaException e) {
                logger.warn("Unable to retrieve language settings for site: " + site, e);
            }

            List<Locale> availableBundleLocales = getAvailableBundleLocales();
            for (Locale siteLocale : siteLocales) {
                if (availableBundleLocales.contains(siteLocale)) {
                    // this one is available
                    locale = siteLocale;
                    break;
                } else if (StringUtils.isNotEmpty(siteLocale.getCountry())) {

                    Locale languageOnlyLocale = new Locale(siteLocale.getLanguage());
                    if (availableBundleLocales.contains(languageOnlyLocale)) {
                        // get lanugtage without the country
                        locale = new Locale(siteLocale.getLanguage());
                        break;
                    }
                }
            }
            if (null == locale) {
                locale = Jahia.getThreadParamBean().getLocale();
            }
        }
        return locale;
    }

    public static List<Locale> getAvailableBundleLocales() {
        return LanguageCodeConverters.getAvailableBundleLocales(
                JahiaResourceBundle.JAHIA_MESSAGE_RESOURCES, null);
    }

    public static List<GWTJahiaBasicDataBean> getAvailableBundleLanguageBeans() {
        final List<Locale> tmp = getAvailableBundleLocales();
        final List<GWTJahiaBasicDataBean> result = new ArrayList<GWTJahiaBasicDataBean>();
        for (Locale locale : tmp) {
            result.add(new GWTJahiaBasicDataBean(locale.toString(), locale.getDisplayName(locale)));
        }
        return result;
    }

    public List<GWTJahiaPageWrapper> searchInPages(final String queryString) {
        List<GWTJahiaPageWrapper> result = new ArrayList<GWTJahiaPageWrapper>() ;
        ProcessingContext ctx = retrieveParamBean();

        List<String> languageCodes = new ArrayList<String>(1);
        languageCodes.add(ctx.getLocale().toString());

        PageSearcher searcher = new PageSearcher(new String[] { ServicesRegistry.getInstance().getJahiaSearchService().getSearchHandler(ctx.getSiteID()).getName() }, languageCodes);
        try {
            JahiaSearchResult results = searcher.search(new StringBuilder("jahia.title:").append(queryString).toString(), ctx);
            for (JahiaSearchHit hit: results.results()) {
                ObjectKey key = hit.getSearchHitObjectKey();
                if (key != null && key.getType().equals(ContentPageKey.PAGE_TYPE)) {
                    ContentPage page = (ContentPage) JahiaObjectCreator.getContentObjectFromKey(hit.getSearchHitObjectKey());
                    if (page != null && page.getPageType(ctx.getEntryLoadRequest()) == PageInfoInterface.TYPE_DIRECT) {
                        result.add(getJahiaPageWrapper(ctx, page.getPage(ctx))) ;
                    }
                }
            }
        } catch (JahiaException e) {
            logger.error(e.toString(), e);
        } catch (ClassNotFoundException e) {
            logger.error(e.toString(), e);
        }
        return result ;
    }
}
