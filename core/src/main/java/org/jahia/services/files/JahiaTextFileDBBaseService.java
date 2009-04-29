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
