/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.history;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unused node version history purge operation as a background job.
 * 
 * @author Sergiy Shyrkov
 */
public class UnusedVersionHistoryJob extends BackgroundJob {
    private static Logger logger = LoggerFactory.getLogger(UnusedVersionHistoryJob.class);

    @Override
    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();

        long maxUnused = Long.parseLong(StringUtils.defaultString((String) data.get("maxUnused"), "0"));

        long purgeOlderThanTimestamp = getPurgeOlderThanTimestamp(data);

        long timer = System.currentTimeMillis();

        VersionHistoryCheckStatus status = NodeVersionHistoryHelper.checkUnused(maxUnused, true,
                purgeOlderThanTimestamp, null);

        logger.info("Purged unused version histories in {} ms. Status: {}",
                new String[] { String.valueOf(System.currentTimeMillis() - timer), status.toString() });
    }

    private long getPurgeOlderThanTimestamp(JobDataMap data) {
        long purgeOlderThanTimestamp = 0;

        String ageValue = (String) data.get("ageInDays");
        if (ageValue != null) {
            long age = Long.parseLong(ageValue);
            purgeOlderThanTimestamp = age > 0 ? (System.currentTimeMillis() - age * 24L * 60L * 60L * 1000L) : 0;
        } else {
            ageValue = (String) data.get("age");
            if (ageValue != null) {
                long age = Long.parseLong(ageValue);
                purgeOlderThanTimestamp = age > 0 ? (System.currentTimeMillis() - age) : 0;
            } else {
                ageValue = (String) data.get("ageTimestamp");
                if (ageValue != null) {
                    long age = Long.parseLong(ageValue);
                    purgeOlderThanTimestamp = age > 0 ? age : 0;
                }
            }
        }

        return purgeOlderThanTimestamp;
    }
}
