/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.workflow.jbpm;

import org.jahia.services.SpringContextSingleton;
import org.jbpm.runtime.manager.impl.SimpleRuntimeEnvironment;
import org.jbpm.runtime.manager.impl.factory.LocalTaskServiceFactory;
import org.jbpm.services.task.HumanTaskServiceFactory;
import org.jbpm.shared.services.api.JbpmServicesPersistenceManager;
import org.jbpm.shared.services.api.JbpmServicesTransactionManager;
import org.jbpm.shared.services.impl.JbpmLocalTransactionManager;
import org.jbpm.shared.services.impl.JbpmServicesPersistenceManagerImpl;
import org.kie.api.task.TaskService;
import org.kie.internal.runtime.manager.RuntimeEnvironment;

import javax.persistence.EntityManagerFactory;

/**
 * We override this method to be able to provide a different transaction manager
 */
public class JahiaLocalTaskServiceFactory extends LocalTaskServiceFactory {

    private org.kie.api.runtime.manager.RuntimeEnvironment runtimeEnvironment;

    private JbpmServicesPersistenceManager jbpmServicesPersistenceManager;

    public JahiaLocalTaskServiceFactory(org.kie.api.runtime.manager.RuntimeEnvironment runtimeEnvironment, JbpmServicesPersistenceManager jbpmServicesPersistenceManager) {
        super(runtimeEnvironment);
        this.runtimeEnvironment = runtimeEnvironment;
        this.jbpmServicesPersistenceManager = jbpmServicesPersistenceManager;
    }

    @Override
    public TaskService newTaskService() {
        EntityManagerFactory emf = ((SimpleRuntimeEnvironment) runtimeEnvironment).getEmf();
        if (emf != null) {

            final JbpmServicesTransactionManager transactionManager = jbpmServicesPersistenceManager.getTransactionManager();
            TaskService internalTaskService = HumanTaskServiceFactory.newTaskServiceConfigurator()
                    .transactionManager(transactionManager)
                    .persistenceManager(jbpmServicesPersistenceManager)
                    .entityManagerFactory(emf)
                    .userGroupCallback(runtimeEnvironment.getUserGroupCallback())
                    .getTaskService();

            return internalTaskService;
        } else {
            return null;
        }
    }
}
