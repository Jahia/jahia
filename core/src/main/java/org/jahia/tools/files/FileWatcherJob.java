/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.tools.files;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.PathFileComparator;
import org.apache.commons.io.filefilter.*;
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
            long lastCheckTime, IOFileFilter ignoreFilter) {
        if (!folder.isDirectory()) {
            return Collections.emptyList();
        }
        List<File> files = null;
        if (fileOnly || !checkDate) {
            IOFileFilter fileFilter;
            IOFileFilter dirFilter;
            if (ignoreFilter != null) {
                fileFilter = checkDate ? new AndFileFilter(new AgeFileFilter(lastCheckTime, false), ignoreFilter) : ignoreFilter;
                dirFilter = recursive ? ignoreFilter : FalseFileFilter.INSTANCE;
            } else {
                fileFilter = checkDate ? new AgeFileFilter(lastCheckTime, false) : TrueFileFilter.INSTANCE;
                dirFilter = recursive ? TrueFileFilter.INSTANCE : FalseFileFilter.INSTANCE;
            }
            Collection<File> foundFiles = fileOnly ? FileUtils.listFiles(folder, fileFilter, dirFilter) : FileUtils
                    .listFilesAndDirs(folder, fileFilter, dirFilter);
            files = (foundFiles instanceof List<?>) ? (List<File>) foundFiles : new LinkedList<File>(foundFiles);
        } else {
            if (recursive) {
                try {
                    files = new Walker(lastCheckTime).execute(folder);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                Collection<File> foundFiles = FileUtils.listFilesAndDirs(folder, ignoreFilter != null ? new AndFileFilter(new AgeFileFilter(lastCheckTime, false), ignoreFilter) : null, FalseFileFilter.INSTANCE);
                files = (foundFiles instanceof List<?>) ? (List<File>) foundFiles : new LinkedList<File>(foundFiles);
            }
        }

        return files;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        FileWatcher fileWatcher = (FileWatcher) jobDataMap.get("fileWatcher");
        if (fileWatcher != null) {
            File folder = fileWatcher.getFolder();
            boolean fileOnly = fileWatcher.getFileOnly();
            boolean recursive = fileWatcher.isRecursive();
            IOFileFilter ignoreFilter = fileWatcher.getIgnoreFilter();
            List<File> changedFiles = checkFiles(folder, fileOnly, recursive, fileWatcher.getCheckDate(),
                    fileWatcher.getLastCheckTime(), ignoreFilter);
            fileWatcher.setLastCheckTime(System.currentTimeMillis());
            if (fileWatcher.getRemovedFiles()) {
                Collection<File> currentFiles;
                IOFileFilter filter = ignoreFilter != null ? ignoreFilter : TrueFileFilter.INSTANCE;
                if (fileOnly) {
                    currentFiles = FileUtils.listFiles(folder, filter, recursive ? filter : FalseFileFilter.INSTANCE);
                } else {
                    currentFiles = FileUtils.listFilesAndDirs(folder, filter, recursive ? filter : FalseFileFilter.INSTANCE);
                }
                Set<File> deletedFiles = new TreeSet<File>(new PathFileComparator());
                deletedFiles.addAll(fileWatcher.getPreviousFiles());
                deletedFiles.removeAll(currentFiles);
                changedFiles.addAll(deletedFiles);
                fileWatcher.setPreviousFiles(currentFiles);
            }

            // Notify Observers if number of files > 0
            if (changedFiles.size() > 0) {
                fileWatcher.externalSetChanged(); // Alert the Observable Object That there are change in the folder
                fileWatcher.notifyObservers(changedFiles);
            }
        }
    }
}
