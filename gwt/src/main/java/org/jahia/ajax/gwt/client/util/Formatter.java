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
package org.jahia.ajax.gwt.client.util;

import java.util.Date;

import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * Helper to format strings, numbers, and other stuff.
 *
 * @author rfelden
 * @version 23 juin 2008 - 16:58:56
 */
public class Formatter {

    private final static float GB = 1073741824f;
    private final static float MB = 1048576f;
    private final static float KB = 1024f;

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
        return DateTimeFormat.getFormat(pattern).format(date);
    }

    /**
     * Enable a TextToolItem changing the CSS style according to its state.
     *
     * @param ti the ToolItem
     */
    public static void enableTextToolItem(TextToolItem ti) {
        if (!ti.isEnabled()) {
            ti.enable();
            String iconStyle = ti.getIconStyle();
            if (iconStyle != null) {
                int ind = iconStyle.indexOf("-bw");
                if (ind != -1) {
                    iconStyle = iconStyle.substring(0, ind);
                    ti.setIconStyle(iconStyle);
                }
            }
        }
    }

    /**
     * Disable a TextToolItem changing the CSS style according to its state.
     *
     * @param ti the ToolItem
     */
    public static void disableTextToolItem(TextToolItem ti) {
        if (ti.isEnabled()) {
            ti.disable();
            String iconStyle = ti.getIconStyle();
            if (iconStyle != null) {
                int ind = iconStyle.indexOf("-bw");
                if (ind == -1) {
                    iconStyle = iconStyle + "-bw";
                    ti.setIconStyle(iconStyle);
                }
            }
        }
    }

    public static void setTextToolItemEnabled(TextToolItem ti, boolean b) {
        if (b) {
            Formatter.enableTextToolItem(ti);
        } else {
            Formatter.disableTextToolItem(ti);
        }
    }


    /**
     * Enable a MenuItem changing the CSS style according to its state.
     *
     * @param mi the MenuItem
     */
    public static void enableMenuItem(MenuItem mi) {
        if (!mi.isEnabled()) {
            mi.enable();
            String iconStyle = mi.getIconStyle();
            if (iconStyle != null) {
                int ind = iconStyle.indexOf("-bw");
                if (ind != -1) {
                    iconStyle = iconStyle.substring(0, ind);
                    mi.setIconStyle(iconStyle);
                }
            }
        }
    }

    /**
     * Disable a MenuItem changing the CSS style according to its state.
     *
     * @param mi the MenuItem
     */
    public static void disableMenuItem(MenuItem mi) {
        if (mi.isEnabled()) {
            mi.disable();
            String iconStyle = mi.getIconStyle();
            if (iconStyle != null) {
                int ind = iconStyle.indexOf("-bw");
                if (ind == -1) {
                    iconStyle = iconStyle + "-bw";
                    mi.setIconStyle(iconStyle);
                }
            }
        }
    }


    public static void setMenuItemEnabled(MenuItem ti, boolean b) {
        if (b) {
            Formatter.enableMenuItem(ti);
        } else {
            Formatter.disableMenuItem(ti);
        }
    }


}
