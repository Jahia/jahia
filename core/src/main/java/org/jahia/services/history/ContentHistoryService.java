package org.jahia.services.history;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.ProcessorEndpoint;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.impl.SessionFactoryImpl;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This service is responsible for listening to the metrics log and then constructing a history
 * table that we can query to know all the history of a content object.
 * <p/>
 * User: loom
 * Date: Oct 5, 2010
 * Time: 11:29:45 AM
 * To change this template use File | Settings | File Templates.
 *
 * @todo generate SQL DDL scripts for each DB, add messages to entries, fix date ordering bug, see if we can add a
 * Log4J appender component to Camel to speed up parsing.
 */
public class ContentHistoryService implements Processor, InitializingBean, CamelContextAware {
    private transient static Logger logger = Logger.getLogger(ContentHistoryService.class);

    private org.hibernate.impl.SessionFactoryImpl sessionFactoryBean;
    private Class mappingClass;
    private long processedCount = 0;

    private static ContentHistoryService instance;

    private Pattern pattern = Pattern.compile(
            "([0-9\\-]+ [0-9:,]+) user ([\\sa-zA-Z@.0-9_\\-]*) ip ([0-9.:]*) session ([a-zA-Z@0-9_\\-\\/]*) identifier ([a-zA-Z@0-9_\\-\\/:]*) path (.*) nodetype ([a-zA-Z:]*) (.*)");
    private CamelContext camelContext;
    private String from;
    private Set<String> ignoreProperties = new HashSet<String>();
    private Set<String> ignoreNodeTypes = new HashSet<String>();
    public static final String WITH_COMMENTS_MESSAGE_PART = "with comments ";
    public static final String VIEWED_ACTION_NAME = "viewed";
    public static final String PUBLISHED_ACTION_NAME = "published";

    public void setSessionFactoryBean(SessionFactoryImpl sessionFactoryBean) {
        this.sessionFactoryBean = sessionFactoryBean;
    }

    public void setMappingClass(Class mappingClass) {
        this.mappingClass = mappingClass;
    }

    public void setIgnoreProperties(Set<String> ignoreProperties) {
        this.ignoreProperties = ignoreProperties;
    }

    public void setIgnoreNodeTypes(Set<String> ignoreNodeTypes) {
        this.ignoreNodeTypes = ignoreNodeTypes;
    }

    public static ContentHistoryService getInstance() {
        if (instance == null) {
            instance = new ContentHistoryService();
        }
        return instance;
    }

    public void start() {
    }

    public void process(Exchange exchange) throws Exception {
        final String message = (String) exchange.getIn().getBody();
        final Matcher matcher = pattern.matcher(message);
        if (matcher.matches()) {
            processedCount++;
            if (processedCount % 1000 == 0) {
                logger.info("Processed " + processedCount + " content history messages.");
            }
            final String dateStr = matcher.group(1);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
            final Date date = dateFormat.parse(dateStr);
            final String userKey = matcher.group(2);
            final String ipAddress = matcher.group(3);
            final String httpSessionId = matcher.group(4);
            final String nodeIdentifier = matcher.group(5);
            final String path = matcher.group(6);
            final String nodeType = matcher.group(7);
            final String args = matcher.group(8);
            String propertyName = null;
            String[] argList = args.split(" ");
            String objectType = null;
            String action = null;
            if (argList.length >= 2) {
                objectType = argList[0].trim();
                action = argList[1].trim();
            }

            if ("property".equals(objectType)) {
                int lastSlashPos = path.lastIndexOf("/");
                if (lastSlashPos > -1) {
                    propertyName = path.substring(lastSlashPos + 1);
                }
            }

            if ((propertyName != null) && ignoreProperties.contains(propertyName)) {
                logger.debug("Ignoring property " + propertyName + " as configured.");
                return;
            }

            if ((nodeIdentifier != null) && (ignoreNodeTypes.size() > 0)) {
                final JCRTemplate tpl = JCRTemplate.getInstance();
                String matchingNodeType = null;
                try {
                    matchingNodeType = tpl.doExecuteWithSystemSession(new JCRCallback<String>() {
                        public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            JCRNodeWrapper node = session.getNodeByIdentifier(nodeIdentifier);
                            if (node != null) {
                                for (String ignoreNodeType : ignoreNodeTypes) {
                                    if (node.isNodeType(ignoreNodeType)) {
                                        return ignoreNodeType;
                                    }
                                }
                            }
                            return null;
                        }
                    });
                    if (matchingNodeType != null) {
                        logger.debug("Ignoring node type " + matchingNodeType + " as configured.");
                        return;
                    }
                } catch (RepositoryException e) {
                    // Node not found might be due to old logs so fail silently
                    logger.debug("Couldn't find node " + nodeIdentifier + " will not insert log entry. This could be due to parsing an old log.");
                    return;
                }

            }
            Session session = sessionFactoryBean.openSession();
            try {
                Criteria criteria = session.createCriteria(HistoryEntry.class);
                criteria.add(Restrictions.eq("uuid", nodeIdentifier));
                criteria.add(Restrictions.eq("date", date));
                criteria.add(Restrictions.eq("propertyName", propertyName));

                HistoryEntry historyEntry = (HistoryEntry) criteria.uniqueResult();
                // Found update object
                if (historyEntry != null) {
                    // history entry already exists, we will not update it.
                    if (logger.isDebugEnabled()) {
                        logger.debug("Content history entry " + historyEntry + " already exists, ignoring...");
                    }
                }
                // Not found new object
                else {
                    if (!VIEWED_ACTION_NAME.equals(action)) {
                        historyEntry = new HistoryEntry();
                        historyEntry.setDate(date);
                        historyEntry.setPath(path);
                        historyEntry.setUuid(nodeIdentifier);
                        historyEntry.setUserKey(userKey);
                        historyEntry.setAction(action);
                        historyEntry.setPropertyName(propertyName);
                        String historyMessage = "";
                        if (PUBLISHED_ACTION_NAME.equals(action)) {
                            if (argList.length >= 8) {
                                String sourceWorkspace = argList[3].trim();
                                String destinationWorkspace = argList[5].trim();
                                String historyComments = "";
                                int commentsPos = args.indexOf(WITH_COMMENTS_MESSAGE_PART);
                                if (commentsPos > -1) {
                                    historyComments = ";;" + args.substring(commentsPos + WITH_COMMENTS_MESSAGE_PART.length());
                                }
                                historyMessage = sourceWorkspace + ";;" + destinationWorkspace + historyComments;
                            }
                        }
                        historyEntry.setMessage(historyMessage);
                        session.save(historyEntry);
                    }
                }
            } catch (HibernateException e) {
                logger.error(e.getMessage(), e);
            } finally {
                session.flush();
                session.close();
            }
        }
    }

    public List<HistoryEntry> getNodeHistory(JCRNodeWrapper node, boolean withLanguageNodes) {
        Session session = sessionFactoryBean.openSession();
        Criteria criteria = session.createCriteria(HistoryEntry.class);
        try {
            criteria.add(Restrictions.eq("uuid", node.getIdentifier()));
            List<HistoryEntry> result = (List<HistoryEntry>) criteria.list();
            if (withLanguageNodes) {
                List<Locale> existingLocales = node.getExistingLocales();
                for (Locale existingLocale : existingLocales) {
                    Node localeNode = node.getI18N(existingLocale);
                    criteria = session.createCriteria(HistoryEntry.class);
                    criteria.add(Restrictions.eq("uuid", localeNode.getIdentifier()));
                    List<HistoryEntry> languageHistoryEntries = (List<HistoryEntry>) criteria.list();
                    for (HistoryEntry languageHistoryEntry : languageHistoryEntries) {
                        languageHistoryEntry.setLocale(existingLocale);
                        result.add(languageHistoryEntry);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            return new ArrayList<HistoryEntry>();
        } finally {
            session.close();
        }
    }

    public long deleteHistoryBeforeDate(Date date) {
        Session session = sessionFactoryBean.openSession();

        try {
            Transaction tx = session.beginTransaction();

            String hqlDelete = "delete HistoryEntry c where c.date < :date";
            int deletedEntities = session.createQuery(hqlDelete)
                    .setDate("date", date)
                    .executeUpdate();
            tx.commit();
            return deletedEntities;
        } catch (Exception e) {
            logger.error("Error deleting history entries before date " + date, e);
            return -1;
        } finally {
            session.close();
        }
    }

    public void afterPropertiesSet() throws Exception {
        ApplicationContext context = SpringContextSingleton.getInstance().getContext();
        AnnotationSessionFactoryBean localSessionFactoryBean = (AnnotationSessionFactoryBean) context.getBeansOfType(
                AnnotationSessionFactoryBean.class).get("&sessionFactory");
        localSessionFactoryBean.setAnnotatedClasses(new Class[]{mappingClass});
        localSessionFactoryBean.afterPropertiesSet();
        localSessionFactoryBean.updateDatabaseSchema();
        sessionFactoryBean = (SessionFactoryImpl) localSessionFactoryBean.getObject();
    }


    public void setCamelContext(final CamelContext camelContext) {
        this.camelContext = camelContext;
        final ContentHistoryService contentHistoryService = this;
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(from).to(new ProcessorEndpoint(
                            "contentHistoryService", camelContext, contentHistoryService));
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public CamelContext getCamelContext() {
        return camelContext;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setFrom(String from) {
        this.from = from;
    }

}
