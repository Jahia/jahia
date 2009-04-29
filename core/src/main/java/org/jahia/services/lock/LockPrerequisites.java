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
package org.jahia.services.lock;

import java.util.*;

import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentContainerListKey;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.ContentPageKey;
import org.jahia.data.containers.JahiaContainerStructure;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.workflow.WorkflowService;
import org.springframework.util.StopWatch;

/**
 * <p>Title: Jahia locking system implementation.</p>
 * <p>Description:
 * Define some methods verifying if locks can be acquirable in given contexts.
 * </p>
 * <p>Copyright: MAP (Jahia Solutions S�rl 2003)</p>
 * <p>Company: Jahia Solutions S�rl</p>
 *
 * @author MAP
 * @version 1.0
 */
public class LockPrerequisites {

    public static final String ALL_LEFT = "All tabs not allready listed";

    public static final String EDIT = "engines.content.Edit";
    public static final String METADATA = "engines.metadata.Metadata_Engine";
    public static final String RIGHTS = "engines.rights.ManageRights";
    public static final String TIME_BASED_PUBLISHING = "engines.timebasedpublishing.TimeBasedPublishingEngine";
    public static final String FIELD_RIGHTS = "engines.containerlistproperties.FieldRightsEngine";
    public static final String PUBLISH_ALL = "engines.actions.publishAll";
    public static final String IMMEDIATE_CRON = "engines.actions.immediateCron";
    public static final String URLKEY = "engines.actions.urlKey";
    public static final String HIDE_FROM_NAVIGATION_MENU = "engines.actions.hideFromNavigationMenu";

    public static final String CONTENT_PICKER = "engines.importexport.ManageContentPicker";
    public static final String VERSIONNING = "engines.versioning.PagesVersioningAction";
    public static final String IMPORT = "engines.importexport.ManageImport";
    public static final String EXPORT = "engines.importexport.ManageExport";
    public static final String LOGS = "engines.audit.ManageLogs_Engine";
    public static final String MANAGE_WORKFLOW = "engines.workflow.ManageWorkflow";

    private Cache lockPrerequisitesResultMap;
    private Cache lockAlreadyAcquiredMap;
    private static LockPrerequisites lockPrerequisites = null;

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(LockKey.class);

    /**
     * Define if a lock can be acquirable in a con
     *
     * @param lockKey     The lock key identifying the lock.
     * @param owner       The lock owner
     * @param lockID      The lock ID
     * @param justTesting true if we just want to test if the lock is acquireable
     *                    in order to report current possible actions, or false if we really want
     *                    to test this in an acquirement context. The main difference is that when
     *                    in real acquirement mode, locks will be released if we are in the same
     *                    context (ie the same user), and when justTesting=true this will not be
     *                    done.
     * @return True if the lock is acquirable, false otherwise.
     *         <p/>
     *         todo Yes, I know that the succession of "if else if else" is very
     *         disgracious ! So there would be better to put these lock verification
     *         sequences in separate classes; one for each test.
     */
    public boolean isLockAcquirable(final LockKey lockKey,
                                    final JahiaUser owner,
                                    final String lockID,
                                    final boolean justTesting) {
        final StopWatch stopWatch = new StopWatch("isLockAcquirable");
        final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
        // Is the lock already acquired in the same context ? ...
        stopWatch.start("isAlreadyAcquired");
        if (lockRegistry.isAlreadyAcquiredInContext(lockKey, owner, lockID)) {
            resetPrerequisite(lockKey); // Clear results from a previous check
            stopWatch.stop();
            return true;
            // ... or is it already acquired from another context ?
        }
        if (stopWatch.isRunning()) stopWatch.stop();
        // Let's look about the prerequisites.
        stopWatch.start("reset");
        resetPrerequisite(lockKey);
        LockPrerequisitesResult results = new LockPrerequisitesResult();
        stopWatch.stop();
        // Lock for workflow engine.
        final String type = lockKey.getType();
        stopWatch.start("verifyLock for type " + type);

        if (type.startsWith(LockKey.WORKFLOW_ACTION)) {
            ContentObjectKey obj = (ContentObjectKey) lockKey.getObjectKey();
            verifyLockForParentDelete(obj, owner, lockID, justTesting, results);
            verifyLockForWorkflow(obj, owner, lockID, justTesting, results, false, false);
            verifyLockForWorkflowGroup(obj, owner, lockID, justTesting, results);
            results.addDisabledTab(CONTENT_PICKER);
            results.addDisabledTab(IMPORT);
            results.addDisabledTab(EXPORT);
            results.addDisabledTab(VERSIONNING);
            results.addReadOnlyTab(ALL_LEFT);
            lockPrerequisitesResultMap.put(lockKey, results);
        } else if (type.startsWith(LockKey.IMPORT_ACTION)) {
            if (!lockKey.getName().equals("SITE")) {
                ContentObjectKey obj = (ContentObjectKey) lockKey.getObjectKey();
                verifyLockForParentDelete(obj, owner, lockID, justTesting, results);
                verifyLockForImport(obj, owner, lockID, justTesting, results, true, false, true);
                verifyLockForWorkflow(obj, owner, lockID, justTesting, results, false, false);
                verifyWaitingState(lockKey.getContentObject(), results);
                verifyLockForParentPage(obj, owner, lockID, justTesting, results);
                verifyLockForEditObject(obj, owner, lockID, justTesting, results);
                results.addDisabledTab(ALL_LEFT);
                lockPrerequisitesResultMap.put(lockKey, results);
            }
        } else if (type.startsWith(LockKey.EXPORT_ACTION)) {
            ContentObjectKey obj = (ContentObjectKey) lockKey.getObjectKey();
            verifyLockForParentDelete(obj, owner, lockID, justTesting, results);
            verifyLockForImport(obj, owner, lockID, justTesting, results, false, false, true);
            verifyLockForParentPage(obj, owner, lockID, justTesting, results);
            verifyLockForEditObject(obj, owner, lockID, justTesting, results);
            verifyLockForAllChildren(obj, owner, lockID, justTesting, results, true, JahiaContainerStructure.ALL_TYPES);
            results.addDisabledTab(CONTENT_PICKER);
            results.addDisabledTab(IMPORT);
            results.addDisabledTab(EXPORT);
            results.addDisabledTab(VERSIONNING);
            results.addReadOnlyTab(ALL_LEFT);
            lockPrerequisitesResultMap.put(lockKey, results);
        } else if (type.startsWith(LockKey.LIVEEXPORT_ACTION)) {
            ContentObjectKey obj = (ContentObjectKey) lockKey.getObjectKey();
            verifyLockForWorkflow(obj, owner, lockID, justTesting, results, false, true);
            results.addDisabledTab(EXPORT);
            results.addDisabledTab(VERSIONNING);
            results.addReadOnlyTab(MANAGE_WORKFLOW);
            lockPrerequisitesResultMap.put(lockKey, results);
        }
        // Lock for add container engine.
        else if (LockKey.ADD_CONTAINER_TYPE.equals(type)) {
            ContentObjectKey obj = (ContentObjectKey) lockKey.getObjectKey();
            verifyLockForParentDelete(obj, owner, lockID, justTesting, results);
            verifyLockForImport(obj, owner, lockID, justTesting, results, true, false, true);
            verifyLockForWorkflow(obj, owner, lockID, justTesting, results, false, false);
            verifyWaitingState(lockKey.getContentObject(), results);
            verifyLockForParentPage(obj, owner, lockID, justTesting, results);
            verifyLockForUpdateContainerList(obj.getIdInType(), owner, lockID, justTesting, results);
            verifyLockForMarkForDeletion(obj, results);
            results.addReadOnlyTab(ALL_LEFT);
            results.addDisabledTab(IMPORT);
            results.addDisabledTab(VERSIONNING);
            results.addDisabledTab(CONTENT_PICKER);
            lockPrerequisitesResultMap.put(lockKey, results);
        }
        // Lock for update container engine.
        else if (LockKey.UPDATE_CONTAINER_TYPE.equals(type)) {
            ContentObjectKey obj = (ContentObjectKey) lockKey.getObjectKey();
            verifyLockForParentDelete(obj, owner, lockID, justTesting, results);
            verifyLockForImport(obj, owner, lockID, justTesting, results, true, false, true);
            verifyLockForWorkflow(obj, owner, lockID, justTesting, results, false, false);
            verifyWaitingState(lockKey.getContentObject(), results);
            verifyLockForParentPage(obj, owner, lockID, justTesting, results);
            verifyLockForUpdateContainerList(obj.getParent(EntryLoadRequest.STAGED).getIdInType(), owner, lockID, justTesting, results);
            verifyLockForEditObject(obj, owner, lockID, justTesting, results);
            verifyLockForMarkForDeletion(obj, results);
            results.addReadOnlyTab(EDIT);
            results.addReadOnlyTab(METADATA);
            results.addReadOnlyTab(RIGHTS);
            results.addReadOnlyTab(EXPORT);
            results.addReadOnlyTab(TIME_BASED_PUBLISHING);
            results.addReadOnlyTab(MANAGE_WORKFLOW);
            results.addReadOnlyTab(VERSIONNING);
            results.addReadOnlyTab(LOGS);
            results.addDisabledTab(IMPORT);
            results.addDisabledTab(VERSIONNING);
            results.addDisabledTab(CONTENT_PICKER);
            lockPrerequisitesResultMap.put(lockKey, results);
        }
        // Lock for delete container engine
        else if (LockKey.DELETE_CONTAINER_TYPE.equals(type)) {
            ContentObjectKey obj = (ContentObjectKey) lockKey.getObjectKey();
            verifyLockForParentDelete(obj, owner, lockID, justTesting, results);
            verifyLockForImport(obj, owner, lockID, justTesting, results, true, false, true);
            verifyLockForWorkflow(obj, owner, lockID, justTesting, results, false, false);
            verifyWaitingState(lockKey.getContentObject(), results);
            verifyLockForParentPage(obj, owner, lockID, justTesting, results);
            verifyLockForAllChildren(obj, owner, lockID, justTesting, results, true, JahiaContainerStructure.JAHIA_CONTAINER);
            verifyLockForUpdateContainerList(obj.getParent(EntryLoadRequest.STAGED).getIdInType(), owner, lockID, justTesting, results);
            verifyLockForEditObject(obj, owner, lockID, justTesting, results);
            verifyLockForMarkForDeletion(obj, results);
            results.addReadOnlyTab(EDIT);
            results.addReadOnlyTab(METADATA);
            results.addReadOnlyTab(RIGHTS);
            results.addReadOnlyTab(TIME_BASED_PUBLISHING);
            results.addReadOnlyTab(MANAGE_WORKFLOW);
            results.addReadOnlyTab(VERSIONNING);
            results.addReadOnlyTab(LOGS);
            results.addReadOnlyTab(EXPORT);
            results.addDisabledTab(CONTENT_PICKER);
            results.addDisabledTab(VERSIONNING);
            results.addDisabledTab(IMPORT);
            lockPrerequisitesResultMap.put(lockKey, results);
        }
        // Lock for update field engine.
        else if (LockKey.UPDATE_FIELD_TYPE.equals(type)) {
            ContentObjectKey obj = (ContentObjectKey) lockKey.getObjectKey();
            verifyLockForParentDelete(obj, owner, lockID, justTesting, results);
            verifyLockForEditObject(obj, owner, lockID, justTesting, results);
            verifyLockForImport(obj, owner, lockID, justTesting, results, true, false, true);
            verifyLockForWorkflow(obj, owner, lockID, justTesting, results, false, false);
            verifyWaitingState(lockKey.getContentObject(), results);
            verifyLockForParentPage(obj, owner, lockID, justTesting, results);
            verifyLockForMarkForDeletion(obj, results);
            results.addReadOnlyTab(EDIT);
            results.addReadOnlyTab(METADATA);
            results.addReadOnlyTab(RIGHTS);
            results.addReadOnlyTab(FIELD_RIGHTS);
            results.addReadOnlyTab(TIME_BASED_PUBLISHING);
            results.addReadOnlyTab(MANAGE_WORKFLOW);
            results.addReadOnlyTab(VERSIONNING);
            results.addReadOnlyTab(LOGS);
            results.addDisabledTab(IMPORT);
            results.addDisabledTab(VERSIONNING);
            lockPrerequisitesResultMap.put(lockKey, results);
        }
        // Lock for update container list engine.
        else if (LockKey.UPDATE_CONTAINERLIST_TYPE.equals(type)) {
            ContentObjectKey obj = (ContentObjectKey) lockKey.getObjectKey();
            verifyLockForParentDelete(obj, owner, lockID, justTesting, results);
            verifyLockForImport(obj, owner, lockID, justTesting, results, true, false, true);
            verifyLockForWorkflow(obj, owner, lockID, justTesting, results, false, false);
            verifyLockForParentPage(obj, owner, lockID, justTesting, results);
            verifyLockForAllChildren(obj, owner, lockID, justTesting, results, false, JahiaContainerStructure.JAHIA_CONTAINER);
            verifyLockForEditObject(obj, owner, lockID, justTesting, results);
            verifyLockForMarkForDeletion(obj, results);
            verifyWaitingState(lockKey.getContentObject(), results);
            results.addReadOnlyTab(EDIT);
            results.addReadOnlyTab(METADATA);
            results.addReadOnlyTab(RIGHTS);
            results.addReadOnlyTab(FIELD_RIGHTS);
            results.addReadOnlyTab(TIME_BASED_PUBLISHING);
            results.addReadOnlyTab(MANAGE_WORKFLOW);
            results.addReadOnlyTab(VERSIONNING);
            results.addReadOnlyTab(LOGS);
            results.addDisabledTab(CONTENT_PICKER);
            results.addDisabledTab(IMPORT);
            results.addDisabledTab(VERSIONNING);
            lockPrerequisitesResultMap.put(lockKey, results);
        }
        // Lock for update page engine.
        else if (LockKey.UPDATE_PAGE_TYPE.equals(type)) {
            ContentObjectKey obj = (ContentObjectKey) lockKey.getObjectKey();
            verifyLockForParentDelete(obj, owner, lockID, justTesting, results);
            verifyLockForImport(obj, owner, lockID, justTesting, results, true, false, true);
            verifyLockForWorkflow(obj, owner, lockID, justTesting, results, false, false);
            verifyWaitingState(lockKey.getContentObject(), results);
            verifyLockForEditObject(obj, owner, lockID, justTesting, results);
            verifyLockForAllChildren(obj, owner, lockID, justTesting, results, false, JahiaContainerStructure.ALL_TYPES);
            verifyLockForMarkForDeletion(obj, results);
            results.addReadOnlyTab(EDIT);
            results.addReadOnlyTab(METADATA);
            results.addReadOnlyTab(RIGHTS);
            results.addReadOnlyTab(FIELD_RIGHTS);
            results.addReadOnlyTab(TIME_BASED_PUBLISHING);
            results.addReadOnlyTab(MANAGE_WORKFLOW);
            results.addReadOnlyTab(VERSIONNING);
            results.addReadOnlyTab(LOGS);
            results.addDisabledTab(VERSIONNING);
            lockPrerequisitesResultMap.put(lockKey, results);

        } else if (LockKey.RESTORE_LIVE_CONTAINER_TYPE.equals(type)) {
            ContentObjectKey obj = (ContentObjectKey) lockKey.getObjectKey();
            verifyLockForRestoreLiveContent(obj, owner, lockID, justTesting, results);
            lockPrerequisitesResultMap.put(lockKey, results);
        }
        stopWatch.stop();
        return results.size() <= 0;
    }

    public LockPrerequisitesResult getLockPrerequisitesResult(LockKey lockKey) {
        return (LockPrerequisitesResult) lockPrerequisitesResultMap.get(lockKey);
    }

    public boolean isLockAlreadyAcquired(LockKey lockKey) {
        Map isAlreadyAcquired = (Map) lockAlreadyAcquiredMap.get(lockKey);
        return isAlreadyAcquired != null && isAlreadyAcquired.containsKey(lockKey.getAction())
                && !((Set) isAlreadyAcquired.get(lockKey.getAction())).isEmpty();
    }

    public void flush() {
        try {
            lockAlreadyAcquiredMap.flush();
            lockPrerequisitesResultMap.flush();
        } catch (IllegalStateException e) {
            // This informs us that the cache is not available so do nothing we are shutdowning
        }
    }

    public static LockPrerequisites getInstance() {
        if (lockPrerequisites == null) {
            lockPrerequisites = new LockPrerequisites();
        }
        return lockPrerequisites;
    }

    protected synchronized void resetPrerequisite(LockKey lockKey) {
        lockPrerequisitesResultMap.remove(lockKey);
        lockAlreadyAcquiredMap.remove(lockKey.getObjectKey());
//        lockAlreadyAcquiredMap.remove(new GroupCacheKey(lockKey, new ArrayList()));
    }

    private void verifyLockForParentDelete(ContentObjectKey object,
                                           JahiaUser owner, String context, boolean justTesting, LockPrerequisitesResult results) {

        while (object != null) {
            if (object.getType().equals(ContentContainerKey.CONTAINER_TYPE)) {
                LockKey lockKey = LockKey.composeLockKey(LockKey.DELETE_ACTION + "_" + object.getType(), object.getIdInType());
                putLockIfNotSameContext(lockKey, owner, context, justTesting, results, false);
            }
            object = object.getParent(EntryLoadRequest.STAGED);
        }
    }

    private void verifyLockForMarkForDeletion(final ContentObjectKey objectKey,
                                              final LockPrerequisitesResult results) {
        if (!org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects()) return;
        try {
            final ContentObject theObject = ContentObject.getContentObjectInstance(objectKey);
            if (theObject.isMarkedForDelete()) {
                final LockKey lockKey = LockKey.composeLockKey(LockKey.MARK_FOR_DELETE_TYPE, objectKey.getIdInType());
                results.put(lockKey);
                lockPrerequisitesResultMap.put(lockKey, results);
            }
        } catch (final Exception e) {
            logger.warn("Error in verifyLockForMarkForDeletion", e);
        }
    }

    private void verifyLockForRestoreLiveContent(final ContentObjectKey objectKey,
                                                 final JahiaUser owner,
                                                 final String context,
                                                 final boolean justTesting,
                                                 LockPrerequisitesResult results) {
        try {
            final ContentObject theObject = ContentObject.getContentObjectInstance(objectKey);
            if (theObject.isMarkedForDelete()) {
                final LockKey lockKey = LockKey.composeLockKey(LockKey.RESTORE_LIVE_CONTAINER_TYPE, objectKey.getIdInType());
                putLockIfNotSameContext(lockKey, owner, context, justTesting, results, false);
            }
        } catch (final Exception e) {
            logger.warn("Error in verifyLockForRestoreLiveContent", e);
        }
    }

    private void verifyLockForAllChildren(ContentObjectKey object,
                                          JahiaUser owner, String context, boolean justTesting,
                                          LockPrerequisitesResult results, boolean recurse, int loadFlag) {
        if (recurse) {
            String[] actions = {LockKey.UPDATE_ACTION, LockKey.ADD_ACTION, LockKey.DELETE_ACTION};

            for (int i = 0; i < actions.length; i++) {
                String action = actions[i];
                Set s = LockRegistry.getInstance().getLockKeys(action);
                for (Iterator iterator = s.iterator(); iterator.hasNext();) {
                    LockKey lockKey = (LockKey) iterator.next();
                    ContentObjectKey k = (ContentObjectKey) lockKey.getObjectKey();
                    while (k != null) {
                        if (k.equals(object)) {
                            putLockIfNotSameContext(lockKey, owner, context, justTesting, results, false);
                            break;
                        }
                        k = k.getParent(EntryLoadRequest.STAGED);
                    }
                }
            }
        } else {
            if (object == null) {
                return;
            }
            Collection c = object.getChilds(EntryLoadRequest.STAGED, loadFlag);
            for (Iterator iterator = c.iterator(); iterator.hasNext();) {
                ContentObjectKey child = (ContentObjectKey) iterator.next();
                verifyLockForEditObject(child, owner, context, justTesting, results);
                if (!(child instanceof ContentPageKey)) {
                    verifyLockForAllChildren(child, owner, context, justTesting, results, recurse, loadFlag);
                }
            }
        }
    }

    private void verifyLockForWorkflowGroup(ContentObjectKey object,
                                            JahiaUser owner, String context, boolean justTesting,
                                            LockPrerequisitesResult results) {
        final ServicesRegistry instance = ServicesRegistry.getInstance();
        try {
            WorkflowService workflowService = instance.getWorkflowService();
            verifyLockForImport(workflowService.getMainLinkObject(object), owner, context, justTesting, results, false, true, true);
            List linked = workflowService.getLinkedContentObjects(object, false);
            for (Iterator iterator = linked.iterator(); iterator.hasNext();) {
                ContentObjectKey linkedObject = (ContentObjectKey) iterator.next();
                LockKey workflowLockKey = LockKey.composeLockKey(LockKey.WORKFLOW_ACTION + "_" + linkedObject.getType(), linkedObject.getIdInType());
                putLockIfNotSameContext(workflowLockKey, owner, context, justTesting, results, false);
                verifyLockForEditObject(linkedObject, owner, context, justTesting, results);
                verifyLockForImport(linkedObject, owner, context, justTesting, results, false, true, false);
            }
        } catch (JahiaException je) {
            logger.warn("Problem when iterate through page childs to get a lock !", je);
        }
    }

    private void verifyLockForEditObject(final ContentObjectKey object,
                                         final JahiaUser owner,
                                         final String context,
                                         final boolean justTesting,
                                         LockPrerequisitesResult results) {

        LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_ACTION + "_" + object.getType(), object.getIdInType());
        putLockIfNotSameContext(lockKey, owner, context, justTesting, results, false);

        if (object instanceof ContentContainerListKey) {
            ContentContainerListKey containerList = (ContentContainerListKey) object;
            lockKey = LockKey.composeLockKey(LockKey.ADD_CONTAINER_TYPE, containerList.getIdInType());
            putLockIfNotSameContext(lockKey, owner, context, justTesting, results, false);
        } else if (object instanceof ContentContainerKey) {
            ContentContainerKey contentContainer = (ContentContainerKey) object;
            LockKey deleteContainerLockKey = LockKey.composeLockKey(LockKey.DELETE_CONTAINER_TYPE, contentContainer.getIdInType());
            putLockIfNotSameContext(deleteContainerLockKey, owner, context, justTesting, results, false);
        }
    }

    private void verifyLockForWorkflow(final ContentObjectKey object,
                                       final JahiaUser owner,
                                       final String context,
                                       final boolean justTesting,
                                       LockPrerequisitesResult results,
                                       final boolean recurse,
                                       final boolean checkNoWorkflow) {
        try {
            if (recurse) {
                Set s = LockRegistry.getInstance().getLockKeys(LockKey.WORKFLOW_ACTION);
                for (Iterator iterator = s.iterator(); iterator.hasNext();) {
                    LockKey lockKey = (LockKey) iterator.next();
                    ContentObjectKey k = (ContentObjectKey) lockKey.getObjectKey();
                    while (k != null) {
                        if (k.equals(object)) {
                            putLockIfNotSameContext(lockKey, owner, context, justTesting, results, false);
                            break;
                        }
                        k = k.getParent(EntryLoadRequest.STAGED);
                    }
                }
            } else {
                WorkflowService workflowService = ServicesRegistry.getInstance().getWorkflowService();
                ContentObjectKey main = workflowService.getMainLinkObject(object);

                LockKey workflowLockKey = LockKey.composeLockKey(LockKey.WORKFLOW_ACTION + "_" + main.getType(), main.getIdInType());
                putLockIfNotSameContext(workflowLockKey, owner, context, justTesting, results, false);

                if (checkNoWorkflow) {
                    boolean isInactive = workflowService.getInheritedMode(object) == WorkflowService.INACTIVE;
                    if (isInactive) {
                        verifyLockForWorkflowGroup(object, owner, context, justTesting, results);
                    }
                }
            }
        } catch (JahiaException e) {
            logger.warn("Cannot get workflow objects", e);
        }
    }

    private void verifyWaitingState(ContentObject object, LockPrerequisitesResult results) {
        try {
            ContentObjectKey key = (ContentObjectKey) object.getObjectKey();
            key = ServicesRegistry.getInstance().getWorkflowService().getMainLinkObject(key);
            final LockKey workflowLockKey = LockKey.composeLockKey(LockKey.WAITING_FOR_APPROVAL_TYPE, key.getIdInType());
            if (!results.getResultsList().contains(workflowLockKey)) {
                final Map languagesStates = ServicesRegistry.getInstance().getWorkflowService().getLanguagesStates(object);
                final Iterator languageIt = languagesStates.keySet().iterator();
                boolean hasPageAWaitingState = false;
                while (languageIt.hasNext()) {
                    final String language = (String) languageIt.next();
                    final Integer languageState = (Integer) languagesStates.get(language);
                    if (languageState.intValue() == EntryLoadRequest.WAITING_WORKFLOW_STATE) {
                        results.addLanguage(language);
                        hasPageAWaitingState = true;
                    }
                }
                if (hasPageAWaitingState) {
                    results.put(workflowLockKey);
                    lockPrerequisitesResultMap.put(workflowLockKey, results);
                }
            }
        } catch (JahiaException e) {
            logger.warn("Problem when getting languages states", e);
        }
    }

    private void verifyLockForUpdateContainerList(int containerListID, JahiaUser owner, String context, boolean justTesting, LockPrerequisitesResult results) {
        // Verify if the parent container list is not edited.
        LockKey updateContainerListLockKey = LockKey.composeLockKey(LockKey.UPDATE_CONTAINERLIST_TYPE, containerListID);
        putLockIfNotSameContext(updateContainerListLockKey, owner, context, justTesting, results, false);
    }

    private void verifyLockForParentPage(ContentObjectKey obj, JahiaUser owner, String context, boolean justTesting, LockPrerequisitesResult results) {
        // Verify if the parent page properties are not edited. Template can change.
        // This test is debatable, perhaps should we refine it in the page properties
        // engine and let the user modify the page title only.
        while (!(obj instanceof ContentPageKey)) {
            obj = obj.getParent(EntryLoadRequest.STAGED);
        }
        LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_PAGE_TYPE, obj.getIdInType());
        putLockIfNotSameContext(lockKey, owner, context, justTesting, results, false);
    }

    private void verifyLockForImport(ContentObjectKey object, JahiaUser owner, String context, boolean justTesting,
                                     LockPrerequisitesResult results, boolean checkExport, boolean checkLiveExport, boolean recurse) {

        LockKey importLockKey = LockKey.composeLockKey(LockKey.IMPORT_ACTION + "_" + object.getType(), object.getIdInType());
        putLockIfNotSameContext(importLockKey, owner, context, justTesting, results, true);

        if (checkExport) {
            LockKey exportLockKey = LockKey.composeLockKey(LockKey.EXPORT_ACTION + "_" + object.getType(), object.getIdInType());
            putLockIfNotSameContext(exportLockKey, owner, context, justTesting, results, true);
        }

        try {
            if (!checkLiveExport) {
                checkLiveExport = ServicesRegistry.getInstance().getWorkflowService().getInheritedMode(object) == WorkflowService.INACTIVE;
            }
        } catch (JahiaException e) {
            logger.error("Cannot get workflow mode", e);
        }

        if (checkLiveExport) {
            LockKey exportLockKey = LockKey.composeLockKey(LockKey.LIVEEXPORT_ACTION + "_" + object.getType(), object.getIdInType());
            putLockIfNotSameContext(exportLockKey, owner, context, justTesting, results, true);
        }

        if (recurse) {
            ContentObjectKey parent = object.getParent(EntryLoadRequest.STAGED);
            if (parent != null) {
                verifyLockForImport(parent, owner, context, justTesting, results, checkExport, checkLiveExport, true);
            }
        }
    }

    private void putLockIfNotSameContext(LockKey lockKey,
                                         JahiaUser owner,
                                         String context,
                                         boolean justTesting,
                                         LockPrerequisitesResult results,
                                         boolean putInResultMap) {
        Set s = LockRegistry.getInstance().getContexts(lockKey);
        if (s.contains(context)) {
            if (!justTesting) {
                LockRegistry.getInstance().release(lockKey, owner, context);
            }
        } else if (!s.isEmpty()) {
            if (!results.getResultsList().contains(lockKey)) {
                // From another context then no way to obtain it.
                results.put(lockKey);
                if (putInResultMap) lockPrerequisitesResultMap.put(lockKey, results);
            }
        }
    }

    private LockPrerequisites() {
    }

    private CacheService cacheService;

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
        try {
            lockPrerequisitesResultMap = cacheService.createCacheInstance("LockPrerequisitesResultMap");
            lockAlreadyAcquiredMap = cacheService.createCacheInstance("LockAlreadyAcquiredMap");
        } catch (JahiaException je) {
            logger.error("Error while creating lock cache", je);
        }
    }
}
