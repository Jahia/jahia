package org.jahia.services.workflow.jbpm.custom;

import org.jahia.services.content.*;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;

import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Map;

public class CustomLockActivity implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    private String type;

    public void setType(String type) {
        this.type = type;
    }

    public void execute(final ActivityExecution execution) throws Exception {
        final List<String> uuids = (List<String>) execution.getVariable("nodeIds");
        String workspace = (String) execution.getVariable("workspace");
        String userKey = (String) execution.getVariable("user");

        JCRTemplate.getInstance().doExecuteWithSystemSession(userKey,
                workspace, null, new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                for (String id : uuids) {
                    doLock(id, session, "process-" + execution.getProcessInstance().getId());
                }
                return null;
            }
        });
        execution.takeDefaultTransition();
    }

    private void doLock(String id, JCRSessionWrapper session, String key)
            throws RepositoryException {
        JCRNodeWrapper node = session.getNodeByUUID(id);
        if (node.isLockable()) {
            node.lockAndStoreToken(type," " +key+" ");
        }
    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
    }

}
