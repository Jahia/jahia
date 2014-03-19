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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.*;

/**
 * Create button for resources (css or javascript).
 */
public class CreateResourceButtonItem extends SaveButtonItem {

    private String fileExtension;

    public Button create(final AbstractContentEngine engine) {
        Button button = new Button(Messages.get("label.save", "Save"));
        button.setHeight(BUTTON_HEIGHT);
        button.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                GWTJahiaNode node = engine.getNode();
                if (node == null) {
                    node = engine.getTargetNode();
                }
                final String nodePath = node.getPath();

                final Window popup = new Window();
                popup.setHeadingHtml(Messages.get("label.saveAs", "Save as..."));
                popup.setHeight(120);
                popup.setWidth(350);
                popup.setModal(true);
                FormPanel f = new FormPanel();
                f.setHeaderVisible(false);
                f.setBorders(false);
                final TextField<String> name = new TextField<String>();
                name.setFieldLabel(Messages.get("label.name", "Name"));
                name.setMinLength(1);
                f.add(name);

                Button b = new Button(Messages.get("label.submit", "submit"));
                f.addButton(b);
                b.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                        Map<String, String> parentNodesType = new LinkedHashMap<String, String>();
                        parentNodesType.put(nodePath, "jnt:folder");
                        String finalName = name.getValue();
                        if (fileExtension != null && !finalName.endsWith("." + fileExtension)) {
                            finalName += "." + fileExtension;
                        }
                        prepareAndSave(nodePath, finalName, parentNodesType, (CreateContentEngine) engine);
                        popup.hide();
                    }
                });
                Button c = new Button(Messages.get("label.cancel", "Cancel"));
                c.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                        popup.hide();
                    }
                });
                f.addButton(c);
                f.setButtonAlign(Style.HorizontalAlignment.CENTER);

                FormButtonBinding binding = new FormButtonBinding(f);
                binding.addButton(b);
                popup.add(f);
                popup.setFocusWidget(name);
                popup.show();
            }
        });
        return button;
    }

    protected void prepareAndSave(final String parentPath, String nodeName, Map<String, String> parentNodesType, final CreateContentEngine engine) {

        final Set<String> addedTypes = new HashSet<String>();
        final Set<String> removedTypes = new HashSet<String>();

        for (TabItem tab : engine.getTabs().getItems()) {
            EditEngineTabItem item = tab.getData("item");
            item.doSave(null, engine.getChangedProperties(), engine.getChangedI18NProperties(), addedTypes, removedTypes, null, engine.getAcl());
        }
        List<GWTJahiaNodeProperty> changedProperties = engine.getChangedProperties();
        List<GWTJahiaNodeProperty> properties = null;
        if (engine.getProperties().size() > 0) {
            // Edit
            Map<String, GWTJahiaNodeProperty> engineProperties = new HashMap<String, GWTJahiaNodeProperty>(engine.getProperties());
            for (GWTJahiaNodeProperty changed : changedProperties) {
                GWTJahiaNodeProperty prop = engineProperties.get(changed.getName());
                if (prop != null) {
                    prop.setValues(changed.getValues());
                }
            }
            properties = new ArrayList<GWTJahiaNodeProperty>(engineProperties.values());
        } else {
            // Create
            properties = changedProperties;
        }
        JahiaContentManagementService.App.getInstance().createNode(parentPath, nodeName, engine.getType().getName(), null, engine.getAcl(), properties, engine.changedI18NProperties, null, parentNodesType, false, new AsyncCallback<GWTJahiaNode>() {
            public void onFailure(Throwable throwable) {
                MessageBox.alert(Messages.get("label.error.processingRequestError","An error occurred while processing your request"), throwable.getMessage(), null);
            }
            public void onSuccess(GWTJahiaNode gwtJahiaNode) {
                Linker linker = engine.getLinker();
                engine.close();
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("node", gwtJahiaNode);
                linker.refresh(data);
            }
        });


    }

    @Override
    protected void prepareAndSave(final AbstractContentEngine engine, boolean closeAfterSave) {
        //Nothing to do here
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }
}
