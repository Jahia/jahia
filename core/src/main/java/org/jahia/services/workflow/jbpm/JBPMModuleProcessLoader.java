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

package org.jahia.services.workflow.jbpm;

import org.apache.commons.lang.StringUtils;
import org.drools.compiler.kie.builder.impl.FileKieModule;
import org.drools.compiler.kie.builder.impl.MemoryKieModule;
import org.drools.compiler.kie.builder.impl.ZipKieModule;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.osgi.compiler.OsgiKieModule;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.templates.JahiaModuleAware;
import org.jahia.services.workflow.jbpm.custom.email.AddressTemplate;
import org.jahia.services.workflow.jbpm.custom.email.MailTemplate;
import org.jahia.services.workflow.jbpm.custom.email.MailTemplateRegistry;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Loader that initializes workflow processes defined in modules
 */
public class JBPMModuleProcessLoader implements InitializingBean, DisposableBean, JahiaModuleAware {

    private transient static Logger logger = LoggerFactory.getLogger(JBPMModuleProcessLoader.class);

    private Resource[] processes;
    private Resource[] mailTemplates;
    private MailTemplateRegistry mailTemplateRegistry;
    private JahiaTemplatesPackage module;
    private JBPM6WorkflowProvider jbpm6WorkflowProvider;

    public void setJbpm6WorkflowProvider(JBPM6WorkflowProvider jbpm6WorkflowProvider) {
        this.jbpm6WorkflowProvider = jbpm6WorkflowProvider;
    }

    public void setMailTemplateRegistry(MailTemplateRegistry mailTemplateRegistry) {
        this.mailTemplateRegistry = mailTemplateRegistry;
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
            logger.info("Found {} workflow processes to be deployed.", processes.length);
            ReleaseId moduleReleaseId = new ReleaseIdImpl("org.jahia.modules", module.getName(), module.getVersion().toString());
            OsgiKieModule osgiKieModule = OsgiKieModule.create(module.getBundle().getEntry("META-INF/kmodule.xml"));
            KieModule kieModule = osgiKieModule;
            for (Resource process : processes) {
                long lastModified = process.lastModified();

                boolean needUpdate = true;
                boolean found = false;
                String fileName = process.getFilename();
                if (kieModule instanceof ZipKieModule) {
                    ZipKieModule zipKieModule = (ZipKieModule) kieModule;
                    if (zipKieModule.getFile().lastModified() >= lastModified) {
                        needUpdate = false;
                    }
                } else if (kieModule instanceof FileKieModule) {
                    FileKieModule fileKieModule = (FileKieModule) kieModule;
                    Collection<String> kieModuleFileNames = fileKieModule.getFileNames();
                    for (String kieModuleFileName : kieModuleFileNames) {
                        if (kieModuleFileName.equals(fileName)) {
                            File kieModuleFile = new File(fileKieModule.getFile(), kieModuleFileName);
                            if (kieModuleFile.lastModified() >= lastModified) {
                                needUpdate = false;
                                break;
                            }
                        }
                    }
                } else if (kieModule instanceof MemoryKieModule) {
                    MemoryKieModule memoryKieModule = (MemoryKieModule) kieModule;
                    Collection<String> kieModuleFileNames = memoryKieModule.getFileNames();
                    for (String kieModuleFileName : kieModuleFileNames) {
                        if (kieModuleFileName.equals(fileName)) {
                            org.drools.compiler.compiler.io.File kieModuleFile = memoryKieModule.getMemoryFileSystem().getFile(kieModuleFileName);
                            /* @todo memory KieModule are not supported because there is no way to test the last modification date. Maybe we could use some kind of CRC instead ?
                            if (kieModuleFile.lastModified() >= lastModified) {
                                needUpdate = false;
                                break;
                            }
                            */
                        }
                    }
                }
                if (needUpdate) {
                    if (found) {
                        logger.info("Found workflow process " + fileName + ". Updating...");
                    } else {
                        logger.info("Found new workflow process " + fileName + ". Deploying...");
                    }
                    if (kieModule == null) {
                        kieModule = new ZipKieModule(moduleReleaseId, KieServices.Factory.get().newKieModuleModel(), new File(module.getFilePath()));
                    }
                    jbpm6WorkflowProvider.getKieRepository().addKieModule(kieModule);
                    logger.info("... done");
                } else {
                    logger.info("Found workflow process " + fileName + ". It is up-to-date.");
                }
            }
            logger.info("...workflow processes deployed.");
        }
        if (mailTemplates != null && mailTemplates.length > 0) {
            logger.info("Found {} workflow mail templates to be deployed.", mailTemplates.length);

            List keys = Arrays.asList("from", "to", "cc", "bcc",
                    "from-users", "to-users", "cc-users", "bcc-users",
                    "from-groups", "to-groups", "cc-groups", "bcc-groups",
                    "subject", "text", "html", "language");

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
