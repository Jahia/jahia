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

import com.extjs.gxt.ui.client.data.RpcMap;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanelTabItem;

import java.util.*;

/**
 * Button Item for update - used for edit content.
 */
public class UpdateButtonItem extends SaveButtonItem {

    @Override
    protected void prepareAndSave(final AbstractContentEngine engine, final boolean closeAfterSave) {
        // node
        final Set<String> addedTypes = new HashSet<String>();
        final Set<String> removedTypes = new HashSet<String>();

        for (TabItem tab : engine.getTabs().getItems()) {
            EditEngineTabItem item = tab.getData("item");
            // case of contentTabItem
            if (item instanceof ContentTabItem) {
                if (((ContentTabItem) item).isNodeNameFieldDisplayed()) {
                    Field<String> name = ((ContentTabItem) item).getName();
                    if(!name.isValid()) {
                        com.google.gwt.user.client.Window.alert(name.getErrorMessage());
                        engine.unmask();
                        engine.setButtonsEnabled(true);
                        return;
                    }
                    engine.setNodeName(name.getValue());
                    engine.getNode().setName(engine.getNodeName());
                }
                final List<CheckBox> validLanguagesChecked = ((ContentTabItem) item).getCheckedLanguagesCheckBox();
                if (!validLanguagesChecked.isEmpty()) {
                    final List<GWTJahiaLanguage> siteLanguages = JahiaGWTParameters.getSiteLanguages();
                    List<String> invalidLanguages = engine.getNode().getInvalidLanguages();
                    boolean doCheck = false;
                    List<String> newInvalidLanguages = new ArrayList<String>();
                    for (GWTJahiaLanguage language : siteLanguages) {
                        boolean found = false;
                        for (CheckBox validLang : validLanguagesChecked) {
                            if (language.getLanguage().equals(validLang.getValueAttribute())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            newInvalidLanguages.add(language.getLanguage());
                        }
                    }
                    boolean hasChanged = newInvalidLanguages.size() != invalidLanguages.size();
                    if (!hasChanged) {
                        for (String lang : newInvalidLanguages) {
                            if (!invalidLanguages.contains(lang)) {
                                hasChanged = true;
                                break;
                            }
                        }
                    }
                    if (hasChanged) {
                        List<String> strings = new ArrayList<String>(siteLanguages.size());
                        for (GWTJahiaLanguage siteLanguage : siteLanguages) {
                            strings.add(siteLanguage.getLanguage());
                        }
                        GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
                        gwtJahiaNodeProperty.setName("j:invalidLanguages");
                        gwtJahiaNodeProperty.setMultiple(true);
                        for (CheckBox value : validLanguagesChecked) {
                            if (value.getValue()) {
                                strings.remove(value.getValueAttribute());
                            }
                        }
                        if(strings.size()>0) {
                            gwtJahiaNodeProperty.setValues(new ArrayList<GWTJahiaNodePropertyValue>());
                            for (String string : strings) {
                                gwtJahiaNodeProperty.getValues().add(new GWTJahiaNodePropertyValue(string));
                            }
                        }
                        final List<GWTJahiaNodePropertyValue> gwtJahiaNodePropertyValues = gwtJahiaNodeProperty.getValues();
                        if (gwtJahiaNodePropertyValues!=null && gwtJahiaNodePropertyValues.size() > 0) {
                            engine.getChangedProperties().add(gwtJahiaNodeProperty);
                            addedTypes.add("jmix:i18n");
                        } else {
                            gwtJahiaNodeProperty.setValues(new ArrayList<GWTJahiaNodePropertyValue>());
                            engine.getChangedProperties().add(gwtJahiaNodeProperty);
                        }
                    }
                }
            }

            // case of right tab
            item.doSave(engine.getNode(), engine.getChangedProperties(), engine.getChangedI18NProperties(), addedTypes, removedTypes, null, engine.getAcl());
        }

        engine.getNode().getNodeTypes().removeAll(removedTypes);
        engine.getNode().getNodeTypes().addAll(addedTypes);

        engine.removeUneditedLanguages();

        JahiaContentManagementService.App.getInstance().saveNode(engine.getNode(),
                engine.getAcl(), engine.getChangedI18NProperties(), engine.getChangedProperties(),
                removedTypes, new BaseAsyncCallback<RpcMap>() {
            public void onApplicationFailure(Throwable throwable) {
                failSave(engine, throwable);
            }

            public void onSuccess(RpcMap o) {
                Info.display(Messages.get("label.information", "Information"), Messages.get("saved_prop", "Properties saved\n\n"));
                Map<String, Object> data = new HashMap<String, Object>();
                data.put(Linker.REFRESH_MAIN, true);
                data.put("forceImageRefresh", true);
                EditLinker l = null;
                if (engine.getLinker() instanceof SidePanelTabItem.SidePanelLinker) {
                    l = ((SidePanelTabItem.SidePanelLinker) engine.getLinker()).getEditLinker();
                } else if (engine.getLinker() instanceof EditLinker) {
                    l = (EditLinker) engine.getLinker();
                }
                GWTJahiaNode node = engine.getNode();
                if (l != null && node.equals(l.getMainModule().getNode()) && !node.getName().equals(l.getMainModule().getNode().getName())) {
                    l.getMainModule().handleNewMainSelection(node.getPath().substring(0, node.getPath().lastIndexOf("/") + 1) + node.getName(), l.getMainModule().getTemplate());
                }
                data.put("node", node);
                ((EditContentEngine) engine).closeEngine();
                if (o != null && o.containsKey(GWTJahiaNode.SITE_LANGUAGES)) {
                    JahiaGWTParameters.getSiteNode().set(GWTJahiaNode.SITE_LANGUAGES, o.get(GWTJahiaNode.SITE_LANGUAGES));
                }
                engine.getLinker().refresh(data);
            }
        });
    }

}
