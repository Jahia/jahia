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
package org.jahia.ajax.gwt.templates.components.actionmenus.server.helper;

import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.actionmenu.actions.*;
import org.jahia.ajax.gwt.utils.JahiaObjectCreator;
import org.jahia.params.ProcessingContext;
import org.jahia.content.ObjectKey;
import org.jahia.content.ContentObject;
import org.jahia.data.beans.*;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.fields.ContentField;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.exceptions.JahiaException;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 27 f�vr. 2008 - 14:57:12
 */
public class ActionMenuHelper {

    private static Logger logger = Logger.getLogger(ActionMenuHelper.class) ;
    private static LockRegistry lockRegistry = LockRegistry.getInstance() ;


    public static String isActionMenuAvailable(ProcessingContext processingContext, GWTJahiaPageContext page, String objectKey, String bundleName, String labelKey) {
        logger.debug("Entered 'isActionMenuAvailable'") ;
        try {

            // if we are not in edit mode we don't display the GUI
            if (!ProcessingContext.EDIT.equals(page.getMode())) {
                logger.debug("live mode -> no action menu") ;
                return null ;
            }

            // get the content object type
            String contentType = objectKey.substring(0, objectKey.indexOf(ObjectKey.KEY_SEPARATOR)) ;

            // get the type of the current object
            ContentObject contentObject = JahiaObjectCreator.getContentObjectFromString(objectKey);

            String type = null ;

            // container list case
            if (contentType.equals(ContainerListBean.TYPE)) {
                final ContainerListBean listBean = (ContainerListBean) ContentBean.getInstance(contentObject, processingContext) ;
                final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
                if (listBean.getFullSize() == 0 &&
                        aclService.getSiteActionPermission("engines.languages." + processingContext.getLocale().toString(),
                        processingContext.getUser(),
                        JahiaBaseACL.READ_RIGHTS, processingContext.getSiteID()) <= 0) {
                    logger.debug("empty list / no rights for the current language") ;
                    return null ;
                }
                type = ActionMenuLabelProvider.CONTAINER_LIST ;
            } else if (contentType.equals(ContainerBean.TYPE)) {
                type = ActionMenuLabelProvider.CONTAINER ;
            } else if (contentType.equals(FieldBean.TYPE)) {
                type = ActionMenuLabelProvider.FIELD ;
            } else if (contentType.equals(PageBean.TYPE)) {
                type = ActionMenuLabelProvider.PAGE ;
            }

            try {
                if (contentObject.getID() > 0 && !contentObject.getACL().getPermission(processingContext.getUser(), JahiaBaseACL.WRITE_RIGHTS)) {
                    // if the user doesn't have Write access on the object, don't display the GUI
                    return null ;
                }
            } catch (Exception t) {
                logger.error(t.getMessage(), t);
                return null ;
            }

            ContentBean parentContentObject = (ContentBean) ContentBean.getInstance(contentObject.getParent(processingContext.getEntryLoadRequest()), processingContext) ;
            if (!(parentContentObject != null && parentContentObject.isPicker())) {
                if (labelKey != null) {
                    String label = ActionMenuLabelProvider.getIconLabel(bundleName, labelKey, type, processingContext) ;
                    if (label != null) {
                        return label ;
                    }
                }
                return "" ;
            }
            else {
                return null ;
            }

        } catch (ClassNotFoundException e) {
            logger.error("action menu availability check failed", e);
            return null ;
        } catch (JahiaException e) {
            logger.error("action menu availability check failed", e);
            return null ;
        }
    }


    public static List<GWTJahiaAction> getAvailableActions(HttpSession session, ProcessingContext jParams, final GWTJahiaPageContext page, final String objectKey, final String bundleName, final String namePostFix) {
        if (logger.isDebugEnabled()) {
            logger.debug("Page : " + page.getPid()) ;
            logger.debug("Object key : " + objectKey) ;
            logger.debug("Bundle name : " + bundleName) ;
            logger.debug("Name postfix " + namePostFix) ;
        }

        List<GWTJahiaAction> actions = new ArrayList<GWTJahiaAction>() ;

        try {
            final JahiaUser currentUser = jParams.getUser() ;
            final ContentObject contentObject = JahiaObjectCreator.getContentObjectFromString(objectKey) ;

            if (currentUser == null || contentObject == null || !contentObject.checkWriteAccess(currentUser)) {
                if (currentUser == null) logger.debug("currentUser is null");
                if (contentObject == null) logger.debug("object is null: " + objectKey);

                if (jParams.getPage() != null && jParams.getPage().checkWriteAccess(currentUser)) {
                    logger.debug("user has write access on currentPage: -> OK");
                } else {
                    throw new JahiaForbiddenAccessException();
                }
            }

            // get the content object type
            String objectType = objectKey.substring(0, objectKey.indexOf(ObjectKey.KEY_SEPARATOR)) ;
            if (logger.isDebugEnabled()) {
                logger.debug("Object type : " + objectType) ;
            }

            final EntryLoadRequest elr = new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, jParams.getLocales(), true);
            EntryLoadRequest savedEntryLoadRequest = jParams.getSubstituteEntryLoadRequest();
            jParams.setSubstituteEntryLoadRequest(elr);

            // GWTJahiaAction Menu for a page
            if (PageBean.TYPE.equals(objectType)) {
                // Update
                String url = ActionMenuURIFormatter.drawPageUpdateUrl(jParams, contentObject.getID()) ;
                if (url != null) {
                    GWTJahiaAction updatePage = new GWTJahiaEngineAction(GWTJahiaAction.UPDATE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.UPDATE, namePostFix, ActionMenuLabelProvider.PAGE),url) ;
                    LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_PAGE_TYPE, contentObject.getID());
                    if (!lockRegistry.isAcquireable(lockKey, currentUser, currentUser.getUserKey())) {
                        updatePage.setLocked(true);
                    }
                    actions.add(updatePage) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Update action : " + url);
                    }
                }

            // GWTJahiaAction Menu for a ContainerList
            } else if (ContainerListBean.TYPE.equals(objectType)) {
                ContentContainerList containerList = (ContentContainerList) contentObject ;
                if (logger.isDebugEnabled()) {
                    logger.debug("ContainerListID: " + containerList.getID() + ", def: " + containerList.getJahiaContainerList(jParams, jParams.getEntryLoadRequest()).getDefinition().getID());
                }
                String url ;
                // Add
                url = ActionMenuURIFormatter.drawContainerListAddUrl(jParams, containerList) ;
                if (url != null) {
                    GWTJahiaAction addContainer = new GWTJahiaEngineAction(GWTJahiaAction.ADD, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.ADD, namePostFix, ActionMenuLabelProvider.CONTAINER_LIST), url) ;
                    LockKey lockKey = LockKey.composeLockKey(LockKey.ADD_CONTAINER_TYPE, contentObject.getID());
                    if (!lockRegistry.isAcquireable(lockKey, currentUser, currentUser.getUserKey())) {
                        addContainer.setLocked(true);
                    }
                    actions.add(addContainer) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Add action : " + url);
                    }
                }
                // Update
                url = ActionMenuURIFormatter.drawContainerListUpdateUrl(jParams, containerList) ;
                if (url != null) {
                    GWTJahiaAction updateContainerList = new GWTJahiaEngineAction(GWTJahiaAction.UPDATE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.UPDATE, namePostFix, ActionMenuLabelProvider.CONTAINER_LIST), url) ;
                    LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_CONTAINERLIST_TYPE, contentObject.getID());
                    if (!lockRegistry.isAcquireable(lockKey, currentUser, currentUser.getUserKey())) {
                        updateContainerList.setLocked(true);
                    }
                    actions.add(updateContainerList) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Update action : " + url);
                    }
                }
                // Purge
//                url = ActionMenuURIFormatter.drawContainerListPurgeUrl(jParams, containerList) ;
//                if (url != null) {
//                    GWTJahiaAction purgeContainerList = new GWTJahiaEngineAction(GWTJahiaAction.DELETE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.DELETE, namePostFix, ActionMenuLabelProvider.CONTAINER_LIST), url) ;
//                    /* Begin global lock check TODO find a better way (optimization ?) */
//                    List<LockKey> locks = new ArrayList<LockKey>() ;
//                    Iterator<JahiaContainer> containerIt = containerList.getJahiaContainerList(jParams, jParams.getEntryLoadRequest()).getContainers() ;
//                    while (containerIt.hasNext()) {
//                        locks.add(LockKey.composeLockKey(LockKey.DELETE_CONTAINER_TYPE, containerIt.next().getID())) ;
//                    }
//                    for (LockKey lk: locks) {
//                        if (!lockRegistry.isAcquireable(lk, currentUser, currentUser.getUserKey())) {
//                            purgeContainerList.setLocked(true);
//                            break ;
//                        }
//                    }
//                    /* End global lock check */
//                    actions.add(purgeContainerList) ;
//                    if (logger.isDebugEnabled()) {
//                        logger.debug("Purge action : " + url);
//                    }
//                }
                // Copy
                if (objectKey != null && !containerList.isMarkedForDelete() && containerList.getJahiaContainerList(jParams, elr).getFullSize() > 0) {
                    actions.add(new GWTJahiaClipboardAction(GWTJahiaAction.COPY, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.COPY, namePostFix, ActionMenuLabelProvider.CONTAINER_LIST), objectKey)) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Copy action : " + objectKey);
                    }
                }
                // Paste
                String pastedType ;
                if ((pastedType = ClipboardHelper.isPasteAllowed(session, jParams, objectKey)) != null) {
                    GWTJahiaAction pasteContainer = new GWTJahiaClipboardAction(GWTJahiaAction.PASTE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.PASTE, namePostFix, pastedType), objectKey) ;
                    LockKey lockKey = LockKey.composeLockKey(LockKey.ADD_CONTAINER_TYPE, contentObject.getID());
                    if (!lockRegistry.isAcquireable(lockKey, currentUser, currentUser.getUserKey())) {
                        pasteContainer.setLocked(true);
                    }
                    actions.add(pasteContainer) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Paste action : " + objectKey);
                    }
                }
                // Paste reference
                if (pastedType != null && ActionMenuLabelProvider.CONTAINER.equals(pastedType) && ClipboardHelper.clipboardContentHasActiveEntry(session)) { // paste only container reference
                    GWTJahiaAction pasteContainerReference = new GWTJahiaClipboardAction(GWTJahiaAction.PASTE_REF, ActionMenuLabelProvider.getActionLabel(jParams, GWTJahiaAction.PASTE_REF), objectKey) ;
                    LockKey lockKey = LockKey.composeLockKey(LockKey.ADD_CONTAINER_TYPE, contentObject.getID());
                    if (!lockRegistry.isAcquireable(lockKey, currentUser, currentUser.getUserKey())) {
                        pasteContainerReference.setLocked(true);
                    }
                    actions.add(pasteContainerReference) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Paste reference action : " + objectKey);
                    }
                }

            // GWTJahiaAction Menu for a Container
            } else if (ContainerBean.TYPE.equals(objectType)) {
                ContentContainer container = (ContentContainer) contentObject ;
                // Update
                String url = ActionMenuURIFormatter.drawContainerUpdateUrl(jParams, container, 0) ;
                if (url != null) {
                    GWTJahiaAction updateContainer = new GWTJahiaEngineAction(GWTJahiaAction.UPDATE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.UPDATE, namePostFix, ActionMenuLabelProvider.CONTAINER), url) ;
                    LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_CONTAINER_TYPE, contentObject.getID());
                    if (!lockRegistry.isAcquireable(lockKey, currentUser, currentUser.getUserKey())) {
                        updateContainer.setLocked(true);
                    }
                    actions.add(updateContainer) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Update action : " + url);
                    }
                }
                // Restore
                // temporarily deactivated TODO
                /*url = ActionMenuURIFormatter.drawContainerRestoreUrl(jParams, container) ;
                if (url != null) {
                    GWTJahiaAction restoreContainer = new GWTJahiaEngineAction(GWTJahiaAction.RESTORE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, "restore", namePostFix), url) ;
                    LockKey lockKey = LockKey.composeLockKey(LockKey.RESTORE_LIVE_CONTAINER_TYPE, contentObject.getID());
                    if (!lockRegistry.isAcquireable(lockKey, currentUser, currentUser.getUserKey())) {
                        restoreContainer.setLocked(true);
                    }
                    actions.add(restoreContainer) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Restore action : " + url);
                    }
                }*/
                // Copy
                if (objectKey != null && !container.isMarkedForDelete()) {
                    actions.add(new GWTJahiaClipboardAction(GWTJahiaAction.COPY, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.COPY, namePostFix, ActionMenuLabelProvider.CONTAINER), objectKey)) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Copy action : " + objectKey);
                    }
                }
                // Delete
                url = ActionMenuURIFormatter.drawContainerDeleteUrl(jParams, container) ;
                if (url != null) {
                    GWTJahiaAction deleteContainer = new GWTJahiaEngineAction(GWTJahiaAction.DELETE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.DELETE, namePostFix, ActionMenuLabelProvider.CONTAINER), url) ;
                    LockKey lockKey = LockKey.composeLockKey(LockKey.DELETE_CONTAINER_TYPE, contentObject.getID());
                    if (!lockRegistry.isAcquireable(lockKey, currentUser, currentUser.getUserKey())) {
                        deleteContainer.setLocked(true);
                    }
                    actions.add(deleteContainer) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Delete action : " + url);
                    }
                }
                // Picked / source
                Map<String, String> source = ActionMenuURIFormatter.drawContainerPickedUrl(jParams, container) ;
                if (source != null && source.size() == 1) {
                    List<GWTJahiaRedirectAction> sourceRedirect = new ArrayList<GWTJahiaRedirectAction>(1) ;
                    String title = source.keySet().iterator().next() ;
                    sourceRedirect.add(new GWTJahiaRedirectAction(GWTJahiaAction.SOURCE, title, source.get(title))) ;
                    actions.add(new GWTJahiaDisplayPickersAction(GWTJahiaAction.SOURCE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.SOURCE, namePostFix, ActionMenuLabelProvider.CONTAINER), sourceRedirect)) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Picked action : " + source.get(title));
                    }
                }
                // Picker List
                Map<String, String> pickers = ActionMenuURIFormatter.getContainerPickerList(jParams, container) ;
                if (pickers != null && pickers.size() > 0) {
                    List<GWTJahiaRedirectAction> pickersRedirect = new ArrayList<GWTJahiaRedirectAction>(pickers.size()) ;
                    for (String title: pickers.keySet()) {
                        pickersRedirect.add(new GWTJahiaRedirectAction(GWTJahiaAction.PICKER_LIST, title, pickers.get(title))) ;
                    }
                    actions.add(new GWTJahiaDisplayPickersAction(GWTJahiaAction.PICKER_LIST, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.PICKER_LIST, namePostFix, ActionMenuLabelProvider.CONTAINER), pickersRedirect)) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Picker list action : " + pickers.size());
                    }
                }
                // Source not in use any more
//                url = ActionMenuURIFormatter.drawContainerSourcePageReferenceUrl(jParams, container) ;
//                if (url != null) {
//                    actions.add(new GWTJahiaRedirectAction(GWTJahiaAction.SOURCE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.SOURCE, namePostFix, ActionMenuLabelProvider.CONTAINER), url)) ;
//                    if (logger.isDebugEnabled()) {
//                        logger.debug("Source action : " + url);
//                    }
//                }

            // GWTJahiaAction Menu for a Field
            } else if (FieldBean.TYPE.equals(objectType)) {
                ContentField field = (ContentField)contentObject ;
                // Update
                String url = ActionMenuURIFormatter.drawFieldUpdateUrl(field, jParams) ;
                if (url != null) {
                    GWTJahiaAction updateField = new GWTJahiaEngineAction(GWTJahiaAction.UPDATE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.UPDATE, namePostFix, ActionMenuLabelProvider.FIELD), url) ;
                    LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_FIELD_TYPE, contentObject.getID());
                    if (!lockRegistry.isAcquireable(lockKey, currentUser, currentUser.getUserKey())) {
                        updateField.setLocked(true);
                    }
                    actions.add(updateField) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Update action : " + url);
                    }
                }
                // Source
                url = ActionMenuURIFormatter.drawFieldSourcePageReferenceUrl(field, jParams) ;
                if (url != null) {
                    actions.add(new GWTJahiaRedirectAction(GWTJahiaAction.SOURCE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.SOURCE, namePostFix, ActionMenuLabelProvider.FIELD), url)) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Source action : " + url);
                    }
                }

            // unknown object type
            } else {
                final String msg = "Unknown 'ObjectType' value ! 'ObjectType' value should be '" +
                        PageBean.TYPE + "', '" + ContainerListBean.TYPE + "', '" +
                        ContainerBean.TYPE + "' or '" + FieldBean.TYPE + "'.";
                logger.error(msg);
                return null;
            }

            jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);

        } catch (JahiaSessionExpirationException ex) {
            logger.warn("Session already expired, unable to proceed.", ex);
            return null ;
        } catch (JahiaForbiddenAccessException ex) {
            logger.warn("Unauthorized attempt to use gwt action menu");
            return null ;
        } catch (Exception e) {
            logger.error("Unable to process the request !", e);
            return null ;
        }
        return actions ;
    }

}
