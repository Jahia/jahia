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
