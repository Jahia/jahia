/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.modules.forum.actions;

import org.jahia.api.Constants;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.bin.Render;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.slf4j.Logger;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 3 juin 2010
 */
public class AddTopic extends Action {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(AddTopic.class);

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        JCRSessionWrapper jcrSessionWrapper = resource.getNode().getSession();
        JCRNodeWrapper node = resource.getNode();
        if (!node.isNodeType("jnt:topic")) {
            String topicTitle = parameters.get("jcr:title").get(0);
            node.checkout();
            node = node.addNode(JCRContentUtils.generateNodeName(topicTitle, 32), "jnt:topic");
            node.setProperty("topicSubject",topicTitle);
        }
        JCRNodeWrapper newNode = createNode(req, parameters, jcrSessionWrapper.getNode(node.getPath()), "jnt:post","");

        if (!session.getUser().getUsername().equals(Constants.GUEST_USERNAME)) {
            List<String> roles = Arrays.asList("owner");
            newNode.grantRoles("u:"+session.getUser().getUsername(), new HashSet<String>(roles));
        }

        jcrSessionWrapper.save();
        // Remove any existing REDIRECT_TO parameter to be sure to go to node.getPath
        parameters.remove(Render.REDIRECT_TO);
        return new ActionResult(HttpServletResponse.SC_OK, node.getPath(), Render.serializeNodeToJSON(newNode));
    }
}
