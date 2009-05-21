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
