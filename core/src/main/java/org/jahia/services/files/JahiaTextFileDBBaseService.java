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
 package org.jahia.services.files;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaBigTextDataManager;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.FileUtils;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 juin 2005
 * Time: 17:28:14
 * To change this template use File | Settings | File Templates.
 */
public class JahiaTextFileDBBaseService extends JahiaTextFileService {
    private static JahiaTextFileDBBaseService instance;
    private JahiaBigTextDataManager bigTextDataManager;
    /**
     * Return the unique instance of this class.
     *
     * @return  the unique instance of this class.
     */
    public static synchronized JahiaTextFileDBBaseService getInstance () {
        if (instance == null) {
            instance = new JahiaTextFileDBBaseService(); ;
        }
        return instance;
    }

    public void start() throws JahiaInitializationException {
    }

    public void stop() {}

    public void setBigTextDataManager(JahiaBigTextDataManager bigTextDataManager) {
        this.bigTextDataManager = bigTextDataManager;
    }

    public String loadBigTextValue(int jahiaID, int pageID, int fieldID, String fieldValue, int versionID, int workflowState, String languageCode) throws JahiaException {
        String fileName = FileUtils.composeBigTextFileNamePart(jahiaID, pageID, fieldID, versionID, workflowState, languageCode);
        return bigTextDataManager.load(fileName);
    }

    public String saveContents(int jahiaID, int pageID, int fieldID, String fieldValue, int versionID, int workflowState, String languageCode) throws JahiaException {
        String fileName = FileUtils.composeBigTextFileNamePart(jahiaID, pageID, fieldID, versionID, workflowState, languageCode);
        bigTextDataManager.save(fileName, fieldValue);
        return fileName;
    }

    public String getFileName(int jahiaID, int pageID, int fieldID, int versionID, int versionStatus, String languageCode) throws JahiaException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int copySiteBigText(int siteID, String destFolder) throws IOException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean deleteSiteBigText(int siteID, JahiaUser user) throws IOException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean renameFile(int jahiaID, int pageID, int fieldID, int versionID, int versionStatus, String languageCode,

                              int njahiaID, int npageID, int nfieldID, int nversionID, int nversionStatus, String nlanguageCode) throws Exception {
        String oldFileName = FileUtils.composeBigTextFileNamePart (jahiaID, pageID, fieldID, versionID, versionStatus, languageCode);
        String newFileName = FileUtils.composeBigTextFileNamePart (njahiaID, npageID, nfieldID, nversionID, nversionStatus, nlanguageCode);
        return bigTextDataManager.rename(oldFileName,newFileName);
    }

    public boolean copyFile(int jahiaID, int pageID, int fieldID, int versionID, int versionStatus, String languageCode,

                            int njahiaID, int npageID, int nfieldID, int nversionID, int nversionStatus, String nlanguageCode) {
        String oldFileName = FileUtils.composeBigTextFileNamePart (jahiaID, pageID, fieldID, versionID, versionStatus, languageCode);
        String newFileName = FileUtils.composeBigTextFileNamePart (njahiaID, npageID, nfieldID, nversionID, nversionStatus, nlanguageCode);
        return bigTextDataManager.copy(oldFileName,newFileName);
    }

    public boolean deleteFile(int jahiaID, int pageID, int fieldID, int versionID, int versionStatus, String languageCode) {
        String fileName = FileUtils.composeBigTextFileNamePart (jahiaID, pageID, fieldID, versionID, versionStatus, languageCode);
        return bigTextDataManager.delete(fileName);
    }
}
