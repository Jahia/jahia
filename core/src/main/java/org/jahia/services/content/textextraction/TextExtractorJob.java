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
package org.jahia.services.content.textextraction;

import org.slf4j.Logger;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.content.rules.ExtractionService;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

/**
 *
 * User: toto
 * Date: 29 janv. 2008
 * Time: 13:58:32
 */
public class TextExtractorJob extends BackgroundJob {
    public static final String JOB_PATH = "path";
    public static final String JOB_PROVIDER = "provider";
    public static final String JOB_WORKSPACE = "workspace";
    public static final String JOB_EXTRACTNODE_PATH = "extractnode-path";

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TextExtractorJob.class);

    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
        String path = (String) data.get(JOB_PATH);
        String providerPath = (String) data.get(JOB_PROVIDER);
        String extractNodePath = (String) data.get(JOB_EXTRACTNODE_PATH);
        String workspace = (String) data.get(JOB_WORKSPACE);
        JCRStoreProvider provider = JCRSessionFactory.getInstance().getProvider(providerPath);

        if (logger.isDebugEnabled()) {
            logger.debug("Start text extraction job for provider '" + provider.getKey() + "' path " + path
                    + " and extractNodePath " + extractNodePath);
        } else {
            logger.info("Start text extraction job for node " + path);
        }

        ExtractionService.getInstance().extractText(provider, path, extractNodePath, workspace);

        logger.info("... finished text extraction job for node " + path);
    }
}
