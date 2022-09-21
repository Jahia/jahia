/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.rules;

import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;

/**
 * Background job that executes a predefined action. 
 */
public class ActionJob extends BackgroundJob {
	
    public static final String NAME_PREFIX = "ACTION_JOB_"; 
    
    private static transient Logger logger = LoggerFactory.getLogger(ActionJob.class);

    public static final String JOB_ACTION_TO_EXECUTE = "actionToExecute";
    public static final String JOB_NODE_UUID = "node";
    public static final String JOB_WORKSPACE = "workspace";
    
    public static final String getJobGroup(String actionName) {
    	return BackgroundJob.getGroupName(ActionJob.class) + "." + actionName;
    }

    public static final String getJobName(String actionName, String nodeIdentifier) {
    	return actionName + "-" + nodeIdentifier;
    }

    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            final JobDataMap map = jobExecutionContext.getJobDetail().getJobDataMap();
            String actionName = map.getString(JOB_ACTION_TO_EXECUTE);
			final BackgroundAction action = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getBackgroundActions().get(
                    actionName);
            if (action != null) {
                BackgroundAction backgroundAction = (BackgroundAction) action;
                final JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
                final JCRSessionWrapper jcrSessionWrapper = sessionFactory.getCurrentUserSession(map.getString(ActionJob.JOB_WORKSPACE),
                        map.getString(ActionJob.JOB_CURRENT_LOCALE) != null ? LanguageCodeConverters.getLocaleFromCode(map.getString(ActionJob.JOB_CURRENT_LOCALE)): null);
                try {
                    JCRNodeWrapper node = jcrSessionWrapper.getNodeByUUID(map.getString(JOB_NODE_UUID));
                    backgroundAction.executeBackgroundAction(node);
                } catch (ItemNotFoundException e) {
                    logger.warn("The node with UUID {} cannot be found in the repository. Skip executing background action.", map.getString(JOB_NODE_UUID));
                    throw new JobExecutionException(e);
                }
            } else {
                throw new JobExecutionException("Background action with the name " + actionName + " is not found in the registry."
                        + " Skip executing action.");
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}