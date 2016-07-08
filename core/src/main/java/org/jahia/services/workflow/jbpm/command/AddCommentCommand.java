/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
}
