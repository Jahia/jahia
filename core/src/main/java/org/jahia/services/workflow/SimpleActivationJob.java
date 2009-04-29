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
 package org.jahia.services.workflow;

import java.util.Set;

import org.jahia.content.ContentObject;
import org.jahia.content.JahiaObject;
import org.jahia.content.ObjectKey;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.version.StateModificationContext;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 6 avr. 2006
 * Time: 16:25:16
 * To change this template use File | Settings | File Templates.
 */
public class SimpleActivationJob extends BackgroundJob {
    private static final WorkflowService service = ServicesRegistry.getInstance().getWorkflowService();

    public static final String KEYS = "keys";
    public static final String LANGS = "langs";

    public void executeJahiaJob(JobExecutionContext jobExecutionContext, ProcessingContext processingContext) throws Exception {
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();

        ContentObject object;
        Set<ObjectKey> keys = (Set<ObjectKey>) data.get(KEYS);

        Set<String> languages = (Set<String>) data.get(LANGS);
        JahiaSaveVersion saveVersion = ServicesRegistry.getInstance ().getJahiaVersionService ().
                getSiteSaveVersion (processingContext.getSiteID ());

        for (ObjectKey key : keys) {
            try {
                object = (ContentObject) JahiaObject.getInstance(key);
                StateModificationContext stateModifContext = new StateModificationContext(object.getObjectKey(), languages);
                stateModifContext.addModifiedObjects(keys);
                service.activate(object, languages, saveVersion, processingContext, stateModifContext);
            } catch (ClassNotFoundException e) {
                throw new JobExecutionException(e);
            }
        }
    }
}
