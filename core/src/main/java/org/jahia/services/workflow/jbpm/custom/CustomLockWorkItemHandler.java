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
package org.jahia.services.workflow.jbpm.custom;

import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import javax.jcr.RepositoryException;

import java.util.List;

public class CustomLockWorkItemHandler extends AbstractWorkItemHandler implements WorkItemHandler {

    private void doLock(String id, JCRSessionWrapper session, String key, String type)
            throws RepositoryException {
        JCRNodeWrapper node = session.getNodeByUUID(id);
        if (node.isLockable()) {
            node.lockAndStoreToken(type, " " + key + " ");
        }
    }

    @Override
    public void executeWorkItem(final WorkItem workItem, WorkItemManager manager) {
        @SuppressWarnings("unchecked")
        final List<String> uuids = (List<String>) workItem.getParameter("nodeIds");
        String workspace = (String) workItem.getParameter("workspace");
        String userKey = (String) workItem.getParameter("user");
        JCRUserNode user = JahiaUserManagerService.getInstance().lookupUserByPath(userKey);
        JahiaUser jahiaUser = null;
        if (user != null) {
            jahiaUser = user.getJahiaUser();
        }
        final String type = (String) workItem.getParameter("type");

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(jahiaUser,
                    workspace, null, new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            for (String id : uuids) {
                                doLock(id, session, "process-" + workItem.getProcessInstanceId(), type);
                            }
                            return null;
                        }
                    });
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        manager.completeWorkItem(workItem.getId(), null);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        manager.abortWorkItem(workItem.getId());
    }
}
