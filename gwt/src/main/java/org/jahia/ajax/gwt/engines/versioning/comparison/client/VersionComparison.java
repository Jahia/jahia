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

package org.jahia.ajax.gwt.engines.versioning.comparison.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.engines.versioning.comparison.client.dao.DataFactory;
import org.jahia.ajax.gwt.engines.versioning.comparison.client.dao.RPCDataFactory;
import org.jahia.ajax.gwt.engines.versioning.comparison.client.view.VersionComparisonView;
import org.jahia.ajax.gwt.commons.client.util.DOMUtil;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 22 avr. 2008
 * Time: 16:37:30
 * To change this template use File | Settings | File Templates.
 */
public class VersionComparison implements EntryPoint {

    public static final String ID = "versioncomparison";
    public static final String VERSIONABLE_UUID = "versionableUUID";
    public static final String VERSION1 = "version1";
    public static final String VERSION2 = "version2";
    public static final String LANG = "lang";

    public void onModuleLoad() {

        // init panel
        RootPanel rootPanel = RootPanel.get(ID);

        String versionableUUID = getVersionableUUID(rootPanel);
        String version1 = getVersion(rootPanel,VERSION1);
        String version2 = getVersion(rootPanel,VERSION2);
        String lang = getLanguage(rootPanel);

        // create the data factory implementation which may be RPC based or REST based etc
        DataFactory dataFactory = new RPCDataFactory();

        // create panel depending on state
        rootPanel.add(new VersionComparisonView(dataFactory,versionableUUID, version1, version2, lang));
    }

    private String getVersionableUUID(RootPanel rootPanel) {
        return DOMUtil.getRootAttr(rootPanel, VERSIONABLE_UUID);

    }

    private String getVersion(RootPanel rootPanel, String attributeName) {
        return DOMUtil.getRootAttr(rootPanel, attributeName);

    }

    private String getLanguage(RootPanel rootPanel) {
        return DOMUtil.getRootAttr(rootPanel, LANG);
    }

}
