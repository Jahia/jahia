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
package org.jahia.ajax.gwt.client.widget.versioning;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.DatePicker;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Event;
import org.jahia.ajax.gwt.client.widget.menu.VersionMenu;
import org.jahia.ajax.gwt.client.widget.menu.CalendarMenu;
import org.jahia.ajax.gwt.client.widget.menu.VersionMenuItem;
import org.jahia.ajax.gwt.client.widget.calendar.CalendarPicker;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;
import org.jahia.ajax.gwt.client.widget.versioning.PageVersionMenuItem;
import org.jahia.ajax.gwt.client.data.GWTJahiaVersion;
import org.jahia.ajax.gwt.client.widget.versioning.PageVersionsBrowser;

import java.util.Date;

/**
 * Provides a calendar input field with a {@link com.extjs.gxt.ui.client.widget.DatePicker} dropdown and automatic
 * date validation.
 *
 * User: hollis
 * Date: 17 juil. 2008
 * Time: 14:34:36
 * To change this template use File | Settings | File Templates.
 */
public class VersionField extends TriggerField<Date> implements VersionsBrowserListener {

    private int startingPageID;
    private Date minValue;
    private Date maxValue;
    private VersionMenu versionMenu;
    private VersionsBrowser versionsBrowser;
    private CalendarMenu calendarMenu;
    private DateField dateField;
    protected El versionTrigger;
    protected EventListener versionTriggerListener;
    protected Date selectedDate;
    protected GWTJahiaVersion selectedVersion;
    protected boolean useVersion;

    /**
     *
     * @param startingPageID
     * @param date
     */
    public VersionField(int startingPageID, Date date, GWTJahiaVersion version, boolean useVersion) {
        super();
        this.startingPageID = startingPageID;
        autoValidate = false;
        propertyEditor = new DateTimePropertyEditor(CalendarField.DEFAULT_DATE_FORMAT);
        dateField = new DateField();
        initFieldValue(date,version, useVersion);
        messages = dateField.getMessages();
        setTriggerStyle("x-form-date-trigger");
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
    protected void onBlur(ComponentEvent ce) {
        String v = getRawValue();
        try {
            boolean useVersionState = this.useVersion;
            setValue(getPropertyEditor().convertStringValue(v));
            this.useVersion = useVersionState;
        } catch (Exception e) {

        }
        super.onBlur(ce);
    }

    @Override
    protected void onTriggerClick(ComponentEvent ce) {
        super.onTriggerClick(ce);
        if (disabled || isReadOnly()) {
            return;
        }
        if (calendarMenu == null) {
            calendarMenu = new CalendarMenu(this.getSelectedDate());
            calendarMenu.addListener(Events.Select, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent ce) {
                    focusValue = getValue();
                    Date date = calendarMenu.getDate();
                    date.setHours(((CalendarPicker) calendarMenu.getDatePicker()).getSelectedHour());
                    date.setMinutes(((CalendarPicker) calendarMenu.getDatePicker()).getSelectedMinute());
                    setValue(date);
                    setUseVersion(false);
                    fireChangeEvent(focusValue, getValue());
                }
            });
        }
        DatePicker picker = calendarMenu.getDatePicker();

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

        if ("versionTrigger".equals(ce.source)){
            if (versionMenu == null){
                versionsBrowser = new PageVersionsBrowser(startingPageID);
                versionsBrowser.addPageVersionsBrowserListener(this);
                VersionMenuItem menuItem = new PageVersionMenuItem((Component)versionsBrowser);
                versionMenu = new VersionMenu(menuItem);
            }
            versionMenu.show();
            calendarMenu.hide();
        } else {
            calendarMenu.show(wrap.dom, "tl-bl?");
            if (versionMenu != null){
                versionMenu.hide();
            }
        }

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
            if (dateField.getMessages().getMinText() != null) {
                error = Format.substitute(dateField.getMessages().getMinText(), format.format(minValue));
            } else {
                error = GXT.MESSAGES.dateField_minText(format.format(minValue));
            }
            markInvalid(error);
            return false;
        }
        if (maxValue != null && date.after(maxValue)) {
            String error = null;
            if (dateField.getMessages() != null) {
                error = Format.substitute(dateField.getMessages().getMaxText(), format.format(maxValue));
            } else {
                error = GXT.MESSAGES.dateField_minText(format.format(maxValue));
            }
            markInvalid(error);
            return false;
        }

        return true;
    }

    protected void setElement(com.google.gwt.user.client.Element element, 
                              com.google.gwt.user.client.Element element1, int i){
        wrap.addStyleName("x-form-versionfield-wrap");
        versionTrigger = new El(DOM.createImg());
        versionTrigger.dom.setClassName("x-form-trigger x-form-version-trigger " + this.getTriggerStyle());
        versionTrigger.dom.setPropertyString("src", GXT.BLANK_IMAGE_URL);
        //@todo to restore when moved to last gxt version
        //if (this.isHideTrigger()) {
        //    versionTrigger.setVisible(false);
        //}
        element.appendChild(versionTrigger.dom);
        super.setElement(element, element1, i);
    }

    protected void onRender(Element target, int index) {
        versionTriggerListener = new EventListener() {
          public void onBrowserEvent(Event event) {
            FieldEvent ce = new FieldEvent(VersionField.this);
            ce.event = event;
            ce.type = DOM.eventGetType(event);
            ce.source = "versionTrigger";
            ce.stopEvent();
            onTriggerEvent(ce);
          }
        };
        super.onRender(target, index);
        DOM.sinkEvents(versionTrigger.dom, Event.ONCLICK | Event.MOUSEEVENTS);
    }

    protected void onResize(int width, int height) {
        if (width != Style.DEFAULT) {
            int tw = trigger.getWidth();
            if (tw == 0) { // need to look into why 0 is returned
                tw = 2*17;
            }
            getInputEl().setWidth(this.adjustWidth("input", width - tw));
            wrap.setWidth(getInputEl().getWidth() + trigger.getWidth() + versionTrigger.getWidth(), true);
        }
    }

    @Override
    protected void doAttachChildren() {
      super.doAttachChildren();
      DOM.setEventListener(versionTrigger.dom, versionTriggerListener);
    }

    @Override
    protected void doDetachChildren() {
      super.doDetachChildren();
      DOM.setEventListener(versionTrigger.dom, null);
    }

    protected void onTriggerEvent(ComponentEvent ce) {
        if ( "versionTrigger".equals(ce.source)){
            int type = ce.type;
            switch (type) {
            case Event.ONMOUSEOVER:
              versionTrigger.addStyleName("x-form-trigger-over");
              break;
            case Event.ONMOUSEOUT:
              versionTrigger.removeStyleName("x-form-trigger-over");
              break;
            case Event.ONCLICK:
              onTriggerClick(ce);
              break;
            }
        } else {
            super.onTriggerEvent(ce);
        }
    }

    public void onSave(GWTJahiaVersion selectedVersion){
        focusValue = getValue();
        if (selectedVersion != null){
            this.setSelectedVersion(selectedVersion);
            fireChangeEvent(focusValue, getValue());
        }
    }

    public void initFieldValue(Date date, GWTJahiaVersion version, boolean useVersion){
        if (this.calendarMenu != null && date != null){
            this.calendarMenu.setDate(date);
        }
        this.selectedVersion = version;
        this.useVersion = useVersion;

        focusValue = getValue();
        if (useVersion && version != null && version.getDate()>0){
            setSelectedVersion(version);
        } else if (date != null){
            setValue(date);
        }
        fireChangeEvent(focusValue, getValue());
    }

    public void setValue(Date date){
        this.selectedDate = date;
        //this.useVersion = false;
        super.setValue(date);
    }

    public Date getSelectedDate(){
        return this.selectedDate;
    }

    public void setSelectedVersion(GWTJahiaVersion version){
        this.selectedVersion = version;
        if (version == null || version.getDate() == 0){
            super.reset();
        } else {
            this.selectedDate = new Date(version.getDate());
            super.setValue(new Date(version.getDate()));
            this.useVersion = true;
        }
    }

    public GWTJahiaVersion getSelectedVersion(){
        return this.selectedVersion;
    }

    public boolean isUseVersion() {
        return useVersion;
    }

    public void setUseVersion(boolean useVersion) {
        this.useVersion = useVersion;
    }

    public void resetSetting(){
        this.reset();
        this.selectedVersion = null;
        this.useVersion = false;
    }

}