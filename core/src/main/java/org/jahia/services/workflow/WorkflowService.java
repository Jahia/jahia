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
package org.jahia.services.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentFieldKey;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.ContentPageKey;
import org.jahia.content.JahiaObject;
import org.jahia.content.NodeOperationResult;
import org.jahia.content.ObjectKey;
import org.jahia.content.PageReferenceableInterface;
import org.jahia.content.events.ContentActivationEvent;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaLanguagesStatesManager;
import org.jahia.hibernate.manager.JahiaWorkflowManager;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaListenersRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.security.license.LicenseActionChecker;
import org.jahia.services.JahiaService;
import org.jahia.services.audit.LoggingEventListener;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.GroupCacheKey;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.fields.ContentField;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.version.StateModificationContext;
import org.jahia.settings.SettingsBean;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: May 26, 2003
 * Time: 11:59:40 AM
 * To change this template use Options | File Templates.
 */
public class WorkflowService extends JahiaService {
    /** logging */
    final static private org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (WorkflowService.class);

    public static final int INACTIVE = 0;
    public static final int JAHIA_INTERNAL = 1;
    public static final int EXTERNAL = 2;
    public static final int INHERITED = 3;
    public static final int LINKED = 4;

    public static String FIELD_KEY = "objectKey";
    public static String FIELD_MODE= "mode";
    public static String FIELD_EXTERNAL_NAME = "externalname";
    public static String FIELD_EXTERNAL_PROCESS = "externalprocess";
    public static String FIELD_MAIN = "main";

    private static WorkflowService mInstance;

    private Map<String, ExternalWorkflow> externals;
    JahiaWorkflowManager workflowManager;
    JahiaLanguagesStatesManager languagesStatesManager;
    public static final String WORKFLOWLANGUAGESSTATES_CACHENAME = "WorkflowServiceCache";
    public static final String WORKFLOWMODE_CACHENAME = "WorkflowModeCache";
    public static final String WORKFLOWHARDLINKEDOBJECTS_CACHENAME = "WorkflowHardLinkedObjectsCache";
    public static final String WORKFLOWMAINLINKEDOBJECT_CACHENAME = "WorkflowMainLinkedObject";
    public static final String WORKFLOWLINKEDOBJECT_CACHENAME = "WorkflowLinkedObject";
    public static final String WORKFLOWSERVICE_KEYPREFIX = "WorkflowLanguageStates_";
    public static final String WORKFLOWMODE_KEYPREFIX = "WorkflowMode_";
    public static final String WORKFLOWSERVICESITE_KEYPREFIX = "WorkflowServiceSite_";
    public static final String WORKFLOWLINKEDOBJECT_KEYPREFIX = "WorkflowLinkedObject_";
    public static final String WORKFLOWHARDLINKEDOBJECT_KEYPREFIX = "WorkflowHardLinkedObject_";
    public static final String MAINPREFIX = "Main_";
    private CacheService cacheService = null;
    private Cache<GroupCacheKey, Map<String, Integer>> cache;
    private Cache<String, Integer> modeCache;
//    private Cache hardLinkedObjectsCache;
//    private Cache mainLinkedObjectsCache;
    private Cache<GroupCacheKey, List<ContentObjectKey>[]> linkedObjectsCache;
    private EntryLoadRequest loadRequest;

    public void setWorkflowManager(JahiaWorkflowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    public void setLanguagesStatesManager(JahiaLanguagesStatesManager languagesStatesManager) {
        this.languagesStatesManager = languagesStatesManager;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    protected WorkflowService() {
    }

    public static WorkflowService getInstance ()
    {
        if (mInstance == null)
        {
            mInstance = new WorkflowService ();
        }
        return mInstance;
    }

    /** Initialize the service. Override this method if specific initialization
     *  is needed to start the service.
     *
     * @exception   JahiaInitializationException
     *      Thows this exception on any failure.
     */
    public synchronized void start()
            throws JahiaInitializationException {
        loadRequest = new EntryLoadRequest(EntryLoadRequest.STAGED);
        loadRequest.setWithMarkedForDeletion(true);
        cache = cacheService.createCacheInstance(WORKFLOWLANGUAGESSTATES_CACHENAME);
        modeCache = cacheService.createCacheInstance(WORKFLOWMODE_CACHENAME);
        linkedObjectsCache = cacheService.createCacheInstance(WORKFLOWLINKEDOBJECT_CACHENAME);
    }

    public void stop() {
    }

    public void setExternals(Map<String, ExternalWorkflow> map) {
        externals = map;
    }

    private ContentObjectKey getParent(ContentObjectKey object) throws JahiaException {
        ContentObjectKey parent = object.getParent(loadRequest);
        if (parent == null) {
            ContentObject o  = ContentObject.getContentObjectFromMetadata(object);
            if (o != null) {
                parent = (ContentObjectKey) o.getObjectKey();
            }
        }
        if (parent == null && (object instanceof ContentFieldKey)) {
            ContentField contentField = ContentField.getField(object.getIdInType());
            if ( contentField != null && contentField.isMetadata() ) {
                ContentObject contentObject = ContentObject.getContentObjectFromMetadata(object);
                if ( contentObject != null ){
                    parent = (ContentObjectKey)contentObject.getObjectKey();
                }
            }
        }
        return parent;
    }

    private int getPageType(ContentPageKey k) throws JahiaException {
        if (k.getPageType() != -1) {
            return k.getPageType();
        } else {
            return ContentPage.getPage(k.getIdInType()).getPageType(null);
        }
    }

    public int getWorkflowMode(ContentObject object)
            throws JahiaException {
        return getWorkflowMode((ContentObjectKey) object.getObjectKey());
    }
    /**
     * Return the type of workflow used for a specified object in the tree. This method does not return the
     * effective workflow mode, which can be obtained after inheritance resolution.
     *
     * If none has been defined, default values for a page are JAHIA_INTERNAL if root page, INHERITED otherwise -
     * for other object type, workflow is LINKED to the parent.
     *
     * @param object
     * @return workflow mode
     * @throws JahiaException
     */
    private int getWorkflowMode(ContentObjectKey object, boolean cache)
            throws JahiaException {

        Integer mode = null;
        mode = modeCache.get(object.toString());
        int modeValue;
        if (mode == null) {
            ContentObjectKey mainObject = getHardLinkedMainObject(object);
            modeValue = getWorkflowMode(object, mainObject);
            if (cache) {
                modeCache.put(object.toString(), new Integer(modeValue));
            }
        } else {
            modeValue = mode.intValue();
        }
        return modeValue;
    }

    public int getWorkflowMode(ContentObjectKey object)
            throws JahiaException {
        return getWorkflowMode(object, true);
    }

    private int getWorkflowMode(ContentObjectKey object,ContentObjectKey mainObject)
            throws JahiaException {

        if (ContentFieldKey.FIELD_TYPE.equals(mainObject.getType())) {
            return LINKED;
        }

        Map<String, Object> map = getDbEntry(mainObject);
        Integer mode = (Integer)map.get(FIELD_MODE);

        if (mode == null) {
            int modei = evalWorkflowMode(object, mainObject);
            if (mainObject.getParent(loadRequest)!= null) {
                if (map.isEmpty()) {
                    workflowManager.createWorkflowEntry(mainObject.toString(), modei, null, null);
                } else {
                    workflowManager.updateWorkflowEntry(mainObject.toString(), modei, null, null);
                }
            }
            return modei;
        } else {
            return mode.intValue();
        }
    }

    private int evalWorkflowMode(ContentObjectKey object, ContentObjectKey mainObject) throws JahiaException {
        ContentObjectKey parent = getParent(mainObject);
        if (parent != null) {
            if (ContentPageKey.PAGE_TYPE.equals(mainObject.getType())) {
                if (getPageType((ContentPageKey) mainObject) == ContentPage.TYPE_DIRECT) {
                    ContentObjectKey pp = parent.getParent(loadRequest);
                    if (pp != null && ContentContainerKey.CONTAINER_TYPE.equals(pp.getType())) {
                        pp = pp.getParent(loadRequest);
                    }
                    try {
                        if (pp != null) {
                            ContentObject contentObject = ((ContentObject) ContentObject.getInstance(pp));
                            if (contentObject != null && contentObject.getPickedObject() != null) {
                                return LINKED;
                            } else {
                                return INHERITED;
                            }
                        } else {
                            return JAHIA_INTERNAL;
                        }
                    } catch (ClassNotFoundException e) {
                        return INHERITED;
                    }
                } else {
                    return LINKED;
                }
            } else {
                return LINKED;
            }
        } else {
            if (ContentObject.getContentObjectFromMetadata(object) != null) {
                return LINKED;
            }
            return JAHIA_INTERNAL;
        }
    }

    public boolean isModeDifferentFromDefault(ContentObjectKey object) throws JahiaException {
        if (ContentFieldKey.FIELD_TYPE.equals(object.getType())) { return false; }
        Map<String, Object> m = getDbEntry(object);
        Integer mode = (Integer) m.get(WorkflowService.FIELD_MODE);
        if (mode != null) {
            if (mode.intValue() == EXTERNAL || mode.intValue() == INACTIVE) {
                return true;
            }
            int i = evalWorkflowMode(object, object);
            if (i != mode.intValue()) {
                return true;
            }
        }
        return false;
    }

    public int getInheritedMode(ContentObject object)
            throws JahiaException {
        return getInheritedMode((ContentObjectKey) object.getObjectKey());
    }

    /**
     * Get the effective workflow mode, after inheritance resolution.
     *
     * @param object
     * @return
     * @throws JahiaException
     */
    public int getInheritedMode(ContentObjectKey object) throws JahiaException {
        int mode = getWorkflowMode(object);
        if (mode == INHERITED || mode == LINKED) {
            ContentObjectKey parent = getParent(object);
            if (parent == null) {
                return INACTIVE;
            }

            return getInheritedMode(parent);
        }
        return mode;
    }

    public String getExternalWorkflowName(ContentObject object)
            throws JahiaException {
        return getExternalWorkflowName((ContentObjectKey) object.getObjectKey());
    }

    public String getExternalWorkflowName(ContentObjectKey object)
            throws JahiaException {
        return (String) getDbEntry(object).get(FIELD_EXTERNAL_NAME);
    }

    public String getInheritedExternalWorkflowName(ContentObject object)
            throws JahiaException {
        return getInheritedExternalWorkflowName((ContentObjectKey) object.getObjectKey());
    }

    public String getInheritedExternalWorkflowName(ContentObjectKey object)
            throws JahiaException {
        int mode = getWorkflowMode(object);
        if (mode == INHERITED || mode == LINKED) {
            return getInheritedExternalWorkflowName(getParent(object));
        }
        return getExternalWorkflowName(object);
    }

    public String getExternalWorkflowProcessId(ContentObject object)
            throws JahiaException {
        return getExternalWorkflowProcessId((ContentObjectKey) object.getObjectKey());
    }

    public String getExternalWorkflowProcessId(ContentObjectKey object)
            throws JahiaException
    {
        return (String) getDbEntry(object).get(FIELD_EXTERNAL_PROCESS);
    }

    public String getInheritedExternalWorkflowProcessId(ContentObject object)
            throws JahiaException {
        return getInheritedExternalWorkflowProcessId((ContentObjectKey) object.getObjectKey());
    }

    public String getInheritedExternalWorkflowProcessId(ContentObjectKey object)
            throws JahiaException {
        int mode = getWorkflowMode(object);
        if (mode == INHERITED || mode == LINKED) {
            return getInheritedExternalWorkflowProcessId(getParent(object));
        }
        return getExternalWorkflowProcessId(object);
    }

    public JahiaGroup getRoleGroup(ContentObject object, String role, boolean create) throws JahiaException{
        return getRole((ContentObjectKey) object.getObjectKey(), role, create).getGroup();
    }

    public WorkflowRole getRole(ContentObject object, String role, boolean create) throws JahiaException{
        return getRole((ContentObjectKey) object.getObjectKey(), role, create);
    }

    public WorkflowRole getRole(ContentObjectKey object, String role, boolean create) throws JahiaException{
        ContentObjectKey main = getMainLinkObject(object);
        JahiaGroup grp = ServicesRegistry.getInstance().
                getJahiaGroupManagerService().
                lookupGroup(0,
                        "workflowrole_" + main + "_" + role);
        if (grp == null && create) {
            // create group
            grp = ServicesRegistry.getInstance().
                    getJahiaGroupManagerService().
                    createGroup(0, "workflowrole_" + main + "_" + role, new Properties(), true);
        }
        WorkflowRole r = new WorkflowRole(role, grp, main);

        int mode = -1;
        ContentObjectKey current = main;
        do {
            current = getParent(current);
            if (current != null) {
                mode = getWorkflowMode(current);
            }
        } while (current != null && main.equals(getMainLinkObject(current)));
        if (current != null) {
            WorkflowRole inherited = getRole(current, role, false);
            r.addPrincipal(inherited);
        }

        return r;
    }

    public void setWorkflowMode(ContentObject contentObject, int mode, String workflowName, String processId, ProcessingContext jParams) throws JahiaException {
        ContentObjectKey object = (ContentObjectKey)contentObject.getObjectKey();

        ContentObjectKey workflowMainObject = getHardLinkedMainObject(object);
        int oldMode = getWorkflowMode(object);
        String oldName = getExternalWorkflowName(contentObject);
        String oldId = getExternalWorkflowProcessId(contentObject);
        if (oldMode == mode &&
                ((oldName == null && workflowName == null) || (oldName != null && oldName.equals(workflowName))) &&
                ((oldId == null && processId == null) || (oldId != null && oldId.equals(processId)))) {
            return;
        }
        cache.flush();

        List<ContentObjectKey> all = null;
        if (oldMode != WorkflowService.LINKED) {
            all = getAllInheritedObjects(object);
        }

        ContentObjectKey oldMainObject = getMainLinkObject(object);
        boolean detach = false;
        int oldInheritedMode = getInheritedMode(object);

        if (oldInheritedMode == WorkflowService.EXTERNAL) {
            if (oldMode != WorkflowService.LINKED) {
                String name = getInheritedExternalWorkflowName(object);
                ExternalWorkflow workflow = getExternalWorkflow(name);
                String inheritedExternalWorkflowProcessId = getInheritedExternalWorkflowProcessId(workflowMainObject);
                for (Iterator<ContentObjectKey> iterator = all.iterator(); iterator.hasNext();) {
                    ContentObjectKey current = iterator.next();
                    ContentObjectKey currentMain = getMainLinkObject(current);
                    Set<String> languages = getStagingLanguages(current, jParams.getSiteID());
                    for (Iterator<String> iterator2 = languages.iterator(); iterator2.hasNext();) {
                        String lang = iterator2.next();
                        workflow.abortProcess(inheritedExternalWorkflowProcessId,currentMain.toString(), lang, jParams);
                    }
                }
            } else {
                // Detach a group from old main group
                detach = true;
            }
        }

        if (mode == LINKED) {
            workflowManager.clearMainObject(workflowMainObject.toString());
            languagesStatesManager.clearEntries(oldMainObject.toString());
        } else if (oldMode == LINKED) {
            workflowManager.clearMainObject(oldMainObject.toString());
        }

        if (mode == LINKED || oldMode == LINKED) {
            flushCacheForObjectChanged(object);
        }

        boolean insert = getDbEntry(object).isEmpty();
        if (insert) {
            workflowManager.createWorkflowEntry(object.toString(), mode, workflowName, processId);
        } else {
            workflowManager.updateWorkflowEntry(object.toString(), mode, workflowName, processId);
        }

        modeCache.flush();

        int newMode = getInheritedMode(object);

        if (all == null) {
            all = getAllInheritedObjects(object);
        }
        Set<ContentObjectKey> toValidate = new HashSet<ContentObjectKey>();

        if (newMode == WorkflowService.INACTIVE) {
            for (Iterator<ContentObjectKey> iterator = all.iterator(); iterator.hasNext();) {
                ContentObjectKey current = iterator.next();
                toValidate.add(getMainLinkObject(current));
            }
        } else if (newMode == WorkflowService.EXTERNAL) {
            for (Iterator<ContentObjectKey> iterator = all.iterator(); iterator.hasNext();) {
                ContentObjectKey current = iterator.next();
                ExternalWorkflow workflow = getExternalWorkflow(getInheritedExternalWorkflowName(current));
                Set<String> languages = getStagingLanguages(current, jParams.getSiteID());
                changeStagingStatus(current, languages,EntryLoadRequest.STAGING_WORKFLOW_STATE, new StateModificationContext(current, languages), jParams, false);
                for (Iterator<String> iterator2 = languages.iterator(); iterator2.hasNext();) {
                    String lang = iterator2.next();
                    ContentObjectKey currentMainObject = getMainLinkObject(current);
                    String newProcessId = getInheritedExternalWorkflowProcessId(current);
                    if (!workflow.isProcessStarted(newProcessId,currentMainObject.toString(), lang)) {
                        workflow.initProcess(newProcessId,currentMainObject.toString(), lang, jParams);
                    }
                }
            }
        }

        if (!toValidate.isEmpty()) {
            Set<String> languages = new HashSet<String>();
            for (Iterator<Locale> iterator = jParams.getSite().getLanguageSettingsAsLocales(true).iterator(); iterator.hasNext();) {
                languages.add(iterator.next().toString());
            }
            Class<SimpleActivationJob> jobClass = SimpleActivationJob.class;
            JobDetail jobDetail = BackgroundJob.createJahiaJob("Activating", jobClass, jParams);
            JobDataMap jobDataMap;
            jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.put(SimpleActivationJob.KEYS, toValidate);
            jobDataMap.put(SimpleActivationJob.LANGS, languages);
            jobDataMap.put(BackgroundJob.JOB_TYPE, AbstractActivationJob.WORKFLOW_TYPE);

            LockRegistry lockReg = LockRegistry.getInstance();
            Set<LockKey> locks = new HashSet<LockKey>();
            for (Iterator<ContentObjectKey> iterator = toValidate.iterator(); iterator.hasNext();) {
                ObjectKey objectKey = iterator.next();
                LockKey lockKey = LockKey.composeLockKey(LockKey.WORKFLOW_ACTION + "_" +
                        objectKey.getType(), objectKey.getIdInType());
                locks.add(lockKey);
                synchronized(lockReg) {
                    lockReg.release(lockKey, jParams.getUser(), jParams.getUser().getUserKey());
                    lockReg.acquire(lockKey, jParams.getUser(), jobDetail.getName(), BackgroundJob.getMaxExecutionTime(), false);
                }
            }
            jobDataMap.put(BackgroundJob.JOB_LOCKS, locks);

            SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();
            schedulerServ.scheduleJobNow(jobDetail);
        }

        if (detach) {
            String name = getInheritedExternalWorkflowName(oldMainObject);
            ExternalWorkflow workflow = getExternalWorkflow(name);
            String inheritedExternalWorkflowProcessId = getInheritedExternalWorkflowProcessId(oldMainObject);

            Map<String, Integer> oldMainLanguagesStates = getLanguagesStates(oldMainObject, jParams.getSiteID());
            Integer oldSharedLanguageState = oldMainLanguagesStates.remove(ContentObject.SHARED_LANGUAGE);
            for (String lang : oldMainLanguagesStates.keySet()) {
                Integer state = oldMainLanguagesStates.get(lang);
                if ( oldSharedLanguageState != null && state.intValue() < oldSharedLanguageState.intValue() ){
                    state = oldSharedLanguageState;
                }
                if (state.intValue() < EntryLoadRequest.STAGING_WORKFLOW_STATE) {
                    if (workflow.isProcessStarted(inheritedExternalWorkflowProcessId,oldMainObject.toString(), lang)) {
                        workflow.abortProcess(inheritedExternalWorkflowProcessId,oldMainObject.toString(), lang, jParams);
                    }
                }
            }
        }
        if (oldMode == LINKED || mode == LINKED) {
            ServicesRegistry.getInstance().getJahiaSiteMapService().resetSiteMap();

            List<Locale> locales = jParams.getSite().getLanguageSettingsAsLocales(true);
            for (Locale locale : locales) {
                WorkflowEvent theEvent = new WorkflowEvent (this, contentObject, jParams.getUser(), locale.toString(), false);
                ServicesRegistry.getInstance().getJahiaEventService().fireObjectChanged(theEvent);
            }
        }
        if (oldMode == LINKED) {
            storeLanguageState(oldMainObject, jParams.getSiteID());
            storeLanguageState(object, jParams.getSiteID());
        }
        if (mode == LINKED) {
            storeLanguageState(getMainLinkObject(object), jParams.getSiteID());
            languagesStatesManager.clearEntries(object.toString());
        }
    }

    public boolean hasChanged(ContentObjectKey object, int mode, String workflowName, String processId) throws JahiaException {
        int oldMode = getWorkflowMode(object);
        if (oldMode == mode) {
            if (oldMode == WorkflowService.EXTERNAL) {
                String oldName = getExternalWorkflowName(object);
                String oldId = getExternalWorkflowProcessId(object);
                if (oldName != null && oldId != null && oldName.equals(workflowName) && oldId.equals(processId)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public Set<String> getStagingLanguages(final ContentObjectKey object, final int siteID) throws JahiaException {
        Map<String, Integer> languagesStates = getLanguagesStates(getMainLinkObject(object), siteID);
        Integer sharedLanguageState = (Integer)languagesStates.remove(ContentObject.SHARED_LANGUAGE);

        Set<String> languages = new HashSet<String>();
        for (String lang : languagesStates.keySet()) {
            Integer state = (Integer) languagesStates.get(lang);
            if ( sharedLanguageState != null && state.intValue() < sharedLanguageState.intValue() ){
                state = sharedLanguageState;
            }
            if (state.intValue() >= EntryLoadRequest.STAGING_WORKFLOW_STATE) {
                languages.add(lang);
            }
        }
        return languages;
    }

    private List<ContentObjectKey> getAllInheritedObjects(ContentObjectKey object) throws JahiaException {
        List<ContentObjectKey> l = new ArrayList<ContentObjectKey>();
        l.add(object);
        List<ContentObjectKey> s = getUnlinkedContentObjects(object);
        for (ContentObjectKey contentObject : s) {
            if (getWorkflowMode(contentObject) == INHERITED) {
                l.addAll(getAllInheritedObjects(contentObject));
            }
        }
        return l;
    }

    public ContentObject getHardLinkedMainObject(ContentObject object) throws JahiaException {
        try {
            return (ContentObject) ContentObject.getInstance(getHardLinkedMainObject((ContentObjectKey)object.getObjectKey()));
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    public ContentObjectKey getHardLinkedMainObject(ContentObjectKey object) throws JahiaException {
        ContentObjectKey workflowMainObject = object;
        Collection<ContentObjectKey> c = null;
        if (ContentContainerKey.CONTAINER_TYPE.equals(object.getType())) {
            Collection<ContentObjectKey> l = ((ContentContainerKey)object).getChildsFields(loadRequest);
            for (ContentObjectKey co : l) {
                c = co.getChilds(loadRequest);
                if (!c.isEmpty()) {
                    object = co;
                    break;
                }
            }
        }
        if (ContentFieldKey.FIELD_TYPE.equals(object.getType())) {
            if (c == null) {
                c = object.getChilds(loadRequest);
            }
            if (!c.isEmpty()) {
                ContentPageKey page = (ContentPageKey) c.iterator().next();

                if (page != null) {
                    try {
                        if (getPageType((ContentPageKey) page) == ContentPage.TYPE_DIRECT) {
                            workflowMainObject = page;
                        }
                    } catch (JahiaException e) {
                    }
                }
            }
        }
        return workflowMainObject;
    }

    public ContentObject getInheritingParent(ContentObject object) throws JahiaException {
        try {
            return (ContentObject) ContentObject.getInstance(getInheritingParent((ContentObjectKey)object.getObjectKey()));
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    /**
     * Depends of the workflow mode of the object :
     *  INHERITED : the object from which the effective workflow mode should be inherited
     *  LINKED : the object to which this object is linked
     *  other mode : the object itself
     *
     * @param object
     * @return
     * @throws JahiaException
     */
    public ContentObjectKey getInheritingParent(ContentObjectKey object) throws JahiaException {
        int mode = getWorkflowMode(object);
        return getInheritingParent(object, mode);
    }

    private ContentObjectKey getInheritingParent(ContentObjectKey object, int mode) throws JahiaException {
        if (mode == INHERITED || mode == LINKED) {
            ContentObjectKey parent = getParent(object);
            if (parent == null) {
                return null;
            }
            if (getHardLinkedMainObject(parent).equals(getHardLinkedMainObject(object))) {
                return getInheritingParent(parent, mode);
            } else {
                return getInheritingParent(parent);
            }
        }
        return object;
    }

    public ContentObject getMainLinkObject(ContentObject object) throws JahiaException {
        try {
            return (ContentObject) ContentObject.getInstance(getMainLinkObject((ContentObjectKey)object.getObjectKey()));
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    public ContentObjectKey getMainLinkObject(ContentObjectKey object) throws JahiaException {
        if (ContentFieldKey.FIELD_TYPE.equals(object.getType())) {
            object = object.getParent(null);
        }
        if (object == null) {
            return null;
        }
        String main = (String) getDbEntry(object).get(FIELD_MAIN);
        if (main != null) {
            try {
                return (ContentObjectKey) ContentObjectKey.getInstance(main);
            } catch (ClassNotFoundException e) {
                //..
            }
        }

        int mode = getWorkflowMode(object);
        ContentObjectKey mainKey = getMainLinkObject(object,mode);

        workflowManager.updateWorkflowEntry(object.toString(),mainKey.toString());
        return mainKey;
    }

    private ContentObjectKey getMainLinkObject(ContentObjectKey object, int mode) throws JahiaException {
        ContentObjectKey main = null;
        if (mode == LINKED) {
            ContentObjectKey parent = getParent(object);
            if (parent == null) {
                return getHardLinkedMainObject(object);
            }
            if (getHardLinkedMainObject(parent).equals(getHardLinkedMainObject(object))) {
                main = getMainLinkObject(parent, mode);
            } else {
                main = getMainLinkObject(parent);
            }
        } else {
            object = getHardLinkedMainObject(object);
            main = object;
        }
        return main;
    }

    public ContentObjectKey getTopLinkedObject(ContentObjectKey object) throws JahiaException{
        if (ContentFieldKey.FIELD_TYPE.equals(object.getType())) {
            object = object.getParent(null);
        }
        if (object != null) {
            int mode = getWorkflowMode(object, false);
            return getMainLinkObject(object,mode);
        } else {
            return null;
        }
    }

    public List<JahiaObject> getLinkedContentObjects(ContentObject object, boolean descendInAllSubGroups) throws JahiaException{
        List<ContentObjectKey> l = getLinkedContentObjects((ContentObjectKey) object.getObjectKey(), descendInAllSubGroups);
        List<JahiaObject> r = new ArrayList<JahiaObject>();
        for (ContentObjectKey contentObjectKey : l) {
            try {
                JahiaObject instance = ContentObject.getInstance(contentObjectKey);
                if (instance != null) {
                    r.add(instance);
                }
            } catch (ClassNotFoundException e) {
            }
        }
        return r;
    }

    public List<ContentObjectKey> getLinkedContentObjects(ContentObjectKey object, boolean descendInAllSubGroups) throws JahiaException{
        List<ContentObjectKey>[] l = getLinkedContentObjects(object, descendInAllSubGroups, true);
        return new ArrayList<ContentObjectKey>(l[0]);
    }

    public List<JahiaObject> getUnlinkedContentObjects(ContentObject object) throws JahiaException{
        List<ContentObjectKey> l = getUnlinkedContentObjects((ContentObjectKey) object.getObjectKey());
        List<JahiaObject> r = new ArrayList<JahiaObject>();
        for (ContentObjectKey contentObjectKey : l) {
            try {
                JahiaObject instance = ContentObject.getInstance(contentObjectKey);
                if (instance != null) {
                    r.add(instance);
                }
            } catch (ClassNotFoundException e) {
            }
        }
        return r;
    }

    public List<ContentObjectKey> getUnlinkedContentObjects(ContentObjectKey object) throws JahiaException{
        return getUnlinkedContentObjects(object, false, true);
    }

    public List<JahiaObject> getUnlinkedContentObjects(ContentObject object, boolean descendInAllSubGroups, boolean checkParents) throws JahiaException{
        List<ContentObjectKey> l = getUnlinkedContentObjects((ContentObjectKey) object.getObjectKey(), descendInAllSubGroups, checkParents);
        List<JahiaObject> r = new ArrayList<JahiaObject>();
        for (ContentObjectKey contentObjectKey : l) {
            try {
                JahiaObject instance = ContentObject.getInstance(contentObjectKey);
                if (instance != null) {
                    r.add(instance);
                }
            } catch (ClassNotFoundException e) {
            }
        }
        return r;
    }

    public List<ContentObjectKey> getUnlinkedContentObjects(ContentObjectKey object, boolean descendInAllSubGroups, boolean checkParents) throws JahiaException{
        List<ContentObjectKey>[] l = getLinkedContentObjects(object, descendInAllSubGroups, checkParents);
        return new ArrayList<ContentObjectKey>(l[1]);
    }

    private List<ContentObjectKey>[] getLinkedContentObjects(ContentObjectKey object, boolean descendInAllSubGroups, boolean checkParents) throws JahiaException {
        GroupCacheKey cacheKey = new GroupCacheKey(object.toString()+"_"+descendInAllSubGroups+"_"+checkParents, new HashSet<String>());
        List<ContentObjectKey>[] l = linkedObjectsCache.get(cacheKey);
        if (l == null) {
            ContentObjectKey main = getTopLinkedObject(object);
            GroupCacheKey mainCacheKey = null;
            if (!main.equals(object)) {
                mainCacheKey = new GroupCacheKey(main.toString()+"_"+descendInAllSubGroups+"_"+checkParents, new HashSet<String>());
                l = linkedObjectsCache.get(mainCacheKey);
            }
            cacheKey.getGroups().add(object.toString());
            cacheKey.getGroups().add(MAINPREFIX + main.toString());
            if (l != null) {
                linkedObjectsCache.put(cacheKey, l);
            } else {
                Set<ContentObjectKey> keys = new HashSet<ContentObjectKey>();
                l = new List[] { new ArrayList<ContentObjectKey>(), new ArrayList<ContentObjectKey>() };
                getLinkedContentObjects(object, keys, l[0], l[1], descendInAllSubGroups, checkParents);
                l[1].removeAll(l[0]);
                linkedObjectsCache.put(cacheKey, l);
                if (mainCacheKey != null) {
                    mainCacheKey.getGroups().add(main.toString());
                    mainCacheKey.getGroups().add(MAINPREFIX + main.toString());
                    linkedObjectsCache.put(mainCacheKey, l);
                }
            }
        }
        return l;
    }

//    private void getLinkedContentObjects(ContentObjectKey object, Set keys, List linked, List unlinked, boolean descendInAllSubGroups, boolean checkParents) throws JahiaException{
//        if (keys.contains(object)) {
//            return;
//        }
//
//        keys.add(object);
//
//        final Collection childs = object.getChilds(loadRequest);
//        ContentObjectKey objectHardLinkedMainObject = getHardLinkedMainObject(object);
//        final Iterator iterator = childs.iterator();
//        while ( iterator.hasNext() ) {
//            ContentObjectKey child = (ContentObjectKey) iterator.next();
//            ContentObjectKey childHardLinkedMainObject = getHardLinkedMainObject(child);
//            boolean childIsMain = (childHardLinkedMainObject.equals(objectHardLinkedMainObject));
//            if ((getWorkflowMode(child,childHardLinkedMainObject) == LINKED || descendInAllSubGroups || childIsMain)
//                // && !child.getStagingLanguages(true).isEmpty()
//                    ) {
//                getLinkedContentObjects(child, keys, linked, unlinked, false, true);
//            }
//            if (getWorkflowMode(child,childHardLinkedMainObject) != LINKED && !childIsMain) {
//                if (!unlinked.contains(child)) {
//                    unlinked.add(child);
//                }
//            }
//        }
//
//        getHardLinkedContentObjects(object, keys, linked, unlinked);
//
//        if (checkParents && getWorkflowMode(object,objectHardLinkedMainObject) == LINKED) {
//            getLinkedContentObjects(getParent(object), keys, linked, unlinked, false, checkParents);
//        }
//
//    }
//
//    private void getHardLinkedContentObjects(ContentObjectKey object, Set keys, List linked, List unlinked) throws JahiaException {
//        linked.add(object);
//        if (ContentPageKey.PAGE_TYPE.equals(object.getType())) {
//            ContentObjectKey parent = object.getParent(loadRequest);
//            if (parent != null) {
//                ContentObjectKey parent2 = parent.getParent(loadRequest);
//                getLinkedContentObjects(parent, keys, linked, new ArrayList(), false, true);
//                if (parent2 != null && ContentContainerKey.CONTAINER_TYPE.equals(parent2.getType())) {
//                    getLinkedContentObjects(parent2, keys, linked, new ArrayList(), false, true);
//                }
//            }
//        }
//    }
//

    private void getLinkedContentObjects(ContentObjectKey object, Set<ContentObjectKey> keys, List<ContentObjectKey> linked, List<ContentObjectKey> unlinked, boolean descendInAllSubGroups, boolean checkParents) throws JahiaException{
        if (keys.contains(object)) {
            return;
        }

        keys.add(object);
        linked.add(object);
        ContentObjectKey k = getMainLinkObject(object);
        for (ContentObjectKey child : object.getChilds(loadRequest)) {
            if (getMainLinkObject(child).equals(k)) {
                getLinkedContentObjects(child, keys, linked, unlinked, descendInAllSubGroups, false);
            } else {
                unlinked.add(child);
            }

        }

        if (checkParents) {
            ContentObjectKey parent = getParent(object);
            if (parent != null && getMainLinkObject(parent).equals(k)) {
                getLinkedContentObjects(parent, keys, linked, unlinked, false, true);
            }
            if (object instanceof ContentPageKey) {
                parent = object.getParent(EntryLoadRequest.CURRENT);
                if (parent != null && getMainLinkObject(parent).equals(k)) {
                    getLinkedContentObjects(parent, keys, linked, unlinked, false, true);
                }
            }
        }
    }

//    private void getLinkedContentObjects(ContentObjectKey object, Set keys, List linked, List unlinked, boolean descendInAllSubGroups, boolean checkParents) throws JahiaException{
//        linked.add()
//        List l = workflowManager.getLinkedObjectForMain(getMainLinkObject(object).toString());
//
//        for (Iterator iterator = l.iterator(); iterator.hasNext();) {
//            String s = (String) iterator.next();
//            ContentObjectKey k = ContentObjectKey.getInstance(s);
//            if (ContentContainerKey.CONTAINER_TYPE.equals(object.getType())) {
//                List v = ServicesRegistry.getInstance().getJahiaContainersService().getFieldIDsInContainer(object.getIdInType(),loadRequest);
//                for (Iterator it2 = v.iterator(); it2.hasNext();) {
//                    Object[] o = (Object[]) iterator.next();
//                    ContentFieldKey key = new ContentFieldKey(((Integer) o[0]).intValue());
//                    results.add(key);
//                }
//            }
//
//        }
//
//    }


    /**
     * Get the list of all available external workflow
     *
     * @param locale current display locale
     * @return map
     */
    public Map<String, String> getExternalWorkflowNames(Locale locale) {
        Map<String, String> results = new HashMap<String, String>();
        for (Iterator<String> iterator = externals.keySet().iterator(); iterator.hasNext();) {
            String name = iterator.next();
            String displayName = getExternalWorkflow(name).getDisplayName(locale);
            results.put(name, displayName);
        }
        return results;
    }

    /**
     * Get the list of all available external workflow
     *
     * @return map
     */
    public Map<String, ExternalWorkflow> getExternalWorkflows() {
        return new HashMap<String, ExternalWorkflow>(externals);
    }

    public ExternalWorkflow getExternalWorkflow(String name) {
        return externals.get(name);
    }


    private Map<String, Object> getDbEntry(ContentObjectKey object) {
        return workflowManager.getWorkflowEntry(object.toString());
    }

    public boolean hasStagingEntries(ContentObject object, String languageCode) throws JahiaException{
        return hasStagingEntries((ContentObjectKey) object.getObjectKey(), languageCode);
    }

    public boolean hasStagingEntries(ContentObjectKey object, String languageCode) throws JahiaException{
        List<ContentObjectKey> linkedObjects = getLinkedContentObjects(object, false);
        for (ContentObjectKey contentObjectKey : linkedObjects) {
            try {
                ContentObject contentObject = (ContentObject) ContentObject.getInstance(contentObjectKey);
                Set<String> stagingLanguages = contentObject.getStagingLanguages(false,true);
                if (stagingLanguages.contains(languageCode) || stagingLanguages.contains(ContentObject.SHARED_LANGUAGE)) {
                    return true;
                }
            } catch (ClassNotFoundException e) {
            }
        }
        return false;
    }

    public Map<String, Integer> getLanguagesStates(final ContentObject contentObject) throws JahiaException {
        if (contentObject == null) {
            return new HashMap<String, Integer>();
        }
        return getLanguagesStates(getMainLinkObject((ContentObjectKey) contentObject.getObjectKey()), contentObject.getSiteID());
    }

    private Map<String, Integer> getLanguagesStates(final ContentObjectKey contentObject, final int siteID) throws JahiaException {
        Map<String, Integer> r = languagesStatesManager.getLanguagesStates(contentObject.toString(), siteID, true);
        if (r.isEmpty()) {
            r = storeLanguageState(contentObject, siteID);
        }
        return r;
    }

    public Map<String, Integer> evalLanguagesStates(ContentObjectKey contentObject) throws JahiaException {
        Map<String, Integer> results = null;
        GroupCacheKey cacheKey = new GroupCacheKey(WORKFLOWSERVICE_KEYPREFIX+contentObject.toString(), new HashSet<String>());
        results = cache.get(cacheKey);
        if (results == null) {
            contentObject = getTopLinkedObject(contentObject);
            cacheKey.getGroups().add(MAINPREFIX +contentObject.toString());

            Collection<ContentObjectKey> c = getLinkedContentObjects(contentObject, false);
            results = new HashMap<String, Integer>();

            for (ContentObjectKey key : c) {
                try {
                    ContentObject o = (ContentObject) ContentObject.getInstance(key);
                    if (o != null) {
                        mergeLanguageStates(results, o.getLanguagesStates());
                    }
                } catch (Exception e) {
                    logger.error("Cannot get information for object "+key,e);
                }
            }
            cache.put(cacheKey,results);
        }
        return results;
    }

    private Map<String, Integer> mergeLanguageStates (Map<String, Integer> destination, Map<String, Integer> source) {
        Iterator<String> sourceIter = source.keySet ().iterator ();
        while (sourceIter.hasNext ()) {
            String curLanguageCode = sourceIter.next ();
            Integer languageState = source.get (curLanguageCode);
            Integer resultState = destination.get (curLanguageCode);
            if (resultState != null) {
                if (resultState.intValue () < languageState.intValue ()) {
                    destination.put (curLanguageCode, languageState);
                }
            } else {
                destination.put (curLanguageCode, languageState);
            }
        }

        return destination;

    }

    public void changeStagingStatus (ContentObject object, Set<String> languageCodes, int newWorkflowState,
                                     StateModificationContext stateModifContext,
                                     ProcessingContext jParams,
                                     boolean rollBackMarkForDelete)
            throws JahiaException {
        changeStagingStatus((ContentObjectKey) object.getObjectKey(),languageCodes,newWorkflowState,
                stateModifContext, jParams,rollBackMarkForDelete);
    }

    /**
     *
     * @param object
     * @param languageCodes
     * @param newWorkflowState
     * @param stateModifContext
     * @param jParams
     * @param rollBackMarkForDelete if true, rollback mark for delete
     * @throws JahiaException
     */
    public void changeStagingStatus (ContentObjectKey object, Set<String> languageCodes, int newWorkflowState,
                                     StateModificationContext stateModifContext,
                                     ProcessingContext jParams,
                                     boolean rollBackMarkForDelete)
            throws JahiaException {
        changeStagingStatus(object, languageCodes, newWorkflowState,
                stateModifContext, jParams, rollBackMarkForDelete, false);
    }

    /**
    *
    * @param object
    * @param languageCodes
    * @param newWorkflowState
    * @param stateModifContext
    * @param jParams
    * @param rollBackMarkForDelete if true, rollback mark for delete
    * @param bypassValidation check if the object is valid for activation or skip this check?
    * @throws JahiaException
    */
   public void changeStagingStatus (ContentObjectKey object, Set<String> languageCodes, int newWorkflowState,
                                    StateModificationContext stateModifContext,
                                    ProcessingContext jParams,
                                    boolean rollBackMarkForDelete,
                                    boolean bypassValidation)
           throws JahiaException {
       if (newWorkflowState < EntryLoadRequest.STAGING_WORKFLOW_STATE) {
           return;
       }
       List<ContentObjectKey> objects = getLinkedContentObjects(object, stateModifContext.isDescendingInSubPages());
       // the first operation to do is to change the status of all the fields
       // in the page.
       for (ContentObjectKey contentObjectKey : objects) {
           try {
               ContentObject contentObject = (ContentObject) ContentObject.getInstance(contentObjectKey);
               Set<String> stagingLanguages = contentObject.getStagingLanguages(false, true);
               boolean ok = false;
               for (String languageCode : languageCodes) {
                   if (stagingLanguages.contains(languageCode)) {
                       ok = true;
                   }
               }
               if (ok) {
                   if (contentObject.checkWriteAccess(jParams.getUser())) {
                       
                       ActivationTestResults testActivationResults = isValidForActivation(contentObjectKey, languageCodes, jParams, stateModifContext);
                       if (!bypassValidation) {
                           if (testActivationResults.getStatus() == ActivationTestResults.FAILED_OPERATION_STATUS) {
                               if (testActivationResults.getErrors().size() == 0) {
                                   testActivationResults.appendError(new NodeOperationResult(null,
                                           null, "Notification aborted because of errors !"));
                               }
                               return;
                           }
    
                           if (testActivationResults.hasBlockerError()) {
                               return;
                           }
                       }
                       ContentActivationEvent event = new ContentActivationEvent(contentObject, contentObject.getObjectKey(), jParams.getUser(),
                               languageCodes, true, new JahiaSaveVersion(), jParams, stateModifContext, testActivationResults);

                       ServicesRegistry.getInstance().getJahiaEventService().fireContentWorkflowStatusChanged(event);

                       contentObject.setWorkflowState(languageCodes, newWorkflowState, jParams, stateModifContext);
                       if ( rollBackMarkForDelete && contentObject.isMarkedForDelete() ){
//                           this.undoStaging(contentObject,jParams);
                       }
                   }
               }
           } catch (ClassNotFoundException e) {
           }
           cache.flushGroup(MAINPREFIX +getTopLinkedObject(object).toString());
       }

       storeLanguageState(getMainLinkObject(object), jParams.getSiteID());
   }
   
    public ActivationTestResults isValidForActivation(ContentObject object,
                                                      Set<String> languageCodes,
                                                      ProcessingContext jParams,
                                                      StateModificationContext stateModifContext) throws JahiaException {
        return isValidForActivation((ContentObjectKey) object.getObjectKey(), languageCodes , jParams, stateModifContext);
    }

    public ActivationTestResults isValidForActivation(final ContentObjectKey objectKey,
                                                      final Set<String> languageCodes,
                                                      final ProcessingContext jParams,
                                                      final StateModificationContext stateModifContext) throws JahiaException {
        final ActivationTestResults activationTestResults = new ActivationTestResults ();
        final List<ContentObjectKey> objects = getLinkedContentObjects(objectKey, stateModifContext.isDescendingInSubPages());
        if (logger.isDebugEnabled()) {
            logger.debug("isValidForActivation: objects" + objects);
        }
        for (final ContentObjectKey contentObjectKey : objects) {
            try {
                ContentObject contentObject = (ContentObject) ContentObject.getInstance(contentObjectKey);
                if (contentObject != null) {
                    activationTestResults.merge(contentObject.isValidForActivation(languageCodes, jParams, stateModifContext));
                }
            } catch (ClassNotFoundException e) {
            }
        }

        final Collection<ContentObjectKey> u = getUnlinkedContentObjects(objectKey);

        // do not take in account page links
        List<ContentObjectKey> unlinkedContents = new ArrayList<ContentObjectKey>();
        for ( ContentObjectKey co : u ){
            if (co instanceof ContentPageKey){
                try {
                    ContentPage contentPage = (ContentPage) ContentObject.getInstance(co);
                    if (contentPage.getPageType(EntryLoadRequest.STAGED) != ContentPage.TYPE_DIRECT ){
                        continue;
                    }
                } catch (ClassNotFoundException e) {
                }
            }
            unlinkedContents.add(co);
        }

        return activationTestResults;
    }

    public ActivationTestResults activate(ContentObject object,
                                          Set<String> languageCodes,
                                          JahiaSaveVersion saveVersion,
                                          ProcessingContext jParams,
                                          StateModificationContext stateModifContext) throws JahiaException {
        return activate((ContentObjectKey) object.getObjectKey(), languageCodes , saveVersion, jParams, stateModifContext);
    }

    public ActivationTestResults activate(ContentObjectKey object,
                                          Set<String> languageCodes,
                                          JahiaSaveVersion saveVersion,
                                          ProcessingContext jParams,
                                          StateModificationContext stateModifContext) throws JahiaException {
        return activate(object, languageCodes, saveVersion, jParams, stateModifContext, true);
    }

    private ActivationTestResults activate(ContentObjectKey objectKey,
                                           Set<String> languageCodes,
                                           JahiaSaveVersion saveVersion,
                                           ProcessingContext jParams,
                                           StateModificationContext stateModifContext, boolean checkStatusAtEnd) throws JahiaException {
        ActivationTestResults testActivationResults = isValidForActivation(objectKey, languageCodes, jParams, stateModifContext);
        if (testActivationResults.getStatus() == ActivationTestResults.FAILED_OPERATION_STATUS) {
            if (testActivationResults.getErrors().size() == 0) {
                testActivationResults.appendError(new NodeOperationResult(null,
                        null, "Activation aborted because of errors !"));
            }
            return testActivationResults;
        }

        if (testActivationResults.hasBlockerError()) {
            return testActivationResults;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Activating "+objectKey);
        }
        
        boolean versioningActive = jParams.getSite().isVersioningEnabled();
        ActivationTestResults activationResults = new ActivationTestResults ();
        Set<ContentObjectKey> deletedChilds = new HashSet<ContentObjectKey>();
        List<ContentObjectKey> objects = getLinkedContentObjects(objectKey, stateModifContext.isDescendingInSubPages());
        Set<Integer> fieldsActivation = new HashSet<Integer>();
        Set<Integer> containersActivation = new HashSet<Integer>();
        Set<Integer> pagesActivation = new HashSet<Integer>();
        Integer pageID = null;
        Integer ctnID = null;
        for (ContentObjectKey contentObjectKey : objects) {
            try {
                ContentObject contentObject = (ContentObject) ContentObject.getInstance(contentObjectKey);
                if (contentObject != null && !contentObject.getStagingLanguages(false,true).isEmpty()) {
                        Object[] failedObjects = getActivationResultsByContainer(
                                testActivationResults, jParams);
                        Set<Integer> failedContainers = (Set<Integer>) failedObjects[0];
                        Map<Integer, ActivationTestResults> failedFields = (Map<Integer, ActivationTestResults>) failedObjects[1];

                        boolean doActivate = true;
                        ActivationTestResults objectActivationResult = null;
                        if (!failedContainers.isEmpty()) {
                            if (contentObject instanceof ContentField) {
                                ContentField fld = (ContentField) contentObject;
                                objectActivationResult = failedFields.get(fld
                                        .getID());
                                doActivate = objectActivationResult == null
                                        && (fld.getContainerID() == 0 || !failedContainers
                                                .contains(fld.getContainerID()));
                            } else if (contentObject instanceof ContentContainer) {
                                doActivate = failedContainers
                                        .contains(((ContentContainer) contentObject)
                                                .getID());
                            }
                        }
                        if (doActivate) {
                            if (contentObject.isMarkedForDelete()) {
                                deletedChilds.addAll(getUnlinkedContentObjects(objectKey,false,false));
                            }
                            activationResults.merge(contentObject.activate(languageCodes, versioningActive, saveVersion, jParams.getUser(), jParams, stateModifContext));
                            if (contentObject instanceof ContentField){
                                ctnID = new Integer(((ContentField)contentObject).getContainerID());
                                if ( !containersActivation.contains(ctnID) && !fieldsActivation.contains(ctnID) ){
                                    fieldsActivation.add(ctnID);
                                }
                            } else if ( contentObject instanceof ContentContainer){
                                ctnID = new Integer(contentObject.getID());
                                if ( !containersActivation.contains(ctnID) ){
                                    containersActivation.add(ctnID);
                                }
                            } else if ( contentObject instanceof ContentPage ){
                                pageID = new Integer(contentObject.getID());
                                if ( !pagesActivation.contains(pageID) ){
                                    pagesActivation.add(pageID);
                                }
                            }
                        } else if (objectActivationResult != null) {
                            activationResults.merge(objectActivationResult);
                        }
//                    }
                }
            } catch (ClassNotFoundException e) {
            }
            cache.flushGroup(MAINPREFIX +getTopLinkedObject(objectKey).toString());
        }
        Iterator<Integer> it = fieldsActivation.iterator();
        ContentActivationEvent event = null;
        ContentContainer container = null;
        ContentPage page = null;
        LoggingEventListener loggingListener = (LoggingEventListener)JahiaListenersRegistry.getInstance()
                .getListenerByClassName(LoggingEventListener.class.getName());
        if ( loggingListener != null ){
            while (it.hasNext()){
                ctnID =it.next();
                if (ctnID.intValue()>0 && !containersActivation.contains(ctnID)){
                    try {
                        container = ContentContainer.getContainer(ctnID.intValue());
                        event = new ContentActivationEvent(container,container.getObjectKey(),
                                jParams.getUser(),languageCodes,versioningActive,
                                saveVersion,jParams,stateModifContext,activationResults);
                        loggingListener.contentActivation(event);
                        pageID = new Integer(container.getPageID());
                        if (!pagesActivation.contains(pageID)){
                            try {
                                page = ContentPage.getPage(pageID.intValue());
                                if ( page != null ){
                                    event = new ContentActivationEvent(page,page.getObjectKey(),
                                                                                  jParams.getUser(),languageCodes,
                                            versioningActive,
                                            saveVersion,jParams,stateModifContext,activationResults);
                                    loggingListener.contentActivation(event);
                                }
                            } catch ( Exception t ){
                                logger.debug("Exception occurent logging parent page validation",t);
                            }
                        }
                    } catch ( Exception t ){
                        logger.debug("Exception occurent logging field's parent container validation",t);
                    }
                }
            }
            it = containersActivation.iterator();
            while (it.hasNext()){
                ctnID =it.next();
                if (ctnID.intValue()>0){
                    try {
                        container = ContentContainer.getContainer(ctnID.intValue());
                        pageID = new Integer(container.getPageID());
                        if (!pagesActivation.contains(pageID)){
                            try {
                                page = ContentPage.getPage(pageID.intValue());
                                if ( page != null ){
                                    event = new ContentActivationEvent(page,page.getObjectKey(),
                                            jParams.getUser(),languageCodes,versioningActive,
                                            saveVersion,jParams,stateModifContext,activationResults);
                                    loggingListener.contentActivation(event);
                                }
                            } catch ( Exception t ){
                                logger.debug("Exception occurent logging parent page validation",t);
                            }
                        }
                    } catch ( Exception t ){
                        logger.debug("Exception occurent logging field's parent container validation",t);
                    }
                }
            }
        }

        ContentObject contentObject = null;
        try {   
            contentObject = (ContentObject) ContentObject.getInstance(objectKey);

            // propagate deletion to childs
            for (ContentObjectKey childObject : deletedChilds) {
                activationResults.merge(activate(childObject, languageCodes, saveVersion, jParams, stateModifContext));
            }
               
            // propagate to parent container list if need
            if ( contentObject instanceof ContentContainer ){
                container = (ContentContainer)contentObject;
                ContentContainerList ctnList = ContentContainerList
                        .getContainerList(container.getParentContainerListID());
                ctnList.activateMetadatas(languageCodes,versioningActive,saveVersion,jParams.getUser(),jParams,stateModifContext);
            }

            // activate parent page metadatas
            if ( contentObject instanceof PageReferenceableInterface ){
                PageReferenceableInterface pageRefObj = (PageReferenceableInterface)contentObject;
                try {
                    ContentPage parentPage = pageRefObj.getPage();
                    if ( parentPage != null ){
                        parentPage.activateMetadatas(languageCodes,versioningActive,saveVersion,jParams.getUser(),
                                jParams,stateModifContext);
                    }
                } catch ( Exception t){
                    logger.debug("exception occured updating parent page metadata",t);
                }
            }
        } catch (ClassNotFoundException e) {
        }

        // activate metadatas
        for (ContentObjectKey contentObjectKey: objects) {
            try {
                ContentObject childContentObject = (ContentObject) ContentObject.getInstance(contentObjectKey);
                if (childContentObject != null) {
                    activationResults.merge(childContentObject.activateMetadatas(languageCodes, versioningActive, saveVersion, jParams.getUser(), jParams, stateModifContext));
                }
            } catch (ClassNotFoundException e) {
            }
//            cache.flushGroup(MAINPREFIX +getTopLinkedObject(objectKey).toString());
        }

        event = new ContentActivationEvent(contentObject,objectKey, jParams.getUser(), languageCodes, true, saveVersion, jParams, stateModifContext, testActivationResults);
        ServicesRegistry.getInstance().getJahiaEventService().fireAfterGroupActivation(event);

        storeLanguageState(getMainLinkObject(objectKey), jParams.getSiteID());

        return activationResults;
    }

    private Object[] getActivationResultsByContainer(
            ActivationTestResults testActivationResults,
            ProcessingContext jParams) {

        Set<Integer> failedContainers = new HashSet<Integer>();
        Map<Integer, ActivationTestResults> failedFields = new HashMap<Integer, ActivationTestResults>();
        if (testActivationResults.getErrors().size() > 0) {
            for (Object activationErrors : testActivationResults.getErrors()) {
            	NodeOperationResult activationResult = (NodeOperationResult)activationErrors;
                if (ContentFieldKey.FIELD_TYPE.equals(activationResult
                        .getObjectType())) {
                    ContentObjectKey fieldKey = (ContentObjectKey) activationResult
                            .getNodeKey();
                    ContentObjectKey ctnKey = fieldKey.getParent(jParams
                            .getEntryLoadRequest());
                    if (ctnKey != null
                            && ContentContainerKey.CONTAINER_TYPE.equals(ctnKey
                                    .getType())) {
                        ActivationTestResults ctnActivationResult = failedFields
                                .get(fieldKey.getIdInType());
                        if (null == ctnActivationResult) {
                            ctnActivationResult = new ActivationTestResults(
                                    testActivationResults.getStatus());
                        }
                        ctnActivationResult.appendError(activationResult);
                        failedContainers.add(ctnKey.getIdInType());
                        failedFields.put(fieldKey.getIdInType(),
                                ctnActivationResult);
                    }
                }
            }
        }

        return failedFields.size() > 0 ? new Object[] { failedContainers,
                failedFields } : new Object[] { Collections.emptySet(),
                Collections.emptyMap() };
    }

    public void pageMoved(ContentObjectKey parent, ContentPageKey page, ProcessingContext jParams) throws JahiaException {
        if (getInheritedMode(parent) == WorkflowService.INACTIVE) {
            List<ContentObjectKey> all = getAllInheritedObjects(page);
            Set<String> languages = new HashSet<String>();
            for (Iterator<Locale> iterator = jParams.getSite().getLanguageSettingsAsLocales(true).iterator(); iterator.hasNext();) {
                languages.add(iterator.next().toString());
            }
            JahiaSaveVersion saveVersion = ServicesRegistry.getInstance ().getJahiaVersionService ().
                    getSiteSaveVersion (jParams.getSiteID ());
            StateModificationContext stateModifContext = new StateModificationContext(page, languages);
            stateModifContext.addModifiedObjects(all);

            for (Iterator<ContentObjectKey> iterator = all.iterator(); iterator.hasNext();) {
                ContentObjectKey contentObjectKey = iterator.next();
                activate(contentObjectKey, languages, saveVersion, jParams, stateModifContext);
            }
        }
    }

    public Map<String,List<String>> getAllStagingObject(final int siteID) {
        return languagesStatesManager.getAllStagingObject(siteID);
    }

    public Map<String,List<String>> getAllWaitingObject(final int siteID) {
        return languagesStatesManager.getAllWaitingObject(siteID);
    }

    public Map<String,List<String>> getAllStagingAndWaitingObject(final int siteID) {
        return languagesStatesManager.getAllStagingAndWaitingObject(siteID);
    }

    public Map<String, Integer> storeLanguageState(final ContentObjectKey key, final int siteID) throws JahiaException {
        Map<String, Integer> m = evalLanguagesStates(key);
        languagesStatesManager.updateLanguagesStates(key.toString(), m, siteID);
        return m;
    }

    public void storeLanguageState(final ContentObjectKey key, final String languageCode, final int value, final int siteID) {
        try {
            Map<String, Integer> m = evalLanguagesStates(key);
            if (m.containsKey(languageCode)) {
                languagesStatesManager.updateLanguagesState(key.toString(),
                        languageCode, value, siteID);
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Object '" + key
                            + "' does not contain entries for language '"
                            + languageCode
                            + "'. Ignore storing lanaguage state.");
                }

            }
        } catch (JahiaException e) {
            logger.error("Error storing language state '" + value
                    + "' for object '" + key + "' and language '"
                    + languageCode + "'", e);
        }
    }


//    /**
//     * Performs an undo staging rollback on the given content object
//     *
//     * @param contentObject
//     * @param context
//     * @throws JahiaException
//     */

//    private void undoStaging(ContentObject contentObject,
//                            ProcessingContext context) throws JahiaException {
//        if ( contentObject == null || context == null ){
//            return;
//        }
//        EntryLoadRequest loadRequest = (EntryLoadRequest)EntryLoadRequest.STAGED.clone();
//        loadRequest.setWithMarkedForDeletion(true);
//
//        UndoStagingContentTreeVisitor visitor =
//                new UndoStagingContentTreeVisitor(contentObject, context.getUser(),
//                        loadRequest, context.getOperationMode(), context);
//        visitor.undoStaging();
//        if ( contentObject instanceof ContentPage ){
//            // reset page cache
//            ((ContentPage)(contentObject)).commitChanges(true, context);
//            // reset site map cache
//            ServicesRegistry.getInstance().getJahiaSiteMapService().resetSiteMap();
//        }
//    }

    public void clearLanguageStateEntries(ContentObjectKey key) {
        this.languagesStatesManager.clearEntries(key.toString());
    }

    public void flushCacheForObjectChanged(ContentObjectKey key) {
        if (key.getIdInType()>0)  {
            try {
                ContentObjectKey main = getTopLinkedObject(key);
                linkedObjectsCache.flushGroup(MAINPREFIX + main);
                ContentObjectKey parent = key;
                ContentObjectKey mainParent = null;
                while ((parent=parent.getParent(loadRequest)) != null && (mainParent = getTopLinkedObject(parent)).toString().equals(main.toString()));
                if (mainParent != null) {
                    linkedObjectsCache.flushGroup(MAINPREFIX + mainParent);
                }
                cache.flushGroup(MAINPREFIX +main);
            } catch (Exception e) {
                linkedObjectsCache.flush();
            }
        }
    }

    public void flushCacheForPageCreatedOrDeleted(ContentObjectKey key) {
        if (key.getIdInType()==-1) {
            return;
        }
        modeCache.remove(key.toString());
        ContentObjectKey current = key;
        int m = 3;
        if (ContentFieldKey.FIELD_TYPE.equals(key.getType())) {
            m = 2;
        }
        for (int l=0;l<m && current != null; l++) {
            modeCache.remove(current.toString());
            if (!ContentFieldKey.FIELD_TYPE.equals(current.getType())) {
                workflowManager.updateWorkflowEntry(current.toString(), null);
                try {
                    getMainLinkObject(current);
                } catch (JahiaException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            current = current.getParent(loadRequest);
        }
        if (key instanceof ContentFieldKey) {
            Collection<ContentObjectKey> c = key.getChilds(loadRequest);
            for (Iterator<ContentObjectKey> iterator = c.iterator(); iterator.hasNext();) {
                modeCache.remove(iterator.next().toString());
            }
        }
        flushCacheForObjectChanged(key);
    }
    
    /**
     * Returns the next workflow step number if the object has the external
     * workflow; otherwise returns <code>0</code>.
     * 
     * @param contentObject
     *            the content object
     * @param language
     *            current languge code
     * @return the next workflow step number if the object has the external
     *         workflow; otherwise returns <code>0</code>
     */
    public int getExternalWorkflowNextStep(ContentObject contentObject, String language) {
        int extWorkflowStep = 0;
        try {
            int workflowMode = getWorkflowMode(contentObject);
            ContentObjectKey objKey = (ContentObjectKey) contentObject.getObjectKey();
            if (WorkflowService.LINKED == workflowMode) {
                objKey = getMainLinkObject((ContentObjectKey) contentObject.getObjectKey());
            }
            if (objKey != null) {
                String processName = getInheritedExternalWorkflowName(objKey);
                ExternalWorkflow workflow = getExternalWorkflow(processName);
                if (workflow != null) {
                    ExternalWorkflowInstanceCurrentInfos info = workflow.getCurrentInfo(objKey.getKey(), language);
                    if (info != null) {
                        extWorkflowStep = info.getNextStep();
                    }
                }
            }
        } catch (JahiaException ex) {
            logger.error("Unable to retrieve additional information about external workflow state for object: " + contentObject.getObjectKey(), ex);
        }
        return extWorkflowStep;
    }

    /**
     * Returns the string representation of the extended workflow status, in the
     * form <code>xyz</code>, where <code>x</code> is the workflow mode
     * (exception is the INACTIVE mode - it corresponds to '5'), <code>y</code> -
     * the language state or a next step number in case of N-Step worklfow and
     * <code>z</code> is <code>"1"</code> in case the content object can be
     * edited, or <code>"0"</code> otherwise.
     * 
     * @param contentObject
     *            the content object
     * @param language
     *            current language
     * @return the string representation of the extended workflow status, in the
     *         form <code>xyz</code>, where <code>x</code> is the workflow
     *         mode (exception is the INACTIVE mode - it corresponds to '5'),
     *         <code>y</code> - the language state or a next step number in
     *         case of N-Step worklfow and <code>z</code> is <code>"1"</code>
     *         in case the content object can be edited, or <code>"0"</code>
     *         otherwise
     * @throws Exception
     *             in case of an error
     */
    public String getExtendedWorkflowState(ContentObject contentObject, String language) throws Exception {

        int workflowMode = getInheritedMode(contentObject);
        final Map<String, Integer> languagesStates = getLanguagesStates(contentObject);

        return getExtendedWorkflowState(contentObject, workflowMode, languagesStates, language);
    }

    /**
     * Returns a map of string representations of the extended workflow statuses
     * corresponding to each available language.
     *
     * @param contentObject
     *            the content object
     * @return a map of (language, state)
     * @throws Exception
     *             in case of an error
     */
    public Map<String, String> getExtendedWorkflowStates(ContentObject contentObject) throws Exception {
        int workflowMode = getInheritedMode(contentObject);
        final Map<String, Integer> languagesStates = getLanguagesStates(contentObject);
        Map<String, String> extendedWorkflowStates = new HashMap<String, String>() ;
        List<Locale> list = ServicesRegistry.getInstance().getJahiaSitesService().getSite(contentObject.getSiteID()).getLanguageSettingsAsLocales(false);
        for (Locale locale : list) {
            String extendedWorkflowState = getExtendedWorkflowState(contentObject, workflowMode, languagesStates, locale.toString());
            if (logger.isDebugEnabled()) {
                logger.debug(contentObject.getObjectKey().getKey() + " : " + locale + " / "  + extendedWorkflowState) ;
            }
            extendedWorkflowStates.put(locale.toString(), extendedWorkflowState) ;
        }
        return extendedWorkflowStates ;
    }

    private String getExtendedWorkflowState(ContentObject contentObject, int workflowMode, Map<String, Integer> languagesStates, String language) throws JahiaException {
        String extendedWorkflowState ;
        Integer languageState = languagesStates.get(language);
        Integer sharedLanguageState = languagesStates.get(ContentObject.SHARED_LANGUAGE);

        if (WorkflowService.INACTIVE == workflowMode) {
            if (SettingsBean.getInstance().isWorkflowDisplayStatusForLinkedPages()
                    && getWorkflowMode(contentObject) == LINKED) {
                if (languageState != null) {
                    extendedWorkflowState = "910" ;
                } else {
                    extendedWorkflowState = "920";
                }
            } else {
                if (languageState != null) {
                    extendedWorkflowState = "511" ;
                } else {
                    extendedWorkflowState = "521";
                }
            }
        } else {
            if (languageState != null && languageState.intValue() != -1) {
                if (sharedLanguageState != null && languageState.intValue() < sharedLanguageState.intValue()) {
                    languageState = sharedLanguageState;
                }
            } else if (languageState == null) {
                languageState = EntryLoadRequest.STAGING_WORKFLOW_STATE;
            }

            int workflowState = (languageState != null) ? languageState.intValue() : 1;

            boolean isEditable = EntryLoadRequest.WAITING_WORKFLOW_STATE != workflowState;

            if (WorkflowService.EXTERNAL == workflowMode) {
                if (!languagesStates.containsKey(language)) {
                    workflowState = 2;
                } else if (workflowState >= EntryLoadRequest.STAGING_WORKFLOW_STATE) {
                    workflowState = getExternalWorkflowNextStep(getHardLinkedMainObject(contentObject), language) + 1;
                } else {
                    workflowState = 1;
                }
            }

            // current language is editable? --> check if any other is in "waiting for approval",
            // because of our container-level locking system
            if (isEditable) {
                for (Iterator<Map.Entry<String, Integer>> langIterator = languagesStates.entrySet().iterator(); langIterator.hasNext();) {
                    Map.Entry<String, Integer> langEntry = langIterator.next();
                    if (!ContentObject.SHARED_LANGUAGE.equals(langEntry.getKey()) && EntryLoadRequest.WAITING_WORKFLOW_STATE == ((Integer) langEntry.getValue()).intValue()) {
                        isEditable = false;
                        break;
                    }
                }
            }

            if (getWorkflowMode(contentObject) == LINKED) {
                workflowMode = 9;
            }
            if (contentObject.isMarkedForDelete(language)) {
                workflowMode = 6;
                isEditable = false;
            }

            extendedWorkflowState = new StringBuffer(3).append(workflowMode).append(workflowState).append(isEditable ? "1" : "0").toString();
        }
        return extendedWorkflowState;
    }

    public WorkflowInfo getDefaultWorkflowEntry() {

        WorkflowInfo workflowEntry = null;
        String defType = getSettingsBean().getWorkflowDefaultType();

        if ("standard".equals(defType)) {
            workflowEntry = WorkflowInfo.STANDARD;
        } else if ("inactive".equals(defType)) {
            workflowEntry = WorkflowInfo.INACTIVE;
        } else if (externals != null
                && !externals.isEmpty()
                && LicenseActionChecker.isAuthorizedByLicense(
                        "org.jahia.engines.workflow.ExternalWorkflows", 0)) {

            Map.Entry<String, ExternalWorkflow> defaultWokflow = externals
                    .entrySet().iterator().next();
            // create workflow entry
            String name = defaultWokflow.getKey();
            ExternalWorkflow workflow = defaultWokflow.getValue();
            Collection<String> processes = workflow.getAvailableProcesses();
            if (!processes.isEmpty()) {
                String processId = processes.contains(defType) ? defType
                        : processes.iterator().next();
                workflowEntry = new WorkflowInfo(WorkflowService.EXTERNAL,
                        name, processId);
            }
        }

        return workflowEntry != null ? workflowEntry : WorkflowInfo.STANDARD;
    }

}