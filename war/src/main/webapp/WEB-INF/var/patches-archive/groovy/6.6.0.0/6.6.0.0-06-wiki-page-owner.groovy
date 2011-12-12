import java.util.*

import javax.jcr.*
import javax.jcr.nodetype.NoSuchNodeTypeException
import javax.jcr.query.*

import org.jahia.services.content.*

def log = log;

log.info("Start checking for wiki pages to set owner");

Integer updated = JCRTemplate.getInstance().doExecuteWithSystemSession(null, "live", new JCRCallback<Integer>() {
    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
        int count = 0;
        Set<String> roles = new HashSet<String>();
        roles.add("owner");
        try {
        for (NodeIterator pageIterator = session.getWorkspace().getQueryManager()
                        .createQuery("select * from [jnt:wikiPage] where [jcr:createdBy] is not null",
                                Query.JCR_SQL2).execute().getNodes(); pageIterator.hasNext();) {
            JCRNodeWrapper page = (JCRNodeWrapper) pageIterator.nextNode();
            log.debug("Found {}", page.getPath());
            String owner = page.hasProperty("jcr:createdBy") ? page.getProperty("jcr:createdBy").getString() : null;
            if (owner != null && owner.length() > 0 && !"root".equals(owner)) {
                page.grantRoles("u:" + owner, roles);
                log.info("    granted owner role on wiki page {} to {}", page.getPath(), owner);
                count++;
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
});

log.info("... done setting owner for wiki pages: " + updated + " nodes updated.");