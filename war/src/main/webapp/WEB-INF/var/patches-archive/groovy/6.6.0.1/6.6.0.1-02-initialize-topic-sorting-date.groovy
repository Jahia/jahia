import javax.jcr.*
import javax.jcr.query.*

import org.jahia.services.content.*

def log = log;

log.info("Start initializing new topic parameter for forum sorting")

Integer updated = JCRTemplate.getInstance().doExecuteWithSystemSession(null,"live",null, new JCRCallback<Integer>() {
    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
        int count = 0;
        Boolean forumDeployed = session.nodeExists("/templateSets/forum");
        if (forumDeployed){
            for (NodeIterator postIterator = session.getWorkspace().getQueryManager()
            .createQuery("SELECT * FROM [jnt:post] as post where ISDESCENDANTNODE(post,['/sites/']) order by [jcr:lastModified]",
            Query.JCR_SQL2).execute().getNodes(); postIterator .hasNext();) {
                JCRNodeWrapper post = (JCRNodeWrapper) postIterator.nextNode();
                log.debug("looking for topic on post : {}", post.getPath());
                if (post.getParent().isNodeType("jnt:topic")) {
                    Calendar postCreationDate = post.getProperty("jcr:created").getDate();
                    post.getParent().setProperty("topicLastContributionDate",postCreationDate);
                    log.debug("update topic : {}", post.getParent().getName());
                    count++;
                }
                if (count > 0 && (count % 1000 == 0)) {
                    session.save();
                }
            }
            if (count > 0) {
                session.save();
            }
            log.info("Number of posts checked: {}", count);
            return count;
        }else{
            log.info("Forum not deployed on this instance");
            return 0;
        }
    }
});