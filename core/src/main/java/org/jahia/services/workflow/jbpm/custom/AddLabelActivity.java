package org.jahia.services.workflow.jbpm.custom;

import org.jahia.ajax.gwt.helper.VersioningHelper;
import org.jahia.services.content.*;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;

import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Map;

public class AddLabelActivity implements ExternalActivityBehaviour {

    private String label;

    public void setLabel(String label) {
        this.label = label;
    }

    public void execute(ActivityExecution execution) throws Exception {
        final List<String> nodeIds = (List<String>) execution.getVariable("nodeIds");
        String workspace = (String) execution.getVariable("workspace");
        JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspace, new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                for (String id : nodeIds) {
                    JCRNodeWrapper node = session.getNodeByIdentifier(id);
                    JCRVersionService.getInstance().addVersionLabel(node, label + "_at_" + VersioningHelper.formatForLabel(System.currentTimeMillis()));
                }
                return null;
            }
        });

    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
    }

}
