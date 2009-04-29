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
package org.jahia.ajax.gwt.engines.versioning.server;

import org.jahia.params.ParamBean;
import org.jahia.services.usermanager.JahiaUser;


/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 30 avr. 2008
 * Time: 11:57:51
 * To change this template use File | Settings | File Templates.
 */
public class VersionComparisonContext {

    private String versionableUUID;
    private String version1;
    private String version2;
    private String lang;
    private ParamBean jParams;
    private JahiaUser user;

    public VersionComparisonContext(String versionableUUID, String version1, String version2, String lang,
                                    ParamBean jParams, JahiaUser user) {
        this.versionableUUID = versionableUUID;
        this.version1 = version1;
        this.version2 = version2;
        this.lang = lang;
        this.jParams = jParams;
        this.user = user;
    }

    public String getVersionableUUID() {
        return versionableUUID;
    }

    public void setVersionableUUID(String versionableUUID) {
        this.versionableUUID = versionableUUID;
    }

    public String getVersion1() {
        return version1;
    }

    public void setVersion1(String version1) {
        this.version1 = version1;
    }

    public String getVersion2() {
        return version2;
    }

    public void setVersion2(String version2) {
        this.version2 = version2;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public ParamBean getJParams() {
        return jParams;
    }

    public void setJParams(ParamBean jParams) {
        this.jParams = jParams;
    }

    public JahiaUser getUser() {
        return user;
    }

    public void setUser(JahiaUser user) {
        this.user = user;
    }
}
