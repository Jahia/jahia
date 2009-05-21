/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
