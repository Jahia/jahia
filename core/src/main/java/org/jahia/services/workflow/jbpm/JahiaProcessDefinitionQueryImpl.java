package org.jahia.services.workflow.jbpm;

import org.hibernate.Query;
import org.jbpm.pvm.internal.query.ProcessDefinitionQueryImpl;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: 14.03.11
 * Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
public class JahiaProcessDefinitionQueryImpl extends ProcessDefinitionQueryImpl {

    @Override
    protected void applyParameters(Query query) {
        super.applyParameters(query);    //To change body of overridden methods use File | Settings | File Templates.
        query.setCacheable(true);
    }
}
