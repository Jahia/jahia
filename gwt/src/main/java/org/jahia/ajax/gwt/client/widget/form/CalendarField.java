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
package org.jahia.ajax.gwt.client.widget.form;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.util.BaseEventPreview;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.widget.DatePicker;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.extjs.gxt.ui.client.widget.menu.DateMenu;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;

import org.jahia.ajax.gwt.client.widget.calendar.CalendarPicker;
import org.jahia.ajax.gwt.client.widget.menu.CalendarMenu;

import java.util.Date;

/**
 * Provides a calendar input field with a {@link com.extjs.gxt.ui.client.widget.DatePicker} dropdown and automatic
 * date validation.
 * <p/>
 * User: hollis
 * Date: 17 juil. 2008
 * Time: 14:34:36
 */
public class CalendarField extends TriggerField<Date> {

    public static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy HH:mm";
    
    /**
     * DateField error messages.
     */
    public class DateFieldMessages extends TextFieldMessages {

      private String minText;
      private String maxText;
      private String invalidText;

      /**
       * Returns the invalid text.
       * 
       * @return the invalid text
       */
      public String getInvalidText() {
        return invalidText;
      }

      /**
       * Returns the max error text.
       * 
       * @return the error text
       */
      public String getMaxText() {
        return maxText;
      }

      /**
       * Returns the min error text.
       * 
       * @return the error text
       */
      public String getMinText() {
        return minText;
      }

      /**
       * "The error text to display when the date in the field is invalid " +
       * "(defaults to '{value} is not a valid date - it must be in the format
       * {format}')."
       * 
       * @param invalidText the invalid text
       */
      public void setInvalidText(String invalidText) {
        this.invalidText = invalidText;
      }

      /**
       * Sets the error text to display when the date in the cell is after
       * maxValue (defaults to 'The date in this field must be before {
       * {@link #setMaxValue}').
       * 
       * @param maxText the max error text
       */
      public void setMaxText(String maxText) {
        this.maxText = maxText;
      }

      /**
       * The error text to display when the date in the cell is before minValue
       * (defaults to 'The date in this field must be after {@link #setMinValue} 
       * ').
       * 
       * @param minText the min text
       */
      public void setMinText(String minText) {
        this.minText = minText;
      }

    }
    

    private Date minValue;
    private Date maxValue;
    private Menu menu;
    private BaseEventPreview focusPreview;
    private boolean displayTime;

    /**
     * Initializes an instance of this class.
     * @param datePattern
     * @param displayTime
     * @param readOnly
     * @param fieldName
     * @param shadow
     * @param value
     */
    public CalendarField(String datePattern,
            boolean displayTime,
            boolean readOnly,
            final String fieldName,
            boolean shadow,
            Date value) {
        
        this.displayTime = displayTime; 
        autoValidate = false;
        propertyEditor = new DateTimePropertyEditor(datePattern);
        
//        dateField = new DateField();
//        messages = dateField.getMessages();
        messages = new DateFieldMessages();
        
        setTriggerStyle("x-form-date-trigger");
        
        if (value != null) {
//            dateField.setValue(value);
            setValue(value);
        }
        
        if (fieldName != null && fieldName.length() > 0) {
            setName(fieldName);
            setItemId(fieldName);
        }
        
        setReadOnly(readOnly);
        setHideTrigger(readOnly);
        setShadow(shadow);
    }

    /**
     * Creates a new date field.
     */
    public CalendarField() {
        this(DEFAULT_DATE_FORMAT, true, false, null, false, null);
    }

    /**
     * Creates a new date field.
     */
    public CalendarField(Date date) {
        this(DEFAULT_DATE_FORMAT, true, false, null, false, date);
    }

    /**
     * Returns the field's max value.
     *
     * @return the max value
     */
    public Date getMaxValue() {
        return maxValue;
    }

    /**
     * Returns the field's min value.
     *
     * @return the min value
     */
    public Date getMinValue() {
        return minValue;
    }

    @Override
    public DateTimePropertyEditor getPropertyEditor() {
        return (DateTimePropertyEditor) propertyEditor;
    }

    /**
     * Sets the field's max value.
     *
     * @param maxValue the max value
     */
    public void setMaxValue(Date maxValue) {
        this.maxValue = maxValue;
    }

    /**
     * The maximum date allowed.
     *
     * @param minValue the max value
     */
    public void setMinValue(Date minValue) {
        this.minValue = minValue;
    }

    @Override
    public void setRawValue(String value) {
        super.setRawValue(value);
    }

    @Override
    protected void onTriggerClick(ComponentEvent ce) {
        super.onTriggerClick(ce);
        if (disabled || isReadOnly()) {
            return;
        }
        if (menu == null) {
            menu = displayTime ? new CalendarMenu() : new DateMenu();
            menu.addListener(Events.Select, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent ce) {
                    focusValue = getValue();
                    Date date = getDate();
                    if (displayTime) {
                        date.setHours(((CalendarPicker) getDatePicker()).getSelectedHour());
                        date.setMinutes(((CalendarPicker) getDatePicker()).getSelectedMinute());
                    }
                    setValue(date);
                    fireChangeEvent(focusValue, getValue());
                }
            });
        }
        expand();
    }

    @Override
    protected boolean validateValue(String value) {
        if (!super.validateValue(value)) {
            return false;
        }
        if (value.length() < 1) { // if it's blank and textfield didn't flag it then
            // it's valid
            return true;
        }

        DateTimeFormat format = getPropertyEditor().getFormat();

        Date date = null;

        try {
            date = getPropertyEditor().convertStringValue(value);
        } catch (Exception e) {

        }

        if (date == null) {
            String error = null;
            if (getMessages().getInvalidText() != null) {
                error = Format.substitute(getMessages().getInvalidText(), 0);
            } else {
                error = GXT.MESSAGES.dateField_invalidText(value, format.getPattern());
            }
            forceInvalid(error);
            return false;
        }

        if (minValue != null && date.before(minValue)) {

            String error = null;
            if (getMessages().getMinText() != null) {
                error = Format.substitute(getMessages().getMinText(), format.format(minValue));
            } else {
                error = GXT.MESSAGES.dateField_minText(format.format(minValue));
            }
            forceInvalid(error);
            return false;
        }
        if (maxValue != null && date.after(maxValue)) {
            String error = null;
            if (getMessages() != null) {
                error = Format.substitute(getMessages().getMaxText(), format.format(maxValue));
            } else {
                error = GXT.MESSAGES.dateField_minText(format.format(maxValue));
            }
            forceInvalid(error);
            return false;
        }

        return true;
    }

    private void doBlur(ComponentEvent ce) {
        if (menu != null && menu.isVisible()) {
            menu.hide();
        }
        super.onBlur(ce);
        focusPreview.remove();
    }

    @Override
    protected void onBlur(final ComponentEvent ce) {
        String v = getRawValue();
        try {
            setValue(getPropertyEditor().convertStringValue(v));
        } catch (Exception e) {

        }
        Rectangle rec = trigger.getBounds();
        if (rec.contains(BaseEventPreview.getLastClientX(), BaseEventPreview.getLastClientY())) {
            ce.stopEvent();
            return;
        }
        if (menu != null && menu.isVisible()) {
            return;
        }
        hasFocus = false;
        doBlur(ce);
    }

    protected void onDown(FieldEvent fe) {
        fe.cancelBubble();
        if (menu == null || !menu.isAttached()) {
            expand();
        }
    }

    @Override
    protected void onFocus(ComponentEvent ce) {
        super.onFocus(ce);
        focusPreview.add();
    }

    @Override
    protected void onKeyPress(FieldEvent fe) {
        super.onKeyPress(fe);
        int code = fe.event.getKeyCode();
        if (code == 8 || code == 9) {
            if (menu != null && menu.isAttached()) {
                menu.hide();
            }
        }
    }

    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
        focusPreview = new BaseEventPreview();

        new KeyNav<FieldEvent>(this) {
            public void onDown(FieldEvent fe) {
                CalendarField.this.onDown(fe);
            }
        };
    }

    protected void expand() {
        DatePicker picker = getDatePicker();

        Object v = getValue();
        final Date d;
        if (v instanceof Date) {
            d = (Date) v;
        } else {
            d = new Date();
        }
        picker.setValue(d, true);
        picker.setMinDate(minValue);
        picker.setMaxDate(maxValue);
        menu.show(wrap.dom, "tl-bl?");
        menu.focus();
    }

    @Override
    public DateFieldMessages getMessages() {
      return (DateFieldMessages) messages;
    }

    private DatePicker getDatePicker() {
        return menu instanceof DateMenu ? ((DateMenu) menu).getDatePicker()
                : ((CalendarMenu) menu).getDatePicker();
    }

    private Date getDate() {
        return menu instanceof DateMenu ? ((DateMenu) menu).getDate()
                : ((CalendarMenu) menu).getDate();
    }

}
