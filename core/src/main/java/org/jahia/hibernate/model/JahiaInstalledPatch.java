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
package org.jahia.hibernate.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @hibernate.class table="jahia_installedpatch"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaInstalledPatch implements Serializable {

    private Integer installNumber;

    private String name;

    private Integer buildNumber;

    private Integer resultCode;

    private Date installationDate;

    public JahiaInstalledPatch() {
    }

    public JahiaInstalledPatch(String name, Integer buildNumber, Integer resultCode, Date installationDate) {
        this.name = name;
        this.buildNumber = buildNumber;
        this.resultCode = resultCode;
        this.installationDate = installationDate;
    }

    /**
     * @hibernate.id generator-class="org.jahia.hibernate.dao.JahiaIdentifierGenerator"
     * column="install_number"
     */
    public Integer getInstallNumber() {
        return installNumber;
    }

    public void setInstallNumber(Integer installNumber) {
        this.installNumber = installNumber;
    }

    /**
     * @hibernate.property column="name"
     * length="100"
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @hibernate.property column="build"
     * length="11"
     */
    public Integer getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(Integer buildNumber) {
        this.buildNumber = buildNumber;
    }

    /**
     * @hibernate.property column="result_code"
     * length="11"
     */
    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    /**
     * @hibernate.property column="install_date"
     */
    public Date getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(Date installationDate) {
        this.installationDate = installationDate;
    }

}
