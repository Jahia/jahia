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

import java.util.ArrayList;
import java.util.List;

public class DatePickerData
{

    private List errors = new ArrayList();
    private long date = 0;

    public DatePickerData(){
    }

    public DatePickerData(long date){
        this.date = date;
    }

    public long getDate(){
        return this.date;
    }

    public void setDate(long date){
        this.date = date;
    }

    public void addError(String errorMsg){
        this.errors.add(errorMsg);
    }

    public List getErrors(){
        return this.errors;
    }

    public void resetErrors(){
        this.errors = new ArrayList();
    }

    public boolean hasError(){
        return (this.errors.size()>0);
    }
}