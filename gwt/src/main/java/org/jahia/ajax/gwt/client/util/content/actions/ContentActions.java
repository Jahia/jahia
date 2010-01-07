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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
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
import org.jahia.ajax.gwt.client.widget.edit.contentengine.CreateContentEngine;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.CreateReusableContentEngine;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.EditContentEngine;
import org.jahia.ajax.gwt.client.widget.form.FormDeployPortletDefinition;
import org.jahia.ajax.gwt.client.widget.form.FormQuickGoogleGadget;
import org.jahia.ajax.gwt.client.widget.form.FormQuickRSS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
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
        final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
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
                JahiaContentManagementService.App.getInstance().checkWriteable(JCRClientUtils.getPathesList(actualSelection), new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        Window.alert(Messages.getResource("fm_failCut") + "\n" + throwable.getLocalizedMessage());
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
        final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
        GWTJahiaNode m = null;
        if (selectedItems != null && selectedItems.size() == 1) {
            m = selectedItems.get(0);
        }
        if (m == null) {
            m = linker.getMainNode();
        }
        if (m != null && !m.isFile()) {
            linker.loading(Messages.getResource("fm_pasting"));
            final CopyPasteEngine copyPasteEngine = CopyPasteEngine.getInstance();
            JahiaContentManagementService.App.getInstance().paste(JCRClientUtils.getPathesList(copyPasteEngine.getCopiedPaths()), m.getPath(), null, copyPasteEngine.isCut(), new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    Window.alert(Messages.getResource("fm_failPaste") + "\n" + throwable.getLocalizedMessage());
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
                        linker.refresh();
                    } else {
                        linker.refreshMainComponent();
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
        final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
        GWTJahiaNode m = null;
        if (selectedItems != null && selectedItems.size() == 1) {
            m = selectedItems.get(0);
        }
        if (m == null) {
            m = linker.getMainNode();
        }
        if (m != null && !m.isFile()) {
            linker.loading(Messages.getResource("fm_pastingref"));
            final CopyPasteEngine copyPasteEngine = CopyPasteEngine.getInstance();
            JahiaContentManagementService.App.getInstance().pasteReferences(JCRClientUtils.getPathesList(copyPasteEngine.getCopiedPaths()), m.getPath(), null, new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    Window.alert(Messages.getResource("fm_failPasteref") + "\n" + throwable.getLocalizedMessage());
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
                        linker.refresh();
                    } else {
                        linker.refreshMainComponent();
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
        JahiaContentManagementService.App.getInstance().paste(JCRClientUtils.getPathesList(sources), target.getPath(), null, true, new AsyncCallback() {
            public void onFailure(Throwable throwable) {
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
                    linker.refresh();
                } else {
                    linker.refreshMainComponent();
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
            final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
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
        final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
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

    /**
     * Display node preview
     *
     * @param linker
     */
    public static void preview(final Linker linker) {
        final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
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
        List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
        final GWTJahiaNode selection;
        if (selectedItems == null || selectedItems.size() > 1 || (selectedItems.size() == 1 && selectedItems.get(0).isFile())) {
            selection = (GWTJahiaNode) linker.getMainNode();
        } else {
            selection = selectedItems.get(0);
        }
        if (selection != null && !selection.isFile().booleanValue()) {
            linker.loading(Messages.getResource("fm_webfoldering"));
            JahiaContentManagementService.App.getInstance().getAbsolutePath(selection.getPath(), new AsyncCallback<String>() {
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

    /**
     * Create a folder
     *
     * @param linker
     */
    public static void createFolder(final Linker linker) {
        GWTJahiaNode parent = linker.getMainNode();
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
            if (selectedItems != null && selectedItems.size() == 1) {
                parent = selectedItems.get(0);
            }
        }
        if (parent != null && !parent.isFile()) {
            String newFolder = Window.prompt(Messages.getResource("fm_newdirname"), "untitled");
            if (newFolder != null && newFolder.length() > 0) {
                linker.loading(Messages.getResource("fm_newfoldering"));
                JahiaContentManagementService.App.getInstance().createFolder(parent.getPath(), newFolder, new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        Window.alert(Messages.getResource("fm_failNewdir") + "\n" + throwable.getLocalizedMessage());
                        linker.loaded();
                    }

                    public void onSuccess(Object o) {
                        linker.loaded();
                        linker.refresh();
                    }
                });
            }
        }
    }

    /**
     * Show mashup wizard form
     *
     * @param linker
     */
    public static void showMashupWizard(final Linker linker) {
        GWTJahiaNode parent = (GWTJahiaNode) linker.getMainNode();
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
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
     * Showw rss from
     *
     * @param linker
     */
    public static void showRSSForm(final Linker linker) {
        GWTJahiaNode parent = (GWTJahiaNode) linker.getMainNode();
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
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
            w.add(new FormQuickRSS(parent.getPath()) {
                public void onMashupCreated() {
                    linker.refreshMainComponent();
                }
            });
            w.setScrollMode(Style.Scroll.AUTO);
            w.layout();
            w.show();
        }
    }

    /**
     * Display google gadget form
     *
     * @param linker
     */
    public static void showGoogleGadgetForm(final Linker linker) {
        GWTJahiaNode parent = (GWTJahiaNode) linker.getMainNode();
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
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
                    linker.refreshMainComponent();
                }
            });
            w.setScrollMode(Style.Scroll.AUTO);
            w.layout();
            w.show();
        }
    }

    /**
     * Show deploy portlet form
     * @param linker
     */
     /**
     * Show deploy portlet form
     * @param linker
     */
    public static void showDeployPortletForm(final Linker linker) {
        GWTJahiaNode parent = (GWTJahiaNode) linker.getMainNode();
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
            if (selectedItems != null && selectedItems.size() == 1) {
                parent = selectedItems.get(0);
            }
        }
        if (parent != null && !parent.isFile()) {
            final com.extjs.gxt.ui.client.widget.Window w = new com.extjs.gxt.ui.client.widget.Window();
            w.setHeading(Messages.getNotEmptyResource("fm_portlet_deploy", "New portlets"));
            w.setModal(true);
            w.setResizable(false);
            w.setBodyBorder(false);
            w.setLayout(new FillLayout());
            w.setWidth(500);
            w.add(new FormDeployPortletDefinition(){
                public void closeParent() {
                    w.hide();
                }
            });
            w.setScrollMode(Style.Scroll.AUTO);
            w.layout();
            w.show();
        }
    }

    /**
     * Show the content wizard
     *
     * @param linker
     */
    public static void showContentWizard(final Linker linker) {
        showContentWizard(linker, null);
    }

    public static void showContentWizard(final Linker linker, final String nodeType){
        showContentWizard(linker, nodeType, false);
    }
    /**
     * Show content wizard with a selected node type
     *
     * @param linker
     * @param nodeType
     */
    public static void showContentWizard(final Linker linker, final String nodeType, final boolean displayReusableComponents) {
        if(nodeType ==  null){
            showContentWizardByNodeType(linker, null, true);
            return;
        }

        // retrieve the jahiaNodeType and display the wizard
        JahiaContentDefinitionService.App.getInstance().getNodeType(nodeType, new AsyncCallback<GWTJahiaNodeType>() {
            public void onSuccess(GWTJahiaNodeType jahiaNodeType) {
                if (jahiaNodeType != null) {
                    Log.debug("jahia node type found" + jahiaNodeType.getLabel() + "," + jahiaNodeType.getName());
                    showContentWizardByNodeType(linker, jahiaNodeType, displayReusableComponents);
                }else{
                    Log.error("Error while triing to get GWTNodetype with type[" + nodeType + "]");                    
                }
            }

            public void onFailure(Throwable throwable) {
                MessageBox.alert("Alert", "Unable to display 'Add content page wizard' ", null);
                Log.error("Error while triing to get GWTNodetype with type[" + nodeType + "]", throwable);
            }
        });
    }

    /**
     * Show content wizard with a pre-seleected node Type
     *
     * @param linker
     * @param nodeType
     * @param displayReusableComponents
     */
    public static void showContentWizardByNodeType(final Linker linker, final GWTJahiaNodeType nodeType,
                                                   boolean displayReusableComponents) {
        GWTJahiaNode parent = linker.getSelectedNode();
        if (parent == null) {
            parent = linker.getMainNode();
        }
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
            if (selectedItems != null && selectedItems.size() == 1) {
                parent = selectedItems.get(0);
            }
        }
        if (parent != null && !parent.isFile()) {
            new ContentTypeWindow(linker, parent, nodeType,displayReusableComponents).show();
        }
    }

    /**
     * Mount a folder
     *
     * @param linker
     */
    public static void mountFolder(final Linker linker) {
//        GWTJahiaNode parent = (GWTJahiaNode) linker.getMainNode() ;
//        if (parent == null) {
//            final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes() ;
//            if (selectedItems != null && selectedItems.size() == 1) {
//                parent = selectedItems.get(0) ;
//            }
//        }
//        if (parent != null && !parent.isFile()) {
        new Mounter(linker);
//        }
    }

    /**
     * Unmount a folder
     *
     * @param linker
     */
    public static void unmountFolder(final Linker linker) {
        final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() == 1) {
            GWTJahiaNode selection = selectedItems.get(0);
            if (selection.isLocked()) {
                Window.alert(Messages.getResource("fm_failUnmountLock1") + " " + selection.getName() + Messages.getResource("fm_failUnmountLock2") + " " + selection.getLockOwner());
            } else if (Window.confirm(Messages.getResource("fm_confUnmount") + " " + selection.getName() + " ?")) {
                linker.loading(Messages.getResource("fm_unmounting"));
                List<String> selectedPaths = new ArrayList<String>(1);
                selectedPaths.add(selection.getPath());
                JahiaContentManagementService.App.getInstance().deletePaths(selectedPaths, new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        Window.alert(Messages.getResource("fm_failUnmount") + "\n" + throwable.getLocalizedMessage());
                        linker.loaded();
                    }

                    public void onSuccess(Object o) {
                        linker.loaded();
                        linker.refresh();
                    }
                });
            }
        }
    }

    public static void remove(final Linker linker) {
        final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
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
                    JahiaContentManagementService.App.getInstance().deletePaths(selectedPaths, new AsyncCallback() {
                        public void onFailure(Throwable throwable) {
                            Window.alert(Messages.getResource("fm_failDelete") + "\n" + throwable.getLocalizedMessage());
                            linker.loaded();
                        }

                        public void onSuccess(Object o) {
                            linker.loaded();
                            if (refreshTree) {
                                linker.refresh();
                            } else {
                                linker.refreshMainComponent();
                            }
                        }
                    });
                }
            }
        }
    }

    public static void rename(final Linker linker) {
        final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
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
                    JahiaContentManagementService.App.getInstance().rename(selection.getPath(), newName, new AsyncCallback() {
                        public void onFailure(Throwable throwable) {
                            Window.alert(Messages.getResource("fm_failRename") + "\n" + throwable.getLocalizedMessage());
                            linker.loaded();
                        }

                        public void onSuccess(Object o) {
                            linker.loaded();
                            if (folder) {
                                linker.refresh();
                            } else {
                                linker.refreshMainComponent();
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
        final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
        final GWTJahiaNode parentItem = (GWTJahiaNode) linker.getMainNode();
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
                    JahiaContentManagementService.App.getInstance().checkExistence(parentItem.getPath() + "/" + archName, new AsyncCallback<Boolean>() {
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

    private static void forceZip(final List<GWTJahiaNode> selectedItems, final String archName, final Linker linker) {
        List<String> selectedPaths = new ArrayList<String>(selectedItems.size());
        for (GWTJahiaNode node : selectedItems) {
            selectedPaths.add(node.getPath());
        }
        JahiaContentManagementService.App.getInstance().zip(selectedPaths, archName, new AsyncCallback() {
            public void onFailure(Throwable throwable) {
                Window.alert(Messages.getResource("fm_failZip") + "\n" + throwable.getLocalizedMessage());
                linker.loaded();
            }

            public void onSuccess(Object o) {
                linker.loaded();
                linker.refreshMainComponent();
            }
        });
    }

    public static void unzip(final Linker linker) {
        final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() > 0) {
            linker.loading(Messages.getResource("fm_unzipping"));
            List<String> selectedPaths = new ArrayList<String>(selectedItems.size());
            for (GWTJahiaNode node : selectedItems) {
                if (node.getName().endsWith(".zip") || node.getName().endsWith(".ZIP")) {
                    selectedPaths.add(node.getPath());
                }
            }
            JahiaContentManagementService.App.getInstance().unzip(selectedPaths, new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    Window.alert(Messages.getResource("fm_failUnzip") + "\n" + throwable.getLocalizedMessage());
                    linker.loaded();
                }

                public void onSuccess(Object o) {
                    linker.loaded();
                    linker.refresh();
                }
            });
        }
    }

    public static void lock(boolean lock, final Linker linker) {
        final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
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
                JahiaContentManagementService.App.getInstance().setLock(selectedPaths, lock, new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        MessageBox.alert("Error", throwable.getLocalizedMessage(), null);
                        linker.loaded();
                        linker.refreshMainComponent();
                    }

                    public void onSuccess(Object o) {
                        linker.loaded();
                        linker.refreshMainComponent();
                    }
                });
            }
        }
    }

    public static void resizeImage(final Linker linker) {
        final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() == 1) {
            final GWTJahiaNode selectedNode = selectedItems.get(0);
            if (selectedNode != null) {
                new ImageResize(linker, selectedNode).show();
            }
        }
    }

    public static void rotateImage(final Linker linker) {
        final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() == 1) {
            final GWTJahiaNode selectedNode = selectedItems.get(0);
            if (selectedNode != null) {
                new ImageRotate(linker, selectedNode).show();
            }
        }
    }

    public static void exportContent(final Linker linker) {
        final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
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
        final List<GWTJahiaNode> selectedItems =  linker.getSelectedNodes();
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

    public static void saveAsReusableComponent(final Linker linker) {
        final GWTJahiaNode target = linker.getSelectedNode();
        if (target != null) {
            JahiaContentDefinitionService.App.getInstance().getNodeType("jnt:reusableComponent", new AsyncCallback<GWTJahiaNodeType>() {
                        public void onFailure(Throwable caught) {
                            MessageBox.alert("Alert",
                                    "Unable to load node type definitions for type 'jnt:reusableComponent'. Cause: "
                                            + caught.getLocalizedMessage(),
                                    null);
                        }

                        public void onSuccess(final GWTJahiaNodeType nodeType) {

                            JahiaContentManagementService.App.getInstance().getRoot(JCRClientUtils.REUSABLE_COMPONENTS_REPOSITORY, target.getNodeTypes().get(0), null, null, null, null,true, new AsyncCallback<List<GWTJahiaNode>>() {
                                public void onFailure(Throwable caught) {
                                    MessageBox.alert("Alert",
                                            "Unable to load reusable component node for current site. Cause: "
                                                    + caught.getLocalizedMessage(),
                                            null);
                                }

                                public void onSuccess(List<GWTJahiaNode> result) {
                                    if (result.isEmpty()) {
                                        MessageBox.alert("Alert",
                                                "Unable to load reusable components root node",
                                                null);
                                    } else {
                                        Map<String, GWTJahiaNodeProperty> props = new HashMap<String, GWTJahiaNodeProperty>(1);
                                        props.put("j:targetReference", new GWTJahiaNodeProperty("j:targetReference", new GWTJahiaNodePropertyValue(target, GWTJahiaNodePropertyType.WEAKREFERENCE)));
                                        new CreateReusableContentEngine(linker, result.get(0), nodeType, props, null, false).show();                                        
                                    }
                                }
                            });
                        }
                    });
        }
    }

}
