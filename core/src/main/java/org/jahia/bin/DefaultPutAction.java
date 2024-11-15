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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.api.Constants;
import org.jahia.services.content.CompositeConstraintViolationException;
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
import javax.jcr.Value;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.*;

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
                        if (values.size() == 1 && values.get(0).equals("jcrClearAllValues")) {
                            node.setProperty(key, new Value[0]);
                        } else {
                            node.setProperty(key, values.toArray(new String[values.size()]));
                        }
                    } else if (propertyDefinition.getRequiredType() == PropertyType.DATE) {
                        // Expecting ISO date yyyy-MM-dd'T'HH:mm:ss
                        DateTime dateTime = ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(values.get(0));
                        node.setProperty(key, dateTime.toCalendar(Locale.ENGLISH));
                    } else {
                        node.setProperty(key, values.get(0));
                    }
                }
            }
            session.save();
        } catch (CompositeConstraintViolationException e) {
            List<JSONObject> jsonErrors = new ArrayList<JSONObject>();
            for (ConstraintViolationException exception : e.getErrors()) {
                jsonErrors.add(getJSONConstraintError(exception));
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("validationError", jsonErrors);
            return new ActionResult(HttpServletResponse.SC_BAD_REQUEST, null, jsonObject);
        } catch (ConstraintViolationException e) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("validationError", Arrays.asList(getJSONConstraintError(e)));
            return new ActionResult(HttpServletResponse.SC_BAD_REQUEST, null, jsonObject);
        } catch (Exception e) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("error", e.getMessage());
            return new ActionResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, jsonObject);
        }

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
        if (req.getHeader("accept") != null && req.getHeader("accept").contains("application/json") && requestWith != null &&
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
