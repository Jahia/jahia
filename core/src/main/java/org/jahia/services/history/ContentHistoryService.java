/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.history;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.ProcessorEndpoint;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.impl.SessionFactoryImpl;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

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
 */
public class ContentHistoryService implements Processor, CamelContextAware {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(ContentHistoryService.class);

    private org.hibernate.impl.SessionFactoryImpl sessionFactoryBean;
    private volatile long processedCount = 0;
    private volatile long ignoredCount = 0;
    private volatile long insertedCount = 0;
    private volatile long processedSinceLastReport = 0;
    private volatile long timeSinceLastReport = 0;
    private volatile long latestTimeProcessed = 0;
    private volatile String lastUUIDProcessed = null;
    private volatile String lastPropertyProcessed = null;
    private volatile String lastActionProcessed = null;

    private static ContentHistoryService instance;

    private static final Pattern PATTERN = Pattern.compile(
            "([0-9\\-]+ [0-9:,]+) user ([\\sa-zA-Z@.0-9_\\-]*) ip ([0-9.:]*) session ([a-zA-Z@0-9_\\-\\/]*) identifier ([a-zA-Z@0-9_\\-\\/:]*) path (.*) nodetype ([a-zA-Z:]*) (.*)");
    private static final Pattern ARG_SPLIT_PATTERN = Pattern.compile(" ");
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
        timeSinceLastReport = System.currentTimeMillis();
        latestTimeProcessed = getMostRecentTimeInHistory();
    }

    public void process(Exchange exchange) throws Exception {
        final String message = (String) exchange.getIn().getBody();
        final Matcher matcher = PATTERN.matcher(message);
        if (matcher.matches()) {
            processedCount++;
            processedSinceLastReport++;
            final String dateStr = matcher.group(1);
            final Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").parse(dateStr);
            final String userKey = matcher.group(2);
//            final String ipAddress = matcher.group(3);
//            final String httpSessionId = matcher.group(4);
            final String nodeIdentifier = matcher.group(5);
            final String path = matcher.group(6);
//            final String nodeType = matcher.group(7);
            final String args = matcher.group(8);
            String propertyName = null;
            String[] argList = ARG_SPLIT_PATTERN.split(args);
            String objectType = null;
            String action = null;
            if (argList.length >= 2) {
                objectType = argList[0].trim();
                action = argList[1].trim();
            }
            
            if (VIEWED_ACTION_NAME.equals(action)) {
            	ignoredCount++;
            	return;
            }

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
                ignoredCount++;
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
                        ignoredCount++;
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
                    ignoredCount++;
                    return;
                }

            }
            long timer = System.currentTimeMillis();
            Session session = sessionFactoryBean.openSession();
            String whatDidWeDo = "inserted";
            boolean shouldSkipInsertion = false;
            try {

                if (latestTimeProcessed > date.getTime()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Skipping content history entry since it's date {} is older than last processed date", date);
                    }
                    ignoredCount++;
                    whatDidWeDo = "skipped";
                    shouldSkipInsertion = true;
                } else {
                    // if the time is the same, we have to check for existing entries (or maybe it would be faster to
                    // delete and re-create them ?)
                    if (latestTimeProcessed == date.getTime()) {

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
                                ignoredCount++;
                                whatDidWeDo = "skipped";
                                shouldSkipInsertion = true;
                            }
                        }
                    }
                }
                // Not found new object
                if (!shouldSkipInsertion) {
					HistoryEntry historyEntry = new HistoryEntry();
					historyEntry.setDate(date != null ? date.getTime() : null);
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
					session.save(historyEntry);
	                session.flush();
					insertedCount++;
                    latestTimeProcessed = date.getTime();
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
                double elapsedTimeInSeconds = ((double)(nowTime - timeSinceLastReport)) / 1000.0;
                double rate = ((double)processedSinceLastReport) / elapsedTimeInSeconds;
				logger.info("Total count of processed content history messages: {}. Ignored: {}. Inserted: {}. Rate={} msgs/sec.", new Object[] {processedCount, ignoredCount, insertedCount, rate});
                processedSinceLastReport = 0;
                timeSinceLastReport = nowTime;
            }
        }
    }

    @SuppressWarnings("unchecked")
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

            int deletedEntities = session.createQuery("delete HistoryEntry c where c.date < :date")
                    .setLong("date", date != null ? date.getTime() : Long.MAX_VALUE)
                    .executeUpdate();
            tx.commit();
            if (deletedEntities > 0) {
                logger.info("Successfully deleted " + deletedEntities + " content history entries before date " + date);
            }
            return deletedEntities;
        } catch (Exception e) {
            logger.error("Error deleting history entries before date " + date, e);
            return -1;
        } finally {
            session.close();
        }
    }

    public long getMostRecentTimeInHistory() {
        Long timeStamp = -1L;
        Session session = sessionFactoryBean.openSession();

        try {
            timeStamp = (Long) session.createQuery("select max(c.date) as latestDate from HistoryEntry c").uniqueResult();
        } catch (Exception e) {
            logger.error("Error while trying to retrieve latest date processed.", e);
        } finally {
            session.close();
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
                    from(from).filter("groovy", "!request.body.contains(\" viewed \")").to(new ProcessorEndpoint(
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
