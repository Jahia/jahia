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
package org.jahia.ajax.gwt.commons.server.rpc;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaProcessJobAction;
import org.jahia.ajax.gwt.client.data.GWTJahiaNodeOperationResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaNodeOperationResultItem;
import org.jahia.ajax.gwt.client.data.*;
import org.jahia.ajax.gwt.engines.workflow.server.helper.WorkflowServiceHelper;
import org.jahia.ajax.gwt.utils.JahiaObjectCreator;
import org.jahia.content.ContentObject;
import org.jahia.content.NodeOperationResult;
import org.jahia.content.ObjectKey;
import org.jahia.content.TreeOperationResult;
import org.jahia.engines.EngineMessage;
import org.jahia.engines.calendar.CalendarHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.ProcessAction;
import org.jahia.services.search.JahiaSiteIndexingJob;
import org.jahia.services.workflow.AbstractActivationJob;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.registries.ServicesRegistry;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 19, 2008
 * Time: 12:43:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessDisplayHelper {
    private static transient final Logger logger = Logger.getLogger(ProcessDisplayHelper.class);

    public static GWTJahiaProcessJob getGWTJahiaProcessJob(JobDetail currentJobDetail, ProcessingContext jParams) {
        GWTJahiaProcessJob gwtProcessJob = new GWTJahiaProcessJob();
        JobDataMap currentJobDataMap = currentJobDetail.getJobDataMap();
        String currentJobStatus = currentJobDataMap.getString(BackgroundJob.JOB_STATUS);
        String currentJobUserKey = currentJobDataMap.getString(BackgroundJob.JOB_USERKEY);

        //jon: local
        String currentJobLocale = currentJobDataMap.getString(BackgroundJob.JOB_CURRENT_LOCALE);
        gwtProcessJob.setJobLocal(currentJobLocale);

        //job: pid;
        String currentJobPid = currentJobDataMap.getString(BackgroundJob.JOB_PID);
        if (currentJobPid != null) {
            int jobPid = Integer.parseInt(currentJobPid);
            gwtProcessJob.setJobPid(currentJobPid);
            String pageUrl = "";
            try {
                pageUrl = jParams.composePageUrl(jobPid, currentJobLocale);
            } catch (JahiaException e) {
                logger.debug("Can't compose page url [pid:" + jobPid + "]", e);
            }
            gwtProcessJob.setAbsolutePageUrl(pageUrl);
        }

        // job: name
        String currentJobName = currentJobDetail.getName();
        gwtProcessJob.setJobName(currentJobName);

        // job: group name
        String currentJobGroupName = currentJobDetail.getGroup();
        gwtProcessJob.setJobGroupName(currentJobGroupName);

        // job: description
        String currentJobDescription = currentJobDetail.getDescription();
        gwtProcessJob.setJobServer(currentJobDescription);

        if (currentJobDataMap.getString(BackgroundJob.JOB_TITLE) != null ) {
            gwtProcessJob.setJobTitle(currentJobDataMap.getString(BackgroundJob.JOB_TITLE));
        }

        // job: extra comment depending on the type
        String currentJobExtraComment = currentJobDataMap.getString(AbstractActivationJob.COMMENTS_INPUT);
        if (currentJobExtraComment != null) {
            // gwtProcessJob.setJobExtraComment(currentJobExtraComment);
        }

        // job: server
        String currentJobServer = currentJobDataMap.getString(BackgroundJob.JOB_SERVER);
        gwtProcessJob.setJobServer(currentJobServer);

        TreeOperationResult treeOperationResult = (TreeOperationResult) currentJobDataMap.get(BackgroundJob.RESULT);

        // job: status
        if (treeOperationResult != null && !"pooled".equals(currentJobStatus)) {
            switch (treeOperationResult.getStatus()) {
                case TreeOperationResult.FAILED_OPERATION_STATUS:
                    currentJobStatus = "failed";
                    break;
                case TreeOperationResult.PARTIAL_OPERATION_STATUS:
                    currentJobStatus = "partial";
            }
        }
        gwtProcessJob.setJobStatus(currentJobStatus);

        // job: begin
        String currentJobBegin = currentJobDataMap.getString(BackgroundJob.JOB_BEGIN);
        logger.debug("job begin at: " + currentJobBegin);
        gwtProcessJob.setJobBeginComparable(currentJobBegin);
        gwtProcessJob.setJobBegin(prettyPrintDate(jParams.getLocale(), currentJobBegin));

        // job: created
        String currentJobCreated = currentJobDataMap.getString(BackgroundJob.JOB_CREATED);
        gwtProcessJob.setJobCreatedComparable(currentJobCreated);
        gwtProcessJob.setJobCreated(prettyPrintDate(jParams.getLocale(), currentJobCreated));

        // job: duration
        String currentJobDuration = currentJobDataMap.getString(BackgroundJob.JOB_DURATION);
        gwtProcessJob.setJobDuration(prettyPrintDuration(currentJobDuration));

        //job: end
        String currentJobEnd = currentJobDataMap.getString(BackgroundJob.JOB_END);
        gwtProcessJob.setJobEndComparable(currentJobEnd);
        gwtProcessJob.setJobEnd(prettyPrintDate(jParams.getLocale(), currentJobEnd));

        //job: site key
        String currentJobSiteKey = currentJobDataMap.getString(BackgroundJob.JOB_SITEKEY);
        gwtProcessJob.setJobSiteKey(currentJobSiteKey);

        // job: scheduled
        String currentJobScheduled = currentJobDataMap.getString(BackgroundJob.JOB_SCHEDULED);
        gwtProcessJob.setJobScheduled(currentJobScheduled);

        // job: userkey
        if (currentJobUserKey != null) {
            JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(currentJobUserKey);
            gwtProcessJob.setJobUserName(user.getUsername());
        }

        //job: type
        String currentJobType = currentJobDataMap.getString(BackgroundJob.JOB_TYPE);
        gwtProcessJob.setJobType(currentJobType);

        Map<String, List<GWTJahiaProcessJobAction>> batchContent = new HashMap<String, List<GWTJahiaProcessJobAction>>();
        Map<String, String> displayTitles = new HashMap<String, String>();
        List<ProcessAction> actions = (List<ProcessAction>) currentJobDataMap.get(BackgroundJob.ACTIONS);
        if (actions != null) {
            for (ProcessAction action : actions) {
                ObjectKey objectKey = action.getKey();
                Map<String, String> wfStates = null ;
                try {
                    ContentObject obj = JahiaObjectCreator.getContentObjectFromKey(objectKey) ;
                    if (WorkflowService.getInstance().getWorkflowMode(obj) != WorkflowService.LINKED) {
                        wfStates = WorkflowServiceHelper.getWorkflowStates(JahiaObjectCreator.getContentObjectFromKey(objectKey)) ;
                    }
                } catch (ClassNotFoundException e) {
                    logger.error(e.toString(), e) ;
                } catch (JahiaException e) {
                    logger.error(e.toString(), e);
                }
                GWTJahiaProcessJobAction gwtAction = new GWTJahiaProcessJobAction(action.getKey().toString(), new HashSet<String>(action.getLangs()), action.getAction(), wfStates);
                if (!batchContent.containsKey(action.getAction())) {
                    batchContent.put(action.getAction(), new ArrayList<GWTJahiaProcessJobAction>());
                }
                batchContent.get(action.getAction()).add(gwtAction);

//                if (!batchContent.get(action.getAction()).containsKey(objectKey.getKey())) {
//                    batchContent.get(action.getAction()).put(objectKey.getKey(), new HashSet<String>());
//                }
//                for (String lang : action.getLangs()) {
//                    batchContent.get(action.getAction()).get(objectKey.getKey()).add(lang);
//                }
                if (!displayTitles.containsKey(objectKey.getKey())) {
                    String title = null;
                    try {
                        title = ContentObject.getInstance(objectKey).getDisplayName(jParams);
                    } catch (Exception e) {}
                    if (title == null || title.length() == 0) {
                        title = objectKey.getKey();
                    }
                    displayTitles.put(objectKey.getKey(), title);
                }
            }
        }
        gwtProcessJob.setActions(batchContent);
        gwtProcessJob.setTitleForObjectKey(displayTitles);

        //job: interrupt status
        String currentJobInterruptStatus = currentJobDataMap.getString(JahiaSiteIndexingJob.INTERRUPT_STATUS);
        gwtProcessJob.setJobInterruptStatus(currentJobInterruptStatus);

        //job: log
        TreeOperationResult currentJobLogTOR = treeOperationResult;


        Map<String, Map<String, GWTJahiaNodeOperationResult>> logs = new HashMap<String, Map<String, GWTJahiaNodeOperationResult>>();
        if (currentJobLogTOR != null) {
            processLogs(currentJobLogTOR.getWarnings(), GWTJahiaNodeOperationResultItem.WARNING, logs, jParams);
            processLogs(currentJobLogTOR.getErrors(), GWTJahiaNodeOperationResultItem.ERROR, logs, jParams);
            gwtProcessJob.setStatus(currentJobLogTOR.getStatus());
            gwtProcessJob.setLogSize(currentJobLogTOR.getWarnings().size() + currentJobLogTOR.getErrors().size());
        }


        gwtProcessJob.setLogs(logs);

        logger.debug("Current job: " + gwtProcessJob.getJobType() + "," + gwtProcessJob.getJobBegin() + "," + gwtProcessJob.getJobEnd() + "," + gwtProcessJob.getJobStatus() + "," + gwtProcessJob.getJobUserName() + "," + gwtProcessJob.getJobInterruptStatus());
        logger.debug("Current job extrat comment: " + currentJobExtraComment);

        return gwtProcessJob;
    }

    private static void processLogs(List errList, int type, Map<String, Map<String, GWTJahiaNodeOperationResult>> logs, ProcessingContext jParams) {
        if (errList != null) {
            for (Object o : errList) {
                try {
                    NodeOperationResult nodeOperationResult = (NodeOperationResult) o;
//                    if (nodeOperationResult.getNodeKey() != null) {
                        String k = nodeOperationResult.getNodeKey() != null ? nodeOperationResult.getNodeKey().toString() : null;
                        if (!logs.containsKey(k)) {
                            logs.put(k, new HashMap<String, GWTJahiaNodeOperationResult>());
                        }
                        Map<String, GWTJahiaNodeOperationResult> logsForObject = logs.get(k);

                        String lang = nodeOperationResult.getLanguageCode();
                        if (!logsForObject.containsKey(lang)) {
                            logsForObject.put(lang, new GWTJahiaNodeOperationResult());
                        }
                        GWTJahiaNodeOperationResult logsForObjectAndLang = logsForObject.get(lang);
                        EngineMessage currentEngineMessage = nodeOperationResult.getMsg();
                        String localeKeyValue = currentEngineMessage.isResource() ? getLocaleJahiaEnginesResource(jParams.getLocale(), currentEngineMessage.getKey()) : currentEngineMessage.getKey();
                        String message = null;
                        if (localeKeyValue != null) {
                            if (currentEngineMessage.getValues() != null) {
                                MessageFormat msgFormat = new MessageFormat(localeKeyValue);
                                msgFormat.setLocale(jParams.getLocale());
                                message = msgFormat.format(currentEngineMessage.getValues());
                            } else {
                                message = localeKeyValue;
                            }
                        }
                        if (message != null) {
                            logsForObjectAndLang.addErrorOrWarning(new GWTJahiaNodeOperationResultItem(type, message));
                        }
//                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public static String getLocaleJahiaEnginesResource(Locale l, String label) {
        try {
            return ResourceBundle.getBundle("JahiaInternalResources", l).getString(label);
        } catch (Exception e) {
            try {
                return ResourceBundle.getBundle("JahiaMessageResources", l).getString(label);
            } catch (Exception e1) {
                return "";
            }
        }
    }

    public static String prettyPrintDate(Locale locale, String date) {
        if (date != null) {
            return prettyPrintDate(locale, Long.parseLong(date));
        } else {
            return "";
        }
    }

    public static String prettyPrintDate(Locale locale, long dateL) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(CalendarHandler.DEFAULT_DATE_FORMAT, locale);
        return dateFormat.format(new Date(dateL));
    }

    public static String prettyPrintDuration(String currentJobDuration) {
        try {
            if (currentJobDuration == null) {
                return "";
            }
            int currentJobDurationInt = Integer.parseInt(currentJobDuration);
            if (currentJobDurationInt < 60) {
                return currentJobDuration + "s";
            } else if (currentJobDurationInt == 60) {
                return "1m";
            } else if (currentJobDurationInt < 3600) {
                return String.format("%02dm%02ds", (currentJobDurationInt % 3600) / 60, (currentJobDurationInt % 60));
            }
            return String.format("%d:%02d:%02d", currentJobDurationInt / 3600, (currentJobDurationInt % 3600) / 60, (currentJobDurationInt % 60));
        } catch (NumberFormatException e) {
            logger.error(currentJobDuration + " is not a number.");
            return currentJobDuration + "s";
        }

    }

}
