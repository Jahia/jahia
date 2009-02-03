/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

 package org.jahia.services.search.indexingscheduler;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.search.JahiaSearchConfigConstant;
import org.jahia.utils.JahiaTools;
import org.quartz.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 17 oct. 2005
 * Time: 15:45:19
 * To change this template use File | Settings | File Templates.
 */
public class IndexingJobConsummer implements StatefulJob {

    public static final String JOB_NAME = "IndexingJobConsummer";
    public static final String JOB_GROUP_NAME = "IndexingJobConsummer";
    public static final String TRIGGER_NAME = "IndexingJobConsummerTrigger";
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(IndexingJobConsummer.class);

    public IndexingJobConsummer() {
    }

    public synchronized void execute(JobExecutionContext context)
            throws JobExecutionException {
        try {
            Date now = new Date();
            try {
                long indexingJobMaxLifeTime = JahiaTools.getTimeAsLong(
                        ServicesRegistry.getInstance()
                        .getJahiaSearchService().getIndexationConfig()
                        .getProperty(JahiaSearchConfigConstant.SEARCH_INDEXING_JOB_MAX_LIFE_TIME,"1d"),"1d").longValue();

                ServicesRegistry.getInstance()
                        .getJahiaSearchService().getIndJobMgr()
                        .deleteIndexingJobsBefore(now.getTime()-indexingJobMaxLifeTime,true);
            } catch ( Exception t ){
                logger.debug("Exception deleting old indexing job",t);
            }
        } catch ( Exception t ){
            logger.debug("Exception occured deleting expired jobs", t);
        }
    }
}
