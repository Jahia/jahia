package org.jahia.modules.pagehit;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.ProcessorEndpoint;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.slf4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.impl.SessionFactoryImpl;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.RepositoryException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Dorth
 * Date: 26 août 2010
 * Time: 11:11:05
 */
public class PageHitService implements Processor, CamelContextAware, InitializingBean {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(PageHitService.class);

    private SessionFactoryImpl sessionFactoryBean;
    private EhCacheProvider cacheProviders;
    private Cache pageHitCache;
    private static PageHitService instance;
    private Pattern pattern = Pattern.compile(
            "([0-9\\-]+ [0-9:,]+) user ([a-zA-Z@.0-9_\\-]+) ip ([0-9.:]+) session ([a-zA-Z@0-9_\\-\\/]+) identifier ([a-zA-Z@0-9_\\-\\/]+) path (.*) nodetype ([a-zA-Z:]+) page viewed with (.*)");
    private CamelContext camelContext;
    private String from;
    private int maxNumberOfHitsInCacheBeforeFlush;

    public PageHitService() {
    }

    public void setSessionFactoryBean(SessionFactoryImpl sessionFactoryBean) {
        this.sessionFactoryBean = sessionFactoryBean;
    }

    public static PageHitService getInstance() {
        if (instance == null) {
            instance = new PageHitService();
        }
        return instance;
    }

    public void start() {
        // do nothing
    }

    public void process(Exchange exchange) throws Exception {
        final String message = (String) exchange.getIn().getBody();
        final Matcher matcher = pattern.matcher(message);
        if (matcher.matches()) {
            final String path = matcher.group(6);
            final JCRTemplate tpl = JCRTemplate.getInstance();
            JCRNodeWrapper node;
            try {
                node = tpl.doExecuteWithSystemSession(new JCRCallback<JCRNodeWrapper>() {
                    public JCRNodeWrapper doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        return session.getNode(path);
                    }
                });
            } catch (RepositoryException e) {
                // Node not found might be due to old logs so fail silently
                return;
            }
            if (node == null) {
                // stupid security check should not happen
                logger.warn("Node not found in system but it has not thrown an exception strange");
                return;
            }
            final String uuid = node.getIdentifier();
            Session session = sessionFactoryBean.openSession();
            try {
                PageHit pageHit = (PageHit) session.get(PageHit.class, uuid);
                // Found Update object
                if (pageHit != null) {
                    if (!pageHit.getPath().equals(path)) {
                        //the node's path have change so udpate it in database and increment it directly
                        pageHit.setPath(path);
                        if (pageHitCache.get("pageHitUuid" + uuid) == null) {    //path change and no version in cache
                            pageHit.setHits(pageHit.getHits() + 1);
                        } else {                                                 //path change and version in cache exist
                            pageHit.setHits(Long.valueOf(pageHitCache.get("pageHitUuid" + uuid).getValue().toString()) + 1);
                            pageHitCache.remove("pageHitUuid" + uuid);           //remove the version in cache
                        }
                        session.flush();
                        if (logger.isDebugEnabled()) {
                            logger.debug("Update into database pageHit page's path as change to: " + pageHit.getPath());
                        }
                    } else {
                        //increment and cache
                        Element elementHit;
                        if (pageHitCache.get("pageHitUuid" + uuid) == null) {
                            elementHit = new Element("pageHitUuid" + uuid, pageHit.getHits() + 1);
                        } else {
                            elementHit = new Element("pageHitUuid" + uuid, Long.valueOf(pageHitCache.get("pageHitUuid" + uuid).getValue().toString()) + 1);
                        }
                        pageHitCache.put(elementHit);
                        //if the counter of hits = maxNumberOfHitsInCacheBeforeFlush refresh and save in db
                        if (Integer.valueOf(pageHitCache.get("pageHitUuid" + uuid).getValue().toString()) % maxNumberOfHitsInCacheBeforeFlush == 0) {
                            pageHit.setHits(Long.valueOf(pageHitCache.get("pageHitUuid" + uuid).getValue().toString()));
                            pageHitCache.remove("pageHitUuid" + uuid);           //remove the version in cache
                            session.flush();
                            if (logger.isDebugEnabled()) {
                                logger.debug("Update into database pageHit page's path: " + pageHit.getPath() + " with number of view: " + pageHit.getHits());
                            }
                        }
                    }
                }
                // Not found new object
                else {
                    pageHit = new PageHit();
                    Long hit = (long) 1;
                    pageHit.setHits(hit);
                    pageHit.setPath(path);
                    pageHit.setUuid(uuid);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Insert into database pageHit page's path: " + pageHit.getPath() + " with number of view: " + pageHit.getHits());
                    }
                    session.save(pageHit);
                    session.flush();
                }
            } catch (HibernateException e) {
                logger.error(e.getMessage(), e);
            } finally {
                session.close();
            }
        }
    }


    public long getNumberOfHits(JCRNodeWrapper node) {
        Session session = sessionFactoryBean.openSession();
        Criteria criteria = session.createCriteria(PageHit.class);
        try {
            criteria.add(Restrictions.eq("uuid", node.getIdentifier()));
            PageHit pageHit = (PageHit) criteria.uniqueResult();
            if (pageHitCache.get("pageHitUuid" + node.getIdentifier()) != null) {
                return Long.valueOf(pageHitCache.get("pageHitUuid" + node.getIdentifier()).getValue().toString());
            }
            return pageHit != null ? pageHit.getHits() : 0;
        } catch (Exception e) {
            return 0;
        } finally {
            session.close();
        }
    }

    public void setCamelContext(final CamelContext camelContext) {
        this.camelContext = camelContext;
        final PageHitService pageHitService = this;
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(from).filter("groovy", "request.body.contains(\"page viewed\")").to(new ProcessorEndpoint(
                            "pageHitService", camelContext, pageHitService));
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

    public void afterPropertiesSet() throws Exception {
        CacheManager cacheManager = cacheProviders.getCacheManager();
        if (!cacheManager.cacheExists("PageHitsCache")) {
            cacheManager.addCache("PageHitsCache");
        }
        pageHitCache = cacheManager.getCache("PageHitsCache");
    }

    public void setCacheProviders(EhCacheProvider cacheProviders) {
        this.cacheProviders = cacheProviders;
    }

    public void setMaxNumberOfHitsInCacheBeforeFlush(int maxNumberOfHitsInCacheBeforeFlush) {
        this.maxNumberOfHitsInCacheBeforeFlush = maxNumberOfHitsInCacheBeforeFlush;
    }
}
