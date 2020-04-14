/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.workflow.jbpm.custom.email;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.tools.generic.DateTool;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.widget.publication.PublicationWorkflow;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.usermanager.*;
import org.jahia.services.workflow.Workflow;
import org.jahia.services.workflow.WorkflowComment;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.jbpm.JBPMTaskIdentityService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.ScriptEngineUtils;
import org.jahia.utils.i18n.Messages;
import org.jahia.utils.i18n.ResourceBundles;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.TaskIdentityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A work item handler to send email. This implementation extends the built-in JBPM 6 implementation by
 * re-introducing the mail template mechanism available in JBPM 4
 *
 * @author rincevent
 * @since JAHIA 6.5
 */
public class JBPMMailProducer {

    private static final Logger logger = LoggerFactory.getLogger(JBPMMailProducer.class);
    private static final Pattern ACTORS_PATTERN = Pattern.compile("(assignableFor\\([^)]+\\))|([^,;\\s]+)");
    protected ScriptEngine scriptEngine;
    protected Bindings bindings;

    protected WorkflowService workflowService;
    protected MailTemplateRegistry mailTemplateRegistry;
    protected TaskIdentityService taskIdentityService;
    protected JahiaUserManagerService userManagerService;
    protected JahiaGroupManagerService groupManagerService;

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setMailTemplateRegistry(MailTemplateRegistry mailTemplateRegistry) {
        this.mailTemplateRegistry = mailTemplateRegistry;
    }

    public void setTaskIdentityService(TaskIdentityService taskIdentityService) {
        this.taskIdentityService = taskIdentityService;
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    public Collection<Message> produce(final WorkItem workItem) {
        if (!ServicesRegistry.getInstance().getMailService().isEnabled()) {
            return Collections.emptyList();
        }
        final Map<String, Object> vars = workItem.getParameters();
        Locale locale = (Locale) vars.get("locale");
        String templateKey = (String) vars.get("templateKey");
        MailTemplate template = null;

        if (templateKey != null) {
            if (locale != null) {
                template = (mailTemplateRegistry.getTemplate(templateKey + "." + locale.toString()));
                if (template == null) {
                    template = (mailTemplateRegistry.getTemplate(templateKey + "." + locale.getLanguage()));
                }
            }
            if (template == null) {
                template = mailTemplateRegistry.getTemplate(templateKey);
            }
        }

        if (template != null) {
            final MailTemplate usedTemplate = template;
            try {
                return JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.EDIT_WORKSPACE, locale, new JCRCallback<Collection<Message>>() {

                    @Override
                    public Collection<Message> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        try {
                            scriptEngine = ScriptEngineUtils.getInstance().getEngineByName(usedTemplate.getLanguage());
                            bindings = null;
                            Message email = instantiateEmail();
                            fillFrom(usedTemplate, email, workItem, session);
                            fillRecipients(usedTemplate, email, workItem, session);
                            fillSubject(usedTemplate, email, workItem, session);
                            fillContent(usedTemplate, email, workItem, session);
                            Address[] addresses = email.getRecipients(Message.RecipientType.TO);
                            if (addresses != null && addresses.length > 0) {
                                return Collections.singleton(email);
                            } else {
                                addresses = email.getRecipients(Message.RecipientType.BCC);
                                if (addresses != null && addresses.length > 0) {
                                    return Collections.singleton(email);
                                }
                                addresses = email.getRecipients(Message.RecipientType.CC);
                                if (addresses != null && addresses.length > 0) {
                                    return Collections.singleton(email);
                                }
                                return Collections.emptyList();
                            }
                        } catch (Exception e) {
                            logger.error("Cannot produce mail", e);
                        }
                        return Collections.emptyList();
                    }
                });
            } catch (RepositoryException e) {
                logger.error("Cannot produce mail", e);
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
    protected void fillFrom(MailTemplate template, Message email, WorkItem workItem, JCRSessionWrapper session) throws Exception {
        AddressTemplate fromTemplate = template.getFrom();
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
                org.kie.api.task.model.Group group = taskIdentityService.getGroupById(groupId);
                email.addFrom(getAddresses(group));
            }
        }
    }

    protected void fillRecipients(MailTemplate template, Message email, WorkItem workItem, JCRSessionWrapper session) throws Exception {
        // to
        AddressTemplate to = template.getTo();
        if (to != null) {
            fillRecipients(to, email, Message.RecipientType.TO, workItem, session);
        }

        // cc
        AddressTemplate cc = template.getCc();
        if (cc != null) {
            fillRecipients(cc, email, Message.RecipientType.CC, workItem, session);
        }

        // bcc
        AddressTemplate bcc = template.getBcc();
        if (bcc != null) {
            fillRecipients(bcc, email, Message.RecipientType.BCC, workItem, session);
        }
    }

    protected void fillRecipients(AddressTemplate addressTemplate, Message email, Message.RecipientType recipientType, WorkItem workItem, JCRSessionWrapper session) throws Exception {

        LinkedHashSet<Address> recipients = new LinkedHashSet<>();

        // resolve and parse addresses
        String addresses = addressTemplate.getAddresses();
        if (addresses != null) {
            addresses = evaluateExpression(workItem, addresses, session);
            // non-strict parsing applies to a list of mail addresses entered by a human
            Collections.addAll(recipients, InternetAddress.parse(addresses, false));
        }

        // resolve and tokenize users
        String userList = addressTemplate.getUsers();
        if (userList != null) {
            String[] userIds = tokenizeActors(userList, workItem, session);
            List<User> users = new ArrayList<User>();
            for (String userId : userIds) {
                if (userId.startsWith("assignableFor(")) {
                    String task = StringUtils.substringBetween(userId, "assignableFor(", ")");
                    WorkflowDefinition definition = workflowService.getWorkflow("jBPM", Long.toString(workItem.getProcessInstanceId()), null).getWorkflowDefinition();
                    List<JahiaPrincipal> principals = workflowService.getAssignedRole(definition, task, Long.toString(workItem.getProcessInstanceId()), session);
                    for (JahiaPrincipal principal : principals) {
                        if (principal instanceof JahiaUser) {
                            if (!UserPreferencesHelper.areEmailNotificationsDisabled((JahiaUser) principal)) {
                                users.add(taskIdentityService.getUserById(((JahiaUser) principal).getUserKey()));
                            }
                        } else if (principal instanceof JahiaGroup) {
                            JCRGroupNode groupNode = groupManagerService.lookupGroupByPath(principal.getLocalPath());
                            if (groupNode != null) {
                                for (JCRUserNode user : groupNode.getRecursiveUserMembers()) {
                                    if (!UserPreferencesHelper.areEmailNotificationsDisabled(user)) {
                                        users.add(taskIdentityService.getUserById(user.getPath()));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    User userById = taskIdentityService.getUserById(userId);
                    if (userById instanceof JBPMTaskIdentityService.UserImpl
                            && !((JBPMTaskIdentityService.UserImpl) userById).areEmailNotificationsDisabled()) {
                        users.add(userById);
                    }
                }
            }
            Collections.addAll(recipients, getAddresses(users));
        }

        // resolve and tokenize groups
        String groupList = addressTemplate.getGroups();
        if (groupList != null) {
            for (String groupId : tokenizeActors(groupList, workItem, session)) {
                org.kie.api.task.model.Group group = taskIdentityService.getGroupById(groupId);
                Collections.addAll(recipients, getAddresses(group));
            }
        }

        email.addRecipients(recipientType, recipients.toArray(new Address[recipients.size()]));
    }

    protected String[] tokenizeActors(String recipients, WorkItem workItem, JCRSessionWrapper session) throws Exception {
        List<String> actors = new ArrayList<String>();

        Matcher m = ACTORS_PATTERN.matcher(recipients);
        while (m.find()) {
            actors.add(m.group());
        }
        return actors.toArray(new String[actors.size()]);
    }

    /**
     * construct recipient addresses from user entities
     */
    protected Address[] getAddresses(List<User> users) {
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

    protected Address[] getAddresses(org.kie.api.task.model.Group group) {
        List<Address> addresses = new ArrayList<Address>();
        JCRGroupNode jahiaGroup = JahiaGroupManagerService.getInstance().lookupGroupByPath(group.getId());
        if (jahiaGroup == null) {
            return new Address[0];
        }
        Set<JCRUserNode> recursiveUsers = jahiaGroup.getRecursiveUserMembers();
        for (JCRUserNode user : recursiveUsers) {
            if (UserPreferencesHelper.areEmailNotificationsDisabled(user)) {
                continue;
            }
            Address address = null;
            try {
                address = getAddress(user.getPropertyAsString("j:firstName"), user.getPropertyAsString("j:lastName"), user.getPropertyAsString("j:email"));
            } catch (UnsupportedEncodingException e) {
                logger.error("Error while trying to get email address for user " + user, e);
                address = null;
            }
            if (address != null) {
                addresses.add(address);
            }
        }
        return addresses.toArray(new Address[addresses.size()]);
    }

    protected Address getAddress(User user) throws UnsupportedEncodingException {
        if (user instanceof JBPMTaskIdentityService.UserImpl) {
            JBPMTaskIdentityService.UserImpl userImpl = (JBPMTaskIdentityService.UserImpl) user;
            return getAddress(userImpl.getGivenName(), userImpl.getFamilyName(), userImpl.getBusinessEmail());
        } else {
            return null;
        }
    }

    protected Address getAddress(String firstName, String lastName, String email) throws UnsupportedEncodingException {
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

    protected void fillSubject(MailTemplate template, Message email, WorkItem workItem, JCRSessionWrapper session) throws Exception {
        String subject = template.getSubject();
        if (subject != null) {
            String evaluatedSubject = evaluateExpression(workItem, subject, session).replaceAll("[\r\n]", "");
            email.setHeader("Subject", WordUtils.abbreviate(evaluatedSubject, 60, 74, "..."));
        }
    }

    protected void fillContent(MailTemplate template, Message email, WorkItem workItem, JCRSessionWrapper session) throws Exception {
        String text = template.getText();
        String html = template.getHtml();
        List<AttachmentTemplate> attachmentTemplates = template.getAttachmentTemplates();

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
                addAttachments(template, workItem, multipart, session);
            }

            email.setContent(multipart);
        } else if (text != null) {
            // unipart
            text = evaluateExpression(workItem, text, session);
            email.setText(text);
        }
    }

    protected String evaluateExpression(WorkItem workItem, String scriptToExecute, JCRSessionWrapper session)
            throws RepositoryException, ScriptException {
        ScriptContext scriptContext = new SimpleScriptContext();
        if (bindings == null) {
            bindings = getBindings(workItem, session);
        }
        addStaticClasses(bindings);
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

    //Add Static Classes whose methods may need to be accessed in the template.
    protected void addStaticClasses(Bindings bindings) {
        if (bindings != null) {
            bindings.put("PrincipalViewHelper", PrincipalViewHelper.class);
        }
    }

    protected Bindings getBindings(WorkItem workItem, JCRSessionWrapper session) throws RepositoryException {
        final Map<String, Object> vars = workItem.getParameters();
        Locale locale = (Locale) vars.get("locale");
        final Bindings bindings = new MyBindings(workItem);
        WorkflowDefinition workflowDefinition = (WorkflowDefinition) vars.get("workflow");
        ResourceBundle resourceBundle = ResourceBundles.get(
                workflowDefinition.getPackageName() + "." + workflowDefinition.getName(), locale);
        bindings.put("bundle", resourceBundle);
        // user is the one that initiate the Execution  (WorkflowService.startProcess)
        // currentUser is the one that "moves" the Execution  (JBPMProvider.assignTask)
        JCRUserNode jahiaUser = userManagerService.lookupUserByPath((String) vars.get("user"));
        if (vars.containsKey("currentUser")) {
            JCRUserNode currentUser = userManagerService.lookupUserByPath((String) vars.get("currentUser"));
            bindings.put("currentUser", currentUser);
        } else {
            bindings.put("currentUser", jahiaUser);
        }
        bindings.put("user", jahiaUser);
        if (jahiaUser != null && !UserPreferencesHelper.areEmailNotificationsDisabled(jahiaUser)) {
            bindings.put("userNotificationEmail", UserPreferencesHelper.getPersonalizedEmailAddress(jahiaUser));
        }

        bindings.put("date", new DateTool());
        bindings.put("submissionDate", Calendar.getInstance());
        bindings.put("locale", locale);
        bindings.put("workspace", vars.get("workspace"));
        bindings.put("workflow", workflowDefinition);

        Workflow workflow = workflowService.getWorkflow(workflowDefinition.getProvider(), String.valueOf(workItem.getProcessInstanceId()), locale);
        if (workflow != null) {
            processPublicationWorkFlow(session, workflowDefinition, workflow, bindings, locale);
            processWorkflowComments(workflow, bindings);
        }

        //Setup server and site related bindings
        String contextPath = Jahia.getContextPath();
        bindings.put("contextPath", contextPath);

        JCRSiteNode siteNode = null;
        List<JCRNodeWrapper> nodes = new LinkedList<>();

        @SuppressWarnings("unchecked") List<String> stringList = (List<String>) vars.get("nodeIds");
        for (String s : stringList.size() > 10 ? stringList.subList(0, 9) : stringList) {
            JCRNodeWrapper nodeByUUID = session.getNodeByUUID(s);
            if (!nodeByUUID.isNodeType("jnt:translation")) {
                //Set the site node on first iteration.
                if (siteNode == null) {
                    siteNode = nodeByUUID.getResolveSite();
                }
                nodes.add(nodeByUUID);
            }
        }
        bindings.put("nodes", nodes);

        if (siteNode != null) {
            final int siteURLPortOverride = SettingsBean.getInstance().getSiteURLPortOverride();
            String servername = "http" + (siteURLPortOverride == 443 ? "s" : "") + "://" + siteNode.getServerName() +
                    ((siteURLPortOverride != 0 && siteURLPortOverride != 80 && siteURLPortOverride != 443) ?
                            ":" + siteURLPortOverride : "");
            bindings.put("site", siteNode);
            bindings.put("servername", servername);
            bindings.put("previewPrefix", String.format("%s%s/cms/render/%s/%s", servername, contextPath, Constants.EDIT_WORKSPACE, locale));
            bindings.put("editPrefix", String.format("%s%s/cms/edit/%s/%s", servername, contextPath, Constants.EDIT_WORKSPACE, locale));
            bindings.put("cmmPrefix", String.format("%s%s/cms/contentmanager/%s/%s", servername, contextPath, siteNode.getSiteKey(), locale));
            bindings.put("renderContext", new RenderContext(null, null, JCRSessionFactory.getInstance().getCurrentUser()));
        }
        return bindings;
    }

    private void processPublicationWorkFlow(JCRSessionWrapper session, WorkflowDefinition workflowDefinition, Workflow workflow, final Bindings bindings, Locale locale) throws RepositoryException {
        if (workflow.getVariables().containsKey("customWorkflowInfo") && workflow.getVariables().get("customWorkflowInfo") instanceof PublicationWorkflow) {
            @SuppressWarnings("unchecked") Map<String, List<Map<String, Object>>> publications = new LinkedHashMap();
            initPublicationsMap(publications, locale);
            @SuppressWarnings("unchecked") PublicationWorkflow publicationWorkflow = (PublicationWorkflow) workflow.getVariables().get("customWorkflowInfo");
            List<GWTJahiaPublicationInfo> publicationInfoList = publicationWorkflow.getPublicationInfos();
            int publicationCount = publicationInfoList.size();
            bindings.put("publicationCount", publicationCount);
            JCRUserNode workflowUser = userManagerService.lookupUserByPath(workflow.getStartUser());
            String workflowUserName = workflowUser != null ? PrincipalViewHelper.getFullName(workflowUser) : "";
            DateTool dateTool = new DateTool();
            String workflowTitle = String.format("%s - %s started by %s on %s - %d content item(s) involved",
                    locale, workflowDefinition.getDisplayName(), workflowUserName, dateTool.format("short_date", workflow.getStartTime(), locale), publicationCount);
            bindings.put("workflowTitle", workflowTitle);
            if (publicationCount > 10) {
                publicationInfoList = publicationInfoList.subList(0, 9);
            }
            for (GWTJahiaPublicationInfo publicationInfo : publicationInfoList) {
                Map<String, Object> node = new HashMap<>();
                node.put("status", publicationInfo.getStatus());
                //For a deleted node populate with information from workflow
                if (publicationInfo.getStatus() == PublicationInfo.DELETED) {
                    node.put("path", publicationInfo.getPath());
                    node.put("type", publicationInfo.getNodetype());
                    String displayableName = publicationInfo.getTitle();
                    if (displayableName.length() > 100) {
                        displayableName = String.format("%s...", displayableName.substring(0, 100));
                    }
                    node.put("displayableName", displayableName);
                    publications.get(getPublicationLabelI18N(publicationInfo.getStatus(), locale)).add(node);
                } else {
                    JCRNodeWrapper nodeByUUID = session.getNodeByUUID(publicationInfo.getUuid());
                    node.put("node", nodeByUUID);
                    node.put("path", publicationInfo.getPath());
                    node.put("type", publicationInfo.getNodetype());
                    String displayableName = nodeByUUID.getDisplayableName();
                    if (displayableName.length() > 100) {
                        displayableName = String.format("%s...", displayableName.substring(0, 100));
                    }
                    node.put("displayableName", displayableName);
                    node.put("displayablePath", publicationInfo.getMainPath());
                    //Place node in the related publication status list
                    publications.get(getPublicationLabelI18N(publicationInfo.getStatus(), locale)).add(node);
                }
            }
            bindings.put("publications", publications);
        }
    }

    private void processWorkflowComments(Workflow workflow, final Bindings bindings) {
        @SuppressWarnings("unchecked") List<WorkflowComment> workflowCommentsList = workflow.getComments();
        if (workflowCommentsList != null) {
            List<Map<String, Object>> comments = new LinkedList<>();
            for (WorkflowComment workflowComment : workflowCommentsList) {
                Map<String, Object> commentInfo = new LinkedHashMap<>();
                commentInfo.put("comment", workflowComment.getComment());
                commentInfo.put("time", workflowComment.getTime());
                JCRUserNode user = userManagerService.lookupUserByPath(workflowComment.getUser());
                if (user != null) {
                    commentInfo.put("userName", PrincipalViewHelper.getFullName(user));
                }
                comments.add(commentInfo);
            }
            bindings.put("comments", comments);
        }
    }

    private void initPublicationsMap(Map<String, List<Map<String, Object>>> publications, Locale locale) {
        //Initialize list entries for each type of publication
        publications.put(getPublicationLabelI18N(PublicationInfo.NOT_PUBLISHED, locale), new LinkedList<>());
        publications.put(getPublicationLabelI18N(PublicationInfo.MODIFIED, locale), new LinkedList<>());
        publications.put(getPublicationLabelI18N(PublicationInfo.PUBLISHED, locale), new LinkedList<>());
        publications.put(getPublicationLabelI18N(PublicationInfo.MARKED_FOR_DELETION, locale), new LinkedList<>());
        publications.put(getPublicationLabelI18N(PublicationInfo.DELETED, locale), new LinkedList<>());
        publications.put(getPublicationLabelI18N(PublicationInfo.UNPUBLISHED, locale), new LinkedList<>());
        publications.put(getPublicationLabelI18N(PublicationInfo.MANDATORY_LANGUAGE_VALID, locale), new LinkedList<>());
        publications.put(getPublicationLabelI18N(PublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE, locale), new LinkedList<>());
        publications.put(getPublicationLabelI18N(PublicationInfo.LIVE_ONLY, locale), new LinkedList<>());
        publications.put(getPublicationLabelI18N(PublicationInfo.LIVE_MODIFIED, locale), new LinkedList<>());
        publications.put(getPublicationLabelI18N(PublicationInfo.CONFLICT, locale), new LinkedList<>());
    }

    private String getPublicationLabelI18N(int status, Locale locale) {
        String publicationResourceKey = String.format("label.publication.%s", PublicationInfo.getLabel(status));
        return Messages.getInternal(publicationResourceKey, locale);
    }

    protected void addAttachments(MailTemplate template, WorkItem workItem, Multipart multipart, JCRSessionWrapper session)
            throws Exception {
        for (AttachmentTemplate attachmentTemplate : template.getAttachmentTemplates()) {
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

    protected DataHandler createDataHandler(AttachmentTemplate attachmentTemplate, WorkItem workItem, JCRSessionWrapper session) throws Exception {
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

        URL targetUrl;
        String url = attachmentTemplate.getUrl();
        if (url != null) {
            // resolve external url
            url = evaluateExpression(workItem, url, session);
            targetUrl = new URL(url);
        } else {
            // resolve classpath resource
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

    protected static String extractResourceName(URL url) {
        String path = url.getPath();
        if (path == null || path.length() == 0) return null;

        int sepIndex = path.lastIndexOf('/');
        return sepIndex != -1 ? path.substring(sepIndex + 1) : null;
    }


    public class MyBindings extends SimpleBindings {
        protected final WorkItem workItem;

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
