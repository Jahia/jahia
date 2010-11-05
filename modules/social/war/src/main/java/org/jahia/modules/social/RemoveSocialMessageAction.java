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

package org.jahia.modules.social;

import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.jahia.bin.ActionResult;
import org.jahia.bin.BaseAction;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

/**
 * Action handler for removing social message.
 * 
 * @author Sergiy Shyrkov
 */
public class RemoveSocialMessageAction extends BaseAction {
    
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(RemoveSocialMessageAction.class);

    /* (non-Javadoc)
     * @see org.jahia.bin.Action#doExecute(javax.servlet.http.HttpServletRequest, org.jahia.services.render.RenderContext, org.jahia.services.render.Resource, java.util.Map, org.jahia.services.render.URLResolver)
     */
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
            Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {

        JCRSessionWrapper jcrSession = JCRSessionFactory.getInstance().getCurrentUserSession(resource.getWorkspace(),
                resource.getLocale());

        final String messageId = req.getParameter("messageId");

        if (messageId != null) {
            JCRNodeWrapper message = null;
            try {
                message = jcrSession.getNodeByIdentifier(messageId);
                jcrSession.checkout(message.getParent());
                message.remove();
                jcrSession.save();
            } catch (ItemNotFoundException e) {
                logger.warn("Unable to find message node for ID " + messageId);
                return ActionResult.BAD_REQUEST;
            }

        } else {
            return ActionResult.BAD_REQUEST;
        }

        return ActionResult.OK_JSON;
    }

}
