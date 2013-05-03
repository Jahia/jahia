/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.util.content;

import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
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
    private List<GWTJahiaNode> copiedPaths;
    private boolean cut ;

    public static CopyPasteEngine getInstance() {
        if (m_instance == null) {
            m_instance = new CopyPasteEngine() ;
        }
        return m_instance ;
    }

    protected CopyPasteEngine() {}

    public void paste(GWTJahiaNode m, final Linker linker) {
        final List<String> copiedPaths = new ArrayList<String>();
        for (GWTJahiaNode node : getCopiedPaths()) {
            copiedPaths.add(m.getPath() + "/" + node.getName());
        }
        JahiaContentManagementService
                .App.getInstance().paste(JCRClientUtils.getPathesList(getCopiedPaths()), m.getPath(), null, isCut(), new BaseAsyncCallback() {
            public void onApplicationFailure(Throwable throwable) {
                Window.alert(Messages.get("failure.paste.label") + "\n" + throwable.getLocalizedMessage());
                linker.loaded();
            }

            public void onSuccess(Object o) {
                afterPaste(linker, copiedPaths);
            }
        });
    }
    public void pasteReference(GWTJahiaNode m, final Linker linker) {
        final List<String> copiedPaths = new ArrayList<String>();
        for (GWTJahiaNode node : getCopiedPaths()) {
            copiedPaths.add(m.getPath() + "/" + node.getName());
        }
        JahiaContentManagementService
                .App.getInstance().pasteReferences(JCRClientUtils.getPathesList(getCopiedPaths()), m.getPath(), null, new BaseAsyncCallback() {
            public void onApplicationFailure(Throwable throwable) {
                Window.alert(Messages.get("failure.paste.label") + "\n" + throwable.getLocalizedMessage());
                linker.loaded();
            }

            public void onSuccess(Object o) {
                afterPaste(linker, copiedPaths);
            }
        });

    }

    private void afterPaste(Linker linker, List<String> copiedPaths) {
        boolean refresh = false;
        for (GWTJahiaNode n : getCopiedPaths()) {
            if (!n.isFile()) {
                refresh = true;
                break;
            }
        }
        onPastedPath();
        linker.setSelectPathAfterDataUpdate(copiedPaths);
        linker.loaded();
        Map<String, Object> data = new HashMap<String, Object>();
        if (refresh) {
            data.put(Linker.REFRESH_ALL, true);
        } else {
            data.put(Linker.REFRESH_MAIN, true);
        }
        linker.refresh(data);
    }

    public List<GWTJahiaNode> getCopiedPaths() {
        return copiedPaths;
    }

    public void setCopiedPaths(List<GWTJahiaNode> copiedPaths) {
        cut = false ;
        this.copiedPaths = new ArrayList<GWTJahiaNode>(copiedPaths);
        ClipboardActionItem.setCopied(copiedPaths);
        updatePlaceholders();
    }

    public void setCutPaths(List<GWTJahiaNode> cutPaths) {
        this.copiedPaths = new ArrayList<GWTJahiaNode>(cutPaths);
        this.copiedPaths = cutPaths;
        cut = true ;
        ClipboardActionItem.setCopied(cutPaths);
        updatePlaceholders();
    }

    public void onPastedPath() {
        ClipboardActionItem.removeCopied(copiedPaths);
        copiedPaths = null ;
        cut = false ;
    }

    public boolean isCut() {
        return cut ;
    }

    public boolean canCopyTo(GWTJahiaNode dest) {
        if (dest == null) {
            return false ;
        }
        if (copiedPaths == null) {
            return false;
        }

        for (GWTJahiaNode copiedPath : copiedPaths) {
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
        List<GWTJahiaNode> sources = getCopiedPaths();
        boolean allowed = true;

        if (nodetypes != null && nodetypes.length() > 0) {
            if (sources != null) {
                String[] allowedTypes = nodetypes.split(" |,");
                for (GWTJahiaNode source : sources) {
                    boolean nodeAllowed = false;
                    for (String type : allowedTypes) {
                        if (source.getNodeTypes().contains(type) || source.getInheritedNodeTypes().contains(type)) {
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
    
    private void updatePlaceholders() {
        for (PlaceholderModule module : placeholders) {
            module.updatePasteButton();
        }
    }
    
    public void addPlaceholder(PlaceholderModule module) {
        placeholders.add(module);
    }
}
