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

package org.jahia.ajax.gwt.engines.pdisplay.server.util;

import org.jahia.ajax.gwt.commons.client.beans.GWTJahiaProcessJob;

import java.util.Comparator;

/**
 * User: jahia
 * Date: 31 janv. 2008
 * Time: 14:25:11
 */
public class GWTProcessJobComparator<T extends GWTJahiaProcessJob> implements Comparator<T> {

    public final static int CREATED = 0;
    public final static int TYPE = 1;
    public final static int OWNER = 2;
    public final static int START = 3;
    public final static int END = 4;
    public final static int DURATION = 5;
    public final static int STATUS = 6;
    private int field;
    private boolean asc;

    public GWTProcessJobComparator(int field, boolean asc) {
        this.field = field;
        this.asc = asc;
    }

    public int compare(GWTJahiaProcessJob gwtProcessJob1, GWTJahiaProcessJob gwtProcessJob2) {
        String comparable1 ;
        String comparable2 ;
        switch (field) {
            case CREATED:
                //  logger.debug("Sort Param.: created ");
                comparable1 = gwtProcessJob1.getJobCreatedComparable();
                comparable2 = gwtProcessJob2.getJobCreatedComparable();
                break;
            case TYPE:
                //   logger.debug("Sort Param.: type ");
                comparable1 = gwtProcessJob1.getJobType();
                comparable2 = gwtProcessJob2.getJobType();
                break;
            case OWNER:
                //     logger.debug("Sort Param.: owner ");
                comparable1 = gwtProcessJob1.getJobUserName();
                comparable2 = gwtProcessJob2.getJobUserName();
                break;
            case START:
                //     logger.debug("Sort Param.: start ");
                comparable1 = gwtProcessJob1.getJobBeginComparable();
                comparable2 = gwtProcessJob2.getJobBeginComparable();
                break;
            case END:
                //     logger.debug("Sort Param.: end ");
                comparable1 = gwtProcessJob1.getJobEndComparable();
                comparable2 = gwtProcessJob2.getJobEndComparable();
                break;
            case DURATION:
                //     logger.debug("Sort Param.: duration ");
                comparable1 = gwtProcessJob1.getJobDuration();
                comparable2 = gwtProcessJob2.getJobDuration();
                break;
            case STATUS:
                //     logger.debug("Sort Param.: status ");
                comparable1 = gwtProcessJob1.getJobStatus();
                comparable2 = gwtProcessJob2.getJobStatus();
                break;
            default:
                //     logger.debug("Sort Param.: default ");
                comparable1 = gwtProcessJob1.getJobCreated();
                comparable2 = gwtProcessJob2.getJobCreated();
                break;
        }
        if (comparable1 == null) {
            if (comparable2 == null) {
                return 0;
            }
            if (asc) {
                return -1;
            } else {
                return 1 ;
            }
        } else {
            if (comparable2 == null) {
                if (asc) {
                    return 1;
                } else {
                    return -1 ;
                }
            }
        }
        if (asc) {
            return comparable2.compareTo(comparable1);
        } else {
            return comparable1.compareTo(comparable2);
        }
    }
}
