import org.jahia.services.content.JCRTemplate
import org.jahia.services.content.JCRCallback
import org.jahia.services.content.JCRSessionWrapper
import javax.jcr.RepositoryException
import javax.jcr.query.QueryResult
import javax.jcr.NodeIterator
import org.jahia.services.content.JCRNodeWrapper
import javax.jcr.query.Query
import org.jahia.services.content.JCRObservationManager
import org.apache.jackrabbit.core.state.ItemStateReferenceCache
import org.apache.log4j.Logger
import org.apache.log4j.Level

if (!org.jahia.settings.SettingsBean.getInstance().isProcessingServer()) {
    return;
}

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
        JCRObservationManager.setEventsDisabled(Boolean.TRUE);
        Logger logger = Logger.getLogger(ItemStateReferenceCache.class);
        Level previousLevel = logger.getLevel();
        logger.setLevel(Level.ERROR);

        try {
            removeVersionable(jcrsession, "jnt:component");
        } finally {
            logger.setLevel(previousLevel);
            JCRObservationManager.setEventsDisabled(Boolean.FALSE);
        }

        return null;
    }

    private void removeVersionable(JCRSessionWrapper jcrsession, String type) {
        QueryResult result = jcrsession.getWorkspace().getQueryManager().createQuery("select * from ["+ type +"]", Query.JCR_SQL2).execute();
        NodeIterator ni = result.getNodes();
        while (ni.hasNext()) {
            try {
                JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
                next.getRealNode().addMixin("mix:versionable");
                jcrsession.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        result = jcrsession.getWorkspace().getQueryManager().createQuery("select * from ["+ type +"]", Query.JCR_SQL2).execute();
        ni = result.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            next.getRealNode().removeMixin("mix:versionable");
        }
        jcrsession.save();
    }
});
