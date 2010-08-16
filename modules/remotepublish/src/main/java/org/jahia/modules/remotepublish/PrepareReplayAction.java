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

package org.jahia.modules.remotepublish;

import org.apache.jackrabbit.util.ISO8601;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.log4j.Logger;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 22, 2010
 * Time: 3:05:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrepareReplayAction implements Action {
    private static Logger logger = Logger.getLogger(RemotePublishAction.class);
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        final JCRNodeWrapper node = resource.getNode();
        if (!parameters.containsKey("sourceUuid")) {
            return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject());
        }
        Map map = new HashMap();
        if (node.isNodeType("jmix:remotelyPublished")) {
            String uuid = node.getProperty("uuid").getString();
            if (uuid.equals(parameters.get("sourceUuid").get(0))) {
                map.put("ready", Boolean.TRUE);
                if (node.hasProperty("lastReplay")) {
                    Calendar last = node.getProperty("lastReplay").getDate();
                    map.put("lastReplay", ISO8601.format(last));
                }
            }
        } else {
            node.checkout();
            node.addMixin("jmix:remotelyPublished");
            node.setProperty("uuid", parameters.get("sourceUuid").get(0));
            node.getSession().save();
            map.put("ready", Boolean.TRUE);
        }
        return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject(map));
    }
}
