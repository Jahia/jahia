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

//
//
//  JahiaFileWatcherBaseService
//
//  NK      12.01.2001
//
//


package org.jahia.services.deamons.filewatcher;


import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observer;

import org.jahia.exceptions.JahiaException;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.tools.files.FileWatcher;


/**
 * This Service hold a pool of instance of jahia.tools.FileWatcher Class.
 * Each Thread are identified by a name and accessible through this name.
 * Threads are added in an Map registry.
 *
 * @author Khue ng
 * @version 1.0
 */
public class JahiaFileWatcherBaseService extends JahiaFileWatcherService {

    private static org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger (JahiaFileWatcherBaseService.class);

    /** The singelton instance of this class * */
    private static JahiaFileWatcherBaseService m_Instance = null;

    private SchedulerService schedulerService;

    /**
     * The Pool of Threads *
     */
    private Map m_Registry;


    /**
     * Protected Constructor
     */
    protected JahiaFileWatcherBaseService () {
        m_Registry = new HashMap();

    }

    /**
     * Use this method to get an instance of this class
     */
    public static synchronized JahiaFileWatcherBaseService getInstance () {

        if (m_Instance == null) {
            m_Instance = new JahiaFileWatcherBaseService ();
        }
        return m_Instance;
    }

    /**
     * addFileWatcher
     *
     * @param threadName the Name to identify this thread
     * @param fullFolderPath the real path to the folder to watch
     * @param checkDate check new file by last modif date or not
     * @param interval the interval in millis
     * @param fileOnly if true, only files will be watched, not directories.
     */
    public synchronized void addFileWatcher (String threadName,
                                             String fullFolderPath,
                                             boolean checkDate,
                                             long interval,
                                             boolean fileOnly
                                             ) throws JahiaException {
        try {

            FileWatcher fw = new FileWatcher (fullFolderPath,
                    checkDate,
                    interval,
                    fileOnly,
                    schedulerService);

            m_Registry.put (threadName, fw);

            //fw.start();

        } catch (IOException e) {

            logger.error ("addFileWatcher:: " + e.getMessage (), e);

            throw new JahiaException ("JahiaFileWatcherBaseService", "failed adding File Watcher",
                    JahiaException.SERVICE_ERROR, JahiaException.WARNING_SEVERITY, e);
        }

    }

    /**
     * Call the start method of the thread
     *
     * @param threadName the Name to identify this thread
     */
    public void startFileWatcher (String threadName) throws JahiaException {

        try {

            synchronized (this) {

                getFileWatcher (threadName).start ();
            }

        } catch (IOException e) {
            logger.error ("startFileWatcher:: " + e.getMessage (), e);

            throw new JahiaException ("JahiaFileWatcherBaseService", "failed starting File Watcher",
                    JahiaException.SERVICE_ERROR, JahiaException.WARNING_SEVERITY, e);
        }
    }

    /**
     * Stops the file watcher thread. Should be called when shutting down the
    * application.
     * @param threadName String the name of the thread to shutdown.
     * @throws JahiaException
     */
    public void stopFileWatcher (String threadName)
        throws JahiaException {

        synchronized (this) {
            getFileWatcher(threadName).stop();
        }
    }


    /**
     * Register an Observer Thread with an Observable Thread
     *
     * @param threadName the Name of Observable object
     * @param obs        the observer object
     */
    public void registerObserver (String threadName,
                                  Observer obs
                                  ) {
        synchronized (this) {
            getFileWatcher (threadName).addObserver (obs);
        }
    }

    public void start() {}

    /**
     * Code to clean up the services ressource here. Override this method
     * with specific services shutdown codes.
     *
     * @exception   JahiaException
     *      Raise an JahiaException exception on any failure.
     */
    public synchronized void stop ()
        throws JahiaException
    {
        Iterator fileWatcherIter = m_Registry.values().iterator();
        while (fileWatcherIter.hasNext()) {
            FileWatcher curFileWatcher = (FileWatcher) fileWatcherIter.next();
            curFileWatcher.stop();
        }
    }

    /**
     * getFileWatcher
     *
     * @param threadName the Name to identify this thread
     *
     * @return (FileWatcher) return the FileWatcher Thread or null if
     *         not in registry
     */
    public synchronized FileWatcher getFileWatcher (String threadName) {

        return (FileWatcher) m_Registry.get (threadName);

    }

    public SchedulerService getSchedulerService() {
        return schedulerService;
    }

    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }
}
