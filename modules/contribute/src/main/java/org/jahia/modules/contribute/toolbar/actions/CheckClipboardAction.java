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
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 24 nov. 2010
 */
public class CheckClipboardAction extends Action {
    private transient static Logger logger = Logger.getLogger(CheckClipboardAction.class);

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        List<String> uuids = (List<String>) req.getSession().getAttribute(MultipleCopyAction.UUIDS_TO_COPY);
        List<String> uuidstoCut = (List<String>) req.getSession().getAttribute(MultipleCutAction.UUIDS_TO_CUT);
        if (uuids == null) {
            uuids = uuidstoCut;
        } else if (uuidstoCut != null) {
            uuids.addAll(uuidstoCut);
        }
        if (uuids != null && uuids.size() > 0) {
            JSONObject json = new JSONObject();
            json.put(URLEncoder.encode(MultipleCopyAction.UUIDS,"UTF-8"), uuids);
            List<String> paths = new LinkedList<String>();
            List<String> nodetypes = new LinkedList<String>();
            for (String uuid : uuids) {
                try {
                    JCRNodeWrapper nodeByUUID = session.getNodeByUUID(uuid);
                    paths.add(nodeByUUID.getPath());
                    List<String> nodeTypes = nodeByUUID.getNodeTypes();
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String nodeType : nodeTypes) {
                        if(stringBuilder.length()>0) {
                            stringBuilder.append(" ");
                        }
                        stringBuilder.append(nodeType);
                    }
                    nodetypes.add(stringBuilder.toString());
                } catch (RepositoryException e) {                    
                }
            }
            json.put("paths",paths);
            json.put("nodetypes",nodetypes);
            json.put("size", uuids.size());
            return new ActionResult(HttpServletResponse.SC_OK, null, json);
        } else {
            return ActionResult.OK;
        }
    }
}
