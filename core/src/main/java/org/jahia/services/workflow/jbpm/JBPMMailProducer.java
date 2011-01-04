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
package org.jahia.services.workflow.jbpm;

import org.apache.commons.lang.WordUtils;
import org.apache.velocity.tools.generic.DateTool;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.settings.SettingsBean;
import org.jbpm.api.Execution;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.email.impl.AddressTemplate;
import org.jbpm.pvm.internal.email.impl.AttachmentTemplate;
import org.jbpm.pvm.internal.email.impl.MailProducerImpl;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.script.*;
import java.io.StringWriter;
import java.security.Principal;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 14 sept. 2010
 */
public class JBPMMailProducer extends MailProducerImpl {
    private static final long serialVersionUID = -5084848266010688683L;
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JBPMMailProducer.class);
    ScriptEngine scriptEngine;
    private Bindings bindings;

    public Collection<Message> produce(Execution execution) {
        if (ServicesRegistry.getInstance().getMailService().isEnabled() && getTemplate()!=null) {
            try {
                ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
                scriptEngine = scriptEngineManager.getEngineByName(getTemplate().getLanguage());
                bindings = null;
                Message email = instantiateEmail();
                fillFrom(execution, email);

                fillRecipients(execution, email);
                fillSubject(execution, email);
                fillContent(execution, email);
                Address[] addresses = email.getRecipients(Message.RecipientType.TO);
                if (addresses != null && addresses.length > 0) {
                    return Collections.singleton(email);
                } else {
                    return Collections.emptyList();
                }
            } catch (MessagingException e) {
                logger.error(e.getMessage(),e);
            }
        }
        return Collections.emptyList();
    }

    @Override
    protected void fillSubject(Execution execution, Message email) throws MessagingException {
        String subject = getTemplate().getSubject();
        if (subject != null) {
            try {
                String evaluatedSubject = evaluateExpression(execution, subject).replaceAll("[\r\n]", "");
                email.setSubject(WordUtils.abbreviate(evaluatedSubject, 60, 74, "..."));
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            } catch (ScriptException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    protected void fillRecipients(Execution execution, Message email) throws MessagingException {
        try {
            ExecutionImpl exe = (ExecutionImpl) execution;
            SortedSet<String> emails = new TreeSet<String>();
            AddressTemplate addressTemplate = getTemplate().getTo();
            String s = "";
            if (addressTemplate != null) {
                s = addressTemplate.getUsers();
                if (!"".equals(s)) {
                    if ("assignable".equals(s)) {
                        emails.addAll(getAssignables(exe, s));
                    } else {
                        emails.add(evaluateExpression(execution, s));
                    }

                    for (String m : emails) {
                        if (m != null && !"".equals(m)) {
                            try {
                                InternetAddress address = new InternetAddress(m);
                                address.validate();
                                email.addRecipient(Message.RecipientType.TO, address);
                            } catch (MessagingException e) {
                                logger.debug(e.getMessage(), e);
                            }
                        }
                    }

                    emails.clear();
                }
            }
            if (!"".equals(s)) {
                addressTemplate = getTemplate().getCc();
                if (addressTemplate != null) {
                    s = addressTemplate.getUsers();
                    if ("assignable".equals(s)) {
                        emails.addAll(getAssignables(exe, s));
                    } else {
                        emails.add(evaluateExpression(execution, s));
                    }
                    for (String m : emails) {
                        if (m != null && !"".equals(m)) {
                            try {
                                InternetAddress address = new InternetAddress(m);
                                address.validate();
                                email.addRecipient(Message.RecipientType.CC, address);
                            } catch (MessagingException e) {
                                logger.debug(e.getMessage(), e);
                            }
                        }
                    }
                }
                addressTemplate = getTemplate().getBcc();
                if (addressTemplate != null) {
                    s = addressTemplate.getUsers();
                    if ("assignable".equals(s)) {
                        emails.addAll(getAssignables(exe, s));
                    } else {
                        emails.add(evaluateExpression(execution, s));
                    }
                    for (String m : emails) {
                        if (m != null && !"".equals(m)) {
                            try {
                                InternetAddress address = new InternetAddress(m);
                                address.validate();
                                email.addRecipient(Message.RecipientType.BCC, address);
                            } catch (MessagingException e) {
                                logger.debug(e.getMessage(), e);
                            }
                        }
                    }
                }
            }
        } catch (ScriptException e) {
            logger.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private SortedSet<String> getAssignables(ExecutionImpl exe, String s) throws RepositoryException {
        SortedSet<String> emails = new TreeSet<String>();
        WorkflowDefinition def = (WorkflowDefinition) exe.getVariable("workflow");
        String id = (String) exe.getVariable("nodeId");
        JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(id);
        List<JahiaPrincipal> principals = WorkflowService.getInstance().getAssignedRole(node, def,
                exe.getActivity().getDefaultOutgoingTransition().getDestination().getName(), exe.getProcessInstance().getId());
        for (JahiaPrincipal principal : principals) {
            if (principal instanceof JahiaGroup) {
                Collection<Principal> members = ((JahiaGroup) principal).getMembers();
                for (Principal member : members) {
                    if (member instanceof JahiaUser) {
                        final String property = ((JahiaUser) member).getProperty("j:email");
                        if (property != null) emails.add(property);
                    }
                }
            } else if (principal instanceof JahiaUser) {
                final String property = ((JahiaUser) principal).getProperty("j:email");
                if (property != null) emails.add(property);
            }
        }
        return emails;
    }

    /**
     * Fills the <code>from</code> attribute of the given email. The sender addresses are an
     * optional element in the mail template. If absent, each mail server supplies the current
     * user's email address.
     *
     * @see {@link javax.mail.internet.InternetAddress#getLocalAddress(javax.mail.Session)}
     */
    @Override
    protected void fillFrom(Execution execution, Message email) throws MessagingException {
        try {
            if (getTemplate().getFrom() == null) {
                email.setFrom(new InternetAddress(SettingsBean.getInstance().getMail_from()));
                return;
            }
            String scriptToExecute = getTemplate().getFrom().getUsers();
            String scriptResult = evaluateExpression(execution, scriptToExecute);
            email.setFrom(new InternetAddress(scriptResult));
        } catch (ScriptException e) {
            logger.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void fillContent(Execution execution, Message email) throws MessagingException {
        String text = getTemplate().getText();
        String html = getTemplate().getHtml();
        List<AttachmentTemplate> attachmentTemplates = getTemplate().getAttachmentTemplates();

        try {
            if (html != null || !attachmentTemplates.isEmpty()) {
                // multipart
                Multipart multipart = new MimeMultipart("related");

                // text
                if (text != null) {
                    BodyPart textPart = new MimeBodyPart();
                    text = evaluateExpression(execution, text);
                    textPart.setText(text);
                    multipart.addBodyPart(textPart);
                }

                // html
                if (html != null) {
                    BodyPart htmlPart = new MimeBodyPart();
                    html = evaluateExpression(execution, html);
                    htmlPart.setContent(html, "text/html");
                    multipart.addBodyPart(htmlPart);
                }

                // attachments
                if (!attachmentTemplates.isEmpty()) {
                    addAttachments(execution, multipart);
                }

                email.setContent(multipart);
            } else if (text != null) {
                // unipart
                text = evaluateExpression(execution, text);
                email.setText(text);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (ScriptException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String evaluateExpression(Execution execution, String scriptToExecute)
            throws RepositoryException, ScriptException {
        ScriptContext scriptContext = scriptEngine.getContext();
        if (bindings == null) {
            bindings = getBindings(execution);
        }
        scriptContext.setWriter(new StringWriter());
        scriptContext.setErrorWriter(new StringWriter());
        scriptEngine.eval(scriptToExecute, bindings);
        String error = scriptContext.getErrorWriter().toString();
        return scriptContext.getWriter().toString().trim();
    }

    private Bindings getBindings(Execution execution) throws RepositoryException {
        EnvironmentImpl environment = EnvironmentImpl.getCurrent();
        final Bindings bindings = new MyBindings(environment);
        ResourceBundle resourceBundle = ResourceBundle.getBundle(
                "org.jahia.services.workflow." + ((ExecutionImpl) execution).getProcessDefinition().getKey());
        bindings.put("bundle", resourceBundle);
        final Map<String, Object> vars = ((ExecutionImpl) execution).getVariables();
        JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(
                (String) vars.get("user"));
        if (jahiaUser.getProperty("j:email") != null) {
            bindings.put("user", jahiaUser);
        } else {
            bindings.put("user", null);
        }
        bindings.put("date", new DateTool());
        bindings.put("submissionDate", Calendar.getInstance());
        bindings.put("locale", vars.get("locale"));
        bindings.put("workspace", vars.get("workspace"));
        bindings.put("nodes", JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                @SuppressWarnings("unchecked") List<String> stringList = (List<String>) vars.get("nodeIds");
                List<JCRNodeWrapper> nodes = new LinkedList<JCRNodeWrapper>();
                for (String s : stringList) {
                    nodes.add(session.getNodeByUUID(s));
                }
                return nodes;
            }
        }));
        return bindings;
    }

    public class MyBindings extends SimpleBindings {
        private final Environment environment;

        public MyBindings(Environment environment) {
            super();
            this.environment = environment;
        }

        @Override
        public boolean containsKey(Object key) {
            return super.containsKey(key) || environment.get((String) key) != null;
        }

        @Override
        public Object get(Object key) {
            return super.containsKey(key) ? super.get(key) : environment.get((String) key);
        }

    }


}
