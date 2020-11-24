import org.jahia.services.content.*;

import javax.jcr.RepositoryException
import javax.jcr.observation.Event
import javax.jcr.observation.EventListenerIterator
import javax.jcr.observation.ObservationManager
import javax.jcr.query.Query

def log = log;



def jcrCallback = new JCRCallback<Integer>() {

    @Override
    public Integer doInJCR(JCRSessionWrapper session) throws RepositoryException {
        ObservationManager observationManager = session.getWorkspace().getObservationManager();
        Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:ace]", Query.JCR_SQL2);
        javax.jcr.NodeIterator ni = q.execute().getNodes();
        EventListenerIterator it = observationManager.getRegisteredEventListeners();
        while (it.hasNext()) {
            def eventConsumer = it.next().listener;
            if (eventConsumer.class.name.contains("AclListener")) {
                while (ni.hasNext()) {
                    JCRNodeWrapper node = (JCRNodeWrapper) ni.next();
                    def event = Arrays.asList(new Event() {

                        @Override
                        int getType() {
                            return Event.PROPERTY_ADDED;
                        }

                        @Override
                        String getPath() throws RepositoryException {
                            return node.path;
                        }

                        @Override
                        String getUserID() {
                            return "root";
                        }

                        @Override
                        String getIdentifier() throws RepositoryException {
                            return node.identifier;
                        }

                        @Override
                        Map getInfo() throws RepositoryException {
                            // ${TODO} Auto-generated method stub
                            return null
                        }

                        @Override
                        String getUserData() throws RepositoryException {
                            // ${TODO} Auto-generated method stub
                            return null
                        }

                        @Override
                        long getDate() throws RepositoryException {
                            return new Date().getTime();
                        }
                    });
                    eventConsumer.onEvent(new JCREventIterator(session, 1, 1, event.iterator(), 1));
                }
            }
        }
        return 0;
    }
};

log.info("Updated " + JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, "default", null, jcrCallback));
