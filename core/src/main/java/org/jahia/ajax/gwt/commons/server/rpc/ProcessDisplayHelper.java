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
package org.jahia.ajax.gwt.commons.server.rpc;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.*;
import org.jahia.content.ContentObject;
import org.jahia.content.NodeOperationResult;
import org.jahia.content.ObjectKey;
import org.jahia.content.TreeOperationResult;
import org.jahia.engines.EngineMessage;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.ProcessAction;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.DateUtils;
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
//                try {
//                    ContentObject obj = JahiaObjectCreator.getContentObjectFromKey(objectKey) ;
//                    if (WorkflowService.getInstance().getWorkflowMode(obj) != WorkflowService.LINKED) {
//
//                        wfStates = WorkflowServiceHelper.getWorkflowStates(JahiaObjectCreator.getContentObjectFromKey(objectKey)) ;
//                    }
//                } catch (ClassNotFoundException e) {
//                    logger.error(e.toString(), e) ;
//                } catch (JahiaException e) {
//                    logger.error(e.toString(), e);
//                }
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
        SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtils.DEFAULT_DATETIME_FORMAT, locale);
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
