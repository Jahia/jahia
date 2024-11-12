/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.ArrayList;
import java.util.List;

public class RuleJob extends BackgroundJob {

    public static final String JOB_NODE_UUID = "node";
    public static final String JOB_RULE_TO_EXECUTE = "ruleToExecute";
    public static final String JOB_WORKSPACE = "workspace";
    public static final String JOB_USER = "user";
    public static final String JOB_USER_REALM = "userrealm";

    @Override
    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        final JobDataMap map = jobExecutionContext.getJobDetail().getJobDataMap();
        AddedNodeFact wrapper = null;
        List<Object> list = new ArrayList<Object>();
        final JCRSessionWrapper jcrSessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession();
        wrapper = new AddedNodeFact(jcrSessionWrapper.getNodeByUUID(map.getString(JOB_NODE_UUID)));
        list.add(new JobRuleExecution(map.getString(JOB_RULE_TO_EXECUTE), wrapper));
        final RulesListener listener = RulesListener.getInstance(map.getString(JOB_WORKSPACE));
        listener.executeRules(list, listener.getGlobals(map.getString(JOB_USER), map.getString(JOB_USER_REALM), new ArrayList<Updateable>()));
        jcrSessionWrapper.save();
    }
}
