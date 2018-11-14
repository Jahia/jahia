/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
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
package org.jahia.ajax.gwt.client.util.content.actions;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.FileUploader;
import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.content.portlet.PortletWizardWindow;
import org.jahia.ajax.gwt.client.widget.contentengine.AbstractContentEngine;
import org.jahia.ajax.gwt.client.widget.contentengine.ContentTabItem;
import org.jahia.ajax.gwt.client.widget.contentengine.EditEngineTabItem;
import org.jahia.ajax.gwt.client.widget.edit.ContentTypeWindow;

import java.util.*;

/**
 *
 *
 * @author rfelden
 * @version 7 juil. 2008 - 14:45:03
 */
public class ContentActions {

    /**
     * Pste as reference
     *
     * @param linker
     */
    public static void pasteReference(final Linker linker) {
        GWTJahiaNode m = linker.getSelectionContext().getSingleSelection();
        if (m != null) {
            linker.loading(Messages.get("statusbar.pastingref.label"));
            final CopyPasteEngine copyPasteEngine = CopyPasteEngine.getInstance();
            JahiaContentManagementService.App.getInstance().pasteReferences(JCRClientUtils.getPathesList(copyPasteEngine.getCopiedNodes()), m.getPath(), null, new BaseAsyncCallback<Object>() {

                @Override
                public void onApplicationFailure(Throwable throwable) {
                    Window.alert(Messages.get("failure.pasteref.label") + "\n" + throwable.getLocalizedMessage());
                    linker.loaded();
                }

                @Override
                public void onSuccess(Object o) {
                    boolean refresh = false;
                    for (GWTJahiaNode n : copyPasteEngine.getCopiedNodes()) {
                        if (!n.isFile()) {
                            refresh = true;
                            break;
                        }
                    }
                    copyPasteEngine.onPastedPath();
                    linker.loaded();
                    Map<String, Object> data = new HashMap<String, Object>();
                    if (refresh) {
                        data.put(Linker.REFRESH_ALL, true);
                    } else {
                        data.put(Linker.REFRESH_MAIN, true);
                    }
                    linker.refresh(data);
                }
            });
        }
    }


    /**
     * Move a file
     *
     * @param linker
     * @param sources
     * @param target
     */
    public static void move(final Linker linker, final List<GWTJahiaNode> sources, GWTJahiaNode target) {
        JahiaContentManagementService.App.getInstance().paste(JCRClientUtils.getPathesList(sources), target.getPath(), null, true, null, new BaseAsyncCallback<Object>() {

            @Override
            public void onApplicationFailure(Throwable throwable) {
                Window.alert("Paste failed :\n" + throwable.getLocalizedMessage());
                linker.loaded();
            }

            @Override
            public void onSuccess(Object o) {
                boolean refresh = false;
                for (GWTJahiaNode n : sources) {
                    if (!n.isFile()) {
                        refresh = true;
                        break;
                    }
                }

                linker.loaded();
                Map<String, Object> data = new HashMap<String, Object>();
                if (refresh) {
                    data.put(Linker.REFRESH_ALL, true);
                } else {
                    data.put(Linker.REFRESH_MAIN, true);
                }
                linker.refresh(data);
            }
        });
    }

    /**
     * Display download link
     *
     * @param linker
     */
    public static void download(final Linker linker) {
        final GWTJahiaNode selection = linker.getSelectionContext().getSingleSelection();
        if (selection != null) {
            download(linker, selection, selection.getUrl());
        }
    }

    /**
     * Display download link
     *
     * @param linker
     * @param selection
     * @param url
     */
    public static void download(Linker linker, GWTJahiaNode selection, String url) {
        if (selection != null && selection.isFile()) {
            linker.loading(Messages.get("statusbar.downloading.label"));
            if (url != null) {
                HTML link = new HTML(Messages.get("downloadMessage.label") + "<br /><br /><a href=\"" + url + "\" target=\"_new\">" + SafeHtmlUtils.htmlEscape(selection.getName()) + "</a>");
                final com.extjs.gxt.ui.client.widget.Window dl = new com.extjs.gxt.ui.client.widget.Window();
                dl.addStyleName("download-file");
                dl.setModal(true);
                dl.setHeadingHtml(Messages.get("label.download"));
                dl.setLayout(new FlowLayout());
                dl.setScrollMode(Style.Scroll.AUTO);
                dl.add(link);
                dl.setHeight(120);
                dl.show();
            } else {
                Window.alert(Messages.get("failure.download.label"));
            }
            linker.loaded();
        }
    }

    /**
     * Create a folder
     *
     * @param linker
     */
    public static void createFolder(final Linker linker) {
        GWTJahiaNode parent = linker.getSelectionContext().getSingleSelection();
        if (parent != null) {
            String newFolder = Window.prompt(Messages.get("newDirName.label"), "untitled");
            if (newFolder != null && FileUploader.filenameHasInvalidCharacters(newFolder)) {
                MessageBox.alert(Messages.get("label.error"), Messages.getWithArgs("failure.upload.invalid.filename", "", new String[]{newFolder}), null);
            } else if (newFolder != null && newFolder.length() > 0) {
                linker.loading(Messages.get("statusbar.newfoldering.label"));
                JahiaContentManagementService.App.getInstance().createFolder(parent.getPath(), newFolder, new BaseAsyncCallback<GWTJahiaNode>() {

                    @Override
                    public void onApplicationFailure(Throwable throwable) {
                        Window.alert(Messages.get("failure.newDir.label") + "\n" + throwable.getLocalizedMessage());
                        linker.loaded();
                    }

                    @Override
                    public void onSuccess(GWTJahiaNode node) {
                        linker.setSelectPathAfterDataUpdate(Arrays.asList(node.getPath()));
                        linker.loaded();
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put("node", node);
                        if (linker instanceof ManagerLinker) {
                            data.put(Linker.REFRESH_MAIN, "true");
                        }
                        linker.refresh(data);
                    }
                });
            }
        }
    }

    /**
     * Create a node.
     *
     * @param nodeName the name of the node to be created
     * @param linker
     * @param windowHeader
     * @param nodeType
     * @param mixins
     * @param nodeProperties
     * @param useMainNode
     */
    public static void createNode(final String nodeName, final Linker linker, final String windowHeader, final String nodeType, List<String> mixins, List<GWTJahiaNodeProperty> nodeProperties, boolean useMainNode) {
        GWTJahiaNode parent;
        if (useMainNode) {
            parent = linker.getSelectionContext().getMainNode();
        } else {
            parent = linker.getSelectionContext().getSingleSelection();
        }
        if (parent != null) {
            // if node name is provided, use it; otherwise prompt for it
            boolean isNodeNameProvided = nodeName != null && nodeName.length() > 0;
            String name = isNodeNameProvided ? nodeName : Window.prompt(windowHeader, "untitled");
            if (name != null && name.length() > 0) {
                linker.loading(Messages.get("statusbar.newfoldering.label"));
                JahiaContentManagementService.App.getInstance().createNode(parent.getPath(), name, nodeType, mixins, null, nodeProperties, null, null, null, !isNodeNameProvided, new BaseAsyncCallback<GWTJahiaNode>() {

                    @Override
                    public void onSuccess(GWTJahiaNode node) {
                        linker.setSelectPathAfterDataUpdate(Arrays.asList(node.getPath()));
                        linker.loaded();
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put("node", node);
                        if (linker instanceof ManagerLinker) {
                            data.put(Linker.REFRESH_MAIN, "true");
                        }
                        linker.refresh(data);
                    }

                    @Override
                    public void onApplicationFailure(Throwable throwable) {
                        Log.error("Unable to create [" + nodeType + "]", throwable);
                        linker.loaded();
                    }
                });
            }
        }
    }

    /**
     * Show portlet wizard form
     *
     * @param linker
     */
    public static void showPortletWizard(final Linker linker) {
        GWTJahiaNode parent = linker.getSelectionContext().getSingleSelection();
        if (parent != null) {
            PortletWizardWindow window = new PortletWizardWindow(linker, parent);
            window.show();
        }
    }

    /**
     * Show deploy portlet form
     * @param linker
     */

    /**
     * Show content wizard with a selected node type
     *
     * @param linker
     * @param nodeTypes
     * @param includeSubTypes
     */
    public static void showContentWizard(final Linker linker, final String nodeTypes, boolean includeSubTypes) {
        showContentWizard(linker, nodeTypes, linker.getSelectionContext().getSingleSelection(), includeSubTypes);
    }

    public static void showContentWizard(final Linker linker, final String nodeTypes, final GWTJahiaNode parent, boolean includeSubTypes) {
        showContentWizard(linker, nodeTypes, parent, null, includeSubTypes);
    }

    public static void showContentWizard(final Linker linker, final String nodeTypes, final GWTJahiaNode parent, String name, boolean includeSubTypes) {
        showContentWizard(linker, nodeTypes, parent, name, includeSubTypes, null, false);
    }

    public static void showContentWizard(final Linker linker, final String nodeTypes, final GWTJahiaNode parent, boolean includeSubTypes, boolean skipRefreshOnSave) {
        showContentWizard(linker, nodeTypes, parent, null, includeSubTypes, null, skipRefreshOnSave);
    }

    public static void showContentWizard(final Linker linker, final String nodeTypes, final GWTJahiaNode parent, String name, boolean includeSubTypes, Set<String> displayedNodeTypes, boolean skipRefreshOnSave) {
        if (parent != null) {
            ContentTypeWindow.createContent(linker, name, nodeTypes != null ? Arrays.asList(nodeTypes.split(" ")) : null, new HashMap<String, GWTJahiaNodeProperty>(), parent, includeSubTypes, false, displayedNodeTypes, skipRefreshOnSave);
        }
    }

    public static void lock(boolean lock, final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectionContext().getMultipleSelection();
        if (selectedItems != null && selectedItems.size() > 0) {
            List<String> selectedPaths = new ArrayList<String>(selectedItems.size());
            for (GWTJahiaNode node : selectedItems) {
                if ((lock && node.canLock()) || (!lock && node.canUnlock())) {
                    selectedPaths.add(node.getPath());
                }
            }
            boolean continueOperation = true;
            if (continueOperation && !selectedPaths.isEmpty()) {
                linker.loading(lock ? Messages.get("statusbar.locking.label") : Messages.get("statusbar.unlocking.label"));
                JahiaContentManagementService.App.getInstance().setLock(selectedPaths, lock, new BaseAsyncCallback<Object>() {

                    @Override
                    public void onApplicationFailure(Throwable throwable) {
                        MessageBox.alert(Messages.get("label.error", "Error"), throwable.getLocalizedMessage(), null);
                        linker.loaded();
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put(Linker.REFRESH_MAIN, true);
                        linker.refresh(data);
                    }

                    @Override
                    public void onSuccess(Object o) {
                        linker.loaded();
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put(Linker.REFRESH_MAIN, true);
                        linker.refresh(data);
                    }
                });
            }
        }
    }

    public static Set<String> fillContentChanges (final AbstractContentEngine engine, boolean saveChanges) {
        // node
        final Set<String> addedTypes = new HashSet<String>();
        final Set<String> removedTypes = new HashSet<String>();

        for (TabItem tab : engine.getTabs().getItems()) {
            EditEngineTabItem item = tab.getData("item");
            // case of contentTabItem
            if (item instanceof ContentTabItem) {
                final List<CheckBox> validLanguagesChecked = ((ContentTabItem) item).getCheckedLanguagesCheckBox();
                if (validLanguagesChecked != null) {
                    // Checkboxes are not null so they are displayed, if list is empty this means that this
                    // content is not visible in any language
                    final List<GWTJahiaLanguage> siteLanguages = JahiaGWTParameters.getSiteLanguages();
                    List<String> invalidLanguages = engine.getNode().getInvalidLanguages();
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
                        if (strings.size() > 0) {
                            gwtJahiaNodeProperty.setValues(new ArrayList<GWTJahiaNodePropertyValue>());
                            for (String string : strings) {
                                gwtJahiaNodeProperty.getValues().add(new GWTJahiaNodePropertyValue(string));
                            }
                        }
                        final List<GWTJahiaNodePropertyValue> gwtJahiaNodePropertyValues = gwtJahiaNodeProperty.getValues();
                        if (gwtJahiaNodePropertyValues != null && gwtJahiaNodePropertyValues.size() > 0) {
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
            item.doSave(engine.getNode(), engine.getChangedProperties(), engine.getChangedI18NProperties(), addedTypes, removedTypes, null,
                    engine.getAcl());

            // distinct modifications save from a simple check
            if (saveChanges) {
                engine.getNode().getNodeTypes().removeAll(removedTypes);
                engine.getNode().getNodeTypes().addAll(addedTypes);
                return removedTypes;
            }
        }
        return null;
    }

}
