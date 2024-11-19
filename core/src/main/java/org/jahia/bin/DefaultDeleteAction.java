/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.bin;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

import javax.jcr.Node;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class DefaultDeleteAction extends Action {

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        JCRNodeWrapper node = session.getNode(urlResolver.getPath());
        String url = null;

        String mark = req.getParameter(Render.MARK_FOR_DELETION);
        if (mark != null && mark.length() > 0) {
            if (Boolean.valueOf(mark)) {
                // mark for deletion
                node.markForDeletion(req.getParameter(Render.MARK_FOR_DELETION_MESSAGE));
            } else {
                // unmark the deletion
                node.unmarkForDeletion();
            }
            url = node.getPath();
        } else {
            // do node deletion
            Node parent = node.getParent();

            if (!parent.isCheckedOut()) {
                session.checkout(parent);
            }
            if (!node.isCheckedOut()) {
                session.checkout(node);
            }
            node.remove();
            url = parent.getPath();
        }

        session.save();

        final String requestWith = req.getHeader("x-requested-with");

        if (req.getHeader("accept") != null && req.getHeader("accept").contains("application/json") && requestWith != null &&
                requestWith.equals("XMLHttpRequest")) {
            return ActionResult.OK_JSON;
        } else {
            return new ActionResult(HttpServletResponse.SC_NO_CONTENT, url, new JSONObject());
        }
    }
}
