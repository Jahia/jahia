/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.deamons.filewatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.id.IdentifierGeneratorFactory;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.cluster.ClusterListener;
import org.jahia.services.cluster.ClusterMessage;
import org.jahia.services.cluster.ClusterService;
import org.jahia.services.scheduler.SchedulerService;
import org.jgroups.Address;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 19 avr. 2006
 * Time: 16:02:07
 * To change this template use File | Settings | File Templates.
 */
public class FileListSync implements ClusterListener {

    private static Logger logger = Logger.getLogger(FileListSync.class);
    
    public static class FileListCheckerJob implements Job {

        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            FileListSync.getInstance().trigger();
        }
    }
    
    private static FileListSync instance;

    public static final String CONTEXT_PATH = "contextPath";
    public static final String PATH = "path";

    private ClusterService clusterService;
    private SchedulerService schedulerService;

    private Map entries = new HashMap();
    private Map removed = new HashMap();
    private Map removedBack = new HashMap();

    private String contextPath;

    private boolean initialized = false;
    private Set pathsToSync;
    private String key;
    private long syncDelay;
    private String syncUrl;

    public FileListSync() {
        key = IdentifierGeneratorFactory.newInstance().uuidVersionFourGenerator().nextIdentifier().toString();
    }

    public static FileListSync getInstance() {
        if (instance == null) {
            instance = new FileListSync();
        }
        return instance;
    }

    public String getKey() {
        return key;
    }

    public void setClusterService(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    public void setPathsToSync(Set pathsToSync) {
        this.pathsToSync = pathsToSync;
    }

    public void setSyncDelay(long syncDelay) {
        this.syncDelay = syncDelay;
    }

    public String getSyncUrl() {
        return syncUrl;
    }

    public void setSyncUrl(String syncUrl) {
        this.syncUrl = syncUrl;
    }

    public void replaceEntries(Map newEntries) {
        if (clusterService.isActivated()) {
            removed = entries;
            removed.keySet().removeAll(newEntries.keySet());
            for (Iterator iterator = removed.keySet().iterator(); iterator.hasNext();) {
                removedBack.put(iterator.next(), new Date());
            }
        }

        entries = newEntries;
        initialized = true;

    }

    public void sendMessage() {
        if (!clusterService.isActivated())
              return;
        try {
            if (syncUrl != null) {
                URL url = new URL(syncUrl + Jahia.getContextPath() + "/getFile?key="+key);
                clusterService.sendMessage(new ClusterMessage(new FileListMessage(entries, removed, url)));
            }
        } catch (MalformedURLException e) {
            logger.warn("Malformed URL", e);
        }
    }

    public void start() {
        if (!clusterService.isActivated()) {
            return;
        }

        contextPath = Jahia.getStaticServletConfig().getServletContext().getRealPath("/");

        if (clusterService.isActivated())
            clusterService.addListener(this);

        trigger();

        if (org.jahia.settings.SettingsBean.getInstance().isDevelopmentMode() && syncDelay > 0) {
            JobDetail jobDetail = new JobDetail("Checkfiles_Job", Scheduler.DEFAULT_GROUP,
                    FileListCheckerJob.class);
            JobDataMap jobDataMap = new JobDataMap();
            jobDetail.setJobDataMap(jobDataMap);

            Trigger trigger = new SimpleTrigger("Checkfiles_Trigger",
                    Scheduler.DEFAULT_GROUP,
                    SimpleTrigger.REPEAT_INDEFINITELY,
                    syncDelay);
            // not persisted Job and trigger
            trigger.setVolatility(true);
            jobDetail.setRequestsRecovery(false);
            jobDetail.setDurability(false);
            jobDetail.setVolatility(true);

            try {
                schedulerService.scheduleRamJob(jobDetail, trigger);
            } catch (JahiaException je) {
                logger.error("Error while scheduling file watch", je);
            }
        }
    }

    public void trigger() {
        if (!clusterService.isActivated()) {
            return;
        }
        Map newEntries = new HashMap();

        synchronized(instance) {
            for (Iterator iterator = pathsToSync.iterator(); iterator.hasNext();) {
                String s = (String) iterator.next();
                int starIndex = s.indexOf('*');
                int slashIndex = s.lastIndexOf('/');
                FilenameFilter filter = null;
                if (starIndex > slashIndex) {
                    // TODO use commons-io 1.2 to get a better wildcard filter
                    filter = new SuffixFileFilter(s.substring(starIndex+1));
                    s = s.substring(0, slashIndex);
                }
                getFileList(newEntries, s, filter);
            }
            instance.replaceEntries(newEntries);
        }
        instance.sendMessage();
    }

    private void getFileList(Map entries, String path, FilenameFilter filter) {
        final String s = contextPath + path;
        logger.debug("path to list: " + s);
        File folder = new File(s);
        File[] files = folder.listFiles();
        if (files == null) logger.warn("Cannot list files under " + s);
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String subpath = file.getAbsolutePath().substring(contextPath.length());
                if (file.isFile()) {
                    if (filter == null || filter.accept(file.getParentFile(), file.getName())) {
                        entries.put(subpath, new Date(file.lastModified()));
                    }
                } else if (file.isDirectory()) {
                    getFileList(entries, subpath, filter);
                }
            }
        }
    }

    public synchronized void messageReceived(ClusterMessage message) {
        int removed = 0;
        int updated = 0;
        int created = 0;
        if (message.getObject() instanceof FileListMessage) {

            if (!initialized) {
                // Not yet initialized
                return;
            }

            FileListMessage m = (FileListMessage) message.getObject();

            Set set = new HashSet(entries.keySet());
            Iterator i = set.iterator();
            while (i.hasNext()) {
                String fname = (String) i.next();
                Date date = (Date) m.entries.remove(fname);
                if (date == null) {
                    Date removedDate = (Date) m.removed.get(fname);
                    if (removedDate == null) {
                        // ..
                    } else {
                        // remove the file
                        logger.debug("Remove --> "+fname);
                        removed ++;
                        File f = new File(contextPath + fname);
                        f.delete();
                        entries.remove(fname);
                    }
                } else {
                    if (date.after( (Date) entries.get(fname))) {
                        // copy file
                        logger.debug("Copy updated --> "+fname);
                        updated ++;
                        copyFile(fname, date, m.url, false);
                    }
                }
            }
            for (Iterator iterator = m.entries.keySet().iterator(); iterator.hasNext();) {
                String fname = (String) iterator.next();
                Date date = (Date) m.entries.get(fname);

                if (removedBack.get(fname) != null && ((Date)removedBack.get(fname)).after(date)) {
                    // this file has been deleted, resent info
                    entries.put(fname, new Date());
                } else {
                    // copy
                    logger.debug("Copy missing --> "+fname);
                    created ++;
                    copyFile(fname, date, m.url, true);
                }
            }
            if (removed + updated + created>0) {
                logger.info("Files synchronization from " + m.url.getHost()+":"+m.url.getPort() + " finished, new files created : " + created + ", updated : " + updated + ", removed : " + removed);
            }
        }
    }

    private void copyFile(String fname, Date date, URL u, boolean newFile) {
        OutputStream os = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(u.getProtocol(), u.getHost(), u.getPort(), u.getFile() + "&filename="+fname);
            urlConnection = (HttpURLConnection) url.openConnection();
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpServletResponse.SC_OK) {
                InputStream is = urlConnection.getInputStream();

                File f = new File(contextPath + fname);
                f.getParentFile().mkdirs();
                if (newFile) {
                    f.createNewFile();
                }
                os = new FileOutputStream(f);

                byte[] buff = new byte[2048];
                int in;
                while ( (in=is.read(buff))>0) {
                    os.write(buff,0,in);
                }
                f.setLastModified(date.getTime());

                entries.put(fname, date);
            } else {
                logger.error("Cannot synchronize file, received status code="+responseCode);
            }
        } catch (Exception e) {
            logger.error("Cannot synchronize file",e);
        } finally {
            try {
                if(os != null) os.close();
            } catch (IOException e) {
                logger.error("Cannot synchronize file",e);
            } finally {
                if(urlConnection != null) urlConnection.disconnect();
            }
        }
    }

    public void memberJoined(Address address) {
    }

    public void memberLeft(Address address) {
    }

    public ClusterService getClusterService() {
        return clusterService;
    }


}
