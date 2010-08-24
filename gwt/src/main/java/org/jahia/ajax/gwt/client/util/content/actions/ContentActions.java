/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.util.content.actions;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.*;
import org.jahia.ajax.gwt.client.widget.content.portlet.PortletWizardWindow;
import org.jahia.ajax.gwt.client.widget.edit.ContentTypeWindow;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.EngineLoader;
import org.jahia.ajax.gwt.client.widget.form.FormDeployPortletDefinition;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 7 juil. 2008 - 14:45:03
 */
public class ContentActions {

    /**
     * Copy
     *
     * @param linker
     */
    public static void copy(final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() > 0) {
            CopyPasteEngine.getInstance().setCopiedPaths(selectedItems);
            linker.loaded();
            linker.select(null);
        }
    }

    /**
     * Cut
     *
     * @param linker
     */
    public static void cut(final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() > 0) {
            final List<GWTJahiaNode> actualSelection = new ArrayList<GWTJahiaNode>();
            final List<GWTJahiaNode> lockedFiles = new ArrayList<GWTJahiaNode>();
            for (GWTJahiaNode node : selectedItems) {
                if (node.isLocked()) {
                    lockedFiles.add(node);
                } else {
                    actualSelection.add(node);
                }
            }
            if (!lockedFiles.isEmpty()) {
                StringBuilder s = new StringBuilder(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.warning.lock.label"));
                for (GWTJahiaNode node : lockedFiles) {
                    s.append("\n").append(node.getName());
                }
                Window.alert(s.toString());
            }
            if (!actualSelection.isEmpty()) {
                linker.loading(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.cutting.label"));
                JahiaContentManagementService.App.getInstance().checkWriteable(JCRClientUtils.getPathesList(actualSelection), new BaseAsyncCallback() {
                    public void onApplicationFailure(Throwable throwable) {
                        Window.alert(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.failure.cut.label") + "\n" + throwable.getLocalizedMessage());
                        linker.loaded();
                    }

                    public void onSuccess(Object o) {
                        CopyPasteEngine.getInstance().setCutPaths(actualSelection);
                        linker.loaded();
                        linker.select(null);

                    }
                });
            }
        }
    }

    /**
     * Paste
     *
     * @param linker
     */
    public static void paste(final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        GWTJahiaNode m = null;
        if (selectedItems != null && selectedItems.size() == 1) {
            m = selectedItems.get(0);
        }
        if (m == null) {
            m = linker.getMainNode();
        }
        if (m != null && !m.isFile()) {
            linker.loading(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.pasting.label"));
            final CopyPasteEngine copyPasteEngine = CopyPasteEngine.getInstance();
            JahiaContentManagementService.App.getInstance().paste(JCRClientUtils.getPathesList(copyPasteEngine.getCopiedPaths()), m.getPath(), null, copyPasteEngine.isCut(), new BaseAsyncCallback() {
                public void onApplicationFailure(Throwable throwable) {
                    Window.alert(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.failure.paste.label") + "\n" + throwable.getLocalizedMessage());
                    linker.loaded();
                }

                public void onSuccess(Object o) {
                    boolean refresh = false;
                    for (GWTJahiaNode n : copyPasteEngine.getCopiedPaths()) {
                        if (!n.isFile()) {
                            refresh = true;
                            break;
                        }
                    }
                    copyPasteEngine.onPastedPath();
                    linker.loaded();
                    if (refresh) {
                        linker.refresh(EditLinker.REFRESH_ALL);
                    } else {
                        linker.refresh(Linker.REFRESH_MAIN);
                    }
                }
            });
        }
    }

    /**
     * Pste as reference
     *
     * @param linker
     */
    public static void pasteReference(final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        GWTJahiaNode m = null;
        if (selectedItems != null && selectedItems.size() == 1) {
            m = selectedItems.get(0);
        }
        if (m == null) {
            m = linker.getMainNode();
        }
        if (m != null && !m.isFile()) {
            linker.loading(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.pastingref.label"));
            final CopyPasteEngine copyPasteEngine = CopyPasteEngine.getInstance();
            JahiaContentManagementService.App.getInstance().pasteReferences(JCRClientUtils.getPathesList(copyPasteEngine.getCopiedPaths()), m.getPath(), null, new BaseAsyncCallback() {
                public void onApplicationFailure(Throwable throwable) {
                    Window.alert(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.failure.pasteref.label") + "\n" + throwable.getLocalizedMessage());
                    linker.loaded();
                }

                public void onSuccess(Object o) {
                    boolean refresh = false;
                    for (GWTJahiaNode n : copyPasteEngine.getCopiedPaths()) {
                        if (!n.isFile()) {
                            refresh = true;
                            break;
                        }
                    }
                    copyPasteEngine.onPastedPath();
                    linker.loaded();
                    if (refresh) {
                        linker.refresh(EditLinker.REFRESH_ALL);
                    } else {
                        linker.refresh(Linker.REFRESH_MAIN);
                    }
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
        JahiaContentManagementService.App.getInstance().paste(JCRClientUtils.getPathesList(sources), target.getPath(), null, true, new BaseAsyncCallback() {
            public void onApplicationFailure(Throwable throwable) {
                Window.alert("Paste failed :\n" + throwable.getLocalizedMessage());
                linker.loaded();
            }

            public void onSuccess(Object o) {
                boolean refresh = false;
                for (GWTJahiaNode n : sources) {
                    if (!n.isFile()) {
                        refresh = true;
                        break;
                    }
                }

                linker.loaded();
                if (refresh) {
                    linker.refresh(EditLinker.REFRESH_ALL);
                } else {
                    linker.refresh(Linker.REFRESH_MAIN);
                }
            }
        });
    }

    /**
     * Upload a file
     *
     * @param linker
     */
    public static void upload(final Linker linker) {
        GWTJahiaNode m = (GWTJahiaNode) linker.getMainNode();
        if (m == null) {
            final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
            if (selectedItems != null && selectedItems.size() == 1) {
                m = selectedItems.get(0);
            }
        }
        if (m != null && !m.isFile()) {
            new FileUploader(linker, m);
        }
    }

    /**
     * Display download link
     *
     * @param linker
     */
    public static void download(final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() == 1) {
            final GWTJahiaNode selection = selectedItems.get(0);
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
        if (selection != null && selection.isFile().booleanValue()) {
            linker.loading(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.downloading.label"));
            if (url != null) {
                HTML link = new HTML(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.downloadMessage.label") + "<br /><br /><a href=\"" + url + "\" target=\"_new\">" + selection.getName() + "</a>");
                final com.extjs.gxt.ui.client.widget.Window dl = new com.extjs.gxt.ui.client.widget.Window();
                dl.setModal(true);
                dl.setHeading(Messages.get("label.download"));
                dl.setLayout(new FlowLayout());
                dl.setScrollMode(Style.Scroll.AUTO);
                dl.add(link);
                dl.setHeight(120);
                dl.show();
            } else {
                Window.alert(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.failure.download.label"));
            }
            linker.loaded();
        }
    }

    /**
     * Display node preview
     *
     * @param linker
     */
    public static void preview(final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() == 1) {
            final GWTJahiaNode selection = selectedItems.get(0);
            if (selection != null && selection.isFile().booleanValue()) {
                ImagePopup.popImage(selection);
            }
        }
    }

    /**
     * Open a webfolder
     *
     * @param linker
     */
    public static void openWebFolder(final Linker linker) {
        List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        final GWTJahiaNode selection;
        if (selectedItems == null || selectedItems.size() > 1 || (selectedItems.size() == 1 && selectedItems.get(0).isFile())) {
            selection = (GWTJahiaNode) linker.getMainNode();
        } else {
            selection = selectedItems.get(0);
        }
        if (selection != null && !selection.isFile().booleanValue()) {
            linker.loading(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.webfoldering.label"));
            JahiaContentManagementService.App.getInstance().getAbsolutePath(selection.getPath(), new BaseAsyncCallback<String>() {
                public void onApplicationFailure(Throwable t) {
                    Window.alert(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.failure.webfolder.label") + "\n" + t.getLocalizedMessage());
                    linker.loaded();
                }

                public void onSuccess(String url) {
                    if (url != null) {
                        HTML link = new HTML(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.webFolderMessage.label") + "<br /><br /><a target=\"_new\" folder=\"" + url + "\" style=\"behavior:url(#default#AnchorClick)\">" + selection.getName() + "</a>");
                        final Dialog dl = new Dialog();
                        dl.setModal(true);
                        dl.setHeading(Messages.get("label.openIEFolder"));
                        dl.setHideOnButtonClick(true);
                        dl.setLayout(new FlowLayout());
                        dl.setScrollMode(Style.Scroll.AUTO);
                        dl.add(link);
                        dl.setHeight(150);
                        linker.loaded();
                        dl.show();
                    }
                }
            });
        }
    }

    /**
     * Create a folder
     *
     * @param linker
     */
    public static void createFolder(final Linker linker) {
        GWTJahiaNode parent = linker.getMainNode();
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
            if (selectedItems != null && selectedItems.size() == 1) {
                parent = selectedItems.get(0);
            }
        }
        if (parent != null && !parent.isFile()) {
            String newFolder = Window.prompt(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.newDirName.label"), "untitled");
            if (newFolder != null && newFolder.length() > 0) {
                linker.loading(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.newfoldering.label"));
                JahiaContentManagementService.App.getInstance().createFolder(parent.getPath(), newFolder, new BaseAsyncCallback() {
                    public void onApplicationFailure(Throwable throwable) {
                        Window.alert(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.failure.newDir.label") + "\n" + throwable.getLocalizedMessage());
                        linker.loaded();
                    }

                    public void onSuccess(Object o) {
                        linker.loaded();
                        linker.refresh(Linker.REFRESH_FOLDERS);
                    }
                });
            }
        }
    }

    /**
     * Create a node
     *
     * @param linker
     * @param windowHeaer
     * @param nodeType
     */
    public static void createNode(final Linker linker, final String windowHeaer, final String nodeType) {
        GWTJahiaNode parent = linker.getMainNode();
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
            if (selectedItems != null && selectedItems.size() == 1) {
                parent = selectedItems.get(0);
            }
        }
        if (parent != null && !parent.isFile()) {
            String nodeName = Window.prompt(windowHeaer, "untitled");
            if (nodeName != null && nodeName.length() > 0) {
                linker.loading(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.newfoldering.label"));
                JahiaContentManagementService.App.getInstance().createNode(parent.getPath(), nodeName, nodeType, null, null, null, new BaseAsyncCallback<GWTJahiaNode>() {
                    public void onSuccess(GWTJahiaNode o) {
                        linker.loaded();
                        linker.refresh(EditLinker.REFRESH_ALL);
                    }

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
        GWTJahiaNode parent = (GWTJahiaNode) linker.getMainNode();
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
            if (selectedItems != null && selectedItems.size() == 1) {
                parent = selectedItems.get(0);
            }
        }
        if (parent != null && !parent.isFile()) {
            PortletWizardWindow window = new PortletWizardWindow(linker, parent);
            window.show();
        }
    }

    /**
     * Show deploy portlet form
     * @param linker
     */
    /**
     * Show deploy portlet form
     *
     * @param linker
     */
    public static void showDeployPortletForm(final Linker linker) {
        GWTJahiaNode parent = (GWTJahiaNode) linker.getMainNode();
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
            if (selectedItems != null && selectedItems.size() == 1) {
                parent = selectedItems.get(0);
            }
        }
        if (parent != null && !parent.isFile()) {
            final com.extjs.gxt.ui.client.widget.Window w = new com.extjs.gxt.ui.client.widget.Window();
            w.setHeading(Messages.get("label.deployNewPortlet", "New portlets"));
            w.setModal(true);
            w.setResizable(false);
            w.setBodyBorder(false);
            w.setLayout(new FillLayout());
            w.setWidth(600);
            w.add(new FormDeployPortletDefinition() {
                @Override
                public void closeParent() {
                    w.hide();
                }
                @Override
                public void refreshParent() {
                    linker.refresh(Linker.REFRESH_ALL);
                }
            });
            w.setScrollMode(Style.Scroll.AUTO);
            w.layout();
            w.show();
        }
    }

    /**
     * Show content wizard with a selected node type
     *
     * @param linker
     * @param nodeTypes
     */
    public static void showContentWizard(final Linker linker, final String nodeTypes) {
        GWTJahiaNode parent = linker.getSelectedNode();
        if (parent == null) {
            parent = linker.getMainNode();
        }
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
            if (selectedItems != null && selectedItems.size() == 1) {
                parent = selectedItems.get(0);
            }
        }
        showContentWizard(linker, nodeTypes, parent);
    }

    public static void showContentWizard(final Linker linker, final String nodeTypes, final GWTJahiaNode parent) {
        if (parent != null && !parent.isFile()) {
            JahiaContentDefinitionService.App.getInstance().getSubNodetypes(nodeTypes,
                    new BaseAsyncCallback<Map<GWTJahiaNodeType, List<GWTJahiaNodeType>>>() {
                        public void onApplicationFailure(Throwable caught) {
                            MessageBox.alert("Alert",
                                    "Unable to load content definitions for base type '" + nodeTypes + "'. Cause: " + caught.getLocalizedMessage(),
                                    null);
                        }

                        public void onSuccess(
                                Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> result) {
                            if (result.size() == 1 && result.values().iterator().next().size() == 1) {
                                EngineLoader.showCreateEngine(linker, parent,
                                        result.values().iterator().next().iterator().next(), new HashMap<String, GWTJahiaNodeProperty>(),
                                        null, false);
                                        ;
                            } else {
                                new ContentTypeWindow(linker, parent, result, false).show();
                            }
                        }
                    });
        }
    }

    /**
     * Mount a folder
     *
     * @param linker
     */
    public static void mountFolder(final Linker linker) {
        new Mounter(linker);
    }

    /**
     * Unmount a folder
     *
     * @param linker
     */
    public static void unmountFolder(final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() == 1) {
            GWTJahiaNode selection = selectedItems.get(0);
            if (selection.isLocked()) {
                Window.alert(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.failure.unmountLock1.label") + " " + selection.getName() + Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.failure.unmountLock2.label") + " " + selection.getLockOwner());
            } else if (Window.confirm(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.confirm.unmount.label") + " " + selection.getName() + " ?")) {
                linker.loading(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.unmounting.label"));
                List<String> selectedPaths = new ArrayList<String>(1);
                selectedPaths.add(selection.getPath());
                JahiaContentManagementService.App.getInstance().deletePaths(selectedPaths, new BaseAsyncCallback() {
                    public void onApplicationFailure(Throwable throwable) {
                        Window.alert(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.failure.unmount.label") + "\n" + throwable.getLocalizedMessage());
                        linker.loaded();
                    }

                    public void onSuccess(Object o) {
                        linker.loaded();
                        linker.refresh(Linker.REFRESH_FOLDERS);
                    }
                });
            }
        }
    }

    public static void rename(final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() == 1) {
            final GWTJahiaNode selection = selectedItems.get(0);
            if (selection != null) {
                if (selection.isLocked()) {
                    Window.alert(selection.getName() + " is locked");
                    return;
                }
                linker.loading(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.renaming.label"));
                String newName = Window.prompt(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.confirm.newName.label") + " " + selection.getName(), selection.getName());
                if (newName != null && newName.length() > 0 && !newName.equals(selection.getName())) {
                    final boolean folder = !selection.isFile();
                    JahiaContentManagementService.App.getInstance().rename(selection.getPath(), newName, new BaseAsyncCallback() {
                        public void onApplicationFailure(Throwable throwable) {
                            Window.alert(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.failure.rename.label") + "\n" + throwable.getLocalizedMessage());
                            linker.loaded();
                        }

                        public void onSuccess(Object o) {
                            linker.loaded();
                            if (folder) {
                                linker.refresh(EditLinker.REFRESH_ALL);
                            } else {
                                linker.refresh(Linker.REFRESH_MAIN);
                            }
                        }
                    });
                } else {
                    linker.loaded();
                }
            }
        }
    }

    public static void zip(final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        final GWTJahiaNode parentItem = (GWTJahiaNode) linker.getMainNode();
        if (parentItem != null && selectedItems != null && selectedItems.size() > 0) {
            final GWTJahiaNode selection = selectedItems.get(0);
            if (selection != null) {
                linker.loading(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.zipping.label"));
                String defaultArchName;
                if (selectedItems.size() == 1) {
                    defaultArchName = selection.getName() + ".zip";
                } else {
                    defaultArchName = "archive.zip";
                }
                final String archName = Window.prompt(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.confirm.archiveName.label"), defaultArchName);
                if (archName != null && archName.length() > 0) {
                    JahiaContentManagementService.App.getInstance().checkExistence(parentItem.getPath() + "/" + archName, new BaseAsyncCallback<Boolean>() {
                        public void onApplicationFailure(Throwable throwable) {
                            if (throwable instanceof ExistingFileException) {
                                if (com.google.gwt.user.client.Window.confirm(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.alreadyExists.label") + "\n" + Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.confirm.overwrite.label"))) {
                                    forceZip(selectedItems, archName, linker);
                                }
                            } else {
                                Window.alert(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.failure.zip.label") + "\n" + throwable.getLocalizedMessage());
                                linker.loaded();
                            }
                        }

                        public void onSuccess(Boolean aBoolean) {
                            forceZip(selectedItems, archName, linker);
                        }
                    });
                }
            }
        }
    }

    private static void forceZip(final List<GWTJahiaNode> selectedItems, final String archName, final Linker linker) {
        List<String> selectedPaths = new ArrayList<String>(selectedItems.size());
        for (GWTJahiaNode node : selectedItems) {
            selectedPaths.add(node.getPath());
        }
        JahiaContentManagementService.App.getInstance().zip(selectedPaths, archName, new BaseAsyncCallback() {
            public void onApplicationFailure(Throwable throwable) {
                Window.alert(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.failure.zip.label") + "\n" + throwable.getLocalizedMessage());
                linker.loaded();
            }

            public void onSuccess(Object o) {
                linker.loaded();
                linker.refresh(Linker.REFRESH_MAIN);
            }
        });
    }

    public static void unzip(final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() > 0) {
            linker.loading(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.unzipping.label"));
            List<String> selectedPaths = new ArrayList<String>(selectedItems.size());
            for (GWTJahiaNode node : selectedItems) {
                if (node.getName().endsWith(".zip") || node.getName().endsWith(".ZIP")) {
                    selectedPaths.add(node.getPath());
                }
            }
            JahiaContentManagementService.App.getInstance().unzip(selectedPaths, new BaseAsyncCallback() {
                public void onApplicationFailure(Throwable throwable) {
                    Window.alert(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.failure.unzip.label") + "\n" + throwable.getLocalizedMessage());
                    linker.loaded();
                }

                public void onSuccess(Object o) {
                    linker.loaded();
                    linker.refresh(EditLinker.REFRESH_ALL);
                }
            });
        }
    }

    public static void lock(boolean lock, final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() > 0) {
            List<String> selectedPaths = new ArrayList<String>(selectedItems.size());
            List<String> lockedBySystem = new LinkedList<String>();
            for (GWTJahiaNode node : selectedItems) {
                if (lock && !node.isLocked() || !lock && node.isLocked()) {
                    if (!lock
                            && node.getLockOwner() != null
                            && node.getLockOwner().equals(
                            JahiaGWTParameters.SYSTEM_USER)) {
                        lockedBySystem.add(node.getPath());
                    }
                    selectedPaths.add(node.getPath());
                }
            }
            boolean continueOperation = true;
            if (!lockedBySystem.isEmpty()) {
                StringBuffer lockedFiles = new StringBuffer();
                for (String file : lockedBySystem) {
                    lockedFiles.append("\n").append(
                            file.contains("/") ? file.substring(file
                                    .lastIndexOf('/') + 1) : file);
                }
                continueOperation = Window
                        .confirm(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.warning.systemLock.label") + "\n"
                                + lockedFiles.toString()
                                + "\n\n" + Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.confirm.unlock.label"));
            }
            if (continueOperation && !selectedPaths.isEmpty()) {
                linker.loading(lock ? Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.locking.label") : Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.unlocking.label"));
                JahiaContentManagementService.App.getInstance().setLock(selectedPaths, lock, new BaseAsyncCallback() {
                    public void onApplicationFailure(Throwable throwable) {
                        MessageBox.alert("Error", throwable.getLocalizedMessage(), null);
                        linker.loaded();
                        linker.refresh(Linker.REFRESH_MAIN);
                    }

                    public void onSuccess(Object o) {
                        linker.loaded();
                        linker.refresh(Linker.REFRESH_MAIN);
                    }
                });
            }
        }
    }

    public static void resizeImage(final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() == 1) {
            final GWTJahiaNode selectedNode = selectedItems.get(0);
            if (selectedNode != null) {
                new ImageResize(linker, selectedNode).show();
            }
        }
    }

    public static void rotateImage(final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() == 1) {
            final GWTJahiaNode selectedNode = selectedItems.get(0);
            if (selectedNode != null) {
                new ImageRotate(linker, selectedNode).show();
            }
        }
    }

    public static void exportContent(final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() == 1) {
            GWTJahiaNode selectedNode = selectedItems.get(0);
            if (selectedNode != null) {
                new ContentExport(linker, selectedNode).show();
            }
        } else {
            GWTJahiaNode selectedNode = linker.getMainNode();
            if (selectedNode != null) {
                new ContentExport(linker, selectedNode).show();
            }
        }
    }

    public static void importContent(final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() == 1) {
            GWTJahiaNode selectedNode = selectedItems.get(0);
            if (selectedNode != null) {
                new ContentImport(linker, selectedNode).show();
            }
        } else {
            GWTJahiaNode selectedNode = linker.getMainNode();
            if (selectedNode != null) {
                new ContentImport(linker, selectedNode).show();
            }
        }
    }

    public static void saveAsPortalComponent(final Linker linker) {
        final GWTJahiaNode target = linker.getSelectedNode();
        if (target != null) {
            JahiaContentManagementService.App.getInstance().pasteReferences(Arrays.asList(target.getPath()), "/shared/portalComponents", null, new BaseAsyncCallback() {
                public void onApplicationFailure(Throwable caught) {
                    Info.display("Portal Components", "Error while making your component available for users in their portal page.");
                }

                public void onSuccess(Object result) {
                    Info.display("Portal Components", "Your components is now available for users in their portal page.");
                }
            });
        }
    }

    public static void exportTemplateContent(Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();

        if (linker instanceof EditLinker) {
            new ContentExportTemplate(linker, ((EditLinker) linker).getSidePanel().getRootTemplate()).show();
        } else if (selectedItems != null && selectedItems.size() == 1) {
            GWTJahiaNode selectedNode = selectedItems.get(0);
            if (selectedNode != null) {
                new ContentExportTemplate(linker, selectedNode).show();
            }
        } else {
            GWTJahiaNode selectedNode = linker.getMainNode();
            if (selectedNode != null) {
                new ContentExportTemplate(linker, selectedNode).show();
            }
        }
    }
}
