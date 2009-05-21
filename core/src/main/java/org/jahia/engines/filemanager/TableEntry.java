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
 package org.jahia.engines.filemanager;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA. User: toto Date: Jul 1, 2003 Time: 12:20:40 PM To change this
 * template use Options | File Templates.
 */
public class TableEntry implements Serializable {
    private String name;
    private String encodedName;
    private String displayName;
    private boolean hasChildren = false;
    private boolean isExpanded;
    private int indent;
    private boolean isLastSister;
    private String type;
    private boolean canWrite;
    private boolean canAdmin;
    private boolean canWriteParent;
    private boolean canAdminParent;
    private boolean hasRevisions;
    private boolean isLocked;

    public TableEntry (String name, String displayName, boolean expanded, int indent,
                       String type, boolean canWrite, boolean canAdmin, boolean canWriteParent,
                       boolean canAdminParent, boolean hasRevisions, boolean isLocked) {
        this.name = name;
        this.displayName = displayName;
        isExpanded = expanded;
        this.indent = indent;
        this.type = type;
        this.canWrite = canWrite;
        this.canAdmin = canAdmin;
        this.canWriteParent = canWriteParent;
        this.canAdminParent = canAdminParent;
        this.hasRevisions = hasRevisions;
        this.isLocked = isLocked;
    }

    public String getName () {
        return name;
    }

    public String getDisplayName () {
        return displayName;
    }

    public String getEncodedName() {
        if (encodedName == null) {
            encodedName = javascriptEncode(name);
        }
        return encodedName;
    }

    public static String javascriptEncode(String name) {
        if (name == null) return null;
        name = URLUtil.URLEncode(name, "UTF-8");
        name = name.replace("+","%2B");
        name = name.replace('%','|');
        return name;
    }

    public static String javascriptDecode(String name) {
        if (name == null) return null;
        name = name.replace('|','%');
        name = URLUtil.URLDecode(name, "UTF-8");
        return name;
    }

    public boolean hasChildren () {
        return hasChildren;
    }

    public void setHasChildren (boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public boolean isExpanded () {
        return isExpanded;
    }

    public int getIndent () {
        return indent;
    }

    public String getType () {
        return type;
    }

    public boolean isLastSister () {
        return isLastSister;
    }

    public void setLastSister (boolean lastSister) {
        isLastSister = lastSister;
    }

//    public Iterator getPermissionsList() {
//        return permissionsList;
//    }
//
//    public Iterator getLocksList() {
//        return locksList;
//    }

    public boolean isCanWrite () {
        return canWrite;
    }

    public boolean isCanAdmin () {
        return canAdmin;
    }

    public boolean isCanWriteParent () {
        return canWriteParent;
    }

    public boolean isCanAdminParent () {
        return canAdminParent;
    }

    public boolean hasRevisions () {
        return hasRevisions;
    }

    public boolean isLocked () {
        return isLocked;
    }
}
