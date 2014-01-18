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

JCRTemplate.getInstance().doExecuteWithSystemSession(null, "live", new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
        JCRObservationManager.setEventsDisabled(Boolean.TRUE);
        Logger logger = Logger.getLogger(ItemManager.class);
        Level previousLevel = logger.getLevel();
        logger.setLevel(Level.ERROR);

        try {
            log.info("Start removing components from live workspace");
            QueryResult result = jcrsession.getWorkspace().getQueryManager().createQuery("select * from [jnt:componentFolder] where localname() = 'components'", Query.JCR_SQL2).execute();
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
            log.info("...done removing live nodes");
        } finally {
            logger.setLevel(previousLevel);
            JCRObservationManager.setEventsDisabled(Boolean.FALSE);
        }

        return null;
    }
});

JCRCallback<Object> callback = new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
        log.info("Start removing versioning in workspace " + jcrsession.getWorkspace().getName());
        JCRObservationManager.setEventsDisabled(Boolean.TRUE);
        Logger logger = Logger.getLogger(ItemStateReferenceCache.class);
        Level previousLevel = logger.getLevel();
        logger.setLevel(Level.ERROR);

        try {
            scheduleJob(removeVersionable(jcrsession, "jnt:user"))
            scheduleJob(removeVersionable(jcrsession, "jnt:group"))
            scheduleJob(removeVersionable(jcrsession, "jnt:virtualsite"))
            scheduleJob(removeVersionable(jcrsession, "jnt:virtualsitesFolder"))
            scheduleJob(removeVersionable(jcrsession, "jnt:tag"))
            scheduleJob(removeVersionable(jcrsession, "jnt:tagList"))
            scheduleJob(removeVersionable(jcrsession, "jnt:category"))
            scheduleJob(removeVersionable(jcrsession, "jnt:roles"))
            scheduleJob(removeVersionable(jcrsession, "jnt:role"))
            scheduleJob(removeVersionable(jcrsession, "jnt:permission"))
            scheduleJob(removeVersionable(jcrsession, "jnt:usersFolder"))
            scheduleJob(removeVersionable(jcrsession, "jnt:groupsFolder"))
            scheduleJob(removeVersionable(jcrsession, "jnt:componentFolder"))
            scheduleJob(removeVersionable(jcrsession, "jnt:templateSets"))
            scheduleJob(removeVersionable(jcrsession, "jnt:portletDefinition"))
            scheduleJob(removeVersionable(jcrsession, "jnt:portletDefinitions"))
            removeMixin(jcrsession, "jnt:component", "jmix:lastPublished");
        } finally {
            logger.setLevel(previousLevel);
            JCRObservationManager.setEventsDisabled(Boolean.FALSE);
        }
        log.info("...done removing versioning in workspace " + jcrsession.getWorkspace().getName());
        
        return null;
    }

    private Set<String> removeVersionable(JCRSessionWrapper jcrsession, String type) {
        return removeMixin(jcrsession, type, "mix:versionable");
    }
    
    private Set<String> removeMixin(JCRSessionWrapper jcrsession, String type, String mixin) {
        log.info("Start processing node type " + type + " and removing mixin " + mixin);
        Set<String> uuids = new HashSet<String>();
        QueryResult result = jcrsession.getWorkspace().getQueryManager().createQuery("select * from ["+ type +"]", Query.JCR_SQL2).execute();
        NodeIterator ni = result.getNodes();
        int count = 0;
        while (ni.hasNext()) {
            try {
                JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
                next.getRealNode().addMixin(mixin);
                jcrsession.save();
                count++;
                if (count > 0 && count % 100 == 0) {
                    log.info("Preprocessed " + count + " nodes");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        result = jcrsession.getWorkspace().getQueryManager().createQuery("select * from ["+ type +"]", Query.JCR_SQL2).execute();
        ni = result.getNodes();
        count = 0;
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            next.getRealNode().removeMixin(mixin);
            uuids.add(next.getIdentifier());
            count++;
            if (count > 0 && count % 100 == 0) {
                log.info("Processed " + count + " nodes");
            }
        }
        log.info("Saving changes for " + count + " nodes");
        jcrsession.save();
        
        log.info("..done processing nodes for type " + type);
        
        return uuids;
    }
    
    private void scheduleJob(Set<String> ids) {
        if (ids.size() == 0) {
            return;
        }
        JobDetail jobDetail = BackgroundJob.createJahiaJob("Node version history purge", NodeVersionHistoryJob.class);
        jobDetail.setGroup("Maintenance");
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        jobDataMap.put(NodeVersionHistoryJob.JOB_NODE_IDS, ids);

        log.info("Scheduling node version history purge job for " + ids.size() + " nodes");

        try {
            ServicesRegistry.getInstance().getSchedulerService().scheduleJobNow(jobDetail);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

}

JCRTemplate.getInstance().doExecuteWithSystemSession(callback);
JCRTemplate.getInstance().doExecuteWithSystemSession(null, "live", callback);
