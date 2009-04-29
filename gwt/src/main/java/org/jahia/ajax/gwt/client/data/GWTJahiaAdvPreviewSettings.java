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
package org.jahia.ajax.gwt.client.data;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 ao�t 2008
 * Time: 15:55:37
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaAdvPreviewSettings implements Serializable {

    private GWTJahiaUser mainUser;
    private GWTJahiaUser aliasedUser;
    private boolean enabled;
    private long previewDate;

    public GWTJahiaAdvPreviewSettings() {
    }

    public GWTJahiaAdvPreviewSettings(GWTJahiaUser mainUser, GWTJahiaUser aliasedUser, boolean enabled, long previewDate) {
        this.mainUser = mainUser;
        this.aliasedUser = aliasedUser;
        this.enabled = enabled;
        this.previewDate = previewDate;
    }

    public GWTJahiaUser getMainUser() {
        return mainUser;
    }

    public void setMainUser(GWTJahiaUser mainUser) {
        this.mainUser = mainUser;
    }

    public GWTJahiaUser getAliasedUser() {
        return aliasedUser;
    }

    public void setAliasedUser(GWTJahiaUser aliasedUser) {
        this.aliasedUser = aliasedUser;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getPreviewDate() {
        return previewDate;
    }

    public void setPreviewDate(long previewDate) {
        this.previewDate = previewDate;
    }

}