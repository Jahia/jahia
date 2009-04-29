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
 * @hibernate.class table="jahia_version"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaVersion implements Serializable, Comparable {

    private Integer installNumber;

    private Integer buildNumber;

    private String releaseNumber;

    private Date installationDate;

    public JahiaVersion() {
    }

    public JahiaVersion(Integer buildNumber, String releaseNumber, Date installationDate) {
        this.buildNumber = buildNumber;
        this.releaseNumber = releaseNumber;
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
     * @hibernate.property column="release_number"
     * length="20"
     */
    public String getReleaseNumber() {
        return releaseNumber;
    }

    public void setReleaseNumber(String releaseNumber) {
        this.releaseNumber = releaseNumber;
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

    public int compareTo(Object o) {
        return installNumber.compareTo(((JahiaVersion)o).getInstallNumber());
    }
}
