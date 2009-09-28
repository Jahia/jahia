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
package org.jahia.services.content.rules;

import org.apache.log4j.Logger;
import org.drools.spi.KnowledgeHelper;
import org.jahia.api.Constants;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.security.license.LicenseActionChecker;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapperImpl;
import org.jahia.services.content.impl.jahia.JahiaContentNodeImpl;
import org.jahia.services.importexport.ImportJob;
import org.jahia.services.importexport.ProductionJob;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockRegistry;
import org.jahia.services.notification.NotificationEvent;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.ExternalWorkflow;
import org.jahia.services.workflow.ExternalWorkflowHistoryEntry;
import org.jahia.services.workflow.ExternalWorkflowInstanceCurrentInfos;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.security.Principal;

/**
 * Helper class for accessing Jahia service in rules.
 * User: toto
 * Date: 8 janv. 2008
 * Time: 12:04:29
 */
public class Service {
    private static Logger logger = Logger.getLogger(Service.class);
    private static Service instance;

    private Service() {
        super();
    }

    public static synchronized Service getInstance() {
        if (instance == null) {
            instance = new Service();
        }
        return instance;
    }
    
    public void setPermissions(NodeWrapper node, String acl, KnowledgeHelper drools) {
        User user = (User) drools.getWorkingMemory().getGlobal("user");
        StringTokenizer st = new StringTokenizer(acl,"|");
        while (st.hasMoreTokens()) {
            String ace = st.nextToken();
            int colon = ace.lastIndexOf(':');
            String userstring = ace.substring(0, colon);

            if (userstring.equals("self")) {
                userstring = "u:"+ user.getName();
            }

            Node jcrNode = node.getNode();
            try {
                JCRNodeWrapperImpl.changePermissions(jcrNode, userstring, ace.substring(colon+1));
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void revokeAllPermissions(NodeWrapper node) {
        try {
            JCRNodeWrapperImpl.revokeAllPermissions(node.getNode());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setAclInheritanceBreak(NodeWrapper node, boolean aclInheritanceBreak) {
        try {
            JCRNodeWrapperImpl.setAclInheritanceBreak(node.getNode(), aclInheritanceBreak);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void importNode(NodeWrapper node, KnowledgeHelper drools) throws RepositoryException {
        User user = (User) drools.getWorkingMemory().getGlobal("user");
        String uri = node.getPath();
        String name = node.getName();
        LockRegistry lockRegistry = LockRegistry.getInstance();

        StringTokenizer st = new StringTokenizer(name,"_");

        String type = st.nextToken();
        if(type.equals("importFromSite")) {
            try {
                logger.info("Export received from site "+uri);
                ServicesRegistry registry = ServicesRegistry.getInstance();
                String exportedSite = st.nextToken();
                JahiaSite s;
                if (st.nextToken().equals("toSite")) {
                    String siteKey = st.nextToken();
                    s = registry.getJahiaSitesService().getSiteByKey(siteKey);
                    if(s==null) {
                        logger.error("Could not find site "+siteKey+" for importing content of site "+exportedSite+" aborting process");
                        return;
                    }
                } else {
                    s = registry.getJahiaSitesService().getDefaultSite();
                }

                JahiaUser member = ServicesRegistry.getInstance().getJahiaSiteUserManagerService().getMember(s.getID(), user.getName());
                ContentPage homeContentPage = s.getHomeContentPage();

                ProcessingContext jParams = new ProcessingContext(org.jahia.settings.SettingsBean.getInstance(), System.currentTimeMillis(), s, member, homeContentPage);
                jParams.setCurrentLocale(Locale.getDefault());
                jParams.setOperationMode(ProcessingContext.EDIT);
//                jParams.setServerName(m.getRequest().getServerName());
//                jParams.setScheme(m.getRequest().getScheme());
//                jParams.setServerPort(m.getRequest().getServerPort());
                Class<ImportJob> jobClass = ImportJob.class;
                String dkey = homeContentPage.getObjectKey().toString();
                JobDetail jobDetail = BackgroundJob.createJahiaJob("Production job", jobClass, jParams);

                SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();

                Set<LockKey> locks = new HashSet<LockKey>();
                synchronized(lockRegistry) {
                    // Export lock might have been set during differential export with filename as lockkey
                    LockKey lock = LockKey.composeLockKey(LockKey.IMPORT_ACTION + "_" + homeContentPage.getObjectKey().getType(), homeContentPage.getID());
                    lockRegistry.release(lock, jParams.getUser(), name);
                    // Upgrade to import lock
                    lock = LockKey.composeLockKey(LockKey.IMPORT_ACTION + "_" + homeContentPage.getObjectKey().getType(), homeContentPage.getID());
                    locks.add(lock);
                    if (!lockRegistry.acquire(lock, jParams.getUser(), jobDetail.getName(), BackgroundJob.getMaxExecutionTime(), false)) {
                        logger.info("Cannot acquire lock, do not import");
                        return;
                    }
                }

                JobDataMap jobDataMap;
                jobDataMap = jobDetail.getJobDataMap();
                jobDataMap.put(BackgroundJob.JOB_LOCKS, locks);
                jobDataMap.put(ImportJob.TARGET, dkey);
                jobDataMap.put(BackgroundJob.JOB_DESTINATION_SITE, registry.getJahiaSitesService().getSite(homeContentPage.getSiteID()).getSiteKey());
                jobDataMap.put(ImportJob.URI, uri);
                jobDataMap.put(ImportJob.CONTENT_TYPE,"application/zip");
                jobDataMap.put(BackgroundJob.JOB_TYPE, ProductionJob.PRODUCTION_TYPE);
//                jobDataMap.put(ProductionJob.JOB_TITLE, fr);

                jobDataMap.put(ImportJob.PUBLISH_ALL_AT_END, Boolean.valueOf(uri.indexOf("AndPublish") > -1));
                schedulerServ.scheduleJobNow(jobDetail);
            } catch (Exception e) {
                logger.error("Error during import of file " + uri,e);
                ServicesRegistry.getInstance().getCacheService().flushAllCaches();
            }
        }
        if(type.equals("importInto")) {
            try {
                logger.info("Import file "+uri);
                ServicesRegistry registry = ServicesRegistry.getInstance();

                String destKey = st.nextToken() + "_" + st.nextToken();

                ContentObject dest = ContentObject.getContentObjectInstance(ContentObjectKey.getInstance(destKey));
                JahiaSite site = registry.getJahiaSitesService().getSite(dest.getSiteID());

                Locale locale;
                if (uri.endsWith(".zip")) {
                    locale = (Locale) site.getLanguageSettingsAsLocales(true).iterator().next();
                } else {
                    locale = LanguageCodeConverters.languageCodeToLocale(st.nextToken().replace("-","_"));
                }

                JahiaUser member = ServicesRegistry.getInstance().getJahiaSiteUserManagerService().getMember(site.getID(), user.getName());
                ContentPage page = ContentPage.getPage(dest.getPageID());

                ProcessingContext jParams = new ProcessingContext(org.jahia.settings.SettingsBean.getInstance(), System.currentTimeMillis(), site, member, page);
                jParams.setOperationMode(ProcessingContext.EDIT);
                jParams.setCurrentLocale(locale);
//                jParams.setServerName(m.getRequest().getServerName());
//                jParams.setScheme(m.getRequest().getScheme());
//                jParams.setServerPort(m.getRequest().getServerPort());
                Class<ImportJob> jobClass = ImportJob.class;

                JobDetail jobDetail = BackgroundJob.createJahiaJob("Import content to "+destKey, jobClass, jParams);

                SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();

                JobDataMap jobDataMap;
                jobDataMap = jobDetail.getJobDataMap();

                if (dest != null) {
                    Set<LockKey> locks = new HashSet<LockKey>();
                    LockKey lock = LockKey.composeLockKey(LockKey.ADD_ACTION + "_" + dest.getObjectKey().getType(), dest.getID());
                    locks.add(lock);
                    synchronized (lockRegistry) {
                        if (lockRegistry.isAlreadyAcquiredInContext(lock, jParams.getUser(), jParams.getUser().getUserKey())) {
                            lockRegistry.release(lock, jParams.getUser(), jParams.getUser().getUserKey());
                        }
                        if (!lockRegistry.acquire(lock, jParams.getUser(), jobDetail.getName(), BackgroundJob.getMaxExecutionTime())) {
                            logger.info("Cannot acquire lock, do not import");
                            return;
                        }
                    }
                    jobDataMap.put(BackgroundJob.JOB_LOCKS, locks);
                }

                jobDataMap.put(ImportJob.TARGET, destKey);
                jobDataMap.put(BackgroundJob.JOB_DESTINATION_SITE, site.getID());
                jobDataMap.put(ImportJob.URI, uri);
                if (uri.toLowerCase().endsWith(".zip")) {
                    jobDataMap.put("contentType","application/zip");
                } else {
                    jobDataMap.put("contentType","application/xml");
                }
                jobDataMap.put(BackgroundJob.JOB_TYPE,"import");
                jobDataMap.put(ImportJob.DELETE_FILE, true);
                schedulerServ.scheduleJobNow(jobDetail);
            } catch (Exception e) {
                logger.error("Error during import of file " + uri,e);
                ServicesRegistry.getInstance().getCacheService().flushAllCaches();
            }
        }
        if(type.equals("siteImport")) {
            try {
                logger.info("Import site "+uri);
                JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();

                //String sitename = st.nextToken() + "_" + st.nextToken();

                ZipEntry z;
                Node contentNode = node.getNode().getNode(Constants.JCR_CONTENT);
                ZipInputStream zis2 = new ZipInputStream(contentNode.getProperty(Constants.JCR_DATA).getStream());

                Properties infos = new Properties();
                while ( (z = zis2.getNextEntry())!=null) {
                    if ("site.properties".equals(z.getName())) {
                        infos.load(zis2);
                        zis2.closeEntry();

                        boolean siteKeyEx = service.getSiteByKey((String) infos.get("sitekey")) != null || "".equals(infos.get("sitekey"));
                        boolean serverNameEx = service.getSite((String) infos.get("siteservername")) != null || "".equals(infos.get("siteservername"));
                        if (!user.getJahiaUser().isAdminMember(0)) {
                            return;
                        }
                        if (!siteKeyEx && !serverNameEx) {
                            if (!LicenseActionChecker.isAuthorizedByLicense("org.jahia.actions.server.admin.sites.ManageSites", 0)) {
                                if (service.getNbSites() > 0) {
                                    return;
                                }
                            }
                            // site import
                            String tpl = (String) infos.get("templatePackageName");
                            if ("".equals(tpl)) tpl = null;
                            try {
                                Locale locale = null;
                                for (Object obj : infos.keySet()) {
                                    String s = (String) obj;
                                    if (s.startsWith("language.") && s.endsWith(".rank")) {
                                        String code = s.substring(s.indexOf('.')+1, s.lastIndexOf('.'));
                                        String rank = infos.getProperty(s);
                                        if (rank.equals("1")) {
                                            locale = LanguageCodeConverters.languageCodeToLocale(code);
                                        }
                                    }
                                }
                                ProcessingContext ctx = new ProcessingContext(SettingsBean.getInstance(), System.currentTimeMillis(), null, user.getJahiaUser(), null, ProcessingContext.EDIT);
                                service.addSite(user.getJahiaUser(),
                                        infos.getProperty("sitetitle"), infos.getProperty("siteservername"),infos.getProperty("sitekey"),
                                        infos.getProperty("description"), null, locale , tpl, "importRepositoryFile", null, uri, true, false, ctx);
                            } catch (Exception e) {
                                logger.error("Cannot create site " + infos.get("sitetitle"), e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error during import of file " + uri,e);
                ServicesRegistry.getInstance().getCacheService().flushAllCaches();
            }

        }

    }
    
    public void notify(NodeWrapper node, String eventType,
            KnowledgeHelper drools) {
        notify(node, eventType, (User) drools.getWorkingMemory().getGlobal(
                "user"), null);
    }

    public void notify(NodeWrapper node, String eventType,
            Principal subscriber, KnowledgeHelper drools) {
        if (subscriber != null) {
            Set<Principal> subscribers = new HashSet(1);
            subscribers.add(subscriber);
            notify(node, eventType, subscribers, drools);
        }
    }

    public void notifyUser(NodeWrapper node, String eventType, String user,
            KnowledgeHelper drools) {
        JahiaUser jahiaUser = lookupUser(user);
        if (jahiaUser != null) {
            notify(node, eventType, jahiaUser, drools);
        } else {
            logger.warn("Unable to lookup user '" + user
                    + "'. Ignore notification event.");
        }
    }

    public void notifyGroup(NodeWrapper node, String eventType, String group,
            KnowledgeHelper drools) {
        Node jcrNode = node.getNode();
        int siteId = jcrNode instanceof JahiaContentNodeImpl ? ((JahiaContentNodeImpl) jcrNode)
                .getContentObject().getSiteID()
                : ServicesRegistry.getInstance().getJahiaSitesService()
                        .getDefaultSite().getID();
        JahiaGroup jahiaGroup = lookupGroup(group, siteId);
        if (jahiaGroup != null) {
            notify(node, eventType, jahiaGroup, drools);
        } else {
            logger.warn("Unable to lookup group '" + group
                    + "' for the site with ID '" + siteId
                    + "'. Ignore notification event.");
        }
    }

    public void notify(NodeWrapper node, String eventType,
            Set<Principal> subscribers, KnowledgeHelper drools) {
        if (subscribers != null && !subscribers.isEmpty()) {
            notify(node, eventType, (User) drools.getWorkingMemory().getGlobal(
                    "user"), subscribers);
        }
    }

    private void notify(NodeWrapper node, String eventType,
            User eventInitiator, Set<Principal> subscribers) {
        Node jcrNode = node.getNode();
        try {
            NotificationEvent event = new NotificationEvent(JCRContentUtils
                    .getContentNodeName(jcrNode), eventType);
            event.setAuthor(eventInitiator.getName());
            event.setObjectPath(JCRContentUtils.getContentObjectPath(jcrNode));
            if (jcrNode instanceof JahiaContentNodeImpl) {
                JahiaContentNodeImpl contentNode = (JahiaContentNodeImpl) jcrNode;
                event.setSiteId(contentNode.getContentObject().getSiteID());
                event.setPageId(contentNode.getContentObject().getPageID());
            }
            if (subscribers != null && !subscribers.isEmpty()) {
                event.getSubscribers().addAll(subscribers);
            }
            if (logger.isDebugEnabled()) {
                logger.debug(event);
            }
            ServicesRegistry.getInstance().getJahiaEventService()
                    .fireNotification(event);
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        }
    }
    
    /**
     * Returns all users, having explicitly assigned permissions to perform next
     * step operation. Either assigned directly on the object or inherited from
     * parent.
     * 
     * @param node
     *            the current content node
     * @param languageCode
     *            current language code
     * @return the set of all users, having explicitly assigned permissions to
     *         perform next step operation. Either assigned directly on the
     *         object or inherited from parent.
     */
    public Set<Principal> getWorkflowNextStepPrincipals(NodeWrapper node, String languageCode) {
        WorkflowService workflowService = WorkflowService.getInstance();
        try {
            ContentObject obj = ((JahiaContentNodeImpl)node.getNode()).getContentObject();
            ContentObjectKey mainObjKey = workflowService.getMainLinkObject((ContentObjectKey) obj.getObjectKey());

            //Map<String, Set<String>> actions = new HashMap<String, Set<String>>();
            int mode = workflowService.getInheritedMode(obj);
            if (mode == WorkflowService.EXTERNAL) {
                String wn = workflowService.getInheritedExternalWorkflowName(mainObjKey);
                ExternalWorkflow ext = workflowService.getExternalWorkflow(wn);
                ExternalWorkflowInstanceCurrentInfos info = ext.getCurrentInfo(mainObjKey.toString(), languageCode);
                if (info != null) {
                    return workflowService.getRole(mainObjKey, info.getNextRole(), false).getAllMembers();
                }
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Returns all users, having {@link JahiaBaseACL#ADMIN_RIGHTS} on the
     * object.
     * 
     * @param node
     *            the current content node
     * @return the set of all users, having {@link JahiaBaseACL#ADMIN_RIGHTS} on
     *         the object
     */
    public Set<Principal> getWorkflowAdminPrincipals(NodeWrapper node) {
        Set<Principal> s = new HashSet<Principal>();
        ContentObject obj = ((JahiaContentNodeImpl) node.getNode())
                .getContentObject();

        Map<String, JahiaAclEntry> m = obj.getACL().getACL().getRecursedGroupEntries();
        for (Map.Entry<String,JahiaAclEntry> key : m.entrySet()) {
            JahiaAclEntry e = key.getValue();
            if (e.getPermission(JahiaBaseACL.ADMIN_RIGHTS) == JahiaAclEntry.ACL_YES) {
                if (!key.getKey().equals("administrators:0")) {
                    s.add(ServicesRegistry.getInstance()
                            .getJahiaGroupManagerService().lookupGroup( key.getKey()));
                }
            }
        }
        m = obj.getACL().getACL().getRecursedUserEntries();
        for (Object key : m.keySet()) {
            JahiaAclEntry e = (JahiaAclEntry) m.get(key);
            if (e.getPermission(JahiaBaseACL.ADMIN_RIGHTS) == JahiaAclEntry.ACL_YES) {
                s.add(ServicesRegistry.getInstance()
                        .getJahiaUserManagerService().lookupUserByKey(
                                (String) key));
            }
        }

        return s;
    }

    public Set<Principal> getWorkflowPreviousStepPrincipals(NodeWrapper node, String languageCode) {
        WorkflowService workflowService = WorkflowService.getInstance();
        try {
            ContentObject obj = ((JahiaContentNodeImpl)node.getNode()).getContentObject();
            ContentObjectKey mainObjKey = workflowService.getMainLinkObject((ContentObjectKey) obj.getObjectKey());

            //Map<String, Set<String>> actions = new HashMap<String, Set<String>>();
            int mode = workflowService.getInheritedMode(obj);
            if (mode == WorkflowService.EXTERNAL && workflowService.getWorkflowMode(obj) != WorkflowService.LINKED) {
                String wn = workflowService.getInheritedExternalWorkflowName(mainObjKey);
                ExternalWorkflow ext = workflowService.getExternalWorkflow(wn);
                ExternalWorkflowInstanceCurrentInfos info = ext.getCurrentInfo(mainObjKey.toString(), languageCode);
                if (info != null) {
                    return workflowService.getRole(mainObjKey, info.getCurrentRole(), false).getAllMembers();
                }
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public Principal getFirstUserForAction(NodeWrapper node, String languageCode, String action) {
        List<ExternalWorkflowHistoryEntry> history = getWorkflowHistory(node);
        if (history != null) {
            for (ExternalWorkflowHistoryEntry externalWorkflowHistoryEntry : history) {
                if (languageCode.equals(externalWorkflowHistoryEntry.getLanguage()) && action.equals(externalWorkflowHistoryEntry.getAction())) {
                    return ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(externalWorkflowHistoryEntry.getUser());
                }
            }
        }
        return null;
    }

    public Principal getFirstUser(NodeWrapper node, String languageCode) {
        List<ExternalWorkflowHistoryEntry> history = getWorkflowHistory(node);
        if (history != null) {
            for (ExternalWorkflowHistoryEntry externalWorkflowHistoryEntry : history) {
                if (languageCode.equals(externalWorkflowHistoryEntry.getLanguage())) {
                    return ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(externalWorkflowHistoryEntry.getUser());
                }
            }
        }
        return null;
    }

    public Principal getLastUserForAction(NodeWrapper node, String languageCode, String action) {
        List<ExternalWorkflowHistoryEntry> history = getWorkflowHistory(node);
        if (history != null) {
            Collections.reverse(history);
            for (ExternalWorkflowHistoryEntry externalWorkflowHistoryEntry : history) {
                if (languageCode.equals(externalWorkflowHistoryEntry.getLanguage()) && action.equals(externalWorkflowHistoryEntry.getAction())) {
                    return ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(externalWorkflowHistoryEntry.getUser());
                }
                if (languageCode.equals(externalWorkflowHistoryEntry.getLanguage()) && "start process".equals(externalWorkflowHistoryEntry.getAction())) {
                    break;
                }
            }
        }
        return null;
    }

    public Principal getLastUser(NodeWrapper node, String languageCode) {
        List<ExternalWorkflowHistoryEntry> history = getWorkflowHistory(node);
        if (history != null) {
            Collections.reverse(history);
            for (ExternalWorkflowHistoryEntry externalWorkflowHistoryEntry : history) {
                if (languageCode.equals(externalWorkflowHistoryEntry.getLanguage())) {
                    return ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(externalWorkflowHistoryEntry.getUser());
                }
                if (languageCode.equals(externalWorkflowHistoryEntry.getLanguage()) && "start process".equals(externalWorkflowHistoryEntry.getAction())) {
                    break;
                }
            }
        }
        return null;
    }

    private List<ExternalWorkflowHistoryEntry> getWorkflowHistory(NodeWrapper node) {
        WorkflowService workflowService = WorkflowService.getInstance();
        try {
            ContentObject obj = ((JahiaContentNodeImpl)node.getNode()).getContentObject();
            ContentObjectKey mainObjKey = workflowService.getMainLinkObject((ContentObjectKey) obj.getObjectKey());

            //Map<String, Set<String>> actions = new HashMap<String, Set<String>>();
            int mode = workflowService.getInheritedMode(obj);
            if (mode == WorkflowService.EXTERNAL) {
                String wn = workflowService.getInheritedExternalWorkflowName(mainObjKey);
                ExternalWorkflow ext = workflowService.getExternalWorkflow(wn);
                List<ExternalWorkflowHistoryEntry> history = ext.getWorkflowHistoryByObject(mainObjKey.toString());
                return history;
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }


    private static JahiaUser lookupUser(String username) {
        return ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(username);        
    }

    private static JahiaGroup lookupGroup(String group, int siteId) {
        return ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(siteId, group);        
    }
}
