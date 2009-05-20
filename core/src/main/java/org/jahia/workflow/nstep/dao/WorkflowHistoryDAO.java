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
 package org.jahia.workflow.nstep.dao;

import java.util.List;

import org.jahia.workflow.nstep.model.WorkflowHistoryEntry;


/*
 * Copyright (c) 2004 CODEVA. All Rights Reserved.
 */

public interface WorkflowHistoryDAO extends DAO {
// -------------------------- OTHER METHODS --------------------------

    public WorkflowHistoryEntry getWorkflowHistory(Long historyId);

    public List<WorkflowHistoryEntry> getWorkflowHistoryByAuthor(String author);

    public List<WorkflowHistoryEntry> getWorkflowHistoryByObject(String objectKey);

    public List<WorkflowHistoryEntry> getWorkflowHistoryByProcess(String process);

    public List<WorkflowHistoryEntry> getWorkflowHistoryByUser(String user);

    public void removeWorkflowHistory(Long historyId);

    public void removeWorkflowHistory(String objectKey);

    public void saveWorkflowHistory(WorkflowHistoryEntry history);
}

