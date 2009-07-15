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
package org.jahia.ajax.gwt.client.util.icons;

import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.core.client.GWT;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Jul 15, 2009
 * Time: 10:50:04 AM
 */
public class ContentModelIconProvider implements ModelIconProvider<GWTJahiaNode> {

    public static final ContentIconsImageBundle CONTENT_ICONS = GWT.create(ContentIconsImageBundle.class);

    public static final String EXE = "icon-exe";

    public static final String HTML = "icon-html";

    public static final String PDF = "icon-pdf";

    public static final String PNG = "icon-img";

    public static final String GIF = "icon-gif";

    public static final String JPG = "icon-jpg";

    public static final String JPEG = "icon-jpeg";

    public static final String PORTLET = "icon-portlet";

    public static final String RSS = "icon-rss";


    public ContentModelIconProvider() {
    }



    /**
     * Return an AbstractImagePrototype depending on the extension
     * @param gwtJahiaNode
     * @return
     */
    public AbstractImagePrototype getIcon(GWTJahiaNode gwtJahiaNode) {
        if (gwtJahiaNode != null) {
            if (gwtJahiaNode.isFile()) {
                String ext = gwtJahiaNode.getExt();
                if (ext != null) {
                    if (ext.equalsIgnoreCase(EXE)) {
                        return CONTENT_ICONS.exe();
                    } else if (ext.equalsIgnoreCase(HTML)) {
                        return CONTENT_ICONS.html();
                    } else if (ext.equalsIgnoreCase(PNG) || ext.equalsIgnoreCase(GIF) || ext.equalsIgnoreCase(JPG) || ext.equalsIgnoreCase(JPEG)) {
                        return CONTENT_ICONS.img();
                    }else if (ext.equalsIgnoreCase(PDF)) {
                        return CONTENT_ICONS.pdf();
                    } else {
                        return CONTENT_ICONS.file();
                    }
                }
            } else {
                return CONTENT_ICONS.dir();
            }
        }
        // by default we consedered as a directory
        return CONTENT_ICONS.dir();
    }
}
