/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
//
//
//  JahiaFileWatcherService
//
//  NK      12.01.2001
//
//


package org.jahia.services.deamons.filewatcher;


import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;
import org.jahia.tools.files.FileWatcher;

import java.util.Observer;


/**
 * This Service hold a pool of instance of jahia.tools.FileWatcher Class.
 * Each Thread are identified by a name and accessible through this name.
 * Threads are added in an Map registry.
 *
 * @author Khue ng
 * @version 1.0
 */
public abstract class JahiaFileWatcherService extends JahiaService {


    /**
     * addFileWatcher
     *
     * @param (String)  threadName, the Name to identify this thread
     * @param (String)  fullFolderPath, the real path to the folder to watch
     * @param (boolean) checkDate, check new fle by last modif date
     * @param (long)    intetal, the interval in millis
     */
    public abstract void addFileWatcher (String threadName,
                                         String fullFolderPath,
                                         boolean checkDate,
                                         long interval,
                                         boolean fileOny
                                         ) throws JahiaException;


    /**
     * Call the start method of the thread
     *
     * @param (String) threadName, the Name to identify this thread
     */
    public abstract void startFileWatcher (String threadName) throws JahiaException;

    /**
     * Stops the file watcher thread. Should be called when shutting down the
    * application.
     * @param threadName String the name of the thread to shutdown.
     * @throws JahiaException
     */
    public abstract void stopFileWatcher(String threadName) throws JahiaException;


    /**
     * Register an Observer Thread with an Observable Thread
     *
     * @param (String)   threadName, the Name of Observable object
     * @param (Observer) the observer object
     */
    public abstract void registerObserver (String threadName,
                                           Observer obs
                                           );

    /**
     * getFileWatcher
     *
     * @param threadName the Name to identify this thread
     *
     * @return (FileWatcher) return the FileWatcher Thread or null if
     *         not in registry
     */
    public abstract FileWatcher getFileWatcher (String threadName);

}
