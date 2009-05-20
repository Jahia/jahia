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
package org.jahia.ajax.gwt.module.versioning.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.widget.versioning.comparison.dao.DataFactory;
import org.jahia.ajax.gwt.client.widget.versioning.comparison.dao.RPCDataFactory;
import org.jahia.ajax.gwt.client.widget.versioning.comparison.VersionComparisonView;
import org.jahia.ajax.gwt.client.util.DOMUtil;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 22 avr. 2008
 * Time: 16:37:30
 * To change this template use File | Settings | File Templates.
 */
public class VersionComparisonEntryPoint implements EntryPoint {

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
