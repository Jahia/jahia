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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jahia.content.ContentObjectKey;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.version.StateModificationContext;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

/**
 * 
 *
 * User: toto
 * Date: Sep 29, 2008 - 2:40:10 PM
 */
public class ActivationJob extends AbstractActivationJob {

    public void executeJahiaJob(JobExecutionContext jobExecutionContext, ProcessingContext processingContext) throws Exception {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        List<WorkflowAction> actions = (List<WorkflowAction>) jobDataMap.get(ACTIONS);

        final Set<ExternalWorkflow> externalWorkflows = new HashSet<ExternalWorkflow>();

        JahiaSaveVersion saveVersion = ServicesRegistry.getInstance().getJahiaVersionService().
                getSiteSaveVersion(processingContext.getSiteID());

        ActivationTestResults results = new ActivationTestResults();

        List<ContentObjectKey> allkeys = new ArrayList<ContentObjectKey>();

        for (WorkflowAction action : actions) {
            ContentObjectKey key = action.getKey();
            allkeys.add(key);
        }

            for (WorkflowAction action : actions) {
                Set<String> langs = action.getLangs();
                ContentObjectKey key = action.getKey();

                final StateModificationContext stateModifContext = new StateModificationContext(key, langs);
                stateModifContext.addModifiedObjects(allkeys);

                ExternalWorkflow externalWorkflow = processWorkflow(processingContext,  key ,action.getAction(), langs,
                        saveVersion, action.getComment(), results, stateModifContext);
                if (externalWorkflow != null) {
                    externalWorkflows.add(externalWorkflow);
                }

            }

        jobDataMap.put(RESULT, results);

    }
}
