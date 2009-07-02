/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.service.content.JahiaNodeService;
import org.jahia.ajax.gwt.client.service.content.JahiaNodeServiceAsync;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.widget.form.FormQuickGoogleGadget;
import org.jahia.ajax.gwt.client.widget.form.FormQuickRSS;
import org.jahia.ajax.gwt.client.widget.content.*;
import org.jahia.ajax.gwt.client.widget.content.portlet.PortletWizardWindow;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 7 juil. 2008 - 14:45:03
 */
public class FileActions {

    final static JahiaNodeServiceAsync service = JahiaNodeService.App.getInstance();

    public static void copy(final BrowserLinker linker) {
        final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
        if (selectedItems != null && selectedItems.size() > 0) {
            linker.loading(Messages.getResource("fm_copying"));
            service.copy(selectedItems, new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    Window.alert(Messages.getResource("fm_failCopy") + "\n" + throwable.getLocalizedMessage());
                    linker.loaded();
                }

                public void onSuccess(Object o) {
                    CopyPasteEngine.getInstance().setCopiedPaths(selectedItems);
                    linker.loaded();
                    linker.handleNewSelection();
                }
            });
        }
    }

    public static void cut(final BrowserLinker linker) {
        final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
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
                StringBuilder s = new StringBuilder(Messages.getResource("fm_warningLock"));
                for (GWTJahiaNode node : lockedFiles) {
                    s.append("\n").append(node.getName());
                }
                Window.alert(s.toString());
            }
            if (!actualSelection.isEmpty()) {
                linker.loading(Messages.getResource("fm_cutting"));
                service.cut(actualSelection, new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        Window.alert(Messages.getResource("fm_failCut") + "\n" + throwable.getLocalizedMessage());
                        linker.loaded();
                    }

                    public void onSuccess(Object o) {
                        CopyPasteEngine.getInstance().setCutPaths(actualSelection);
                        linker.loaded();
                        linker.handleNewSelection();

                    }
                });
            }
        }
    }

    public static void paste(final BrowserLinker linker) {
        GWTJahiaNode m = (GWTJahiaNode) linker.getTreeSelection();
        if (m == null) {
            final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
            if (selectedItems != null && selectedItems.size() == 1) {
                m = selectedItems.get(0);
            }
        }
        if (m != null && !m.isFile()) {
            linker.loading(Messages.getResource("fm_pasting"));
            final CopyPasteEngine copyPasteEngine = CopyPasteEngine.getInstance();
            service.paste(copyPasteEngine.getCopiedPaths(), m.getPath(), copyPasteEngine.isCut(), new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    Window.alert(Messages.getResource("fm_failPaste") + "\n" + throwable.getLocalizedMessage());
                    linker.loaded();
                }

                public void onSuccess(Object o) {
                    boolean refreshAll = false;
                    for (GWTJahiaNode n : copyPasteEngine.getCopiedPaths()) {
                        if (!n.isFile()) {
                            refreshAll = true;
                            break;
                        }
                    }
                    copyPasteEngine.onPastedPath();
                    linker.loaded();
                    if (refreshAll) {
                        linker.refreshAll();
                    } else {
                        linker.refreshTable();
                    }
                }
            });
        }
    }


    public static void move(final BrowserLinker linker, final List<GWTJahiaNode> sources, GWTJahiaNode target) {
        service.paste(sources, target.getPath(), true, new AsyncCallback() {
            public void onFailure(Throwable throwable) {
                Window.alert("Paste failed :\n" + throwable.getLocalizedMessage());
                linker.loaded();
            }

            public void onSuccess(Object o) {
                boolean refreshAll = false;
                for (GWTJahiaNode n : sources) {
                    if (!n.isFile()) {
                        refreshAll = true;
                        break;
                    }
                }

                linker.loaded();
                if (refreshAll) {
                    linker.refreshAll();
                } else {
                    linker.refreshTable();
                }
            }
        });
    }

    public static void upload(final BrowserLinker linker) {
        GWTJahiaNode m = (GWTJahiaNode) linker.getTreeSelection();
        if (m == null) {
            final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
            if (selectedItems != null && selectedItems.size() == 1) {
                m = selectedItems.get(0);
            }
        }
        if (m != null && !m.isFile()) {
            new FileUploader(linker, m);
        }
    }

    public static void download(final BrowserLinker linker) {
        final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
        if (selectedItems != null && selectedItems.size() == 1) {
            final GWTJahiaNode selection = selectedItems.get(0);
            download(linker, selection, selection.getUrl());
        }
    }

    public static void download(BrowserLinker linker, GWTJahiaNode selection, String url) {
        if (selection != null && selection.isFile().booleanValue()) {
            linker.loading(Messages.getResource("fm_downloading"));
            if (url != null) {
                HTML link = new HTML(Messages.getResource("fm_downloadMessage") + "<br /><br /><a href=\"" + url + "\" target=\"_new\">" + selection.getName() + "</a>");
                final com.extjs.gxt.ui.client.widget.Window dl = new com.extjs.gxt.ui.client.widget.Window();
                dl.setModal(true);
                dl.setHeading(Messages.getResource("fm_download"));
                dl.setLayout(new FlowLayout());
                dl.setScrollMode(Style.Scroll.AUTO);
                dl.add(link);
                dl.setHeight(120);
                dl.show();
            } else {
                Window.alert(Messages.getResource("fm_failDownload"));
            }
            linker.loaded();
        }
    }

    public static void preview(final BrowserLinker linker) {
        final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
        if (selectedItems != null && selectedItems.size() == 1) {
            final GWTJahiaNode selection = selectedItems.get(0);
            if (selection != null && selection.isFile().booleanValue()) {
                ImagePopup.popImage(selection);
            }
        }
    }

    public static void openWebFolder(final BrowserLinker linker) {
        List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
        final GWTJahiaNode selection;
        if (selectedItems == null || selectedItems.size() > 1 || (selectedItems.size() == 1 && selectedItems.get(0).isFile())) {
            selection = (GWTJahiaNode) linker.getTreeSelection();
        } else {
            selection = selectedItems.get(0);
        }
        if (selection != null && !selection.isFile().booleanValue()) {
            linker.loading(Messages.getResource("fm_webfoldering"));
            service.getAbsolutePath(selection.getPath(), new AsyncCallback<String>() {
                public void onFailure(Throwable t) {
                    Window.alert(Messages.getResource("fm_failWebfolder") + "\n" + t.getLocalizedMessage());
                    linker.loaded();
                }

                public void onSuccess(String url) {
                    if (url != null) {
                        HTML link = new HTML(Messages.getResource("fm_webfolderMessage") + "<br /><br /><a target=\"_new\" folder=\"" + url + "\" style=\"behavior:url(#default#AnchorClick)\">" + selection.getName() + "</a>");
                        final Dialog dl = new Dialog();
                        dl.setModal(true);
                        dl.setHeading(Messages.getResource("fm_webfolder"));
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

    public static void createFolder(final BrowserLinker linker) {
        GWTJahiaNode parent = (GWTJahiaNode) linker.getTreeSelection();
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
            if (selectedItems != null && selectedItems.size() == 1) {
                parent = selectedItems.get(0);
            }
        }
        if (parent != null && !parent.isFile()) {
            String newFolder = Window.prompt(Messages.getResource("fm_newdirname"), "untitled");
            if (newFolder != null && newFolder.length() > 0) {
                linker.loading(Messages.getResource("fm_newfoldering"));
                service.createFolder(parent.getPath(), newFolder, new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        Window.alert(Messages.getResource("fm_failNewdir") + "\n" + throwable.getLocalizedMessage());
                        linker.loaded();
                    }

                    public void onSuccess(Object o) {
                        linker.loaded();
                        linker.refreshAll();
                    }
                });
            }
        }
    }

    public static void showMashupWizard(final BrowserLinker linker) {
        GWTJahiaNode parent = (GWTJahiaNode) linker.getTreeSelection();
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
            if (selectedItems != null && selectedItems.size() == 1) {
                parent = selectedItems.get(0);
            }
        }
        if (parent != null && !parent.isFile()) {
            PortletWizardWindow window = new PortletWizardWindow(linker, parent);
            window.show();
        }
    }

    public static void showRSSForm(final BrowserLinker linker) {
        GWTJahiaNode parent = (GWTJahiaNode) linker.getTreeSelection();
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
            if (selectedItems != null && selectedItems.size() == 1) {
                parent = selectedItems.get(0);
            }
        }
        if (parent != null && !parent.isFile()) {
            com.extjs.gxt.ui.client.widget.Window w = new com.extjs.gxt.ui.client.widget.Window();
            w.setHeading(Messages.getNotEmptyResource("rss_new", "New RSS"));
            w.setModal(true);
            w.setResizable(false);
            w.setBodyBorder(false);
            w.setLayout(new FillLayout());
            w.setWidth(350);
            w.add(new FormQuickRSS(parent.getPath()){
                public void onMashupCreated() {
                    linker.refreshTable();
                }
            });
            w.setScrollMode(Style.Scroll.AUTO);
            w.layout();
            w.show();
        }
    }

    public static void showGoogleGadgetForm(final BrowserLinker linker) {
        GWTJahiaNode parent = (GWTJahiaNode) linker.getTreeSelection();
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
            if (selectedItems != null && selectedItems.size() == 1) {
                parent = selectedItems.get(0);
            }
        }
        if (parent != null && !parent.isFile()) {
            com.extjs.gxt.ui.client.widget.Window w = new com.extjs.gxt.ui.client.widget.Window();
            w.setHeading(Messages.getNotEmptyResource("googlegadget_new", "New Google Gadget"));
            w.setModal(true);
            w.setResizable(false);
            w.setBodyBorder(false);
            w.setLayout(new FillLayout());
            w.setWidth(350);
            w.add(new FormQuickGoogleGadget(parent.getPath()) {
                @Override
                public void onMashupCreated() {
                    linker.refreshTable();
                }
            });
            w.setScrollMode(Style.Scroll.AUTO);
            w.layout();
            w.show();
        }
    }

    public static void mountFolder(final BrowserLinker linker) {
//        GWTJahiaNode parent = (GWTJahiaNode) linker.getTreeSelection() ;
//        if (parent == null) {
//            final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection() ;
//            if (selectedItems != null && selectedItems.size() == 1) {
//                parent = selectedItems.get(0) ;
//            }
//        }
//        if (parent != null && !parent.isFile()) {
        new Mounter(linker);
//        }
    }

    public static void unmountFolder(final BrowserLinker linker) {
        final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
        if (selectedItems != null && selectedItems.size() == 1) {
            GWTJahiaNode selection = selectedItems.get(0);
            if (selection.isLocked()) {
                Window.alert(Messages.getResource("fm_failUnmountLock1") + " " + selection.getName() + Messages.getResource("fm_failUnmountLock2") + " " + selection.getLockOwner());
            } else if (Window.confirm(Messages.getResource("fm_confUnmount") + " " + selection.getName() + " ?")) {
                linker.loading(Messages.getResource("fm_unmounting"));
                List<String> selectedPaths = new ArrayList<String>(1);
                selectedPaths.add(selection.getPath());
                service.deletePaths(selectedPaths, new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        Window.alert(Messages.getResource("fm_failUnmount") + "\n" + throwable.getLocalizedMessage());
                        linker.loaded();
                    }

                    public void onSuccess(Object o) {
                        linker.loaded();
                        linker.refreshAll();
                    }
                });
            }
        }
    }

    public static void createItem(final BrowserLinker linker, List<String> types) {
        GWTJahiaNode parent = (GWTJahiaNode) linker.getTreeSelection();
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
            if (selectedItems != null && selectedItems.size() == 1) {
                parent = selectedItems.get(0);
            }
        }
        if (parent != null && !parent.isFile()) {
            new NodeCreatorWindow(linker, parent, types);
        }
    }

    public static void remove(final BrowserLinker linker) {
        final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
        if (selectedItems != null && selectedItems.size() > 0) {
            boolean rem;
            boolean containFolder = false;
            if (selectedItems.size() == 1) {
                GWTJahiaNode selection = selectedItems.get(0);
                containFolder = !selection.isFile();
                rem = Window.confirm(Messages.getResource("fm_confRemove") + " " + selection.getName() + " ?");
            } else {
                rem = Window.confirm(Messages.getResource("fm_confMultiRemove"));
            }
            final boolean refreshTree = containFolder;
            if (rem) {
                List<GWTJahiaNode> actualSelection = new ArrayList<GWTJahiaNode>();
                final List<GWTJahiaNode> lockedFiles = new ArrayList<GWTJahiaNode>();
                for (GWTJahiaNode node : selectedItems) {
                    if (node.isLocked()) {
                        lockedFiles.add(node);
                    } else {
                        actualSelection.add(node);
                    }
                }
                if (!lockedFiles.isEmpty()) {
                    StringBuilder s = new StringBuilder(Messages.getResource("fm_warningLock"));
                    for (GWTJahiaNode node : lockedFiles) {
                        s.append("\n").append(node.getName());
                    }
                    Window.alert(s.toString());
                }
                if (!actualSelection.isEmpty()) {
                    linker.loading(Messages.getResource("fm_removing"));
                    List<String> selectedPaths = new ArrayList<String>(actualSelection.size());
                    for (GWTJahiaNode node : actualSelection) {
                        selectedPaths.add(node.getPath());
                    }
                    service.deletePaths(selectedPaths, new AsyncCallback() {
                        public void onFailure(Throwable throwable) {
                            Window.alert(Messages.getResource("fm_failDelete") + "\n" + throwable.getLocalizedMessage());
                            linker.loaded();
                        }

                        public void onSuccess(Object o) {
                            linker.loaded();
                            if (refreshTree) {
                                linker.refreshAll();
                            } else {
                                linker.refreshTable();
                            }
                        }
                    });
                }
            }
        }
    }

    public static void rename(final BrowserLinker linker) {
        final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
        if (selectedItems != null && selectedItems.size() == 1) {
            final GWTJahiaNode selection = selectedItems.get(0);
            if (selection != null) {
                if (selection.isLocked()) {
                    Window.alert(selection.getName() + " is locked");
                    return;
                }
                linker.loading(Messages.getResource("fm_renaming"));
                String newName = Window.prompt(Messages.getResource("fm_confNewName") + " " + selection.getName(), selection.getName());
                if (newName != null && newName.length() > 0 && !newName.equals(selection.getName())) {
                    final boolean folder = !selection.isFile();
                    service.rename(selection.getPath(), newName, new AsyncCallback() {
                        public void onFailure(Throwable throwable) {
                            Window.alert(Messages.getResource("fm_failRename") + "\n" + throwable.getLocalizedMessage());
                            linker.loaded();
                        }

                        public void onSuccess(Object o) {
                            linker.loaded();
                            if (folder) {
                                linker.refreshAll();
                            } else {
                                linker.refreshTable();
                            }
                        }
                    });
                } else {
                    linker.loaded();
                }
            }
        }
    }

    public static void zip(final BrowserLinker linker) {
        final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
        final GWTJahiaNode parentItem = (GWTJahiaNode) linker.getTreeSelection();
        if (parentItem != null && selectedItems != null && selectedItems.size() > 0) {
            final GWTJahiaNode selection = selectedItems.get(0);
            if (selection != null) {
                linker.loading(Messages.getResource("fm_zipping"));
                String defaultArchName;
                if (selectedItems.size() == 1) {
                    defaultArchName = selection.getName() + ".zip";
                } else {
                    defaultArchName = "archive.zip";
                }
                final String archName = Window.prompt(Messages.getResource("fm_confArchiveName"), defaultArchName);
                if (archName != null && archName.length() > 0) {
                    service.checkExistence(parentItem.getPath() + "/" + archName, new AsyncCallback<Boolean>() {
                        public void onFailure(Throwable throwable) {
                            if (throwable instanceof ExistingFileException) {
                                if (com.google.gwt.user.client.Window.confirm(Messages.getResource("fm_alreadyExists") + "\n" + Messages.getResource("fm_confOverwrite"))) {
                                     forceZip(selectedItems, archName, linker);
                                 }
                            } else {
                                Window.alert(Messages.getResource("fm_failZip") + "\n" + throwable.getLocalizedMessage());
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

    private static void forceZip(final List<GWTJahiaNode> selectedItems, final String archName, final BrowserLinker linker) {
        List<String> selectedPaths = new ArrayList<String>(selectedItems.size());
        for (GWTJahiaNode node : selectedItems) {
            selectedPaths.add(node.getPath());
        }
        service.zip(selectedPaths, archName, new AsyncCallback() {
            public void onFailure(Throwable throwable) {
                Window.alert(Messages.getResource("fm_failZip") + "\n" + throwable.getLocalizedMessage());
                linker.loaded();
            }

            public void onSuccess(Object o) {
                linker.loaded();
                linker.refreshTable();
            }
        });
    }

    public static void unzip(final BrowserLinker linker) {
        final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
        if (selectedItems != null && selectedItems.size() > 0) {
            linker.loading(Messages.getResource("fm_unzipping"));
            List<String> selectedPaths = new ArrayList<String>(selectedItems.size());
            for (GWTJahiaNode node : selectedItems) {
                if (node.getName().endsWith(".zip") || node.getName().endsWith(".ZIP")) {
                    selectedPaths.add(node.getPath());
                }
            }
            service.unzip(selectedPaths, new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    Window.alert(Messages.getResource("fm_failUnzip") + "\n" + throwable.getLocalizedMessage());
                    linker.loaded();
                }

                public void onSuccess(Object o) {
                    linker.loaded();
                    linker.refreshAll();
                }
            });
        }
    }

    public static void lock(boolean lock, final BrowserLinker linker) {
        final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
        if (selectedItems != null && selectedItems.size() > 0) {
            List<String> selectedPaths = new ArrayList<String>(selectedItems.size());
            List<String> lockedBySystem = new LinkedList<String>();
            boolean userAllowedToUnlockFiles = JahiaGWTParameters.isUserAllowedToUnlockFiles();
            for (GWTJahiaNode node : selectedItems) {
                if (lock && !node.isLocked() || !lock && node.isLocked()) {
                    if (!lock
                            && node.getLockOwner() != null
                            && node.getLockOwner().equals(
                            JahiaGWTParameters.SYSTEM_USER)) {
                        lockedBySystem.add(node.getPath());
                        if (!userAllowedToUnlockFiles) {
                            continue;
                        }
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
                if (userAllowedToUnlockFiles) {
                    continueOperation = Window
                            .confirm(Messages.getResource("fm_warningSystemLock") + "\n"
                                    + lockedFiles.toString()
                                    + "\n\n" + Messages.getResource("fm_confUnlock"));
                } else {
                    MessageBox.alert("Warning", Messages.getResource("fm_failLock") + "\n" + lockedFiles.toString(), null);
                }
            }
            if (continueOperation && !selectedPaths.isEmpty()) {
                linker.loading(lock ? Messages.getResource("fm_locking") : Messages.getResource("fm_unlocking"));
                service.setLock(selectedPaths, lock, new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        MessageBox.alert("Error", throwable.getLocalizedMessage(), null);
                        linker.loaded();
                        linker.refreshTable();
                    }

                    public void onSuccess(Object o) {
                        linker.loaded();
                        linker.refreshTable();
                    }
                });
            }
        }
    }

    public static void cropImage(final BrowserLinker linker) {
        final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
        if (selectedItems != null && selectedItems.size() == 1) {
            final GWTJahiaNode selectedNode = selectedItems.get(0);
            if (selectedNode != null) {
                new ImageCrop(linker, selectedNode).show();
            }
        }
    }

    public static void resizeImage(final BrowserLinker linker) {
        final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
        if (selectedItems != null && selectedItems.size() == 1) {
            final GWTJahiaNode selectedNode = selectedItems.get(0);
            if (selectedNode != null) {
                new ImageResize(linker, selectedNode).show();
            }
        }
    }

    public static void rotateImage(final BrowserLinker linker) {
        final List<GWTJahiaNode> selectedItems = (List<GWTJahiaNode>) linker.getTableSelection();
        if (selectedItems != null && selectedItems.size() == 1) {
            final GWTJahiaNode selectedNode = selectedItems.get(0);
            if (selectedNode != null) {
                new ImageRotate(linker, selectedNode).show();
            }
        }
    }

}
