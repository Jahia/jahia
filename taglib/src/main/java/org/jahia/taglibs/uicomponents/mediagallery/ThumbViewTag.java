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
package org.jahia.taglibs.uicomponents.mediagallery;

import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspException;
import java.util.Random;

/**
 * a simple iterating body tag to display a list of image files in a webdav path
 */
@SuppressWarnings("serial")
public class ThumbViewTag extends AbstractJahiaTag {

    private static final transient org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ThumbViewTag.class);

    private String path;

    public void setPath(String path) {
        this.path = path;
    }

    public int doStartTag() throws JspException {
        final StringBuffer buf = new StringBuffer();
        try {
            if (path.length() > 0 && !path.equals("null")) {
                addThumbViewMessageResources();

                buf.append("<div ");
                if (cssClassName != null && cssClassName.length() > 0) {
                    buf.append("class=\"");
                    buf.append(cssClassName);
                    buf.append("\" ");
                }

                buf.append(JahiaType.JAHIA_TYPE).append("=\"").append(JahiaType.MEDIA_GALLERY);
                buf.append("\" id=\"").append(new Random().nextInt() + System.currentTimeMillis());
                buf.append("\" path=\"").append(path).append("\"></div>\n");

                pageContext.getOut().print(buf.toString());
            }
        } catch (final Exception e) {
            logger.error("Error in ThumbViewTag", e);
        }

        return SKIP_BODY;
    }

    private void addThumbViewMessageResources() {
        // add messages required by the subscriptions toolbar
        addGwtDictionaryMessage("fm_thumbFilter", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.thumbFilter.label"));
        addGwtDictionaryMessage("fm_thumbSort", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.thumbSort.label"));
        addGwtDictionaryMessage("fm_thumbSortName", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.thumbSortName.label"));
        addGwtDictionaryMessage("fm_thumbSortSize", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.thumbSortSize.label"));
        addGwtDictionaryMessage("fm_thumbSortLastModif", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.thumbSortLastModif.label"));
        addGwtDictionaryMessage("fm_invertSort", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.invertSort.label"));
    }

    public int doEndTag() throws JspException {
        path = null;
        return EVAL_PAGE;
    }
}

