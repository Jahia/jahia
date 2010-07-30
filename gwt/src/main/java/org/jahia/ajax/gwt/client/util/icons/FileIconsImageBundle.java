/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 * GWT file icon provider.
 * 
 * @author Sergiy Shyrkov
 */
public interface FileIconsImageBundle extends ImageBundle {

    public static class Provider {
        private static FileIconsImageBundle instance = null;

        public static AbstractImagePrototype get(String fileType) {
            FileIconsImageBundle bundle = getInstance();
            AbstractImagePrototype img = bundle.file();
            if (fileType == null) {
                return img;
            }

            if ("doc".equals(fileType)) {
                img = bundle.doc();
            } else if ("html".equals(fileType)) {
                img = bundle.html();
            } else if ("pdf".equals(fileType)) {
                img = bundle.pdf();
            } else if ("png".equals(fileType)) {
                img = bundle.png();
            } else if ("ppt".equals(fileType)) {
                img = bundle.ppt();
            } else if ("txt".equals(fileType)) {
                img = bundle.txt();
            } else if ("xls".equals(fileType)) {
                img = bundle.xls();
            } else if ("zip".equals(fileType)) {
                img = bundle.zip();
            }

            return img;
        }

        public static synchronized FileIconsImageBundle getInstance() {
            if (instance == null) {
                instance = GWT.create(FileIconsImageBundle.class);
            }
            return instance;
        }
    }

    @Resource("doc.png")
    public AbstractImagePrototype doc();

    @Resource("file.png")
    public AbstractImagePrototype file();

    @Resource("google-docs-32x32.png")
    public AbstractImagePrototype googleDocsLarge();

    @Resource("html.png")
    public AbstractImagePrototype html();

    @Resource("pdf.png")
    public AbstractImagePrototype pdf();

    @Resource("img.png")
    public AbstractImagePrototype png();

    @Resource("ppt.png")
    public AbstractImagePrototype ppt();

    @Resource("swf.png")
    public AbstractImagePrototype swf();

    @Resource("txt.png")
    public AbstractImagePrototype txt();

    @Resource("xls.png")
    public AbstractImagePrototype xls();

    @Resource("zip.png")
    public AbstractImagePrototype zip();

}
