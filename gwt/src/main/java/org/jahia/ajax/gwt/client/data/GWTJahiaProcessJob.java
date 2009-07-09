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
package org.jahia.ajax.gwt.client.data;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.util.Map;
import java.util.List;
import java.io.Serializable;

/**
 *
 *
 * User: jahia
 * Date: 6 d�c. 2007 - 12:37:38
 */
public class GWTJahiaProcessJob extends BaseModelData implements Serializable {
    // field
    public final static String CREATED = "0";
    public final static String  TYPE = "1";
    public final static String  OWNER = "2";
    public final static String  START = "3";
    public final static String  END = "4";
    public final static String  DURATION = "5";
    public final static String  STATUS = "6";

    public static final int COMPLETED = 1;
    public static final int PARTIAL = 2;
    public static final int ERROR = 0;

    public static final String JOB_SITE_KEY = "jobSiteKey";
    public static final String JOB_SCHEDULED = "jobScheduled";
    public static final String JOB_INTERRUPT_STATUS = "jobInterruptStatus";
    public static final String JOB_NAME = "jobName";
    public static final String JOB_TITLE = "jobTitle";
    public static final String JOB_GROUP_NAME = "jobGroupName";
    public static final String JOB_DESCRIPTION = "jobDescription";
    public static final String JOB_SERVER = "jobServer";
    public static final String JOB_PID = "jobPid";
    public static final String ABSOLUTE_PAGE_URL = "absolutePageUrl";
    public static final String JOB_LOCAL = "jobLocal";

    // workflow batch in case of workflow job
    private Map<String, List<GWTJahiaProcessJobAction>> actions = null ;
    private Map<String, Map<String, GWTJahiaNodeOperationResult>> logs = null;
    private int logSize;

    private Map<String, String> titleForObjectKey = null ;

    private int status;

    public GWTJahiaProcessJob() {

    }

    public String getJobPid() {
        return get(JOB_PID);
    }

    public void setJobPid(String jobPid) {
        set(JOB_PID, jobPid);
    }

    public String getAbsolutePageUrl() {
        return get(ABSOLUTE_PAGE_URL);
    }

    public void setAbsolutePageUrl(String absolutePageUrl) {
        set(ABSOLUTE_PAGE_URL, absolutePageUrl);
    }

    public String getJobLocal() {
        return get(JOB_LOCAL);
    }

    public void setJobLocal(String jobLocal) {
        set(JOB_LOCAL, jobLocal);
    }

    public String getJobName() {
        return get(JOB_NAME);
    }

    public void setJobName(String jobName) {
        set(JOB_NAME, jobName);
    }

    public String getJobTitle() {
        return get(JOB_TITLE);
    }

    public void setJobTitle(String jobName) {
        set(JOB_TITLE, jobName);
    }

    public String getJobGroupName() {
        return get(JOB_GROUP_NAME);
    }

    public void setJobGroupName(String jobGroupName) {
        set(JOB_GROUP_NAME, jobGroupName);
    }

    public String getJobDescription() {
        return get(JOB_DESCRIPTION);
    }

    public void setJobDescription(String jobDescription) {
        set(JOB_DESCRIPTION, jobDescription);
    }

    public String getJobServer() {
        return get(JOB_SERVER);
    }

    public void setJobServer(String jobServer) {
        set(JOB_SERVER, jobServer);
    }

    public String getJobStatus() {
        return get(STATUS);
    }

    public void setJobStatus(String jobStatus) {
        set(STATUS, jobStatus);
    }

    public String getJobBegin() {
        return get(START);
    }

    public void setJobBegin(String jobBegin) {
        set(START, jobBegin);
    }

    public String getJobCreated() {
        return get(CREATED);
    }

    public void setJobCreated(String jobCreated) {
        set(CREATED, jobCreated);
    }

    public String getJobDuration() {
        return get(DURATION);
    }

    public void setJobDuration(String jobDuration) {
        set(DURATION, jobDuration);
    }

    public String getJobEnd() {
        return get(END);
    }

    public void setJobEnd(String jobEnd) {
        set(END, jobEnd);
    }

    public String getJobSiteKey() {
        return get(JOB_SITE_KEY);
    }

    public void setJobSiteKey(String jobSiteKey) {
        set(JOB_SITE_KEY, jobSiteKey);
    }

    public String getJobScheduled() {
        return get(JOB_SCHEDULED);
    }

    public void setJobScheduled(String jobScheduled) {
        set(JOB_SCHEDULED, jobScheduled);
    }

    public String getJobUserName() {
        return get(OWNER);
    }

    public void setJobUserName(String jobUserKey) {
        set(OWNER, jobUserKey);
    }

    public String getJobType() {
        return get(TYPE);
    }

    public void setJobType(String jobType) {
        set(TYPE, jobType);
    }

    public String getJobInterruptStatus() {
        return get(JOB_INTERRUPT_STATUS);
    }

    public void setJobInterruptStatus(String jobInterruptStatus) {
        set(JOB_INTERRUPT_STATUS, jobInterruptStatus);
    }

//    public List<GWTJahiaProcessJobLog> getGwtProcessJobLogExtraInfoList() {
//        return get("gwtProcessJobLogExtraInfoList");
//    }
//
//    public void setGwtProcessJobLogExtraInfoList(List<GWTJahiaProcessJobLog> gwtProcessJobLogExtraInfoList) {
//        this.gwtProcessJobLogExtraInfoList = gwtProcessJobLogExtraInfoList;
//    }
//
//    public List<GWTJahiaProcessJobLog> getGwtProcessJobLogWarningList() {
//        return gwtProcessJobLogWarningList;
//    }
//
//    public void setGwtProcessJobLogWarningList(List<GWTJahiaProcessJobLog> gwtProcessJobLogWarningList) {
//        this.gwtProcessJobLogWarningList = gwtProcessJobLogWarningList;
//    }
//
//    public List<GWTJahiaProcessJobLog> getGwtProcessJobLogErrorsList() {
//        return gwtProcessJobLogErrorsList;
//    }
//
//    public void setGwtProcessJobLogErrorsList(List<GWTJahiaProcessJobLog> gwtProcessJobLogErrorsList) {
//        this.gwtProcessJobLogErrorsList = gwtProcessJobLogErrorsList;
//    }

    public Map<String, Map<String, GWTJahiaNodeOperationResult>> getLogs() {
        return logs;
    }

    public void setLogs(Map<String, Map<String, GWTJahiaNodeOperationResult>> logs) {
        this.logs = logs;
    }

    public int getLogSize() {
        return logSize;
    }

    public void setLogSize(int logSize) {
        this.logSize = logSize;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isJobStatusWaiting() {
        return getJobStatus() != null && getJobStatus().equalsIgnoreCase("waiting");
    }

    public boolean isJobStatusFailed() {
        return getJobStatus() != null && getJobStatus().equalsIgnoreCase("failed");
    }

    public boolean isJobStatusRunning() {
        return getJobStatus() != null && getJobStatus().equalsIgnoreCase("executing");
    }

    public String getJobBeginComparable() {
        return get("jobBeginComparable");
    }

    public void setJobBeginComparable(String jobBeginComparable) {
        set("jobBeginComparable", jobBeginComparable);
    }

    public String getJobCreatedComparable() {
        return get("jobCreatedComparable");
    }

    public void setJobCreatedComparable(String jobCreatedComparable) {
        set("jobCreatedComparable", jobCreatedComparable);
    }

    public String getJobEndComparable() {
        return get("jobEndComparable");
    }

    public void setJobEndComparable(String jobEndComparable) {
        set("jobEndComparable", jobEndComparable);
    }

    public Boolean isLeaf() {
        return Boolean.TRUE;
    }

    public Map<String, List<GWTJahiaProcessJobAction>> getActions() {
        return actions;
    }

    public void setActions(Map<String, List<GWTJahiaProcessJobAction>> actions) {
        this.actions = actions;
    }

    public Map<String, String> getTitleForObjectKey() {
        return titleForObjectKey;
    }

    public void setTitleForObjectKey(Map<String, String> titleForObjectKey) {
        this.titleForObjectKey = titleForObjectKey;
    }
}
