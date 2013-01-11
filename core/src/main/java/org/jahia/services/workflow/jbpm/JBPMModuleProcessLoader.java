package org.jahia.services.workflow.jbpm;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.templates.JahiaModuleAware;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.WorklowTypeRegistration;
import org.jahia.utils.FileUtils;
import org.jbpm.api.Deployment;
import org.jbpm.api.NewDeployment;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.RepositoryService;
import org.jbpm.pvm.internal.email.impl.AddressTemplate;
import org.jbpm.pvm.internal.email.impl.MailTemplate;
import org.jbpm.pvm.internal.email.impl.MailTemplateRegistry;
import org.jbpm.pvm.internal.repository.DeploymentImpl;
import org.jbpm.pvm.internal.repository.DeploymentProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loader that initializes workflow processes defined in modules
 */
public class JBPMModuleProcessLoader implements InitializingBean, DisposableBean, JahiaModuleAware {

    private transient static Logger logger = LoggerFactory.getLogger(JBPMModuleProcessLoader.class);

    private ProcessEngine processEngine;
    private Resource[] processes;
    private Resource[] mailTemplates;
    private RepositoryService repositoryService;
    private MailTemplateRegistry mailTemplateRegistry;
    private JahiaTemplatesPackage module;
    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
        repositoryService = processEngine.getRepositoryService();
        mailTemplateRegistry = processEngine.get(MailTemplateRegistry.class);
    }


    @Override
    public void setJahiaModule(JahiaTemplatesPackage module) {
        this.module = module;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        deployDeclaredProcesses();

    }

    @Override
    public void destroy() throws Exception {
        undeployDeclaredProcesses();
    }

    private void deployDeclaredProcesses() throws IOException {
        if (processes != null && processes.length > 0) {
            logger.info("Found " + processes.length + " workflow processes to be deployed.");
            List<Deployment> deploymentList = repositoryService.createDeploymentQuery().list();
            for (Resource process : processes) {
                long lastModified = process.lastModified();

                boolean needUpdate = true;
                boolean found = false;
                String fileName = process.getFilename();
                for (Deployment deployment : deploymentList) {
                    if (deployment.getName().equals(fileName)) {
                        found = true;
                        if (deployment.getTimestamp() >= lastModified) {
                            needUpdate = false;
                            break;
                        }
                    }
                }
                if (needUpdate) {
                    if (found) {
                        logger.info("Found workflow process " + fileName + ". Updating...");
                    } else {
                        logger.info("Found new workflow process " + fileName + ". Deploying...");
                    }
                    NewDeployment newDeployment = repositoryService.createDeployment();
                    newDeployment.addResourceFromInputStream(process.getFilename(), process.getInputStream());
                    newDeployment.setTimestamp(lastModified);
                    newDeployment.setName(fileName);
                    newDeployment.deploy();
//
//                    for (DeploymentProperty property : ((DeploymentImpl) newDeployment).getObjectProperties()) {
//                        if (property.getKey().equals("pdkey")) {
//                            WorkflowService.getInstance().registerWorkflowType(null, property.getStringValue(), module.getRootFolder(), null);
//                        }
//                    }
//
                    logger.info("... done");
                } else {
                    logger.info("Found workflow process " + fileName + ". It is up-to-date.");
                }
            }
            logger.info("...workflow processes deployed.");
        }
        if (mailTemplates != null && mailTemplates.length > 0) {
            logger.info("Found " + processes.length + " workflow mail templates to be deployed.");

            List keys = Arrays.asList("from", "to", "cc", "bcc", "from-users", "to-users", "cc-users", "bcc-users", "from-groups", "to-groups", "cc-groups", "bcc-groups", "subject", "text", "html", "language");

            for (Resource mailTemplateResource : mailTemplates) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(mailTemplateResource.getInputStream(), "UTF-8"));
                MailTemplate mailTemplate = new MailTemplate();
                mailTemplate.setLanguage("velocity");
                mailTemplate.setFrom(new AddressTemplate());
                mailTemplate.setTo(new AddressTemplate());
                mailTemplate.setCc(new AddressTemplate());
                mailTemplate.setBcc(new AddressTemplate());


                int currentField = -1;
                String currentLine;
                StringBuilder buf = new StringBuilder();
                while ((currentLine = reader.readLine()) != null) {
                    if (currentLine.contains(":")) {
                        String prefix = StringUtils.substringBefore(currentLine, ":");
                        if (keys.contains(prefix.toLowerCase())) {
                            setMailTemplateField(mailTemplate, currentField, buf);
                            buf = new StringBuilder();
                            currentField = keys.indexOf(prefix.toLowerCase());
                            currentLine = StringUtils.substringAfter(currentLine, ":").trim();
                        }
                    } else {
                        buf.append('\n');
                    }
                    buf.append(currentLine);
                }
                setMailTemplateField(mailTemplate, currentField, buf);
                mailTemplateRegistry.addTemplate(StringUtils.substringBeforeLast(mailTemplateResource.getFilename(), "."), mailTemplate);
            }
        }

    }

    private void setMailTemplateField(MailTemplate t, int currentField, StringBuilder buf) {
        switch (currentField) {
            case 0:
                t.getFrom().setAddresses(buf.toString());
                break;
            case 1:
                t.getTo().setAddresses(buf.toString());
                break;
            case 2:
                t.getCc().setAddresses(buf.toString());
                break;
            case 3:
                t.getBcc().setAddresses(buf.toString());
                break;
            case 4:
                t.getFrom().setUsers(buf.toString());
                break;
            case 5:
                t.getTo().setUsers(buf.toString());
                break;
            case 6:
                t.getCc().setUsers(buf.toString());
                break;
            case 7:
                t.getBcc().setUsers(buf.toString());
                break;
            case 8:
                t.getFrom().setGroups(buf.toString());
                break;
            case 9:
                t.getTo().setGroups(buf.toString());
                break;
            case 10:
                t.getCc().setGroups(buf.toString());
                break;
            case 11:
                t.getBcc().setGroups(buf.toString());
                break;
            case 12:
                t.setSubject(buf.toString());
                break;
            case 13:
                t.setText(buf.toString());
                break;
            case 14:
                t.setHtml(buf.toString());
                break;
            case 15:
                t.setLanguage(buf.toString());
                break;
        }
    }

    private void undeployDeclaredProcesses() throws IOException {

    }

    public void setProcesses(Resource[] processes) {
        this.processes = processes;
    }

    public void setMailTemplates(Resource[] mailTemplates) {
        this.mailTemplates = mailTemplates;
    }

}
