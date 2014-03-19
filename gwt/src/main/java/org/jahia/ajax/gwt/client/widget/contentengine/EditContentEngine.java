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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaEditEngineInitBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Content editing widget.
 *
 * @author Sergiy Shyrkov
 */
public class EditContentEngine extends AbstractContentEngine {

    private String contentPath;
    private HandlerRegistration handlerRegistration;

    private List<Button> saveButtons = new ArrayList<Button>();

    private Map<String, GWTJahiaGetPropertiesResult> langCodeGWTJahiaGetPropertiesResultMap =
            new HashMap<String, GWTJahiaGetPropertiesResult>();
    private boolean hasOrderableChildNodes;

    /**
     * Initializes an instance of this class.
     *
     * @param node   the content object to be edited
     * @param linker the edit linker for refresh purpose
     */
    public EditContentEngine(GWTJahiaNode node, Linker linker, EngineContainer engineContainer) {
        super(linker.getConfig().getEngineConfiguration(node), linker, node.getPath().substring(0, node.getPath().lastIndexOf('/')));
        contentPath = node.getPath();
        nodeName = node.getName();
        init(engineContainer);
        loadEngine();


        handlerRegistration = Window.addCloseHandler(new CloseHandler<Window>() {
            public void onClose(CloseEvent<Window> event) {
                close();
            }
        });
        //setTopComponent(toolBar);
    }

    public void close() {
        JahiaContentManagementService.App.getInstance().closeEditEngine(contentPath, new BaseAsyncCallback<Object>() {
            public void onSuccess(Object result) {
            }
        });
        closeEngine();
    }

    protected void closeEngine() {
        if (handlerRegistration != null) {
            handlerRegistration.removeHandler();
            handlerRegistration = null;
        }
        container.closeEngine();
    }

    /**
     * Creates and initializes all window tabs.
     */
    protected void initTabs() {
        // container ID, concatenated to each tab's ID
        tabs.setId("JahiaGxtEditEngineTabs");
        for (GWTEngineTab tabConfig : config.getEngineTabs()) {
            EditEngineTabItem tabItem = tabConfig.getTabItem();
            if (tabConfig.getRequiredPermission() == null || PermissionsUtils.isPermitted(tabConfig.getRequiredPermission(), JahiaGWTParameters.getSiteNode())) {
                if ((tabItem.getHideForTypes().isEmpty() || !node.isNodeType(tabItem.getHideForTypes())) &&
                        ((hasOrderableChildNodes && tabItem.isOrderableTab()) || ( !tabItem.isOrderableTab() && (tabItem.getShowForTypes().isEmpty() || node.isNodeType(tabItem.getShowForTypes()))))) {
                    tabs.add(tabItem.create(tabConfig, this));
                }
            }
        }
        tabs.setSelection(tabs.getItem(0));
    }


    /**
     * init buttons
     */
    protected void initFooter() {
        for (ButtonItem buttonItem : config.getEditionButtons()) {
            Button button = buttonItem.create(this);
            saveButtons.add(button);
            buttonBar.add(button);
        }
        for (ButtonItem buttonItem : config.getCommonButtons()) {
            buttonBar.add(buttonItem.create(this));
        }

        setButtonsEnabled(false);
    }

    /**
     * load node
     */
    private void loadProperties() {
        JahiaContentManagementService.App.getInstance().getProperties(contentPath, getSelectedLanguage(), new BaseAsyncCallback<GWTJahiaGetPropertiesResult>() {
            public void onApplicationFailure(Throwable throwable) {
                Log.debug("Cannot get properties", throwable);
            }

            public void onSuccess(GWTJahiaGetPropertiesResult result) {
                node = result.getNode();
                nodeTypes = result.getNodeTypes();
                properties = result.getProperties();
                currentLanguageBean = result.getCurrentLocale();

                // set selectedNode as processed
                if (getSelectedLanguage() != null) {
                    langCodeGWTJahiaGetPropertiesResultMap.put(getSelectedLanguage(), result);
                }

                fillCurrentTab();
            }
        });

    }

    /**
     * load node
     */
    private void loadEngine() {

        JahiaContentManagementService.App.getInstance().initializeEditEngine(contentPath, true, new BaseAsyncCallback<GWTJahiaEditEngineInitBean>() {
            public void onSuccess(GWTJahiaEditEngineInitBean result) {
                node = result.getNode();
                nodeTypes = result.getNodeTypes();
                properties = result.getProperties();
                currentLanguageBean = result.getCurrentLocale();
                defaultLanguageCode = result.getDefaultLanguageCode();
                hasOrderableChildNodes = result.hasOrderableChildNodes();
                langCodeGWTJahiaGetPropertiesResultMap.put(currentLanguageBean.getLanguage(), result);
                acl = result.getAcl();
                referencesWarnings = result.getReferencesWarnings();
                if (!PermissionsUtils.isPermitted("jcr:modifyProperties", node)) {
                    heading = Messages.getWithArgs("label.edit.engine.heading.read.only", "Read {0} ({1})", new String[]{nodeName, nodeTypes.get(0).getLabel()});
                } else {
                    heading = Messages.getWithArgs("label.edit.engine.heading.edit", "Edit {0} ({1})", new String[]{nodeName, nodeTypes.get(0).getLabel()});
                }
                container.getPanel().setHeadingHtml(heading);
                if (node.isLocked()) {
                    StringBuilder infos = new StringBuilder();
                    if (node.getLockInfos().containsKey(null) && node.getLockInfos().size() == 1) {
                        for (String s : node.getLockInfos().get(null)) {
                            infos.append(Formatter.getLockLabel(s));
                        }
                    } else {
                        for (Map.Entry<String, List<String>> entry : node.getLockInfos().entrySet()) {
                            if (entry.getKey() != null) {
                                if (infos.length() > 0) {
                                    infos.append("; ");
                                }
                                infos.append(entry.getKey()).append(" : ");
                                int i = 0;
                                for (String s : entry.getValue()) {
                                    if (i > 0) {
                                        infos.append(", ");
                                    }
                                    infos.append(Formatter.getLockLabel(s));
                                    i++;
                                }
                            }
                        }
                    }
                    heading = heading + "&nbsp;" + Messages.getWithArgs("label.edit.engine.heading.locked.by", "[ locked by {0} ]", new String[]{infos.toString()});
                    container.getPanel().setHeadingHtml(heading);
                } else if (node.getLockInfos() != null && !node.getLockInfos().isEmpty()) {
                    heading = heading + "&nbsp;" + Messages.get("label.edit.engine.heading.locked.by.you", "[ locked by you ]");
                    container.getPanel().setHeadingHtml(heading);
                }

                setAvailableLanguages(result.getAvailabledLanguages());

                // set selectedNode as processed
                if (getSelectedLanguage() != null) {
                    langCodeGWTJahiaGetPropertiesResultMap.put(getSelectedLanguage(), result);
                }

                mixin = result.getMixin();
                choiceListInitializersValues = result.getInitializersValues();
                defaultValues = result.getDefaultValues();
                initTabs();

                tabs.addListener(Events.Select, new Listener<ComponentEvent>() {
                    public void handleEvent(ComponentEvent event) {
                        fillCurrentTab();
                    }
                });

                fillCurrentTab();

                if (PermissionsUtils.isPermitted("jcr:modifyProperties", node) && !node.isLocked()) {
                    setButtonsEnabled(true);
                }
                loaded();
            }

            public void onApplicationFailure(Throwable throwable) {
                Log.debug("Cannot get properties", throwable);
                Info.display(Messages.get("label.error", "Error"), throwable.getLocalizedMessage());
                closeEngine();
            }

            @Override
            public void onSessionExpired() {
                closeEngine();
            }
        });

    }

    /**
     * on language chnage, reload the node
     *
     * @param previous
     */
    protected void onLanguageChange(GWTJahiaLanguage previous) {
        if (previous != null) {
            final String lang = previous.getLanguage();
            for (TabItem item : tabs.getItems()) {
                if (!changedI18NProperties.containsKey(lang)) {
                    changedI18NProperties.put(lang, new ArrayList<GWTJahiaNodeProperty>());
                }
                Object itemData = item.getData("item");
                if (itemData instanceof EditEngineTabItem) {
                    ((EditEngineTabItem) itemData).onLanguageChange(getSelectedLanguage(), item);
                }
            }
        }
        GWTJahiaGetPropertiesResult result = langCodeGWTJahiaGetPropertiesResultMap.get(getSelectedLanguage());
        if (result == null) {
            loadProperties();
        } else {
            node = result.getNode();
            nodeTypes = result.getNodeTypes();
            properties = result.getProperties();
            currentLanguageBean = result.getCurrentLocale();
            fillCurrentTab();
        }
    }


    public void setButtonsEnabled(final boolean enabled) {
        for (Button button : saveButtons) {
            button.setEnabled(enabled);
        }
    }

    @Override
    public String toString() {
        return node.getPath();
    }
}
