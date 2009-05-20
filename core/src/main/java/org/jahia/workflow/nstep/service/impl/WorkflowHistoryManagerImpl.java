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
 package org.jahia.workflow.nstep.service.impl;

import java.util.Calendar;
import java.util.List;

import org.jahia.workflow.nstep.dao.WorkflowHistoryDAO;
import org.jahia.workflow.nstep.model.WorkflowHistoryEntry;
import org.jahia.workflow.nstep.service.WorkflowHistoryManager;

/*
 * Copyright (c) 2004 CODEVA. All Rights Reserved.
 */

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 16 déc. 2004
 * Time: 18:14:08
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowHistoryManagerImpl implements WorkflowHistoryManager {
// ------------------------------ FIELDS ------------------------------

    private WorkflowHistoryDAO dao = null;

// --------------------- GETTER / SETTER METHODS ---------------------

    public void setNstepWorkflowHistoryDAO(WorkflowHistoryDAO dao) {
        this.dao = dao;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface WorkflowHistoryManager ---------------------

    public List<WorkflowHistoryEntry> getWorkflowHistoryByUser(String user) {
        return dao.getWorkflowHistoryByUser(user);  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<WorkflowHistoryEntry> getWorkflowHistoryByAuthor(String author) {
        return dao.getWorkflowHistoryByAuthor(author);  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<WorkflowHistoryEntry> getWorkflowHistoryByProcess(String process) {
        return dao.getWorkflowHistoryByProcess(process);  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<WorkflowHistoryEntry> getWorkflowHistoryByObject(String objectKey) {
        return dao.getWorkflowHistoryByObject(objectKey);  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void saveWorkflowHistory(String author, String user, String process, String action, String objectKey,
                                    String languageCode, String comment) {
        WorkflowHistoryEntry entry = new WorkflowHistoryEntry();
        entry.setAuthor(author);
        entry.setUser(user);
        entry.setAction(action);
        entry.setComment(comment);
        entry.setProcess(process);
        entry.setLanguageCode(languageCode);
        entry.setObjectKey(objectKey);
        entry.setDate(Calendar.getInstance().getTime());
        dao.saveWorkflowHistory(entry);
    }
}

