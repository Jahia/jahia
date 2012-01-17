/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
    public static final String JOB_EXTRACTNODE_PATH = "extractnode-path";

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TextExtractorJob.class);

    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
        String path = (String) data.get(JOB_PATH);
        String providerPath = (String) data.get(JOB_PROVIDER);
        String extractNodePath = (String) data.get(JOB_EXTRACTNODE_PATH);
        JCRStoreProvider provider = JCRSessionFactory.getInstance().getProvider(providerPath);

        if (logger.isDebugEnabled()) {
            logger.debug("Start text extraction job for provider '" + provider.getKey() + "' path " + path
                    + " and extractNodePath " + extractNodePath);
        } else {
            logger.info("Start text extraction job for node " + path);
        }

        ExtractionService.getInstance().extractText(provider, path, extractNodePath);

        logger.info("... finished text extraction job for node " + path);
    }
}
