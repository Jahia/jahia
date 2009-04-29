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
package org.jahia.ajax.gwt.engines.pdisplay.server.util;

import org.jahia.ajax.gwt.client.data.GWTJahiaProcessJob;

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
