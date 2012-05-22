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

package org.jahia.bin;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.logging.MetricsLoggingService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONObject;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DefaultPutAction extends Action {
    
    private MetricsLoggingService loggingService;

    public MetricsLoggingService getLoggingService() {
        return loggingService;
    }

    public void setLoggingService(MetricsLoggingService loggingService) {
        this.loggingService = loggingService;
    }    
    
    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        JCRNodeWrapper node = session.getNode(urlResolver.getPath());
        // Get node information before any modification as a rule can delete it
        String primaryNodeTypeName = node.getPrimaryNodeTypeName();
        session.checkout(node);
        
        if (parameters.containsKey(Render.REMOVE_MIXIN)) {
            for (String mixinType : parameters.get(Render.REMOVE_MIXIN)) {
                node.removeMixin(mixinType);
            }
        }
        if (parameters.containsKey(Constants.JCR_MIXINTYPES)) {
            for (String mixinType : parameters.get(Constants.JCR_MIXINTYPES)) {
                node.addMixin(mixinType);
            }
        }
        Set<Map.Entry<String, List<String>>> set = parameters.entrySet();
        try {
            for (Map.Entry<String, List<String>> entry : set) {
                String key = entry.getKey();
                if (!Render.getReservedParameters().contains(key)) {
                    List<String> values = entry.getValue();
                    final ExtendedPropertyDefinition propertyDefinition =
                            ((JCRNodeWrapper) node).getApplicablePropertyDefinition(key);
                    if (propertyDefinition == null) {
                        continue;
                    }
                    if (propertyDefinition.isMultiple()) {
                        node.setProperty(key, values.toArray(new String[values.size()]));
                    } else if (propertyDefinition.getRequiredType() == PropertyType.DATE) {
                        // Expecting ISO date yyyy-MM-dd'T'HH:mm:ss
                        DateTime dateTime = ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(values.get(0));
                        node.setProperty(key, dateTime.toCalendar(Locale.ENGLISH));
                    } else {
                        node.setProperty(key, values.get(0));
                    }
                }
            }
        } catch (ConstraintViolationException e) {
            return ActionResult.BAD_REQUEST;
        }
        session.save();
        if (req.getParameter(Render.AUTO_CHECKIN) != null && req.getParameter(Render.AUTO_CHECKIN).length() > 0) {
            session.getWorkspace().getVersionManager().checkpoint(node.getPath());
        }

        String sessionID = "";
        HttpSession httpSession = req.getSession(false);
        if (httpSession != null) {
            sessionID = httpSession.getId();
        }
        if (loggingService.isEnabled()) {
            loggingService.logContentEvent(renderContext.getUser().getName(), req.getRemoteAddr(), sessionID,
                    node.getIdentifier(), urlResolver.getPath(), primaryNodeTypeName, "nodeUpdated",
                    new JSONObject(req.getParameterMap()).toString());
        }
        
        final String requestWith = req.getHeader("x-requested-with");
        if (req.getHeader("accept").contains("application/json") && requestWith != null &&
                requestWith.equals("XMLHttpRequest")) {
            JSONObject jsonObject = null;
            try {
                jsonObject = Render.serializeNodeToJSON(node);
            } catch (RepositoryException e) {}
            return new ActionResult(HttpServletResponse.SC_OK, node.getPath(), jsonObject);
        } else {
            return ActionResult.OK;
        }
    }
}
