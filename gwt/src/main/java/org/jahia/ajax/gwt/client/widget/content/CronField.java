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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cron expression picker control.
 * User: toto
 * Date: Dec 1, 2008
 * Time: 6:37:07 PM
 */
public class CronField extends AdapterField {
    public static final int NONE = 0;
    public static final int EVERY_MINUTE = 1;
    public static final int EVERY_HOUR = 2;
    public static final int EVERY_DAY = 3;
    public static final int EVERY_WEEK = 4;
    public static final int EVERY_MONTH = 5;
    public static final int EVERY_YEAR = 6;

    private HorizontalPanel panel = new HorizontalPanel();

    private SimpleComboBox<String> choose;
    private SimpleComboBox<String> minuteCombo;
    private SimpleComboBox<String> hourCombo;
    private SimpleComboBox<String> weekDayCombo;
    private SimpleComboBox<String> monthCombo;
    private SimpleComboBox<String> monthDayCombo;

    public CronField() {
        super(null);
        createUI();
        widget = panel;
    }

    private void createUI() {
        panel.setVerticalAlign(Style.VerticalAlignment.MIDDLE);
        panel.add(new Html(Messages.get("label.cron.every", "Every") + "&nbsp;"));

        choose = new SimpleComboBox<String>();
        choose.setEditable(false);
        choose.setTriggerAction(ComboBox.TriggerAction.ALL);
        choose.add("");
        choose.add(Messages.get("label.minute", "minute"));
        choose.add(Messages.get("label.hour", "hour"));
        choose.add(Messages.get("label.day", "day"));
        choose.add(Messages.get("label.week", "week"));
        choose.add(Messages.get("label.month", "month"));
        choose.add(Messages.get("label.year", "year"));

        minuteCombo = new SimpleComboBox<String>();
        minuteCombo.setEditable(false);
        minuteCombo.setTriggerAction(ComboBox.TriggerAction.ALL);
        minuteCombo.setWidth(50);
        for (int i = 0; i < 60; i += 1) {
            minuteCombo.add((i<10 ? "0":"") + i);
        }
        minuteCombo.setValue(minuteCombo.getStore().getAt(0));

        hourCombo = new SimpleComboBox<String>();
        hourCombo.setEditable(false);
        hourCombo.setTriggerAction(ComboBox.TriggerAction.ALL);
        hourCombo.setWidth(50);
        for (int i = 0; i < 24; i++) {
            hourCombo.add((i<10 ? "0":"") + i);
        }
        hourCombo.setValue(hourCombo.getStore().getAt(0));

        weekDayCombo = new SimpleComboBox<String>();
        weekDayCombo.setEditable(false);
        weekDayCombo.setTriggerAction(ComboBox.TriggerAction.ALL);
        weekDayCombo.add(Messages.get("label.day.monday", "Monday"));
        weekDayCombo.add(Messages.get("label.day.tuesday", "Tuesday"));
        weekDayCombo.add(Messages.get("label.day.wednesday", "Wednesday"));
        weekDayCombo.add(Messages.get("label.day.thursday", "Thursday"));
        weekDayCombo.add(Messages.get("label.day.friday", "Friday"));
        weekDayCombo.add(Messages.get("label.day.saturday", "Saturday"));
        weekDayCombo.add(Messages.get("label.day.sunday", "Sunday"));
        weekDayCombo.setValue(weekDayCombo.getStore().getAt(0));

        monthCombo = new SimpleComboBox<String>();
        monthCombo.setEditable(false);
        monthCombo.setTriggerAction(ComboBox.TriggerAction.ALL);
        monthCombo.add(Messages.get("label.month.january", "January"));
        monthCombo.add(Messages.get("label.month.february", "February"));
        monthCombo.add(Messages.get("label.month.march", "March"));
        monthCombo.add(Messages.get("label.month.april", "April"));
        monthCombo.add(Messages.get("label.month.may", "May"));
        monthCombo.add(Messages.get("label.month.june", "June"));
        monthCombo.add(Messages.get("label.month.july", "July"));
        monthCombo.add(Messages.get("label.month.august", "August"));
        monthCombo.add(Messages.get("label.month.september", "September"));
        monthCombo.add(Messages.get("label.month.october", "October"));
        monthCombo.add(Messages.get("label.month.november", "November"));
        monthCombo.add(Messages.get("label.month.december", "December"));
        monthCombo.setValue(monthCombo.getStore().getAt(0));

        monthDayCombo = new SimpleComboBox<String>();
        monthDayCombo.setEditable(false);
        monthDayCombo.setTriggerAction(ComboBox.TriggerAction.ALL);
        monthDayCombo.setWidth(50);
        for (int i = 1; i <= 31; i++) {
            monthDayCombo.add("" + i);
        }
        monthDayCombo.setValue(monthDayCombo.getStore().getAt(0));

        final Map<Integer, List<Widget>> widgets = new HashMap<Integer, List<Widget>>();

        widgets.put(-1, new ArrayList<Widget>());

        widgets.put(NONE, new ArrayList<Widget>());
        
        widgets.put(EVERY_MINUTE, new ArrayList<Widget>());

        widgets.put(EVERY_HOUR, new ArrayList<Widget>());
        String textEvery = "&nbsp;" + Messages.get("label.cron.at", "Every") + "&nbsp;";
		widgets.get(EVERY_HOUR).add(new Html(textEvery));
        widgets.get(EVERY_HOUR).add(minuteCombo);
        widgets.get(EVERY_HOUR).add(new Html("&nbsp;" + Messages.get("label.cron.minutesPastHour", "minutes past the hour")));

        widgets.put(EVERY_DAY, new ArrayList<Widget>());
        widgets.get(EVERY_DAY).add(new Html(textEvery));
        widgets.get(EVERY_DAY).add(hourCombo);
        widgets.get(EVERY_DAY).add(new Html("&nbsp;:&nbsp;"));
        widgets.get(EVERY_DAY).add(minuteCombo);

        widgets.put(EVERY_WEEK, new ArrayList<Widget>());
        widgets.get(EVERY_WEEK).add(new Html("&nbsp;" + Messages.get("label.cron.on") + "&nbsp;"));
        widgets.get(EVERY_WEEK).add(weekDayCombo);
        widgets.get(EVERY_WEEK).add(new Html(textEvery));
        widgets.get(EVERY_WEEK).add(hourCombo);
        widgets.get(EVERY_WEEK).add(new Html("&nbsp;:&nbsp;"));
        widgets.get(EVERY_WEEK).add(minuteCombo);

        widgets.put(EVERY_MONTH, new ArrayList<Widget>());
        widgets.get(EVERY_MONTH).add(new Html("&nbsp;" + Messages.get("label.cron.onThe") + "&nbsp;"));
        widgets.get(EVERY_MONTH).add(monthDayCombo);
        widgets.get(EVERY_MONTH).add(new Html(textEvery));
        widgets.get(EVERY_MONTH).add(hourCombo);
        widgets.get(EVERY_MONTH).add(new Html("&nbsp;:&nbsp;"));
        widgets.get(EVERY_MONTH).add(minuteCombo);

        widgets.put(EVERY_YEAR, new ArrayList<Widget>());
        widgets.get(EVERY_YEAR).add(new Html("&nbsp;" + Messages.get("label.cron.onThe") + "&nbsp;"));
        widgets.get(EVERY_YEAR).add(monthDayCombo);
        widgets.get(EVERY_YEAR).add(new Html("&nbsp;" + Messages.get("label.cron.of") + "&nbsp;"));
        widgets.get(EVERY_YEAR).add(monthCombo);
        widgets.get(EVERY_YEAR).add(new Html(textEvery));
        widgets.get(EVERY_YEAR).add(hourCombo);
        widgets.get(EVERY_YEAR).add(new Html("&nbsp;:&nbsp;"));
        widgets.get(EVERY_YEAR).add(minuteCombo);


        choose.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> se) {
                List<Widget> w = widgets.get(-1);
                for (Widget widget : w) {
                    panel.remove(widget);
                }
                w = widgets.get(choose.getStore().indexOf(se.getSelectedItem()));
                widgets.put(-1, w);
                for (Widget widget : w) {
                    panel.add(widget);
                }
                panel.layout();
            }
        });

        panel.add(choose);
    }

    @Override
    public void setValue(Object value) {
        String s = (String) value;
        if (s.length() > 0) {
            String[] values = s.split(" ");
            if ("*".equals(values[1])) {
                // Minute
                choose.setValue(choose.getStore().getAt(EVERY_MINUTE));
            } else if ("*".equals(values[2])) {
                // Hour
                minuteCombo.setValue(minuteCombo.getStore().getAt(Integer.parseInt(values[1])));
                choose.setValue(choose.getStore().getAt(EVERY_HOUR));
            } else if (!"?".equals(values[5])) {
                // Week
                hourCombo.setValue(hourCombo.getStore().getAt(Integer.parseInt(values[2])));
                minuteCombo.setValue(minuteCombo.getStore().getAt(Integer.parseInt(values[1])));
                weekDayCombo.setValue(weekDayCombo.getStore().getAt((Integer.parseInt(values[5]) - 2) % 7));
                choose.setValue(choose.getStore().getAt(EVERY_WEEK));
            } else if ("*".equals(values[3])) {
                // Day
                hourCombo.setValue(hourCombo.getStore().getAt(Integer.parseInt(values[2])));
                minuteCombo.setValue(minuteCombo.getStore().getAt(Integer.parseInt(values[1])));
                choose.setValue(choose.getStore().getAt(EVERY_DAY));
            } else if ("*".equals(values[4])) {
                // Month
                hourCombo.setValue(hourCombo.getStore().getAt(Integer.parseInt(values[2])));
                minuteCombo.setValue(minuteCombo.getStore().getAt(Integer.parseInt(values[1])));
                monthDayCombo.setValue(monthDayCombo.getStore().getAt(Integer.parseInt(values[3]) - 1));
                choose.setValue(choose.getStore().getAt(EVERY_MONTH));
            } else {
                // Year
                hourCombo.setValue(hourCombo.getStore().getAt(Integer.parseInt(values[2])));
                minuteCombo.setValue(minuteCombo.getStore().getAt(Integer.parseInt(values[1])));
                monthDayCombo.setValue(monthDayCombo.getStore().getAt(Integer.parseInt(values[3]) - 1));
                monthCombo.setValue(monthCombo.getStore().getAt(Integer.parseInt(values[4]) - 1));
                choose.setValue(choose.getStore().getAt(EVERY_YEAR));
            }
        } else {
        	choose.setValue(choose.getStore().getAt(NONE));
        }
        super.setValue(value);
    }

    @Override
    public String getValue() {
        switch (choose.getSelectedIndex()) {
	        case NONE: {
	            return "";
	        }
            case EVERY_MINUTE: { // Minute
                return "0 * * * * ?";
            }
            case EVERY_HOUR: { // Hour
                return "0 " + minuteCombo.getValue().getValue() + " * * * ?";
            }
            case EVERY_DAY: { // Day
                return "0 " + minuteCombo.getValue().getValue() + " " + hourCombo.getValue().getValue() + " * * ?";
            }
            case EVERY_WEEK: { // Week
                return "0 " + minuteCombo.getValue().getValue() + " " + hourCombo.getValue().getValue() + " ? * " + (weekDayCombo.getSelectedIndex() + 2) % 7;
            }
            case EVERY_MONTH: { // Month
                return "0 " + minuteCombo.getValue().getValue() + " " + hourCombo.getValue().getValue() + " " + (monthDayCombo.getSelectedIndex() + 1) + " * ?";
            }
            case EVERY_YEAR: { // Year
                return "0 " + minuteCombo.getValue().getValue() + " " + hourCombo.getValue().getValue() + " " + (monthDayCombo.getSelectedIndex() + 1) + " " + (monthCombo.getSelectedIndex() + 1) + " ?";
            }
        }
        return "";
    }
}
