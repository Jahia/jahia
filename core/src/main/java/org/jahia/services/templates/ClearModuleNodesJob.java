/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.templates;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Background task for performing cleanup of the module nodes in JCR.
 *
 * @author Sergiy Shyrkov
 */
public class ClearModuleNodesJob extends BackgroundJob {

    private static final Logger logger = LoggerFactory.getLogger(ClearModuleNodesJob.class);

    private static final String MODULE_ID = "moduleId";
    private static final String MODULE_VERSION = "moduleVersion";

    /**
     * Create an instance of {@link JobDetail} for this job with the specified data.
     *
     * @param moduleId the id of the module to clean nodes for
     * @param moduleVersion the version of the module
     * @return the job details instance
     */
    public static JobDetail createJob(String moduleId, String moduleVersion) {
        JobDetail jobDetail = createJahiaJob("Cleanup module nodes for module " + moduleId + " v" + moduleVersion,
                ClearModuleNodesJob.class);
        jobDetail.setGroup("Maintenance");
        jobDetail.getJobDataMap().put(MODULE_ID, moduleId);
        jobDetail.getJobDataMap().put(MODULE_VERSION, moduleVersion);
        return jobDetail;
    }

    @Override
    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {

        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        String id = dataMap.getString(MODULE_ID);
        String version = dataMap.getString(MODULE_VERSION);

        logger.info("Executing cleanup of nodes for the module {} v{}", id, version);

        ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageDeployer()
                .clearModuleNodes(id, new ModuleVersion(version));

        logger.info("...done cleanup of nodes for the module {} v{}", id, version);
    }
}
