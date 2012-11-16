import org.jahia.services.content.*

import javax.jcr.*
import javax.jcr.nodetype.NoSuchNodeTypeException
import javax.jcr.query.InvalidQueryException
import javax.jcr.query.Query

import org.apache.log4j.Level
import org.apache.log4j.LogManager
import org.apache.log4j.Logger

if (!org.jahia.settings.SettingsBean.getInstance().isProcessingServer()) {
    return;
}

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
                if (!next.hasProperty("j:activityType")) {
                    if (!next.hasProperty("j:message")) {
                        next.remove();
                        log.debug("removed ${next.path} node");
                    } else {
                        next.setProperty("j:activityType","text");
                        log.debug("updated ${next.path}");
                    }
                }
                count++;
                log.debug("updated ${next.path} node");
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