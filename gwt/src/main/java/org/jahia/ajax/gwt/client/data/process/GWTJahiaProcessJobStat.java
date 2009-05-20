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
package org.jahia.ajax.gwt.client.data.process;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 10 janv. 2008
 * Time: 09:56:59
 */
public class GWTJahiaProcessJobStat implements Serializable {
    public static final int FULL_MODE = 0;
    public static final int TIMER_MODE = 1;
    private int numberJobs;
    private int numberWaitingJobs;
    private int nextJobCurrentUserIndex;
    private String nextJobCurrentUserType;
    private String lastJobCompletedTime;
    private boolean jobExecuting;
    private boolean needRefresh;

    public GWTJahiaProcessJobStat() {

    }

    public int getNumberJobs() {
        return numberJobs;
    }

    public void setNumberJobs(int numberJobs) {
        this.numberJobs = numberJobs;
    }

    public String getLastJobCompletedTime() {
        return lastJobCompletedTime;
    }

    public void setLastJobCompletedTime(String lastCheckTime) {
        this.lastJobCompletedTime = lastCheckTime;
    }

    public int getNumberWaitingJobs() {
        return numberWaitingJobs;
    }

    public void setNumberWaitingJobs(int numberWaitingJobs) {
        this.numberWaitingJobs = numberWaitingJobs;
    }

    public int getNextJobCurrentUserIndex() {
        return nextJobCurrentUserIndex;
    }

    public void setNextJobCurrentUserIndex(int nextJobCurrentUserIndex) {
        this.nextJobCurrentUserIndex = nextJobCurrentUserIndex;
    }

    public String getNextJobCurrentUserType() {
        return nextJobCurrentUserType;
    }

    public void setNextJobCurrentUserType(String nextJobCurrentUserType) {
        this.nextJobCurrentUserType = nextJobCurrentUserType;
    }

    public boolean isJobExecuting() {
        return jobExecuting;
    }

    public void setJobExecuting(boolean jobExecuting) {
        this.jobExecuting = jobExecuting;
    }

    public boolean isNeedRefresh() {
        return needRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {
        this.needRefresh = needRefresh;
    }
}
