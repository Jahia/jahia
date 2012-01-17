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

package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Text;
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
        panel.add(new Text(Messages.get("label.cron.every", "Every") + "&nbsp;"));

        choose = new SimpleComboBox<String>();
        choose.setEditable(false);
        choose.setTriggerAction(ComboBox.TriggerAction.ALL);
        choose.add(Messages.get("empty", ""));
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
		widgets.get(EVERY_HOUR).add(new Text(textEvery));
        widgets.get(EVERY_HOUR).add(minuteCombo);
        widgets.get(EVERY_HOUR).add(new Text("&nbsp;" + Messages.get("label.cron.minutesPastHour", "minutes past the hour")));

        widgets.put(EVERY_DAY, new ArrayList<Widget>());
        widgets.get(EVERY_DAY).add(new Text(textEvery));
        widgets.get(EVERY_DAY).add(hourCombo);
        widgets.get(EVERY_DAY).add(new Text("&nbsp;:&nbsp;"));
        widgets.get(EVERY_DAY).add(minuteCombo);

        widgets.put(EVERY_WEEK, new ArrayList<Widget>());
        widgets.get(EVERY_WEEK).add(new Text("&nbsp;" + Messages.get("label.cron.on") + "&nbsp;"));
        widgets.get(EVERY_WEEK).add(weekDayCombo);
        widgets.get(EVERY_WEEK).add(new Text(textEvery));
        widgets.get(EVERY_WEEK).add(hourCombo);
        widgets.get(EVERY_WEEK).add(new Text("&nbsp;:&nbsp;"));
        widgets.get(EVERY_WEEK).add(minuteCombo);

        widgets.put(EVERY_MONTH, new ArrayList<Widget>());
        widgets.get(EVERY_MONTH).add(new Text("&nbsp;" + Messages.get("label.cron.onThe") + "&nbsp;"));
        widgets.get(EVERY_MONTH).add(monthDayCombo);
        widgets.get(EVERY_MONTH).add(new Text(textEvery));
        widgets.get(EVERY_MONTH).add(hourCombo);
        widgets.get(EVERY_MONTH).add(new Text("&nbsp;:&nbsp;"));
        widgets.get(EVERY_MONTH).add(minuteCombo);

        widgets.put(EVERY_YEAR, new ArrayList<Widget>());
        widgets.get(EVERY_YEAR).add(new Text("&nbsp;" + Messages.get("label.cron.onThe") + "&nbsp;"));
        widgets.get(EVERY_YEAR).add(monthDayCombo);
        widgets.get(EVERY_YEAR).add(new Text("&nbsp;" + Messages.get("label.cron.of") + "&nbsp;"));
        widgets.get(EVERY_YEAR).add(monthCombo);
        widgets.get(EVERY_YEAR).add(new Text(textEvery));
        widgets.get(EVERY_YEAR).add(hourCombo);
        widgets.get(EVERY_YEAR).add(new Text("&nbsp;:&nbsp;"));
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