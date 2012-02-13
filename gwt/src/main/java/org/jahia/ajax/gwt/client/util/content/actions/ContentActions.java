/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.util.content.actions;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.portlet.PortletWizardWindow;
import org.jahia.ajax.gwt.client.widget.edit.ContentTypeWindow;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

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
            JahiaContentManagementService.App.getInstance().pasteReferences(JCRClientUtils.getPathesList(copyPasteEngine.getCopiedPaths()), m.getPath(), null, new BaseAsyncCallback() {
                public void onApplicationFailure(Throwable throwable) {
                    Window.alert(Messages.get("failure.pasteref.label") + "\n" + throwable.getLocalizedMessage());
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
                HTML link = new HTML(Messages.get("downloadMessage.label") + "<br /><br /><a href=\"" + url + "\" target=\"_new\">" + selection.getName() + "</a>");
                final com.extjs.gxt.ui.client.widget.Window dl = new com.extjs.gxt.ui.client.widget.Window();
                dl.setModal(true);
                dl.setHeading(Messages.get("label.download"));
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
            if (newFolder != null && newFolder.length() > 0) {
                linker.loading(Messages.get("statusbar.newfoldering.label"));
                JahiaContentManagementService.App.getInstance().createFolder(parent.getPath(), newFolder, new BaseAsyncCallback<GWTJahiaNode>() {
                    public void onApplicationFailure(Throwable throwable) {
                        Window.alert(Messages.get("failure.newDir.label") + "\n" + throwable.getLocalizedMessage());
                        linker.loaded();
                    }

                    public void onSuccess(GWTJahiaNode o) {
                        linker.setSelectPathAfterDataUpdate(Arrays.asList(o.getPath()));
                        linker.loaded();
                        linker.refresh(Linker.REFRESH_ALL);
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
    public static void createNode(final Linker linker, final String windowHeaer, final String nodeType, boolean useMainNode) {
        GWTJahiaNode parent;
        if (useMainNode) {
            parent = linker.getSelectionContext().getMainNode();
        }   else {
            parent = linker.getSelectionContext().getSingleSelection();
        }
        if (parent != null) {
            String nodeName = Window.prompt(windowHeaer, "untitled");
            if (nodeName != null && nodeName.length() > 0) {
                linker.loading(Messages.get("statusbar.newfoldering.label"));
                JahiaContentManagementService.App.getInstance().createNode(parent.getPath(), nodeName, nodeType, null, null, null, null, new BaseAsyncCallback<GWTJahiaNode>() {
                    public void onSuccess(GWTJahiaNode o) {
                        linker.setSelectPathAfterDataUpdate(Arrays.asList(o.getPath()));
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
        if (parent != null && !parent.isFile()) {
            ContentTypeWindow.createContent(linker, name, nodeTypes != null ? Arrays.asList(nodeTypes.split(" ")) : null, new HashMap<String, GWTJahiaNodeProperty>(), parent, includeSubTypes, false);
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
                JahiaContentManagementService.App.getInstance().setLock(selectedPaths, lock, new BaseAsyncCallback() {
                    public void onApplicationFailure(Throwable throwable) {
                        MessageBox.alert(Messages.get("label.error", "Error"), throwable.getLocalizedMessage(), null);
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

}
