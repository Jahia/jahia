/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.workflow.jbpm.command;

import org.jahia.services.workflow.WorkflowComment;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* Add a comment to a process
*/
public class AddCommentCommand extends BaseCommand<Object> {

    private final String processId;
    private final String comment;
    private final String user;

    public AddCommentCommand(String processId, String comment, String user) {
        this.processId = processId;
        this.comment = comment;
        this.user = user;
    }

    @Override
    public Object execute() {
        KieSession ksession = getKieSession();
        ProcessInstance processInstance = ksession.getProcessInstance(Long.parseLong(processId));
        WorkflowProcessInstance workflowProcessInstance = (WorkflowProcessInstance) processInstance;
        List<WorkflowComment> comments = (List<WorkflowComment>) workflowProcessInstance.getVariable("comments");
        if (comments == null) {
            comments = new ArrayList<WorkflowComment>();
        }
        final WorkflowComment wfComment = new WorkflowComment();
        wfComment.setComment(comment);
        wfComment.setUser(user);
        wfComment.setTime(new Date());
        comments.add(wfComment);
        workflowProcessInstance.setVariable("comments",comments);
        return null;
    }

    @Override
    public String toString() {
        return super.toString() +
                String.format("%n processId: %s", processId) +
                String.format("%n comment: %s", comment) +
                String.format("%n user: %s", user);
    }
}
