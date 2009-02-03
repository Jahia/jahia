/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
