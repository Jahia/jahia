/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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