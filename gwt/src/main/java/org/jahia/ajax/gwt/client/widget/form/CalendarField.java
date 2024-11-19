/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.form;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.util.BaseEventPreview;
import com.extjs.gxt.ui.client.util.DateWrapper;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.DatePicker;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.extjs.gxt.ui.client.widget.menu.DateMenu;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import org.jahia.ajax.gwt.client.widget.calendar.CalendarPicker;
import org.jahia.ajax.gwt.client.widget.menu.CalendarMenu;

import java.util.Date;

/**
 * Provides an input field with a date and time picker.
 *
 * @author Sergiy Shyrkov
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
    private String ariaText = "Press Down arrow to select date from a calendar grid";

    /**
     * Returns the ARIA instruction text.
     *
     * @return the text
     */
    public String getAriaText() {
      return ariaText;
    }

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
     * Sets the ARIA instructions for invoking the date picker (defaults to
     * 'Press Down arrow to select date from a calendar grid').
     *
     * @param ariaText the aria text
     */
    public void setAriaText(String ariaText) {
      this.ariaText = ariaText;
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
    private boolean formatValue;
    private boolean displayTime;
    private BaseEventPreview eventPreview;

    /**
     * Initializes an instance of this class.
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
     * Initializes an instance of this class.
     *
     * @param datePattern
     * @param displayTime
     * @param readOnly
     * @param fieldName
     * @param shadow
     * @param value
     */
    public CalendarField(String datePattern, boolean displayTime, boolean readOnly, final String fieldName,
                         boolean shadow, Date value) {
        super();
        autoValidate = false;
        propertyEditor = new DateTimePropertyEditor(datePattern);
        messages = new DateFieldMessages();
        setTriggerStyle("x-form-date-trigger");
        this.displayTime = displayTime;

        if (value != null) {
            // dateField.setValue(value);
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
     * Returns the field's date picker.
     *
     * @return the date picker
     */
    public DatePicker getDatePicker() {
        if (menu == null) {
            menu = displayTime ? new CalendarMenu() : new DateMenu();
            getMenuDatePicker().addListener(Events.Select, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent ce) {
                    focusValue = getValue();
                    Date date = menu instanceof DateMenu ? ((DateMenu) menu).getDate() : ((CalendarMenu) menu).getDate();
                    if (displayTime) {
                        date.setHours(((CalendarPicker) getMenuDatePicker()).getSelectedHour());
                        date.setMinutes(((CalendarPicker) getMenuDatePicker()).getSelectedMinute());
                    }
                    setValue(date);
                    menu.hide();
                }
            });
            menu.addListener(Events.Hide, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent be) {
                    eventPreview.remove();
                    focus();
                }
            });
        }
        return getMenuDatePicker();
    }

    /**
     * Returns the field's max value.
     *
     * @return the max value
     */
    public Date getMaxValue() {
        return maxValue;
    }

    @Override
    public DateFieldMessages getMessages() {
        return (DateFieldMessages) messages;
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
     * Returns true if formatting is enabled.
     *
     * @return the format value state
     */
    public boolean isFormatValue() {
        return formatValue;
    }

    /**
     * True to format the user entered value using the field's property editor
     * after passing validation (defaults to false). Format value should not be
     * enabled when auto validating.
     *
     * @param formatValue true to format the user value
     */
    public void setFormatValue(boolean formatValue) {
        this.formatValue = formatValue;
    }

    /**
     * Sets the field's max value.
     *
     * @param maxValue the max value
     */
    public void setMaxValue(Date maxValue) {
        if (maxValue != null) {
            maxValue = new DateWrapper(maxValue).clearTime().asDate();
        }
        this.maxValue = maxValue;
    }

    /**
     * The maximum date allowed.
     *
     * @param minValue the max value
     */
    public void setMinValue(Date minValue) {
        if (minValue != null) {
            minValue = new DateWrapper(minValue).clearTime().asDate();
        }
        this.minValue = minValue;
    }

    @Override
    public void setRawValue(String value) {
        super.setRawValue(value);
    }

    protected void collapseIf(PreviewEvent pe) {
        /*if (!menu.el().isOrHasChild(pe.getTarget()) && !el().isOrHasChild(pe.getTarget())) {
            menu.hide();
        }*/
    }

    protected void expand() {
        DatePicker picker = getDatePicker();

        Object v = getValue();
        Date d = null;
        if (v instanceof Date) {
            d = (Date) v;
        } else {
            d = new Date();
        }
        picker.setValue(d, true);
        picker.setMinDate(minValue);
        picker.setMaxDate(maxValue);
        eventPreview.add();

        // handle case when down arrow is opening menu
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                menu.show(el().dom, "tl-bl?");
                getMenuDatePicker().focus();
            }
        });
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if (eventPreview != null) {
            eventPreview.remove();
        }
    }

    @Override
    protected void onKeyDown(FieldEvent fe) {
        super.onKeyDown(fe);
        if (fe.getKeyCode() == KeyCodes.KEY_DOWN) {
            fe.stopEvent();
            if (menu == null || !menu.isAttached()) {
                expand();
            }
        }
    }

    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);

        eventPreview = new BaseEventPreview() {
            @Override
            protected boolean onPreview(PreviewEvent pe) {
                switch (pe.getType().getEventCode()) {
                    case Event.ONSCROLL:
                    case Event.ONMOUSEWHEEL:
                    case Event.ONMOUSEDOWN:
                        collapseIf(pe);
                        break;
                }
                return true;
            }
        };
        eventPreview.setAutoHide(false);

        new KeyNav<FieldEvent>(this) {

            @Override
            public void onEsc(FieldEvent fe) {
                if (menu != null && menu.isAttached()) {
                    menu.hide();
                }
            }
        };

        if (GXT.isAriaEnabled()) {
            getInputEl().dom.setAttribute("title", getMessages().getAriaText());
        }
    }

    @Override
    protected void onTriggerClick(ComponentEvent ce) {
        super.onTriggerClick(ce);
        if (isReadOnly()) {
            return;
        }

        expand();
    }

    protected boolean validateBlur(DomEvent e, Element target) {
        return menu == null || !menu.isVisible();
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
                error = GXT.MESSAGES.dateField_invalidText(value, format.getPattern().toUpperCase());
            }
            markInvalid(error);
            return false;
        }

        if (minValue != null && date.before(minValue)) {
            String error = null;
            if (getMessages().getMinText() != null) {
                error = Format.substitute(getMessages().getMinText(), format.format(minValue));
            } else {
                error = GXT.MESSAGES.dateField_minText(format.format(minValue));
            }
            markInvalid(error);
            return false;
        }
        if (maxValue != null && date.after(maxValue)) {
            String error = null;
            if (getMessages().getMaxText() != null) {
                error = Format.substitute(getMessages().getMaxText(), format.format(maxValue));
            } else {
                error = GXT.MESSAGES.dateField_maxText(format.format(maxValue));
            }
            markInvalid(error);
            return false;
        }

        if (formatValue && getPropertyEditor().getFormat() != null) {
            setRawValue(getPropertyEditor().getFormat().format(date));
        }

        return true;
    }

    private DatePicker getMenuDatePicker() {
        return menu instanceof DateMenu ? ((DateMenu) menu).getDatePicker() : ((CalendarMenu) menu).getDatePicker();
    }
}
