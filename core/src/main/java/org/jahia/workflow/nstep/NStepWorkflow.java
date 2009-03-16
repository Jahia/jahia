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

package org.jahia.workflow.nstep;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.axis.encoding.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.ContentPageKey;
import org.jahia.content.ObjectKey;
import org.jahia.data.fields.FieldTypes;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.fields.ContentField;
import org.jahia.services.importexport.ProductionJob;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.mail.GroovyMimeMessagePreparator;
import org.jahia.services.mail.MailHelper;
import org.jahia.services.mail.MailService;
import org.jahia.services.notification.templates.WorkflowMessageBuilder;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.EntryStateable;
import org.jahia.services.version.JahiaVersionService;
import org.jahia.services.version.StateModificationContext;
import org.jahia.services.workflow.ExternalWorkflow;
import org.jahia.services.workflow.ExternalWorkflowHistoryEntry;
import org.jahia.services.workflow.ExternalWorkflowInstanceCurrentInfos;
import org.jahia.services.workflow.RecipientInfo;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.utils.JahiaTools;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.workflow.nstep.model.Workflow;
import org.jahia.workflow.nstep.model.WorkflowHistoryEntry;
import org.jahia.workflow.nstep.model.WorkflowInstance;
import org.jahia.workflow.nstep.model.WorkflowStep;
import org.jahia.workflow.nstep.service.WorkflowHistoryManager;
import org.jahia.workflow.nstep.service.WorkflowInstanceManager;
import org.jahia.workflow.nstep.service.WorkflowManager;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * NstepWorkflow is a workflow which allows you to define multiple sequential processes in resource files.
 * These processes can then be applied to your Jahia pages as an alternative to the base 2-step workflow.
 * <p/>
 * Date: 9 déc. 2004
 * Time: 14:53:40
 *
 * @author cedric.mailleux@jahia.com
 * @version $Id$
 *          <p/>
 *          Revision log :
 *          $Log: NStepWorkflow.java,v $
 *          Revision 1.18  2005/12/12 15:25:36  cmailleux
 *          Avoid sending same mail multiple time to same user
 *          <p/>
 *          <p/>
 *          <p/>
 *          Send email only if user have an email (do not check the validity of the email)
 *          <p/>
 *          Revision 1.17  2005/12/12 15:25:18  cmailleux
 *          Avoid sending same mail multiple time to same user
 *          <p/>
 *          Send email only if user have an email (do not check the validity of the email)
 *          <p/>
 *          Revision 1.16  2005/12/12 11:55:37  cmailleux
 *          Avoid ressource excpetion on startstep
 *          <p/>
 *          Revision 1.15  2005/12/12 11:48:18  cmailleux
 *          Only Member of admin group can steal workflow lock
 *          <p/>
 *          Root is allowed to make all step of an NStep Workflow
 *          <p/>
 *          Correctly close lock engine when clicking cancel
 *          <p/>
 *          Revision 1.14  2005/12/07 14:17:08  cmailleux
 *          Change the order of the actions
 *          <p/>
 *          Revision 1.13  2005/11/30 15:04:42  cmailleux
 *          no message
 *          <p/>
 *          Revision 1.12  2005/11/29 14:09:41  cmailleux
 *          Avoid using jParams to get the group
 *          <p/>
 *          Revision 1.11  2005/11/22 18:01:45  cmailleux
 *          Add transactions to nstepWorkflow
 *          Export with files in production
 *          COrrectly flush caches when moving
 *          <p/>
 *          Revision 1.10  2005/10/27 14:44:32  cmailleux
 *          Avoid deleting workflow if validation is unsuccessfull
 *          <p/>
 *          Revision 1.9  2005/10/25 09:07:36  cmailleux
 *          Correct use of trimmed workflow names to avoid removing of workflows just added
 *          <p/>
 *          Revision 1.8  2005/10/20 14:19:55  cmailleux
 *          Correct flush of acl cache
 *          Add option to export site after last step of an nstep workflow
 *          <p/>
 *          Revision 1.7  2005/09/30 08:40:28  cmailleux
 *          Show only rollback to author after first step and show rollback one step and rollback to author after that
 *          <p/>
 *          Add some javadoc
 */
public class NStepWorkflow implements ExternalWorkflow {
// ------------------------------ FIELDS ------------------------------

    private final Log log = LogFactory.getLog(NStepWorkflow.class);
    private WorkflowHistoryManager historyManager = null;
    private WorkflowInstanceManager instanceManager = null;
    private WorkflowManager workflowManager = null;
    private Properties properties = null;
    private String propertiesRelativePath = "/WEB-INF/etc/services/workflow/nstep.properties";
    private static final String ROLLBACK_ACTION_NAME = "rollback_to_previous_step";
    private static final String ROLLBACK_AUTHOR_ACTION_NAME = "rollback_to_author";
    private static final String UNLOCK_ACTION_NAME = "unlock";
    private static final String LOCK_ACTION_NAME = "lock";
// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Constructs a new NStepWorkflow.
     */
    public NStepWorkflow() {
    }

    public void setPropertiesRelativePath(String propertiesRelativePath) {
        this.propertiesRelativePath = propertiesRelativePath;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void start() throws JahiaException {
        /*
        try {
            properties = new Properties();
            properties.load(new FileInputStream(org.jahia.settings.SettingsBean.getInstance().getPathResolver().resolvePath(propertiesRelativePath)));
        } catch (IOException e) {
            log.error("Error during parsing of " + propertiesRelativePath, e);
            throw new JahiaException(e.getLocalizedMessage(), e.getMessage(), JahiaException.CONFIG_ERROR,
                    JahiaException.CRITICAL_SEVERITY);
        }
        */
        initializeWorkflows();
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * Setter for property 'workflowManager'.
     *
     * @param workflowManager Value to set for property 'workflowManager'.
     */
    public void setWorkflowManager(WorkflowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    /**
     * Setter for property 'instanceManager'.
     *
     * @param instanceManager Value to set for property 'instanceManager'.
     */
    public void setInstanceManager(WorkflowInstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }

    /**
     * Setter for property 'historyManager'.
     *
     * @param historyManager Value to set for property 'historyManager'.
     */
    public void setHistoryManager(WorkflowHistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    /**
     * Setter for property 'transactionManager'.
     *
     * @param transactionManager Value to set for property 'transactionManager'.
     */
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        // we do not need transaction manager currently
    }


    /**
     * Get a list of all processes that can be applied to jahia workflow
     *
     * @return A List of process ids
     */
    public List<String> getAvailableProcesses() {
        final List<org.jahia.workflow.nstep.model.Workflow> workflows = workflowManager.getWorkflows();
        final List<String> availableProcesses = new ArrayList<String>(workflows.size());
        for (org.jahia.workflow.nstep.model.Workflow workflow : workflows) {
            availableProcesses.add(workflow.getName());
        }
        //Collections.sort(availableProcesses);
        return availableProcesses;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get the name of the workflow engine, as it will be displayed in jahia engine
     *
     * @param locale current locale
     * @return localized name of the workflow engine
     */
    public String getDisplayName(Locale locale) {
        return localize("workflow.name", locale);
    }

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface ExternalWorkflow ---------------------

    /**
     * Get the name of the process
     *
     * @param locale current locale
     * @return the localized name of the process
     */
    public String getProcessName(String processName, Locale locale) {
        String procName = localize("process." + processName + ".display.name", locale);
        if (procName == null) {
            procName = processName;
        }
        return procName;
    }

    /**
     * Start a new instance of a process. This is called on the first modification of a jahia object.
     *
     * @param processName
     * @param objectKey
     * @param languageCode
     * @param jParams
     * @return null in all case
     */
    public String initProcess(final String processName, final String objectKey, final String languageCode, final ProcessingContext jParams) {
        try {
//            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
//                protected void doInTransactionWithoutResult(TransactionStatus status) {
//                    try {
            org.jahia.workflow.nstep.model.Workflow workflow = workflowManager.getWorkflowByName(processName);
            if (log.isDebugEnabled()) {
                log.debug("Try to init process " + processName + " for object " + objectKey + " in language " + languageCode);
            }
            final JahiaUser user = jParams.getUser();
            instanceManager.initWorkflow(workflow.getId().toString(), user.getUsername(), objectKey, languageCode, user.getUserKey());
            historyManager.saveWorkflowHistory(user.getUsername(), user.getUsername(), processName, "start process", objectKey, languageCode, "start process");
            if (log.isDebugEnabled()) {
                log.debug("Process initialization successfull");
            }
//                    } catch (ObjectRetrievalFailureException e) {
//                        log.warn("Error could not retrieve process " + processName, e);
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            });
        } catch (ObjectRetrievalFailureException e) {
            log.warn("Error could not retrieve process " + processName, e);
        }

        return null;
    }

    /**
     * Check if a process is already started for this object
     *
     * @param processName
     * @param objectKey
     * @param languageCode
     * @return true if process is already started
     */
    public boolean isProcessStarted(String processName, String objectKey, String languageCode) {
        try {
            WorkflowInstance instance = instanceManager.getWorkflowInstanceByObjectKey(objectKey, languageCode);
            if (instance.getWorkflow().getName().equals(processName)) {
                if (log.isDebugEnabled()) {
                    log.debug("Process is started for object " + objectKey + " in language " + languageCode + " with process " + processName);
                }
                return true;
            }
        } catch (ObjectRetrievalFailureException e) {
            if (log.isDebugEnabled()) {
                log.debug("Process is not started " + processName + " for object " + objectKey + " in language " + languageCode);
            }
        }
        return false;
    }

    /**
     * Cancel a process currently running. All running operations should be aborted
     *
     * @param processName
     * @param objectKey
     * @param languageCode
     */
    public void abortProcess(final String processName, final String objectKey, final String languageCode, final ProcessingContext jParams) {
        try {
            WorkflowInstance instance = instanceManager.getWorkflowInstanceByObjectKey(objectKey, languageCode);
            historyManager.saveWorkflowHistory(ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(instance.getAuthorEmail()).getUsername(), jParams.getUser().getUsername(), processName, "abort process", objectKey, languageCode, "abort process");
            instanceManager.abortWorkflow(objectKey, languageCode);
        } catch (ObjectRetrievalFailureException e) {
            log.debug("Object " + objectKey + " not found in database", e);
        }
    }

    /**
     * Get the list of all possible actions used in a process. This is used by
     * jahia for defining roles on each action.
     *
     * @param processName
     * @return Collection of String containing all actions available in
     *         specified workflow
     */
    public List<String> getAllActions(String processName) {
        List<String> roles = null;
        Workflow workflowByName = null;
        try {
            workflowByName = workflowManager.getWorkflowByName(processName);
        } catch (ObjectRetrievalFailureException e) {
            log.warn("Error process " + processName + " not found in database", e);
        }

        if (workflowByName != null) {
            roles = new LinkedList<String>();
            List<WorkflowStep> steps = workflowByName.getSteps();
            for (int i = 1; i < steps.size(); i++) {
                roles.add(steps.get(i).getName());
            }
            if (!roles.isEmpty()) {
                roles.add("rollback_to_previous_step");
                roles.add("rollback_to_author");
                roles.add("unlock");
                roles.add("lock");
            }
        }

        return roles == null || roles.isEmpty() ? Collections.<String>emptyList() : roles;
    }

    public List<String> getAllActionRoles(String processName) {
        List<String> roles = null;
        Workflow workflowByName = null;
        try {
            workflowByName = workflowManager.getWorkflowByName(processName);
        } catch (ObjectRetrievalFailureException e) {
            log.warn("Error process " + processName + " not found in database", e);
        }

        if (workflowByName != null) {
            roles = new LinkedList<String>();
            List<WorkflowStep> steps = workflowByName.getSteps();
            for (int i = 1; i < steps.size(); i++) {
                roles.add(steps.get(i).getName());
                if (i > 1) {
                    roles.add(steps.get(i).getName() + "_unlock");
                }
            }
        }

        return roles == null || roles.isEmpty() ? Collections.<String>emptyList() : roles;
    }

    public boolean hasRightsOnAction(ContentObject contentObjectInstance, String workflowStepName, JahiaUser user) throws JahiaException {
        if (workflowStepName.equals("notify")) {
            return contentObjectInstance.checkWriteAccess(user);
        }
        if (workflowStepName.endsWith("_lock") && contentObjectInstance.checkWriteAccess(user)) {
            return true;
        }
        if (user.isRoot() || user.isAdminMember(contentObjectInstance.getSiteID())) {
            return true;
        }
        return contentObjectInstance.checkAdminAccess(user) || ServicesRegistry.getInstance().getWorkflowService().getRole(contentObjectInstance, workflowStepName, true).isMember(user);
    }

    /**
     * @param processName
     * @param objectKey
     * @param languageCode
     * @param jParams
     * @return Collection of string containing action names
     */
    public Collection<String> getAvailableActions(String processName, String objectKey, String languageCode, ProcessingContext jParams) {
        WorkflowInstance instance = instanceManager.getWorkflowInstanceByObjectKey(objectKey, languageCode);
        final List<WorkflowStep> steps = instance.getWorkflow().getSteps();
        int startIndex = steps.indexOf(instance.getStep()) + 1;
        List<String> list = new ArrayList<String>(3);
        WorkflowStep workflowStep;
        try {
            workflowStep = steps.get(startIndex);
        } catch (Exception e) {
            startIndex = steps.size() - 1;
            workflowStep = steps.get(startIndex);
        }
        boolean lastStep = startIndex == steps.size() - 1;
        final WorkflowService workflowService = ServicesRegistry.getInstance().getWorkflowService();
        try {
            final ContentObject contentObjectInstance = ContentPage.getContentObjectInstance(ContentPageKey.getInstance(objectKey));
            Map<String, Integer> languageStates = workflowService.getLanguagesStates(contentObjectInstance);
            if (!languageStates.containsKey(languageCode)) {
                return Collections.emptyList();
            }
            int languageState = languageStates.get(languageCode).intValue();
            Integer integer = languageStates.get(ContentObject.SHARED_LANGUAGE);
            int sharedLanguageState = 0;
            if (integer != null) sharedLanguageState = integer.intValue();
            if (sharedLanguageState > languageState) languageState = sharedLanguageState;

            if (languageState > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {

                JahiaUser user = jParams.getUser();
                boolean belongsToAdmins = user.isAdminMember(jParams.getSiteID());

                // optimize check by starting with "powerful" users
                boolean hasRightsOnStep;

                hasRightsOnStep = hasRightsOnAction(contentObjectInstance, workflowStep.getName(), user);

                if (hasRightsOnStep) {
                    boolean mandatoryLanguagesCheckPassed = true;
                    // here comes the check on the last step for mandatory
                    // languages in live mode
                    if (lastStep && !belongsToAdmins) {
                        List<SiteLanguageSettings> siteLanguages = jParams.getSite().getLanguageSettings(true);
                        boolean doMandatoryLanguageCheck = true;
                        List<String> mandatoryLanguages = new LinkedList<String>();
                        for (SiteLanguageSettings lang : siteLanguages) {
                            if (lang.isMandatory()) {
                                if (lang.getCode().equals(languageCode)) {
                                    // current language is mandatory --> no
                                    // check for other languages is needed
                                    doMandatoryLanguageCheck = false;
                                    break;
                                }
                                mandatoryLanguages.add(lang.getCode());
                            }
                        }
                        if (doMandatoryLanguageCheck && !mandatoryLanguages.isEmpty()) {
                            // current language is optional; we need to
                            // check if mandatory languages has live entries
                            Set<ContentObjectEntryState> states = contentObjectInstance.getActiveAndStagingEntryStates();
                            Set<String> activeStates = new HashSet<String>();
                            for (EntryStateable entry : states) {
                                if (entry.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                                    activeStates.add(entry.getLanguageCode());
                                }
                            }
                            for (String mandatoryLanguage : mandatoryLanguages) {
                                if (!activeStates.contains(mandatoryLanguage)) {

                                    // if the instance in the mandatory
                                    // language is not on the last step -->
                                    // prevent publishing
                                    if (!isOnLastStep(objectKey, mandatoryLanguage)) {
                                        mandatoryLanguagesCheckPassed = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (mandatoryLanguagesCheckPassed) {
                        list.add(workflowStep.getName());
                    }
                    // avoid rollback on first two step
                    if (startIndex > 2) {
                        // Add rollback to previous step only if we have already
                        // done step one
                        list.add(ROLLBACK_ACTION_NAME);
                    }
                    if (startIndex > 1) {
                        // Add rollback to author (reinit workflow)
                        list.add(ROLLBACK_AUTHOR_ACTION_NAME);
                    }
                }

                // checks:
                // not the first step
                // and (user is a root or admin
                // or user belongs to the allowed group)
                if (startIndex > 1 && (hasRightsOnAction(contentObjectInstance, workflowStep.getName() + "_unlock", user)) && !contentObjectInstance.isMarkedForDelete()) {
                    // Add actions for making content editable
                    list.add(languageState == EntryLoadRequest.WAITING_WORKFLOW_STATE ? UNLOCK_ACTION_NAME : LOCK_ACTION_NAME);
                } else if (languageState != EntryLoadRequest.WAITING_WORKFLOW_STATE && startIndex > 1 && (hasRightsOnAction(contentObjectInstance, workflowStep.getName() + "_lock", user)) && !contentObjectInstance.isMarkedForDelete()) {
                    list.add(LOCK_ACTION_NAME);
                }
            }
        } catch (JahiaException e) {
            log.warn("Error during eval of available actions", e);
        } catch (ClassNotFoundException e) {
            log.warn("Error during eval of available actions", e);
        }
        return list;
    }

    private boolean isOnLastStep(String objectKey, String languageCode) {
        WorkflowInstance instance = instanceManager.getWorkflowInstanceByObjectKey(objectKey, languageCode);
        List<WorkflowStep> steps = instance.getWorkflow().getSteps();
        int startIndex = steps.indexOf(instance.getStep()) + 1;
        return startIndex == steps.size() - 1;
    }

    /**
     * {@inheritDoc}
     */
    public String getActionName(String processName, String actionName,
            Locale locale) {
        String key = "process.step." + actionName;
        String localized = localize(key, locale);
        if (key.equals(localized)
                && (actionName.endsWith("_unlock") || actionName
                        .endsWith("_lock"))) {
            localized = localize("process.step."
                    + (actionName.endsWith("_unlock") ? "unlock" : "lock"),
                    locale);
        }
        return localized;
    }

    /**
     * @param processName
     * @param actionName
     * @param jParams
     * @return a simple string containing an html defining a text area for user input
     */
    public String getParameterForm(String processName, String actionName, ProcessingContext jParams) {
        // We don't need extra information
        String comment = localize("comment.form.label", jParams.getLocale());
        if (comment == null) {
            comment = "comment";
        }
        final StringBuffer s = new StringBuffer(comment + "<textarea class=\"input\" name=\"nstepcomment\" cols=\"80\" rows=\"4\"></textarea>");
        String productionPresence;
        try {
            productionPresence = localize("process." + processName + ".last.step.production.present", jParams.getLocale());
        } catch (MissingResourceException e) {
            productionPresence = Boolean.FALSE.toString();
        }
        if (Boolean.valueOf(productionPresence).booleanValue()) {
            Workflow workflowByName = workflowManager.getWorkflowByName(processName);
            final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
            if ((jParams.getUser().isAdminMember(jParams.getSiteID()) || aclService.getSiteActionPermission(LockPrerequisites.IMMEDIATE_CRON, jParams.getUser(), JahiaBaseACL.READ_RIGHTS, jParams.getSiteID()) > 0) && workflowByName.isLastStep(actionName)) {
                SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();
                try {
                    String[] jobNames = schedulerServ.getJobNames(BackgroundJob.getGroupName(ProductionJob.class));
                    // ensure we only show the site job
                    String jobNamePrefix = ProductionJob.JOB_NAME_PREFIX + jParams.getSiteID();
                    StringBuffer options = new StringBuffer();
                    options.append("<option value=\"none\" selected>").append(localize("workflow.last.step.production.no", jParams.getLocale())).append("</option>");
                    int nbOptions = 0;
                    for (int i = 0; i < jobNames.length; i++) {
                        String jobName = jobNames[i];
                        if (jobName.startsWith(jobNamePrefix)) {
                            nbOptions++;
                            JobDetail jobDetail = schedulerServ.getJobDetail(jobName, BackgroundJob.getGroupName(ProductionJob.class));
                            JobDataMap jobDataMap = jobDetail.getJobDataMap();
                            String targetName = jobDataMap.getString(ProductionJob.TARGET);
                            String username = jobDataMap.getString(ProductionJob.USERNAME);
                            String password = jobDataMap.getString(ProductionJob.PASSWORD);
                            String sitename = jobDataMap.getString(ProductionJob.SITE_NAME);
                            JahiaUser member = ServicesRegistry.getInstance().getJahiaSiteUserManagerService().getMember(jParams.getSiteID(), jobDataMap.getString(ProductionJob.PROFILE));

                            boolean disabled = false;

                            try {
                                URL exportUrl = new URL(targetName + "/engineName/export/site/" + sitename + "/export.xml?exportformat=versions&checklock=test");
                                password = new String(Base64.decode(password));
                                JahiaTools.makeJahiaRequest(exportUrl, member, username, password, 1);
                            } catch (IOException e) {
                                disabled = true;
                            }
                            if (disabled) {
                                options.append("<option disabled=\"disabled\" value=\"").append(jobName).append("\">").append(localize("workflow.last.step.production.job", jParams.getLocale()).replaceAll("\\$\\{jobName\\}", (String) jobDataMap.get(ProductionJob.ALIAS))).append("</option>");
                            } else {
                                options.append("<option value=\"").append(jobName).append("\">").append(localize("workflow.last.step.production.job", jParams.getLocale()).replaceAll("\\$\\{jobName\\}", (String) jobDataMap.get(ProductionJob.ALIAS))).append("</option>");
                            }
                        }
                    }
                    if (nbOptions > 0) {
                        s.append("<p>").append(localize("workflow.last.step.production.label", jParams.getLocale())).append("</p>");
                        s.append("<select name=\"nstepJobNamesToActivate\" multiple size=\"").append(nbOptions < 5 ? nbOptions + 1 : 6).append("\">");
                        s.append(options.toString());
                        s.append("</select>");
                    }
                } catch (JahiaException e) {
                    log.error("Error in getParameterForm !", e);
                }
            }
        }
        s.append("</td></tr></table>");
        return s.toString();
    }

    /**
     * @param processName
     * @param objectKey
     * @param languageCode
     * @param actionName
     * @param jParams
     * @param activationTestResults
     * @param userNotifData
     * @return true if sendAction is successfull false otherwise
     */
    public boolean sendAction(final String processName, final String objectKey, final String languageCode, final String actionName, final ProcessingContext jParams, final ActivationTestResults activationTestResults, final Map<RecipientInfo, Object> userNotifData) {
        try {
//            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
//                protected void doInTransactionWithoutResult(TransactionStatus status) {
//                    try {
            String comment = (String) jParams.getAttribute("nstepcomment");
            if (comment == null) comment = "";
            String[] selectedJobNames = jParams.getParameterValues("nstepJobNamesToActivate");
            boolean isStartStep = false;
            boolean isFinished = false;
            WorkflowInstance instance;
            if (ROLLBACK_ACTION_NAME.equals(actionName)) {
                isStartStep = instanceManager.rollbackWorkflow(jParams.getUser().getUsername(), objectKey, languageCode);
                if (isStartStep) {
                    // Unlock object
                    changeStatus(languageCode, jParams, objectKey, processName, EntryLoadRequest.STAGING_WORKFLOW_STATE, true);
                } else {
                    // Lock object
                    changeStatus(languageCode, jParams, objectKey, processName, EntryLoadRequest.WAITING_WORKFLOW_STATE, true);
                }
                instance = instanceManager.getWorkflowInstanceByObjectKey(objectKey, languageCode);
                historyManager.saveWorkflowHistory(ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(instance.getAuthorEmail()).getUsername(), jParams.getUser().getUsername(), processName, ROLLBACK_ACTION_NAME, objectKey, languageCode, comment);
            } else if (ROLLBACK_AUTHOR_ACTION_NAME.equals(actionName)) {
                while (!isStartStep) {
                    isStartStep = instanceManager.rollbackWorkflow(jParams.getUser().getUsername(), objectKey, languageCode);
                }
                changeStatus(languageCode, jParams, objectKey, processName, EntryLoadRequest.STAGING_WORKFLOW_STATE, true);
                instance = instanceManager.getWorkflowInstanceByObjectKey(objectKey, languageCode);
                historyManager.saveWorkflowHistory(ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(instance.getAuthorEmail()).getUsername(), jParams.getUser().getUsername(), processName, ROLLBACK_AUTHOR_ACTION_NAME, objectKey, languageCode, comment);
            } else if (UNLOCK_ACTION_NAME.equals(actionName)) {
                List<Locale> langs = jParams.getSite().getLanguageSettingsAsLocales(true);
                // we will unlock all languages
                for (Locale locale : langs) {
                    try {
                        instance = instanceManager.getWorkflowInstanceByObjectKey(objectKey, locale.toString());
                    } catch (ObjectRetrievalFailureException ex) {
                        continue;
                    }
                    final List<WorkflowStep> steps = instance.getWorkflow().getSteps();
                    int startIndex = steps.indexOf(instance.getStep()) + 1;
                    // skip if this instance is on the first step
                    if (startIndex == 1) {
                        continue;
                    }
                    changeStatus(locale.toString(), jParams, objectKey, processName, EntryLoadRequest.STAGING_WORKFLOW_STATE, true);
                    historyManager.saveWorkflowHistory(ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(instance.getAuthorEmail()).getUsername(), jParams.getUser().getUsername(), processName, UNLOCK_ACTION_NAME, objectKey, locale.toString(), comment);
                }
                instance = null;
            } else if (LOCK_ACTION_NAME.equals(actionName)) {
                List<Locale> langs = jParams.getSite().getLanguageSettingsAsLocales(true);
                // we will unlock all languages
                for (Locale locale : langs) {
                    try {
                        instance = instanceManager.getWorkflowInstanceByObjectKey(objectKey, locale.toString());
                    } catch (ObjectRetrievalFailureException ex) {
                        continue;
                    }
                    final List<WorkflowStep> steps = instance.getWorkflow().getSteps();
                    int startIndex = steps.indexOf(instance.getStep()) + 1;
                    // skip if this instance is on the first step
                    if (startIndex == 1) {
                        continue;
                    }
                    changeStatus(locale.toString(), jParams, objectKey, processName, EntryLoadRequest.WAITING_WORKFLOW_STATE, true);
                    historyManager.saveWorkflowHistory(ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(instance.getAuthorEmail()).getUsername(), jParams.getUser().getUsername(), processName, LOCK_ACTION_NAME, objectKey, locale.toString(), comment);
                }
                instance = null;
            } else {
                instance = instanceManager.getWorkflowInstanceByObjectKey(objectKey, languageCode);
                List<WorkflowStep> steps = instance.getWorkflow().getSteps();
                int nextStepIndex = steps.indexOf(instance.getStep()) + 1;
                if (steps.get(nextStepIndex).getName().equals(actionName)) {

                    isFinished = instanceManager.stepForwardWorkflow(jParams.getUser().getUsername(), objectKey, languageCode);
                    if (isFinished) {
                        // Publish object
                        ActivationTestResults testResults = finishProcess(languageCode, jParams, objectKey, processName);
                        activationTestResults.merge(testResults);
                        if (testResults.getStatus() != ActivationTestResults.FAILED_OPERATION_STATUS && (selectedJobNames != null && selectedJobNames.length > 0)) {
                            SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();
                            for (int i = 0; i < selectedJobNames.length; i++) {
                                String selectedJobName = selectedJobNames[i];
                                if (!"none".equals(selectedJobName)) {
                                    try {
                                        schedulerServ.triggerJobWithVolatileTrigger(selectedJobName, BackgroundJob.getGroupName(ProductionJob.class));
                                    } catch (JahiaException e) {
                                        log.error("Error during execution of job " + selectedJobName, e);
                                    }
                                }
                            }
                        }
                    } else {
                        changeStatus(languageCode, jParams, objectKey, processName, EntryLoadRequest.WAITING_WORKFLOW_STATE);
                        instance = instanceManager.getWorkflowInstanceByObjectKey(objectKey, languageCode);
                    }
                    historyManager.saveWorkflowHistory(ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(instance.getAuthorEmail()).getUsername(), jParams.getUser().getUsername(), processName, actionName, objectKey, languageCode, comment);
                } else {
                    return false;
                }
            }
            // Send email
            if (instance != null) {
                addActionToMailQueue(jParams, actionName, objectKey, languageCode, comment, isStartStep, isFinished, instance, userNotifData);
            }
        } catch (Exception e) {
            log.warn("Error during workflow of object " + objectKey + " in language " + languageCode + " with process " + processName, e);
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void sendResults(ProcessingContext processingContext, ActivationTestResults activationTestResults, Map<RecipientInfo, Object> userNotifData) {
        final MailService mailService = ServicesRegistry.getInstance().getMailService();
        if (!mailService.isEnabled()) {
            return;
        }
        if (!userNotifData.isEmpty()) {
            for (Map.Entry<RecipientInfo, Object> entry : userNotifData
                    .entrySet()) {
                RecipientInfo recipient = entry.getKey();
                if (entry.getValue() instanceof List) {
                    mailService.sendTemplateMessage(new WorkflowMessageBuilder(
                            recipient.getRecipient(), recipient.getSiteId(),
                            processingContext));
                }
            }
        }
    }

    public boolean isUserAuthorizedForWorkflow(final String processName, final ContentObject object, final JahiaUser user) {
        if (user.isRoot()) return true;
        if (user.isAdminMember(object.getSiteID())) return true;
        if (object.checkAdminAccess(user)) return true;

        final Workflow instance = workflowManager.getWorkflowByName(processName);
        final List<WorkflowStep> steps = instance.getSteps();
        final WorkflowStep workflowStep = steps.get(1);
        try {
            final WorkflowService workflowService = ServicesRegistry.getInstance().getWorkflowService();
            return workflowService.getRole(object, workflowStep.getName(), true).isMember(user);
        } catch (JahiaException e) {
            log.warn("error during eval of user authorization on this workflow", e);
        }
        return false;
    }

    public boolean isUserAuthorizedForAllFollowingSteps(final String processName, final ContentObject object, final JahiaUser user, final Set<String> languageCodes) {
        if (user.isRoot()) return true;
        if (user.isAdminMember(object.getSiteID())) return true;
        if (object.checkAdminAccess(user)) return true;

        boolean result = false;
        for (final String languageCode : languageCodes) {
            WorkflowInstance wfInstance;
            try {
                wfInstance = instanceManager.getWorkflowInstanceByObjectKey(object.getObjectKey().getKey(), languageCode);
            } catch (Exception e) {
                return false;
            }
            final List<WorkflowStep> steps = wfInstance.getWorkflow().getSteps();
            final int nextStepIndex = steps.indexOf(wfInstance.getStep()) + 1;

            for (int i = nextStepIndex; i < steps.size(); i++) {
                final WorkflowStep workflowStep = steps.get(i);
                try {
                    final WorkflowService workflowService = ServicesRegistry.getInstance().getWorkflowService();
                    result = workflowService.getRole(object, workflowStep.getName(), true).isMember(user);
                    if (!result) {
                        if (log.isDebugEnabled()) log.debug("User is not authorized for step: " + i);
                        return false;
                    }
                    if (log.isDebugEnabled()) log.debug("User is authorized for step: " + i);
                } catch (JahiaException e) {
                    log.warn("error during eval of user authorization on this workflow", e);
                }
            }
        }
        return result;
    }

    public boolean isUserAuthorizedForNextStep(final String processName, final ContentObject object, final JahiaUser user, final Set<String> languageCodes) {
        if (user.isRoot()) return true;
        if (user.isAdminMember(object.getSiteID())) return true;
        if (object.checkAdminAccess(user)) return true;

        boolean result = false;
        for (final String languageCode : languageCodes) {
            if (log.isDebugEnabled()) log.debug("isUserAuthorizedForNextStep: " + languageCode + " for " + object);
            WorkflowInstance wfInstance;
            try {
                wfInstance = instanceManager.getWorkflowInstanceByObjectKey(object.getObjectKey().getKey(), languageCode);
            } catch (Exception e) {
                return false;
            }
            final List<WorkflowStep> steps = wfInstance.getWorkflow().getSteps();
            final int nextStepIndex = steps.indexOf(wfInstance.getStep()) + 1;
            final WorkflowStep workflowStep = steps.get(nextStepIndex);
            try {
                final WorkflowService workflowService = ServicesRegistry.getInstance().getWorkflowService();
                result = workflowService.getRole(object, workflowStep.getName(), true).isMember(user);
                if (!result) return false;
            } catch (JahiaException e) {
                log.warn("error during eval of user authorization on this workflow", e);
            }
        }
        return result;
    }

    public ExternalWorkflowInstanceCurrentInfos getCurrentInfo(String processName, String objectKey, String languageCode) {
        WorkflowInstance instance;
        try {
            instance = instanceManager.getWorkflowInstanceByObjectKey(objectKey, languageCode);
        } catch (Exception e) {
            return null;
        }
        final List<WorkflowStep> steps = instance.getWorkflow().getSteps();
        final int nextStepIndex = steps.indexOf(instance.getStep()) + 1;
        final boolean lastStep = nextStepIndex == steps.size() - 1;
        final ExternalWorkflowInstanceCurrentInfos infos = new ExternalWorkflowInstanceCurrentInfos();

        infos.setLastStep(lastStep);
        if (nextStepIndex < steps.size()) {
            final WorkflowStep nextStep = steps.get(nextStepIndex);
            infos.setNextStep(nextStepIndex);
            infos.setNextRole(nextStep.getName());
        }

        final WorkflowStep currentStep = steps.get(nextStepIndex - 1);
        infos.setCurrentStep(nextStepIndex - 1);
        infos.setCurrentRole(currentStep.getName());
        return infos;
    }

    private void changeStatus(String languageCode, ProcessingContext jParams, String objectKey, String processName, int loadRequest) throws Exception {
        changeStatus(languageCode, jParams, objectKey, processName, loadRequest, false);
    }

    private void changeStatus(String languageCode, ProcessingContext jParams, String objectKey, String processName, int loadRequest, boolean bypassValidation) throws Exception {
        final WorkflowService workflowService = ServicesRegistry.getInstance().getWorkflowService();
        Set<String> set = new HashSet<String>(1);
        set.add(languageCode);
        set.add(ContentObject.SHARED_LANGUAGE);
        try {
            final ObjectKey objectKeyInstance = ObjectKey.getInstance(objectKey);
            final StateModificationContext stateModifContext = new StateModificationContext(objectKeyInstance, set);
            stateModifContext.setDescendingInSubPages(true);
            workflowService.changeStagingStatus((ContentObjectKey) objectKeyInstance, set, loadRequest, stateModifContext, jParams, (loadRequest == EntryLoadRequest.STAGING_WORKFLOW_STATE), bypassValidation);
        } catch (Exception e) {
            log.warn("Error during workflow of object " + objectKey + " in language " + languageCode + " with process " + processName, e);
            instanceManager.rollbackWorkflow(jParams.getUser().getUsername(), objectKey, languageCode);
            throw e;
        }
    }

    private ActivationTestResults finishProcess(String languageCode, ProcessingContext jParams, String objectKey, String processName) throws Exception {
        final WorkflowService workflowService = ServicesRegistry.getInstance().getWorkflowService();
        Set<String> set = new HashSet<String>(1);
        set.add(languageCode);
        set.add(ContentObject.SHARED_LANGUAGE);
        ActivationTestResults testResults = null;
        try {
            final ObjectKey objectKeyInstance = ObjectKey.getInstance(objectKey);
            final StateModificationContext stateModifContext = new StateModificationContext(objectKeyInstance, set);
            stateModifContext.setDescendingInSubPages(false);
            final ContentObject contentObjectInstance = ContentObject.getContentObjectInstance(objectKeyInstance);
            final JahiaVersionService jahiaVersionService = ServicesRegistry.getInstance().getJahiaVersionService();
            if (jParams.getEntryLoadRequest().isWithMarkedForDeletion()) {
                testResults = workflowService.activate(contentObjectInstance, set, jahiaVersionService.getSiteSaveVersion(jParams.getSiteID()), jParams, stateModifContext);
            } else {
                testResults = workflowService.activate(contentObjectInstance, set, jahiaVersionService.getSiteSaveVersion(jParams.getSiteID()), jParams, stateModifContext);
            }
            if (testResults.getStatus() == ActivationTestResults.COMPLETED_OPERATION_STATUS) {
                instanceManager.abortWorkflow(objectKey, languageCode);
            } else {
                instanceManager.rollbackWorkflow(jParams.getUser().getUsername(), objectKey, languageCode);
            }
        } catch (Exception e) {
            log.warn("Error during finish of workflow for object " + objectKey + " in language " + languageCode + " with process " + processName, e);
            instanceManager.rollbackWorkflow(jParams.getUser().getUsername(), objectKey, languageCode);
            throw e;
        }
        return testResults;
    }

    private MailInfo getKey(final String key, WorkflowInstance workflow, ProcessingContext jParams, final String displayName, String comment, String authorName, WorkflowStep curStep, WorkflowStep nextStep, ContentObject contentObject, Locale locale, boolean isStartStep, boolean isFinished, boolean isDeleted, boolean isAdvanced, boolean isRollbacked) {

        String processName = workflow.getWorkflow().getName();
        int pageID = 0;
        if (contentObject instanceof ContentPage) {
            pageID = contentObject.getID();
        } else if (contentObject instanceof ContentContainer) {
            pageID = ((ContentContainer) contentObject).getPageID();
        } else if (contentObject instanceof ContentContainerList) {
            pageID = ((ContentContainerList) contentObject).getPageID();
        } else if (contentObject instanceof ContentField) {
            ContentField contentField = (ContentField) contentObject;
            if (contentField.getType() != FieldTypes.PAGE) {
                pageID = contentField.getPageID();
            }
        }
        String pageLink = "";
        String pageTitle = null;
        if (pageID > 0) {
            String useLanguage = null;
            try {
                useLanguage = jParams.getSite().getLanguageSettings(true).size() > 1 ? locale.toString() : null;
            } catch (JahiaException e) {
                log.warn("Unable to retrieve active languages for site with key '" + jParams.getSiteKey() + "'", e);
            }
            pageLink = jParams.getSiteURL(jParams.getSite(), pageID, false, isFinished ? ProcessingContext.NORMAL : ProcessingContext.EDIT, useLanguage, true);
            try {
                ContentPage page = ContentPage.getPage(pageID, false, true);
                if (page != null) {
                    pageTitle = (String) page.getTitles(true).get(locale.toString());
                }
            } catch (JahiaException e) {
                log.warn("Unable to retrieve page title for page '" + pageID, e);
            }
        }

        String currentStepLabel = curStep != null ? "process." + processName + ".step." + curStep.getName() : "";

        String nextStepLabel = nextStep != null ? "process." + processName + ".step." + nextStep.getName() : "";

        return new MailInfo(key, processName, jParams.getUser().getUsername(), displayName, comment, authorName, locale, pageID, pageLink, currentStepLabel, nextStepLabel, isStartStep, isFinished, isDeleted, isAdvanced, isRollbacked, pageTitle);
    }

    private void initializeWorkflows() throws JahiaException {
        String workflowNamesList = properties.getProperty("workflowsName", "");
        String[] workflowNamesArray = workflowNamesList.split(",");
        List<String> workflowNames = new ArrayList<String>(workflowNamesArray.length);
        for (int i = 0; i < workflowNamesArray.length; i++) {
            String workflowName = workflowNamesArray[i].trim();
            workflowNames.add(workflowName);
            org.jahia.workflow.nstep.model.Workflow workflow;
            try {
                workflow = workflowManager.getWorkflowByName(workflowName);
            } catch (ObjectRetrievalFailureException e) {
                workflow = new org.jahia.workflow.nstep.model.Workflow();
                workflow.setName(workflowName);
            }
            String stepNamesList = properties.getProperty(workflowName + ".steps", "");
            if ("".equalsIgnoreCase(stepNamesList.trim())) {
                final String errorMsg = "Error during parsing of " + propertiesRelativePath + " missing " + workflowName + ".steps property, or property is empty";
                throw new JahiaException(errorMsg, errorMsg, JahiaException.CONFIG_ERROR, JahiaException.CRITICAL_SEVERITY);
            }
            final String[] stepNamesArray = stepNamesList.split(",");
            List<WorkflowStep> steps = new ArrayList<WorkflowStep>(stepNamesArray.length);
            List<WorkflowStep> existingSteps = workflow.getSteps();
            if (existingSteps == null || existingSteps.size() == 0) {
                org.jahia.workflow.nstep.model.WorkflowStep init = new org.jahia.workflow.nstep.model.WorkflowStep();
                init.setName("InitStep");
                steps.add(init);
                for (int j = 0; j < stepNamesArray.length; j++) {
                    String stepName = stepNamesArray[j].trim();
                    org.jahia.workflow.nstep.model.WorkflowStep step = new WorkflowStep();
                    step.setName(stepName);
                    steps.add(step);
                }
            } else {
                steps.add(existingSteps.get(0));
                for (int j = 0; j < stepNamesArray.length; j++) {
                    String s1 = stepNamesArray[j].trim();
                    WorkflowStep workflowStep;
                    try {
                        workflowStep = existingSteps.get(j + 1);
                        workflowStep.setName(s1);
                    } catch (IndexOutOfBoundsException e) {
                        workflowStep = new org.jahia.workflow.nstep.model.WorkflowStep();
                        workflowStep.setName(s1);
                    }
                    steps.add(workflowStep);
                }
            }
            workflow.setSteps(steps);
            workflowManager.saveWorkflow(workflow);
        }
        List<org.jahia.workflow.nstep.model.Workflow> workflowList = workflowManager.getWorkflows();
        if (workflowNames.size() != workflowList.size()) {
            for (int i = 0; i < workflowList.size(); i++) {
                org.jahia.workflow.nstep.model.Workflow workflow = workflowList.get(i);
                if (!workflowNames.contains(workflow.getName())) {
                    workflowManager.removeWorkflow(workflow.getId().toString());
                }
            }
        }
    }

    public List<ExternalWorkflowHistoryEntry> getWorkflowHistoryByObject(String objectKey) {
        List<ExternalWorkflowHistoryEntry> results = new ArrayList<ExternalWorkflowHistoryEntry>();
        List<WorkflowHistoryEntry> l = historyManager.getWorkflowHistoryByObject(objectKey);
        for (WorkflowHistoryEntry workflowHistory : l) {
            results.add(new ExternalWorkflowHistoryEntry(workflowHistory.getDate(), workflowHistory.getAction(), workflowHistory.getUser(), workflowHistory.getComment(), workflowHistory.getLanguageCode()));
        }
        Collections.sort(results, new Comparator<ExternalWorkflowHistoryEntry>() {
            public int compare(ExternalWorkflowHistoryEntry workflowHistoryEntry, ExternalWorkflowHistoryEntry workflowHistoryEntry1) {
                return workflowHistoryEntry.getDate().compareTo(workflowHistoryEntry1.getDate());
            }
        });
        return results;
    }


    private void addActionToMailQueue(ProcessingContext jParams, String actionName, String objectKey, String languageCode, String comment, boolean startStep, boolean finished, WorkflowInstance workflow, Map<RecipientInfo, Object> userNotifData) throws JahiaException, ClassNotFoundException {
        if (!ServicesRegistry.getInstance().getMailService().isEnabled()) {
            return;
        }
//        ResourceBundle bundle = ResourceBundle.getBundle("nstep-workflowResources", LanguageCodeConverters.languageCodeToLocale(languageCode));
        Locale locale = LanguageCodeConverters.languageCodeToLocale(languageCode);
        final WorkflowService workflowService = ServicesRegistry.getInstance().getWorkflowService();
        org.jahia.workflow.nstep.model.WorkflowStep nextStep = null;
        WorkflowStep curStep;
        curStep = workflow.getStep();
        if (!finished) {
            nextStep = workflow.getWorkflow().getSteps().get(workflow.getWorkflow().getSteps().indexOf(curStep) + 1);
        }
        final ContentObject contentObjectInstance = ContentObject.getContentObjectInstance(ObjectKey.getInstance(objectKey));
        MailInfo mailBody;

        if (jParams.getContentPage() == null && jParams.getPage() != null) {
            jParams.setContentPage(jParams.getPage().getContentPage());
        }

// Is workflow over ?
        final String displayName = contentObjectInstance.getDisplayName(jParams);
        final JahiaUser author = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(workflow.getAuthorEmail());
        final String username = author.getUsername();
        String processName = workflow.getWorkflow().getName();
        String email = MailHelper.getEmailAddress(author);
        email = !MailHelper.areEmailNotificationsDisabled(author) ? email : null;
        if (startStep) {
            // Send an email to the author that the modification have been refused
            if (email != null) {
                mailBody = getKey("process." + processName + ".mail.body.rollback.to.author", workflow, jParams, displayName, comment, username, nextStep, null, contentObjectInstance, locale, true, false, false, false, false);
                addMailInfoToQueue(new RecipientInfo(author,jParams.getSiteID()), mailBody, userNotifData);
            }
        } else {
            JahiaGroup group;
            if (actionName.equals(ROLLBACK_ACTION_NAME)) {
                mailBody = getKey("process." + processName + ".mail.body.rollback", workflow, jParams, displayName, comment, username, nextStep, null, contentObjectInstance, locale, false, false, false, false, true);
// Send mail to group who refuse to validate
                group = workflowService.getRoleGroup(contentObjectInstance, nextStep.getName(), true);
                sendMails(group, mailBody, jParams, userNotifData, contentObjectInstance);
            } else if (finished) {
                if (contentObjectInstance.hasActiveEntries()) {
                    mailBody = getKey("process." + processName + ".mail.body.finish.published", workflow, jParams, displayName, comment, username, curStep, nextStep, contentObjectInstance, locale, false, true, false, false, false);
                } else {
                    mailBody = getKey("process." + processName + ".mail.body.finish.deleted", workflow, jParams, displayName, comment, username, curStep, nextStep, contentObjectInstance, locale, false, false, true, false, false);
                }
                List<WorkflowStep> steps = workflow.getWorkflow().getSteps();
                for (int i = 0; i < steps.size(); i++) {
                    org.jahia.workflow.nstep.model.WorkflowStep step = steps.get(i);
                    group = workflowService.getRoleGroup(contentObjectInstance, step.getName(), true);
                    sendMails(group, mailBody, jParams, userNotifData, contentObjectInstance);
                }
            } else {
                mailBody = getKey("process." + processName + ".mail.body.step.advance", workflow, jParams, displayName, comment, username, curStep, nextStep, contentObjectInstance, locale, false, false, false, true, false);
                group = workflowService.getRoleGroup(contentObjectInstance, nextStep.getName(), true);
                sendMails(group, mailBody, jParams, userNotifData, contentObjectInstance);
            }
            if (email != null) {
                addMailInfoToQueue(new RecipientInfo(author, jParams.getSiteID()), mailBody, userNotifData);
            }
        }
    }

    private void addMailInfoToQueue(RecipientInfo recipient, MailInfo mailBody, Map<RecipientInfo, Object> userNotifData) {
        List<MailInfo> collection = (List<MailInfo>)userNotifData.get(recipient);
        if (collection == null) {
            collection = new ArrayList<MailInfo>(32);
            userNotifData.put(recipient, (List<MailInfo>)collection);
        }
        if (!collection.contains(mailBody)) collection.add(mailBody);
    }

    private void sendMails(JahiaGroup group, MailInfo mailBody, ProcessingContext processingContext, Map<RecipientInfo, Object> userNotifData, ContentObject contentObjectInstance) {
        final MailService mailService = ServicesRegistry.getInstance().getMailService();
        if (!mailService.isEnabled()) {
            return;
        }
        Set<?> users = group.getRecursiveUserMembers();
        if (users.size() == 0) {
            return;
        }

        if (users.size() > processingContext.settings().getWorkflowMaxNotificationEmails()) {
            GroovyScriptEngine groovyScriptEngine = (GroovyScriptEngine) SpringContextSingleton.getInstance().getContext().getBean("groovyScriptEngine");
            GroovyMimeMessagePreparator adminMessageMimePreparator = new GroovyMimeMessagePreparator();
            adminMessageMimePreparator.setGroovyScriptEngine(groovyScriptEngine);
            Binding binding = new Binding();
            binding.setVariable("processingContext", processingContext);
            binding.setVariable("from", mailService.defaultSender());
            binding.setVariable("to", mailService.defaultRecipient());
            binding.setVariable("limit", Integer.toString(processingContext.settings().getWorkflowMaxNotificationEmails()));
            binding.setVariable("originalSubject", "N-Step workflow notification");
            binding.setVariable("targetObjects", "");

            adminMessageMimePreparator.setBinding(binding);
            adminMessageMimePreparator.setTemplatePath(processingContext.settings().getWorkflowMaxNotificationReachedTemplate());
            mailService.sendTemplateMessage(adminMessageMimePreparator);
        }

        Iterator<?> iterator = users.iterator();
        int userCount = 0;
        while (iterator.hasNext() && (userCount < processingContext.settings().getWorkflowMaxNotificationEmails())) {
            JahiaUser user = (JahiaUser) iterator.next();
            String email = MailHelper.getEmailAddress(user);
            if (email != null && !MailHelper.areEmailNotificationsDisabled(user) && contentObjectInstance.checkReadAccess(user)) {
                addMailInfoToQueue(new RecipientInfo(user, processingContext.getSiteID()), mailBody, userNotifData);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.jahia.services.workflow.ExternalWorkflow#needToRestartProcess(java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean needToRestartProcess(String processName, String objectKey, String languageCode) {

        // we return false in order to prevent updated object to go back to the first workflow step.
        return false;
    }

    public String localize(String messageKey, Locale locale) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(
                "nstep-workflowResources", locale);
        String label = messageKey;
        if (resourceBundle != null) {
            try {
                label = resourceBundle.getString(messageKey);
            } catch (MissingResourceException e) {
                // use messageKey as the default value
            }
        }
        return label;
    }

    public static boolean isValidationNeededForAction(String action) {
        return null == null || !action.endsWith(NStepWorkflow.ROLLBACK_ACTION_NAME) && !action.endsWith(NStepWorkflow.ROLLBACK_AUTHOR_ACTION_NAME) && !action.endsWith(NStepWorkflow.UNLOCK_ACTION_NAME) && !action.endsWith(NStepWorkflow.LOCK_ACTION_NAME);
    }
}
