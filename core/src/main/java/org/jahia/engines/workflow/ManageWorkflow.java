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
package org.jahia.engines.workflow;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.JahiaEngine;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.registries.EnginesRegistry;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockRegistry;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.ExternalWorkflow;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.WorkflowRole;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.security.license.LicenseActionChecker;
import org.jahia.hibernate.model.JahiaAclEntry;

import java.security.Principal;
import java.util.*;

/**
 * @author Thomas Draier
 */
public class ManageWorkflow {
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ManageWorkflow.class);

    private static ManageWorkflow instance = null;
    private static final String JSP_FILE = "/engines/workflow/changeworkflow.jsp";
    private static final String READONLY_JSP = "/engines/workflow/readonly_changeworkflow.jsp";
    public static final String WORKFLOW_ROLES = "workflowRoles";

    /**
     * @return a single instance of the object
     */
    public static synchronized ManageWorkflow getInstance() {
        if (instance == null) {
            instance = new ManageWorkflow();
        }
        return instance;
    }

    public boolean handleActions(final ProcessingContext jParams,
                                 final int mode, final Map engineMap, ContentObject object)
            throws JahiaException, JahiaSessionExpirationException {
        EntryLoadRequest loadRequest = null;
        if ( jParams != null ){
            loadRequest = jParams.getEntryLoadRequest();
        }
        return handleActions(jParams, mode, engineMap, object, object.getParent(loadRequest));
    }

    public boolean handleActions(final ProcessingContext jParams,
                                 final int mode, final Map engineMap, ContentObject object, ContentObject parent)
            throws JahiaException, JahiaSessionExpirationException {
        WorkflowService wfService = WorkflowService.getInstance();
        if (object != null) {
            object = wfService.getHardLinkedMainObject(object);
        }
        if (!engineMap.containsKey("locksActive"))
            engineMap.put("locksActive", Boolean.FALSE);
        switch (mode) {
            case (JahiaEngine.LOAD_MODE) :
                return load(jParams, engineMap, object, parent);
            case (JahiaEngine.UPDATE_MODE) :
                return update(jParams, engineMap, object);
            case (JahiaEngine.CANCEL_MODE) :
                unlock(engineMap, jParams);
                return true;
            case (JahiaEngine.SAVE_MODE) :
                if (engineMap.get("workflowMode") == null) {
                    return true;
                } else {
                    int wfMode = ((Integer) engineMap.get("workflowMode")).intValue();
                    if (wfMode == -1) {
                        return true;
                    }
                    boolean res = save(jParams, engineMap, object);

                    if (res) {
                        if (setRoles(object, engineMap)) {
                            object.setUnversionedChanged();
                        }
                        return res;
                    } else {
                        load(jParams, engineMap, object, parent);
                    }
                }
        }
        return false;
    }

    private boolean load(final ProcessingContext jParams,
                        final Map engineMap,
                        final ContentObject object,
                        ContentObject parent)
            throws JahiaException, JahiaSessionExpirationException {
        WorkflowService service;
        try {
            service = ServicesRegistry.getInstance().getWorkflowService();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }

        final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap
                .get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
        final Locale locale = elh != null && elh.getCurrentLocale() != null ? elh
                .getCurrentLocale() : jParams.getCurrentLocale();

        int workflowMode;
        
        Map workflowNames = null;

        final LockPrerequisitesResult results = LockPrerequisites.getInstance().
                getLockPrerequisitesResult((LockKey) engineMap.get("LockKey"));
        boolean isLocked = false;
        if (results != null) {
            isLocked = results.getReadOnlyTabs().contains(LockPrerequisites.MANAGE_WORKFLOW) ||
                    results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT);
        }
        final boolean readOnly = (results != null && isLocked);

        if (engineMap.containsKey("workflowMode")) {
            workflowMode = ((Integer) engineMap.get(("workflowMode"))).intValue();
        } else if (object == null) {
            workflowMode = ((Integer) engineMap.get(("defaultMode"))).intValue();
        } else {
            workflowMode = service.getWorkflowMode(object);
            engineMap.put("workflowMode", new Integer(workflowMode));
        }

        if (LicenseActionChecker.isAuthorizedByLicense("org.jahia.engines.workflow.ExternalWorkflows", 0)) {
            workflowNames = service.getExternalWorkflowNames(locale);
            String workflowName = null;
            if (engineMap.containsKey("workflowName")) {
                workflowName = (String) engineMap.get("workflowName");
            } else {
                if (object != null) {
                    workflowName = service.getExternalWorkflowName(object);
                }
                if (workflowName == null && workflowNames.size() > 0) {
                    workflowName = (String) workflowNames.keySet().iterator().next();
                }
                engineMap.put("workflowName", workflowName);
            }

            Map<String, Map<String, String>> wfProcesses = new LinkedHashMap();
            for (Map.Entry<String, String> wf : ((Map<String, String>) workflowNames).entrySet()) {
                ExternalWorkflow workflow = service.getExternalWorkflow(wf.getKey());
                Collection<String> availableProcesses = workflow.getAvailableProcesses();
                Map<String, String> processesWithNames = new LinkedHashMap<String, String>();
                for (String wfProcess : availableProcesses) {
                    processesWithNames.put(wfProcess,
                            workflowNames.size() > 1 ? wf.getValue()
                                    + " - "
                                    + workflow
                                            .getProcessName(wfProcess, locale)
                                    : workflow
                                            .getProcessName(wfProcess, locale));
                }
                wfProcesses.put(wf.getKey(), processesWithNames);
            }
            engineMap.put("processes", wfProcesses);
            
            String process = null;
            if (engineMap.containsKey("process")) {
                process = (String) engineMap.get("process");
            } else {
                if (object != null) {
                    process = service.getInheritedExternalWorkflowProcessId(object);
                }
            }
            if (process == null && workflowName != null
                    && !wfProcesses.isEmpty()
                    && !wfProcesses.get(workflowName).isEmpty()) {
                process = wfProcesses.get(workflowName).keySet().iterator()
                        .next();
            }
            engineMap.put("process", process);

            final List roles = new ArrayList();
            engineMap.put("roles", roles);
            if (process != null) {
                ExternalWorkflow workflow = service.getExternalWorkflow(workflowName);
                final Collection rolesList = workflow.getAllActionRoles(process);
                final Map rolesWithNames = new HashMap();

                roles.addAll(rolesList);
                // Handle roles changes
                final List roleMembersList = new ArrayList();
                final List roleInheritedMembersList = new ArrayList();
                for (int i = 0; i < roles.size(); i++) {
                    final String role = (String) roles.get(i);
                    Set appMembers = (Set) engineMap.get("authMembers" + i);
                    if (appMembers == null) {
                        if (object != null) {
                            JahiaGroup grp = service.getRoleGroup(object, role,
                                    false);
                            if (grp != null) {
                                appMembers = getAppMembers(grp);
                            }
                        }
                    }
                    roleMembersList.add(appMembers != null ? appMembers : Collections.emptySet());
                    Set<Principal> s;
                    if (object == null) {
                        WorkflowRole role1 = service.getRole(parent, role, false);
                        s = new HashSet<Principal>(role1.getMembers());
                        s.addAll(role1.getInheritedMembers().keySet());
                    } else {
                        s = new HashSet<Principal>(service.getRole(object, role, false).getInheritedMembers().keySet());
                    }

                    ContentObject current = object == null ? parent : object;
                    Map m = current.getACL().getACL().getRecursedGroupEntries();
                    for (Object key : m.keySet()) {
                        JahiaAclEntry e = (JahiaAclEntry) m.get(key);
                        if (e.getPermission(JahiaBaseACL.ADMIN_RIGHTS)== JahiaAclEntry.ACL_YES) {
                            if (!key.equals("administrators:0")) {
                                s.add(ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup((String) key));
                            }
                        }
                    }
                    m = current.getACL().getACL().getRecursedUserEntries();
                    for (Object key : m.keySet()) {
                        JahiaAclEntry e = (JahiaAclEntry) m.get(key);
                        if (e.getPermission(JahiaBaseACL.ADMIN_RIGHTS)== JahiaAclEntry.ACL_YES) {
                            s.add(ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey((String) key));
                        }
                    }
                    roleInheritedMembersList.add(s);
                    rolesWithNames.put(role, workflow.getActionName(process, role, locale));
                }

                if (object != null) {
                    ContentObject objectHavingTheWorkflow = service.getMainLinkObject(object);
                    if (objectHavingTheWorkflow != null) {
                        engineMap.put("infos", workflow.getCurrentInfo(
                                objectHavingTheWorkflow.getObjectKey().toString(), locale.getLanguage()));
                    }
                }
                engineMap.put("roleMapping", rolesWithNames);
                engineMap.put("workflowRoles", roleMembersList);
                engineMap.put("inheritedWorkflowRoles", roleInheritedMembersList);
            }

        } else {
            workflowNames = new HashMap();
        }
        engineMap.put("workflowNames", workflowNames);
        
        if (workflowMode == WorkflowService.INHERITED || workflowMode == WorkflowService.LINKED) {
            ContentObject mainLinkObject = service.getMainLinkObject(parent);
            engineMap.put("linked", mainLinkObject.getDisplayName(jParams));
            engineMap.put("inheritingParent", service.getInheritingParent(mainLinkObject).getDisplayName(jParams));
            int inheritedMode = service.getInheritedMode(mainLinkObject);
            engineMap.put("inheritedMode", inheritedMode);
            //engineMap.put("inheritedName", service.getInheritedExternalWorkflowName(mainLinkObject));
            if (inheritedMode == WorkflowService.EXTERNAL) {
                engineMap.put("inheritedProcess",
                        service.getExternalWorkflow(
                                service.getInheritedExternalWorkflowName(mainLinkObject))
                                .getProcessName(service.getInheritedExternalWorkflowProcessId(mainLinkObject), elh.getCurrentLocale()));
            }
        }

        engineMap.put("hasParent", Boolean.valueOf(parent != null));
        engineMap.put("selectUsrGrp", EnginesRegistry.getInstance().getEngineByBeanName("selectUGEngine").renderLink(jParams, ""));
        engineMap.put("contentObject", object);
        engineMap.put("fieldsEditCallingEngineName", "manageworkflow_engine");

        if (readOnly) {
            engineMap.put("manageworkflow_engine" + ".fieldForm",
                    ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet((ParamBean) jParams, READONLY_JSP));
        } else {
            engineMap.put("manageworkflow_engine" + ".fieldForm", ServicesRegistry.getInstance().
                    getJahiaFetcherService().fetchServlet((ParamBean) jParams, JSP_FILE));
        }

        return true;
    }

    private boolean acquireLocks(ContentObjectKey object, Map engineMap, ProcessingContext jParams) throws JahiaException {
        Set locks = new HashSet();
        LockRegistry lockReg = LockRegistry.getInstance();
        JahiaUser user = jParams.getUser();

        WorkflowService workflowService = ServicesRegistry.getInstance().getWorkflowService();
//        object = workflowService.getMainLinkObject(object);
        getLocks(object, workflowService, locks);
        boolean locked = true;
        for (Iterator iterator = locks.iterator(); iterator.hasNext() && locked;) {
            LockKey lockKey = (LockKey) iterator.next();
            locked = lockReg.acquire(lockKey, user, user.getUserKey(), jParams.getSessionState().getMaxInactiveInterval());
        }
        engineMap.put("workflowLocks", locks);
        return locked;
    }

    private void unlock(Map engineMap, ProcessingContext jParams) {
        Set locks;
        locks = (Set) engineMap.get("workflowLocks");
        if (locks == null) {
            return;
        }
        LockRegistry lockReg = LockRegistry.getInstance();
        for (Iterator iterator = locks.iterator(); iterator.hasNext();) {
            LockKey lockKey = (LockKey) iterator.next();
            lockReg.release(lockKey, jParams.getUser(), jParams.getUser().getUserKey());
        }
    }

    private void getLocks(ContentObjectKey object,
                          WorkflowService workflowService, Set locks) throws JahiaException {
        LockKey lockKey = LockKey.composeLockKey(LockKey.WORKFLOW_ACTION + "_" +
                object.getType(), object.getIdInType());
        locks.add(lockKey);

        List l = workflowService.getUnlinkedContentObjects(object, false, false);
        for (Iterator iterator = l.iterator(); iterator.hasNext();) {
            ContentObjectKey child = (ContentObjectKey) iterator.next();
            child = workflowService.getMainLinkObject(child);
            if (workflowService.getWorkflowMode(child) == WorkflowService.INHERITED) {
                getLocks(child, workflowService, locks);
            }
        }
    }

    public boolean update(final ProcessingContext jParams, final Map engineMap, final ContentObject object)
            throws JahiaException, JahiaSessionExpirationException {

        if (jParams.getParameter("workflowMode") == null) {
            return true;
        }

        int mode = Integer.parseInt(jParams.getParameter("workflowMode"));

        Integer def = (Integer) engineMap.get("defaultMode");
        if (def != null && def.intValue() == mode) {
            engineMap.remove("workflowMode");
            return true;
        }

        engineMap.put("workflowMode", new Integer(mode));
        if (mode == WorkflowService.EXTERNAL) {
            String workflowName = jParams.getParameter("workflowName");
            String process = jParams.getParameter("process");
            if (workflowName != null) {
                engineMap.put("workflowName", workflowName);
            }
            if (process != null) {
                engineMap.put("process", process);
            }
        } else {
            engineMap.remove("process");
            if (mode == WorkflowService.INACTIVE) {
                engineMap.remove("inheritedMode");
            }
        }
        final List roles = (List) engineMap.get("roles");
        if (roles != null) {
            for (int roleNb = 0; roleNb < roles.size(); roleNb++) {
                String paramName = "authMembers" + roleNb;
                engineMap.put(paramName, getFormMembers(jParams, paramName));
            }
        }
        return true;
    }

    private Set getFormMembers(final ProcessingContext jParams, String parameterName) {
        final String[] authMembersStr = jParams.getParameterValues(parameterName);
        final Set membersSet = new HashSet();
        if (authMembersStr != null) {
            for (int i = 0; i < authMembersStr.length; i++) {
                if (authMembersStr[i].length() > 0) {
                    if (authMembersStr[i].charAt(0) == 'u') {
                        final JahiaUser user = ServicesRegistry.getInstance().
                                getJahiaUserManagerService().
                                lookupUserByKey(authMembersStr[i].substring(1));
                        membersSet.add(user);
                    } else {
                        final JahiaGroup group = ServicesRegistry.getInstance().
                                getJahiaGroupManagerService().
                                lookupGroup(authMembersStr[i].substring(1));
                        membersSet.add(group);
                    }
                }
            }
        }
        return membersSet;
    }

    private Set getAppMembers(final JahiaGroup grp) {
        final Set membersSet = new HashSet();
        final Iterator groupEnum = new EnumerationIterator(grp.members());
        while (groupEnum.hasNext()) {
            membersSet.add(groupEnum.next());
        }
        return membersSet;
    }


    private boolean save(final ProcessingContext jParams, final Map engineMap, final ContentObject object)
            throws JahiaException {
        final WorkflowService service;
        try {
            service = ServicesRegistry.getInstance().getWorkflowService();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }

        boolean result = false;
        int mode = ((Integer) engineMap.get("workflowMode")).intValue();

        Integer def = (Integer) engineMap.get("defaultMode");
        if (def!=null && def.intValue() == mode) {
            return true;
        }

        logger.debug("InvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsi");

        if (mode == WorkflowService.EXTERNAL) {
            try {
                final String workflowName = (String) engineMap.get("workflowName");
                final String process = (String) engineMap.get("process");
                if (workflowName != null) {
                    engineMap.put("workflowName", workflowName);
                    final ExternalWorkflow workflow = service.getExternalWorkflow(workflowName);
                    final Collection names = workflow.getAvailableProcesses();
                    if (process != null && names.contains(process)) {
                        if (service.hasChanged((ContentObjectKey) object.getObjectKey(),
                                WorkflowService.EXTERNAL, workflowName, process)) {
                            boolean locked = acquireLocks((ContentObjectKey) object.getObjectKey(), engineMap, jParams);
                            engineMap.put("locksActive", Boolean.valueOf(!locked));
                            if (locked) {
                                service.setWorkflowMode(object, WorkflowService.EXTERNAL, workflowName, process, jParams);
                                result = true;
                            }
                            unlock(engineMap, jParams);
                        } else {
                            result = true;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error during add operation of a new element we must flush all caches to ensure integrity between database and viewing");
                ServicesRegistry.getInstance().getCacheService().flushAllCaches();
                throw new JahiaException(e.getMessage(), e.getMessage(),
                        JahiaException.DATABASE_ERROR, JahiaException.CRITICAL_SEVERITY, e);
            }
        } else {
            if (service.hasChanged((ContentObjectKey) object.getObjectKey(), mode, null, null)) {
                boolean locked = acquireLocks((ContentObjectKey) object.getObjectKey(), engineMap, jParams);
                engineMap.put("locksActive", Boolean.valueOf(!locked));
                if (locked) {
                    service.setWorkflowMode(object, mode, null, null, jParams);
                    object.setUnversionedChanged();
                    result = true;
                }
                unlock(engineMap, jParams);
            } else {
                result = true;
            }
        }

        return result;
    }

    private boolean setRoles(ContentObject object, Map engineMap) throws JahiaException {
        final WorkflowService service = ServicesRegistry.getInstance().getWorkflowService();
        final List roles = (List) engineMap.get("roles");
        // Handle roles changes
        boolean changed = false;
        if (roles != null) {
            for (int roleNb = 0; roleNb < roles.size(); roleNb++) {
                final String role = (String) roles.get(roleNb);
                JahiaGroup grp = service.getRoleGroup(object, role, true);
                // appply changes to step roles
                changed = applyChangesToGroup(grp, new HashSet((Set) engineMap
                        .get("authMembers" + roleNb)))
                        || changed;
            }
        }
        return changed;
    }
    
    private boolean applyChangesToGroup(JahiaGroup grp, Set memberSet) {
        boolean changed = false;
        final Iterator members = new EnumerationIterator(grp.members());
        while (members.hasNext()) {
            final Principal p = (Principal) members.next();
            if (p instanceof JahiaUser) {
                if (!memberSet.contains(p)) {
                    logger.debug("removed member=" + p.getName());
                    grp.removeMember(p);
                    changed = true;
                } else {
                    memberSet.remove(p);
                }
            } else if (p instanceof JahiaGroup) {
                if (!memberSet.contains(p)) {
                    logger.debug("removed member=" + p.getName());
                    grp.removeMember(p);
                    changed = true;
                } else {
                    memberSet.remove(p);
                }
            }
        }

        final Iterator it = memberSet.iterator();
        while (it.hasNext()) {
            final Principal p = (Principal) it.next();
            grp.addMember(p);
            changed = true;
        }
        
        return changed;
    }

}
