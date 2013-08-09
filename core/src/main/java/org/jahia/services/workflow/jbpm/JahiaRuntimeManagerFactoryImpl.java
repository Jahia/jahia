package org.jahia.services.workflow.jbpm;

import org.jbpm.runtime.manager.impl.RuntimeManagerFactoryImpl;
import org.kie.internal.runtime.manager.RuntimeEnvironment;
import org.kie.internal.runtime.manager.TaskServiceFactory;

/**
 * We override the parent class to be able to customize the LocalTaskServiceFactory implementation
 */
public class JahiaRuntimeManagerFactoryImpl extends RuntimeManagerFactoryImpl {

    private static JahiaRuntimeManagerFactoryImpl instance = new JahiaRuntimeManagerFactoryImpl();

    public static JahiaRuntimeManagerFactoryImpl getInstance() {
        return instance;
    }

    @Override
    protected TaskServiceFactory getTaskServiceFactory(RuntimeEnvironment environment) {
        return new JahiaLocalTaskServiceFactory(environment);
    }
}
