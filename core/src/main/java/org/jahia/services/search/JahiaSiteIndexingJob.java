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
package org.jahia.services.search;

import org.quartz.*;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.bin.Jahia;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.RamJob;
import org.jahia.services.search.indexingscheduler.RuleEvaluationContext;
import org.jahia.services.search.indexingscheduler.IndexationRuleInterface;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.manager.JahiaContainerDefinitionManager;
import org.jahia.hibernate.model.indexingjob.JahiaContainerIndexingJob;
import org.jahia.content.TreeOperationResult;
import org.jahia.content.NodeOperationResult;
import org.springframework.context.ApplicationContext;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 6 mars 2007
 * Time: 16:58:29
 *
 * A scheduled full site re-indexation
 */
public class JahiaSiteIndexingJob extends BackgroundJob implements RamJob {

    public static final String SITE_INDEXATION_JOB_TYPE = "siteindexation";
    public static final String JOB_NAME_PREFIX = "JahiaSiteIndexingJob_";
    public static final String JOB_GROUP_NAME = "JahiaSiteIndexingJob";
    static final String TRIGGER_NAME_PREFIX = "JahiaSiteIndexingJobTrigger_";

    public static final String SITE_INDEXATION_JOBNAME = "siteReindexation.jobName";
    public static final String SITE_INDEXATION_STATUS = "siteReindexation.status";
    public static final String LAST_INDEXED_PAGE = "siteReindexation.lastIndexingPage";
    public static final String INTERRUPT_STATUS = "siteReindexation.interruptStatus";
    public static final String INTERRUPT_STATUS_ABORT_REQUESTED = "ABORT_REQUESTED";
    public static final String INTERRUPT_STATUS_ABORTED = "ABORTED";
    public static final String INTERRUPT_STATUS_INTERRUPT_REQUESTED = "INTERRUPT_REQUESTED";
    public static final String INTERRUPT_STATUS_RESUME_REQUESTED = "RESUME_REQUESTED";
    public static final String INTERRUPT_STATUS_RESUMED = "RESUMED";
    public static final String INTERRUPT_STATUS_UNKNOWN = "UNKNOWN";
    public static final String INTERRUPT_STATUS_NOT_SET = "NOT_SET";
    public static final String INTERRUPT_TIME = "INTERRUPT_TIME";
    
    public static final String ADD_JOBS_KEY = "toAdd";
    public static final String REMOVE_JOBS_KEY = "toRemove";

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JahiaSiteIndexingJob.class);

    private byte[] lock = new byte[]{};

    private int siteId = -1;

    private Map<String, List<String>> concurrentJobs = new HashMap<String, List<String>>();

    public JahiaSiteIndexingJob () {
    }

    public void executeJahiaJob(JobExecutionContext jobExecutionContext, ProcessingContext processingContext)
    throws Exception {
        TreeOperationResult result = new TreeOperationResult();
        String serverId = ServicesRegistry.getInstance().getClusterService().getServerId();
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        jobDataMap.putAsString(BackgroundJob.JOB_BEGIN, System.currentTimeMillis());//execution begin
        jobDataMap.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_RUNNING);//status
        jobDataMap.put(BackgroundJob.JOB_SERVER, serverId);
        
        String siteKey = jobDataMap.getString(JOB_SITEKEY);
        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(siteKey);
        siteId = site.getID();
        Properties newSettings = new Properties();
        String userKey = (String)jobDataMap.get(JOB_USERKEY);
        newSettings.setProperty(serverId + "_" + BackgroundJob.JOB_CREATED,
                jobDataMap.getString(BackgroundJob.JOB_CREATED));
        newSettings.setProperty(serverId + "_" + BackgroundJob.JOB_BEGIN,
                jobDataMap.getString(BackgroundJob.JOB_BEGIN));
        newSettings.setProperty(serverId + "_" + BackgroundJob.JOB_STATUS,
                jobDataMap.getString(BackgroundJob.JOB_STATUS));
        newSettings.setProperty(serverId + "_" + JahiaSiteIndexingJob.INTERRUPT_STATUS,"");
        ServicesRegistry.getInstance().getJahiaSitesService().updateSiteProperties(site, newSettings);
        JahiaUser user = null;
        try {
            user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(userKey);
        } catch ( Exception t ){
            logger.info("Exception retrieving user " + userKey,t);
            logger.info("site id not found, re-indexation aborted");
            jobDataMap.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_FAILED);
            NodeOperationResult nodeOperationResult =
                    new NodeOperationResult(null,Jahia.getThreadParamBean().getLocale().getDisplayName()
                            ,"user not found");
            result.appendError(nodeOperationResult);
            jobDataMap.put(BackgroundJob.RESULT,result);
            Properties resultSettings = new Properties();
            resultSettings.setProperty(serverId + "_" + JOB_STATUS,STATUS_FAILED);
            resultSettings.setProperty(serverId + "_" + BackgroundJob.RESULT,"user: " + userKey + " not found");
            ServicesRegistry.getInstance().getJahiaSitesService().updateSiteProperties(site, resultSettings);
            return;
        }

        int lastProcessedPage = this.getLastProcessedPage();

        // start the chrono...
        long startTime = System.currentTimeMillis();

        try {
            String interruptStatus = site.getSettings().getProperty(serverId + "_" +
                    JahiaSiteIndexingJob.INTERRUPT_STATUS,"");
            if ( JahiaSiteIndexingJob.INTERRUPT_STATUS_RESUME_REQUESTED.equals(interruptStatus) ){
                Properties resultSettings = new Properties();
                resultSettings.setProperty(serverId + "_" + JahiaSiteIndexingJob.INTERRUPT_STATUS,
                        JahiaSiteIndexingJob.INTERRUPT_STATUS_RESUMED);
                ServicesRegistry.getInstance().getJahiaSitesService().updateSiteProperties(site, resultSettings);                
            }

            // remove all object with siteId
            JahiaSearchService searchService = ServicesRegistry.getInstance()
                    .getJahiaSearchService();
            logger.info("starting re-indexing site ["
                    + (System.currentTimeMillis() - startTime) +
                    "ms]");
            List<Locale> locales = new ArrayList<Locale>();
            locales.add(Locale.ENGLISH);
            EntryLoadRequest loadRequest = new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,0,locales);
            loadRequest.setWithMarkedForDeletion(true);
            if ( lastProcessedPage == -1 ){
                searchService.removeFromSearchEngine(siteId, JahiaSearchConstant.JAHIA_ID,
                    NumberPadding.pad(siteId), user,false,false, null);
            }
            JahiaContainersService containerService = ServicesRegistry.getInstance().getJahiaContainersService();
            JahiaPageService pageService = ServicesRegistry.getInstance()
                    .getJahiaPageService();

            List<Integer> pageIds = pageService.getPageIdsInSiteOrderById(siteId);

            List<RemovableDocument> toRemove = new ArrayList<RemovableDocument>();
            List<IndexableDocument> toAdd = new ArrayList<IndexableDocument>();
            int batchSize = 100;
            try {
                 batchSize = Integer.parseInt(ServicesRegistry.getInstance()
                        .getJahiaSearchService().getIndexationConfig()
                        .getProperty(JahiaSearchConfigConstant.SEARCH_INDEXING_JOB_BATCH_SIZE));
            } catch ( Exception t) {
                 batchSize = 100;
            }
            ApplicationContext ctx = SpringContextSingleton.getInstance().getContext();
            JahiaContainerDefinitionManager ctnDefManager = (JahiaContainerDefinitionManager)
                    ctx.getBean(JahiaContainerDefinitionManager.class.getName());
            for (Integer id : pageIds){
                if ( !checkInterruptStatus(jobDataMap,result,serverId) ){
                    return;
                }
                IndexationRuleInterface rule = null;
                if ( lastProcessedPage != -1 ){
                    if ( id.intValue() != lastProcessedPage ) {
                        continue;
                    } else {
                        lastProcessedPage = -1;
                    }
                }
                ContentPage contentPage = null;
                try {
                    contentPage = ContentPage.getPage(id.intValue());
                } catch ( Exception t ){
                    logger.debug("Error retrieving page " + id + " for indexation. Page won't be re-indexed",t);
                    continue;
                }
                if ( contentPage != null ){
                    RuleEvaluationContext ruleCtx = new RuleEvaluationContext(contentPage.getObjectKey(), contentPage, processingContext,user);
                    try {
                        rule = ServicesRegistry.getInstance()
                            .getJahiaSearchIndexationService()
                            .evaluateContentIndexationRules(ruleCtx);
                    } catch ( Exception t ){
                        logger.debug("Exeption evaluating indexation rule for content page id="
                                + id, t);
                    }
                }
                if ( rule == null || rule.getIndexationMode() != IndexationRuleInterface.DONT_INDEX ){
                    List<IndexableDocument> docs = null;
                    try {
                        docs = ServicesRegistry.getInstance().getJahiaSearchService()
                            .getIndexableDocumentsForPage(id.intValue(), user);
                    } catch ( Exception t ){
                        logger.debug("Exception preparing indexing document for page " + id, t);
                    }
                    if ( docs != null ){
                        for (IndexableDocument doc : docs ){
                            if ( doc instanceof RemovableDocument ){
                                toRemove.add((RemovableDocument)doc);
                            } else {
                                toAdd.add(doc);
                            }
                        }
                        if ( toAdd.size() > batchSize ){
                            synchronized (lock){
                                try {
                                    lock.wait();
                                } catch ( Exception t ){
                                }
                            }
                            this.processBachIndexation(toRemove,toAdd);
                            if ( ! saveLastProcessedPage(id) ){
                                return;
                            }
                            toRemove.clear();
                            toAdd.clear();
                        }
                    }
                    try {
                        ctnDefManager.invalidateContainerDefinitionInTemplate(contentPage.getPageTemplateID(loadRequest));
                    } catch ( Exception t ){
                        logger.debug("Error retrieving page " + id + " for indexation. Page won't be re-indexed",t);
                        continue;
                    }
                }                    
                List<Integer> ctnListIds = containerService.getContainerListIDsInPage(contentPage,loadRequest);
                ContentContainer ctn = null;
                JahiaContainerIndexingJob  indJob = null;
                for ( Integer listId : ctnListIds ){
                    if ( !checkInterruptStatus(jobDataMap,result,serverId) ){
                        return;
                    }
                    List<Integer> ctnIds = containerService.getctnidsInList(listId.intValue(), loadRequest);
                    for (Integer ctnIdInt : ctnIds) {
                        rule = null;
                        Integer ctnId = ctnIdInt;
                        ctn = null;
                        try {
                            ctn = ContentContainer.getContainer(ctnId.intValue());
                        } catch ( Exception t ){
                            continue;
                        }
                        if ( !checkInterruptStatus(jobDataMap,result,serverId) ){
                            return;
                        }
                        if ( ctn != null ){
                            RuleEvaluationContext ruleCtx = new RuleEvaluationContext(ctn.getObjectKey(), ctn, processingContext,user);
                            try {
                                rule = ServicesRegistry.getInstance()
                                    .getJahiaSearchIndexationService()
                                    .evaluateContentIndexationRules(ruleCtx);
                            } catch ( Exception t ){
                                logger.debug("Exeption evaluating indexation rule for container id="
                                        + ctnId, t);
                            }
                            if ( rule == null || rule.getIndexationMode() != IndexationRuleInterface.DONT_INDEX ){
                                indJob = new JahiaContainerIndexingJob (ctnId.intValue(), 0);
                                try {
                                    indJob.prepareBatchIndexation(toRemove,toAdd,user);
                                } catch ( Exception t  ){
                                    logger.debug("Exception preparing indexing document for container " + ctnId, t);
                                    continue;
                                }
                                if ( toAdd.size() > batchSize ){
                                    synchronized (lock){
                                        try {
                                            lock.wait();
                                        } catch ( Exception t ){
                                        }
                                    }
                                    this.processBachIndexation(toRemove,toAdd);
                                    if ( ! saveLastProcessedPage(id) ){
                                        return;
                                    }
                                    toRemove.clear();
                                    toAdd.clear();
                                }
                            }
                        }
                    }
                }
            }
            this.processBachIndexation(toRemove,toAdd);
            jobDataMap.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_SUCCESSFUL);
            jobDataMap.put(BackgroundJob.RESULT,result);

            Properties resultSettings = new Properties();
            resultSettings.setProperty(serverId + "_" + BackgroundJob.RESULT,STATUS_SUCCESSFUL);
            resultSettings.setProperty(serverId + "_" + JOB_STATUS,STATUS_SUCCESSFUL);            
            ServicesRegistry.getInstance().getJahiaSitesService().updateSiteProperties(site, resultSettings);            

        } catch ( Exception t ){
            logger.info("Exception re-indexing site " + siteId, t);
            jobDataMap.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_FAILED);
            NodeOperationResult nodeOperationResult =
                    new NodeOperationResult(null,Jahia.getThreadParamBean().getLocale().getDisplayName()
                            ,"Exception re-indexing site " + t.getMessage());
            result.appendError(nodeOperationResult);
            jobDataMap.put(BackgroundJob.RESULT,result);
            Properties resultSettings = new Properties();
            resultSettings.setProperty(serverId + "_" + BackgroundJob.RESULT,"Exception re-indexing site " + t.getMessage());
            resultSettings.setProperty(serverId + "_" + JOB_STATUS,STATUS_FAILED);            
            ServicesRegistry.getInstance().getJahiaSitesService().updateSiteProperties(site, resultSettings);            
        } finally {
            logger.info("indexing time ["
                    + (System.currentTimeMillis() - startTime) +
                    "ms]");
        }
    }

    public void postExecution(JobExecutionContext jobExecutionContext,
                              ProcessingContext processingContext){
        if ( this.siteId != -1 ){
            try {
                String serverId = ServicesRegistry.getInstance().getClusterService().getServerId();
                JobDetail jobDetail = jobExecutionContext.getJobDetail();
                JobDataMap jobDataMap = jobDetail.getJobDataMap();
                JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSite(this.siteId);
                Properties newSettings = new Properties();
                copyData(newSettings,jobDataMap,BackgroundJob.JOB_BEGIN,
                        serverId + "_" + BackgroundJob.JOB_BEGIN,null);
                copyData(newSettings,jobDataMap,BackgroundJob.JOB_CREATED,
                        serverId + "_" + BackgroundJob.JOB_CREATED,null);
                copyData(newSettings,jobDataMap,BackgroundJob.JOB_DURATION,
                        serverId + "_" + BackgroundJob.JOB_DURATION,null);
                copyData(newSettings,jobDataMap,BackgroundJob.JOB_END,
                        serverId + "_" + BackgroundJob.JOB_END,null);
                copyData(newSettings,jobDataMap,BackgroundJob.JOB_SCHEDULED,
                        serverId + "_" + BackgroundJob.JOB_SCHEDULED,null);
                copyData(newSettings,jobDataMap,BackgroundJob.JOB_STATUS,
                        serverId + "_" + BackgroundJob.JOB_STATUS,"");
                ServicesRegistry.getInstance().getJahiaSitesService().updateSiteProperties(site, newSettings);
            } catch ( Exception t ){
                logger.debug("Exception occured in postExecution",t);
            }
        }
    }

    public void addConcurrentJobs(List<? extends IndexableDocument> jobs,
            boolean toAdd) {
        synchronized (this.concurrentJobs) {
            String key = toAdd ? ADD_JOBS_KEY : REMOVE_JOBS_KEY;
            List<String> list = this.concurrentJobs.get(key);
            if (list == null) {
                list = new ArrayList<String>();
                this.concurrentJobs.put(key, list);
            }
            for (IndexableDocument doc : jobs) {
                list.add(doc.getKeyFieldName() + "_" + doc.getKey());
            }
        }
    }

    protected void processBachIndexation(List<RemovableDocument> toRemove, List<IndexableDocument> toAdd) throws Exception {
        synchronized (this.concurrentJobs){
            List<RemovableDocument> remList = new ArrayList<RemovableDocument>();
            List<IndexableDocument> addList = new ArrayList<IndexableDocument>();
            List<String> checkList = this.concurrentJobs.get(REMOVE_JOBS_KEY);
            if ( checkList == null || checkList.isEmpty() ){
                remList = toRemove;
            } else {
                for ( IndexableDocument doc : toRemove ){
                    if (!checkList.contains(doc.getKeyFieldName()+"_"+doc.getKey())){
                        remList.add((RemovableDocument)doc);
                    }
                }
            }
            List<String> toAddCheckList = this.concurrentJobs.get(ADD_JOBS_KEY);
            if ( toAddCheckList == null ){
                toAddCheckList = new ArrayList<String>();
            }
            if ( (checkList == null || checkList.isEmpty()) && (toAddCheckList.isEmpty()) ){
                addList = toAdd;
            } else {
                for ( IndexableDocument doc : toAdd ){
                    String key = null;
                    if ( doc instanceof JahiaContainerIndexableDocument ) {
                        key = JahiaSearchConstant.OBJECT_KEY + "_" + ((JahiaContainerIndexableDocument)doc).getObjectKey().toString();
                    } else if ( doc instanceof JahiaPageIndexableDocument ){
                        key = JahiaSearchConstant.OBJECT_KEY + "_" + ((JahiaPageIndexableDocument)doc).getObjectKey().toString();
                    } else if ( doc instanceof JahiaFieldIndexableDocument || doc instanceof JahiaReferenceIndexableDocument){
                        key = JahiaSearchConstant.FIELD_FIELDID + "_" + doc.getField(JahiaSearchConstant.FIELD_FIELDID).getValues().get(0).toString();
                    } 
                    if (!checkList.contains(key.toLowerCase()) && !toAddCheckList.contains(key.toLowerCase())){
                        addList.add(doc);
                    }
                }
            }

            SearchHandler sh = ServicesRegistry.getInstance().getJahiaSearchService().getSearchHandler(siteId);
            if (sh != null) {
                sh.batchIndexing(remList, addList);
                this.concurrentJobs.clear();
            }
        }
    }

    protected void copyData(Properties props, JobDataMap data, String dataName,
                            String propName, String defaultValue){
        String value = null;
        Object obj = data.get(dataName);
        if ( obj != null ){
            value = obj.toString();    
        }
        if (value == null){
            value = defaultValue;
        }
        if ( defaultValue == null ){
            props.remove(propName);
        }
        props.setProperty(propName,value);
    }

    public void notifyJob(){
        synchronized(lock){
            try {
                lock.notify();
            } catch ( Exception t){
            }
        }
    }

    public int getSiteID(){
        return siteId;
    }
    
    private boolean saveLastProcessedPage(Integer pageId){
        try {
            JahiaSite site = ServicesRegistry.getInstance()
                    .getJahiaSitesService().getSite(siteId);
            Properties settings = new Properties();
            settings.setProperty(ServicesRegistry.getInstance().getClusterService().getServerId()
                    + "_" + LAST_INDEXED_PAGE, String.valueOf(pageId));
            ServicesRegistry.getInstance().getJahiaSitesService().updateSiteProperties(site, settings);
        } catch ( Exception t ) {
            logger.error("Cannot save last processed page", t);
            return false;
        }
        return true;
    }

    private int getLastProcessedPage(){
        int pageId = -1;
        try {
            JahiaSite site = ServicesRegistry.getInstance()
                    .getJahiaSitesService().getSite(this.siteId);
            Properties settings = site.getSettings();
            pageId = Integer.parseInt(settings.getProperty(ServicesRegistry.getInstance().getClusterService()
                    .getServerId() + "_" + LAST_INDEXED_PAGE,
                    "-1"));                       
        } catch ( Exception t ) {
            logger.error("Cannot get last processed page", t);
        }
        return pageId;
    }

    private boolean checkInterruptStatus(JobDataMap jobDataMap,
                                         TreeOperationResult result, String serverId) {
        String interruptStatus = getInterruptStatus(siteId,serverId);
        if ( INTERRUPT_STATUS_ABORT_REQUESTED.equals(interruptStatus) ){
            //this.saveSiteProperty(BackgroundJob.JOB_STATUS,BackgroundJob.STATUS_ABORTED);
            jobDataMap.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_ABORTED);
            NodeOperationResult nodeOperationResult =
                    new NodeOperationResult(null,Jahia.getThreadParamBean().getLocale().getDisplayName()
                            ,BackgroundJob.STATUS_ABORTED);
            result.appendError(nodeOperationResult);
            jobDataMap.put(BackgroundJob.RESULT,result);
            return false;
        } else if ( INTERRUPT_STATUS_INTERRUPT_REQUESTED.equals(interruptStatus) ){
            Properties newSettings = new Properties();
            newSettings.setProperty(serverId + "_" + BackgroundJob.JOB_STATUS,BackgroundJob.STATUS_INTERRUPTED);
            newSettings.setProperty(serverId + "_" + JahiaSiteIndexingJob.INTERRUPT_STATUS,
                    JahiaSiteIndexingJob.STATUS_INTERRUPTED);
            newSettings.setProperty(serverId + "_" + JahiaSiteIndexingJob.INTERRUPT_STATUS,
                    JahiaSiteIndexingJob.STATUS_INTERRUPTED);
            long startTime = System.currentTimeMillis();
            try {
                startTime = jobDataMap.getLong(BackgroundJob.JOB_BEGIN);
            } catch ( Exception t ){
            }
            newSettings.setProperty(serverId + "_" + JahiaSiteIndexingJob.INTERRUPT_TIME,String.valueOf(startTime));
            try {
                ServicesRegistry.getInstance().getJahiaSitesService().updateSiteProperties(
                        ServicesRegistry.getInstance().getJahiaSitesService().getSite(this.siteId), newSettings);
            } catch (Exception t) {
                logger.debug("Error saving site indexation status", t);
            }
            
            jobDataMap.put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_INTERRUPTED);
            NodeOperationResult nodeOperationResult =
                    new NodeOperationResult(null,Jahia.getThreadParamBean().getLocale().getDisplayName()
                            ,BackgroundJob.STATUS_INTERRUPTED);
            result.appendError(nodeOperationResult);
            jobDataMap.put(BackgroundJob.RESULT,result);
            return false;
        } else if ( BackgroundJob.STATUS_ABORTED.equals(interruptStatus) ){
            return false;
        } else if ( BackgroundJob.STATUS_INTERRUPTED.equals(interruptStatus) ){
            return false;
        }
        return true;
    }

    public static String getInterruptStatus(int siteId, String serverId){
        try {
            return ServicesRegistry.getInstance()
                    .getJahiaSitesService().getSite(siteId).getSettings()
                    .getProperty(serverId + "_" + JahiaSiteIndexingJob.INTERRUPT_STATUS,
                            JahiaSiteIndexingJob.INTERRUPT_STATUS_NOT_SET);
        } catch ( Exception t ) {
            logger.debug("Error retrieving site indexing INTERRUPT_STATUS",t);
        }
        return JahiaSiteIndexingJob.INTERRUPT_STATUS_UNKNOWN;
    }


}
