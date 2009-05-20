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
 * Time: 15:59:31
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaContext implements Serializable {

    private GWTJahiaAdvPreviewSettings advPreviewSettings;
    private GWTJahiaAdvCompareModeSettings advCompareModeSettings;

    public GWTJahiaContext() {
    }

    public GWTJahiaContext(GWTJahiaAdvPreviewSettings advPreviewSettings) {
        this.advPreviewSettings = advPreviewSettings;
    }

    public GWTJahiaContext(GWTJahiaAdvCompareModeSettings advCompareModeSettings) {
        this.advCompareModeSettings = advCompareModeSettings;
    }

    public GWTJahiaContext(GWTJahiaAdvPreviewSettings advPreviewSettings,
                           GWTJahiaAdvCompareModeSettings advCompareModeSettings) {
        this.advPreviewSettings = advPreviewSettings;
        this.advCompareModeSettings = advCompareModeSettings;
    }

    public GWTJahiaAdvPreviewSettings getAdvPreviewSettings() {
        return advPreviewSettings;
    }

    public void setAdvPreviewSettings(GWTJahiaAdvPreviewSettings advPreviewSettings) {
        this.advPreviewSettings = advPreviewSettings;
    }

    public GWTJahiaAdvCompareModeSettings getAdvCompareModeSettings() {
        return advCompareModeSettings;
    }

    public void setAdvCompareModeSettings(GWTJahiaAdvCompareModeSettings advCompareModeSettings) {
        this.advCompareModeSettings = advCompareModeSettings;
    }
}
