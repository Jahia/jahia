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
import org.jahia.services.content.JCRNodeWrapper;
import org.springframework.beans.factory.InitializingBean;

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
        Session session = null;
        if (matcher.matches()) {
            final String path = matcher.group(6);
            final String uuid = matcher.group(5);
            if (pageHitCache.get("pageHitUuid" + uuid) != null) {
                //if exist in cache, increment and update in cache
                PageHit pageHit = (PageHit) pageHitCache.get("pageHitUuid" + uuid).getValue();
                pageHit.setHits(pageHit.getHits() + 1);
                if (!path.equals(pageHit.getPath())) {
                    pageHit.setPath(path);
                }
                Element elementToUpdate = new Element("pageHitUuid" + uuid, pageHit);
                pageHitCache.put(elementToUpdate);
                //if is the time to update in database
                if(pageHit.getHits() % maxNumberOfHitsInCacheBeforeFlush == 0){
                    session = sessionFactoryBean.openSession();
                    PageHit pageHitToUpdate = (PageHit) session.get(PageHit.class, uuid);
                    pageHitToUpdate.setHits(pageHit.getHits());
                    if(!pageHitToUpdate.getPath().equals(pageHit.getPath())) pageHitToUpdate.setPath(pageHit.getPath());
                    session.flush();
                }
            } else {
                //if not exist in cache recup pageHit from database, increment it and cache it
                session = sessionFactoryBean.openSession();
                try {
                    PageHit pageHit = (PageHit) session.get(PageHit.class, uuid);
                    // Found Update object
                    if (pageHit != null) {
                        pageHit.setHits(pageHit.getHits() + 1);
                        if(!path.equals(pageHit.getPath())){
                            pageHit.setPath(path);
                        }
                        Element elementNew = new Element("pageHitUuid" + uuid, pageHit);
                        pageHitCache.put(elementNew);
                        //if is the time to update in database
                        if(pageHit.getHits() % maxNumberOfHitsInCacheBeforeFlush == 0){
                            session.flush();
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
                }
            }
        }
        if(session != null){
            session.close();
        }
    }


    public long getNumberOfHits(JCRNodeWrapper node) {
        Session session = null;
        PageHit pageHit;
        try {
            if (pageHitCache.get("pageHitUuid" + node.getIdentifier()) != null) {
                pageHit = (PageHit) pageHitCache.get("pageHitUuid" + node.getIdentifier()).getValue();
            } else {
                session = sessionFactoryBean.openSession();
                Criteria criteria = session.createCriteria(PageHit.class);
                criteria.add(Restrictions.eq("uuid", node.getIdentifier()));
                pageHit = (PageHit) criteria.uniqueResult();
            }
            return pageHit != null ? pageHit.getHits() : 0;
        } catch (Exception e) {
            return 0;
        } finally {
            if (session != null) session.close();
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
