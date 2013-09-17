package org.jahia.services.workflow.jbpm;

import org.jbpm.runtime.manager.impl.SimpleRuntimeEnvironment;
import org.jbpm.runtime.manager.impl.factory.LocalTaskServiceFactory;
import org.jbpm.services.task.HumanTaskServiceFactory;
import org.jbpm.shared.services.impl.JbpmLocalTransactionManager;
import org.kie.api.task.TaskService;
import org.kie.internal.runtime.manager.RuntimeEnvironment;

import javax.persistence.EntityManagerFactory;

/**
 * We override this method to be able to provide a different transaction manager
 */
public class JahiaLocalTaskServiceFactory extends LocalTaskServiceFactory {

    private RuntimeEnvironment runtimeEnvironment;

    public JahiaLocalTaskServiceFactory(RuntimeEnvironment runtimeEnvironment) {
        super(runtimeEnvironment);
        this.runtimeEnvironment = runtimeEnvironment;
    }

    @Override
    public TaskService newTaskService() {
        EntityManagerFactory emf = ((SimpleRuntimeEnvironment) runtimeEnvironment).getEmf();
        if (emf != null) {

            TaskService internalTaskService = HumanTaskServiceFactory.newTaskServiceConfigurator()
                    .transactionManager(new JbpmLocalTransactionManager())
                    .entityManagerFactory(emf)
                    .userGroupCallback(runtimeEnvironment.getUserGroupCallback())
                    .getTaskService();

            return internalTaskService;
        } else {
            return null;
        }
    }
}
