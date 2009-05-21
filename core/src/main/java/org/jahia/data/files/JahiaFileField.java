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