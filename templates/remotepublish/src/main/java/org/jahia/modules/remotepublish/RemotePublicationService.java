package org.jahia.modules.remotepublish;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.services.content.*;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventJournal;
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
public class RemotePublicationService {
    private transient static Logger logger = Logger.getLogger(RemotePublicationService.class);

    private JCRSessionFactory sessionFactory;

    private static RemotePublicationService instance;

    public static RemotePublicationService getInstance() {
        if (instance == null) {
            instance = new RemotePublicationService();
        }
        return instance;
    }

    public void start() {
    }

    public JCRSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void generateLog(JCRNodeWrapper source, Calendar calendar, OutputStream os) throws Exception {
        LogBundle bundle = new LogBundle();
        bundle.setSourceUuid(source.getIdentifier());
        bundle.setSourcePath(source.getPath());

        ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(os));

        oos.writeObject(bundle);

        final String workspace = source.getSession().getWorkspace().getName();
        JCRSessionWrapper liveSession = sessionFactory.getCurrentUserSession(workspace, null);

        EventJournal journal =
                liveSession.getProviderSession(source.getProvider()).getWorkspace().getObservationManager()
                        .getEventJournal(-1, source.getPath(), true, null, null);
        if (calendar != null) {
            journal.skipTo(calendar.getTimeInMillis());
        } else {
            journal.skipTo(source.getProperty(Constants.JCR_CREATED).getLong());
        }

        Set<String> addedPath = new HashSet<String>();

        long lastDate = 0;
        if (calendar != null) {
            lastDate = calendar.getTimeInMillis();
        }

        while (journal.hasNext()) {
            Event event = journal.nextEvent();

            String path = event.getPath();

            final LogEntry logEntry = new LogEntry(path, event.getType());
            switch (event.getType()) {
                case Event.NODE_ADDED: {
                    try {
                        JCRNodeWrapper node = liveSession.getNode(path);
                        oos.writeObject(logEntry);
                        oos.writeObject(node.getPrimaryNodeTypeName());
                    } catch (PathNotFoundException e) {
                        // not present anymore
                    }
                    break;
                }

                case Event.NODE_REMOVED: {
                    oos.writeObject(logEntry);
                    addedPath.add(path);
                    break;
                }

                case Event.PROPERTY_ADDED: {
                    String nodePath = StringUtils.substringBeforeLast(path, "/");
                    final JCRNodeWrapper node = liveSession.getNode(nodePath);
                    try {
                        final JCRPropertyWrapper property = node.getProperty(StringUtils.substringAfterLast(path, "/"));
                        if (!property.getDefinition().isProtected()) {
                            oos.writeObject(logEntry);
                            serializePropertyValue(oos, property);
                        }
                    } catch (PathNotFoundException e) {
                        logger.debug(e.getMessage(), e);
                    }
                    break;
                }

                case Event.PROPERTY_CHANGED: {
                    String nodePath = StringUtils.substringBeforeLast(path, "/");
                    try {
                        final JCRNodeWrapper node = liveSession.getNode(nodePath);
                        final JCRPropertyWrapper property = node.getProperty(StringUtils.substringAfterLast(path, "/"));
                        if (!property.getDefinition().isProtected()) {
                            oos.writeObject(logEntry);
                            serializePropertyValue(oos, property);
                        }
                    } catch (PathNotFoundException e) {
                        logger.debug(e.getMessage(), e);
                    }
                    break;
                }

                case Event.PROPERTY_REMOVED: {
                    if (!addedPath.contains(path)) {
                        oos.writeObject(logEntry);
                    }
                    break;
                }
            }
            lastDate = event.getDate();
        }

        GregorianCalendar date = new GregorianCalendar();
        date.setTimeInMillis(lastDate);
        oos.writeObject(new LogBundleEnd(date));


        oos.close();
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

        JCRTemplate.getInstance().doExecuteWithUserSession(sessionFactory.getCurrentUser().getName(), targetWorkspace,
                new JCRCallback() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        try {
                            JCRNodeWrapper target = session.getNodeByUUID(t.getIdentifier());
                            ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(in));
                            LogBundle log = (LogBundle) ois.readObject();

                            session.getPathMapping().put(log.getSourcePath(), target.getPath());
                            Set<String> addedPath = new HashSet<String>();
                            Map<String, Object> missedProperties = new HashMap<String, Object>();
                            Object o;
                            while ((o = ois.readObject()) instanceof LogEntry) {
                                LogEntry entry = (LogEntry) o;
                                String path =
                                        target.getPath() + StringUtils.substringAfter(entry.getPath(), log.getSourcePath());

                                switch (entry.getEventType()) {
                                    case Event.NODE_ADDED: {
                                        if (!addedPath.contains(path)) {
                                            String nodeType = (String) ois.readObject();
                                            if (logger.isInfoEnabled()) {
                                                logger.info("Adding Node " + path + " with nodetype: " + nodeType);
                                            }
                                            String parentPath = StringUtils.substringBeforeLast(path, "/");
                                            JCRNodeWrapper parent = session.getNode(parentPath);
                                            parent.checkout();
                                            parent.addNode(StringUtils.substringAfterLast(path, "/"), nodeType);
                                            addedPath.add(path);
                                        }
                                        break;
                                    }
                                    case Event.NODE_REMOVED: {
                                        if (logger.isInfoEnabled()) {
                                            logger.info("Removing Node " + path);
                                        }
                                        final JCRNodeWrapper node = session.getNode(path);
                                        node.getParent().checkout();
                                        node.checkout();
                                        node.remove();
                                        addedPath.remove(path);
                                        break;
                                    }
                                    case Event.PROPERTY_ADDED:
                                    case Event.PROPERTY_CHANGED: {
                                        Object o1 = ois.readObject();
                                        try {
                                            updateProperty(session, path, o1);
                                            if (path.contains("jcr:language")) {
                                                for (Map.Entry<String, Object> missedProperty : missedProperties
                                                        .entrySet()) {
                                                    updateProperty(session, missedProperty.getKey(),
                                                            missedProperty.getValue());
                                                }
                                                missedProperties.clear();
                                            }
                                        } catch (ConstraintViolationException e) {
                                            logger.debug("Issue during add/update of property " + path + " (error: " +
                                                    e.getMessage() + ")", e);
                                        } catch (PathNotFoundException e) {
                                            logger.debug("Error during add/update of property " + path + " (error: " +
                                                    e.getMessage() + ")", e);
                                            missedProperties.put(path, o1);
                                        } catch (RepositoryException e) {
                                            logger.error("Error during add/update of property " + path + " (error: " +
                                                    e.getMessage() + ")", e);
                                            throw e;
                                        }
                                        break;
                                    }
                                    case Event.PROPERTY_REMOVED: {
                                        if (logger.isDebugEnabled()) {
                                            logger.debug("Removing Property " + path);
                                        }
                                        final JCRNodeWrapper node =
                                                session.getNode(StringUtils.substringBeforeLast(path, "/"));
                                        node.checkout();
                                        try {
                                            node.getProperty(StringUtils.substringAfterLast(path, "/")).remove();
                                        } catch (PathNotFoundException e) {
                                            logger.debug("Issue during removal of property " + path + " (error: " +
                                                    e.getMessage() + ")", e);
                                        } catch (ConstraintViolationException e) {
                                            logger.debug("Issue during removal of property " + path + " (error: " +
                                                    e.getMessage() + ")", e);
                                        }
                                        break;
                                    }
                                }
                            }
                            LogBundleEnd end = (LogBundleEnd) o;

                            target.checkout();
                            target.addMixin("jmix:remotelyPublished");
                            target.setProperty("uuid", log.getSourceUuid());
                            target.setProperty("lastReplay", end.getDate());
                            session.save();
                        } catch (Exception e) {
                            throw new RepositoryException(e);
                        }
                        return null;
                    }
                });
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
            node.setProperty(propertyName, values);
        } else {
            node.setProperty(propertyName, deserializePropertyValue(o1, session.getValueFactory()));
        }
    }

}
