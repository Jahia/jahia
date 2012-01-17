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
//  FileWatcher
//
//  NK      12.01.2001
//
//


package org.jahia.tools.files;

import org.jahia.services.scheduler.SchedulerService;
import org.quartz.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Observable;

/**
 * An Observer/Observable Implementation of a Deamon Thread
 * that looks at any new File created in a gived Folder.
 * New files are checked by their last modif date.
 *
 * Build a list of files and pass it to any Registered Observer.<br>
 *
 * <pre>
 * Works in two modes : mode = ALL           -> returns all files in the folder.
 *                      mode = CHECK_DATE    -> returns only new files.
 * </pre>
 *
 * @author Khue ng
 * @version 1.0
 */
public class FileWatcher extends Observable implements Serializable {

    private static org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(FileWatcher.class);

    /** The Full Real Path to the Folder to Watch **/
    private String m_FolderPath = "";

    /** The Abstract File object of the Folder to watch **/
    private File m_Folder;

    /** Define at what interval the folder must be checked, in millis **/
    private long m_Interval;

    /** The internal Thread Timer **/
    private JobDetail jobDetail;
    private Trigger trigger;

    private String jobName;
    private int maxJobNameLength = 50;

    /** Both file and directory or only file **/
    private boolean m_FileOnly = true;

    /** Define if the Thread is User or Deamon Thread **/
    private boolean m_IsDeamon = true;

    /** Check files by their last modif date status or not **/
    public boolean mCheckDate = false;

    /** the Last Time the Folder was checked **/
    private long m_LastCheckTime;

    private SchedulerService schedulerService;
    /**
     * Constructor
     *
     * @param fullFolderPath  the real Path to the folder to watch
     * @param checkDate check by last modif date or not
     * @param interval the interval to do the repeat Task
     * @param fileOnly checks only files if true, not directories
     * @exception IOException
     */
    public FileWatcher( String fullFolderPath,
                        boolean checkDate,
                        long interval,
                        boolean fileOnly,
                        SchedulerService schedulerService)
            throws IOException {

        setFolderPath(fullFolderPath);
        setCheckDate(checkDate);
        setInterval(interval);
        setFileOnly(fileOnly);
        this.schedulerService = schedulerService;
    }

    /**
     * Constructor
     * Precise if the Thread to Create is a Deamon or not
     *
     * @param fullFolderPath the real Path to the folder to watch
     * @param checkDate check new files by date changes ?
     * @param interval the interval to do the repeat Task
     * @param isDeamon create a User or Deamon Thread
     * @param fileOnly checks only files if true, not directories
     * @param schedulerService a dependency injection of the service to use to schedule the file watching job.
     * @exception IOException
     */
    public FileWatcher( String fullFolderPath,
                        boolean checkDate,
                        long interval,
                        boolean fileOnly,
                        boolean isDeamon,
                        SchedulerService schedulerService
    )
            throws IOException {

        setFolderPath(fullFolderPath);
        setCheckDate(checkDate);
        setInterval(interval);
        setFileOnly(fileOnly);
        setDeamon(isDeamon);
        this.schedulerService = schedulerService;
    }

    /**
     * Creates the Timer Thread and starts it
     *
     * @throws IOException
     */
    public void start ()
            throws IOException {

        initialize();

        logger.debug("Time created, Check Interval=" + getInterval() +
                " (millis) ");

        jobDetail = new JobDetail(jobName + "_Job", Scheduler.DEFAULT_GROUP,
                FileWatcherJob.class);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("fileWatcher", this);
        jobDetail.setJobDataMap(jobDataMap);

        trigger = new SimpleTrigger(jobName + "_Trigger",
                Scheduler.DEFAULT_GROUP,
                SimpleTrigger.REPEAT_INDEFINITELY,
                m_Interval);
        // not persisted Job and trigger
        trigger.setVolatility(true);
        jobDetail.setRequestsRecovery(false);
        jobDetail.setDurability(false);
        jobDetail.setVolatility(true);

        try {
            schedulerService.getRAMScheduler().deleteJob(jobName + "_Job", Scheduler.DEFAULT_GROUP);
        } catch (SchedulerException e) {
        }
        try {
            schedulerService.getRAMScheduler().scheduleJob(jobDetail, trigger);
        } catch (SchedulerException je) {
            logger.error("Error while scheduling file watch for " + m_FolderPath, je);
        }

    }

    public void stop() {
    }

    /**
     * Returns The Interval Values
     *
     * @return (long) the interval
     */
    public long getInterval(){
        return m_Interval;
    }

    /**
     * Set The Interval Values
     *
     * @param interval the interval used to do repeatitive task in millis
     */
    protected void setInterval(long interval){
        m_Interval = interval;
    }

    /**
     * Small trick to export the setChanged method that has a protected. This
     * is necessary for compilation with Ant...
     */
    public void externalSetChanged() {
        setChanged();
    }

    /**
     * Returns The Path of the Folder to watch
     *
     * @return (String) the path to the folder to watch
     */
    public String getFolderPath(){
        return m_FolderPath;
    }

    /**
     * Set The FolderPath
     *
     * @param fullFolderPath the path to the folder to watch
     */
    protected void setFolderPath( String fullFolderPath ){
        m_FolderPath = fullFolderPath;
        jobName = m_FolderPath;
        int jobNameLength = jobName.length();
        if (jobNameLength > maxJobNameLength) {
            int jobNameHashCode = jobName.hashCode();
            String jobNameHashCodeStr = Integer.toString(jobNameHashCode);
            jobName = "..." +
                    jobName.substring(jobNameLength - maxJobNameLength + 4 + jobNameHashCodeStr.length())
                    + jobNameHashCodeStr;
        }
    }

    /**
     * set file only mode
     *
     * @param fileOnly file only or not
     */
    public void setFileOnly(boolean fileOnly){
        m_FileOnly = fileOnly;
    }

    public boolean getFileOnly() {
        return m_FileOnly;
    }

    /**
     * Returns The Check File Mode
     *
     * @return (boolean) if check new file by controling the last modif date
     */
    public boolean getCheckDate(){
        return mCheckDate;
    }

    /**
     * Set The Check File Mode ( by last modif date or returns all files found
     *
     * @param checkDate check by last modif date or not
     */
    public void setCheckDate( boolean checkDate){
        mCheckDate = checkDate;
    }

    /**
     * Thread is a Deamon or not
     *
     * @return (boolean) true if is Deamon Thread
     */
    public boolean isDeamon(){
        return m_IsDeamon;
    }

    /**
     * Create a Deamon or User Thread
     *
     * @param isDeamon
     */
    protected void setDeamon( boolean isDeamon ){
        m_IsDeamon = isDeamon;
    }

    public File getFolder() {
        return m_Folder;
    }

    public long getLastCheckTime() {
        return m_LastCheckTime;
    }

    public int getMaxJobNameLength() {
        return maxJobNameLength;
    }

    public void setMaxJobNameLength(int maxJobNameLength) {
        this.maxJobNameLength = maxJobNameLength;
    }

    /**
     * Verify if the Folder to watch exists.
     * Create the Archive Folder if not exist.
     *
     * @exception IOException
     */
    protected void initialize ()
            throws IOException {

        logger.debug("Initializing file watcher"  );

        /*
           For Test Purpose
           ToChange : restore the last check time from ext. file !
        */
        m_LastCheckTime = System.currentTimeMillis();
        logger.debug("Watching directory=" + getFolderPath()   );
        File tmpFile = new File(getFolderPath());
        if ( tmpFile.isDirectory() && !tmpFile.canWrite() ){
            logger.debug("No write access to directory " + getFolderPath() +
                    " tmpFile=" + tmpFile.toString());
        } else if ( !tmpFile.exists() ) {
            logger.debug("Directory " + tmpFile.toString() +
                    " does not exist, creating...");
            tmpFile.mkdirs();
            logger.debug("Directory " + tmpFile.toString() +
                    " created successfully.");
        }
        m_Folder = tmpFile;
    }

}
