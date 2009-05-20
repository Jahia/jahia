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
 package org.jahia.services.search.valves;

import org.quartz.*;
import org.jahia.registries.ServicesRegistry;
import org.jahia.params.SerializableParamBean;
import org.jahia.services.search.indexingscheduler.RuleEvaluationContext;
import org.jahia.services.containers.ContentContainer;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 26 avr. 2005
 * Time: 16:08:05
 * To change this template use File | Settings | File Templates.
 */
public class ContainerIndexationJob implements StatefulJob {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ContainerIndexationJob.class);


    public ContainerIndexationJob () {}

    public void execute (JobExecutionContext context)
        throws JobExecutionException {

        JobDetail jobDetail = context.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        Integer ctnId = (Integer)jobDataMap.get("ctnId");
        SerializableParamBean jParams = (SerializableParamBean)
                jobDataMap.get("jParams");
        if ( ctnId != null && jParams != null ){
            try {
                ContentContainer contentContainer = ContentContainer.getContainer(ctnId.intValue());
                RuleEvaluationContext ctx = new RuleEvaluationContext(contentContainer.getObjectKey(),
                        contentContainer,jParams,jParams.getUser());
                ServicesRegistry.getInstance().getJahiaSearchService()
                    .indexContainer(ctnId.intValue(), jParams.getUser(), ctx);
            } catch ( Exception t ){
                logger.debug("Exception indexing container " + ctnId.intValue(), t);
            }
        }
    }

}
