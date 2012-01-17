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

package org.jahia.modules.tags.actions;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


public class RemoveTag extends Action {

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        JCRSessionWrapper jcrSessionWrapper = resource.getNode().getSession();
        JCRNodeWrapper node = resource.getNode();
        String tagToDelete = req.getParameter("tag");
        Map<String,String> res = new HashMap<String,String>();
        if (tagToDelete != null) {
            JCRNodeWrapper tag = session.getNode("/sites/" + urlResolver.getSiteKey() + "/tags/" + tagToDelete.trim());
            Map<String, String> properties = node.getPropertiesAsString();
            String[] tags = properties.get("j:tags").split(" ");
            ArrayList<String> tagsList = new ArrayList<String>();
            tagsList.addAll(Arrays.asList(tags));
            if (tagsList.contains(tag.getIdentifier())) {
                if (tagsList.size() > 0) {
                    for (int i = 0; i < tagsList.size(); i++) {
                        if (tagsList.get(i).equals(tag.getIdentifier())) {
                            tagsList.remove(i);
                        }
                    }
                    String[] str = tagsList.toArray(new String[tagsList.size()]);
                    node.setProperty("j:tags", str);
                    jcrSessionWrapper.save();
                } else {
                     node.removeMixin("jmix:tagged");
                }
            }
            res.put("size", String.valueOf(tagsList.size()));
        }
        return new ActionResult(HttpServletResponse.SC_OK, node.getPath(), new JSONObject(res));
    }
}
