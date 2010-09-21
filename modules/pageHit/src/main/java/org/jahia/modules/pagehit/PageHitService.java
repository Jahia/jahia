package org.jahia.modules.pagehit;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.ProcessorEndpoint;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.impl.SessionFactoryImpl;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.springframework.beans.factory.InitializingBean;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

import javax.jcr.RepositoryException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Dorth
 * Date: 26 août 2010
 * Time: 11:11:05
 * To change this template use File | Settings | File Templates.
 */
public class PageHitService implements Processor, InitializingBean, CamelContextAware {
    private transient static Logger logger = Logger.getLogger(PageHitService.class);

    private org.hibernate.impl.SessionFactoryImpl sessionFactoryBean;
    private Class mappingClass;

    private static PageHitService instance;

    private Pattern pattern = Pattern.compile(
            "([0-9\\-]+ [0-9:,]+) user ([a-zA-Z@.0-9_\\-]+) ip ([0-9.:]+) session ([a-zA-Z@0-9_\\-\\/]+) path (.*) nodetype ([a-zA-Z:]+) page viewed with (.*)");
    private CamelContext camelContext;
    private String from;

    public void setSessionFactoryBean(SessionFactoryImpl sessionFactoryBean) {
        this.sessionFactoryBean = sessionFactoryBean;
    }

    public void setMappingClass(Class mappingClass) {
        this.mappingClass = mappingClass;
    }

    public static PageHitService getInstance() {
        if (instance == null) {
            instance = new PageHitService();
        }
        return instance;
    }

    public void start() {
    }

    public void process(Exchange exchange) throws Exception {
        final String message = (String) exchange.getIn().getBody();
        final Matcher matcher = pattern.matcher(message);
        if (matcher.matches()) {
            final String path = matcher.group(5);
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
            if(node==null) {
                // stupid security check should not happen
                logger.warn("Node not found in system but it has not thrown an exception strange");
                return;
            }
            final String uuid = node.getIdentifier();
            Session session = sessionFactoryBean.openSession();
            try {
                Criteria criteria = session.createCriteria(PageHit.class);
                criteria.add(Restrictions.eq("uuid", uuid));

                PageHit pageHit = (PageHit) criteria.uniqueResult();
                // Found update object
                if (pageHit != null) {
                    pageHit.setHit(pageHit.getHit() + 1);
                    if(!pageHit.getPath().equals(path)){
                        pageHit.setPath(path);
                        logger.debug("Update into database pageHit page's path as change to: " + pageHit.getPath());
                    }
                    logger.debug("Update into database pageHit page's path: " + pageHit.getPath() + " with number of view: " + pageHit.getHit());
                }
                // Not found new object
                else {
                    pageHit = new PageHit();
                    Long hit = (long) 1;
                    pageHit.setHit(hit);
                    pageHit.setPath(path);
                    pageHit.setUuid(uuid);
                    session.save(pageHit);
                    logger.debug("Insert into database pageHit page's path: " + pageHit.getPath() + " with number of view: " + pageHit.getHit());
                }
            } catch (HibernateException e) {
                logger.error(e.getMessage(), e);
            } finally {
                session.flush();
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
            return pageHit.getHit();
        } catch (Exception e) {
            return 0;
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
        return camelContext;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
