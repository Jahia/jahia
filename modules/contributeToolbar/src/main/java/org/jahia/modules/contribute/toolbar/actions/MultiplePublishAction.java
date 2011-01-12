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
package org.jahia.modules.contribute.toolbar.actions;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.widget.publication.PublicationWorkflow;
import org.jahia.ajax.gwt.helper.PublicationHelper;
import org.jahia.bin.ActionResult;
import org.jahia.bin.Action;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 24 nov. 2010
 */
public class MultiplePublishAction extends Action {
    private transient static Logger logger = Logger.getLogger(MultiplePublishAction.class);
    private WorkflowService workflowService;
    private PublicationHelper publicationHelper;

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setPublicationHelper(PublicationHelper publicationHelper) {
        this.publicationHelper = publicationHelper;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        List<String> uuids = parameters.get("uuids");

        Set<String> locales = new LinkedHashSet<String>(Arrays.asList(
                renderContext.getMainResourceLocale().toString()));

        JCRSessionWrapper session = resource.getNode().getSession();

        List<GWTJahiaPublicationInfo> pubInfos = publicationHelper.getFullPublicationInfos(uuids, locales, session, false);
        Map<PublicationWorkflow, WorkflowDefinition> workflows = publicationHelper.createPublicationWorkflows(pubInfos);

        for (Map.Entry<PublicationWorkflow, WorkflowDefinition> entry : workflows.entrySet()) {
            final HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("customWorkflowInfo", entry.getKey());
            if (entry.getValue() != null) {
                workflowService.startProcess(entry.getKey().getAllUuids(),
                        session, entry.getValue().getKey(),
                        entry.getValue().getProvider(), map);
            }
        }
        return ActionResult.OK_JSON;
    }
}
