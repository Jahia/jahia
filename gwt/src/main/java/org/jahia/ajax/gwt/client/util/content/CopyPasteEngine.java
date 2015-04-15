/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
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
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
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
 */
package org.jahia.ajax.gwt.client.util.content;

import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.PlaceholderModule;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ClipboardActionItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author rfelden
 * @version 8 juil. 2008 - 17:13:07
 */
public class CopyPasteEngine {

    private static CopyPasteEngine m_instance = null ;
    private List<PlaceholderModule> placeholders = new ArrayList<PlaceholderModule>();
    
    // Copy-paste
    private final List<GWTJahiaNode> copiedNodes = new ArrayList<GWTJahiaNode>();
    private boolean cut ;

    public static CopyPasteEngine getInstance() {
        if (m_instance == null) {
            m_instance = new CopyPasteEngine() ;
        }
        return m_instance ;
    }

    protected CopyPasteEngine() {}

    public void paste(final GWTJahiaNode m, final Linker linker, List<String> childNodeTypesToSkip) {
        if (!getCopiedNodes().isEmpty()) {
            JahiaContentManagementService
                    .App.getInstance().paste(JCRClientUtils.getPathesList(getCopiedNodes()), m.getPath(), null, isCut(), childNodeTypesToSkip, new BaseAsyncCallback() {
                public void onApplicationFailure(Throwable throwable) {
                    Window.alert(Messages.get("failure.paste.label") + "\n" + throwable.getLocalizedMessage());
                    if (linker instanceof ManagerLinker) {
                        ManagerLinker managerLinker = (ManagerLinker) linker;
                        managerLinker.getLeftComponent().unmask();
                        managerLinker.getTopRightComponent().unmask();
                    } else {
                        linker.loaded();
                    }
                }

                public void onSuccess(Object o) {
                    afterPaste(linker, m);
                }
            });
        }
    }
    public void pasteReference(final GWTJahiaNode m, final Linker linker) {
        JahiaContentManagementService
                .App.getInstance().pasteReferences(JCRClientUtils.getPathesList(getCopiedNodes()), m.getPath(), null, new BaseAsyncCallback() {
            public void onApplicationFailure(Throwable throwable) {
                Window.alert(Messages.get("failure.paste.label") + "\n" + throwable.getLocalizedMessage());
                if (linker instanceof ManagerLinker) {
                    ManagerLinker managerLinker = (ManagerLinker) linker;
                    managerLinker.getLeftComponent().unmask();
                    managerLinker.getTopRightComponent().unmask();
                } else {
                    linker.loaded();
                }
            }

            public void onSuccess(Object o) {
                afterPaste(linker, m);
            }
        });

    }

    private void afterPaste(Linker linker, GWTJahiaNode target) {
        linker.loaded();
        Map<String, Object> data = new HashMap<String, Object>();
        List<String> newPaths = new ArrayList<String>();
        for (GWTJahiaNode node : getCopiedNodes()) {
            newPaths.add(target.getPath() + "/" +  node.getName());
        }
        data.put("node", getCopiedNodes().get(0));
        data.put("nodes", newPaths);
        if (linker instanceof ManagerLinker) {
            ManagerLinker managerLinker = (ManagerLinker) linker;
            managerLinker.getLeftComponent().unmask();
            managerLinker.getTopRightComponent().unmask();
            data.put(Linker.REFRESH_ALL,true);
            linker.refresh(data);
        } else if (isCut() && MainModule.getInstance().getPath().contains(getCopiedNodes().get(0).getPath())) {
            MainModule.staticGoTo(newPaths.get(0),MainModule.getInstance().getTemplate());
        } else {
            data.put(Linker.REFRESH_MAIN, true);
            linker.refresh(data);
        }
        onPastedPath();
    }

    public List<GWTJahiaNode> getCopiedNodes() {
        return copiedNodes;
    }

    public void setCopiedNodes(List<GWTJahiaNode> copiedPaths) {
        cut = false ;
        this.copiedNodes.clear();
        this.copiedNodes.addAll(copiedPaths);
        ClipboardActionItem.setCopied(copiedPaths);
        updatePlaceholders();
    }

    public void setCutPaths(List<GWTJahiaNode> cutPaths) {
        this.copiedNodes.clear();
        this.copiedNodes.addAll(cutPaths);
        cut = true ;
        ClipboardActionItem.setCopied(cutPaths);
        updatePlaceholders();
    }

    public void onPastedPath() {
        ClipboardActionItem.removeCopied(copiedNodes);
        copiedNodes.clear();
        cut = false ;
    }

    public boolean isCut() {
        return cut ;
    }

    public boolean canCopyTo(GWTJahiaNode dest) {
        if (dest == null) {
            return false ;
        }
        if (copiedNodes == null) {
            return false;
        }

        for (GWTJahiaNode copiedPath : copiedNodes) {
            if ((dest.getPath()+"/").startsWith(copiedPath.getPath()+"/")) {
                return false;
            }
            if (isCut() && copiedPath.getPath().substring(0,copiedPath.getPath().lastIndexOf('/')).equals(dest.getPath())) {
                return false;
            }

            // check only first node ..
            return true;    
        }
        return true;
    }

    public boolean checkNodeType(String nodetypes) {
        List<GWTJahiaNode> sources = getCopiedNodes();
        boolean allowed = true;

        if (nodetypes != null && nodetypes.length() > 0) {
            if (!sources.isEmpty()) {
                String[] allowedTypes = nodetypes.split(" |,");
                for (GWTJahiaNode source : sources) {
                    boolean nodeAllowed = false;
                    for (String type : allowedTypes) {
                        if (source.isNodeType(type)
                                || (source.isReference() && source.getReferencedNode() != null && source.getReferencedNode().isNodeType(type))) {
                            nodeAllowed = true;
                            break;
                        }
                    }
                    allowed &= nodeAllowed;
                }
            }
        } else {
            allowed = false;
        }
        return allowed;
    }

    public boolean canPasteAsReference() {
        if (isCut()) {
            return false;
        }
        List<GWTJahiaNode> sources = getCopiedNodes();
        if (!sources.isEmpty()) {
            for (GWTJahiaNode source : sources) {
                if (source.isReference()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updatePlaceholders() {
        for (PlaceholderModule module : placeholders) {
            module.updatePasteButton();
        }
    }
    
    public void addPlaceholder(PlaceholderModule module) {
        placeholders.add(module);
    }
}
