/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
