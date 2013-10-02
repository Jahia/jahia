/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Background job that allows to watch files for modifications and call appropriate observers.
 */
public class FileWatcherJob implements StatefulJob {

    private static class Walker extends DirectoryWalker<File> {

        private AgeFileFilter ageFileFilter;
        private List<File> results = new LinkedList<File>();

        Walker(long newerThan) {
            super();
            ageFileFilter = new AgeFileFilter(newerThan, false);
        }

        List<File> execute(File startFolder) throws IOException {
            walk(startFolder, results);
            return results;
        }

        protected boolean handleDirectory(File directory, int depth, Collection<File> results) throws IOException {
            if (ageFileFilter.accept(directory)) {
                results.add(directory);
            }
            return true;
        }

        protected void handleFile(File file, int depth, Collection<File> results) throws IOException {
            if (ageFileFilter.accept(file)) {
                results.add(file);
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(FileWatcherJob.class);;

    /**
     * Checks new files and builds the List of files to pass to Observers
     * 
     * @param folder
     *            the root folder where to watch files
     * @param fileOnly
     *            if <code>true</code> we consider only files; otherwise we also consider folders
     * @param recursive
     *            should we recurse into sub-folders
     * @param checkDate
     *            do we need to check the last modification date or in case of <code>false</code> value just return all found files
     * @param lastCheckTime
     *            the last check time
     * @return a list of files and folders matching the provided criteria
     */
    protected List<File> checkFiles(File folder, boolean fileOnly, boolean recursive, boolean checkDate,
            long lastCheckTime) {
        if (!folder.isDirectory()) {
            return Collections.emptyList();
        }
        List<File> files = null;
        if (fileOnly || !checkDate) {
            IOFileFilter fileFilter = checkDate ? new AgeFileFilter(lastCheckTime, false) : TrueFileFilter.INSTANCE;
            IOFileFilter dirFilter = recursive ? TrueFileFilter.INSTANCE : FalseFileFilter.INSTANCE;
            Collection<File> foundFiles = fileOnly ? FileUtils.listFiles(folder, fileFilter, dirFilter) : FileUtils
                    .listFilesAndDirs(folder, fileFilter, dirFilter);
            files = (foundFiles instanceof List<?>) ? (List<File>) foundFiles : new LinkedList<File>(foundFiles);
        } else {
            try {
                files = new Walker(lastCheckTime).execute(folder);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return files;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        FileWatcher fileWatcher = (FileWatcher) jobDataMap.get("fileWatcher");
        if (fileWatcher != null) {
            List<File> newFiles = checkFiles(fileWatcher.getFolder(), fileWatcher.getFileOnly(),
                    fileWatcher.isRecursive(), fileWatcher.getCheckDate(), fileWatcher.getLastCheckTime());
            fileWatcher.setLastCheckTime(System.currentTimeMillis());
            // Notify Observers if number of files > 0
            if (newFiles.size() > 0) {
                fileWatcher.externalSetChanged(); // Alert the Observable Object That there are change in the folder
                fileWatcher.notifyObservers(newFiles);
            }
        }
    }
}
