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

package org.jahia.modules.docspace;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.PublicationInfo;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Mar 26, 2010
 * Time: 5:27:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublishFile implements org.jahia.bin.Action {
    private static Logger logger = Logger.getLogger(SetACLAction.class);
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        JCRSessionWrapper jcrSessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(
                resource.getWorkspace(), resource.getLocale());
        try {
            final JCRPublicationService service = JCRPublicationService.getInstance();
            final Set<String> languages = Collections.singleton(resource.getLocale().toString());
//            final PublicationInfo publicationInfo = service.getPublicationInfo(resource.getNode().getIdentifier(), languages,
//                                                                               false, false, false, resource.getNode().getSession().getWorkspace().getName(), "live");
            boolean publishChildren = req.getParameter("publishChildren")!=null && Boolean.valueOf(req.getParameter("publishChildren"));
            service.publish(resource.getNode().getIdentifier(), resource.getWorkspace(), Constants.LIVE_WORKSPACE, languages,
                    publishChildren);
            jcrSessionWrapper.save();
            return ActionResult.OK;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return ActionResult.BAD_REQUEST;
        }
    }
}
