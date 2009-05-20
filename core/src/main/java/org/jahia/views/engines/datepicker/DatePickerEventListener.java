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


public class DatePickerEventListener
{
    protected String eventTarget;
    protected long date = 0;
    protected DateValidator validator;

    public DatePickerEventListener(String eventTarget,
                                   long date,
                                   DateValidator validator ){
        this.eventTarget = eventTarget;
        this.date = date;
        this.validator = validator;
    }

    public void beforeDisplayCalendar( DatePickerEvent ev ){
        if ( ev.getDatePickerData().getDate() == 0 ){
            ev.getDatePickerData().setDate(date);
        }
    }

    public void saveDate ( DatePickerEvent ev ){
        if ( !this.eventTarget.equals(ev.getObject()) ){
            return;
        }

        DatePickerData data = ev.getDatePickerData();
        data.resetErrors();

        String msg = null;
        if ( !validator.isValid(data.getDate()) ){
            switch (validator.getComparator()){
                case DateValidator.COMP_BIGGER:
                    msg = "Date must be bigger than " + DatePicker.getDate(validator.getStartDate());
                    break;
                case DateValidator.COMP_BIGGER_EQUAL:
                    msg = "Date must be bigger or equal to " + DatePicker.getDate(validator.getStartDate());
                    break;
                case DateValidator.COMP_EQUAL:
                    msg = "Date must be equal to " + DatePicker.getDate(validator.getStartDate());
                    break;
                case DateValidator.COMP_SMALLER:
                    msg = "Date must be smaller to " + DatePicker.getDate(validator.getStartDate());
                    break;
                case DateValidator.COMP_SMALLER_EQUAL:
                    msg = "Date must be smaller or equal to " + DatePicker.getDate(validator.getStartDate());
                    break;
            }
        }
        if ( msg != null ){
            data.addError(msg);
            return;
        }
        this.date = data.getDate();
    }

    public long getDate(){
        return this.date;
    }

    public void setDate(long date){
        this.date = date;
    }

    public DateValidator getValidator(){
        return this.validator;
    }

    public void setValidator(DateValidator validator){
        this.validator = validator;
    }
}