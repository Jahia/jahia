import org.jahia.services.content.JCRCallback
import org.jahia.services.content.JCRNodeWrapper
import org.jahia.services.content.JCRSessionWrapper
import org.jahia.services.content.JCRTemplate

import javax.jcr.ItemNotFoundException
import javax.jcr.NodeIterator
import javax.jcr.RepositoryException

def log = log;

log.info("Start cleaning reference keeper...")

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback() {
    @Override
    Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper refRoot = session.getNode("/referencesKeeper");
        int batchCount = 0;
        int removed = 0;
        int maxBatch = 1000;

        NodeIterator ni = refRoot.getNodes();
        log.info("Found " + ni.getSize() + " entries, start checking")
        while (ni.hasNext()) {
            batchCount++;

            if (batchCount % maxBatch == 0) {
                session.save();
                log.info("Checked "+batchCount + " entries, keep on reading nodes ..")
            }

            javax.jcr.Node refNode = ni.nextNode();
            String refuuid = refNode.getProperty("j:node").getString();

            try {
                JCRNodeWrapper n = session.getNodeByUUID(refuuid);
            } catch (ItemNotFoundException e) {
                refNode.remove();
                removed ++;
            }
        }
        session.save();
        log.info("Checked "+batchCount + " entries, removed " + removed + ".");
        if (batchCount > 5000) {
            log.warn("You still have "+ (batchCount-removed) +" nodes under /referencesKeeper, please consider checking the fine-tuning guide to clean them.");
        }
    }
})

