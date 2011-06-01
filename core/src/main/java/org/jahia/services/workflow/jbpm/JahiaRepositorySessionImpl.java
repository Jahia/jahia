package org.jahia.services.workflow.jbpm;

import org.hibernate.Query;
import org.jbpm.pvm.internal.query.ProcessDefinitionQueryImpl;
import org.jbpm.pvm.internal.repository.DeploymentImpl;
import org.jbpm.pvm.internal.repository.DeploymentProperty;
import org.jbpm.pvm.internal.repository.RepositorySessionImpl;

/**
 * We have sub-classed jBPM default repository session in order to make the queries cacheable.
 */
public class JahiaRepositorySessionImpl extends RepositorySessionImpl {

    public JahiaRepositorySessionImpl() {

    }

    @Override
    public ProcessDefinitionQueryImpl createProcessDefinitionQuery() {
        return new JahiaProcessDefinitionQueryImpl();
    }

    @Override
    public DeploymentProperty findDeploymentPropertyByProcessDefinitionId(String processDefinitionId) {
        Query query = session.createQuery(
                "select deploymentProperty " +
                        "from " + DeploymentProperty.class.getName() + " as deploymentProperty " +
                        "where deploymentProperty.key = '" + DeploymentImpl.KEY_PROCESS_DEFINITION_ID + "' " +
                        "  and deploymentProperty.stringValue = '" + processDefinitionId + "' "
        );
        query.setCacheable(true);
        query.setMaxResults(1);
        DeploymentProperty deploymentProperty = (DeploymentProperty) query.uniqueResult();
        return deploymentProperty;
    }
}
