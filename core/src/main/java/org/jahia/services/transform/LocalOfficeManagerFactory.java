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
