/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.history;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.ProcessorEndpoint;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.*;
import org.hibernate.criterion.Restrictions;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This service is responsible for listening to the metrics log and then constructing a history
 * table that we can query to know all the history of a content object.
 * <p/>
 * @author loom
 */
public class ContentHistoryService implements Processor, CamelContextAware {
    private transient static Logger logger = LoggerFactory.getLogger(ContentHistoryService.class);

    private org.hibernate.SessionFactory sessionFactoryBean;
    private AtomicLong processedCount = new AtomicLong(0);
    private AtomicLong ignoredCount = new AtomicLong(0);
    private AtomicLong insertedCount = new AtomicLong(0);
    private AtomicLong processedSinceLastReport = new AtomicLong(0);
    private AtomicLong timeSinceLastReport = new AtomicLong(0);
    private AtomicLong latestTimeProcessed = new AtomicLong(0);
    private volatile String lastUUIDProcessed = null;
    private volatile String lastPropertyProcessed = null;
    private volatile String lastActionProcessed = null;

    private static ContentHistoryService instance = new ContentHistoryService();

    private static final Pattern PATTERN = Pattern.compile(
            "([0-9\\-]+ [0-9:,]+) user ([\\sa-zA-Z@.0-9_\\-]*) ip ([0-9.:]*) session ([a-zA-Z@0-9_\\-\\/]*) identifier ([a-zA-Z@0-9_\\-\\/:]*) path (.*) nodetype ([a-zA-Z:]*) (.*)");
    private CamelContext camelContext;
    private String from;
    private Set<String> ignoreProperties = new HashSet<String>();
    private Set<String> ignoreNodeTypes = new HashSet<String>();
    public static final String WITH_COMMENTS_MESSAGE_PART = "with comments ";
    public static final String VIEWED_ACTION_NAME = "viewed";
    public static final String PUBLISHED_ACTION_NAME = "published";

    public void setSessionFactoryBean(SessionFactory sessionFactoryBean) {
        this.sessionFactoryBean = sessionFactoryBean;
    }

    public void setIgnoreProperties(Set<String> ignoreProperties) {
        this.ignoreProperties = ignoreProperties;
    }

    public void setIgnoreNodeTypes(Set<String> ignoreNodeTypes) {
        this.ignoreNodeTypes = ignoreNodeTypes;
    }

    public static ContentHistoryService getInstance() {
        return instance;
    }

    private void initTimestamps(Session session) {
        timeSinceLastReport.set(System.currentTimeMillis());
        latestTimeProcessed.set(getMostRecentTimeInHistory(session));
    }

    public void process(Exchange exchange) throws Exception {
        final String message = (String) exchange.getIn().getBody();
        final Matcher matcher = PATTERN.matcher(message);
        if (matcher.matches()) {
            long processedCount = this.processedCount.incrementAndGet();
            processedSinceLastReport.incrementAndGet();
//            final String ipAddress = matcher.group(3);
//            final String httpSessionId = matcher.group(4);
//            final String nodeType = matcher.group(7);
            final String args = matcher.group(8);
            String propertyName = null;
            String[] argList = args != null && args.length() > 0 ? StringUtils.split(args, ' ')
                    : ArrayUtils.EMPTY_STRING_ARRAY;
            String objectType = null;
            String action = null;
            if (argList.length >= 2) {
                objectType = argList[0];
                action = argList[1];
            }

            if (VIEWED_ACTION_NAME.equals(action)) {
                ignoredCount.incrementAndGet();
                return;
            }

            final String path = matcher.group(6);
            if ("property".equals(objectType)) {
                int lastSlashPos = path.lastIndexOf("/");
                if (lastSlashPos > -1) {
                    propertyName = path.substring(lastSlashPos + 1);
                }
            }

            if ((propertyName != null) && ignoreProperties.contains(propertyName)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignoring property " + propertyName + " as configured.");
                }
                ignoredCount.incrementAndGet();
                return;
            }

            final String nodeIdentifier = matcher.group(5);
            if ((nodeIdentifier != null) && !"null".equals(nodeIdentifier) && (ignoreNodeTypes.size() > 0)) {
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
                        ignoredCount.incrementAndGet();
                        if (logger.isDebugEnabled()) {
                            logger.debug("Ignoring node type " + matchingNodeType + " as configured.");
                        }
                        return;
                    }
                } catch (RepositoryException e) {
                    // Node not found might be due to old logs so fail silently
                    if (logger.isDebugEnabled()) {
                        logger.debug("Couldn't find node " + nodeIdentifier + " will not insert log entry. This could be due to parsing an old log.");
                    }
                    ignoredCount.incrementAndGet();
                    return;
                }

            }
            long timer = System.currentTimeMillis();
            Session session = sessionFactoryBean.openSession();
            String whatDidWeDo = "inserted";
            boolean shouldSkipInsertion = false;
            try {
                if (latestTimeProcessed.get() == 0L) {
                    initTimestamps(session);
                }
                final Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").parse(matcher.group(1));
                if (latestTimeProcessed.get() > date.getTime()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Skipping content history entry since it's date {} is older than last processed date", date);
                    }
                    ignoredCount.incrementAndGet();
                    whatDidWeDo = "skipped";
                    shouldSkipInsertion = true;
                } else {
                    // if the time is the same, we have to check for existing entries (or maybe it would be faster to
                    // delete and re-create them ?)
                    if (latestTimeProcessed.get() == date.getTime()) {

                        // we will now check if the UUID, property name and actions are equal to the last processed
                        // action, in order to avoid performing duplicate checks in the database if we can avoid them.
                        boolean mustCheckInDB = false;
                        if ((lastUUIDProcessed != null) && (lastUUIDProcessed.equals(nodeIdentifier))) {
                            if ((lastPropertyProcessed != null) && (lastPropertyProcessed.equals(propertyName)) ||
                                    ((lastPropertyProcessed == null) && (propertyName == null))) {
                                if (lastActionProcessed.equals(action)) {
                                    // everything is equal, we will have to check for duplicate in database.
                                    mustCheckInDB = true;
                                }
                            }
                        }

                        if (mustCheckInDB) {
                            Criteria criteria = session.createCriteria(HistoryEntry.class);
                            criteria.add(Restrictions.eq("uuid", nodeIdentifier));
                            criteria.add(Restrictions.eq("date", date != null ? Long.valueOf(date.getTime()) : null));
                            criteria.add(Restrictions.eq("propertyName", propertyName));
                            criteria.add(Restrictions.eq("action", action));

                            HistoryEntry historyEntry = (HistoryEntry) criteria.uniqueResult();
                            // Found update object
                            if (historyEntry != null) {
                                // history entry already exists, we will not update it.
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Content history entry " + historyEntry + " already exists, ignoring...");
                                }
                                ignoredCount.incrementAndGet();
                                whatDidWeDo = "skipped";
                                shouldSkipInsertion = true;
                            }
                        }
                    }
                }
                // Not found new object
                if (!shouldSkipInsertion) {
                    session.beginTransaction();
                    HistoryEntry historyEntry = new HistoryEntry();
                    historyEntry.setDate(date != null ? date.getTime() : null);
                    historyEntry.setPath(path);
                    historyEntry.setUuid(nodeIdentifier);
                    final String userKey = matcher.group(2);
                    historyEntry.setUserKey(userKey);
                    historyEntry.setAction(action);
                    historyEntry.setPropertyName(propertyName);
                    String historyMessage = "";
                    if (PUBLISHED_ACTION_NAME.equals(action)) {
                        if (argList.length >= 8) {
                            String sourceWorkspace = argList[3];
                            String destinationWorkspace = argList[5];
                            String historyComments = "";
                            int commentsPos = args.indexOf(WITH_COMMENTS_MESSAGE_PART);
                            if (commentsPos > -1) {
                                String comment = args.substring(commentsPos
                                        + WITH_COMMENTS_MESSAGE_PART.length());
                                if ((comment != null) && (!StringUtils.isEmpty(comment.trim()))) {
                                    historyComments = ";;" + comment.trim();
                                }
                            }
                            historyMessage = sourceWorkspace + ";;" + destinationWorkspace
                                    + historyComments;
                        }
                    }
                    historyEntry.setMessage(historyMessage);
                    try {
                        session.save(historyEntry);
                        session.flush();
                        session.getTransaction().commit();
                    } catch (Exception e) {
                        session.getTransaction().rollback();
                        throw e;
                    }
                    insertedCount.incrementAndGet();
                    latestTimeProcessed.set(date.getTime());
                    lastUUIDProcessed = nodeIdentifier;
                    lastPropertyProcessed = propertyName;
                    lastActionProcessed = action;
                }
            } catch (HibernateException e) {
                whatDidWeDo = "insertion failed";
                logger.error(e.getMessage(), e);
            } finally {
                session.close();
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Entry " + whatDidWeDo + " in " + (System.currentTimeMillis() - timer) + " ms");
            }

            if (processedCount % 2000 == 0) {
                long nowTime = System.currentTimeMillis();
                double elapsedTimeInSeconds = ((double) (nowTime - timeSinceLastReport.get())) / 1000.0;
                double rate = ((double) processedSinceLastReport.get()) / elapsedTimeInSeconds;
                logger.info("Total count of processed content history messages: {}. Ignored: {}. Inserted: {}. Rate={} msgs/sec.", new Object[]{processedCount, ignoredCount, insertedCount, rate});
                this.processedSinceLastReport.set(0);
                timeSinceLastReport.set(nowTime);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<HistoryEntry> getNodeHistory(JCRNodeWrapper node, boolean withLanguageNodes) {
        Session session = sessionFactoryBean.openSession();
        try {
            Criteria criteria = session.createCriteria(HistoryEntry.class);
            Map<String, Locale> i18ns = null;
            if (withLanguageNodes) {
                i18ns = new HashMap<String, Locale>(4);
                i18ns.put(node.getIdentifier(), null);
                for (NodeIterator ni = node.getI18Ns(); ni.hasNext();) {
                    Node n = ni.nextNode();
                    i18ns.put(n.getIdentifier(),
                            LanguageCodeConverters.languageCodeToLocale(n.getProperty("jcr:language").getString()));
                }
                criteria.add(Restrictions.in("uuid", i18ns.keySet()));
            } else {
                criteria.add(Restrictions.eq("uuid", node.getIdentifier()));
            }
            Transaction tx = session.beginTransaction();
            List<HistoryEntry> result = (List<HistoryEntry>) criteria.list();
            if (withLanguageNodes) {
                for (HistoryEntry entry : result) {
                    entry.setLocale(i18ns.get(entry.getUuid()));
                }
            }
            tx.commit();
            return result;
        } catch (Exception e) {
            session.getTransaction().rollback();
            return Collections.emptyList();
        } finally {
            session.close();
        }
    }

    public long deleteHistoryBeforeDate(Date date) {
        Session session = sessionFactoryBean.openSession();

        try {
            Transaction tx = session.beginTransaction();

            int deletedEntities = session.createQuery("delete HistoryEntry c where c.date < :date")
                    .setLong("date", date != null ? date.getTime() : Long.MAX_VALUE)
                    .executeUpdate();
            tx.commit();
            if (deletedEntities > 0) {
                logger.info("Successfully deleted " + deletedEntities + " content history entries before date " + date);
            }
            return deletedEntities;
        } catch (Exception e) {
            session.getTransaction().rollback();
            logger.error("Error deleting history entries before date " + date, e);
            return -1;
        } finally {
            session.close();
        }
    }

    public long getMostRecentTimeInHistory(Session session) {
        Long timeStamp = -1L;
        try {
            timeStamp = (Long) session.createQuery("select max(c.date) as latestDate from HistoryEntry c").uniqueResult();
        } catch (Exception e) {
            logger.error("Error while trying to retrieve latest date processed.", e);
        }
        return timeStamp != null ? timeStamp : -1;
    }

    public void setCamelContext(final CamelContext camelContext) {
        this.camelContext = camelContext;
        final ContentHistoryService contentHistoryService = this;
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(from).filter(body().not().contains(" viewed ")).to(
                            new ProcessorEndpoint("contentHistoryService", camelContext, contentHistoryService));
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }

    public void setFrom(String from) {
        this.from = from;
    }

}
