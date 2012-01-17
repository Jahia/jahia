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
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 24 nov. 2010
 */
public class MultiplePasteAction extends Action {
    private transient static Logger logger = Logger.getLogger(MultiplePasteAction.class);

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        List<String> uuids = (List<String>) req.getSession().getAttribute(MultipleCopyAction.UUIDS_TO_COPY);
        if (uuids != null && uuids.size() > 0) {
            JCRNodeWrapper targetNode = resource.getNode();
            try {
                for (String uuid : uuids) {
                    JCRNodeWrapper node = session.getNodeByUUID(uuid);
                    session.checkout(node);
                    node.copy(targetNode, JCRContentUtils.findAvailableNodeName(targetNode, node.getName()), true);
                }
                session.save();
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                return ActionResult.BAD_REQUEST;
            }
        }
        req.getSession().removeAttribute(MultipleCopyAction.UUIDS_TO_COPY);
        uuids = (List<String>) req.getSession().getAttribute(MultipleCutAction.UUIDS_TO_CUT);
        if (uuids != null && uuids.size() > 0) {
            JCRNodeWrapper targetNode = resource.getNode();
            try {
                for (String uuid : uuids) {
                    JCRNodeWrapper node = session.getNodeByUUID(uuid);
                    session.checkout(node);
                    session.move(node.getPath(),targetNode.getPath()+"/"+JCRContentUtils.findAvailableNodeName(targetNode, node.getName()));
                }
                session.save();
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                return ActionResult.BAD_REQUEST;
            }
        }
        req.getSession().removeAttribute(MultipleCutAction.UUIDS_TO_CUT);
        return ActionResult.OK_JSON;
    }
}
