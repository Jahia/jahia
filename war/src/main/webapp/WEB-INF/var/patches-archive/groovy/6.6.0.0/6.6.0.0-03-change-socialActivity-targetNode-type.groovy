import org.jahia.services.content.*

import javax.jcr.*
import javax.jcr.nodetype.NoSuchNodeTypeException
import javax.jcr.query.InvalidQueryException
import javax.jcr.query.Query

import org.apache.log4j.Level
import org.apache.log4j.LogManager
import org.apache.log4j.Logger

def log = log;
log.info("Start patch for social activity nodes")

JCRCallback callback = new JCRCallback<Integer>() {
    public Integer doInJCR(JCRSessionWrapper session) throws RepositoryException {
        Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:socialActivity]", Query.JCR_SQL2);
        int count = 0;
        try {
            NodeIterator ni = q.execute().getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
                log.debug("  processing  {}", next.path);
                if (!next.hasProperty("j:targetNode")) {
                    log.debug("    no j:targetNode property found -> skipped");
                    continue;
                }
                String targetUuid = next.getProperty("j:targetNode").getString();
                log.debug("    j:targetNode value: {}", targetUuid);
                if (JCRContentUtils.isNotJcrUuid(targetUuid)) {
                    log.debug("    j:targetNode value is not a valid UUID -> skipped");
                    continue;
                }
                
                try {
                    String path = session.getNodeByIdentifier(targetUuid).getPath();
                    log.debug("    setting j:targetNode to: {}", path)
                    Node parent = next.parent;
                    session.checkout(parent);
                    String newName = next.name + "c";
                    next.copy(parent.path, newName);
                    parent.getNode(newName).setProperty("j:targetNode", path);
                    next.remove();
                    count++;
                    log.debug("updated ${next.path} node, converted j:targetNode value from ${targetUuid} to ${path}");
                } catch (ItemNotFoundException e) {
                    log.debug("    target no longer exists -> skipped");
                }
                if (count > 0 && (count % 1000 == 0)) {
                    session.save();
                }
            }
        } catch (InvalidQueryException e) {
            if (e.getCause() == null || !(e.getCause() instanceof NoSuchNodeTypeException)) {
                log.warn("Unable to execute the query. Cause: " + e.getMessage(), e);
            }
            
        }
        
        if (count > 0) {
            session.save();
        }
        return count;
    }
};

Logger imLogger = LogManager.getLogger("org.apache.jackrabbit.core.ItemManager");
Level oldLevel = imLogger.getLevel();
imLogger.setLevel(Level.ERROR);

try {
log.info("---------------------------------------------------");
log.info("  Looking in default workspace");
int defaulCount = JCRTemplate.getInstance().doExecuteWithSystemSession(callback);
log.info("  updated {} activities in default workspace", defaulCount);

log.info("---------------------------------------------------");
log.info("Looking in live workspace");
int liveCount = JCRTemplate.getInstance().doExecuteWithSystemSession(null, "live", callback);
log.info("  updated {} activities in live workspace", liveCount);
log.info("---------------------------------------------------");


log.info("...done patch for social activity nodes.");
} finally {
imLogger.setLevel(oldLevel);
}