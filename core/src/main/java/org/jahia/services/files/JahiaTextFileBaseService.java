/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.files;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.utils.FileUtils;
import org.jahia.utils.JahiaTools;


public class JahiaTextFileBaseService extends JahiaTextFileService {

    /** logging */
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JahiaTextFileBaseService.class);

    /** the service name */
    private static String serviceName = "JahiaTextFileService";

    /** the disk path where to store the data */
    private String jahiaDataDiskPath = "";

    /** the service unique instance */
    private static JahiaTextFileBaseService instance;

    // the Text File cache name.
    public static final String TEXT_FILE_CACHE = "TextFileCache";
    /** the service cache */
    private static Cache cacheText;     // filename -> content

    private CacheService cacheService;

    static class DummyCacheMarker {        
    }

    private static final DummyCacheMarker DUMMY_CACHE_MARKER = new DummyCacheMarker();

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }


    /** Default constructor, creates a new <code>JahiaTextFileBaseService</code> instance.
     */
    protected JahiaTextFileBaseService () {
        logger.debug ("***** Starting " + serviceName + " *****");
    }


    /**
     * Return the unique instance of this class.
     *
     * @return  the unique instance of this class.
     */
    public static synchronized JahiaTextFileBaseService getInstance () {
        if (instance == null) {
            instance = new JahiaTextFileBaseService ();
        }
        return instance;
    }


    /** Initializes the service with the settings passed as argument.
     *
     */
    public void start()
            throws JahiaInitializationException
    {
        jahiaDataDiskPath = settingsBean.getJahiaFilesBigTextDiskPath();
        File filePath = new File (jahiaDataDiskPath);
        if (!filePath.exists ()) {
            logger.debug ("Creating " + jahiaDataDiskPath + " path ");
            filePath.mkdirs ();
        }

        /**
         * Warning we only use the file name part here because we had problems
         * with cache synchronization in clusters that didn't use a 100% similar
         * "slashing" scheme and this was causing sync problems.
         */
        cacheText = cacheService.createCacheInstance(TEXT_FILE_CACHE);
    }

    public synchronized void stop ()
        throws JahiaException {

        // flush the cache
        cacheText.flush();
    }


    /**
     * Load a big text file content and set its value.
     *
     * @param jahiaID       The site ID referenced by the big text.
     * @param pageID        The page ID referenced by the big text.
     * @param fieldID       The big text field ID.
     * @param fieldValue  The default big text value if file does not exists.
     * @param versionID     The big text version ID.
     * @param workflowState The big text workflow state.
     * @param languageCode  The big text language code.
     * @return
     *     The big text value (aka file content) as a String.
     * @throws JahiaException
     *      when a general exception occured.
     */
    public String loadBigTextValue (int jahiaID, int pageID, int fieldID, String fieldValue,
                                    int versionID, int workflowState, String languageCode)
            throws JahiaException
    {
        String fileName = FileUtils.composeBigTextFileNamePart(jahiaID, pageID, fieldID, versionID, workflowState, languageCode);
        String fullPath = FileUtils.composeBigTextFullPathName (jahiaDataDiskPath, fileName);
        String strResult = null;
        Object result = cacheText.get (fileName);
        if (result == null) {
            if (FileUtils.fileExists (fullPath)) {
                result = FileUtils.readFile (fullPath, languageCode);
            } else {
                result = fieldValue;
            }
            if (result == null) {
                result = DUMMY_CACHE_MARKER;
            }
            cacheText.put (fileName, result);
        }
        if (result == DUMMY_CACHE_MARKER) {
            strResult = null;
        } else {
            strResult = (String) result;
        }
        if (logger.isDebugEnabled()) {
            logger.debug ("File : " + fullPath + ", value : " + result);
        }

        return strResult;
    }

    /**
     * saveContents
     * EV    18.11.2000
     * EV    18.11.2000  returns the file name
     *
     */
    public String saveContents (int jahiaID, int pageID, int fieldID, String fieldValue,
                                int versionID, int versionStatus, String languageCode)
            throws JahiaException
    {
        String fileName = FileUtils.composeBigTextFileNamePart(jahiaID, pageID, fieldID, versionID, versionStatus, languageCode);
        String fullPath = FileUtils.composeBigTextFullPathName (
                jahiaDataDiskPath, fileName);

        if (logger.isDebugEnabled()) {
            logger.debug ("File : " + fullPath + ", value : " + fieldValue);
        }
        FileUtils.writeFile (fullPath, fieldValue, languageCode);
        cacheText.put (fileName, fieldValue);
        return fileName;
    }


    /**
     * getFileName
     * YG    29.08.2001
     * returns the file name
     *
     */
    public String getFileName (int jahiaID, int pageID, int fieldID, int versionID,
                               int versionStatus, String languageCode)
            throws JahiaException
    {
        return FileUtils.composeBigTextFullPathName (
                jahiaDataDiskPath, jahiaID, pageID, fieldID, versionID,
                versionStatus, languageCode);
    }


    /**
     * set the service name
     *
     * @param   name    the service name to be set
     */
    public void setName (String name) {
        serviceName = name;
    }

    /**
     * renames a file
     * first half of the parameters are describing the old file, other half the new one.
     * @return true if it worked
     */
    public boolean renameFile (int jahiaID, int pageID, int fieldID, int versionID, int versionStatus, String languageCode,
                               int njahiaID, int npageID, int nfieldID, int nversionID, int nversionStatus, String nlanguageCode) throws Exception {

        String oldFileName = FileUtils.composeBigTextFileNamePart (jahiaID, pageID, fieldID, versionID, versionStatus, languageCode);
        String newFileName = FileUtils.composeBigTextFileNamePart (njahiaID, npageID, nfieldID, nversionID, nversionStatus, nlanguageCode);
        String oldFullPath = FileUtils.composeBigTextFullPathName (jahiaDataDiskPath, oldFileName);
        String newFullPath = FileUtils.composeBigTextFullPathName (jahiaDataDiskPath, newFileName);

        if (logger.isDebugEnabled()) {
            logger.debug (" from File : " + oldFullPath + ", to File : " + newFullPath);
        }
        boolean result = true;
        if (oldFullPath != null && newFullPath != null
                && !oldFullPath.equals (newFullPath)) {
            // First delete the new file if exists
            FileUtils.deleteFile (newFullPath);
            result = FileUtils.renameFile (oldFullPath, newFullPath);
            if (result) {
                // let's synchronize cache
                cacheText.remove (oldFileName);
                cacheText.remove (newFileName);
            }
        }

        return result;
    }

    /**
     * copy a file
     * first half of the parameters are describing the old file, other half the new one.
     * @return true if it worked
     */
    public boolean copyFile (int jahiaID, int pageID, int fieldID, int versionID, int versionStatus, String languageCode,
                             int njahiaID, int npageID, int nfieldID, int nversionID, int nversionStatus, String nlanguageCode) {
        String oldFileName = FileUtils.composeBigTextFileNamePart (jahiaID, pageID, fieldID, versionID, versionStatus, languageCode);
        String newFileName = FileUtils.composeBigTextFileNamePart (njahiaID, npageID, nfieldID, nversionID, nversionStatus, nlanguageCode);
        String oldFullPath = FileUtils.composeBigTextFullPathName (jahiaDataDiskPath, oldFileName);
        String newFullPath = FileUtils.composeBigTextFullPathName (jahiaDataDiskPath, newFileName);
        if (logger.isDebugEnabled()) {
            logger.debug (" from File : " + oldFullPath + ", to File : " + newFullPath);
        }

        // test if the oldFileName exist or not, if not, create an empty file
        if (versionStatus < 1) {
            // does the archive file exist ? Create an empty one else.
            File f = new File (oldFullPath);
            if (!f.exists ()) {
                try {
                    f.createNewFile ();
                } catch (Exception t) {
                    logger.debug (" the old file doesn't exist and an exception occured when trying to create an empty one " + oldFullPath + ", to File : ", t);
                }
            }
        }
        boolean result = FileUtils.copyFile (oldFullPath, newFullPath);
        if (result) {
            // let's synchronize cache
            cacheText.remove (newFileName);
        }
        return result;
    }

    /**
     * delete a file
     * @return true if it worked
     */
    public boolean deleteFile (int jahiaID, int pageID, int fieldID, int versionID, int versionStatus, String languageCode) {
        String fileName = FileUtils.composeBigTextFileNamePart (jahiaID, pageID, fieldID, versionID, versionStatus, languageCode);
        String fullPath = FileUtils.composeBigTextFullPathName (jahiaDataDiskPath, fileName);

        if (logger.isDebugEnabled()) {
            logger.debug (" File : " + fullPath);
        }

        boolean result = FileUtils.deleteFile (fullPath);
        if (result) {
            cacheText.remove(fileName);
        }
        return result;
    }


/////////////////////////////////  CacheIO objects ///////////////////////////////////////

    // NK
    /**
     * Copy all big text of a site in a gived folder
     * This folder must be a valid folder
     *
     * @param   siteID       the id of the requested site
     * @param   destFolder   the destination Folder
     * @return  the number of duplicated files, -1 on error
     */
    public int copySiteBigText (int siteID, String destFolder)
            throws IOException {

        File f = new File (destFolder);
        if (!f.isDirectory () || !f.canWrite ()) {
            return -1;
        }

        String destFolderPath = destFolder + File.separator;

        f = null;
        f = new File (jahiaDataDiskPath);
        if (!f.isDirectory () || !f.canRead ()) {
            return -1;
        }

        File[] files = f.listFiles ();
        if (files.length == 0) {
            return 0;
        }

        String siteLabel = siteID + "-";
        int nb = files.length;
        File destFile = null;
        int nbCopy = 0;
        for (int i = 0; i < nb; i++) {
            if (files[i].getName ().startsWith (siteLabel)) {
                destFile = new File (destFolderPath + files[i].getName ());
                try {
                    FileInputStream fileInput = new FileInputStream (files[i]);
                    FileOutputStream fileOutput = new FileOutputStream (destFile);
                    JahiaTools.copyStream (fileInput, fileOutput);
                    fileInput = null;
                    fileOutput = null;
                    nbCopy += 1;
                } catch (java.io.FileNotFoundException fnfe) {
                    //
                }
            }
        }
        return nbCopy;
    }


    // NK
    /**
     * Delete all big text files of a site
     *
     * @param   siteID  the id of the requested site
     * @param   user    the user must be a root user
     * @return  false on error
     */
    public boolean deleteSiteBigText (int siteID, JahiaUser user)
            throws IOException {

        if (!user.isAdminMember (0)) {
            return false;
        }

        File f = new File (jahiaDataDiskPath);
        if (!f.isDirectory () || !f.canWrite ()) {
            return false;
        }

        File[] files = f.listFiles ();

        String siteLabel = siteID + "-";
        int nb = files.length;
        for (int i = 0; i < nb; i++) {
            if (files[i].getName ().startsWith (siteLabel)) {
                files[i].delete ();
            }
        }
        return true;
    }

}
