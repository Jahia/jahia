/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.workflow.jbpm.custom.email;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.tools.generic.DateTool;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.jbpm.JBPMTaskIdentityService;
import org.jahia.utils.ScriptEngineUtils;
import org.jahia.utils.i18n.ResourceBundles;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.TaskIdentityService;
import org.slf4j.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.jcr.RepositoryException;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.script.*;
import java.io.File;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A work item handler to send email. This implementation extends the built-in JBPM 6 implementation by
 * re-introducing the mail template mechanism available in JBPM 4
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 14 sept. 2010
 */
public class JBPMMailProducer {
    private static final long serialVersionUID = -5084848266010688683L;
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JBPMMailProducer.class);
    private static final Pattern ACTORS_PATTERN = Pattern.compile("[,;\\s]+");
    ScriptEngine scriptEngine;
    private Bindings bindings;

    private ThreadLocal<MailTemplate> template = new ThreadLocal<MailTemplate>();
    private MailTemplateRegistry mailTemplateRegistry;
    private TaskIdentityService taskIdentityService;

    public void setMailTemplateRegistry(MailTemplateRegistry mailTemplateRegistry) {
        this.mailTemplateRegistry = mailTemplateRegistry;
    }

    public void setTaskIdentityService(TaskIdentityService taskIdentityService) {
        this.taskIdentityService = taskIdentityService;
    }

    public MailTemplate getTemplate() {
        return template.get();
    }

    public void setTemplate(MailTemplate template) {
        this.template.set(template);
    }

    public Collection<Message> produce(final WorkItem workItem) {
        final Map<String, Object> vars = workItem.getParameters();
        Locale locale = (Locale) vars.get("locale");
        String templateKey = (String) vars.get("templateKey");

        if (templateKey != null) {
            MailTemplate template = null;
            if (locale != null) {
                template = (mailTemplateRegistry.getTemplate(templateKey + "." + locale.toString()));
                if (template == null) {
                    template = (mailTemplateRegistry.getTemplate(templateKey + "." + locale.getLanguage()));
                }
            }
            if (template == null) {
                template = mailTemplateRegistry.getTemplate(templateKey);
            }
            setTemplate(template);
        }

        if (ServicesRegistry.getInstance().getMailService().isEnabled() && getTemplate() != null) {
            try {
                return JCRTemplate.getInstance().doExecuteWithSystemSession(null, "default", locale, new JCRCallback<Collection<Message>>() {
                    public Collection<Message> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        try {
                            scriptEngine = ScriptEngineUtils.getInstance().getEngineByName(getTemplate().getLanguage());
                            bindings = null;
                            Message email = instantiateEmail();
                            fillFrom(email, workItem, session);
                            fillRecipients(email, workItem, session);
                            fillSubject(email, workItem, session);
                            fillContent(email, workItem, session);
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

    protected Message instantiateEmail() {
        return new MimeMessage((Session) null);
    }

    /**
     * Fills the <code>from</code> attribute of the given email. The sender addresses are an
     * optional element in the mail template. If absent, each mail server supplies the current
     * user's email address.
     *
     * @see {@link InternetAddress#getLocalAddress(Session)}
     */
    protected void fillFrom(Message email, WorkItem workItem, JCRSessionWrapper session) throws MessagingException {
        try {
            AddressTemplate fromTemplate = getTemplate().getFrom();
            // "from" attribute is optional
            if (fromTemplate == null) return;

            // resolve and parse addresses
            String addresses = fromTemplate.getAddresses();
            if (addresses != null) {
                addresses = evaluateExpression(workItem, addresses, session);
                // non-strict parsing applies to a list of mail addresses entered by a human
                email.addFrom(InternetAddress.parse(addresses, false));
            }

            // resolve and tokenize users
            String userList = fromTemplate.getUsers();
            if (userList != null) {
                String[] userIds = tokenizeActors(userList, workItem, session);
                List<User> users = new ArrayList<User>();
                for (String userId : userIds) {
                    users.add(taskIdentityService.getUserById(userId));
                }
                email.addFrom(getAddresses(users));
            }

            // resolve and tokenize groups
            String groupList = fromTemplate.getGroups();
            if (groupList != null) {
                for (String groupId : tokenizeActors(groupList, workItem, session)) {
                    Group group = taskIdentityService.getGroupById(groupId);
                    email.addFrom(getAddresses(group));
                }
            }
        } catch (ScriptException e) {
            logger.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected void fillRecipients(Message email, WorkItem workItem, JCRSessionWrapper session) throws MessagingException {
        try {
            // to
            AddressTemplate to = getTemplate().getTo();
            if (to != null) {
                fillRecipients(to, email, Message.RecipientType.TO, workItem, session);
            }

            // cc
            AddressTemplate cc = getTemplate().getCc();
            if (cc != null) {
                fillRecipients(cc, email, Message.RecipientType.CC, workItem, session);
            }

            // bcc
            AddressTemplate bcc = getTemplate().getBcc();
            if (bcc != null) {
                fillRecipients(bcc, email, Message.RecipientType.BCC, workItem, session);
            }
        } catch (ScriptException e) {
            logger.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void fillRecipients(AddressTemplate addressTemplate, Message email, Message.RecipientType recipientType, WorkItem workItem, JCRSessionWrapper session) throws Exception {
        // resolve and parse addresses
        String addresses = addressTemplate.getAddresses();
        if (addresses != null) {
            addresses = evaluateExpression(workItem, addresses, session);
            // non-strict parsing applies to a list of mail addresses entered by a human
            email.addRecipients(recipientType, InternetAddress.parse(addresses, false));
        }

        // resolve and tokenize users
        String userList = addressTemplate.getUsers();
        if (userList != null) {
            String[] userIds = tokenizeActors(userList, workItem, session);
            List<User> users = new ArrayList<User>();
            for (String userId : userIds) {
                users.add(taskIdentityService.getUserById(userId));
            }
            email.addRecipients(recipientType, getAddresses(users));
        }

        // resolve and tokenize groups
        String groupList = addressTemplate.getGroups();
        if (groupList != null) {
            for (String groupId : tokenizeActors(groupList, workItem, session)) {
                Group group = taskIdentityService.getGroupById(groupId);
                email.addRecipients(recipientType, getAddresses(group));
            }
        }
    }

    private String[] tokenizeActors(String recipients, WorkItem workItem, JCRSessionWrapper session) throws Exception {
        String[] actors = ACTORS_PATTERN.split(evaluateExpression(workItem, recipients, session));
        if (actors.length == 0) throw new Exception("recipient list is empty: " + recipients);
        return actors;
    }

    /**
     * construct recipient addresses from user entities
     */
    private Address[] getAddresses(List<User> users) {
        int userCount = users.size();
        List<Address> addresses = new ArrayList<Address>();
        for (int i = 0; i < userCount; i++) {
            Address userAddress = null;
            try {
                userAddress = getAddress(users.get(i));
            } catch (UnsupportedEncodingException e) {
                logger.error("Error retrieving email address for user " + users.get(i), e);
            }
            if (userAddress != null) {
                addresses.add(userAddress);
            }
        }
        return addresses.toArray(new Address[addresses.size()]);
    }

    private Address[] getAddresses(Group group) {
        List<Address> addresses = new ArrayList<Address>();
        JahiaGroup jahiaGroup = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(group.getId());
        if (jahiaGroup == null) {
            return new Address[0];
        }
        Set<Principal> recursiveUsers = jahiaGroup.getRecursiveUserMembers();
        for (Principal principal : recursiveUsers) {
            JahiaUser jahiaUser = (JahiaUser) principal;
            Address address = null;
            try {
                address = getAddress(jahiaUser.getProperty("firstName"), jahiaUser.getProperty("lastName"), jahiaUser.getProperty("email"));
            } catch (UnsupportedEncodingException e) {
                logger.error("Error while trying to get email address for user " + jahiaUser, e);
                address = null;
            }
            if (address != null) {
                addresses.add(address);
            }
        }
        return addresses.toArray(new Address[addresses.size()]);
    }

    private Address getAddress(User user) throws UnsupportedEncodingException {
        if (user instanceof JBPMTaskIdentityService.UserImpl) {
            JBPMTaskIdentityService.UserImpl userImpl = (JBPMTaskIdentityService.UserImpl) user;
            return getAddress(userImpl.getGivenName(), userImpl.getFamilyName(), userImpl.getBusinessEmail());
        } else {
            return null;
        }
    }

    private Address getAddress(String firstName, String lastName, String email) throws UnsupportedEncodingException {
        String personal = null;
        if (StringUtils.isEmpty(email)) {
            return null;
        }
        if (StringUtils.isNotEmpty(firstName) && StringUtils.isNotEmpty(lastName)) {
            personal = firstName + " " + lastName;
        } else if (StringUtils.isEmpty(firstName) && StringUtils.isEmpty(lastName)) {
            personal = null;
        } else if (StringUtils.isEmpty(firstName)) {
            personal = lastName;
        } else {
            personal = firstName;
        }
        return new InternetAddress(email, personal, "UTF-8");
    }

    protected void fillSubject(Message email, WorkItem workItem, JCRSessionWrapper session) throws MessagingException {
        String subject = getTemplate().getSubject();
        if (subject != null) {
            try {
                String evaluatedSubject = evaluateExpression(workItem, subject, session).replaceAll("[\r\n]", "");
                email.setSubject(WordUtils.abbreviate(evaluatedSubject, 60, 74, "..."));
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            } catch (ScriptException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    protected void fillContent(Message email, WorkItem workItem, JCRSessionWrapper session) throws MessagingException {
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
                    html = evaluateExpression(workItem, html, session);
                    htmlPart.setContent(html, "text/html; charset=UTF-8");
                    alternatives.addBodyPart(htmlPart);
                }

                // text
                if (text != null) {
                    BodyPart textPart = new MimeBodyPart();
                    text = evaluateExpression(workItem, text, session);
                    textPart.setContent(text, "text/plain; charset=UTF-8");
                    alternatives.addBodyPart(textPart);
                }

                // attachments
                if (!attachmentTemplates.isEmpty()) {
                    addAttachments(workItem, multipart, session);
                }

                email.setContent(multipart);
            } else if (text != null) {
                // unipart
                text = evaluateExpression(workItem, text, session);
                email.setText(text);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (ScriptException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String evaluateExpression(WorkItem workItem, String scriptToExecute, JCRSessionWrapper session)
            throws RepositoryException, ScriptException {
        ScriptContext scriptContext = new SimpleScriptContext();
        if (bindings == null) {
            bindings = getBindings(workItem, session);
        }
        scriptContext.setWriter(new StringWriter());
        scriptContext.setErrorWriter(new StringWriter());
        scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        scriptContext.setBindings(scriptContext.getBindings(ScriptContext.GLOBAL_SCOPE), ScriptContext.GLOBAL_SCOPE);
        scriptEngine.eval(scriptToExecute, scriptContext);
        String error = scriptContext.getErrorWriter().toString();
        if (error.length() > 0) {
            logger.error("Scripting error : " + error);
        }
        return scriptContext.getWriter().toString().trim();
    }

    private Bindings getBindings(WorkItem workItem, JCRSessionWrapper session) throws RepositoryException {
        final Map<String, Object> vars = workItem.getParameters();
        Locale locale = (Locale) vars.get("locale");
        final Bindings bindings = new MyBindings(workItem);
        ResourceBundle resourceBundle = ResourceBundles.get(
                "org.jahia.services.workflow." + vars.get("processName"), locale);
        bindings.put("bundle", resourceBundle);
        // user is the one that initiate the Execution  (WorkflowService.startProcess)
        // currentUser is the one that "moves" the Execution  (JBPMProvider.assignTask)
        JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey((String) vars.get("user"));
        if (vars.containsKey("currentUser")) {
            JahiaUser currentUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey((String) vars.get("currentUser"));
            bindings.put("currentUser", currentUser);
        } else {
            bindings.put("currentUser", jahiaUser);
        }
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

    protected void addAttachments(WorkItem workItem, Multipart multipart, JCRSessionWrapper session)
            throws Exception {
        for (AttachmentTemplate attachmentTemplate : getTemplate().getAttachmentTemplates()) {
            BodyPart attachmentPart = new MimeBodyPart();

            // resolve description
            String description = attachmentTemplate.getDescription();
            if (description != null) {
                attachmentPart.setDescription(evaluateExpression(workItem, description, session));
            }

            // obtain interface to data
            DataHandler dataHandler = createDataHandler(attachmentTemplate, workItem, session);
            attachmentPart.setDataHandler(dataHandler);

            // resolve file name
            String name = attachmentTemplate.getName();
            if (name != null) {
                attachmentPart.setFileName(evaluateExpression(workItem, name, session));
            } else {
                // fall back on data source
                DataSource dataSource = dataHandler.getDataSource();
                if (dataSource instanceof URLDataSource) {
                    name = extractResourceName(((URLDataSource) dataSource).getURL());
                } else {
                    name = dataSource.getName();
                }
                if (name != null) {
                    attachmentPart.setFileName(name);
                }
            }

            multipart.addBodyPart(attachmentPart);
        }
    }

    private DataHandler createDataHandler(AttachmentTemplate attachmentTemplate, WorkItem workItem, JCRSessionWrapper session) throws Exception {
        // evaluate expression
        String expression = attachmentTemplate.getExpression();
        if (expression != null) {
            Object object = evaluateExpression(workItem, expression, session);
            return new DataHandler(object, attachmentTemplate.getMimeType());
        }

        // resolve local file
        String file = attachmentTemplate.getFile();
        if (file != null) {
            File targetFile = new File(evaluateExpression(workItem, file, session));
            if (!targetFile.isFile()) {
                throw new Exception("could not read attachment content, file not found: "
                        + targetFile);
            }
            // set content from file
            return new DataHandler(new FileDataSource(targetFile));
        }

        // resolve external url
        URL targetUrl;
        String url = attachmentTemplate.getUrl();
        if (url != null) {
            url = evaluateExpression(workItem, url, session);
            try {
                targetUrl = new URL(url);
            } catch (MalformedURLException e) {
                throw new Exception("could not read attachment content, malformed url: " + url, e);
            }
        }
        // resolve classpath resource
        else {
            String resource = evaluateExpression(workItem, attachmentTemplate.getResource(), session);
            targetUrl = Thread.currentThread().getContextClassLoader().getResource(resource);
            if (targetUrl == null) {
                throw new Exception("could not read attachment content, resource not found: "
                        + resource);
            }
        }
        // set content from url
        return new DataHandler(targetUrl);
    }

    private static String extractResourceName(URL url) {
        String path = url.getPath();
        if (path == null || path.length() == 0) return null;

        int sepIndex = path.lastIndexOf('/');
        return sepIndex != -1 ? path.substring(sepIndex + 1) : null;
    }

    public class MyBindings extends SimpleBindings {
        private final WorkItem workItem;

        public MyBindings(WorkItem workItem) {
            super();
            this.workItem = workItem;
        }

        @Override
        public boolean containsKey(Object key) {
            return super.containsKey(key) || workItem.getParameter((String) key) != null;
        }

        @Override
        public Object get(Object key) {
            return super.containsKey(key) ? super.get(key) : workItem.getParameter((String) key);
        }

    }


}
