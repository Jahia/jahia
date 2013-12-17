package org.jahia.services.workflow.jbpm;

import org.jbpm.runtime.manager.impl.RuntimeManagerFactoryImpl;
import org.jbpm.shared.services.impl.JbpmServicesPersistenceManagerImpl;
import org.kie.internal.runtime.manager.RuntimeEnvironment;
import org.kie.internal.runtime.manager.TaskServiceFactory;

/**
 * We override the parent class to be able to customize the LocalTaskServiceFactory implementation
 */
public class JahiaRuntimeManagerFactoryImpl extends RuntimeManagerFactoryImpl {

    private static JahiaRuntimeManagerFactoryImpl instance = new JahiaRuntimeManagerFactoryImpl();

    private JbpmServicesPersistenceManagerImpl jbpmServicesPersistenceManager;

    public static JahiaRuntimeManagerFactoryImpl getInstance() {
        return instance;
    }

    public void setJbpmServicesPersistenceManager(JbpmServicesPersistenceManagerImpl jbpmServicesPersistenceManager) {
        this.jbpmServicesPersistenceManager = jbpmServicesPersistenceManager;
    }

    @Override
    protected TaskServiceFactory getTaskServiceFactory(org.kie.api.runtime.manager.RuntimeEnvironment environment) {
        return new JahiaLocalTaskServiceFactory(environment, jbpmServicesPersistenceManager);
    }

}
