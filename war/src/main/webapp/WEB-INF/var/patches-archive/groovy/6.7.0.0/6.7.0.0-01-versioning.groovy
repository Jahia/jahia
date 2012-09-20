import org.jahia.services.content.JCRTemplate
import org.jahia.services.content.JCRCallback
import org.jahia.services.content.JCRSessionWrapper
import javax.jcr.RepositoryException
import javax.jcr.query.QueryResult
import javax.jcr.NodeIterator
import org.jahia.services.content.JCRNodeWrapper
import javax.jcr.query.Query
import org.jahia.services.content.JCRObservationManager

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
        JCRObservationManager.setEventsDisabled(Boolean.TRUE);

        try {
            removeVersionable(jcrsession, "jnt:user")
            removeVersionable(jcrsession, "jnt:group")
            removeVersionable(jcrsession, "jnt:virtualsitesFolder")
            removeVersionable(jcrsession, "jnt:virtualsite")
            removeVersionable(jcrsession, "jnt:tag")
            removeVersionable(jcrsession, "jnt:category")
            removeVersionable(jcrsession, "jnt:roles")
            removeVersionable(jcrsession, "jnt:role")
            removeVersionable(jcrsession, "jnt:permission")
            removeVersionable(jcrsession, "jnt:usersFolder")
            removeVersionable(jcrsession, "jnt:groupsFolder")
            removeVersionable(jcrsession, "jnt:componentFolder")
            removeVersionable(jcrsession, "jnt:templateSets")
            removeVersionable(jcrsession, "jnt:portletDefinition");
        } finally {
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

JCRTemplate.getInstance().doExecuteWithSystemSession(null, "live", new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
        JCRObservationManager.setEventsDisabled(Boolean.TRUE);

        try {
            removeLive(jcrsession, "jnt:componentFolder")
            removeLive(jcrsession, "jnt:component")
        } finally {
            JCRObservationManager.setEventsDisabled(Boolean.FALSE);
        }

        return null;
    }

    private void removeLive(JCRSessionWrapper jcrsession, String type) {
        QueryResult result = jcrsession.getWorkspace().getQueryManager().createQuery("select * from ["+ type +"]", Query.JCR_SQL2).execute();
        NodeIterator ni = result.getNodes();
        while (ni.hasNext()) {
            try {
                JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
                next.getRealNode().remove();
                jcrsession.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

});

