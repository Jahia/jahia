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

package org.jahia.services.content.automation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.drools.spi.KnowledgeHelper;
import org.jahia.api.Constants;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.security.license.LicenseActionChecker;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.content.JCRNodeWrapperImpl;
import org.jahia.services.content.impl.jahia.JahiaContainerNodeImpl;
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
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 8 janv. 2008
 * Time: 12:04:29
 * To change this template use File | Settings | File Templates.
 */
public class Service {
    private static Logger logger = Logger.getLogger(Service.class);
    private static Service instance;

    private Service() {
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
                Class jobClass = ImportJob.class;
                String dkey = homeContentPage.getObjectKey().toString();
                JobDetail jobDetail = BackgroundJob.createJahiaJob("Production job", jobClass, jParams);

                SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();

                Set locks = new HashSet();
                synchronized(lockRegistry) {
                    // Export lock might have been set during differential export with filename as lockkey
                    LockKey lock = LockKey.composeLockKey(LockKey.IMPORT_ACTION + "_" + homeContentPage.getObjectKey().getType(), homeContentPage.getID(), homeContentPage.getID());
                    lockRegistry.release(lock, jParams.getUser(), name);
                    // Upgrade to import lock
                    lock = LockKey.composeLockKey(LockKey.IMPORT_ACTION + "_" + homeContentPage.getObjectKey().getType(), homeContentPage.getID(), homeContentPage.getID());
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

                jobDataMap.put(ImportJob.PUBLISH_ALL_AT_END, new Boolean(uri.indexOf("AndPublish")>-1));
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
                Class jobClass = ImportJob.class;

                JobDetail jobDetail = BackgroundJob.createJahiaJob("Import content to "+destKey, jobClass, jParams);

                SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();

                JobDataMap jobDataMap;
                jobDataMap = jobDetail.getJobDataMap();

                if (dest != null) {
                    Set locks = new HashSet();
                    LockKey lock = LockKey.composeLockKey(LockKey.ADD_ACTION + "_" + dest.getObjectKey().getType(), dest.getID(), dest.getID());
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
                                for (Iterator iterator = infos.keySet().iterator(); iterator.hasNext();) {
                                    String s = (String) iterator.next();
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
        User user = (User) drools.getWorkingMemory().getGlobal("user");
        Node jcrNode = node.getNode();
        NotificationEvent event = new NotificationEvent();
        event.setAuthor(user.getName());
        event.setEventType(eventType);
        event.setTimestamp(System.currentTimeMillis());
        try {
            event.setObjectPath(jcrNode.getPath());
            if (jcrNode instanceof JahiaContentNodeImpl) {
                JahiaContentNodeImpl contentNode = (JahiaContentNodeImpl) jcrNode;
                event.setSiteId(contentNode.getContentObject().getSiteID());
                event.setObjectKey(contentNode.getContentObject()
                        .getObjectKey().getKey());
                if (jcrNode instanceof JahiaContainerNodeImpl) {
                    // fix path
                    JahiaContainerNodeImpl containerNode = (JahiaContainerNodeImpl) jcrNode;
                    event.setObjectPath(StringUtils.substringBeforeLast(jcrNode
                            .getPath(), "/")
                            + "/ContentContainerList_"
                            + ((ContentContainer) containerNode
                                    .getContentObject())
                                    .getParentContainerListID()
                            + "/"
                            + event.getObjectKey());
                }
            } else {
                event.setObjectKey(StringUtils.substringAfterLast(jcrNode
                        .getPath(), "/"));
            }
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        }
        if (logger.isDebugEnabled()) {
        	logger.debug(event);
        }
        AggregatedNotificationEvent aggregatedEvent = (AggregatedNotificationEvent) drools
                .getWorkingMemory().getGlobal("aggregatedNotificationEvent");
        if (aggregatedEvent != null) {
            aggregatedEvent.add(event);
        }
    }
}
