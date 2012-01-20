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

package org.jahia.services.workflow.jbpm;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.tools.generic.DateTool;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.ScriptEngineUtils;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.identity.Group;
import org.jbpm.api.identity.User;
import org.jbpm.pvm.internal.email.impl.*;
import org.jbpm.pvm.internal.email.spi.AddressResolver;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.identity.spi.IdentitySession;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.script.*;
import java.io.StringWriter;
import java.util.*;

/**
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 14 sept. 2010
 */
public class JBPMMailProducer extends MailProducerImpl {
    private static final long serialVersionUID = -5084848266010688683L;
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JBPMMailProducer.class);
    ScriptEngine scriptEngine;
    private Bindings bindings;

    private String templateKey;

    public String getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
    }

    public Collection<Message> produce(final Execution execution) {
        final Map<String, Object> vars = ((ExecutionImpl) execution).getVariables();
        Locale locale = (Locale) vars.get("locale");
        
        if (templateKey != null) {
            MailTemplate template = null;
            MailTemplateRegistry templateRegistry = ((ProcessEngine) SpringContextSingleton.getBean("processEngine")).get(MailTemplateRegistry.class);
            if (locale != null) {
                template = (templateRegistry.getTemplate(templateKey + "." + locale.toString()));
                if (template == null) {
                    template = (templateRegistry.getTemplate(templateKey + "." + locale.getLanguage()));
                }
            }
            if (template == null) {
                template = templateRegistry.getTemplate(templateKey);
            }
            setTemplate(template);
        }

        if (ServicesRegistry.getInstance().getMailService().isEnabled() && getTemplate() != null) {
            try {
                return JCRTemplate.getInstance().doExecuteWithSystemSession(null,"default",locale,new JCRCallback<Collection<Message>>() {
                    public Collection<Message> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        try {
                            scriptEngine = ScriptEngineUtils.getInstance().getEngineByName(getTemplate().getLanguage());
                            bindings = null;
                            Message email = instantiateEmail();
                            fillFrom(email, execution, session);
                            fillRecipients(email, execution, session);
                            fillSubject(email, execution, session);
                            fillContent(email, execution, session);
                            Address[] addresses = email.getRecipients(Message.RecipientType.TO);
                            if (addresses != null && addresses.length > 0) {
                                return Collections.singleton(email);
                            } else {
                                return Collections.emptyList();
                            }
                        } catch (MessagingException e) {
                            logger.error(e.getMessage(), e);
                        } catch (ScriptException e) {
                            logger.error(e.getMessage(), e);
                        }
                        return Collections.emptyList();
                    }
                });
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return Collections.emptyList();
    }

    /**
       * Fills the <code>from</code> attribute of the given email. The sender addresses are an
       * optional element in the mail template. If absent, each mail server supplies the current
       * user's email address.
       *
       * @see {@link InternetAddress#getLocalAddress(Session)}
     */
    protected void fillFrom(Message email, Execution execution, JCRSessionWrapper session) throws MessagingException {
        try {
            AddressTemplate fromTemplate = getTemplate().getFrom();
            // "from" attribute is optional
            if (fromTemplate == null) return;

            // resolve and parse addresses
            String addresses = fromTemplate.getAddresses();
            if (addresses != null) {
                addresses = evaluateExpression(execution, addresses, session);
                // non-strict parsing applies to a list of mail addresses entered by a human
                email.addFrom(InternetAddress.parse(addresses, false));
            }

            EnvironmentImpl environment = EnvironmentImpl.getCurrent();
            IdentitySession identitySession = environment.get(IdentitySession.class);
            AddressResolver addressResolver = environment.get(AddressResolver.class);

            // resolve and tokenize users
            String userList = fromTemplate.getUsers();
            if (userList != null) {
                String[] userIds = tokenizeActors(userList, execution, session);
                List<User> users = identitySession.findUsersById(userIds);
                email.addFrom(resolveAddresses(users, addressResolver));
            }

            // resolve and tokenize groups
            String groupList = fromTemplate.getGroups();
            if (groupList != null) {
                for (String groupId : tokenizeActors(groupList, execution, session)) {
                    Group group = identitySession.findGroupById(groupId);
                    email.addFrom(addressResolver.resolveAddresses(group));
                }
            }
        } catch (ScriptException e) {
            logger.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected void fillRecipients(Message email, Execution execution, JCRSessionWrapper session) throws MessagingException {
        try {
            // to
            AddressTemplate to = getTemplate().getTo();
            if (to != null) {
                fillRecipients(to, email, Message.RecipientType.TO, execution, session);
            }

            // cc
            AddressTemplate cc = getTemplate().getCc();
            if (cc != null) {
                fillRecipients(cc, email, Message.RecipientType.CC, execution, session);
            }

            // bcc
            AddressTemplate bcc = getTemplate().getBcc();
            if (bcc != null) {
                fillRecipients(bcc, email, Message.RecipientType.BCC, execution, session);
            }
        } catch (ScriptException e) {
            logger.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void fillRecipients(AddressTemplate addressTemplate, Message email, Message.RecipientType recipientType, Execution execution, JCRSessionWrapper session) throws MessagingException, RepositoryException, ScriptException {
        // resolve and parse addresses
        String addresses = addressTemplate.getAddresses();
        if (addresses != null) {
            addresses = evaluateExpression(execution, addresses, session);
            // non-strict parsing applies to a list of mail addresses entered by a human
            email.addRecipients(recipientType, InternetAddress.parse(addresses, false));
        }

        EnvironmentImpl environment = EnvironmentImpl.getCurrent();
        IdentitySession identitySession = environment.get(IdentitySession.class);
        AddressResolver addressResolver = environment.get(AddressResolver.class);

        // resolve and tokenize users
        String userList = addressTemplate.getUsers();
        if (userList != null) {
            String[] userIds = tokenizeActors(userList, execution, session);
            List<User> users = identitySession.findUsersById(userIds);
            email.addRecipients(recipientType, resolveAddresses(users, addressResolver));
        }

        // resolve and tokenize groups
        String groupList = addressTemplate.getGroups();
        if (groupList != null) {
            for (String groupId : tokenizeActors(groupList, execution, session)) {
                Group group = identitySession.findGroupById(groupId);
                email.addRecipients(recipientType, addressResolver.resolveAddresses(group));
            }
        }
    }

    private String[] tokenizeActors(String recipients, Execution execution, JCRSessionWrapper session) throws RepositoryException, ScriptException{
        String[] actors = evaluateExpression(execution, recipients, session).split("[,;\\s]+");
        if (actors.length == 0) throw new JbpmException("recipient list is empty: " + recipients);
        return actors;
    }

    /** construct recipient addresses from user entities */
    private Address[] resolveAddresses(List<User> users, AddressResolver addressResolver) {
        int userCount = users.size();
        List<Address> addresses = new ArrayList<Address>();
        for (int i = 0; i < userCount; i++) {
            if (!StringUtils.isEmpty(users.get(i).getBusinessEmail())) {
                addresses.add(addressResolver.resolveAddress(users.get(i)));
            }
        }
        return addresses.toArray(new Address[addresses.size()]);
    }

    protected void fillSubject(Message email, Execution execution, JCRSessionWrapper session) throws MessagingException {
        String subject = getTemplate().getSubject();
        if (subject != null) {
            try {
                String evaluatedSubject = evaluateExpression(execution, subject, session).replaceAll("[\r\n]", "");
                email.setSubject(WordUtils.abbreviate(evaluatedSubject, 60, 74, "..."));
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            } catch (ScriptException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    protected void fillContent(Message email, Execution execution, JCRSessionWrapper session) throws MessagingException {
        String text = getTemplate().getText();
        String html = getTemplate().getHtml();
        List<AttachmentTemplate> attachmentTemplates = getTemplate().getAttachmentTemplates();

        try {
            if (html != null || !attachmentTemplates.isEmpty()) {
                // multipart
                MimeMultipart multipart = new MimeMultipart("related");

                BodyPart p = new MimeBodyPart();
                Multipart alternatives = new MimeMultipart("alternative");
                p.setContent(alternatives, "multipart/alternative");
                multipart.addBodyPart(p);

                // html
                if (html != null) {
                    BodyPart htmlPart = new MimeBodyPart();
                    html = evaluateExpression(execution, html, session);
                    htmlPart.setContent(html, "text/html; charset=UTF-8");
                    alternatives.addBodyPart(htmlPart);
                }

                // text
                if (text != null) {
                    BodyPart textPart = new MimeBodyPart();
                    text = evaluateExpression(execution, text, session);
                    textPart.setContent(text, "text/plain; charset=UTF-8");
                    alternatives.addBodyPart(textPart);
                }

                // attachments
                if (!attachmentTemplates.isEmpty()) {
                    addAttachments(execution, multipart);
                }

                email.setContent(multipart);
            } else if (text != null) {
                // unipart
                text = evaluateExpression(execution, text, session);
                email.setText(text);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (ScriptException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String evaluateExpression(Execution execution, String scriptToExecute, JCRSessionWrapper session)
            throws RepositoryException, ScriptException {
        ScriptContext scriptContext = scriptEngine.getContext();
        if (bindings == null) {
            bindings = getBindings(execution, session);
        }
        scriptContext.setWriter(new StringWriter());
        scriptContext.setErrorWriter(new StringWriter());
        scriptEngine.eval(scriptToExecute, bindings);
        String error = scriptContext.getErrorWriter().toString();
        if (error.length() > 0) {
            logger.error("Scripting error : " + error);
        }
        return scriptContext.getWriter().toString().trim();
    }

    private Bindings getBindings(Execution execution, JCRSessionWrapper session) throws RepositoryException {
        EnvironmentImpl environment = EnvironmentImpl.getCurrent();
        final Map<String, Object> vars = ((ExecutionImpl) execution).getVariables();
        Locale locale = (Locale) vars.get("locale");
        final Bindings bindings = new MyBindings(environment);
        ResourceBundle resourceBundle = JahiaResourceBundle.lookupBundle(
                "org.jahia.services.workflow." + ((ExecutionImpl) execution).getProcessDefinition().getKey(), locale);
        bindings.put("bundle", resourceBundle);
        JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(
                (String) vars.get("user"));
        bindings.put("user", jahiaUser);
        bindings.put("date", new DateTool());
        bindings.put("submissionDate", Calendar.getInstance());
        bindings.put("locale", locale);
        bindings.put("workspace", vars.get("workspace"));

        List<JCRNodeWrapper> nodes = new LinkedList<JCRNodeWrapper>();
        @SuppressWarnings("unchecked") List<String> stringList = (List<String>) vars.get("nodeIds");
        for (String s : stringList) {
            JCRNodeWrapper nodeByUUID = session.getNodeByUUID(s);
            if (!nodeByUUID.isNodeType("jnt:translation")) {
                nodes.add(nodeByUUID);
            }
        }
        bindings.put("nodes", nodes);
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
