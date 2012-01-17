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
