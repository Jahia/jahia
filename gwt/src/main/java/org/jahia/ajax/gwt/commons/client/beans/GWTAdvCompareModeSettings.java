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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.commons.client.beans;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 ao�t 2008
 * Time: 15:55:37
 * To change this template use File | Settings | File Templates.
 */
public class GWTAdvCompareModeSettings implements Serializable {

    private boolean enabled;

    private GWTRevision revision1;
    private GWTRevision revision2;

    public GWTAdvCompareModeSettings() {
    }

    public GWTAdvCompareModeSettings(boolean enabled, GWTRevision revision1, GWTRevision revision2) {
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

    public GWTRevision getRevision1() {
        return revision1;
    }

    public void setRevision1(GWTRevision revision1) {
        this.revision1 = revision1;
    }

    public GWTRevision getRevision2() {
        return revision2;
    }

    public void setRevision2(GWTRevision revision2) {
        this.revision2 = revision2;
    }
}