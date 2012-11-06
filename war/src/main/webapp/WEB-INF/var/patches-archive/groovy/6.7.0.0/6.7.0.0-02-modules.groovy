import javax.jcr.NodeIterator
import javax.jcr.RepositoryException
import javax.jcr.query.Query
import javax.jcr.query.QueryResult

import org.apache.jackrabbit.core.ItemManager;
import org.apache.jackrabbit.core.state.ItemStateReferenceCache
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.jahia.registries.ServicesRegistry
import org.jahia.services.content.JCRCallback
import org.jahia.services.content.JCRNodeWrapper
import org.jahia.services.content.JCRObservationManager
import org.jahia.services.content.JCRSessionWrapper
import org.jahia.services.content.JCRTemplate
import org.jahia.services.history.NodeVersionHistoryJob
import org.jahia.services.scheduler.BackgroundJob
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.SchedulerException

final Logger log = Logger.getLogger("org.jahia.tools.groovyConsole");

def removeTemplateSetsCallback = new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
        JCRObservationManager.setEventsDisabled(Boolean.TRUE);
        Logger logger = Logger.getLogger(ItemManager.class);
        Level previousLevel = logger.getLevel();
        logger.setLevel(Level.ERROR);

        try {
            log.info("Start removing templateSets from "+jcrsession.getWorkspace().getName()+" workspace");
            QueryResult result = jcrsession.getWorkspace().getQueryManager().createQuery("select * from [jnt:templateSets] where localname() = 'templateSets'", Query.JCR_SQL2).execute();
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
            log.info("...done removing "+jcrsession.getWorkspace().getName()+" nodes");
        } finally {
            logger.setLevel(previousLevel);
            JCRObservationManager.setEventsDisabled(Boolean.FALSE);
        }

        return null;
    }
}


JCRCallback<Object> callback = new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
        JCRObservationManager.setEventsDisabled(Boolean.TRUE);
        Logger logger = Logger.getLogger(ItemManager.class);
        Level previousLevel = logger.getLevel();
        logger.setLevel(Level.ERROR);

        try {
            log.info("Start updating jmix:hasTemplateNode from "+jcrsession.getWorkspace().getName()+" workspace going from references to name lookup");
            QueryResult result = jcrsession.getWorkspace().getQueryManager().createQuery("select * from [jmix:hasTemplateNode]", Query.JCR_SQL2).execute();
            NodeIterator ni = result.getNodes();
            while (ni.hasNext()) {
                try {
                    JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
                    if(next.hasProperty("j:templateNode")) {
                        String templateName = next.getProperty("j:templateNode").getNode().getName();
                        next.setProperty("j:templateName",templateName);
                        next.getProperty("j:templateNode").remove();
                    }
                    jcrsession.save();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            log.info("...done updating "+jcrsession.getWorkspace().getName()+" nodes");
        } finally {
            logger.setLevel(previousLevel);
            JCRObservationManager.setEventsDisabled(Boolean.FALSE);
        }

        return null;
    }

}

def removeTemplatesFolderCallback = new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
        JCRObservationManager.setEventsDisabled(Boolean.TRUE);
        Logger logger = Logger.getLogger(ItemManager.class);
        Level previousLevel = logger.getLevel();
        logger.setLevel(Level.ERROR);

        try {
            log.info("Start removing templates folder from "+jcrsession.getWorkspace().getName()+" workspace");
            QueryResult result = jcrsession.getWorkspace().getQueryManager().createQuery("select * from [jnt:templatesFolder] where localname() = 'templates'", Query.JCR_SQL2).execute();
            NodeIterator ni = result.getNodes();
            while (ni.hasNext()) {
                try {
                    JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
                    if(next.getParent().isNodeType("jnt:virtualsite"))
                    next.getRealNode().remove();
                    jcrsession.save();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            log.info("...done removing "+jcrsession.getWorkspace().getName()+" nodes");
        } finally {
            logger.setLevel(previousLevel);
            JCRObservationManager.setEventsDisabled(Boolean.FALSE);
        }

        return null;
    }
}


JCRTemplate.getInstance().doExecuteWithSystemSession(callback);
JCRTemplate.getInstance().doExecuteWithSystemSession(null, "live", callback);
JCRTemplate.getInstance().doExecuteWithSystemSession(null, "live", removeTemplateSetsCallback);
JCRTemplate.getInstance().doExecuteWithSystemSession(null, null, removeTemplateSetsCallback);
JCRTemplate.getInstance().doExecuteWithSystemSession(null, "live", removeTemplatesFolderCallback);
JCRTemplate.getInstance().doExecuteWithSystemSession(null, null, removeTemplatesFolderCallback);