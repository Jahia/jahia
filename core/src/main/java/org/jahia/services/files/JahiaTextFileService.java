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
//  Interface JahiaTextFileService
//  EV      18.11.2000
//
//  init()
//  composeJahiaFileName()
//  loadContents()
//  saveContents()
//  fileExists()
//  readFile()
//  writeFile()
//

package org.jahia.services.files;


import java.io.IOException;

import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;
import org.jahia.services.usermanager.JahiaUser;

/**
 * <p>Title: Jahia text file service</p>
 * <p>Description: This services provides all the operations to store and
 * retrieve "BigText" jahia fields data.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */
public abstract class JahiaTextFileService extends JahiaService {


   /***
    * loadContents
    */
    public abstract String loadBigTextValue(int jahiaID, int pageID, int fieldID, String fieldValue,
            int versionID, int versionStatus, String languageCode)
    throws JahiaException;


   /***
    * saveContents
    */
    public abstract String saveContents( int jahiaID, int pageID, int fieldID,
                                         String fieldValue,
                                         int versionID,
                                         int versionStatus,
                                         String languageCode )
    throws JahiaException;


    /***
    * getFileName
     * YG    29.08.2001
     * returns the file name
    *
    */
    public abstract String getFileName( int jahiaID, int pageID, int fieldID,
                                        int versionID,
                                        int versionStatus,
                                        String languageCode)
    throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * Copy all big text of a site in a gived folder
     * This folder must be a valid folder
     *
     * @param int siteID the id of the requested site
     * @param String destFolder the destination Folder
     * @return the number of duplicated files, -1 on error
     * @author NK
     */
    public abstract int copySiteBigText(int siteID, String destFolder)
    throws IOException;

    //--------------------------------------------------------------------------
    /**
     * Delete all big text files of a site
     *
     * @param int siteID the id of the requested site
     * @param User the user must be a root user
     * @return false on error
     * @author NK
     */
    public abstract boolean deleteSiteBigText(int siteID, JahiaUser user)
    throws IOException;

    /**
     * renames a file
     * first half of the parameters are describing the old file, other half the new one.
     * @return true if it worked
     */
    public abstract boolean renameFile (int jahiaID, int pageID, int fieldID,
                                        int versionID,
                                        int versionStatus,
                                        String languageCode,

                                        int njahiaID, int npageID, int nfieldID,
                                        int nversionID,
                                        int nversionStatus,
                                        String nlanguageCode) throws Exception;

    /**
     * copy a file
     * first half of the parameters are describing the old file, other half the new one.
     * @return true if it worked
     */
    public abstract boolean copyFile (int jahiaID, int pageID, int fieldID,
                                      int versionID,
                                      int versionStatus,
                                      String languageCode,

                                      int njahiaID, int npageID, int nfieldID,
                                      int nversionID,
                                      int nversionStatus,
                                      String nlanguageCode);

    /**
     * delete a file
     * @return true if it worked
     */
    public abstract boolean deleteFile (int jahiaID, int pageID, int fieldID,
                                        int versionID,
                                        int versionStatus,
                                        String languageCode);

} // end JahiaTextFileService
