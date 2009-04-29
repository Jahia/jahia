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
 package org.jahia.workflow.nstep.service;

import java.util.List;

import org.jahia.workflow.nstep.model.WorkflowHistoryEntry;

/*
 * Copyright (c) 2004 CODEVA. All Rights Reserved.
 */

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 16 déc. 2004
 * Time: 18:09:24
 * To change this template use File | Settings | File Templates.
 */
public interface WorkflowHistoryManager {
// -------------------------- OTHER METHODS --------------------------

    public List<WorkflowHistoryEntry> getWorkflowHistoryByAuthor(String author);

    public List<WorkflowHistoryEntry> getWorkflowHistoryByObject(String objectKey);

    public List<WorkflowHistoryEntry> getWorkflowHistoryByProcess(String process);
    public List<WorkflowHistoryEntry> getWorkflowHistoryByUser(String user);

    public void saveWorkflowHistory(String author, String user, String process, String action, String objectKey,
                                    String languageCode, String comment);
}

