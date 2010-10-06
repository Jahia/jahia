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
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.impl.SessionFactoryImpl;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Oct 5, 2010
 * Time: 11:29:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class ContentHistoryService implements Processor, InitializingBean, CamelContextAware {
    private transient static Logger logger = Logger.getLogger(ContentHistoryService.class);

    private org.hibernate.impl.SessionFactoryImpl sessionFactoryBean;
    private Class mappingClass;

    private static ContentHistoryService instance;

    private Pattern pattern = Pattern.compile(
            "([0-9\\-]+ [0-9:,]+) user ([\\sa-zA-Z@.0-9_\\-]*) ip ([0-9.:]*) session ([a-zA-Z@0-9_\\-\\/]*) identifier ([a-zA-Z@0-9_\\-\\/:]*) path (.*) nodetype ([a-zA-Z:]*) (.*)");
    private CamelContext camelContext;
    private String from;

    public void setSessionFactoryBean(SessionFactoryImpl sessionFactoryBean) {
        this.sessionFactoryBean = sessionFactoryBean;
    }

    public void setMappingClass(Class mappingClass) {
        this.mappingClass = mappingClass;
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

            if ("property".equals(argList[0].trim())) {
                int lastSlashPos = path.lastIndexOf("/");
                if (lastSlashPos > -1) {
                    propertyName = path.substring(lastSlashPos + 1);
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
                }
                // Not found new object
                else {
                    if (!"viewed".equals(argList[1].trim())) {
                        historyEntry = new HistoryEntry();
                        historyEntry.setDate(date);
                        historyEntry.setPath(path);
                        historyEntry.setUuid(nodeIdentifier);
                        historyEntry.setUserKey(userKey);
                        historyEntry.setAction(argList[1]);
                        historyEntry.setPropertyName(propertyName);
                        historyEntry.setMessage("");
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

    public List<HistoryEntry> getNodeHistory(JCRNodeWrapper node) {
        Session session = sessionFactoryBean.openSession();
        Criteria criteria = session.createCriteria(HistoryEntry.class);
        try {
            criteria.add(Restrictions.eq("uuid", node.getIdentifier()));
            return (List<HistoryEntry>) criteria.list();
        } catch (Exception e) {
            return new ArrayList<HistoryEntry>();
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
