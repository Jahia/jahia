/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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
<<<<<<< .working
=======
        if (configuration.equals("userpicker")) {
            new UserGroupSelect(new UserPickerAdder(), UserGroupSelect.VIEW_USERS, "site:" + JahiaGWTParameters.getSiteNode().getName(), !multiple);
        } else if (configuration.equals("usergrouppicker")) {
            new UserGroupSelect(new UserPickerAdder(), UserGroupSelect.VIEW_TABS, "site:" + JahiaGWTParameters.getSiteNode().getName(), !multiple);
        } else {
            JahiaContentManagementService.App.getInstance()
                    .getManagerConfiguration(configuration, new BaseAsyncCallback<GWTManagerConfiguration>() {
                        public void onSuccess(GWTManagerConfiguration config) {
                            PermissionsUtils.loadPermissions(config.getPermissions());
                            final Window w = new Window();
                            w.setLayout(new FitLayout());
                            w.setId("JahiaGxtContentPickerWindow");
                            final ContentPicker contentPicker =
                                    new ContentPicker(selectorOptions, getValue(), types, filters, mimeTypes,
                                            config, multiple);
>>>>>>> .merge-right.r49094

<<<<<<< .working
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
=======
                            w.setHeading(Messages.get("label." + config.getName(), config.getName()));
                            int windowHeight=com.google.gwt.user.client.Window.getClientHeight()-10;
>>>>>>> .merge-right.r49094

<<<<<<< .working
                            w.setHeadingHtml(Messages.get("label." + config.getName(), config.getName()));
                            int windowHeight = com.google.gwt.user.client.Window.getClientHeight() - 10;
=======
                            w.setModal(true);
                            w.setSize(900, windowHeight);
                            w.setResizable(true);
                            w.setMaximizable(true);
                            w.setBodyBorder(false);
>>>>>>> .merge-right.r49094

<<<<<<< .working
                            w.setModal(true);
                            w.setSize(900, windowHeight);
                            w.setResizable(true);
                            w.setMaximizable(true);
                            w.setBodyBorder(false);
=======
                            final ButtonBar bar = new ButtonBar();
                            bar.setAlignment(Style.HorizontalAlignment.CENTER);
>>>>>>> .merge-right.r49094

<<<<<<< .working
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
=======
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
                            if (getValue() == null || getValue().size() ==0) {
                                ok.setEnabled(false);
>>>>>>> .merge-right.r49094
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

<<<<<<< .working
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
                    Log.error("Error while loading user permission", throwable);
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
                    Log.error("Error while loading user permission", throwable);
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

=======
    private class UserPickerAdder implements UserGroupAdder {
        public void addUsers(List<GWTJahiaUser> users) {
            List<String> l = new ArrayList<String>();
            for (GWTJahiaUser user : users) {
                l.add(user.getKey());
            }
            mask();
            JahiaContentManagementService.App.getInstance().getNodesForUsers(l, new BaseAsyncCallback<List<GWTJahiaNode>>() {
                @Override
                public void onApplicationFailure(Throwable throwable) {
                    Log.error("Error while loading user permission", throwable);
                    unmask();
                }

                public void onSuccess(List<GWTJahiaNode> result) {
                    setValue(result);
                    unmask();
                }
            });
        }

        public void addGroups(List<GWTJahiaGroup> groups) {
            List<String> l = new ArrayList<String>();
            for (GWTJahiaGroup group : groups) {
                l.add(group.getKey());
            }
            mask();
            JahiaContentManagementService.App.getInstance().getNodesForGroups(l, new BaseAsyncCallback<List<GWTJahiaNode>>() {
                @Override
                public void onApplicationFailure(Throwable throwable) {
                    Log.error("Error while loading user permission", throwable);
                    unmask();
                }

                public void onSuccess(List<GWTJahiaNode> result) {
                    setValue(result);
                    unmask();
                }
            });

        }
    }

>>>>>>> .merge-right.r49094
}

