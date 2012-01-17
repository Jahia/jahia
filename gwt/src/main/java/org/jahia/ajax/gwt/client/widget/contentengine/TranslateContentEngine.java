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

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.TextArea;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.definition.FormFieldCreator;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.ContentPickerField;
import org.jahia.ajax.gwt.client.widget.definition.LangPropertiesEditor;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.widget.form.CKEditorField;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 
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
        int windowHeight=com.google.gwt.user.client.Window.getClientHeight()-10;
        int windowWidth=com.google.gwt.user.client.Window.getClientWidth()-10;

        setSize(windowWidth, windowHeight);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        setHeading(Messages.get("cm_translate " + node.getName(), "Translate " + node.getName()));
        LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new BorderLayout());

        sourceLangPropertiesEditor = new LangPropertiesEditor(node, Arrays.asList(GWTJahiaItemDefinition.CONTENT), false, srcLanguage);
        targetLangPropertiesEditor = new LangPropertiesEditor(node, Arrays.asList(GWTJahiaItemDefinition.CONTENT), true, destLanguage);

        Button widget = new Button(Messages.get("label.translate.copy", "Copy to other language"));
        sourceLangPropertiesEditor.getTopBar().add(widget);
        widget.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                List<GWTJahiaNodeProperty> props = sourceLangPropertiesEditor.getPropertiesEditorByLang(sourceLangPropertiesEditor.getDisplayedLocale().getLanguage()).getProperties();
                Map<String,PropertiesEditor.PropertyAdapterField> fieldsMap = targetLangPropertiesEditor.getPropertiesEditorByLang(targetLangPropertiesEditor.getDisplayedLocale().getLanguage()).getFieldsMap();
                for (final GWTJahiaNodeProperty prop : props) {
                    final Field<?> f = fieldsMap.get(prop.getName()).getField();
                    FormFieldCreator.copyValue(prop, f);
                }
            }
        });

        panel.add(sourceLangPropertiesEditor, new BorderLayoutData(Style.LayoutRegion.WEST, windowWidth/2));
        panel.add(targetLangPropertiesEditor, new BorderLayoutData(Style.LayoutRegion.EAST, windowWidth/2));

        add(panel);

        LayoutContainer buttonsPanel = new LayoutContainer();
        buttonsPanel.setBorders(false);

        buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);

        initFooter();

        buttonsPanel.add(buttonBar);

        setBottomComponent(buttonsPanel);

        setFooter(true);
        layout();
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
            JahiaContentManagementService.App.getInstance().savePropertiesAndACL(nodes, null, targetLangPropertiesEditor.getLangPropertiesMap(), null, null, new BaseAsyncCallback<Object>() {
                public void onApplicationFailure(Throwable throwable) {
                    String message = throwable.getMessage();
                    if (message.contains("Invalid link")) {
                        message = Messages.get("label.error.invalidlink", "Invalid link") + " : " + message.substring(message.indexOf(":")+1);
                    }
                    com.google.gwt.user.client.Window.alert(Messages.get("label.error.invalidlink", "Properties save failed") + "\n\n"
                            + message);
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

