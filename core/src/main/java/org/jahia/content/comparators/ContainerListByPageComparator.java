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
 package org.jahia.content.comparators;

import java.util.Comparator;
import org.jahia.services.containers.ContentContainerList;

/**
 * <p>Title: This container list comparator orders containers lists by page ID</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class ContainerListByPageComparator implements Comparator {

    public ContainerListByPageComparator() {
    }

    public int compare(Object o1, Object o2) {
        ContentContainerList leftContainerList = (ContentContainerList) o1;
        ContentContainerList rightContainerList = (ContentContainerList) o2;
        return new Integer(leftContainerList.getPageID()).compareTo(new Integer(rightContainerList.getPageID()));
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            return true;
        }
        return false;
    }
}