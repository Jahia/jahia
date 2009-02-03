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

 package org.jahia.services.workflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.StructuralRelationship;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SoapParamBean;
import org.jahia.services.cache.CacheService;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.fields.ContentField;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.version.StateModificationContext;

/**
 *
 *
 * Created by IntelliJ IDEA.
 * Date: Dec 4, 2003
 * Copyright Codeva 2003
 * @author Thomas Draier
 */
public class WorkflowEventListener extends JahiaEventListener {
    private static Logger logger = Logger.getLogger(WorkflowEventListener.class);

    private WorkflowService workflowService;
    private JahiaSitesService jahiaSitesService;
    private JahiaUserManagerService jahiaUserManagerService;
    private CacheService cacheService;

    public WorkflowService getWorkflowService() {
        return workflowService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public JahiaSitesService getJahiaSitesService() {
        return jahiaSitesService;
    }

    public void setJahiaSitesService(JahiaSitesService jahiaSitesService) {
        this.jahiaSitesService = jahiaSitesService;
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public JahiaUserManagerService getJahiaUserManagerService() {
        return jahiaUserManagerService;
    }

    public void setJahiaUserManagerService(JahiaUserManagerService jahiaUserManagerService) {
        this.jahiaUserManagerService = jahiaUserManagerService;
    }

    public void objectChanged(WorkflowEvent je) {
        ContentObject object = (ContentObject) je.getObject();
        if (object != null) {
            if (je.isNew() && object instanceof ContentContainer) {
                try {
                    JahiaContainerDefinition def = (JahiaContainerDefinition) ContentDefinition.getContentDefinitionInstance(object.getDefinitionKey(EntryLoadRequest.STAGED));

                    ExtendedNodeDefinition nd = def.getContainerListNodeDefinition();
                    if (nd != null) {
                        String workflow = nd.getWorkflow();
                        if (workflow != null) {
                            ProcessingContext jParams = je.getProcessingContext();

                            JahiaUser user = je.getUser();
                            JahiaSite site =  jahiaSitesService.getSite(object.getSiteID());

                            if (jParams == null) {
                                jParams = Jahia.getThreadParamBean();
                                if (jParams == null) {
                                    jParams = new SoapParamBean(Jahia.getStaticServletConfig().getServletContext(),org.jahia.settings.SettingsBean.getInstance(),System.currentTimeMillis(), site, user);
                                }
                            }

                            if (workflow.toLowerCase().equals("none")) {
                                workflowService.setWorkflowMode(object, WorkflowService.INACTIVE, null, null, jParams);
                            } else if (workflow.toLowerCase().equals("inherited")) {
                                workflowService.setWorkflowMode(object, WorkflowService.INHERITED, null, null, jParams);
                            } else if (workflow.toLowerCase().equals("jahia")) {
                                workflowService.setWorkflowMode(object, WorkflowService.JAHIA_INTERNAL, null, null, jParams);
                            } else if (workflow.toLowerCase().startsWith("nstep-")) {
                                workflowService.setWorkflowMode(object, WorkflowService.EXTERNAL, "N-Step",workflow.substring(6), jParams);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Cannot set workflow on "+object.getObjectKey(),e);
                }
            }


            ContentObjectKey.flushCache(object);
        }
    }

    public void aggregatedObjectChanged(JahiaEvent je) {
        List<?> allEvents = (List<?>) je.getObject();

        Set<String> viewed = new HashSet<String>();

        for (int i = 0; i < allEvents.size(); ) {
            WorkflowEvent we = (WorkflowEvent) allEvents.get(i);
            ContentObject object = (ContentObject) we.getObject();
            if (object == null) {
                allEvents.remove(i);                
            } else {
                String k = object.getObjectKey() + we.getLanguageCode();
                if (viewed.contains(k)) {
                    allEvents.remove(i);
                } else {
                    i++;
                }
                viewed.add(k);
            }
        }

        Map<ContentObjectKey, Integer> allTops = new HashMap<ContentObjectKey, Integer>();
        for (int i = allEvents.size()-1; i >=0 ; i--) {
            WorkflowEvent we = (WorkflowEvent) allEvents.get(i);
            ContentObject contentObject = (ContentObject) we.getObject();
            ContentObjectKey key = (ContentObjectKey) contentObject.getObjectKey();
            try {
                ContentObjectKey k = workflowService.getTopLinkedObject(key);
                if (k != null) {
                    allTops.put(k, new Integer(contentObject.getSiteID()));
                }
            } catch (JahiaException e) {
                logger.error("Cannot get main object", e);
            }
        }
        for (Iterator<ContentObjectKey> iterator = allTops.keySet().iterator(); iterator.hasNext();) {
            workflowService.flushCacheForObjectChanged(iterator.next());
        }
        for (int i = allEvents.size()-1; i >=0 ; i--) {
            WorkflowEvent we = (WorkflowEvent) allEvents.get(i);
            myObjectChanged(we);
        }
        for (Iterator<ContentObjectKey> iterator = allTops.keySet().iterator(); iterator.hasNext();) {
            ContentObjectKey contentObjectKey = iterator.next();
            try {
                if (workflowService.getInheritedMode(contentObjectKey) == WorkflowService.INACTIVE) {
                    workflowService.storeLanguageState(contentObjectKey, allTops.get(contentObjectKey).intValue());
                }
            } catch (JahiaException e) {
                logger.error("Cannot reset workflow states", e);
            }
        }
    }

    public void myObjectChanged(WorkflowEvent we) {
        ContentObject object = (ContentObject) we.getObject();
        if(object ==null) return;
        String languageCode = we.getLanguageCode();
        if (logger.isDebugEnabled()) {
            logger.debug("Object changed : "+object.getObjectKey()+ " , "+languageCode);
        }

        JahiaUser user = we.getUser();
        boolean descendingInSubPages = we.isShouldDescend();

        if (logger.isDebugEnabled()) {
            logger.debug("A new workflow event has been fired for object "+object.getObjectKey() + "(" + object.getClass() + "), language "+languageCode );
        }
        try {
            int mode;
            ContentObject mainObject = workflowService.getMainLinkObject(object);

            if ( mainObject == null ){
                // in case of JahiaFields used as metadata, they don't have parent container or page
                // so, what should the correct behavior here ?
                return;
            }

            mode = workflowService.getInheritedMode(mainObject);

            if (mode != WorkflowService.INACTIVE) {
                workflowService.storeLanguageState((ContentObjectKey) mainObject.getObjectKey(),languageCode,EntryLoadRequest.STAGING_WORKFLOW_STATE, mainObject.getSiteID());
            }

            //JahiaSite site =  ServicesRegistry.getInstance().getJahiaSitesService().getSite(mainObject.getSiteID());
            JahiaSite site =  jahiaSitesService.getSite(object.getSiteID());
            if (user == null) {
                user = jahiaUserManagerService.lookupUser(JahiaUserManagerService.GUEST_USERNAME);
            }

            ProcessingContext jParams = we.getProcessingContext();

            if (jParams == null) {
                jParams = Jahia.getThreadParamBean();
                if (jParams == null) {
                    jParams = new SoapParamBean(Jahia.getStaticServletConfig().getServletContext(),org.jahia.settings.SettingsBean.getInstance(),System.currentTimeMillis(), site, user);
                }
            }

            switch (mode) {
                case WorkflowService.INACTIVE:
                    Set<String> langs = new HashSet<String>();
                    if (languageCode.equals(ContentObject.SHARED_LANGUAGE)) {
                        for (SiteLanguageSettings currentLang : jahiaSitesService.getSite(object.getSiteID()).getLanguageSettings()) {
                            langs.add(currentLang.getCode());
                        }
                    } else {
                        langs.add(languageCode);
                    }
                    boolean versioningActive = site.isVersioningEnabled();
                    JahiaSaveVersion saveVersion = new JahiaSaveVersion (true, versioningActive);
                    StateModificationContext stateModifContext = new StateModificationContext(mainObject.getObjectKey(), langs, descendingInSubPages);
                    object = ContentObject.getContentObjectInstance(object.getObjectKey());
                    ActivationTestResults testActivationResults = object.isValidForActivation(langs, jParams, stateModifContext);
                    if (!object.getStagingLanguages(false,true).isEmpty() && testActivationResults.getStatus() != ActivationTestResults.FAILED_OPERATION_STATUS) {
                        ContentObject parent = object.getParent(null);
                        if (parent != null) {
                            if (parent.getPickedObject(StructuralRelationship.CHANGE_PICKER_LINK) != null && object.getPickedObject(StructuralRelationship.CHANGE_PICKER_LINK) == null) {
                                break;
                            }
                        }
                        object.activate(langs, versioningActive, saveVersion, user, jParams, stateModifContext);
                        if(object instanceof ContentField) {
                            object.getParent(jParams.getEntryLoadRequest()).activateMetadatas(langs, versioningActive, saveVersion, user, jParams, stateModifContext);
                        } else {
                            object.activateMetadatas(langs, versioningActive, saveVersion, user, jParams, stateModifContext);
                        }
                    }
                    break;
                case WorkflowService.JAHIA_INTERNAL:
                    break;
                case WorkflowService.EXTERNAL:
                    String name = workflowService.getInheritedExternalWorkflowName(mainObject);
                    String processId = workflowService.getInheritedExternalWorkflowProcessId(mainObject);
                    ExternalWorkflow workflow = workflowService.getExternalWorkflow(name);
                    synchronized(this) {
                        if (languageCode.equals(ContentObject.SHARED_LANGUAGE)) {
                            for (SiteLanguageSettings currentLang : jahiaSitesService.getSite(object.getSiteID()).getLanguageSettings()) {
                                if (!workflow.isProcessStarted(processId,mainObject.getObjectKey().toString(), currentLang.getCode())) {
                                    workflow.initProcess(processId,mainObject.getObjectKey().toString(), currentLang.getCode(), jParams);
                                }
                            }
                        } else {
                            if (!workflow.isProcessStarted(processId,mainObject.getObjectKey().toString(), languageCode)) {
                                workflow.initProcess(processId,mainObject.getObjectKey().toString(), languageCode, jParams);
                            } else if (workflow.needToRestartProcess(processId, mainObject.getObjectKey().toString(), languageCode)){
                                workflow.abortProcess(processId,mainObject.getObjectKey().toString(), languageCode, jParams);
                                workflow.initProcess(processId,mainObject.getObjectKey().toString(), languageCode, jParams);
                            }
                        }
                    }
                    break;
            }
        } catch(JahiaPageNotFoundException e) {
            // We try to manage workflow for a non existent page probably a staging page removed so non existent in any state in jahia
            logger.debug("Page not found",e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }
}
