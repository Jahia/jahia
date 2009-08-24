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
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Jul 15, 2009
 * Time: 10:50:04 AM
 */
public class ContentModelIconProvider implements ModelIconProvider<GWTJahiaNode> {

    public static final ContentIconsImageBundle CONTENT_ICONS = GWT.create(ContentIconsImageBundle.class);

    public static final String CONTENT = "icon-content";

    public static final String DIR = "icon-dir";

    public static final String DOC = "icon-doc";

    public static final String EXE = "icon-exe";

    public static final String FILE = "icon-file";

    public static final String GEAR = "icon-gearth";

    public static final String HTML = "icon-html";

    public static final String IMG = "icon-img";

    public static final String LIST = "icon-list";

    public static final String MASHUP = "icon-mashup";

    public static final String PAGE = "icon-page";

    public static final String PDF = "icon-pdf";

    public static final String PLACE_HOLDER = "icon-placeholder";

    public static final String PORTLET = "icon-portlet";

    public static final String PPT = "icon-ppt";

    public static final String RAR = "icon-rar";

    public static final String SOUND = "icon-sound";

    public static final String TXT = "icon-txt";

    public static final String USER_GROUP = "icon-user-group";

    public static final String USER = "icon-user";

    public static final String VIDEO = "icon-video";

    public static final String XLS = "icon-xls";

    public static final String ZIP = "icon-zip";

    public static final String LOCK = "lock";


    private static ContentModelIconProvider iconProvider = new ContentModelIconProvider();

    private ContentModelIconProvider() {
    }


    public static ContentModelIconProvider getInstance() {
        if (iconProvider == null) {
            iconProvider = new ContentModelIconProvider();
        }
        return iconProvider;
    }


    /**
     * Return an AbstractImagePrototype depending on the extension and the displayLock flag
     *
     * @param gwtJahiaNode
     * @return
     */
    public AbstractImagePrototype getIcon(GWTJahiaNode gwtJahiaNode) {
        if (gwtJahiaNode != null) {
            String ext = gwtJahiaNode.getExt();
            return getIcon(ext);
        }
        return CONTENT_ICONS.file();
    }

    public AbstractImagePrototype getIcon(String ext) {
        if (ext != null) {
            if (ext.equalsIgnoreCase(CONTENT)) {
                return CONTENT_ICONS.content();
            } else if (ext.equalsIgnoreCase(DIR)) {
                return CONTENT_ICONS.dir();
            } else if (ext.equalsIgnoreCase(DOC)) {
                return CONTENT_ICONS.doc();
            } else if (ext.equalsIgnoreCase(EXE)) {
                return CONTENT_ICONS.exe();
            } else if (ext.equalsIgnoreCase(FILE)) {
                return CONTENT_ICONS.file();
            } else if (ext.equalsIgnoreCase(GEAR)) {
                return CONTENT_ICONS.gearth();
            } else if (ext.equalsIgnoreCase(HTML)) {
                return CONTENT_ICONS.html();
            } else if (ext.equalsIgnoreCase(IMG)) {
                return CONTENT_ICONS.img();
            } else if (ext.equalsIgnoreCase(LIST)) {
                return CONTENT_ICONS.list();
            } else if (ext.equalsIgnoreCase(MASHUP)) {
                return CONTENT_ICONS.mashup();
            } else if (ext.equalsIgnoreCase(PAGE)) {
                return CONTENT_ICONS.page();
            } else if (ext.equalsIgnoreCase(PDF)) {
                return CONTENT_ICONS.pdf();
            } else if (ext.equalsIgnoreCase(PLACE_HOLDER)) {
                return CONTENT_ICONS.placeholder();
            } else if (ext.equalsIgnoreCase(PORTLET)) {
                return CONTENT_ICONS.portlet();
            } else if (ext.equalsIgnoreCase(PPT)) {
                return CONTENT_ICONS.ppt();
            } else if (ext.equalsIgnoreCase(RAR)) {
                return CONTENT_ICONS.rar();
            } else if (ext.equalsIgnoreCase(SOUND)) {
                return CONTENT_ICONS.sound();
            } else if (ext.equalsIgnoreCase(TXT)) {
                return CONTENT_ICONS.txt();
            } else if (ext.equalsIgnoreCase(USER_GROUP)) {
                return CONTENT_ICONS.userGroup();
            } else if (ext.equalsIgnoreCase(USER)) {
                return CONTENT_ICONS.user();
            } else if (ext.equalsIgnoreCase(VIDEO)) {
                return CONTENT_ICONS.video();
            } else if (ext.equalsIgnoreCase(XLS)) {
                return CONTENT_ICONS.xls();
            } else if (ext.equalsIgnoreCase(ZIP)) {
                return CONTENT_ICONS.zip();
            } else if (ext.equalsIgnoreCase(LOCK)) {
                return CONTENT_ICONS.lock();
            }
        }
        return CONTENT_ICONS.file();
    }

    public AbstractImagePrototype getLockIcon() {
        return CONTENT_ICONS.lock();
    }
}
