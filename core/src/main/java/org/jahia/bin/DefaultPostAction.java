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
    
    protected JCRNodeWrapper resolveParent(JCRSessionWrapper session, URLResolver urlResolver, String missingParentType, boolean createMissingParents) throws RepositoryException {
        String[] subPaths = Patterns.SLASH.split(urlResolver.getPath());
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
                            if (createMissingParents) {
                                node = node.addNode(subPath, missingParentType);
                            } else {
                                // return last existing node.
                                return node;
                            }
                        }
                    }
                }
            }
        }

        return node;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  final JCRSessionWrapper session, final Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        JCRNodeWrapper newNode = null;
        String[] subPaths = Patterns.SLASH.split(urlResolver.getPath());
        String lastPath = subPaths[subPaths.length - 1];

        String missingParentType = parameters.containsKey(Render.PARENT_TYPE) ? parameters.get(Render.PARENT_TYPE).get(0) : "jnt:contentList";
        JCRNodeWrapper node = resolveParent(session, urlResolver, missingParentType, true);
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
