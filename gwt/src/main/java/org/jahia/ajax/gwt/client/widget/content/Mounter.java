/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
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
        setHeadingHtml(Messages.get("label.mount"));
        setSize(500, 350);
        ButtonBar buttons = new ButtonBar() ;
        form.setLabelWidth(140);
        form.setPadding(5);
        form.setCollapsible(false);
        form.setFrame(false);
        form.setAnimCollapse(false);
        form.setBorders(false);
        form.setBodyBorder(false);
        form.setHeaderVisible(false);
        form.setScrollMode(Style.Scroll.AUTO);
        form.setButtonAlign(Style.HorizontalAlignment.CENTER);
        form.setLayout(new FormLayout(FormPanel.LabelAlign.TOP));
        setModal(true);
        final ComboBox<GWTJahiaNodeType> factoriesTypeBox = new ComboBox<GWTJahiaNodeType>();
        factoriesTypeBox.setDisplayField("label");
        factoriesTypeBox.setAllowBlank(false);
        factoriesTypeBox.setFieldLabel(Messages.get("label.chooose.providerType", "choose provider"));
        factoriesTypeBox.setTypeAhead(false);
        factoriesTypeBox.setTriggerAction(ComboBox.TriggerAction.ALL);
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
        buttons.setAlignment(Style.HorizontalAlignment.CENTER);
        setBottomComponent(buttons);

        add(form);
        setScrollMode(Style.Scroll.AUTO);

    }
    private void mountPointPropertiesEditor(final GWTJahiaNodeType type,final Linker linker) {
        final TextField<String> mountName = new TextField<String>();
        mountName.setFieldLabel(Messages.get("label.name","name"));
        mountName.setEmptyText(Messages.get("label.enterMountNodeName", "mount node name"));
        mountName.setAllowBlank(false);
        mountName.setValidator(new Validator() {
            @Override
            public String validate(Field<?> field, String value) {
                return value == null || !value.matches("[A-Za-z0-9_]+") ? Messages.get(
                        "label.error.invalidNodeTypeName", "The entered node type name is not valid."
                                + " The value should match the following pattern: [A-Za-z0-9_]+"
                ) : null;
            }
        });
        mountName.setValidateOnBlur(true);
        form.setLayout(new FormLayout());
        form.add(mountName, new FormData());
        final PropertiesEditor pe = new PropertiesEditor(Arrays.asList(type),new HashMap<String, GWTJahiaNodeProperty>(), Arrays.asList(GWTJahiaItemDefinition.CONTENT));
        pe.renderNewFormPanel();
        pe.setLabelWidth(140);
        form.add(pe);
        submit.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                linker.loading(Messages.get("label.loading", "Loading"));
                if(form.isValid()) {
                    JahiaContentManagementService.App.getInstance().mount(
                            mountName.getValue(),
                            type.getName(),
                            pe.getProperties(true, true, false),
                            new AsyncCallback<Object>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    linker.loaded();
                                    MessageBox.alert(Messages.get("label.error", "error"), caught.getMessage(), null);
                                    hide();
                                }

                                @Override
                                public void onSuccess(Object result) {
                                    linker.loaded();
                                    Map<String, Object> data = new HashMap<String, Object>();
                                    data.put(Linker.REFRESH_ALL, true);
                                    linker.refresh(data);
                                    MessageBox.info(Messages.get("label.information", "Information"), Messages.get("message.success", "Success"), null);
                                    hide();
                                }
                            });
                }
            }
        });
        layout();
    }

}