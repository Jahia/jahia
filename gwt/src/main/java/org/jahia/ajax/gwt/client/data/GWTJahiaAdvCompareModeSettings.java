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
public class GWTJahiaAdvCompareModeSettings implements Serializable {

    private boolean enabled;

    private GWTJahiaRevision revision1;
    private GWTJahiaRevision revision2;

    public GWTJahiaAdvCompareModeSettings() {
    }

    public GWTJahiaAdvCompareModeSettings(boolean enabled, GWTJahiaRevision revision1, GWTJahiaRevision revision2) {
        this.enabled = enabled;
        this.revision1 = revision1;
        this.revision2 = revision2;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public GWTJahiaRevision getRevision1() {
        return revision1;
    }

    public void setRevision1(GWTJahiaRevision revision1) {
        this.revision1 = revision1;
    }

    public GWTJahiaRevision getRevision2() {
        return revision2;
    }

    public void setRevision2(GWTJahiaRevision revision2) {
        this.revision2 = revision2;
    }
}