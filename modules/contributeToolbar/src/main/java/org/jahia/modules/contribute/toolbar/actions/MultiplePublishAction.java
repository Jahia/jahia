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
import org.jahia.api.Constants;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.PublicationInfo;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.jbpm.JBPMProvider;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 24 nov. 2010
 */
public class MultiplePublishAction implements Action {
    private transient static Logger logger = Logger.getLogger(MultiplePublishAction.class);
    private String name;
    private WorkflowService workflowService;
    private JCRPublicationService publicationService;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        List<String> uuids = parameters.get("uuids");
        assert uuids != null && uuids.size() > 0;
        JCRSessionWrapper session = resource.getNode().getSession();
        Map<WorkflowDefinition, List<String>> workflowDefinitionListMap = new HashMap<WorkflowDefinition, List<String>>();
        Set<String> locales = new LinkedHashSet<String>(Arrays.asList(
                renderContext.getMainResourceLocale().toString()));
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            for (String uuid : uuids) {
                JCRNodeWrapper node = session.getNodeByUUID(uuid);
                Map<String, WorkflowDefinition> workflowDefinitionMap = workflowService.getPossibleWorkflows(node,
                        renderContext.getUser(), renderContext.getMainResourceLocale());
                WorkflowDefinition action = workflowDefinitionMap.values().iterator().next();
                List<String> workflowUUIDS = workflowDefinitionListMap.get(action);
                if (workflowUUIDS == null) {
                    workflowUUIDS = new LinkedList<String>();
                    workflowDefinitionListMap.put(action, workflowUUIDS);
                }
                workflowUUIDS.add(uuid);
            }
            for (Map.Entry<WorkflowDefinition, List<String>> definitionListEntry : workflowDefinitionListMap.entrySet()) {
                List<PublicationInfo> infoList = publicationService.getPublicationInfos(definitionListEntry.getValue(),
                        locales, true, true, false, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE);
                map.put("publicationInfos", infoList);
                workflowService.startProcess(definitionListEntry.getValue(),
                        renderContext.getMainResource().getNode().getSession(), definitionListEntry.getKey().getKey(),
                        JBPMProvider.getInstance().getKey(), map);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return ActionResult.BAD_REQUEST;
        }
        return ActionResult.OK_JSON;
    }
}
