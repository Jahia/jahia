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
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
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
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Definition Tab Item
 * used to edit definition properties
 * same as ContentTabItem but change the behavior of the name field
 * User: david
 * Date: 5/13/13
 * Time: 11:05 AM
 */
@SuppressWarnings("serial")
public class NamespaceTabItem extends ContentTabItem {

    @Override
    protected void setNameField(NodeHolder engine, AsyncTabItem tab) {
        if (!engine.isMultipleSelection()) {
            tab.setLayout(new RowLayout());
            final FormLayout fl = new FormLayout();
            fl.setLabelWidth(0);
            final HBoxLayout hBoxLayout = new HBoxLayout();
            hBoxLayout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.MIDDLE);
            final LayoutContainer panel = new LayoutContainer(hBoxLayout);
            if (nameText == null) {
                nameFieldSet = new FieldSet();
                nameFieldSet.setHeadingHtml(Messages.get("label.type.name", "Type name"));
                nameFieldSet.setLayout(fl);
                nameText = new NameField();
                nameText.addStyleName("JahiaGxtField_systemName");
                nameText.setWidth("250");
                ((NameField) nameText).setMaxLength(maxNameSize);
                ((NameField) nameText).setAllowBlank(false);
                nameText.setStyleAttribute("padding-left", "0");
                nameText.setFireChangeEventOnSetValue(true);
                panel.add(this.nameText, new HBoxLayoutData(0, 5, 0, 5));

                name = new AdapterField(panel);
                name.setFieldLabel(Messages.get("label.type.name", "Type name"));

                FormData fd = new FormData("98%");
                fd.setMargins(new Margins(0));
                nameFieldSet.add(name, fd);
                if (engine.getNode() != null && engine.getNode().isLocked()) {
                    nameText.setReadOnly(true);
                    if (autoUpdateName != null) {
                        autoUpdateName.setEnabled(false);
                    }
                }
            }

            String nodeName = engine.getNodeName();
            if (nodeName != null) {
                if (nodeName.equals("primary-nodetype")) {
                    nodeName = "jnt:newNodeType";
                } else if (nodeName.equals("mixin-nodetype")) {
                    nodeName = "jmix:newMixin";
                }
            }

            nameText.setValue(nodeName);
        }
    }

    private class NameField extends MultiField<String> {
        private transient SimpleComboBox<String> namespaces;
        private transient TextField<String> name;

        public NameField() {
            namespaces = new SimpleComboBox<String>();
            namespaces.setTriggerAction(ComboBox.TriggerAction.ALL);
            name = new TextField<String>();
            JahiaContentManagementService.App.getInstance().getNamespaces(
                    new AsyncCallback<List<String>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            //it does not work
                        }

                        @Override
                        public void onSuccess(List<String> result) {
                            result.remove(namespaces.getSimpleValue());
                            namespaces.add(result);
                        }
                    }
            );
            add(namespaces);
            add(name);
            name.setValidator(new Validator() {
                @Override
                public String validate(Field<?> field, String value) {
                    return value == null || !value.matches("[A-Za-z0-9_]+") ? Messages.get(
                            "label.error.invalidNodeTypeName", "The entered node type name is not valid."
                                    + " The value should match the following pattern: [A-Za-z0-9_]+") : null;
                }
            });
        }

        @Override
        public String getValue() {
            return namespaces.getSimpleValue() == null || namespaces.getSimpleValue().trim().equals("")?name.getValue():namespaces.getSimpleValue() + ":" + name.getValue();
        }

        @Override
        public void setValue(String value) {
            if (value != null && value.contains(":")) {
                String[] s = value.split(":");
                namespaces.add(s[0]);
                namespaces.setSimpleValue(s[0]);
                namespaces.select(namespaces.getStore().findModel(s[0]));
                name.setValue(s[1]);
            } else {
                name.setValue(value);
            }
        }

        public void setMaxLength(int maxLength) {
            name.setMaxLength(maxLength);
        }

        public void setAllowBlank(boolean allowBlank) {
            name.setAllowBlank(allowBlank);
        }
    }
}
