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

 package org.jahia.soap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jahia.bin.Jahia;
import org.jahia.content.ContentObject;
import org.jahia.content.JahiaObject;
import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SoapParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaACLEntry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.cache.Cache;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.version.StateModificationContext;
import org.jahia.services.workflow.WorkflowService;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jun 16, 2003
 * Time: 1:38:37 PM
 * To change this template use Options | File Templates.
 */
public class Publication {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(Publication.class);

    public Publication() {

    }

    public int changeContentObjectState(String userKey, String objectKey, String languageCode, int mode) throws JahiaException, ClassNotFoundException {
        try {
            Cache pageChildCache = ServicesRegistry.getInstance().getCacheService().getCache(JahiaPageService.PAGE_CHILD_CACHE);
            if (pageChildCache == null)
                throw new JahiaException ("Internal Cache error", "Could not get the cache ["+
                        JahiaPageService.PAGE_CHILD_CACHE +"] instance.",
                        JahiaException.CACHE_ERROR, JahiaException.CRITICAL_SEVERITY);

            ObjectKey key = ObjectKey.getInstance(objectKey);
            WorkflowService wrkf = ServicesRegistry.getInstance().getWorkflowService();

            Set languageCodes = new HashSet();
            languageCodes.add(languageCode);

            StateModificationContext stateModifContext = new StateModificationContext(key, languageCodes);
            stateModifContext.setDescendingInSubPages(false);

//            LockService lockRegistry = ServicesRegistry.getInstance().getLockService();

            ContentObject contentObject = (ContentObject) JahiaObject.getInstance(key);
            JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSite(contentObject.getSiteID());
            JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(userKey);
            if (user == null) {
                user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(userKey);
            }
            ProcessingContext jParams = new SoapParamBean(Jahia.getStaticServletConfig().getServletContext(), org.jahia.settings.SettingsBean.getInstance(),System.currentTimeMillis(),site,user);

            JahiaSaveVersion saveVersion = ServicesRegistry.getInstance().getJahiaVersionService().
                    getSiteSaveVersion(site.getID());

            switch (mode) {
                case EntryLoadRequest.STAGING_WORKFLOW_STATE:
                    logger.debug("Switch to staging state");
                    wrkf.changeStagingStatus(contentObject, languageCodes, EntryLoadRequest.STAGING_WORKFLOW_STATE, stateModifContext, jParams ,true);
                    return 1;
//                    contentPage.setWorkflowState(languageCodes,
//                            EntryLoadRequest.STAGING_WORKFLOW_STATE, jParams, stateModifContext);
//                    LockKey lockKey = LockKey.composeLockKey(LockKey.WAITING_FOR_APPROVAL_TYPE,
//                            contentPage.getID(), contentPage.getID());
//                            lockRegistry.release(lockKey, jParams.getUser(), jParams.getSessionID());
                case EntryLoadRequest.ACTIVE_WORKFLOW_STATE:
                    logger.debug("Switch to active state");
                    ActivationTestResults results = wrkf.activate(contentObject, languageCodes, saveVersion, jParams, stateModifContext);
                    return results.getStatus();
                case EntryLoadRequest.WAITING_WORKFLOW_STATE:
                    logger.debug("Switch to waiting state");
                    wrkf.changeStagingStatus(contentObject, languageCodes, EntryLoadRequest.WAITING_WORKFLOW_STATE, stateModifContext, jParams,false);
                    return 1;
                case 4:
                    JahiaBaseACL acl = contentObject.getACL();
                    JahiaAclEntry filter = new JahiaAclEntry();
                    filter.setPermission (JahiaBaseACL.READ_RIGHTS, JahiaAclEntry.ACL_YES);
                    JahiaAclEntry filter2 = new JahiaAclEntry();
                    filter2.setPermission (JahiaBaseACL.WRITE_RIGHTS, JahiaACLEntry.ACL_YES);
                    JahiaAclEntry def = new JahiaAclEntry();
                    def.setPermission (JahiaBaseACL.READ_RIGHTS, JahiaACLEntry.ACL_NO);
                    def.setPermission (JahiaBaseACL.WRITE_RIGHTS, JahiaACLEntry.ACL_NO);
                    def.setPermission (JahiaBaseACL.ADMIN_RIGHTS, JahiaACLEntry.ACL_NEUTRAL);
                    List v = acl.getUsernameList(filter);
                    v.removeAll(acl.getUsernameList(filter2));
                    for (Iterator iterator = v.iterator(); iterator.hasNext();) {
                        JahiaUser user1 = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey((String) iterator.next());
                        acl.setUserEntry(user1, def);
                    }
                    v = acl.getGroupnameListNoAdmin(filter);
                    v.removeAll(acl.getGroupnameList(filter2));
                    for (Iterator iterator = v.iterator(); iterator.hasNext();) {
                        JahiaGroup group = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup((String) iterator.next());
                        acl.setGroupEntry(group, def);
                    }
                    return 1;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }



}
