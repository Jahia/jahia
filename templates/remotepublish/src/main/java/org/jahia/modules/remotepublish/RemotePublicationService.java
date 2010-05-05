package org.jahia.modules.remotepublish;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.impl.SessionFactoryImpl;
import org.jahia.api.Constants;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.services.content.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.observation.Event;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 22, 2010
 * Time: 5:57:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemotePublicationService implements InitializingBean {
    private transient static Logger logger = Logger.getLogger(RemotePublicationService.class);

    private JCRSessionFactory sessionFactory;
    private org.hibernate.impl.SessionFactoryImpl sessionFactoryBean;
    private Class mappingClass;

    private static RemotePublicationService instance;

    private static List<String> protectedPropertiesToExport = new ArrayList<String>();

    static {
        protectedPropertiesToExport.add(Constants.JCR_MIXINTYPES);
    }

    public static RemotePublicationService getInstance() {
        if (instance == null) {
            instance = new RemotePublicationService();
        }
        return instance;
    }

    public void start() {
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setSessionFactoryBean(org.hibernate.impl.SessionFactoryImpl sessionFactoryBean) {
        this.sessionFactoryBean = sessionFactoryBean;
    }

    public void setMappingClass(Class mappingClass) {
        this.mappingClass = mappingClass;
    }

    public void generateLog(JCRNodeWrapper source, Calendar calendar, OutputStream os) throws Exception {
        LogBundle bundle = new LogBundle();
        bundle.setSourceUuid(source.getIdentifier());
        String sourcePath = source.getPath();
        bundle.setSourcePath(sourcePath);

        ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(os));

        oos.writeObject(bundle);

        Session session = sessionFactoryBean.openSession();
        List<Journal> journalList;

        if (calendar != null) {
            final Criteria criteria = session.createCriteria(Journal.class);
            criteria.add(Restrictions.ge("eventDate", calendar.getTime()));
            journalList = criteria.list();
        } else {
            final Criteria criteria = session.createCriteria(Journal.class);
            criteria.add(Restrictions.ge("eventDate", source.getProperty(Constants.JCR_CREATED).getDate().getTime()));
            journalList = criteria.list();
        }

        Date lastDate = new Date();
        Calendar journalCalendar = new GregorianCalendar();
        for (Journal journal : journalList) {
            boolean startOfNewJournal = true;
            journalCalendar.setTime(journal.getEventDate());
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(journal.getEvents());
            ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(byteArrayInputStream));
            Object o;
            try {
                while ((o = ois.readObject()) != null) {
                    if (o instanceof LogEntry) {
                        LogEntry logEntry = (LogEntry) o;
                        final String path = logEntry.getPath();
                        if (path.startsWith(sourcePath) && path.length() > sourcePath.length()) {
                            if (startOfNewJournal) {
                                startOfNewJournal = false;
                                oos.writeObject(new LogEntries());
                            }
                            oos.writeObject(logEntry);
                            switch (logEntry.getEventType()) {
                                case Event.NODE_ADDED: {
                                    logger.debug("generateLog - Add Node " + path);
                                    oos.writeObject(ois.readObject());
                                    oos.writeObject(ois.readObject());
                                    break;
                                }
                                case Event.NODE_MOVED: {
                                    logger.debug("generateLog - Move Node " + path);
                                    oos.writeObject(ois.readObject());
                                    break;
                                }
                                case Event.PROPERTY_ADDED: {
                                    logger.debug("generateLog - Add Property " + path);
                                    oos.writeObject(ois.readObject());
                                    break;
                                }
                                case Event.PROPERTY_CHANGED: {
                                    logger.debug("generateLog - Change Property " + path);
                                    oos.writeObject(ois.readObject());
                                    break;
                                }
                                case Event.NODE_REMOVED: {
                                    logger.debug("generateLog - Remove Node " + path);
                                    break;
                                }
                                case Event.PROPERTY_REMOVED: {
                                    logger.debug("generateLog - Remove Proeprty " + path);
                                    break;
                                }
                            }
                        }
                        lastDate = journal.getEventDate();
                    }
                }
            } catch (EOFException e) {
                ;
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            ois.close();
            byteArrayInputStream.close();
        }

        GregorianCalendar date = new GregorianCalendar();
        date.setTime(lastDate);
        oos.writeObject(new LogBundleEnd(date));
        oos.close();
        logger.info("generateLog - end");
    }

    public boolean addEntry(Event event, ObjectOutputStream oos, JCRSessionWrapper session, Set<String> addedPath)
            throws RepositoryException, IOException {
        String path = event.getPath();
        boolean eventWritten = false;
        final LogEntry logEntry = new LogEntry(path, event.getType());
        switch (event.getType()) {
            case Event.NODE_ADDED: {
                try {
                    logger.debug("addEntry - Add node " + path);
                    JCRNodeWrapper node = session.getNode(path);
                    oos.writeObject(logEntry);
                    oos.writeObject(node.getPrimaryNodeTypeName());
                    NodeIterator ni = node.getSharedSet();
                    List<String> sharedSet = new ArrayList<String>();
                    while (ni.hasNext()) {
                        JCRNodeWrapper sub = (JCRNodeWrapper) ni.next();
                        sharedSet.add(sub.getPath());
                    }
                    oos.writeObject(sharedSet);
                    eventWritten = true;
                } catch (PathNotFoundException e) {
                    // not present anymore
                }
                break;
            }

            case Event.NODE_REMOVED: {
                logger.debug("addEntry - Remove node " + path);
                oos.writeObject(logEntry);
                eventWritten = true;
                addedPath.add(path);
                break;
            }

            case Event.NODE_MOVED: {
                Map map = event.getInfo();
                if (map.containsKey("srcChildRelPath")) {
                    logger.debug("addEntry - Move node " + path);
                    oos.writeObject(logEntry);
                    Map<String, String> newMap = new HashMap<String, String>();
                    newMap.put("srcChildPath", map.get("srcChildRelPath").toString());
                    Object o = map.get("destChildRelPath");
                    if (o != null) {
                        newMap.put("destChildPath", o.toString());
                    }
                    oos.writeObject(newMap);
                    addedPath.add(path);
                    eventWritten = true;
                }
                break;
            }

            case Event.PROPERTY_ADDED: {
                String nodePath = StringUtils.substringBeforeLast(path, "/");
                try {
                    final JCRNodeWrapper node = session.getNode(nodePath);
                    final JCRPropertyWrapper property = node.getProperty(StringUtils.substringAfterLast(path, "/"));
                    if (!property.getDefinition().isProtected() || protectedPropertiesToExport.contains(
                            property.getName())) {
                        logger.debug("addEntry - Add property " + path);
                        oos.writeObject(logEntry);
                        serializePropertyValue(oos, property);
                        eventWritten = true;
                    }
                } catch (PathNotFoundException e) {
                    logger.debug(e.getMessage(), e);
                }
                break;
            }

            case Event.PROPERTY_CHANGED: {
                String nodePath = StringUtils.substringBeforeLast(path, "/");
                try {
                    final JCRNodeWrapper node = session.getNode(nodePath);
                    final JCRPropertyWrapper property = node.getProperty(StringUtils.substringAfterLast(path, "/"));
                    if (!property.getDefinition().isProtected() || protectedPropertiesToExport.contains(
                            property.getName())) {
                        logger.debug("addEntry - Change property " + path);
                        oos.writeObject(logEntry);
                        serializePropertyValue(oos, property);
                        eventWritten = true;
                    }
                } catch (PathNotFoundException e) {
                    logger.debug(e.getMessage(), e);
                }
                break;
            }

            case Event.PROPERTY_REMOVED: {
                if (!addedPath.contains(path)) {
                    logger.debug("addEntry - Remove property " + path);
                    oos.writeObject(logEntry);
                    eventWritten = true;
                }
                break;
            }
        }
        return eventWritten;
    }

    private void serializePropertyValue(ObjectOutputStream oos, JCRPropertyWrapper property)
            throws RepositoryException, IOException {
        if (property.isMultiple()) {
            final Value[] obj = property.getValues();
            Object[] builder = new String[obj.length];
            for (int i = 0; i < obj.length; i++) {
                Value value = obj[i];
                builder[i] = serializePropertyValue(value);
            }
            oos.writeObject(builder);
        } else {
            oos.writeObject(serializePropertyValue(property.getValue()));
        }
    }

    private Object serializePropertyValue(Value value) throws RepositoryException {
        if (value.getType() == PropertyType.BINARY) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                IOUtils.copy(value.getBinary().getStream(), baos);
                return baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return value.getString();
        }
        return null;
    }

    private Value deserializePropertyValue(Object object, ValueFactory factory) throws RepositoryException {
        if (object instanceof byte[]) {
            return factory.createValue(factory.createBinary(new ByteArrayInputStream((byte[]) object)));
        } else {
            return factory.createValue((String) object);
        }
    }


    public void replayLog(final JCRNodeWrapper t, final InputStream in)
            throws IOException, ClassNotFoundException, RepositoryException {

        final String targetWorkspace = t.getSession().getWorkspace().getName();
        logger.info("replayLog - start in node " + t.getIdentifier() + " " + t.getPath());
        final JCRTemplate tpl = JCRTemplate.getInstance();
        tpl.doExecuteWithUserSession(sessionFactory.getCurrentUser().getName(), targetWorkspace, new JCRCallback() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {
                    JCRNodeWrapper target = session.getNodeByUUID(t.getIdentifier());
                    ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(in));
                    LogBundle log = (LogBundle) ois.readObject();

                    session.getPathMapping().put(log.getSourcePath(), target.getPath());
                    Set<String> addedPath = new HashSet<String>();
                    Map<String, Map<String, Object>> missedProperties = new HashMap<String, Map<String, Object>>();
                    Object o;
                    while ((o = ois.readObject()) != null) {
                        if (o instanceof LogEntry) {
                            parseLogEntry(session, target, ois, log, addedPath, missedProperties, o);
                        } else if (o instanceof LogEntries) {
                            if (logger.isInfoEnabled()) {
                                logger.info("replayLog - Start Of new LogEntries");
                            }
                            session.save();
                        } else if (o instanceof LogBundleEnd) {
                            if (logger.isInfoEnabled()) {
                                logger.info("replayLog - End of LogBundle");
                            }
                            LogBundleEnd end = (LogBundleEnd) o;
                            target.checkout();
                            target.addMixin("jmix:remotelyPublished");
                            target.setProperty("uuid", log.getSourceUuid());
//                            target.setProperty("lastReplay", end.getDate());
                            session.save();
                        }
                    }
                } catch (EOFException e) {
                    ;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RepositoryException(e);
                }
                return null;
            }
        });
    }

    private void parseLogEntry(JCRSessionWrapper session, JCRNodeWrapper target, ObjectInputStream ois, LogBundle log,
                               Set<String> addedPath, Map<String, Map<String, Object>> missedProperties, Object o)
            throws IOException, ClassNotFoundException, RepositoryException {
        LogEntry entry = (LogEntry) o;
        String path = target.getPath() + StringUtils.substringAfter(entry.getPath(), log.getSourcePath());

        String name = StringUtils.substringAfterLast(path, "/");
        switch (entry.getEventType()) {
            case Event.NODE_ADDED: {
                String nodeType = (String) ois.readObject();
                List<String> sharedSet = (List<String>) ois.readObject();
                if (!addedPath.contains(path)) {
                    if (logger.isInfoEnabled()) {
                        logger.info("replayLog - Adding Node " + path + " with nodetype: " + nodeType);
                    }
                    String parentPath = StringUtils.substringBeforeLast(path, "/");
                    JCRNodeWrapper parent = null;
                    try {
                        parent = session.getNode(parentPath);
                    } catch (RepositoryException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        break;
                    }
                    if (name.endsWith("]")) {
                        name = StringUtils.substringBeforeLast(name, "[");
                    }
                    if (parent.hasNode(name)) {
                        break;
                    }
                    parent.checkout();

                    boolean sharedNode = false;
                    for (String sharedPath : sharedSet) {
                        if (!sharedPath.equals(entry.getPath()) && sharedPath.startsWith(log.getSourcePath())) {
                            String s = target.getPath() + StringUtils.substringAfter(sharedPath, log.getSourcePath());
                            try {
                                JCRNodeWrapper node = session.getNode(s);
                                logger.info("replayLog - Found an existing share at : " + s);
                                parent.clone(node, name);
                                sharedNode = true;
                                break;
                            } catch (PathNotFoundException e) {
                                // Share not found : ignore
                            }
                        }
                    }
                    if (!sharedNode) {
                        parent.addNode(name, nodeType);
                    }
                    addedPath.add(path);
                }
                break;
            }
            case Event.NODE_REMOVED: {
                if (logger.isInfoEnabled()) {
                    logger.info("replayLog - Removing Node " + path);
                }
                try {
                    final JCRNodeWrapper node = session.getNode(path);
                    node.getParent().checkout();
                    node.checkout();
                    node.remove();
                    addedPath.remove(path);
                } catch (RepositoryException e) {
                    logger.debug(e.getMessage(), e);
                }
                break;
            }
            case Event.NODE_MOVED: {
                Map<String, String> map = (Map<String, String>) ois.readObject();
                String srcPath = map.get("srcChildPath");
                String destPath = map.get("destChildPath");
                if (logger.isInfoEnabled()) {
                    logger.info(
                            "replayLog - Moving Node " + path + " srcChildPath = " + srcPath + " destChildPath = " + destPath);
                }
                final JCRNodeWrapper node = session.getNode(path);
                JCRNodeWrapper parent = node.getParent();
                parent.checkout();
                node.checkout();
                parent.orderBefore(srcPath, destPath);
                break;
            }
            case Event.PROPERTY_ADDED:
            case Event.PROPERTY_CHANGED: {
                Object o1 = ois.readObject();
                try {
                    if (logger.isInfoEnabled()) {
                        logger.info("replayLog - Add/Update of property Node " + path);
                    }
                    updateProperty(session, path, o1);
                    if (path.contains("jcr:language") && path.contains("j:translation")) {
                        String translationPath = StringUtils.substringBeforeLast(path, "/");
                        Map<String, Object> map = missedProperties.get(translationPath);
                        if (map != null) {
                            for (Map.Entry<String, Object> missedProperty : map.entrySet()) {
                                updateProperty(session, missedProperty.getKey(), missedProperty.getValue());
                            }
                        }
                        missedProperties.remove(translationPath);
                    }
                } catch (ConstraintViolationException e) {
                    logger.debug(
                            "replayLog - Issue during add/update of property " + path + " (error: " + e.getMessage() + ")",
                            e);
                } catch (PathNotFoundException e) {
                    logger.debug(
                            "replayLog - Error during add/update of property " + path + " (error: " + e.getMessage() + ")",
                            e);
                    if (path.contains("j:translation")) {
                        String translationPath = StringUtils.substringBeforeLast(path, "/");
                        Map<String, Object> map = missedProperties.get(translationPath);
                        if (map == null) {
                            map = new HashMap<String, Object>();
                        }
                        map.put(path, o1);
                        missedProperties.put(translationPath, map);
                    }
                } catch (RepositoryException e) {
                    logger.error(
                            "replayLog - Error during add/update of property " + path + " (error: " + e.getMessage() + ")",
                            e);
                    throw e;
                }
                break;
            }
            case Event.PROPERTY_REMOVED: {
                if (logger.isInfoEnabled()) {
                    logger.info("replayLog - Removing Property " + path);
                }
                try {
                    final JCRNodeWrapper node = session.getNode(StringUtils.substringBeforeLast(path, "/"));
                    node.checkout();
                    node.getProperty(name).remove();
                } catch (PathNotFoundException e) {
                    logger.debug(
                            "replayLog - Issue during removal of property " + path + " (error: " + e.getMessage() + ")",
                            e);
                } catch (ConstraintViolationException e) {
                    logger.debug(
                            "replayLog - Issue during removal of property " + path + " (error: " + e.getMessage() + ")",
                            e);
                }
                break;
            }
        }
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    public void afterPropertiesSet() throws Exception {
        ApplicationContext context = SpringContextSingleton.getInstance().getContext();
        AnnotationSessionFactoryBean localSessionFactoryBean = (AnnotationSessionFactoryBean) context.getBeansOfType(
                AnnotationSessionFactoryBean.class).get("&sessionFactory");
        localSessionFactoryBean.setAnnotatedClasses(new Class[]{mappingClass});
        localSessionFactoryBean.afterPropertiesSet();
        localSessionFactoryBean.updateDatabaseSchema();
        sessionFactoryBean = (SessionFactoryImpl) localSessionFactoryBean.getObject();
    }

    private void updateProperty(JCRSessionWrapper session, String path, Object o1) throws RepositoryException {
        final JCRNodeWrapper node = session.getNode(StringUtils.substringBeforeLast(path, "/"));
        node.checkout();
        String propertyName = StringUtils.substringAfterLast(path, "/");

        if (o1 instanceof Object[]) {
            final Object[] objects = (Object[]) o1;
            Value[] values = new Value[objects.length];
            for (int i = 0; i < objects.length; i++) {
                Object object = objects[i];
                values[i] = deserializePropertyValue(object, session.getValueFactory());
            }
            if (!protectedPropertiesToExport.contains(propertyName)) {
                node.setProperty(propertyName, values);
            } else if (propertyName.equals(Constants.JCR_MIXINTYPES)) {
                for (Value value : values) {
                    node.addMixin(value.getString());
                }
            }
        } else {
            node.setProperty(propertyName, deserializePropertyValue(o1, session.getValueFactory()));
        }
    }

    public void saveEventsBatch(Journal journal) {
        org.hibernate.classic.Session hibSession = sessionFactoryBean.openSession();
        hibSession.save(journal);
        hibSession.close();
    }
}
