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
import org.jahia.ajax.usersession.userSettings;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentObject;
import org.jahia.content.JahiaObject;
import org.jahia.content.PropertiesInterface;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainerStructure;
import org.jahia.data.fields.JahiaField;
import org.jahia.exceptions.JahiaException;
import org.jahia.gui.GuiBean;
import org.jahia.gui.HTMLToolBox;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentPageField;
import org.jahia.services.fields.ContentSmallTextField;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.utils.InsertionSortedMap;

import java.util.*;

/**
 * <p>Title: Container JavaBean compliant JahiaContainer facade</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber, Xavier Lawrence
 * @version $Id$
 */

public class ContainerBean extends ContentBean implements PropertiesInterface {

    private static final transient Logger logger = Logger.getLogger(ContainerBean.class);

    public static final String TYPE = "ContentContainer";

    private JahiaContainer jahiaContainer;
    private ContentContainer contentContainer;
    private Map fields;
    private Map actionURIs;
    private boolean completelyLocked;
    private boolean independantWorkflow;
    private boolean independantWorkflowInitialized = false;
    private int groupWorkflowState = 0;
    private boolean groupWorkflowStateInitialized = false;

    static {
        registerType(ContentContainer.class.getName(), ContainerBean.class.getName());
    }

    private final ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
    private final WorkflowService workflowService = servicesRegistry.getWorkflowService();
    private final ImportExportService importExportService = servicesRegistry.getImportExportService();
    private final LockService lockRegistry = servicesRegistry.getLockService();

    public ContainerBean() {
    }

    public ContainerBean(final JahiaContainer aJahiaContainer,
                         final ProcessingContext aProcessingContext) {
        this.jahiaContainer = aJahiaContainer;
        this.processingContext = aProcessingContext;
    }

    public static AbstractJahiaObjectBean getChildInstance(final JahiaObject jahiaObject,
                                                           final ProcessingContext processingContext) {
        final ContentContainer contentContainer = (ContentContainer) jahiaObject;
        try {
            return new ContainerBean(contentContainer.getJahiaContainer(
                    processingContext, processingContext.getEntryLoadRequest()), processingContext);
        } catch (JahiaException je) {
            logger.error("Error while converting content container to jahia container", je);
            return null;
        }
    }

    public int getID() {
        return jahiaContainer.getID();
    }

    public String getBeanType() {
        return TYPE;
    }

    public JahiaContainer getJahiaContainer() {
        return jahiaContainer;
    }

    public ContentContainer getContentContainer() {
        if (contentContainer == null) {
            contentContainer = jahiaContainer.getContentContainer();
        }
        return contentContainer;
    }

    public JahiaContainerDefinition getDefinition() {
        try {
            return jahiaContainer.getDefinition();
        } catch (JahiaException je) {
            logger.error(
                    "Error while accessing container list definition for container " +
                            getID() + ":", je);
            return null;
        }
    }

    public int getDefinitionID() {
        return getDefinition().getID();
    }

    public FieldBean getField(final String name) {
        try {
            final JahiaField jahiaField = jahiaContainer.getField(name);
            if (jahiaField == null) {
                return null;
            }
            return new FieldBean(jahiaField, processingContext);
        } catch (JahiaException je) {
            logger.error("Error while retrieving field " + name +
                    " for container " + getID() + ":", je);
            return null;
        }
    }

    //-------------------------------------------------------------------------

    /**
     * get a field value
     *
     * @param fieldName the field name
     * @throws JahiaException a critical jahia exception if field not found
     */
    public Object getFieldValue(final String fieldName)
            throws JahiaException {
        return getFieldValue(fieldName, false, null);
    }

    //-------------------------------------------------------------------------

    /**
     * get a field value
     *
     * @param fieldName
     * @param allowDiffVersionHighlight
     * @return
     * @throws JahiaException a critical jahia exception if field not found
     */
    public Object getFieldValue(final String fieldName,
                                final boolean allowDiffVersionHighlight,
                                final ProcessingContext jParams)
            throws JahiaException {
        final FieldBean theField = this.getField(fieldName);
        if (theField != null) {
            if (!allowDiffVersionHighlight) {
                return theField.getValue();
            } else {
                return theField.getHighLightDiffValue();
            }
        }
        return null;
    }

    //-------------------------------------------------------------------------

    /**
     * get a field value
     *
     * @param fieldName    the field name
     * @param defaultValue
     * @throws JahiaException a critical jahia exception if field not found
     */
    public Object getFieldValue(final String fieldName,
                                final String defaultValue,
                                final boolean allowDiffVersionHighlight,
                                final ProcessingContext jParams)
            throws JahiaException {
        final String value = (String)
                getFieldValue(fieldName, allowDiffVersionHighlight, jParams);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    //-------------------------------------------------------------------------

    /**
     * get a field value
     *
     * @param fieldName    the field name
     * @param defaultValue
     * @throws JahiaException a critical jahia exception if field not found
     */
    public Object getFieldValue(final String fieldName, final String defaultValue)
            throws JahiaException {
        final String value = (String) getFieldValue(fieldName);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    //-------------------------------------------------------------------------

    /**
     * get a field value
     *
     * @param fieldName the field name
     * @throws JahiaException a critical jahia exception if field not found
     */
    public Object getFieldObject(final String fieldName)
            throws JahiaException {
        final FieldBean theField = this.getField(fieldName);
        if (theField != null) {
            return theField.getObject();
        }
        return null;
    }


    public Map getFields() {
        if (fields != null) {
            return fields;
        }
        fields = new TreeMap();
        final Iterator<JahiaField> fieldEnum = jahiaContainer.getFields();
        while (fieldEnum.hasNext()) {
            final JahiaField curJahiaField = (JahiaField) fieldEnum.next();
            final FieldBean curFieldBean = new FieldBean(curJahiaField, processingContext);
            fields.put(curFieldBean.getName(), curFieldBean);
        }
        return fields;
    }

    public ContainerListBean getContainerList(final String name) {
        try {
            final JahiaContainerList jahiaContainerList = jahiaContainer.
                    getContainerList(name);
            if (jahiaContainerList == null) {
                return null;
            }
            return new ContainerListBean(
                    jahiaContainerList, processingContext);
        } catch (JahiaException je) {
            logger.error("Error while retrieving sub container list " + name +
                    " for container " + getID() + ":", je);
            return null;
        }
    }

    public int getSiteID() {
        return jahiaContainer.getSiteID();
    }

    public int getNbFields() {
        return jahiaContainer.getNbFields();
    }

    public int getContainerDefinitionID() {
        return jahiaContainer.getctndefid();
    }

    public JahiaBaseACL getACL() {
        return jahiaContainer.getACL();
    }

    public int getAclID() {
        return jahiaContainer.getAclID();
    }

    public int getPageID() {
        return jahiaContainer.getPageID();
    }

    public int getContainerListID() {
        return jahiaContainer.getListID();
    }

    public int getVersionID() {
        return jahiaContainer.getVersionID();
    }

    public int getWorkflowState() {
        return jahiaContainer.getWorkflowState();
    }

    public String getLanguageCode() {
        return jahiaContainer.getLanguageCode();
    }

    public int getRank() {
        return jahiaContainer.getRank();
    }

    public Properties getProperties() {
        return jahiaContainer.getProperties();
    }

    public void setProperties(final Properties properties) {
        jahiaContainer.setProperties(properties);
    }

    public String getProperty(final String propertyName) {
        return jahiaContainer.getProperty(propertyName);
    }

    public void setProperty(final String propertyName, final String propertyValue) {
        jahiaContainer.setProperty(propertyName, propertyValue);
    }

    public Map getActionURIBeans() {
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
            final Iterator actionURIIter = actionURIs.entrySet().iterator();
            boolean partiallyLocked = false;
            while (actionURIIter.hasNext()) {
                final Map.Entry curActionURIEntry = (Map.Entry) actionURIIter.next();
                final ActionURIBean curActionURIBean = (ActionURIBean) curActionURIEntry.getValue();
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
        if (!independantWorkflowInitialized) {
            try {
                final ContentContainer theContainer = jahiaContainer.getContentContainer();
                final int mode = workflowService.getWorkflowMode(theContainer);
                // Set independantWorkflow variable
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
                final ContentContainer theContainer = jahiaContainer.getContentContainer();
                final Map<String, Integer> languagesStates = workflowService.getLanguagesStates(
                        theContainer);
                Integer languageState = languagesStates.get(
                        processingContext.getLocale().toString());
                final Integer sharedLanguageState = (Integer) languagesStates.get(
                        ContentObject.SHARED_LANGUAGE);
                if (languageState != null && languageState.intValue() != -1) {
                    if (sharedLanguageState != null &&
                            languageState.intValue() < sharedLanguageState.intValue()) {
                        languageState = sharedLanguageState;
                    }
                } else if (languageState == null) {
                    languageState = sharedLanguageState;
                }
                // Set groupWorkflowState variable
                groupWorkflowState = (languageState != null) ? languageState.intValue() : 1;
                groupWorkflowStateInitialized = true;
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return groupWorkflowState;
    }

    public boolean isActionURIsEmpty() {
        if (!processingContext.getOperationMode().equals(ProcessingContext.EDIT)) {
            return true;
        }
        if (actionURIs == null) {
            buildActionURIs();
        }
        return actionURIs.isEmpty();
    }

    public ContentBean getParent() {
        return getParentContainerList();
    }

    public ContainerListBean getParentContainerList() {
        int containerListID = getContainerListID();
        try {
            final ContentContainerList parentContainerList = ContentContainerList.
                    getContainerList(containerListID);
            final JahiaContainerList parentJahiaContainerList = parentContainerList.
                    getJahiaContainerList(processingContext,
                            processingContext.getEntryLoadRequest());
            return new ContainerListBean(parentJahiaContainerList, processingContext);
        } catch (JahiaException je) {
            logger.error("Error while trying to retrieve parent container list ID=" + containerListID, je);
        }
        return null;
    }

    private void buildActionURIs() {
        actionURIs = new InsertionSortedMap();
        final GuiBean guiBean = new GuiBean(processingContext);
        final HTMLToolBox htmlToolBox = new HTMLToolBox(guiBean, processingContext);
        completelyLocked = true;
        final JahiaUser user = processingContext.getUser();
        final int siteID = processingContext.getSiteID();
        boolean isPicker = isPicker();

        try {
            final ContentContainer theContainer = jahiaContainer.getContentContainer();
            String curURL;
            String curLauncherURI;
            ActionURIBean curActionURIBean;

            // action menu item to see the source of linked copy
            if (isPicker) {
                ContentObject pickedObject;// the source of the linked copy(the picked)
                ContentContainer pickedContainer;
                String pickedpageID;
                int pickedSiteID;
                boolean pagefound = false;
                try {
                    pickedObject = theContainer.getPickedObject();
                    pickedSiteID = pickedObject.getSiteID();
                    pickedContainer = ContentContainer.getContainer(pickedObject.getID());
                    if (pickedContainer.getJahiaContainer(processingContext, processingContext.getEntryLoadRequest()) != null)
                    {
                        pickedpageID = String.valueOf(pickedContainer.getPageID());
                        if (pickedpageID != null) {
                            logger.debug("pageID:" + pickedpageID);
                            pagefound = true;
                            if (pickedSiteID != siteID) {
                                logger.debug("cross-site");
                            }
                        }
                        final List l = pickedContainer.getChilds(null, EntryLoadRequest.STAGED);
                        for (final Iterator iterator1 = l.iterator(); iterator1.hasNext();) {
                            final Object o = iterator1.next();
                            if (!(o instanceof ContentPageField)) continue;
                            //defensive code relative to poor impl of exception catching/throwing of method getPage below
                            final JahiaPage page = ((ContentPageField) o).getPage(processingContext, EntryLoadRequest.STAGED);

                            if (page != null) {
                                if (page.getPageType() == JahiaPage.TYPE_DIRECT) {
                                    logger.debug("" + page.toString());
                                    pickedpageID = String.valueOf(page.getID());
                                    logger.debug("found contentpagefield:" + pickedpageID);
                                    pagefound = true;
                                    break;
                                }
                            }
                        }
                        if (pagefound) {
                            final StringBuffer buff = new StringBuffer();
                            buff.append("document.location='");
                            int pickedPageID = 0;
                            try {
                                pickedPageID = Integer.parseInt(pickedpageID);
                            } catch (NumberFormatException nfe) {
                                pickedPageID = 0;
                            }
                            buff.append(processingContext.composePageUrl(pickedPageID));
                            buff.append("'");
                            curActionURIBean = new ActionURIBean("picker", "", buff.toString());
                            actionURIs.put(curActionURIBean.getName(), curActionURIBean);
                        }
                    } else {
                        isPicker = false;
                    }
                } catch (JahiaException e) {
                    logger.error(e);
                }
            }

            // update action
            curURL = guiBean.drawUpdateContainerUrl(theContainer);
            curLauncherURI = htmlToolBox.drawUpdateContainerLauncher(jahiaContainer.getContextualContainerListID(),
                    theContainer);
            curActionURIBean = new ActionURIBean("update", curURL, curLauncherURI);
            LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_CONTAINER_TYPE, jahiaContainer.getID());

            if (!isPicker || jahiaContainer.checkAdminAccess(user)) {
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
            }
            // before generating delete URL we must check for any page fields
            // that ARE the current page. We are not allowed to perform a
            // delete operation on the page we are currently on.

            ContainerListBean parentBean = this.getParentContainerList();
            int nbContainers = parentBean.getContainerIdsInList(null).size();

            boolean singleContainer = ((this.jahiaContainer.getDefinition().getContainerListType()
                    & JahiaContainerDefinition.MANDATORY_TYPE) != 0) && nbContainers <= 1;

            if (! getContentObject().isMarkedForDelete()) {
                if (!singleContainer) {
                    curURL = guiBean.drawDeleteContainerUrl(theContainer);
                    curLauncherURI = htmlToolBox.drawDeleteContainerLauncher(
                            theContainer);
                    curActionURIBean = new ActionURIBean("delete", curURL,
                            curLauncherURI);
                    lockKey = LockKey.composeLockKey(LockKey.DELETE_CONTAINER_TYPE, jahiaContainer.getID());
                    if (!lockRegistry.isAcquireable(lockKey, user, user.getUserKey())) {
                        curActionURIBean.setLocked(true);
                    } else {
                        completelyLocked = false;
                    }
                    if (!lockRegistry.canRelease(lockKey, user, user.getUserKey())) {
                        curActionURIBean.setReleaseable(true);
                    }
                    if ((curActionURIBean.getUri() != null) &&
                            (!"".equals(curActionURIBean.getUri()))) {
                        actionURIs.put(curActionURIBean.getName(), curActionURIBean);
                    }
                }
            } else {
                if (jahiaContainer.checkWriteAccess(user)) {
                    curURL = guiBean.drawRestoreContainerUrl(theContainer);
                    curLauncherURI = htmlToolBox.drawRestoreContainerLauncher(theContainer);
                    curActionURIBean = new ActionURIBean("restore", curURL, curLauncherURI);
                    final LockKey restoreLockKey = LockKey.composeLockKey(LockKey.RESTORE_LIVE_CONTAINER_TYPE,
                            jahiaContainer.getID());
                    if (!lockRegistry.isAcquireable(restoreLockKey, user, user.getUserKey())) {
                        curActionURIBean.setLocked(true);
                    } else {
                        completelyLocked = false;
                    }
                    if (!lockRegistry.canRelease(restoreLockKey, user, user.getUserKey())) {
                        curActionURIBean.setReleaseable(true);
                    }
                    if ((curActionURIBean.getUri() != null) &&
                            (!"".equals(curActionURIBean.getUri()))) {
                        actionURIs.put(curActionURIBean.getName(), curActionURIBean);
                    }
                }
            }

            // copy action menu
            final StringBuffer buff = new StringBuffer();
            buff.append("clipboard('");
            buff.append(((ParamBean) processingContext).getRequest().getContextPath());
            buff.append("','");
            buff.append(jahiaContainer.getContentContainer().getObjectKey());
            buff.append("','copy',");
            buff.append(processingContext.getPageID());
            buff.append(")");
            curActionURIBean = new ActionURIBean("copy", "", buff.toString());

            lockKey = LockKey.composeLockKey(LockKey.EXPORT_CONTAINER_TYPE, jahiaContainer.getID());

            if (!lockRegistry.isAcquireable(lockKey, user, user.getUserKey())) {
                curActionURIBean.setLocked(true);
                curActionURIBean.setLauncherUri("void");
            } else {
                completelyLocked = false;
            }
            if (!lockRegistry.canRelease(lockKey, user, user.getUserKey())) {
                curActionURIBean.setReleaseable(true);
            }

            actionURIs.put(curActionURIBean.getName(), curActionURIBean);

            // action menu to see the pickers list
            int pickerscount = theContainer.getPickerObjects().size();
            if (pickerscount > 0) {

                logger.debug("number of pickers" + pickerscount);
                buff.delete(0, buff.length());
                buff.append("displayPickers('").append(((ParamBean) processingContext).getRequest().getContextPath()).append("','");
                buff.append(theContainer.getID());
                buff.append("',600,400)");
                curActionURIBean = new ActionURIBean("picked", "", buff.toString());
                actionURIs.put(curActionURIBean.getName(), curActionURIBean);

                //looping for pickers to display the list directly in actionmenu
                Iterator pickers = theContainer.getPickerObjects().iterator();
                int count = 0;
                while (pickers.hasNext() && count < 4) {
                    ContentObject co = (ContentObject) pickers.next();

                    try {
                        ActionURIBean aub = getActionUriPicker(co);
                        actionURIs.put(aub.getName() + "_" + count, aub);
                        count++;
                    } catch (Exception e) {
                        logger.error("Error when getting picker info", e);
                    }

                }
            }

            // If the container is displayed as an absolute reference, add a link to the source page where it has been declared
            // unless the workflow icon is already displayed next to it
            if (theContainer.getPageID() != processingContext.getPageID()
                    && (!isIndependantWorkflow() || !(org.jahia.settings.SettingsBean.getInstance()
                            .isDevelopmentMode()
                            || processingContext.getSessionState()
                                    .getAttribute(userSettings.WF_VISU_ENABLED) != null || Jahia
                            .getSettings().isWflowDisp()))) {
                curURL = processingContext.composePageUrl(theContainer
                        .getPageID(), processingContext.getLocale().toString());
                final StringBuffer url = new StringBuffer();
                url.append("document.location.href='");
                url.append(curURL);
                url.append("'");
                curActionURIBean = new ActionURIBean("source", url.toString(),
                        url.toString());
                if (curActionURIBean.getUri() != null
                        && curActionURIBean.getUri().length() > 0) {
                    actionURIs
                            .put(curActionURIBean.getName(), curActionURIBean);
                }
            }                

        } catch (JahiaException je) {
            logger.error("Error while retrieving action URI map for container " + getID(), je);
        }
    }

    /**
     * to get the actionuri for the pickers
     * specific format for the launcher to be processed by addactions js function<br>
     *
     * @param o the picker contentobject
     * @return an ActionUriBean
     */
    private ActionURIBean getActionUriPicker(ContentObject o) {
        int pageID = 0;
        int siteID = 0;
        String t = "NA";
        String u = "";
        /*
        boolean isPage = false;
        boolean isActive = false;
        boolean isReadable = false;
        ContentPage thepage = null;
        */
        if (o instanceof ContentContainer) {
            try {
                //thepage = o.getPage();
                pageID = o.getPageID();
                siteID = o.getSiteID();

                List l = o.getChilds(processingContext.getUser(), EntryLoadRequest.STAGED, JahiaContainerStructure.JAHIA_FIELD);

                for (Iterator iterator1 = l.iterator(); iterator1.hasNext();) {

                    ContentField contentField = (ContentField) iterator1.next();
                    String value = contentField.getValue(processingContext, EntryLoadRequest.STAGED);
                    if (value != null && !value.trim().equals("") && !value.equals("<empty>")) {
                        t = value;
                        if (value.length() > 12) t = value.substring(0, 12) + " (...)";
                        logger.debug("tkey=" + t);

                        break;
                    }
                }

                // case the content object is text
                for (Iterator iterator1 = l.iterator(); iterator1.hasNext();) {

                    ContentField contentField = (ContentField) iterator1.next();
                    if (contentField instanceof ContentSmallTextField) {
                        logger.debug("child object is smalltextField");
                        String value = contentField.getValue(null, EntryLoadRequest.STAGED);
                        if (value != null && !value.trim().equals("") && !value.equals("<empty>")) {
                            t = value;
                            //thepage = contentField.getPage();
                            pageID = contentField.getPageID();
                            siteID = contentField.getSiteID();
                            logger.debug("text value key=" + t);
                            break;
                        }
                    }
                }

                //looping list of childs to check page type?
                for (Iterator iterator1 = l.iterator(); iterator1.hasNext();) {

                    ContentField contentField = (ContentField) iterator1.next();
                    if (contentField instanceof ContentPageField) {
                        //isPage = true;
                        logger.debug("child object is Contentpage field");
                        ContentPage contentPage = ((ContentPageField) contentField).getContentPage(EntryLoadRequest.STAGED);
                        if (contentPage != null) {
                            if (contentPage.getPageType(EntryLoadRequest.STAGED) == JahiaPage.TYPE_DIRECT) {
                                List li=new ArrayList();
                                li.add(processingContext.getLocale());
                                EntryLoadRequest lr = new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0,
                                        li, org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects());
                                t = contentPage.getTitle(lr);
                                if (t == null) {
                                    t = (String) contentPage.getTitles(true).get(processingContext.getLocale().getDisplayName());
                                }
                                //thepage = contentPage;
                                pageID = contentPage.getID();
                                siteID = contentPage.getSiteID();
                                logger.debug("page value key=" + t);
                                break;
                            }
                        }
                    }
                }
                u=processingContext.composePageUrl(pageID);

            } catch (JahiaException e) {
                logger.error("error", e);
                u="";
            }
        }
        // ad the end, we set the teaser as key:sitekey or pid:sitekey
        try {
            t = "site:" + ServicesRegistry.getInstance().getJahiaSitesService().getSite(siteID).getSiteKey()+" - pid:"+pageID;
        } catch (JahiaException e) {
            logger.debug(e);
        }

        //format pid,is ok for writeacess,title,url
        String launcher = "" + pageID + "," + o.checkWriteAccess(processingContext.getUser()) + "," + t + "," + u;
        return new ActionURIBean("pickedlist", "", launcher);
    }


    public boolean isPicker() {
        try {
            return importExportService.isPicker(jahiaContainer.getContentContainer());
        } catch (JahiaException e) {
            logger.error(e);
        }
        return false;
    }

    public ContentObject getContentObject() {
        return getContentContainer();
    }

    public String getJCRPath() throws JahiaException {
        return getContentContainer().getJCRPath(processingContext);
    }

}
