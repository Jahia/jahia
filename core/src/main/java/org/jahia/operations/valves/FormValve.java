package org.jahia.operations.valves;

import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.content.ContentObject;
import org.jahia.params.ParamBean;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.SelectorType;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.captcha.CaptchaService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.filemanagement.server.helper.Utils;
import org.jahia.ajax.gwt.filemanagement.server.GWTFileManagerUploadServlet;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.apache.commons.fileupload.disk.DiskFileItem;

import javax.jcr.RepositoryException;
import javax.jcr.Node;
import javax.jcr.Value;
import javax.servlet.ServletRequest;
import java.util.*;
import java.io.IOException;

import com.octo.captcha.service.CaptchaServiceException;

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

    public void initialize() {          
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        if (context instanceof ParamBean) {
            ParamBean jParams = (ParamBean) context;
            String token = jParams.getParameter("formToken");
            if (tokens.containsKey(token)) {
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
                try {
                    JahiaUser jahiaUser = jParams.getUser();
                    if (Boolean.TRUE.equals(action.getParams().get("ignoreAcl"))) {
                        jahiaUser = JahiaAdminUser.getAdminUser(jParams.getSiteID());
                    }
                    if (type.equals("createNode")) {
                        Node parent = JCRStoreService.getInstance().getNodeByUUID((String) action.getParams().get("target"), jahiaUser);
                        Node child = parent.addNode("node" + System.currentTimeMillis(), (String) action.getParams().get("nodeType"));
                        setProperties(child, jParams, token);
                        parent.save();
                    } else if (type.equals("updateNode")) {
                        Node node = JCRStoreService.getInstance().getNodeByUUID((String) action.getParams().get("target"), jahiaUser);
                        setProperties(node, jParams, token);
                        node.save();
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

    private void setProperties(Node n, ParamBean r, String tok) throws RepositoryException {
        Map<String,Object> parameters = r.getFileUpload().getParameterMap();
        for (String key : parameters.keySet()) {
            String hash = Integer.toString(tok.hashCode());
            if (key.endsWith(hash) && parameters.get(key) instanceof String[]) {
                String[] values = (String[]) parameters.get(key);
                String name = key.substring(0,key.length() - hash.length());
                n.setProperty(name, values[0]);
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
