/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.workflow.jbpm.custom;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.WorkflowVariable;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Publish custom activity for jBPM workflow
 * <p/>
 * Publish the current node
 */
public class Publish implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    private transient static Logger logger = Logger.getLogger(Publish.class);

    public void execute(ActivityExecution execution) throws Exception {
        List<PublicationInfo> info = (List<PublicationInfo>) execution.getVariable("publicationInfos");
        String workspace = (String) execution.getVariable("workspace");
        String userKey = (String) execution.getVariable("user");
        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        final JahiaUserManagerService userMgr = ServicesRegistry.getInstance().getJahiaUserManagerService();
        JahiaUser user = userMgr.lookupUserByKey(userKey);
        JahiaUser currentUser = sessionFactory.getCurrentUser();
        sessionFactory.setCurrentUser(user);
        String label = "published_at_"+ new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(GregorianCalendar.getInstance().getTime());
        JCRPublicationService.getInstance().unlockForPublication(info, workspace, "process-"+execution.getProcessInstance().getId());
        JCRPublicationService.getInstance().publish(info, workspace, Constants.LIVE_WORKSPACE);
        for (PublicationInfo publicationInfo : info) {
            JCRVersionService.getInstance().addVersionLabel(publicationInfo.getAllUuids(),label,Constants.LIVE_WORKSPACE);
            JCRVersionService.getInstance().addVersionLabel(publicationInfo.getAllUuids(),label,workspace);
        }
        sessionFactory.setCurrentUser(currentUser);
        List<WorkflowVariable> workflowVariables = (List<WorkflowVariable>) execution.getVariable("endDate");
        if (workflowVariables.isEmpty()) {
            execution.take("to end");
        } else {
            execution.take("timeBasedUnpublish");
        }
    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
    }

}
