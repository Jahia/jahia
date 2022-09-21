/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.services.transform;

import org.apache.commons.lang3.SystemUtils;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.process.ProcessManager;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.io.File;

/**
 * Factory bean for instantiating and configuring instance of the
 * {@link OfficeManager} that uses local installation of the OpenOffice.
 * 
 * @author Sergiy Shyrkov
 */
public class LocalOfficeManagerFactory extends AbstractFactoryBean<OfficeManager> {

    private LocalOfficeManager.Builder cfg;
    
    private boolean killExistingOfficeProcessOnWindows = true;

    /**
     * Initializes an instance of this class.
     */
    public LocalOfficeManagerFactory() {
        super();
        cfg = LocalOfficeManager.builder();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.beans.factory.config.AbstractFactoryBean#createInstance
     * ()
     */
    @Override
    protected OfficeManager createInstance() throws Exception {
        if (killExistingOfficeProcessOnWindows && SystemUtils.IS_OS_WINDOWS) {
            cfg.existingProcessAction(ExistingProcessAction.KILL);
        }

        return cfg.build();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.beans.factory.config.AbstractFactoryBean#getObjectType
     * ()
     */
    @Override
    public Class<? extends OfficeManager> getObjectType() {
        return OfficeManager.class;
    }

    public void setMaxTasksPerProcess(int maxTasksPerProcess) {
        cfg.maxTasksPerProcess(maxTasksPerProcess);
    }

    public void setOfficeHome(File officeHome) throws NullPointerException, IllegalArgumentException {
        cfg.officeHome(officeHome);
    }

    public void setOfficeHome(String officeHome) throws NullPointerException, IllegalArgumentException {
        cfg.officeHome(officeHome);
    }

    public void setPipeName(String pipeName) throws NullPointerException {
        cfg.pipeNames(pipeName);
    }

    public void setPipeNames(String... pipeNames) throws NullPointerException, IllegalArgumentException {
        cfg.pipeNames(pipeNames);
    }

    public void setPortNumber(int portNumber) {
        cfg.portNumbers(portNumber);
    }

    public void setPortNumbers(int... portNumbers) throws NullPointerException, IllegalArgumentException {
        cfg.portNumbers(portNumbers);
    }

    public void setProcessManager(ProcessManager processManager) throws NullPointerException {
        cfg.processManager(processManager);
    }

    public void setTaskExecutionTimeout(long taskExecutionTimeout) {
        cfg.taskExecutionTimeout(taskExecutionTimeout);
    }

    public void setTaskQueueTimeout(long taskQueueTimeout) {
        cfg.taskQueueTimeout(taskQueueTimeout);
    }

    public void setTemplateProfileDir(File templateProfileDir) throws IllegalArgumentException {
        cfg.templateProfileDir(templateProfileDir);
    }

    public void setKillExistingOfficeProcessOnWindows(boolean killExistingOfficeProcessOnWindows) {
        this.killExistingOfficeProcessOnWindows = killExistingOfficeProcessOnWindows;
    }
}
