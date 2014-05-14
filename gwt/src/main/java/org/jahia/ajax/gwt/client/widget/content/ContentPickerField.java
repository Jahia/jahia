/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
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
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
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
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.PropertyEditor;
import com.extjs.gxt.ui.client.widget.form.TwinTriggerField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupAdder;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupSelect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * User: toto
 * Date: Dec 1, 2008
 * Time: 6:37:07 PM
 */
public class ContentPickerField extends TwinTriggerField<List<GWTJahiaNode>> {
//    protected El clear;
//    protected String clearStyle = "x-form-clear-trigger";

    private List<GWTJahiaNode> value;
    private String selectionLabel;
    private List<String> types;
    private List<String> filters;
    private List<String> mimeTypes;
    private String configuration;
    private boolean multiple;
    private Map<String, String> selectorOptions;

    public ContentPickerField(Map<String, String> selectorOptions, List<String> types, List<String> filters, List<String> mimeTypes, String configuration, boolean multiple) {
        super();
        setPropertyEditor(new PropertyEditor<List<GWTJahiaNode>>() {
            public String getStringValue(List<GWTJahiaNode> value) {
                StringBuilder s = new StringBuilder();
                for (Iterator<GWTJahiaNode> it = value.iterator(); it.hasNext(); ) {
                    GWTJahiaNode currentNode = it.next();
                    if (currentNode.get("j:url") != null) {
                        s.append(currentNode.get("j:url"));
                    } else {
                        s.append(currentNode.getName());
                    }
                    if (it.hasNext()) {
                        s.append(", ");
                    }
                }
                return s.toString();
            }

            public List<GWTJahiaNode> convertStringValue(String value) {
                return new ArrayList<GWTJahiaNode>();
            }
        });
        this.types = types;
        this.filters = filters;
        this.mimeTypes = mimeTypes;
        this.configuration = configuration;
        this.multiple = multiple;
        this.selectorOptions = selectorOptions;
        setTwinTriggerStyle("x-form-clear-trigger");
        setEditable(false);
        setValue(new ArrayList<GWTJahiaNode>());
        propertyEditor = new PropertyEditor<List<GWTJahiaNode>>() {
            public String getStringValue(List<GWTJahiaNode> value) {
                StringBuilder result = new StringBuilder();
                for (GWTJahiaNode gwtJahiaNode : value) {
                    if (result.length() > 0) {
                        result.append(",");
                    }
                    result.append(gwtJahiaNode.getDisplayName());
                }
                return result.toString();
            }

            public List<GWTJahiaNode> convertStringValue(String value) {
                return null;
            }
        };
    }

    @Override
    protected void onTriggerClick(ComponentEvent ce) {
        super.onTriggerClick(ce);
        if (disabled || isReadOnly()) {
            return;
        }

        if (configuration.equals("userpicker")) {
            new UserGroupSelect(new UserPickerAdder(), UserGroupSelect.VIEW_USERS, "site:" + JahiaGWTParameters.getSiteNode().getName(), !multiple);
        } else if (configuration.equals("usergrouppicker")) {
            new UserGroupSelect(new UserPickerAdder(), UserGroupSelect.VIEW_TABS, "site:" + JahiaGWTParameters.getSiteNode().getName(), !multiple);
        } else {
            JahiaContentManagementService.App.getInstance()
                    .getManagerConfiguration(configuration, null, new BaseAsyncCallback<GWTManagerConfiguration>() {
                        public void onSuccess(GWTManagerConfiguration config) {
                            PermissionsUtils.loadPermissions(config.getPermissions());
                            final Window w = new Window();
                            w.setLayout(new FitLayout());
                            w.setId("JahiaGxtContentPickerWindow");
                            final ContentPicker contentPicker =
                                    new ContentPicker(selectorOptions, getValue(), types, filters, mimeTypes,
                                            config, multiple);

                            if (config.getTitle() != null) {
                                w.setHeadingHtml(config.getTitle());
                            } else {
                                w.setHeadingHtml(Messages.get("label." + config.getName(), config.getName()));
                            }
                            int windowHeight = com.google.gwt.user.client.Window.getClientHeight() - 10;

                            w.setModal(true);
                            w.setSize(900, windowHeight);
                            w.setResizable(true);
                            w.setMaximizable(true);
                            w.setBodyBorder(false);

                            final ButtonBar bar = new ButtonBar();
                            bar.setAlignment(Style.HorizontalAlignment.CENTER);

                            final Button ok = new Button(Messages.get("label.save"), new SelectionListener<ButtonEvent>() {
                                public void componentSelected(ButtonEvent event) {
                                    List<GWTJahiaNode> selection = contentPicker.getSelectedNodes();
                                    setValue(selection);
                                    w.hide();
                                }
                            });
                            ok.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
                            bar.add(ok);

                            contentPicker.setSaveButton(ok);
                            if (getValue() == null || getValue().size() == 0) {
                                ok.setEnabled(false);
                            }

                            final Button cancel =
                                    new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
                                        public void componentSelected(ButtonEvent event) {
                                            w.hide();
                                        }
                                    });
                            cancel.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonCancel());

                            bar.add(cancel);
                            w.add(contentPicker);
                            w.setBottomComponent(bar);
                            w.show();
                        }

                        public void onApplicationFailure(Throwable throwable) {
                            Log.error("Error while loading user permission", throwable);
                        }
                    });
        }
    }

    protected void onTwinTriggerClick(ComponentEvent ce) {
        if (disabled || isReadOnly()) {
            return;
        }
        setValue(new ArrayList<GWTJahiaNode>());
    }

    @Override
    public List<GWTJahiaNode> getValue() {
        Log.debug("Get value: " + value);
        return value;
    }

    @Override
    public void setValue(List<GWTJahiaNode> value) {
        this.value = value;
        super.setValue(value);
    }

    private class UserPickerAdder implements UserGroupAdder {
        @Override
        public void addUsers(List<GWTJahiaUser> users) {
            List<String> l = new ArrayList<String>();
            for (GWTJahiaUser user : users) {
                l.add(user.getKey());
            }
            mask();
            JahiaContentManagementService.App.getInstance().getNodesForUsers(l, new BaseAsyncCallback<List<GWTJahiaNode>>() {
                @Override
                public void onApplicationFailure(Throwable throwable) {
                    Log.error("Error while loading users", throwable);
                    unmask();
                }

                @Override
                public void onSuccess(List<GWTJahiaNode> result) {
                    setValue(result);
                    unmask();
                }
            });
        }

        @Override
        public void addGroups(List<GWTJahiaGroup> groups) {
            List<String> l = new ArrayList<String>();
            for (GWTJahiaGroup group : groups) {
                l.add(group.getKey());
            }
            mask();
            JahiaContentManagementService.App.getInstance().getNodesForGroups(l, new BaseAsyncCallback<List<GWTJahiaNode>>() {
                @Override
                public void onApplicationFailure(Throwable throwable) {
                    Log.error("Error while loading groups", throwable);
                    unmask();
                }

                @Override
                public void onSuccess(List<GWTJahiaNode> result) {
                    setValue(result);
                    unmask();
                }
            });

        }
    }

}

