/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.bin;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.params.ParamBean;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.services.logging.MetricsLoggingService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.tools.files.FileUpload;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONObject;
import org.springframework.web.bind.ServletRequestUtils;

import javax.jcr.PathNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 11 mars 2010
 */
public class DefaultPostAction implements Action {
    public static final String ACTION_NAME = "default";

    private MetricsLoggingService loggingService;

    public DefaultPostAction() {
    }

    public MetricsLoggingService getLoggingService() {
        return loggingService;
    }

    public void setLoggingService(MetricsLoggingService loggingService) {
        this.loggingService = loggingService;
    }

    public String getName() {
        return ACTION_NAME;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(urlResolver.getWorkspace(),
                                                                                          urlResolver.getLocale());
        JCRNodeWrapper newNode = null;
        String[] subPaths = urlResolver.getPath().split("/");
        String lastPath = subPaths[subPaths.length - 1];
        JCRNodeWrapper node = null;
        StringBuffer realPath = new StringBuffer();
        for (String subPath : subPaths) {
            if (StringUtils.isNotBlank(subPath) && !"*".equals(subPath) && !subPath.equals(lastPath)) {
                realPath.append("/").append(subPath);
                try {
                    node = session.getNode(realPath.toString());
                } catch (PathNotFoundException e) {
                    if (node != null) {
                        node = node.addNode(subPath, "jnt:folder");
                    }
                }
            }
        }
        if (node != null) {
            String nodeType = null;
            if (parameters.containsKey(Render.NODE_TYPE)) {
                nodeType = (String) ((List) parameters.get(Render.NODE_TYPE)).get(0);
            }
            if (StringUtils.isBlank(nodeType)) {
//                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing nodeType Property");
                return new ActionResult(HttpServletResponse.SC_BAD_REQUEST,null,null);
            }
            String nodeName = null;
            if (parameters.containsKey(Render.NODE_NAME)) {
                nodeName = (String) ((List) parameters.get(Render.NODE_NAME)).get(0);
            }
            if (!"*".equals(lastPath)) {
                nodeName = lastPath;
            }
            if (StringUtils.isBlank(nodeName)) {
                if (parameters.get("jcr:title") != null) {
                    nodeName = JCRContentUtils.generateNodeName(parameters.get("jcr:title").get(0), 32);
                } else {
                    nodeName = nodeType.substring(nodeType.lastIndexOf(":") + 1);
                }
                nodeName = JCRContentUtils.findAvailableNodeName(node, nodeName, session);
            }
            if (ServletRequestUtils.getBooleanParameter(req, Render.NORMALIZE_NODE_NAME, false)) {
                nodeName = JCRContentUtils.generateNodeName(nodeName, 255);
            }
            try {
                newNode = session.getNode(realPath + "/" + nodeName);
                if (!newNode.isCheckedOut()) {
                    newNode.checkout();
                }
            } catch (PathNotFoundException e) {
                if (!node.isCheckedOut()) {
                    node.checkout();
                }
                newNode = node.addNode(nodeName, nodeType);
            }
            if (parameters.containsKey(Constants.JCR_MIXINTYPES)) {
                for (Object o : ((ArrayList) parameters.get(Constants.JCR_MIXINTYPES))) {
                    String mixin = (String) o;
                    newNode.addMixin(mixin);
                }
            }
            Set<Map.Entry<String, List<String>>> set = parameters.entrySet();
            for (Map.Entry<String, List<String>> entry : set) {
                String key = entry.getKey();
                if (!Render.reservedParameters.contains(key)) {
                    List<String> values = entry.getValue();
                    if (!values.get(0).equals("Submit")) {
                        ExtendedPropertyDefinition propertyDefinition = newNode.getApplicablePropertyDefinition(key);
                        if (propertyDefinition.isMultiple()) {
                            newNode.setProperty(key, values.toArray(new String[values.size()]));
                        } else if(values.get(0).length() > 0){
                            if(propertyDefinition.getRequiredType() == ExtendedPropertyType.DATE) {
                                DateTime dateTime = ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(values.get(0));
                                newNode.setProperty(key,dateTime.toCalendar(Locale.ENGLISH));
                            } else {
                                newNode.setProperty(key, values.get(0));
                            }
                        }
                    }
                }
            }
            final ParamBean paramBean = (ParamBean) Jahia.getThreadParamBean();
            final FileUpload fileUpload = paramBean.getFileUpload();
            if (fileUpload != null && fileUpload.getFileItems() != null && fileUpload.getFileItems().size() > 0) {
                final Map<String, DiskFileItem> stringDiskFileItemMap = fileUpload.getFileItems();
                for (Map.Entry<String, DiskFileItem> itemEntry : stringDiskFileItemMap.entrySet()) {
                    newNode.uploadFile(itemEntry.getValue().getName(), itemEntry.getValue().getInputStream(),
                                       itemEntry.getValue().getContentType());
                }
            }
            session.save();
            if (parameters.containsKey(Render.AUTO_CHECKIN) && ((String) ((List) parameters.get(Render.AUTO_CHECKIN)).get(
                    0)).length() > 0) {
                newNode.checkin();
            }
        }

        String sessionID = "";
        HttpSession httpSession = req.getSession(false);
        if (httpSession != null) {
            sessionID = httpSession.getId();
        }
        loggingService.logContentEvent(renderContext.getUser().getName(), req
                .getRemoteAddr(), sessionID, urlResolver.getPath(), (String) ((List) parameters.get(Render.NODE_TYPE)).get(0), "nodeCreated", new JSONObject(parameters).toString());

        if (newNode != null) {
            return new ActionResult(HttpServletResponse.SC_CREATED, newNode.getPath(), Render.serializeNodeToJSON(newNode));
        } else {
            return null;
        }
    }
}
