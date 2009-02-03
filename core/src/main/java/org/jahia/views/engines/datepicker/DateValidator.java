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