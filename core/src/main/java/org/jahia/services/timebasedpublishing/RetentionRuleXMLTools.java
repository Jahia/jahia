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

package org.jahia.services.timebasedpublishing;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.jahia.services.search.savedsearch.JahiaSavedSearch;
import org.jahia.exceptions.JahiaException;

import java.io.StringWriter;
import java.io.StringReader;
import java.util.*;

/**
 * XML tools used with RetentionRule
 *
 * User: hollis
 * Date: 3 juil. 2007
 * Time: 09:48:56
 */
public class RetentionRuleXMLTools {

    public static final String SETTINGS = "settings";
    public static final String INHERIT_FROM_PARENT = "inherit-from-parent";
    public static final String RULE_TYPE_EL = "rule-type";

    public static final String DAILY_EL = "daily";
    public static final String XDAYSINWEEKLY_EL = "xdays-in-week";
    public static final String DAYS_EL = "days";
    public static final String DAY_EL = "day";
    public static final String DAY_NAME_ATTR = "name";
    public static final String RECURRENCE_START_DATE_EL = "recurrence-start-date";
    public static final String RECURRENCE_END_DATE_EL = "recurrence-end-date";
    public static final String FROM_HOURS_EL = "from-hours";
    public static final String FROM_MINUTES_EL = "from-minutes";
    public static final String TO_HOURS_EL = "to-hours";
    public static final String TO_MINUTES_EL = "to-minutes";

    public static final String START_DATE_EL = "start-date";
    public static final String END_DATE_EL = "end-date";
    public static final String SELECTED_ATTR = "selected";

    /**
     * Returns an XML representation of retention rule settings
     *
     * @param rule
     * @return
     * @throws JahiaException
     */
    public static String getRuleSettings(BaseRetentionRule rule) throws JahiaException {
        String result = "";
        try {
            Document doc = DocumentHelper.createDocument();
            Element root = doc.addElement(JahiaSavedSearch.SEARCH_ELEMENT);

            root.addElement(INHERIT_FROM_PARENT).addText(String.valueOf(rule.getInherited()));

            root.addElement(RULE_TYPE_EL)
                    .addText(rule.getRuleType());

            // recurrence once
            root.addElement(START_DATE_EL).addText(rule.getStartDate().toString());
            root.addElement(END_DATE_EL).addText(rule.getEndDate().toString());

            // recurrence daily
            Element el = root.addElement(DAILY_EL);
            el.addElement(RECURRENCE_START_DATE_EL).addText(rule.getDailyStartDate().toString());
            el.addElement(RECURRENCE_END_DATE_EL).addText(rule.getDailyEndDate().toString());
            el.addElement(FROM_HOURS_EL).addText(String.valueOf(rule.getDailyFromHours()));
            el.addElement(FROM_MINUTES_EL).addText(String.valueOf(rule.getDailyFromMinutes()));
            el.addElement(TO_HOURS_EL).addText(String.valueOf(rule.getDailyToHours()));
            el.addElement(TO_MINUTES_EL).addText(String.valueOf(rule.getDailyToMinutes()));

            // recurrence weekly
            el = root.addElement(XDAYSINWEEKLY_EL);
            el.addElement(RECURRENCE_START_DATE_EL).addText(rule.getDaysInWeekStartDate().toString());
            el.addElement(RECURRENCE_END_DATE_EL).addText(rule.getDaysInWeekEndDate().toString());
            Element daysEl = el.addElement(DAYS_EL);
            Iterator it = rule.getDaysInWeek().iterator();
            DayInWeekBean dayBean = null;
            Element dayEl = null;
            while ( it.hasNext() ){
                dayBean = (DayInWeekBean)it.next();
                dayEl = daysEl.addElement(DAY_EL);
                dayEl.addAttribute(SELECTED_ATTR,String.valueOf(dayBean.isSelected()));
                dayEl.addAttribute(DAY_NAME_ATTR,dayBean.getDay());
                dayEl.addElement(FROM_HOURS_EL).addText(String.valueOf(dayBean.getFromHours()));
                dayEl.addElement(FROM_MINUTES_EL).addText(String.valueOf(dayBean.getFromMinutes()));
                dayEl.addElement(TO_HOURS_EL).addText(String.valueOf(dayBean.getToHours()));
                dayEl.addElement(TO_MINUTES_EL).addText(String.valueOf(dayBean.getToMinutes()));
            }

            rule.appendExtendedSettings(root);

            StringWriter out = new StringWriter(1024);
            XMLWriter writer = new XMLWriter(OutputFormat.createPrettyPrint());
            writer.setWriter(out);
            writer.write(doc);
            result = out.toString();
        } catch ( Exception t ){
            throw new JahiaException("Error occured when creating the XML representation of retention rule settings",
                "Error occured when creating the XML representation of retention rule settings",
                JahiaException.APPLICATION_ERROR,JahiaException.ERROR_SEVERITY,t);
        }
        return result;
    }

    public static void loadRuleSettings(BaseRetentionRule rule, String settings) throws JahiaException {
        if ( settings == null || "".equals(settings.trim()) ){
            return;
        }

        try
        {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new StringReader(settings));
            Element root = document.getRootElement();
            if (root != null)
            {
                Element el = root.element(INHERIT_FROM_PARENT);
                if ( el != null ){
                    rule.setInherited(Boolean.valueOf("true".equals(el.getText())));
                }

                el = root.element(RULE_TYPE_EL);
                if ( el != null ){
                    rule.setRuleType(el.getText());
                }

                // recurrence once
                el = root.element(START_DATE_EL);
                if ( el != null ){
                    rule.setStartDate(new Long(el.getText()));
                }
                el = root.element(END_DATE_EL);
                if ( el != null ){
                    rule.setEndDate(new Long(el.getText()));
                }

                // recurrence daily
                el = root.element(DAILY_EL);
                if ( el != null ){
                    rule.setDailyStartDate(new Long(el.element(RECURRENCE_START_DATE_EL).getText()));
                    rule.setDailyEndDate(new Long(el.element(RECURRENCE_END_DATE_EL).getText()));
                    rule.setDailyFromHours(Integer.parseInt(el.element(FROM_HOURS_EL).getText()));
                    rule.setDailyFromMinutes(Integer.parseInt(el.element(FROM_MINUTES_EL).getText()));
                    rule.setDailyToHours(Integer.parseInt(el.element(TO_HOURS_EL).getText()));
                    rule.setDailyToMinutes(Integer.parseInt(el.element(TO_MINUTES_EL).getText()));
                }

                // recurrence weekly
                el = root.element(XDAYSINWEEKLY_EL);
                if ( el != null ){
                    rule.setDaysInWeekStartDate(new Long(el.element(RECURRENCE_START_DATE_EL).getText()));
                    rule.setDaysInWeekEndDate(new Long(el.element(RECURRENCE_END_DATE_EL).getText()));
                    Iterator it = el.element(DAYS_EL).elementIterator(DAY_EL);
                    List days = new ArrayList();
                    int index = 0;
                    DayInWeekBean dayBean = null;
                    while ( it.hasNext() ){
                        el = (Element)it.next();
                        dayBean = new DayInWeekBean(el.attribute(DAY_NAME_ATTR).getText(),index);
                        dayBean.setSelected("true".equals(el.attribute(SELECTED_ATTR).getText()));
                        dayBean.setFromHours(Integer.parseInt(el.element(FROM_HOURS_EL).getText()));
                        dayBean.setFromMinutes(Integer.parseInt(el.element(FROM_MINUTES_EL).getText()));
                        dayBean.setToHours(Integer.parseInt(el.element(TO_HOURS_EL).getText()));
                        dayBean.setToMinutes(Integer.parseInt(el.element(TO_MINUTES_EL).getText()));
                        days.add(dayBean);
                        index++;
                    }
                    rule.setDaysInWeek(days);
                }

                rule.loadExtendedSettings(root);
            }
        }
        catch (Exception t){
            throw new JahiaException("Error occured when parsing the XML representation of retention rule settings",
                "Error occured when creating the XML representation of retention rule settings",
                JahiaException.APPLICATION_ERROR,JahiaException.ERROR_SEVERITY,t);
        }
    }

}
