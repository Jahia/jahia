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
package org.jahia.ajax.gwt.client.data.process;

import java.io.Serializable;
import java.util.List;

/**
 * User: jahia
 * Date: 28 juil. 2008
 * Time: 16:23:03
 */
public class GWTJahiaProcessJobPreference implements Serializable {
    public static int PREF_GENERAL = 1;
    public static int PREF_FILTER = 2;
    private int dataType;
    private int maxJobs = 100;
    private int jobsPerPage = 50;
    private List<String> jobsTypeToIgnore;
    private List<String> jobsStatusToIgnore;
    private boolean onlyCurrentUser;
    private boolean autoRefresh;
    private boolean refreshAtEndOfAnyPageWorkflow;
    private int pageRefreshNotification;

    public GWTJahiaProcessJobPreference() {
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    public boolean isRefreshAtEndOfAnyPageWorkflow() {
        return refreshAtEndOfAnyPageWorkflow;
    }

    public void setRefreshAtEndOfAnyPageWorkflow(boolean refreshAtEndOfAnyPageWorkflow) {
        this.refreshAtEndOfAnyPageWorkflow = refreshAtEndOfAnyPageWorkflow;
    }

    public int getJobsPerPage() {
        return jobsPerPage;
    }

    public void setJobsPerPage(int jobsPerPage) {
        this.jobsPerPage = jobsPerPage;
    }

    public List<String> getJobsStatusToIgnore() {
        return jobsStatusToIgnore;
    }

    public void setJobsStatusToIgnore(List<String> jobsStatusToIgnore) {
        this.jobsStatusToIgnore = jobsStatusToIgnore;
    }

    public List<String> getJobsTypeToIgnore() {
        return jobsTypeToIgnore;
    }

    public void setJobsTypeToIgnore(List<String> jobsTypeToIgnore) {
        this.jobsTypeToIgnore = jobsTypeToIgnore;
    }

    public int getMaxJobs() {
        return maxJobs;
    }

    public void setMaxJobs(int maxJobs) {
        this.maxJobs = maxJobs;
    }

    public boolean isOnlyCurrentUser() {
        return onlyCurrentUser;
    }

    public void setOnlyCurrentUser(boolean onlyCurrentUser) {
        this.onlyCurrentUser = onlyCurrentUser;
    }

    public int getPageRefreshNotification() {
        return pageRefreshNotification;
    }

    public void setPageRefreshNotification(int pageRefreshNotification) {
        this.pageRefreshNotification = pageRefreshNotification;
    }
}
