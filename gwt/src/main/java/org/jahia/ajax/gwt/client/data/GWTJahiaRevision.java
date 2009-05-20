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

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 19 nov. 2008
 * Time: 13:12:01
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaRevision extends GWTJahiaProperty {

    private long date;
    private GWTJahiaVersion version;
    private boolean useVersion;

    public GWTJahiaRevision() {
    }

    public GWTJahiaRevision(long date, GWTJahiaVersion version, boolean useVersion) {
        this.date = date;
        this.version = version;
        this.useVersion = useVersion;
    }

    public boolean isUseVersion() {
        return useVersion;
    }

    public void setUseVersion(boolean useVersion) {
        this.useVersion = useVersion;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public GWTJahiaVersion getVersion() {
        return version;
    }

    public void setVersion(GWTJahiaVersion version) {
        this.version = version;
    }
}
