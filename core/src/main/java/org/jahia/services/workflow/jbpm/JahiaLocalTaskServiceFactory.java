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
