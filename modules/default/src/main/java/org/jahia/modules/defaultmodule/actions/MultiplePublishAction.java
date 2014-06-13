/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.modules.defaultmodule.actions;

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
import org.jahia.utils.i18n.Messages;

import javax.jcr.PropertyType;
import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.util.*;

/**
 * @author rincevent
 * @since JAHIA 6.5
 *        Created : 24 nov. 2010
 */
public class MultiplePublishAction extends Action {
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

            String title = Messages.getInternalWithArguments("label.workflow.start.message", session.getLocale(), "{0} started by {1} on {2} - {3} content items involved",
                    entry.getValue().getDisplayName(), session.getUser().getName(), DateFormat.getDateInstance(DateFormat.SHORT, session.getLocale()).format(new Date()), pubInfos.size());

            WorkflowVariable var = new WorkflowVariable(title, PropertyType.STRING);
            map.put("jcr:title", var);
            
            if (entry.getValue() != null) {
                workflowService.startProcessAsJob(entry.getKey().getAllUuids(),
                        session, entry.getValue().getKey(),
                        entry.getValue().getProvider(), map, null);
            }
        }
        return ActionResult.OK_JSON;
    }
}
