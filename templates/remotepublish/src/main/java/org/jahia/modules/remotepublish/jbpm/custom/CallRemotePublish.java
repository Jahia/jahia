package org.jahia.modules.remotepublish.jbpm.custom;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;

import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Publish custom activity for jBPM workflow
 *
 * Publish the current node
 *
 */
public class CallRemotePublish implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;

    public void execute(ActivityExecution execution) throws Exception {
        String id = (String) execution.getVariable("nodeId");
        String workspace = (String) execution.getVariable("workspace");
        Locale locale = (Locale) execution.getVariable("locale");

        JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(id);
        final Query query = JCRSessionFactory.getInstance().getCurrentUserSession().getWorkspace().getQueryManager().createQuery("select * from [jnt:remotePublication]",Query.JCR_SQL2);
        final QueryResult queryResult = query.execute();
        final NodeIterator nodes = queryResult.getNodes();
        if(nodes.hasNext()) {
            while (nodes.hasNext()) {
                JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodes.nextNode();
                if(node.getPath().startsWith(nodeWrapper.getProperty("node").getNode().getPath())) {
                    execution.take("choose remote publication");
                    return;
                }
            }
        }
        execution.take("no remote publication");        
    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
    }

}