import java.util.*

import javax.jcr.*
import javax.jcr.query.*

import org.jahia.services.content.*

def log = log;

log.info("Start checking for JahiApp module types");

Integer updated = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Integer>() {
    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
        int count = 0;
        for (NodeIterator moduleIterator = session.getWorkspace().getQueryManager()
                        .createQuery("select * from [jnt:virtualsite] where ischildnode('/templateSets')" 
                            + " and [j:siteType] <> 'jahiapp' and (localname() = 'blog' or localname() = 'forum' or localname() = 'newsletter' or localname() = 'wiki')",
                                Query.JCR_SQL2).execute().getNodes(); moduleIterator.hasNext();) {
            JCRNodeWrapper module = (JCRNodeWrapper) moduleIterator.nextNode();
            module.setProperty("j:siteType", "jahiapp");
            log.info("    updating type of module {} to jahiapp", module.getPath());
            count++;
        }
        if (count > 0) {
            session.save();
        }
        return count;
    }
});

log.info("... done changing module type to jahiapp: " + updated + " modules updated.");