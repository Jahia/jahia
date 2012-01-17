/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.calendar;

import java.util.Date;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.DateWrapper;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.DatePicker;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.google.gwt.dom.client.Node;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;

/**
 * @author Khue Nguyen
 */
public class CalendarPicker extends DatePicker {

    private HorizontalPanel hoursPanel;

    /**
     * The date time format used to format each entry (defaults to
     * {@link com.google.gwt.i18n.client.DateTimeFormat#getShortDateFormat()}.
     */
    private DateTimeFormat format = DateTimeFormat.getShortTimeFormat();
    private HourModel hour;
    private MinuteModel minute;
    private ComboBox hours;
    private ComboBox minutes;

    public CalendarPicker(Date date) {
        super();
        if (date==null){
            date = new Date();
        }
        this.setValue(date,true);
        DateWrapper dateWrapper = new DateWrapper(this.getValue());
        hour = new HourModel(dateWrapper.getHours());
        minute = new MinuteModel(dateWrapper.getMinutes());
    }

    public CalendarPicker() {
        this(null);
    }

    public DateTimeFormat getFormat() {
        return format;
    }

    public void setFormat(DateTimeFormat format) {
        this.format = format;
    }

    public HourModel getHour() {
        return hour;
    }

    public int getSelectedHour() {
        HourModel hourModel = this.hour;
        if (!this.hours.getSelection().isEmpty()){
            hourModel = (HourModel)this.hours.getSelection().get(0);
        }
        return Integer.parseInt(hourModel.getValue());
    }

    public ComboBox getHours() {
        return hours;
    }

    public void setHour(int hour) {
        this.hour = new HourModel(hour);
        if (hours != null) {
            hours.setValue(this.hour);
        }
    }

    public MinuteModel getMinute() {
        return minute;
    }

    public int getSelectedMinute() {
        MinuteModel minuteModel = this.minute;
        if (!this.minutes.getSelection().isEmpty()){
            minuteModel = (MinuteModel)this.minutes.getSelection().get(0);
        }
        return Integer.parseInt(minuteModel.getValue());
    }

    public ComboBox getMinutes() {
        return minutes;
    }

    public void setMinute(int minute) {
        this.minute = new MinuteModel(minute);
        if (minutes != null) {
            minutes.setValue(this.minute);
        }
    }

    /**
     * Sets the value of the date field.
     *
     * @param date the date
     * @param supressEvent true to spress the select event
     */
    public void setValue(Date date, boolean supressEvent) {
      super.setValue(date,supressEvent);
      if (date != null){
          this.setMinute(date.getMinutes());
          this.setHour(date.getHours());
      }
    }

    protected void initHours(){

        Date date = getValue();
        DateWrapper dateWrapper = null;
        if (date!=null){
            dateWrapper = new DateWrapper();
        } else {
            dateWrapper = new DateWrapper(date);
        }

        hoursPanel = new HorizontalPanel();
        hoursPanel.setTableWidth("100%");
        hoursPanel.setHorizontalAlign(HorizontalAlignment.CENTER);
        hoursPanel.setStyleName("x-date-hours-panel");
        hoursPanel.sinkEvents(Event.ONCHANGE);
        if (GXT.isIE) {
            hoursPanel.setWidth(175);
        }
        
        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.setLayout(new FitLayout());
        hPanel.setHorizontalAlign(Style.HorizontalAlignment.LEFT);
        hPanel.setStyleName("x-date-bottom");

        hours = new ComboBox() {
          @Override
            protected void onRender(Element parent, int index) {
                super.onRender(parent, index);
                getListView().addStyleName("x-datetime-selector");
            }  
        };
        hours.setDisplayField("display");
        hours.setMinListWidth(40);
        hours.setWidth(40);
        hours.setStore(getHours(0,23));
        hours.setValue(hour != null ? hour : new HourModel(dateWrapper.getHours()));
        hours.setForceSelection(true);
        hours.setTriggerAction(TriggerAction.ALL);
        hours.addSelectionChangedListener(new SelectionChangedListener<HourModel>() {
            public void selectionChanged(SelectionChangedEvent se) {
                HourModel hourModel = (HourModel) se.getSelection().get(0);
                if (hourModel!=null){
                    hour = new HourModel(Integer.parseInt(hourModel.getValue()));
                }
            }
        });

        hours.addListener(Events.Change, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent be) {
                HourModel hourModel = (HourModel) be.getValue();
                if (hourModel!=null){
                    hour = new HourModel(Integer.parseInt(hourModel.getValue()));
                }
            }
        });
        hPanel.add(hours);

        HTML sep = new HTML(":");
        sep.setStyleName("x-date-hours-separator");
        hPanel.add(sep, new TableData(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE));

        minutes = new ComboBox() {
          @Override
            protected void onRender(Element parent, int index) {
                super.onRender(parent, index);
                getListView().addStyleName("x-datetime-selector");
            }  
        };
        minutes.setDisplayField("display");
        minutes.setMinListWidth(40);
        minutes.setWidth(40);
        minutes.setStore(getMinutes(0,59));
        minutes.setValue(minute != null ? minute : new MinuteModel(dateWrapper.getMinutes()));
        minutes.setForceSelection(true);
        minutes.setTriggerAction(TriggerAction.ALL);
        minutes.getListView().addStyleName("x-datetime-selector");

        minutes.addSelectionChangedListener(new SelectionChangedListener<MinuteModel>() {
            public void selectionChanged(SelectionChangedEvent se) {
                MinuteModel minuteModel = (MinuteModel) se.getSelection().get(0);
                if (minuteModel!=null){
                    minute = new MinuteModel(Integer.parseInt(minuteModel.getValue()));
                }
            }
        });
        minutes.addListener(Events.Change, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent be) {
                MinuteModel minuteModel = (MinuteModel) be.getValue();
                if (minuteModel!=null){
                    minute = new MinuteModel(Integer.parseInt(minuteModel.getValue()));
                }
            }
        });
        
        hPanel.add(minutes);
        
        hoursPanel.add(hPanel, new TableData(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE));
    }

    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
        initHours();
        Node lastChild = DOM.getChild(getElement(), 3);
        getElement().insertBefore(hoursPanel.getElement(),lastChild);
        el().addEventsSunk(Event.ONCLICK | Event.MOUSEEVENTS);
    }

    @Override
    protected void doAttachChildren() {
      super.doAttachChildren();
      ComponentHelper.doAttach(hoursPanel);
    }

    @Override
    protected void doDetachChildren() {
      super.doDetachChildren();
      ComponentHelper.doDetach(hoursPanel);
    }

    private class HourModel extends BaseModel {

        public HourModel() {
            super();
        }

        public HourModel(int value){
            super();
            set("value",String.valueOf(value));
            if (value<10){
                set("display","0" + value);
            } else {
                set("display",value);
            }
        }

        public String getValue() {
            return get("value");
        }

        public String toString() {
          return getValue();
        }
    }

    private class MinuteModel extends BaseModel {

        public MinuteModel() {
            super();
        }

        public MinuteModel(int value){
            super();
            set("value",String.valueOf(value));
            if (value<10){
                set("display","0" + value);
            } else {
                set("display",value);
            }
        }

        public String getValue() {
            return get("value");
        }

        public String toString() {
          return getValue();
        }

    }

    private ListStore<HourModel> getHours(int startHour, int endHour){
        ListStore<HourModel> hours = new ListStore<HourModel>();
        for (int i=startHour; i<=endHour; i++){
            hours.add(new HourModel(i));
        }
        return hours;
    }

    private ListStore<MinuteModel> getMinutes(int startMinute, int endMinute){
        ListStore<MinuteModel> minutes = new ListStore<MinuteModel>();
        for (int i=startMinute; i<=endMinute; i++){
            minutes.add(new MinuteModel(i));
        }
        return minutes;
    }

    @Override
    protected void onClick(ComponentEvent be) {
        super.onClick(be);
    }

    @Override
    protected void onHide() {
        super.onHide();
    }
    
}
