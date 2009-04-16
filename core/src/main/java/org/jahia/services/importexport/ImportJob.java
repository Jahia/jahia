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