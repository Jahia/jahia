package org.jahia.modules.remotepublish;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventJournal;
import java.io.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
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
                        if (!addedPath.contains(path)) {
                            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            liveSession.exportDocumentView(event.getPath(), baos, false, true);
                            oos.writeObject(logEntry);
                            oos.writeObject(baos.toByteArray());
                            addedPath.add(path);
                        }
                    } catch (PathNotFoundException e) {
                        // not present anymore
                    }
                    break;
                }
                case Event.NODE_REMOVED: {
                    oos.writeObject(logEntry);
                    addedPath.remove(path);
                    break;
                }
                case Event.PROPERTY_ADDED: {
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

    public void replayLog(JCRNodeWrapper target, InputStream in)
            throws IOException, ClassNotFoundException, RepositoryException {

        final String targetWorkspace = target.getSession().getWorkspace().getName();

        JCRSessionWrapper session = sessionFactory.getCurrentUserSession(targetWorkspace, null);

        target = session.getNodeByUUID(target.getIdentifier());
        ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(in));
        LogBundle log = (LogBundle) ois.readObject();

        session.getPathMapping().put(log.getSourcePath(), target.getPath());

        Object o;
        while ((o = ois.readObject()) instanceof LogEntry) {
            LogEntry entry = (LogEntry) o;
            String path = target.getPath() + StringUtils.substringAfter(entry.getPath(), log.getSourcePath());

            switch (entry.getEventType()) {
                case Event.NODE_ADDED: {
                    byte[] b = (byte[]) ois.readObject();
                    path = StringUtils.substringBeforeLast(path, "/");
                    session.getNode(path).checkout();
                    session.importXML(path, new ByteArrayInputStream(b),
                            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
                    break;
                }
                case Event.NODE_REMOVED: {
                    final JCRNodeWrapper node = session.getNode(path);
                    node.getParent().checkout();
                    node.checkout();
                    node.remove();
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
    }

}
