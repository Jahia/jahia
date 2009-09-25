/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.operations.valves;

import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.data.events.JahiaEvent;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.captcha.CaptchaService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.registries.ServicesRegistry;
import org.apache.commons.fileupload.disk.DiskFileItem;

import javax.jcr.RepositoryException;
import javax.jcr.Node;
import java.util.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 1, 2009
 * Time: 12:33:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class FormValve implements Valve {
    private static Map<String,FormAction> tokens = new HashMap<String, FormAction>();
    private static Map<FormAction,String> reverseTokens = new HashMap<FormAction,String>();
    private static Map<String,String> userTokens = new HashMap<String, String>();

    public void initialize() {          
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        if (context instanceof ParamBean) {
            ParamBean jParams = (ParamBean) context;
            String token = jParams.getParameter("formToken");
            String userToken = jParams.getParameter("formUserToken");
            if (tokens.containsKey(token) && userTokens.containsKey(userToken) && userTokens.get(userToken).equals(token)) {
                FormAction action = tokens.get(token);
                if (Boolean.TRUE.equals(action.getParams().get("checkCaptcha"))) {
                    boolean captchaOk = false;
                    try {
                        captchaOk = CaptchaService.getInstance().validateResponseForID(jParams.getSessionID(), jParams.getRequest().getParameter("captcha"));
                    } catch (Exception e ) {
                    }
                    if (!captchaOk) {
                        valveContext.invokeNext(context);
                        return;
                    }
                }
                String type = action.getActionType();
                userTokens.remove(userToken);
                try {
                    JahiaUser jahiaUser = jParams.getUser();
                    if (Boolean.TRUE.equals(action.getParams().get("ignoreAcl"))) {
                        jahiaUser = JahiaAdminUser.getAdminUser(jParams.getSiteID());
                    }
                    if (type.equals("createNode") || type.equals("updateNode")) {
                        try {
                            final JahiaEvent theEvent = new JahiaEvent(this, jParams, null);
                            ServicesRegistry.getInstance().getJahiaEventService().fireBeforeFormHandling(theEvent);
                        } catch (JahiaException e) {
                            e.printStackTrace(); 
                        }
                    }
                    
                    if (type.equals("createNode")) {
                        // fire event
                        JCRSessionWrapper session = JCRSessionFactory.getInstance().getThreadSession(jahiaUser);
                        Node parent = session.getNodeByUUID((String) action.getParams().get("target"));
                        Node child = parent.addNode("node" + System.currentTimeMillis(), (String) action.getParams().get("nodeType"));
                        setProperties(child, jParams, token);
                        session.save();
                    } else if (type.equals("updateNode")) {
                        JCRSessionWrapper session = JCRSessionFactory.getInstance().getThreadSession(jahiaUser);
                        Node node = session.getNodeByUUID((String) action.getParams().get("target"));
                        setProperties(node, jParams, token);
                        session.save();
                    } else if (type.equals("sendMail")) {

                    }

                } catch (RepositoryException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                ServicesRegistry.getInstance().getJahiaEventService().fireAggregatedEvents();
            }
        }
        valveContext.invokeNext(context);
    }

    public static synchronized String createNewToken(String actionType, Map<String,Object> params) {
        FormAction formAction = new FormAction(actionType, params);
        if (reverseTokens.containsKey(formAction)) {
            return reverseTokens.get(formAction);
        }
        String key = UUID.randomUUID().toString();
        tokens.put(key, formAction);
        reverseTokens.put(formAction, key);
        return key;
    }

    public static String createNewUserToken(String token) {
        String key = UUID.randomUUID().toString();
        userTokens.put(key, token);
        return key;
    }

    private void setProperties(Node n, ParamBean r, String tok) throws RepositoryException {
        if (r.getFileUpload() != null) {
            Map<String,Object> parameters = r.getFileUpload().getParameterMap();
            for (String key : parameters.keySet()) {
                String hash = Integer.toString(tok.hashCode());
                if (key.endsWith(hash) && parameters.get(key) instanceof List) {
                    List<String> values = (List<String>) parameters.get(key);
                    if (values.size()>0) {
                        String name = key.substring(0,key.length() - hash.length());
                        n.setProperty(name, values.get(0));
                    }
                }
            }
            Map<String, DiskFileItem> files = r.getFileUpload().getFileItems();
            for (String key : files.keySet()) {
                String hash = Integer.toString(tok.hashCode());
                if (key.endsWith(hash)) {
                    DiskFileItem file = files.get(key);
                    String name = key.substring(0,key.length() - hash.length());
                    ExtendedPropertyDefinition epd = ((ExtendedNodeType)n.getPrimaryNodeType()).getPropertyDefinitionsAsMap().get(name);
                    String path = epd.getSelectorOptions().get("path");
                    try {
                        if (path.contains("$home")) {
                            List<JCRNodeWrapper> f = JCRStoreService.getInstance().getUserFolders(r.getSiteKey(),r.getUser());
                            if (f.size()>0) {
                                path = path.replace("$home",f.iterator().next().getPath()+"/files");
                            } else {
                                continue;
                            }
                        }
                        JCRNodeWrapper folder = JCRStoreService.getInstance().getFileNode(path,r.getUser());
                        JCRNodeWrapper uploadedFile = folder.uploadFile(file.getName(), file.getInputStream(), file.getContentType());
                        folder.save();
                        n.setProperty(name, uploadedFile.getStorageName());
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                }
            }
        } else {
            Map<String,Object> parameters = r.getParameterMap();
            for (String key : parameters.keySet()) {
                String hash = Integer.toString(tok.hashCode());
                if (key.endsWith(hash)) {
                    String[] values = (String[]) parameters.get(key);
                    if (values.length>0) {
                        String name = key.substring(0,key.length() - hash.length());
                        n.setProperty(name, (String) values[0]);
                    }
                }
            }

        }
    }

    public static class FormAction {
        private String actionType;
        private Map<String,Object> params;

        public FormAction(String actionType, Map<String,Object> params) {
            this.actionType = actionType;
            this.params = params;
        }

        public String getActionType() {
            return actionType;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FormAction that = (FormAction) o;

            if (actionType != null ? !actionType.equals(that.actionType) : that.actionType != null) return false;
            if (params != null ? !params.equals(that.params) : that.params != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = actionType != null ? actionType.hashCode() : 0;
            result = 31 * result + (params != null ? params.hashCode() : 0);
            return result;
        }
    }

}
