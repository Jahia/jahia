/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.*;
import org.jahia.services.logging.MetricsLoggingService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.tools.files.FileUpload;
import org.jahia.utils.Patterns;
import org.json.JSONObject;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Performs the default action when data is posted to the render servlet, i.e. creates or modifies a node.
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 11 mars 2010
 */
public class DefaultPostAction extends Action {

    public static final String ACTION_NAME = "default";

    private MetricsLoggingService loggingService;

    public DefaultPostAction() {
        setName(ACTION_NAME);
    }

    public MetricsLoggingService getLoggingService() {
        return loggingService;
    }

    public void setLoggingService(MetricsLoggingService loggingService) {
        this.loggingService = loggingService;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  final JCRSessionWrapper session, final Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        JCRNodeWrapper newNode = null;
        String[] subPaths = Patterns.SLASH.split(urlResolver.getPath());
        String lastPath = subPaths[subPaths.length - 1];
        JCRNodeWrapper node = null;
        StringBuilder realPath = new StringBuilder();
        String startPath = "";
        int index = 0;
        for (String subPath : subPaths) {
            index++;
            if (StringUtils.isNotBlank(subPath) && !"*".equals(subPath) && index != subPaths.length) {
                realPath.append("/").append(subPath);
                try {
                    session.getNode(JCRContentUtils.escapeNodePath(realPath.toString()));
                    startPath = "";

                } catch (PathNotFoundException e) {
                    if ("".equals(startPath)) {
                        startPath = realPath.substring(0,realPath.lastIndexOf("/"));
                    }
                }
            }
        }
        startPath = "".equals(startPath)?realPath.toString():startPath;
        realPath = realPath.delete(0,realPath.length());
        index = 0;
        for (String subPath : subPaths) {
            index++;
            if (StringUtils.isNotBlank(subPath) && !"*".equals(subPath) && index != subPaths.length) {
                realPath.append("/").append(subPath);
                if (realPath.toString().contains(startPath)) {
                    try {
                        node = session.getNode(JCRContentUtils.escapeNodePath(realPath.toString()));
                    } catch (PathNotFoundException e) {
                        if (node != null) {
                            if (!node.isCheckedOut()) {
                                session.checkout(node);
                            }
                            String parentType = "jnt:contentList";
                            if (parameters.containsKey(Render.PARENT_TYPE)) {
                                parentType = parameters.get(Render.PARENT_TYPE).get(0);
                            }
                            node = node.addNode(subPath, parentType);
                        }
                    }
                }
            }
        }
        if (node != null) {
            String nodeType = null;
            if (parameters.containsKey(Render.NODE_TYPE)) {
                nodeType = parameters.get(Render.NODE_TYPE).get(0);
            }
            if (StringUtils.isBlank(nodeType)) {
//                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing nodeType Property");
                return ActionResult.BAD_REQUEST;
            }
            String nodeName = null;
            if (parameters.containsKey(Render.NODE_NAME)) {
                nodeName = parameters.get(Render.NODE_NAME).get(0);
            }
            boolean forceCreation = false;
            if (!"*".equals(lastPath)) {
                nodeName = lastPath;
            } else {
                forceCreation = true;
            }
            try {
                newNode = createNode(req, parameters, node, nodeType, nodeName, forceCreation);
                final FileUpload fileUpload = (FileUpload) req.getAttribute(FileUpload.FILEUPLOAD_ATTRIBUTE);
                if (fileUpload != null && fileUpload.getFileItems() != null && fileUpload.getFileItems().size() > 0) {
                    final Map<String, DiskFileItem> stringDiskFileItemMap = fileUpload.getFileItems();
                    for (Map.Entry<String, DiskFileItem> itemEntry : stringDiskFileItemMap.entrySet()) {
                        newNode.uploadFile(itemEntry.getValue().getName(), itemEntry.getValue().getInputStream(),
                                JCRContentUtils.getMimeType(itemEntry.getValue().getName(), itemEntry.getValue().getContentType()));
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
            }

            final String nodeId = newNode.getIdentifier();
            if (parameters.containsKey(Render.AUTO_ASSIGN_ROLE) && !JahiaUserManagerService.isGuest(session.getUser())) {
                JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(session.getUser(), session.getWorkspace().getName(), null, new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper rootSession) throws RepositoryException {
                        JCRNodeWrapper createdNode = rootSession.getNodeByUUID(nodeId);
                        List<String> roles = parameters.get(Render.AUTO_ASSIGN_ROLE);
                        createdNode.grantRoles("u:" + session.getUser().getName(), new HashSet<String>(roles));
                        rootSession.save();
                        return null;
                    }
                });
            }


            if (parameters.containsKey(Render.AUTO_CHECKIN) && (parameters.get(Render.AUTO_CHECKIN)).get(
                    0).length() > 0) {
                newNode.checkpoint();
            }
        }

        String sessionID = "";
        HttpSession httpSession = req.getSession(false);
        if (httpSession != null) {
            sessionID = httpSession.getId();
        }
        String nodeIdentifier = null;
        if (newNode != null) {
            nodeIdentifier = newNode.getIdentifier();
        }
        if (loggingService.isEnabled()) {
            loggingService.logContentEvent(renderContext.getUser().getName(), req
                    .getRemoteAddr(), sessionID, nodeIdentifier, urlResolver.getPath(), parameters.get(Render.NODE_TYPE).get(0), "nodeCreated", new JSONObject(parameters).toString());
        }

        if (newNode != null) {
            return new ActionResult(HttpServletResponse.SC_CREATED, newNode.getPath(), Render.serializeNodeToJSON(newNode));
        } else {
            return null;
        }
    }

}
