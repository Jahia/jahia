/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
import org.jahia.ajax.gwt.client.widget.contentengine.NodeHolder;

import java.util.Date;

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
        setBodyBorder(false);
        int windowHeight = com.google.gwt.user.client.Window.getClientHeight() - 10;
        int windowWidth = com.google.gwt.user.client.Window.getClientWidth() - 10;
        setSize(windowWidth, windowHeight);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);

        if (node != null) {
            setHeading(Messages.get("label_compare " + node.getPath(), "Compare " + node.getPath()));
        } else {
            setHeading(Messages.get("label_compare " + path, "Compare " + path));
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
            // staging version
            VersionViewer rightPanel;
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
            if (engine != null) {
                engine.close();
                engine.getLinker().refresh(Linker.REFRESH_ALL);
            }
            if (linker != null) {
                linker.refresh(Linker.REFRESH_ALL);
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
}


