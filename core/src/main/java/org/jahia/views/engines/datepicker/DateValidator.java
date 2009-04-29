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
package org.jahia.views.engines.datepicker;


public class DateValidator implements DateValidatorInterface {

    public static final int NO_COMP = -1;
    public static final int COMP_BIGGER = 1;
    public static final int COMP_SMALLER = 2;
    public static final int COMP_EQUAL = 3;
    public static final int COMP_BIGGER_EQUAL = 4;
    public static final int COMP_SMALLER_EQUAL = 5;

    private long startDate;
    private int comparator;

    public DateValidator(long startDate, int comparator){
        this.startDate = startDate;
        this.comparator = comparator;
    }

    public int getComparator(){
        return this.comparator;
    }

    public long getStartDate(){
        return this.startDate;
    }

    public void setStartDate(long date){
        this.startDate = date;
    }

    public boolean isValid(long date){
        switch (comparator) {
            case COMP_BIGGER:
                return (date > startDate);
            case COMP_SMALLER:
                return (date < startDate);
            case COMP_EQUAL:
                return (date == startDate);
            case COMP_BIGGER_EQUAL:
                return (date >= startDate);
            case COMP_SMALLER_EQUAL:
                return (date <= startDate);
        }
        return true;
    }
}