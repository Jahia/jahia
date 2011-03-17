/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.content.compare;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;

/**
 * Created by IntelliJ IDEA.
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
    private VersionViewer rightPanel;
    protected ButtonBar buttonBar;
    private String uuid;
    private String path;


    /**
     * Initializes an instance of this class.
     *
     * @param node   the content object to be edited
     * @param linker the edit linker for refresh purpose
     * @param b
     */
    public CompareEngine(GWTJahiaNode node, String locale, Linker linker, boolean displayVersionSelector) {
        this.linker = linker;
        this.node = node;
        this.locale = locale;
        init(displayVersionSelector);
    }

    public CompareEngine(String uuid, String locale, boolean displayVersionSelector,String path) {
        this.locale = locale;
        this.uuid = uuid;
        this.path = path;
        init(displayVersionSelector);
    }

    protected void init(boolean displayVersionSelector) {
        setLayout(new BorderLayout());
        setBodyBorder(false);
        setSize(1300, 750);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        if(node!=null) {
            setHeading(Messages.get("label_compare " + node.getPath(), "Compare " + node.getPath()));
        } else {
            setHeading(Messages.get("label_compare " + path, "Compare " + path));
        }
        ContentPanel panel = new ContentPanel();
        panel.setLayout(new RowLayout(Style.Orientation.HORIZONTAL));
        panel.setWidth("100%");
        panel.setHeight("100%");
        panel.setFrame(true);
        panel.setCollapsible(false);
        panel.setHeaderVisible(false);

        //live version
        if (node != null) {
            leftPanel = new VersionViewer(node, linker.getSelectionContext().getSingleSelection().getLanguageCode(),
                    linker, "live", false, displayVersionSelector);
        } else {
            leftPanel = new VersionViewer(uuid, locale, "live", false, displayVersionSelector);
        }
        leftPanel.setSize(650, 750);
        BorderLayoutData liveLayoutData = new BorderLayoutData(Style.LayoutRegion.WEST, 650);
        liveLayoutData.setCollapsible(true);
        add(leftPanel, liveLayoutData);

        // staging version
        if (node != null) {
            rightPanel = new VersionViewer(node, linker.getSelectionContext().getSingleSelection().getLanguageCode(),
                    linker, "default", true, displayVersionSelector) {
                @Override
                public String getCompareWith() {
                    return leftPanel.getInnerHTML();
                }
            };
        } else {
            rightPanel = new VersionViewer(uuid, locale, "default", true, displayVersionSelector) {
                @Override
                public String getCompareWith() {
                    return leftPanel.getInnerHTML();
                }
            };
        }
        rightPanel.setSize(650, 750);
        BorderLayoutData stagingLayoutData = new BorderLayoutData(Style.LayoutRegion.CENTER, 650);
        add(rightPanel, stagingLayoutData);

        LayoutContainer buttonsPanel = new LayoutContainer();
        buttonsPanel.setBorders(false);

        buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);

        //initFooter();

        buttonsPanel.add(buttonBar);
        setBottomComponent(buttonsPanel);

        setFooter(true);
    }


    /**
     * init buttons
     */
    protected void initFooter() {
        Button cancel = new Button(Messages.get("label.cancel"));
        cancel.setHeight(BUTTON_HEIGHT);
        cancel.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                CompareEngine.this.hide();
            }
        });
        buttonBar.add(cancel);
    }


}


