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
package org.jahia.ajax.gwt.module.contentmanager.client;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.DOM;
import org.jahia.ajax.gwt.client.core.CommonEntryPoint;
import org.jahia.ajax.gwt.client.widget.content.ContentManager;
import org.jahia.ajax.gwt.client.widget.content.ContentManagerEmbedded;
import org.jahia.ajax.gwt.client.util.DOMUtil;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 10 juil. 2008 - 16:58:16
 */
public class ContentManagerEntryPoint extends CommonEntryPoint {

    public void onModuleLoad() {
        super.onModuleLoad();
        RootPanel panel = RootPanel.get("contentmanager");
        if (panel != null) {
            boolean embedded = DOMUtil.getRootAttrAsBoolean(panel, "embedded");
            String nodeTypes = DOMUtil.getRootAttr(panel, "nodeTypes");
            String filters = DOMUtil.getRootAttr(panel, "filters");
            String mimeTypes = DOMUtil.getRootAttr(panel, "mimeTypes");
            String config = DOMUtil.getRootAttr(panel, "config");
            String rootPath = DOMUtil.getRootAttr(panel, "rootPath");
            if (embedded) {
                panel.add(new ContentManagerEmbedded(nodeTypes,filters,mimeTypes,config));
            } else {
                panel.add(new ContentManager(rootPath,nodeTypes,filters,mimeTypes,config));
            }
        }
    }

}
