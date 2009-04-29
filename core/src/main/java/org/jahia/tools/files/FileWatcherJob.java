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
