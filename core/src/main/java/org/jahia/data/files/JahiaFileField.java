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
//  JahiaFileField
//
//  NK      02.02.2001
//
//

package org.jahia.data.files;

import org.jahia.utils.JahiaTools;
import org.jahia.utils.FileUtils;

import java.util.Properties;

/**
 * Class JahiaFileField.<br>
 * A file field item <br>
 *
 * @author Khue ng
 * @version 1.0
 */
public class JahiaFileField extends JahiaFile implements Cloneable {

    public static final String FIELD_FILE_TITLE_PROP = "field_file_title";
    public static final String FIELD_FILE_FILEID_PROP = "field_file_fileid";
    public static final String FIELD_FILE_VERSION_PROP = "field_file_version";

    /**
     * the id *
     */
    private int id = -1;
    /**
     * Properties *
     */
    private Properties properties = new Properties();

    /**
     * Constructor
     */
    protected JahiaFileField() {
    }

    /**
     * @param fileItem
     * @param properties, if null, not set
     */
    public JahiaFileField(JahiaFile fileItem,
                          Properties properties) {
        if (properties != null) {
            this.properties = properties;
        }

        if (fileItem != null) {
            setFile(fileItem);
        }

        // default value
        if (this.getFileFieldTitle() == null
                || "".equals(this.getFileFieldTitle())) {
            this.setFileFieldTitle(fileItem.getTitle());
        }

    }

    public int getID() {
        return this.id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public void setFile(JahiaFile fileItem) {
        if (fileItem == null) {
            return;
        }
        setFileID(fileItem.getFileID());
        setFilemanagerID(fileItem.getFilemanagerID());
        setFolderID(fileItem.getFolderID());
        setUploadUser(fileItem.getUploadUser());
        setRealName(fileItem.getRealName());
        setStorageName(fileItem.getStorageName());
        setLastModifDate(fileItem.getLastModifDate());
        setSize(fileItem.getSize());
        setType(fileItem.getType());
        setTitle(fileItem.getTitle());
        setDescr(fileItem.getDescr());
        setVersion(fileItem.getVersion());
        setState(fileItem.getState());

        this.setProperty(JahiaFileField.FIELD_FILE_FILEID_PROP,
                String.valueOf(fileItem.getFileID()));

        this.setProperty(JahiaFileField.FIELD_FILE_VERSION_PROP, fileItem.getVersion());

        if (this.getFileFieldTitle() == null) {
            this.setFileFieldTitle(fileItem.getTitle());
        }

    }

    public String getFileFieldTitle() {
        if (properties == null) {
            return null;
        }
        return (String) properties.get(FIELD_FILE_TITLE_PROP);
    }

    public void setFileFieldTitle(String title) {
        if (title == null) {
            return;
        }
        setProperty(FIELD_FILE_TITLE_PROP, title);
    }

    public void setProperty(String name, String value) {
        if (properties == null) {
            properties = new Properties();
        }
        properties.setProperty(name, value);
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void setProperties(Properties properties) {
        if (properties != null) {
            this.properties = properties;
        }
    }

    public Object clone() {

        JahiaFile file =
                new JahiaFile(this.getFilemanagerID(),
                        this.getFolderID(), this.getUploadUser(), this.getRealName(),
                        this.getStorageName(), this.getLastModifDate(), this.getSize(),
                        this.getType(), this.getTitle(), this.getDescr(), this.getVersion(), this.getState());
        file.setFileID(this.getFileID());

        Properties props = (Properties) this.properties.clone();

        return new JahiaFileField(file, props);
    }

    public String getPicto() {
        return FileUtils.getFileIcon(getFileFieldTitle());
    }
}