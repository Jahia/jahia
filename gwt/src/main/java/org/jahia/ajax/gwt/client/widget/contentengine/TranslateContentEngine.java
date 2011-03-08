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

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.*;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.definition.LangPropertiesEditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Jan 20, 2010
 * Time: 1:53:30 PM
 * 
 */
public class TranslateContentEngine extends Window {
    public static final int BUTTON_HEIGHT = 24;
    private GWTJahiaNode node;
    private final GWTJahiaLanguage srcLanguage;
    private final GWTJahiaLanguage destLanguage;
    private Linker linker = null;

    private Button ok;
    private LangPropertiesEditor sourceLangPropertiesEditor;
    private LangPropertiesEditor targetLangPropertiesEditor;
    protected ButtonBar buttonBar;


    /**
     * Initializes an instance of this class.
     *
     * @param node   the content object to be edited
     * @param linker the edit linker for refresh purpose
     * @param srcLanguage
     * @param destLanguage
     */
    public TranslateContentEngine(GWTJahiaNode node, Linker linker, GWTJahiaLanguage srcLanguage,
                                  GWTJahiaLanguage destLanguage) {
        this.linker = linker;
        this.node = node;
        this.srcLanguage = srcLanguage;
        this.destLanguage = destLanguage;

        init();
    }

    protected void init() {
        setLayout(new FitLayout());
        setBodyBorder(false);
        setSize(1024, 600);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        setHeading(Messages.get("cm_translate " + node.getName(), "Translate " + node.getName()));
        LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new RowLayout(Style.Orientation.HORIZONTAL));
        panel.setWidth("100%");
        panel.setHeight("100%");

        sourceLangPropertiesEditor = new LangPropertiesEditor(node, Arrays.asList(GWTJahiaItemDefinition.CONTENT), false, srcLanguage);
        sourceLangPropertiesEditor.setSize(504, 530);
        targetLangPropertiesEditor = new LangPropertiesEditor(node, Arrays.asList(GWTJahiaItemDefinition.CONTENT), true, destLanguage);
        targetLangPropertiesEditor.setSize(504, 530);

        panel.add(sourceLangPropertiesEditor, new RowData(504, 530, new Margins(5)));
        panel.add(targetLangPropertiesEditor, new RowData(504, 530, new Margins(5)));

        add(panel);

        LayoutContainer buttonsPanel = new LayoutContainer();
        buttonsPanel.setBorders(false);

        buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);

        initFooter();

        buttonsPanel.add(buttonBar);

        setBottomComponent(buttonsPanel);

        setFooter(true);
    }


    /**
     * init buttons
     */
    protected void initFooter() {
        ok = new Button(Messages.get("label.save"));
        ok.setHeight(BUTTON_HEIGHT);
        ok.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
        ok.addSelectionListener(new SaveSelectionListener());
        buttonBar.add(ok);

        Button cancel = new Button(Messages.get("label.cancel"));
        cancel.setHeight(BUTTON_HEIGHT);
        cancel.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                TranslateContentEngine.this.hide();
            }
        });
        buttonBar.add(cancel);
    }


    /**
     * Save selection listener
     */
    private class SaveSelectionListener extends SelectionListener<ButtonEvent> {
        public void componentSelected(ButtonEvent event) {
            // node
            final List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
            nodes.add(node);


            // Ajax call to update values
            JahiaContentManagementService.App.getInstance().savePropertiesAndACL(nodes, null, targetLangPropertiesEditor.getLangPropertiesMap(), null, new BaseAsyncCallback<Object>() {
                public void onApplicationFailure(Throwable throwable) {
                    com.google.gwt.user.client.Window.alert(Messages.get("saved_prop_failed", "Properties save failed\n\n") + throwable.getLocalizedMessage());
                    Log.error("failed", throwable);
                }

                public void onSuccess(Object o) {
                    Info.display(Messages.get("label.information", "Information"), Messages.get("saved_prop", "Properties saved\n\n"));
                    TranslateContentEngine.this.hide();
                    linker.refresh(Linker.REFRESH_MAIN);
                }
            });
        }

    }
}

