/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.content.compare;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.util.ContentHelper;
import org.jahia.ajax.gwt.client.widget.contentengine.NodeHolder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * User: ktlili
 * Date: Mar 2, 2010
 * Time: 9:27:04 AM
 */
public class CompareEngine extends Window {
    public static final int BUTTON_HEIGHT = 24;
    private GWTJahiaNode node;
    private Linker linker = null;
    private String locale;

    //private LayoutContainer mainComponent;
    private VersionViewer leftPanel;
    protected ButtonBar buttonBar;
    private String uuid;
    private String path;
    private Date versionDate = null;
    private NodeHolder engine = null;
    private String workspace = null;
    private String versionLabel = null;
    private boolean refreshOpener = false;
    private VersionViewer rightPanel;

    /**
     * Initializes an instance of this class.
     *
     * @param node   the content object to be edited
     * @param linker the edit linker for refresh purpose
     */
    public CompareEngine(GWTJahiaNode node, String locale, Linker linker, boolean displayVersionSelector, boolean displayTwoPanels) {
        this.linker = linker;
        this.node = node;
        this.locale = locale;
        init(displayVersionSelector, displayTwoPanels);
    }

    public CompareEngine(String uuid, String locale, boolean displayVersionSelector, String path) {
        this.locale = locale;
        this.uuid = uuid;
        this.path = path;
        init(displayVersionSelector, true);
    }

    public CompareEngine(String uuid, String locale, boolean displayVersionSelector, String path, Date versionDate,
                         NodeHolder engine, String workspace, String versionLabel) {
        this.locale = locale;
        this.uuid = uuid;
        this.path = path;
        this.versionDate = versionDate;
        this.engine = engine;
        this.workspace = workspace;
        this.versionLabel = versionLabel;
        init(displayVersionSelector, true);
    }

    protected void init(boolean displayVersionSelector, boolean displayTwoPanels) {
        setLayout(new RowLayout(Style.Orientation.HORIZONTAL));
        addStyleName("compare-engine");
        setBodyBorder(false);
        int windowHeight = com.google.gwt.user.client.Window.getClientHeight() - 10;
        int windowWidth = com.google.gwt.user.client.Window.getClientWidth() - 10;
        setSize(windowWidth, windowHeight);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        String title = Messages.get("label.compare","Compare ");
        if (node != null) {
            setHeadingHtml(title + node.getPath());
        } else {
            setHeadingHtml(title + path);
        }
        //live version
        if (node != null) {
            leftPanel = new VersionViewer(node, linker.getSelectionContext().getSingleSelection().getLanguageCode(),
                    linker, "live", false, displayVersionSelector, this);
        } else {
            leftPanel = new VersionViewer(uuid, locale, workspace != null ? workspace : "live", false, displayVersionSelector, versionDate, this, versionLabel);
        }

        add(leftPanel, new RowData(displayTwoPanels ? 0.5 : 1,1));
        if (displayTwoPanels) {
            if (node != null) {
                rightPanel = new VersionViewer(node, linker.getSelectionContext().getSingleSelection().getLanguageCode(),
                        linker, displayVersionSelector ? "live" : "default", true, displayVersionSelector, this) {
                    @Override
                    public String getCompareWith() {
                        return leftPanel.getInnerHTML();
                    }
                };
            } else {
                rightPanel = new VersionViewer(uuid, locale, "default", true, displayVersionSelector, null, this, null) {
                    @Override
                    public String getCompareWith() {
                        return leftPanel.getInnerHTML();
                    }
                };
            }
            add(rightPanel, new RowData(0.5, 1));
        }
        layout();
    }


    @Override
    protected void onHide() {
        super.onHide();
        if (refreshOpener) {
            Map<String, Object> data = new HashMap<String, Object>();
            if (uuid != null) {
                ContentHelper.sendContentModificationEvent(uuid, path, null, "version", null);
            }
            data.put(Linker.REFRESH_ALL, true);
            if (engine != null) {
                engine.close();
                engine.getLinker().refresh(data);
            }
            if (linker != null) {
                linker.refresh(data);
            }
        }
    }

    @Override
    public void show() {
        super.show();
        maximize();
    }

    public void setRefreshOpener(boolean refreshOpener) {
        this.refreshOpener = refreshOpener;
    }

    protected VersionViewer getLeftPanel() {
        return leftPanel;
    }

    protected VersionViewer getRightPanel() {
        return rightPanel;
    }
}


