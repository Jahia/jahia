/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
