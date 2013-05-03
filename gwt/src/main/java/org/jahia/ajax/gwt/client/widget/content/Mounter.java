/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.*;

/**
 * Window used to display mount properties for mounting external providers.
 * 
 * @author rfelden
 */
public class Mounter extends Window {
    final Button submit = new Button(Messages.get("label.ok")) ;
    final FormPanel form = new FormPanel() ;

    /**
     * default Mounter constructor
     * @param linker
     */
    public Mounter(final Linker linker) {
        super() ;
        setHeading(Messages.get("label.mount"));
        setSize(500, 250);
        setResizable(false);
        ButtonBar buttons = new ButtonBar() ;
        form.setLabelWidth(150);
        form.setFieldWidth(300);
        form.setBodyBorder(false);
        form.setBorders(false);
        form.setHeaderVisible(false);
        setModal(true);
        final ComboBox<GWTJahiaNodeType> factoriesTypeBox = new ComboBox<GWTJahiaNodeType>();
        factoriesTypeBox.setDisplayField("label");
        factoriesTypeBox.setAllowBlank(false);
        factoriesTypeBox.setFieldLabel(Messages.get("label.chooose.providerType", "choose provider"));
        final ListStore<GWTJahiaNodeType> store =  new ListStore<GWTJahiaNodeType>();
        factoriesTypeBox.setStore(store);
        final SelectionListener<ButtonEvent> selectionListener = new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                factoriesTypeBox.removeFromParent();
                submit.removeSelectionListener(this);
                mountPointPropertiesEditor(factoriesTypeBox.getValue(),linker);
            }

        };
        JahiaContentManagementService.App.getInstance().getProviderFactoriesType(new BaseAsyncCallback<List<GWTJahiaNodeType>>() {
            @Override
            public void onSuccess(List<GWTJahiaNodeType> result) {
                if (result.size() > 1) {
                    store.add(result);
                    factoriesTypeBox.setValue(result.get(0));
                    show();
                }
                else if (result.size() == 1) {
                    factoriesTypeBox.removeFromParent();
                    submit.removeSelectionListener(selectionListener);
                    mountPointPropertiesEditor(result.get(0),linker);
                    show();
                }
                else {
                    MessageBox.info(Messages.get("label.information", "Information"),
                                    Messages.get("label.noProviders", "no provider defined"), null);
                }
            }
        });
        form.add(factoriesTypeBox, new FormData());

        final Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide() ;
            }
        });
        submit.addSelectionListener(selectionListener);
        buttons.add(submit) ;
        buttons.add(cancel) ;
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setBottomComponent(buttons);

        add(form);
        setScrollMode(Style.Scroll.AUTO);

    }
    private void mountPointPropertiesEditor(final GWTJahiaNodeType type,final Linker linker) {
        final TextField<String> mountName = new TextField<String>();
        mountName.setFieldLabel(Messages.get("label.name","name"));
        mountName.setEmptyText(Messages.get("label.enterMountNodeName", "mount node name"));
        form.add(mountName, new FormData());
        final PropertiesEditor pe = new PropertiesEditor(Arrays.asList(type),new HashMap<String, GWTJahiaNodeProperty>(), Arrays.asList(GWTJahiaItemDefinition.CONTENT));
        pe.renderNewFormPanel();
        form.add(pe);
        submit.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                JahiaContentManagementService.App.getInstance().mount(
                        mountName.getValue(),
                        type.getName(),
                        pe.getProperties(true, true, false),
                        new AsyncCallback<Object>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                MessageBox.alert(Messages.get("label.error", "error"), caught.getMessage(), null);
                                hide();
                            }

                            @Override
                            public void onSuccess(Object result) {
                                Map<String,Object> data = new HashMap<String, Object>();
                                data.put(Linker.REFRESH_ALL, true);
                                linker.refresh(data);
                                hide();
                            }
                        });
            }
        });
        layout();
    }

}