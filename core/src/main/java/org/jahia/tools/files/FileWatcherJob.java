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

 package org.jahia.tools.files;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileWatcherJob implements StatefulJob {
    public FileWatcherJob () {
    }

    public void execute (JobExecutionContext context)
        throws JobExecutionException {

        //logger.debug("Checking files in directory " + m_Folder.toString() + "...");
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        FileWatcher fileWatcher = (FileWatcher) jobDataMap.get("fileWatcher");
        if ( fileWatcher != null ){

            /** The List of new files **/
            List newFiles = checkFiles(fileWatcher.getFolder(),
                                         fileWatcher.getFileOnly(),
                                         fileWatcher.getCheckDate(),
                                         fileWatcher.getLastCheckTime());

            // Notify Observers if number of files > 0
            if (newFiles.size() > 0) {
                fileWatcher.externalSetChanged(); // Alert the Observable Object That there are change in the folder
                fileWatcher.notifyObservers(newFiles);
            }
        }
    }

    /**
     * Checks new files and builds the List of files
     * to pass to Observers
     *
     */
    protected List checkFiles (File folder, boolean fileOnly,
                                 boolean checkDate, long lastCheckTime) {

        List newFiles = new ArrayList();
        if (folder.isDirectory()) {

            if (!checkDate) {

                File[] files = folder.listFiles();
                if (files == null) {
                    return newFiles;
                }
                int size = files.length;

                for (int i = 0; i < size; i++) {
                    // logger.debug("FileWatcher found new file " + files[i].getName() );
                    if (files[i].canWrite()) {
                        if (!fileOnly) {
                            newFiles.add(files[i]);
                        } else if (files[i].isFile()) {
                            newFiles.add(files[i]);
                        }
                    }
                }

            } else {

                File[] files = folder.listFiles();
                if (files == null) {
                    return newFiles;
                }
                int size = files.length;

                for (int i = 0; i < size; i++) {

                    if (files[i].lastModified() > lastCheckTime) {
                        newFiles.add(files[i]);
                    }
                }

            }

        }
        return newFiles;

    }
}
