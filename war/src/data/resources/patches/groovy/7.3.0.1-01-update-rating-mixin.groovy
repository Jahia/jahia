import org.jahia.services.content.*

import javax.jcr.RepositoryException
import javax.jcr.query.Query
import org.jahia.services.content.nodetypes.NodeTypeRegistry

def log = log;

if (NodeTypeRegistry.getInstance().hasNodeType("jmix:rating")) {

    def jcrCallback = new JCRCallback<Integer>() {

        @Override
        public Integer doInJCR(JCRSessionWrapper session) throws RepositoryException {
            JCRObservationManager.setAllEventListenersDisabled(true);
            int innerCount = 0;
            try {
                Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jmix:rating]", Query.JCR_SQL2);
                javax.jcr.NodeIterator ni = q.execute().getNodes();
                while (ni.hasNext()) {
                    JCRNodeWrapper node = (JCRNodeWrapper) ni.next();
                    if (node.isNodeType("jmix:liveProperties")) {
                        node.getProperty("j:liveProperties").addValue("j:topRatedRatio");
                        double ratio = node.getProperty("j:sumOfVotes").getDouble()/node.getProperty("j:nbOfVotes").getDouble();
                        node.setProperty("j:topRatedRatio", ratio);
                        innerCount++;
                    }
                }
                session.save();
            } finally {
                JCRObservationManager.setAllEventListenersDisabled(false);
            }
            return innerCount;
        }
    };

    log.info("Update " + JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, "live", null, jcrCallback) + " ratings in live " +
            "workspace");
} else {
    log.info("No jmix:rating type registered. Skipping processing rating nodes");
}