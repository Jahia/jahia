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

/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.manager;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.FastHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.dao.JahiaWorkflowDAO;
import org.jahia.hibernate.model.JahiaWorkflow;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.workflow.WorkflowService;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 21 avr. 2005
 * Time: 10:19:06
 * To change this template use File | Settings | File Templates.
 */
public class JahiaWorkflowManager {
    public static final String WORKFLOW_CACHE_NAME = "JahiaWorkflowManagerCache";
    private JahiaWorkflowDAO dao = null;
    private Log log = LogFactory.getLog(JahiaWorkflowManager.class);
    private Cache<String, Map<String, Object>> fast = null;
    private CacheService cacheService = null;


    public void setJahiaWorkflowDAO(JahiaWorkflowDAO dao) {
        this.dao = dao;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void createWorkflowEntry(String objectKey, int mode, String workflowName, String processId) {
        JahiaWorkflow workflow = new JahiaWorkflow(objectKey, new Integer(mode), workflowName, processId);
        dao.save(workflow);
        if (fast == null) {
            try {
                fast = cacheService.createCacheInstance(WORKFLOW_CACHE_NAME);
            } catch (JahiaInitializationException e) {
                log.error("Cannot get cache",e);
            }
        }
        if (fast != null)
            fast.remove(objectKey);
    }

    public void updateWorkflowEntry(String objectKey, int mode, String workflowName, String processId) {
        JahiaWorkflow workflow = null;
        synchronized(this) {
            try {
                workflow = dao.findByPK(objectKey);
                workflow.setMode(new Integer(mode));
                workflow.setExternalname(workflowName);
                workflow.setExternalprocess(processId);
                dao.update(workflow);
            } catch (Exception e) {
                workflow = new JahiaWorkflow(objectKey, new Integer(mode), workflowName, processId);
                dao.save(workflow);
            }
        }
        
        if (fast == null) {
            try {
                fast = cacheService.createCacheInstance(WORKFLOW_CACHE_NAME);
            } catch (JahiaInitializationException e) {
                log.error("Cannot get cache",e);
            }
        }
        if (fast != null)
        fast.remove(objectKey);
    }

    public void updateWorkflowEntry(String objectKey, String main) {
        JahiaWorkflow workflow = null;
        synchronized(this) {
            try {
                workflow = dao.findByPK(objectKey);
                workflow.setMainObjectkey(main);
                dao.update(workflow);
            } catch (Exception e) {
                workflow = new JahiaWorkflow(objectKey, null,null,null);
                workflow.setMainObjectkey(main);
                dao.save(workflow);
            }
        }

        if (fast == null) {
            try {
                fast = cacheService.createCacheInstance(WORKFLOW_CACHE_NAME);
            } catch (JahiaInitializationException e) {
                log.error("Cannot get cache",e);
            }
        }
        if (fast != null)
        fast.remove(objectKey);
    }

    public Map<String, Object> getWorkflowEntry(String objectKey) {
        if (log.isDebugEnabled()) {
            log.debug("getWorkflowEntry for object key : " + objectKey);
        }
        Map<String, Object> map = null;
        if (fast == null) {
            try {
                fast = cacheService.createCacheInstance(WORKFLOW_CACHE_NAME);
            } catch (JahiaInitializationException e) {
                log.error("Cannot get cache",e);
            }
        }
        if (fast != null)
            map = (Map<String, Object>) fast.get(objectKey);
        if (map == null) {
            JahiaWorkflow workflow = null;
            Map<String, Object> temp = new FastHashMap(3);
            try {
                workflow = dao.findByPK(objectKey);
                if (workflow != null) {
                    temp.put(WorkflowService.FIELD_MODE, workflow.getMode());
                    temp.put(WorkflowService.FIELD_EXTERNAL_NAME, workflow.getExternalname());
                    temp.put(WorkflowService.FIELD_EXTERNAL_PROCESS, workflow.getExternalprocess());
                    temp.put(WorkflowService.FIELD_MAIN, workflow.getMainObjectkey());
                }
            } catch (Exception e) {
                log.debug("no entry for key " + objectKey, e);
            }
            ((FastHashMap)temp).setFast(true);
            map = temp;
            if (fast != null)
                fast.put(objectKey, map);
        }
        return map;
    }

    public List getLinkedObjectForMain(String mainObjectKey) {
        return dao.getLinkedObjectForMain(mainObjectKey);
    }

    public void clearMainObject(String objectKey) {
        dao.clearMainObject(objectKey);
        fast.flush();
    }
}
