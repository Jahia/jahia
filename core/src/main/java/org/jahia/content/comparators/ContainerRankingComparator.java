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

import org.jahia.data.containers.JahiaContainer;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.containers.ContentContainer;

import java.util.Comparator;

/**
 * <p>Title: This container comparator orders containers by rank</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class ContainerRankingComparator implements Comparator {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ContainerRankingComparator.class);

    private ProcessingContext processingContext;

    public ContainerRankingComparator(ProcessingContext processingContext) {
        this.processingContext = processingContext;
    }

    public int compare(Object o1, Object o2) {
        ContentContainer leftContainer = (ContentContainer) o1;
        ContentContainer rightContainer = (ContentContainer) o2;
        try {
            JahiaContainer leftJahiaContainer = leftContainer.getJahiaContainer(
                processingContext, processingContext.getEntryLoadRequest());
            JahiaContainer rightJahiaContainer = rightContainer.
                                                 getJahiaContainer(processingContext,
                processingContext.getEntryLoadRequest());
            if (leftJahiaContainer == null) {
                logger.debug("Left JahiaContainer in comparison is null");
                return -1;
            }
            if (rightJahiaContainer == null) {
                logger.debug("Right JahiaContainer in comparison is null");
                return 1;
            }
            return new Integer(leftJahiaContainer.getRank()).compareTo(new Integer(rightJahiaContainer.getRank()));
        } catch (JahiaException je) {
            logger.error("Error while accessing Jahia containers for entry load request " + processingContext.getEntryLoadRequest(), je);
            return 0;
        }
    }

    public boolean equals(Object obj) {
       if (this == obj) return true;
        
       if (obj != null && this.getClass() == obj.getClass()) {
            return true;
       }
       return false;
    }
}