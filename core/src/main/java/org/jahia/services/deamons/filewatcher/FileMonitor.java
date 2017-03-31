/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
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
package org.jahia.services.deamons.filewatcher;

import java.io.File;
import java.io.Serializable;
import java.util.*;

/**
 * File watcher service implementation that is based on the VFS DefaultFileMonitor.
 * 
 * @author Sergiy Shyrkov
 * @see <a href="http://commons.apache.org/vfs">Commons VFS project</a>
 */
public class FileMonitor implements Serializable {

    /**
     * File monitor agent.
     */
    private static final class FileMonitorAgent implements Serializable {
        private static final long serialVersionUID = -4615339614778628865L;
        private Map<File, Object> children;
        private boolean exists;
        private final File file;

        private final FileMonitor fm;
        private boolean isFile;
        private long timestamp;
        private long length;

        private FileMonitorAgent(FileMonitor fm, File file) {
            this.fm = fm;
            this.file = file;

            this.resetChildrenList();

            this.exists = this.file.exists();

            if (this.exists) {
                this.timestamp = this.file.lastModified();
                this.length = this.file.length();
                this.isFile = file.isFile();
            }
        }

        private void check(FileListener listener) {
            // If the file existed and now doesn't
            if (this.exists && !this.file.exists()) {
                this.exists = this.file.exists();
                this.timestamp = -1;
                this.length = 0;

                // Fire delete event

                if (!fm.onlyFiles || this.isFile) {
                    listener.fileDeleted(this.file);
                }

                // Remove from map
                this.fm.queueRemoveFile(this.file);
            } else if (this.exists && this.file.exists()) {

                // Check the timestamp to see if it has been modified
                long lastModified = this.file.lastModified();
                long length = this.file.length();
                if (this.timestamp != lastModified || this.length != length) {
                    this.timestamp = lastModified;
                    this.length = length;
                    // Fire change event

                    // Don't fire if it's a folder because new file children
                    // and deleted files in a folder have their own event triggered.
                    if (!this.file.isDirectory()) {
                        listener.fileChanged(this.file);
                    }
                }

            } else if (!this.exists && this.file.exists()) {
                this.exists = this.file.exists();
                this.timestamp = this.file.lastModified();
                this.length = this.file.length();
                // Don't fire if it's a folder because new file children
                // and deleted files in a folder have their own event triggered.
                if (!this.file.isDirectory()) {
                    listener.fileCreated(this.file);
                }
            }

            this.checkForNewChildren(listener);
        }

        /**
         * Only checks for new children. If children are removed, they'll eventually be checked.
         * 
         * @param listener
         */
        private void checkForNewChildren(FileListener listener) {
            if (this.file.isDirectory()) {
                File[] newChildren = this.file.listFiles();
                if (newChildren != null && this.children != null) {
                    // See which new children are not listed in the current children map.
                    Map<File, Object> newChildrenMap = new HashMap<File, Object>();
                    Stack<File> missingChildren = new Stack<File>();

                    for (final File newChild : newChildren) {
                        // only procces child if it isn't ignored
                        if (!fm.isIgnored(newChild)) {
                            newChildrenMap.put(newChild, new Object()); // null ?
                            // If the child's not there
                            if (!this.children.containsKey(newChild)) {
                                missingChildren.push(newChild);
                            }
                        }
                    }

                    this.children = newChildrenMap;

                    // If there were missing children
                    if (!missingChildren.empty()) {

                        while (!missingChildren.empty()) {
                            File child = missingChildren.pop();
                            this.fireAllCreate(child, listener);
                        }
                    }

                } else if (newChildren != null) {
                    // First set of children - Break out the cigars
                    if (newChildren.length > 0) {
                        this.children = new HashMap<File, Object>();
                    }
                    for (final File newChild : newChildren) {
                        // only process child if it isn't ignored
                        if (!fm.isIgnored(newChild)) {
                            this.children.put(newChild, new Object()); // null?
                            this.fireAllCreate(newChild, listener);
                        }
                    }
                }
            }
        }

        /**
         * Recursively fires create events for all children if recursive descent is enabled. Otherwise the create event is only fired for
         * the initial FileObject.
         * 
         * @param child
         *            The child to add.
         * @param listener
         *            the file listener to notify
         */
        private void fireAllCreate(File child, FileListener listener) {
            if (!fm.onlyFiles || child.isFile()) {
                listener.fileCreated(child);
            }

            this.fm.queueAddFile(child); // Add

            if (this.fm.isRecursive()) {
                if (child.isDirectory()) {
                    File[] newChildren = child.listFiles();
                    if (newChildren != null) {
                        for (int i = 0; i < newChildren.length; i++) {
                            fireAllCreate(newChildren[i], listener);
                        }
                    }
                }
            }
        }

        private void resetChildrenList() {
            if (this.file.isDirectory()) {
                this.children = new HashMap<File, Object>();
                File[] childrenList = this.file.listFiles();
                if (childrenList != null) {
                    for (int i = 0; i < childrenList.length; i++) {
                        this.children.put(childrenList[i], new Object()); // null?
                    }
                }
            }
        }

    }

    private static final long DEFAULT_DELAY = 1000;

    private static final int DEFAULT_MAX_FILES = 1000;

    private static final long serialVersionUID = -7377571336405032918L;

    /**
     * File objects to be added to the monitor map.
     */
    private final Stack<File> addStack = new Stack<File>();

    private final FileMonitorCallback callback;

    /**
     * Set the number of files to check until a delay will be inserted
     */
    private int checksPerRun = DEFAULT_MAX_FILES;

    /**
     * Set the delay between checks
     */
    private long delay = DEFAULT_DELAY;

    /**
     * File objects to be removed from the monitor map.
     */
    private final Stack<File> deleteStack = new Stack<File>();

    private Set<String> filesToIgnore;

    /**
     * Map from FileName to FileObject being monitored.
     */
    private final Map<File, FileMonitorAgent> monitorMap = new HashMap<File, FileMonitorAgent>();

    private boolean onlyFiles;

    /**
     * A flag used to determine if adding files to be monitored should be recursive.
     */
    private boolean recursive;

    /**
     * Initializes an instance of this class.
     */
    public FileMonitor() {
        this(null);
    }

    public FileMonitor(final FileMonitorCallback callback) {
        super();
        this.callback = callback;
    }

    /**
     * Adds a file to be monitored.
     * 
     * @param file
     *            The FileObject to monitor.
     */
    public void addFile(final File file) {
        synchronized (this.monitorMap) {
            if (this.monitorMap.get(file) == null && !isIgnored(file)) {
                this.monitorMap.put(file, new FileMonitorAgent(this, file));

                if (this.recursive && file.isDirectory()) {
                    // Traverse the children
                    final File[] children = file.listFiles();
                    if (children != null) {
                        for (int i = 0; i < children.length; i++) {
                            this.addFile(children[i]); // Add depth first
                        }
                    }
                }
            }
        }
    }

    /**
     * get the number of files to check per run.
     * 
     * @return The number of files to check per iteration.
     */
    public int getChecksPerRun() {
        return checksPerRun;
    }

    /**
     * Get the delay between runs.
     * 
     * @return The delay period.
     */
    public long getDelay() {
        return delay;
    }

    public Set<String> getFilesToIgnore() {
        return filesToIgnore;
    }

    protected boolean isIgnored(File file) {
        return filesToIgnore != null && filesToIgnore.contains(file.getName());
    }

    /**
     * Access method to get the recursive setting when adding files for monitoring.
     * 
     * @return true if monitoring is enabled for children.
     */
    public boolean isRecursive() {
        return this.recursive;
    }

    /**
     * Queues a file for addition to be monitored.
     * 
     * @param file
     *            The FileObject to add.
     */
    protected void queueAddFile(final File file) {
        this.addStack.push(file);
    }

    /**
     * Queues a file for removal from being monitored.
     * 
     * @param file
     *            The File to be removed from being monitored
     */
    protected void queueRemoveFile(final File file) {
        this.deleteStack.push(file);
    }

    /**
     * Removes a file from being monitored.
     * 
     * @param file
     *            The File to remove from monitoring
     */
    public void removeFile(final File file) {
        synchronized (this.monitorMap) {
            if (this.monitorMap.get(file) != null) {
                File parent = file.getParentFile();

                this.monitorMap.remove(file);

                if (parent != null) { // Not the root
                    FileMonitorAgent parentAgent = this.monitorMap.get(parent);
                    if (parentAgent != null) {
                        parentAgent.resetChildrenList();
                    }
                }
            }
        }
    }

    /**
     * Asks the agent for each file being monitored to check its file for changes.
     */
    public void run() {
        FileMonitorResult result = new FileMonitorResult();
        while (!this.deleteStack.empty()) {
            this.removeFile(this.deleteStack.pop());
        }

        // For each entry in the map
        Object[] fileNames;
        synchronized (this.monitorMap) {
            fileNames = this.monitorMap.keySet().toArray();
        }
        for (int iterFileNames = 0; iterFileNames < fileNames.length; iterFileNames++) {
            File fileName = (File) fileNames[iterFileNames];
            FileMonitorAgent agent;
            synchronized (this.monitorMap) {
                agent = this.monitorMap.get(fileName);
            }
            if (agent != null) {
                agent.check(result);
            }

            if (getChecksPerRun() > 0 && delay > 0 && iterFileNames != 0) {
                if ((iterFileNames % getChecksPerRun()) == 0) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        // Woke up.
                    }
                }
            }
        }

        while (!this.addStack.empty()) {
            this.addFile(this.addStack.pop());
        }

        if (!result.isEmpty() && callback != null) {
            callback.process(result);
        }
    }

    /**
     * set the number of files to check per run. a additional delay will be added if there are more files to check
     * 
     * @param checksPerRun
     *            a value less than 1 will disable this feature
     */
    public void setChecksPerRun(int checksPerRun) {
        this.checksPerRun = checksPerRun;
    }

    /**
     * Set the delay between runs.
     * 
     * @param delay
     *            The delay period.
     */
    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void setFilesToIgnore(Set<String> filesToIgnore) {
        this.filesToIgnore = filesToIgnore != null && filesToIgnore.isEmpty() ? null : filesToIgnore;
    }

    public void setFilesToIgnore(String... filesToIgnore) {
        if (filesToIgnore == null || filesToIgnore.length == 0) {
            setFilesToIgnore((Set<String>) null);
        } else {
            setFilesToIgnore(new HashSet<String>(Arrays.asList(filesToIgnore)));
        }
    }

    public void setOnlyFiles(boolean onlyFiles) {
        this.onlyFiles = onlyFiles;
    }

    /**
     * Access method to set the recursive setting when adding files for monitoring.
     * 
     * @param newRecursive
     *            true if monitoring should be enabled for children.
     */
    public void setRecursive(final boolean newRecursive) {
        this.recursive = newRecursive;
    }

}
