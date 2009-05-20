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
//
//  JahiaFile
//
//  NK      02.02.2001
//
//

package org.jahia.data.files;

import java.text.DateFormat;
import java.util.Date;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;

import java.io.Serializable;


/**
 * Class JahiaFile.<br>
 * A file item in the filemanager Application.<br>
 *
 * @author Khue ng
 * @version 1.0
 */
public class JahiaFile implements Serializable, Cloneable {

    public static final int STATE_ACTIVE = 1;
    public static final int STATE_BACKUP = 0;

    /**
     * the file identifier *
     */
    private int m_FileID = -1;
    /**
     * the filemanager identifier *
     */
    private int m_FilemanagerID = -1;
    /**
     * the folder identifier *
     */
    private int m_FolderID = -1;
    /**
     * the upload user identifer *
     */
    private String m_UploadUser = "";
    /**
     * the page id *
     */
    private int m_PageID = -1;
    /**
     * is public or not *
     */
    private int m_IsPublic = 1;
    /**
     * the real name of the file *
     */
    private String m_RealName = "";
    /**
     * the storage name on disk *
     */
    private String m_StorageName = "";
    /**
     * the last modification date *
     */
    private long m_LastModifDate;
    /**
     * the size in bytes *
     */
    private long m_Size = 0;
    /**
     * the content-type *
     */
    private String m_Type = "";
    /**
     * the general title (short desc) *
     */
    private String m_Title = "";
    /**
     * the general desc of the file *
     */
    private String m_Descr = "";
    /**
     * the download Url *
     */
    private String m_DownloadUrl = "#";
    private String m_ThumbnailUrl = "#";
    private String m_Orientation = "";
    /**
     * the version id *
     */
    private String m_Version = "0";
    /**
     * the state *
     */
    private int m_State = STATE_ACTIVE;


    /**
     * Constructor
     */
    protected JahiaFile() {
    }

    /**
     * Constructor
     *
     * @param filemanagerID
     * @param folderID
     * @param uploadUser
     * @param realName
     * @param storageName
     * @param lastModifDate
     * @param size
     * @param type
     * @param title
     * @param descr
     * @param version
     * @param state
     */
    public JahiaFile(int filemanagerID,
                     int folderID,
                     String uploadUser,
                     String realName,
                     String storageName,
                     long lastModifDate,
                     long size,
                     String type,
                     String title,
                     String descr,
                     String version,
                     int state) {
        m_FilemanagerID = filemanagerID;
        m_FolderID = folderID;
        m_UploadUser = uploadUser;
        m_RealName = realName;
        m_StorageName = storageName;
        m_LastModifDate = lastModifDate;
        m_Size = size;
        m_Type = type;
        m_Title = title;
        m_Descr = descr;
        m_Version = version;
        m_State = state;
    }

    public int getFileID() {
        return m_FileID;
    }

    public void setFileID(int id) {
        m_FileID = id;
    }

    public int getFilemanagerID() {
        return m_FilemanagerID;
    }

    public void setFilemanagerID(int id) {
        m_FilemanagerID = id;
    }

    public int getFolderID() {
        return m_FolderID;
    }

    public void setFolderID(int id) {
        m_FolderID = id;
    }

    public String getUploadUser() {
        return m_UploadUser;
    }

    public void setUploadUser(String name) {
        m_UploadUser = name;
    }

    public int getPageID() {
        return m_PageID;
    }

    public void setPageID(int id) {
        m_PageID = id;
    }

    public int getPublic() {
        return m_IsPublic;
    }

    public void setPublic(int val) {
        m_IsPublic = val;
    }

    public String getRealName() {
        return m_RealName;
    }

    public void setRealName(String realName) {
        m_RealName = realName;
    }

    public String getStorageName() {
        return m_StorageName;
    }

    public void setStorageName(String storageName) {
        m_StorageName = storageName;
    }

    public long getLastModifDate() {
        return m_LastModifDate;
    }

    public void setLastModifDate(long lastModifDate) {
        m_LastModifDate = lastModifDate;
    }

    public long getSize() {
        return m_Size;
    }

    public void setSize(long size) {
        m_Size = size;
    }

    public String getType() {
        return m_Type;
    }

    public void setType(String type) {
        m_Type = type;
    }

    public String getTitle() {
        return m_Title;
    }

    public void setTitle(String title) {
        m_Title = title;
    }

    public String getDescr() {
        return m_Descr;
    }

    public void setDescr(String descr) {
        m_Descr = descr;
    }

    public String getVersion() {
        return this.m_Version;
    }

    public void setVersion(String version) {
        this.m_Version = version;
    }

    public int getState() {
        return this.m_State;
    }

    public void setState(int state) {
        this.m_State = state;
    }

    public String getDownloadUrl() {
        return m_DownloadUrl;
    }

    public void setDownloadUrl(String dUrl) {
        m_DownloadUrl = dUrl;
    }

    public String getThumbnailUrl() {
        return m_ThumbnailUrl;
    }

    public void setThumbnailUrl(String dUrl) {
        m_ThumbnailUrl = dUrl;
    }

    public String getOrientation() {
        return m_Orientation;
    }

    public void setOrientation(String orientation) {
        m_Orientation = orientation;
    }

    // Output Representation purpose

    public String getFormatedLastModifDate() {
        Date tmpDate = new Date();
        tmpDate.setTime(m_LastModifDate);

        return DateFormat.getDateInstance().format(tmpDate);
    }

    public String getFormatedSize() {

        return String.valueOf(m_Size >> 10) + " Kb";
    }


    public boolean isImage() {

        return m_Type.startsWith("image");

    }


    public boolean isDownloadable() {
        if (m_StorageName == null) {
            return false;
        } else {
            return (m_StorageName.trim().length() > 0);
        }
    }


    public String getUploadUsername() {
        if (m_UploadUser != null && m_UploadUser.length() > 0) {
            JahiaUser user = ServicesRegistry.getInstance()
                    .getJahiaUserManagerService()
                    .lookupUserByKey(m_UploadUser);
            if (user != null) {
                return user.getUsername();
            }
        }
        return "";
    }

    public Object clone() throws CloneNotSupportedException {
        super.clone();
        JahiaFile file =
                new JahiaFile(this.getFilemanagerID(),
                        this.getFolderID(), this.getUploadUser(), this.getRealName(),
                        this.getStorageName(), this.getLastModifDate(), this.getSize(),
                        this.getType(), this.getTitle(), this.getDescr(), this.getVersion(), this.getState());
        file.setFileID(this.getFileID());
        return file;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final JahiaFile jahiaFile = (JahiaFile) o;

        if (!m_RealName.equals(jahiaFile.m_RealName)) return false;
        return m_StorageName.equals(jahiaFile.m_StorageName);
    }

    public int hashCode() {
        int result;
        result = m_RealName.hashCode();
        result = 29 * result + m_StorageName.hashCode();
        return result;
    }
}
