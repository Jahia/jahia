/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.workflow.jbpm;

import org.apache.commons.lang.StringUtils;
import org.apache.tika.io.IOUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.workflow.jbpm.custom.email.AddressTemplate;
import org.jahia.services.workflow.jbpm.custom.email.MailTemplate;
import org.jahia.services.workflow.jbpm.custom.email.MailTemplateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * Loader that initializes workflow processes defined in modules
 */
public class JBPMModuleProcessLoader implements InitializingBean, DisposableBean {

    private static final List<String> FIELDS = Arrays.asList("from", "to", "cc", "bcc",
            "from-users", "to-users", "cc-users", "bcc-users",
            "from-groups", "to-groups", "cc-groups", "bcc-groups",
            "subject", "text", "html", "language");

    private transient static Logger logger = LoggerFactory.getLogger(JBPMModuleProcessLoader.class);

    private Resource[] processes;
    private Resource[] mailTemplates;
    private MailTemplateRegistry mailTemplateRegistry;
    private JBPM6WorkflowProvider jbpm6WorkflowProvider;

    public void setJbpm6WorkflowProvider(JBPM6WorkflowProvider jbpm6WorkflowProvider) {
        this.jbpm6WorkflowProvider = jbpm6WorkflowProvider;
    }

    public void setMailTemplateRegistry(MailTemplateRegistry mailTemplateRegistry) {
        this.mailTemplateRegistry = mailTemplateRegistry;
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
//        URL kmoduleURL = module.getBundle().getEntry("META-INF/kmodule.xml");
        if (processes != null && processes.length > 0) {
            logger.info("Found {} workflow processes to be deployed.", processes.length);

            for (Resource process : processes) {
                String fileName = process.getFilename();
                logger.info("Found workflow process " + fileName + ". Updating...");

                jbpm6WorkflowProvider.addResource(process);
                logger.info("... done");
            }
            logger.info("...workflow processes deployed.");
            if (jbpm6WorkflowProvider.isInitialized()) {
                jbpm6WorkflowProvider.recompilePackages();
            }
        }
        if (mailTemplates != null && mailTemplates.length > 0) {
            logger.info("Found {} workflow mail templates to be deployed.", mailTemplates.length);

            for (Resource mailTemplateResource : mailTemplates) {
                MailTemplate mailTemplate = new MailTemplate();
                mailTemplate.setLanguage("velocity");
                mailTemplate.setFrom(new AddressTemplate());
                mailTemplate.setTo(new AddressTemplate());
                mailTemplate.setCc(new AddressTemplate());
                mailTemplate.setBcc(new AddressTemplate());

                int currentField = -1;
                String currentLine;
                StringBuilder buf = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(mailTemplateResource.getInputStream(), "UTF-8"));                
                try {
                    while ((currentLine = reader.readLine()) != null) {
                        if (currentLine.contains(":")) {
                            String prefix = StringUtils.substringBefore(currentLine, ":").toLowerCase();
                            if (FIELDS.contains(prefix)) {
                                setMailTemplateField(mailTemplate, currentField, buf);
                                buf = new StringBuilder();
                                currentField = FIELDS.indexOf(prefix);
                                currentLine = StringUtils.substringAfter(currentLine, ":").trim();
                            }
                        } else {
                            buf.append('\n');
                        }
                        buf.append(currentLine);
                    }
                } finally {
                    IOUtils.closeQuietly(reader);
                }          
                setMailTemplateField(mailTemplate, currentField, buf);
                mailTemplateRegistry.addTemplate(StringUtils.substringBeforeLast(mailTemplateResource.getFilename(), "."), mailTemplate);
            }
        }
    }

    public static void setMailTemplateField(MailTemplate t, int currentField, StringBuilder buf) {
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
        if (!JahiaContextLoaderListener.isRunning()) {
            return;
        }
        
        if (processes != null && processes.length > 0) {
            logger.info("Found {} workflow processes to be undeployed.", processes.length);

            for (Resource process : processes) {
                String fileName = process.getFilename();
                logger.info("Undeploy workflow process " + fileName + ". Updating...");

                jbpm6WorkflowProvider.removeResource(process);
                logger.info("... done");
            }
            logger.info("...workflow processes undeployed.");
            if (JahiaContextLoaderListener.isContextInitialized()) {
                jbpm6WorkflowProvider.recompilePackages();
            }
        }
        if (mailTemplates != null && mailTemplates.length > 0) {
            logger.info("Found {} workflow mail templates to be undeployed.", mailTemplates.length);

            for (Resource mailTemplateResource : mailTemplates) {
                mailTemplateRegistry.removeTemplate(StringUtils.substringBeforeLast(mailTemplateResource.getFilename(), "."));
            }
        }

    }

    public void setProcesses(Resource[] processes) {
        this.processes = processes;
    }

    public void setMailTemplates(Resource[] mailTemplates) {
        this.mailTemplates = mailTemplates;
    }

}
