/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.util.nodes;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 8 juil. 2008 - 17:13:07
 */
public class CopyPasteEngine {

    private static CopyPasteEngine m_instance = null ;

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

    public List<GWTJahiaNode> getCopiedPaths() {
        return copiedPaths;
    }

    public void setCopiedPaths(List<GWTJahiaNode> copiedPaths) {
        cut = false ;
        this.copiedPaths = copiedPaths;
    }

    public void setCutPaths(List<GWTJahiaNode> cutPaths) {
        this.copiedPaths = cutPaths;
        cut = true ;
    }

    public void onPastedPath() {
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

}
