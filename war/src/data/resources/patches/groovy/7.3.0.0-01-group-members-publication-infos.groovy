import org.jahia.api.Constants

import javax.jcr.*
import javax.jcr.query.*

import org.jahia.services.content.*

def log = log;

log.info("Start looking for missing publication metadata on group members ...")

final java.util.Calendar baseCalendar = new java.util.GregorianCalendar();

private static void fixMemberNode(JCRNodeWrapper memberNode, java.util.Calendar calendar) {
    javax.jcr.Node realNode = memberNode.getRealNode();

    // Do not update publication properties if they are already set
    if (realNode.hasProperty(Constants.PUBLISHED) && realNode.getProperty(Constants.PUBLISHED).getBoolean()
            && realNode.hasProperty(Constants.LASTPUBLISHEDBY) && !"".equals(realNode.getProperty(Constants.LASTPUBLISHEDBY).getString())
            && realNode.hasProperty(Constants.LASTPUBLISHED) && realNode.getProperty(Constants.LASTPUBLISHED).getDate() != null) {
        return;
    }

    realNode.setProperty(Constants.LASTPUBLISHED, calendar);
    realNode.setProperty(Constants.LASTPUBLISHEDBY, "root");
    realNode.setProperty(Constants.PUBLISHED, true);
    realNode.getSession().save();
}

private void fixNodesOfType(String nodetype, java.util.Calendar calendar) {
    JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
            QueryResult qr = session.getWorkspace().getQueryManager().createQuery("select * from [" + nodetype + "]", Query.JCR_SQL2).execute();

            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, "live", null,new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper liveSession) throws RepositoryException {

                    for (JCRNodeIteratorWrapper nodeIt = qr.getNodes(); nodeIt.hasNext();) {
                        JCRNodeWrapper nodeToPublish = (JCRNodeWrapper) nodeIt.next();

                        if (nodeToPublish.getProvider().isDefault()) {
                            log.info("group member to publish: " + nodeToPublish.getPath())

                            fixMemberNode(nodeToPublish, calendar);

                            if (liveSession.nodeExists(nodeToPublish.getPath())) {
                                fixMemberNode(liveSession.getNode(nodeToPublish.getPath()), calendar);
                            }
                        }
                    }
                    return null;
                }
            });
            return null;
        }
    })
}

fixNodesOfType("jnt:members", baseCalendar);
fixNodesOfType("jnt:member", baseCalendar);

log.info("...done processing the group members.")
