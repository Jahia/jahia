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
package org.jahia.utils.comparator;

import org.jahia.params.ProcessingContext;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.containers.ContainerFactory;
import org.jahia.content.ContentObject;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.exceptions.JahiaException;

import java.util.Map;
import java.util.List;
import java.util.Comparator;

/**
 *
 * Utility class that allows sorting Jahia pages using their ranking values
 *
 * @author Xavier Lawrence
 */
public class JahiaPageComparator implements Comparator {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaPageComparator.class);
    private ProcessingContext jParams;
    private Map cachedContainerLists;

    public JahiaPageComparator(ProcessingContext jParams, Map cachedContainerLists) {
        this.jParams = jParams;
        this.cachedContainerLists = cachedContainerLists;
    }

    /**
     * Simple comparator that Compares 2 pages and returns the correct value regarding their corresponding ranking.
     */
    public int compare(final Object o1, final Object o2) {
        final ContentPage p1 = (ContentPage) o1;
        final ContentPage p2 = (ContentPage) o2;
        final EntryLoadRequest elr = jParams.getEntryLoadRequest();

        try {
            final ContentObject pageField1 = p1.getParent(elr);
            if (pageField1 == null)
                return 1;
            final ContentContainer cont1 = (ContentContainer) pageField1.getParent(elr);
            if (cont1 == null) 
            		return 1;		
            final ContentContainerList cList1 = (ContentContainerList) cont1.getParent(elr);
            if (cList1 == null) 
            		return 1;		
            final String property = cList1.getProperty("automatic_sort_handler");
            if (property != null && property.trim().length() > 0) {
                final ContentObject pageField2 = p2.getParent(elr);
                if (pageField2 == null)
                    return -1;
                final ContentContainer cont2 = (ContentContainer) pageField2.getParent(elr);
                if (cont2 == null) 
            				return -1;		
                final ContentContainerList cList2 = (ContentContainerList) cont2.getParent(elr);
                if (cList2 == null) 
            				return -1;
                if (cList1.getID() != cList2.getID())
                    return -1;
                else {
                    Integer cListID = new Integer(cList1.getID());
                    List ctnIds = (List) cachedContainerLists.get(cListID);
                    if (ctnIds == null) {
                        ctnIds = ContainerFactory.getInstance().getSorteredAndFilteredCtnIds(
                                        jParams,
                                        elr,
                                        cList1.getJahiaContainerList(jParams, elr), null);
                        cachedContainerLists.put(cListID, ctnIds);
                    }

                    Integer int1 = new Integer(ctnIds.indexOf(new Integer(cont1
                            .getID())));
                    Integer int2 = new Integer(cont2.getID());
                    if (int1.intValue() == -1 && int2.intValue()== -1){
                        return -1;
                    }
                    return int1.compareTo(int2);
                }
            } else {
                final JahiaContainer jahiaContainer1 = cont1.getJahiaContainer(jParams, elr);
                if (jahiaContainer1 == null)
                    return 1;
                final int rank1 = jahiaContainer1.getRank();

                final ContentObject pageField2 = p2.getParent(elr);
                if (pageField2 == null)
                    return -1;
                final ContentContainer cont2 = (ContentContainer) pageField2.getParent(elr);
                if (cont2 == null) return -1;
                final JahiaContainer jahiaContainer2 = cont2.getJahiaContainer(jParams, elr);
                if (jahiaContainer2 == null)
                    return -1;
                final int rank2 = jahiaContainer2.getRank();

                if (rank1 > rank2) {
                    return 1;
                } else if (rank1 <= rank2) {
                    return -1;
                } else {
                    return new Integer(jahiaContainer1.getID())
                            .compareTo(new Integer(jahiaContainer2.getID()));
                }
            }
        } catch (ClassCastException ce) {
            logger.debug("ClassCastException: " + ce);
            return -1;
        } catch (JahiaException je) {
            logger.error("Error in Comparator", je);
            return -1;
        }
    }
}
