/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.importexport;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.content.TreeOperationResult;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.lock.LockKey;
import org.jahia.services.mail.GroovyMimeMessagePreparator;
import org.jahia.services.mail.MailService;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.UserProperty;
import org.jahia.services.workflow.AbstractActivationJob;
import org.jahia.services.workflow.PublishAllJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import javax.jcr.Node;

/**
 * Created by IntelliJ IDEA.
 * Date: 25 oct. 2005 - 16:34:07
 *
 * @author toto
 * @version $Id$
 */
public class ImportJob extends BackgroundJob {
    public static final String IMPORT_TYPE = "import";

    public static final String TARGET = "target";
    public static final String CONTENT_TYPE = "contentType";
    public static final String PUBLISH_ALL_AT_END = "publishAllAtEnd";
    public static final String URI = "uri";
    public static final String FILENAME = "filename";
    public static final String DELETE_FILE = "delete";

    public void executeJahiaJob(JobExecutionContext jobExecutionContext, ProcessingContext context) throws Exception {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        String contentType = (String) jobDataMap.get(CONTENT_TYPE);
        ContentObject target = null;
        String key = (String) jobDataMap.get(TARGET);
        if (key != null) {
            target = ContentObject.getContentObjectInstance(ObjectKey.getInstance(key));
        }
        InputStream inputStream = null;

        String uri = (String) jobDataMap.get(URI);
        JCRNodeWrapper f = ServicesRegistry.getInstance().getJCRStoreService().getFileNode(uri, context.getUser());
        inputStream = f.getFileContent().downloadFile();

        List<ImportAction> actions = new ArrayList<ImportAction>();

        ExtendedImportResult result = new ExtendedImportResult();
        ContentObject imported = null;
        if (inputStream != null) {
            try {
                if (contentType.indexOf("zip")>0) {
                    imported = ServicesRegistry.getInstance().getImportExportService().importFile(target, context, inputStream, true, actions, result);
                } else if ("text/xml".equals(contentType) || "application/xml".equals(contentType)) {
                    imported = ServicesRegistry.getInstance().getImportExportService().importDocument(target, context.getLocale().toString(), context, inputStream, false, true, actions, result, null, null, null, null, null);
                }

                if (Boolean.TRUE.equals(jobDataMap.get(PUBLISH_ALL_AT_END)) ) {
                    if (result.getErrors().isEmpty()) {
                        Class jobClass = PublishAllJob.class;
                        JobDetail publishjobDetail = BackgroundJob.createJahiaJob("ActivatingAll", jobClass, context);
                        JobDataMap publishjobDataMap = publishjobDetail.getJobDataMap();
                        publishjobDataMap.put(BackgroundJob.JOB_DESTINATION_SITE, context.getSiteKey());
                        publishjobDataMap.put(BackgroundJob.JOB_TYPE, AbstractActivationJob.WORKFLOW_TYPE);
                        publishjobDataMap.put(AbstractActivationJob.COMMENTS_INPUT, "Auto publish "+uri);
                        final SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();
                        schedulerServ.scheduleJobAtEndOfRequest(publishjobDetail);
                    } else {
                        MailService mailService = ServicesRegistry.getInstance().getMailService();
                        GroovyScriptEngine groovyScriptEngine = (GroovyScriptEngine) SpringContextSingleton.getInstance().getContext().getBean("groovyScriptEngine");
                        GroovyMimeMessagePreparator messageMimePreparator = new GroovyMimeMessagePreparator();
                        messageMimePreparator.setGroovyScriptEngine(groovyScriptEngine);
                        String senderEmail = mailService.defaultSender();

                        JahiaGroup adminGroup = ServicesRegistry.getInstance().getJahiaGroupManagerService()
                                .lookupGroup(context.getSiteID(), JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME);
                        Set members = adminGroup.getRecursiveUserMembers();

                        String recipientEmail = mailService.defaultRecipient();
                        if ( members.iterator().hasNext() ){
                            JahiaUser user = (JahiaUser)members.iterator().next();
                            UserProperty userProperty = user.getUserProperty("email");
                            if (userProperty != null) {
                            String s = userProperty.getValue();
                                if (s != null && s.trim().length()> 0) {
                                    recipientEmail += ";" + s;
                                }
                            }
                        }
                        Binding binding = new Binding();
                        // Bind all necessary variables for groovy script
                        binding.setVariable("processingContext", context);
                        binding.setVariable("from", senderEmail);
                        binding.setVariable("to", recipientEmail);
                        binding.setVariable("locale", context.getLocale());
                        binding.setVariable("results", result);

                        messageMimePreparator.setBinding(binding);
                        messageMimePreparator.setTemplatePath("autoexport_notvalidated.groovy");
                        mailService.sendTemplateMessage(messageMimePreparator);
                    }
                }
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }

        try {
            if (imported != null) {
                LockKey lock = LockKey.composeLockKey(LockKey.IMPORT_ACTION + "_" + imported.getObjectKey().getType(), imported.getID());
                ((Set)jobDataMap.get(JOB_LOCKS)).add(lock);
            }
        } catch (Exception e) {
        }
        if (jobDataMap.get(DELETE_FILE) != null) {
            if (result.getStatus() == TreeOperationResult.COMPLETED_OPERATION_STATUS) {
                Node parent = f.getParent();
                f.deleteFile();
                parent.save();
            }
        }
        jobDataMap.put(ACTIONS, actions);
        jobDataMap.put(RESULT, result);
    }
}
/**
 *$Log $
 */