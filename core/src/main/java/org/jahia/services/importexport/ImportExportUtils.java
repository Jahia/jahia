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
 package org.jahia.services.importexport;

import org.jahia.params.ProcessingContext;
import org.jahia.content.comparators.ObjectTypeDispatcherComparator;
import org.jahia.content.comparators.ContainerComparator;
import org.jahia.content.comparators.ContainerListComparator;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentContainerListKey;

import java.util.List;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 19 ao�t 2005
 * Time: 16:18:51
 * To change this template use File | Settings | File Templates.
 */
public class ImportExportUtils {
    public static void orderContainerList(List l, ProcessingContext jParams) {
        ObjectTypeDispatcherComparator comparator = new
                ObjectTypeDispatcherComparator();
        comparator.addTypeComparator(ContentContainerKey.CONTAINER_TYPE,
                new ContainerComparator(jParams));
        comparator.addTypeComparator(ContentContainerListKey.
                CONTAINERLIST_TYPE,
                new ContainerListComparator());
        Collections.sort(l, comparator);
    }
}
