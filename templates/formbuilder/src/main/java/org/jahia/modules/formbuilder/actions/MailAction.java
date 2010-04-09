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
package org.jahia.modules.formbuilder.actions;

import groovy.lang.Binding;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.log4j.Logger;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.bin.Jahia;
import org.jahia.bin.Render;
import org.jahia.params.ParamBean;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.mail.MailService;
import org.jahia.services.notification.templates.Attachment;
import org.jahia.services.notification.templates.Link;
import org.jahia.services.notification.templates.MessageBuilder;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.tools.files.FileUpload;
import org.json.JSONObject;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 9 mars 2010
 */
public class MailAction implements Action {
    private transient static Logger logger = Logger.getLogger(MailAction.class);
    private String name;
    private MailService mailService;
    private JahiaUserManagerService userManagerService;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    public ActionResult doExecute(HttpServletRequest req, final RenderContext renderContext,
                                  final Resource resource, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        if (!mailService.isEnabled()) {
            logger.info("Mail service is disabled. Skip sending e-mail notification for form action");
        }
        JCRNodeWrapper node = renderContext.getMainResource().getNode();
        final String path = node.getParent().getPath();
        JCRNodeWrapper actionNode = null;
        NodeIterator nodes = node.getParent().getNode("action").getNodes();
        while (nodes.hasNext()) {
            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodes.nextNode();
            if(nodeWrapper.isNodeType("jnt:mailFormAction")) {
                actionNode = (JCRNodeWrapper) nodeWrapper;
            }
        }
        if (actionNode!=null) {
            JahiaUser to = userManagerService.lookupUser(node.getSession().getNodeByUUID(actionNode.getProperty("j:to").getValue().getString()).getName());
            Set<String> reservedParameters = Render.getReservedParameters();
            final Map<String, List<String>> formDatas = new HashMap<String, List<String>>();
            Set<Map.Entry<String, List<String>>> set = parameters.entrySet();
            for (Map.Entry<String, List<String>> entry : set) {
                String key = entry.getKey();
                if (!reservedParameters.contains(key)) {
                    List<String> values = entry.getValue();
                    formDatas.put(key, values);
                }
            }
            MessageBuilder message = new MessageBuilder(to, renderContext.getSite().getID(), "Jahia Form Builder") {
                @Override
                protected String getTemplateHtmlPart() {
                    return lookupTemplate("/action/mail/body.html");
                }

                @Override
                protected String getTemplateMailScript() {
                    return lookupTemplate("/action/mail/email.groovy");
                }

                @Override
                protected String getTemplateTextPart() {
                    return lookupTemplate("/action/mail/body.txt");
                }

                @Override
                protected Link getUnsubscribeLink() {
                    return null;
                }

                @Override
                protected void populateBinding(Binding binding) {
                    super.populateBinding(binding);
                    binding.setVariable("formDatas", formDatas);
                    try {
                        binding.setVariable("formNode", JCRSessionFactory.getInstance().getCurrentUserSession(resource.getWorkspace(),resource.getLocale()).getNode(path));
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            };
            
            final ParamBean paramBean = (ParamBean) Jahia.getThreadParamBean();
            final FileUpload fileUpload = paramBean.getFileUpload();
            if (fileUpload != null && fileUpload.getFileItems() != null && fileUpload.getFileItems().size() > 0) {
                for (DiskFileItem file : fileUpload.getFileItems().values()) {
                    message.getAttachments().add(new Attachment(file.getName(), new ByteArrayDataSource(file.getInputStream(), file.getContentType())));
                }
                fileUpload.markFilesAsConsumed();
            }
            
            mailService.sendTemplateMessage(message);
            
            logger.info("Form data is sent by e-mail");
        }
        return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject());
    }
}
