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
log.info("Start patch for task nodes")

JCRCallback callback = new JCRCallback<Integer>() {
    public Integer doInJCR(JCRSessionWrapper session) throws RepositoryException {
        Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:task]", Query.JCR_SQL2);
        int count = 0;
        try {
            NodeIterator ni = q.execute().getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
                log.debug("  processing  {}", next.path);
                if (!next.hasProperty("assignee")) {
                    log.debug("    no assignee property found -> skipped");
                    continue;
                }
                String assigneeUuid = next.getProperty("assignee").getString();
                log.debug("    assignee value: {}", assigneeUuid);
                if (JCRContentUtils.isNotJcrUuid(assigneeUuid)) {
                    log.debug("    assignee value is not a valid UUID -> skipped");
                    continue;
                }

                try {
                    String assigneeUserKey = session.getNodeByIdentifier(assigneeUuid).getName();
                    log.debug("    setting assigneeUserKey to: {}", assigneeUserKey)
                    next.setProperty("assigneeUserKey", assigneeUserKey);
                    count++;
                    log.debug("updated ${next.path} node, set assigneeUserKey value to ${assigneeUserKey} based on assignee ${assigneeUuid}");
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
    log.info("  updated {} tasks in default workspace", defaulCount);

    log.info("---------------------------------------------------");
    log.info("Looking in live workspace");
    int liveCount = JCRTemplate.getInstance().doExecuteWithSystemSession(null, "live", callback);
    log.info("  updated {} tasks in live workspace", liveCount);
    log.info("---------------------------------------------------");


    log.info("...done patch for task nodes.");
} finally {
    imLogger.setLevel(oldLevel);
}