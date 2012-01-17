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

package org.jahia.ajax.gwt.client.util;

import java.util.Date;

import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * Helper to format strings, numbers, and other stuff.
 *
 * @author rfelden
 * @version 23 juin 2008 - 16:58:56
 */
public class Formatter {

    public static final float GB = 1073741824f;
    public static final float MB = 1048576f;
    public static final float KB = 1024f;
    public static final DateTimeFormat DEFAULT_DATETIME_FORMAT = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm");

    /**
     * Get a human readable size from a byte size.
     *
     * @param size the size to format
     * @return the formatted size
     */
    public static String getFormattedSize(long size) {
        NumberFormat nf = NumberFormat.getFormat("0.00");
        StringBuffer dispSize = new StringBuffer();
        if (size >= GB) {
            dispSize.append(String.valueOf(nf.format(size / GB))).append(" GB");
        } else if (size >= MB) {
            dispSize.append(String.valueOf(nf.format(size / MB))).append(" MB");
        } else if (size >= KB) {
            dispSize.append(String.valueOf(nf.format(size / KB))).append(" KB");
        } else {
            dispSize.append(String.valueOf(nf.format(size))).append(" B");
        }
        return dispSize.toString();
    }

    /**
     * Get a human readable date formatted according to a given pattern.
     *
     * @param date    the date to format
     * @param pattern the date pattern
     * @return the formatted date
     * @see com.google.gwt.i18n.client.DateTimeFormat
     */
    public static String getFormattedDate(Date date, String pattern) {
        return date != null ? (pattern != null ? DateTimeFormat.getFormat(pattern).format(date) : DEFAULT_DATETIME_FORMAT.format(date)) : "-";
    }

    /**
     * Get a human readable date formatted according to a default pattern.
     *
     * @param date    the date to format
     * @return the formatted date
     * @see com.google.gwt.i18n.client.DateTimeFormat
     */
    public static String getFormattedDate(Date date) {
        return getFormattedDate(date, null);
    }

    /**
     * Enable a Button changing the CSS style according to its state.
     *
     * @param ti the ToolItem
     */
    public static void enableButton(Component ti) {
        if (!ti.isEnabled()) {
            ti.setEnabled(true);
        }
    }

    /**
     * Disable a Button changing the CSS style according to its state.
     *
     * @param ti the ToolItem
     */
    public static void disableButton(Component ti) {
        if (ti.isEnabled()) {
            ti.disable();
        }
    }

    public static void setButtonEnabled(Component ti, boolean b) {
        if (b) {
            Formatter.enableButton(ti);
        } else {
            Formatter.disableButton(ti);
        }
    }


    /**
     * Enable a MenuItem changing the CSS style according to its state.
     *
     * @param mi the MenuItem
     */
    public static void enableMenuItem(Item mi) {
        if (!mi.isEnabled()) {
            mi.enable();
        }
    }

    /**
     * Disable a MenuItem changing the CSS style according to its state.
     *
     * @param mi the MenuItem
     */
    public static void disableMenuItem(Item mi) {
        if (mi.isEnabled()) {
            mi.disable();
        }
    }


    /**
     * Enable / disable item
     * @param ti
     * @param b
     */
    public static void setMenuItemEnabled(MenuItem ti, boolean b) {
        if (b) {
            Formatter.enableMenuItem(ti);
        } else {
            Formatter.disableMenuItem(ti);
        }
    }


}
