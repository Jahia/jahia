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
package org.jahia.services.version;

import org.jahia.content.ContentContainerKey;
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
        } else if (key instanceof ContentContainerKey){
            this.restoredContainers.add(key);
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