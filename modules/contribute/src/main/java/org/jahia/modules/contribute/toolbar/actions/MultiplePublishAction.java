/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.modules.contribute.toolbar.actions;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.widget.publication.PublicationWorkflow;
import org.jahia.ajax.gwt.helper.PublicationHelper;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.WorkflowVariable;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.jcr.PropertyType;
import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.*;

/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
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
                                  JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        List<String> uuids = parameters.get(MultipleCopyAction.UUIDS);

        Set<String> locales = new LinkedHashSet<String>(Arrays.asList(
                renderContext.getMainResourceLocale().toString()));

        List<GWTJahiaPublicationInfo> pubInfos = publicationHelper.getFullPublicationInfos(uuids, locales, session, false,
                false);

        if (pubInfos.size() == 0) {
            return ActionResult.BAD_REQUEST;
        }

        Map<PublicationWorkflow, WorkflowDefinition> workflows = publicationHelper.createPublicationWorkflows(pubInfos);

        for (Map.Entry<PublicationWorkflow, WorkflowDefinition> entry : workflows.entrySet()) {
            final HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("customWorkflowInfo", entry.getKey());

            String title = MessageFormat.format(JahiaResourceBundle.getJahiaInternalResource("label.workflow.start.message", session.getLocale(), "{0} started by {1} on {2} - {3} content items involved"),
                    entry.getValue().getDisplayName(), session.getUser().getName(), DateFormat.getDateInstance(DateFormat.SHORT, session.getLocale()).format(new Date()), pubInfos.size());

            WorkflowVariable var = new WorkflowVariable(title, PropertyType.STRING);
            map.put("jcr:title",Arrays.asList(var));
            
            if (entry.getValue() != null) {
                workflowService.startProcessAsJob(entry.getKey().getAllUuids(),
                        session, entry.getValue().getKey(),
                        entry.getValue().getProvider(), map, null);
            }
        }
        return ActionResult.OK_JSON;
    }
}
