import org.jahia.services.content.*

import javax.jcr.*
import javax.jcr.NodeIterator
import javax.jcr.nodetype.NoSuchNodeTypeException
import javax.jcr.query.InvalidQueryException
import javax.jcr.query.Query

def log = log;
log.info("Start patch for jnt:fieldFacet nodes")

JCRCallback callback = new JCRCallback<Integer>() {
    public Integer doInJCR(JCRSessionWrapper session) throws RepositoryException {
        Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:fieldFacet] where ['sort'] = 'true' or ['sort'] = 'false'", Query.JCR_SQL2);
        int count = 0;
        try {
            NodeIterator ni = q.execute().getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper fieldFacet = (JCRNodeWrapper) ni.next();
                log.debug("  processing  {}", fieldFacet.path);
                if (!fieldFacet.hasProperty("sort")) {
                    log.debug("    no sort property found -> skipped");
                    continue;
                }
                String oldValue = fieldFacet.getProperty("sort").getString();
                log.debug("    sort value: {}", oldValue);
                if (oldValue == null) {
                    log.debug("    sort property is null -> skipped");
                    continue;
                }

                try {
                    session.checkout(fieldFacet);
                    String newValue = "true".equals(oldValue) ? "count" : "index";
                    fieldFacet.setProperty("sort", newValue);
                    count++;
                    log.debug("updated ${fieldFacet.path} node, set sort value from ${oldValue} to ${newValue}");
                } catch (RepositoryException e) {
                    log.error(e.getMessage(), e);
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


log.info("---------------------------------------------------");
log.info("  Looking in default workspace");
int defaulCount = JCRTemplate.getInstance().doExecuteWithSystemSession(callback);
log.info("  updated {} jnt:fieldFacet nodes in default workspace", defaulCount);

log.info("---------------------------------------------------");
log.info("Looking in live workspace");
int liveCount = JCRTemplate.getInstance().doExecuteWithSystemSession(null, "live", callback);
log.info("  updated {} jnt:fieldFacet nodes in live workspace", liveCount);
log.info("---------------------------------------------------");


log.info("...done patch for jnt:fieldFacet nodes.");
