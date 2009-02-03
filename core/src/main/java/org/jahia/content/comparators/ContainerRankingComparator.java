/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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