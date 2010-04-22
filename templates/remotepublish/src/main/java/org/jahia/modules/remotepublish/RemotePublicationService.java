package org.jahia.modules.remotepublish;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;

import javax.jcr.RepositoryException;
import java.io.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
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
        bundle.setDate(new GregorianCalendar());
        bundle.setSourceUuid(source.getIdentifier());
        bundle.setEntries(Arrays.asList(new LogEntry(source.getPath(), 1, null)));
        ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(os));
        oos.writeObject(bundle);
        oos.close();
    }

    public void replayLog(JCRNodeWrapper target, InputStream in)
            throws IOException, ClassNotFoundException, RepositoryException {
        ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(in));
        LogBundle log = (LogBundle) ois.readObject();


        List<LogEntry> l = log.getEntries();

        for (LogEntry entry : l) {
            System.out.println("-- entry " + entry.getPath() + "/" + entry.getEventType());
        }

        target.addMixin("jmix:remotelyPublished");
        target.setProperty("uuid", log.getSourceUuid());
        target.setProperty("lastReplay", log.getDate());
        target.getSession().save();
    }

}
