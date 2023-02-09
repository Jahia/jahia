/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.util;

import java.util.*;

import org.jahia.ajax.gwt.client.messages.Messages;

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
     * Returns formatted lock info label based on the provided key.
     *
     * @param lockInfo
     *            the lock information key
     * @return formatted lock info label based on the provided key
     */
    public static String getLockLabel(String lockInfo) {
        if (lockInfo == null || lockInfo.length() == 0) {
            return lockInfo;
        }
        if (lockInfo.startsWith("label.")) {
            return Messages.get(lockInfo);
        } else {
            return lockInfo.contains(":") ? (lockInfo.substring(0, lockInfo.indexOf(":")) + " ("
                    + lockInfo.substring(lockInfo.indexOf(":") + 1) + ")") : lockInfo;
        }
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

    /**
     * Util to join
     * @param list the list of item to joins
     * @param separator the separator
     */
    public static String join(List<?> list, String separator) {
        if(list == null || list.isEmpty()) {
            return "";
        }

        StringBuilder str = new StringBuilder(list.get(0).toString());
        for (int i = 1; i < list.size(); i++) {
            str.append(separator);
            str.append(list.get(i).toString());
        }
        return str.toString();
    }

    /**
     * Utils to concatenate two arrays
     * @param first first array
     * @param second second array
     * @return concatenate array
     */
    public static String[] concat(String[] first, String[] second) {
        List<String> both = new ArrayList<String>(first.length + second.length);
        Collections.addAll(both, first);
        Collections.addAll(both, second);
        return both.toArray(new String[0]);
    }
}
