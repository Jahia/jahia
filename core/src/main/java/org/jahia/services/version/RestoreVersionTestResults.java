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
package org.jahia.services.version;

import org.jahia.content.ContentPageKey;
import org.jahia.content.ObjectKey;
import org.jahia.content.TreeOperationResult;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>Title: Contains the results of a test of validity for restoration </p>
 * <p>Description: This class contains the results a complete operation of
 * test for a restore version on a content object sub tree.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Serge Huber
 * @version 1.0
 */

public class RestoreVersionTestResults extends TreeOperationResult {
    private static final long serialVersionUID = 8153499401578591739L;

    private Set restoredContainers = new HashSet();
    private Set restoredPages = new HashSet();

    public RestoreVersionTestResults() {
    }

    public RestoreVersionTestResults(int initialStatus) {
        super(initialStatus);
    }

    public Set getRestoredContainers() {
        return restoredContainers;
    }

    public void setRestoredContainers(Set restoredContainers) {
        this.restoredContainers = restoredContainers;
    }

    public Set getRestoredPages() {
        return restoredPages;
    }

    public void setRestoredPages(Set restoredPages) {
        this.restoredPages = restoredPages;
    }

    public void addRestoredContent(ObjectKey key){
        if (key instanceof ContentPageKey){
            this.restoredPages.add(key);
        }
    }

    public void merge(TreeOperationResult input) {
        super.merge(input);
        if (input instanceof RestoreVersionTestResults){
            this.restoredContainers.addAll(((RestoreVersionTestResults)input).getRestoredContainers());
            this.restoredPages.addAll(((RestoreVersionTestResults)input).getRestoredPages());
        }
    }

}