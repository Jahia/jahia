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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget;

import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.ComponentPlugin;
import org.jahia.ajax.gwt.client.data.GWTJahiaProcessJobAction;
import org.jahia.ajax.gwt.client.data.GWTJahiaNodeOperationResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaNodeOperationResultItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * User: toto
 * Date: Nov 7, 2008 - 8:31:41 PM
 */
public class ReportGrid extends Grid<ReportGrid.GWTReportElement> {

    public ReportGrid(final Map<String, List<GWTJahiaProcessJobAction>> actions, final Map<String, String> titleForObjectKey){
        this(actions, titleForObjectKey, false, null);
    }

    public ReportGrid(final Map<String, List<GWTJahiaProcessJobAction>> actions, final Map<String, String> titleForObjectKey, boolean enableExpander, final Map<String, Map<String, GWTJahiaNodeOperationResult>> errorsAndWarnings) {
        super(buildStore(actions, titleForObjectKey, enableExpander, errorsAndWarnings), buildColumnModel(enableExpander, errorsAndWarnings));

        GroupingView view = new GroupingView();
        view.setForceFit(true);
        view.setGroupRenderer(new GridGroupRenderer() {
            public String render(GroupColumnData gcData) {
                String f = cm.getColumnById(gcData.field).getHeader();
                String l = gcData.models.size() == 1 ? "Action" : "Actions";
                return f + ": " + gcData.group + " (" + gcData.models.size() + " " + l + ")";
            }
        });
        setView(view);
        setBorders(true);

        if (enableExpander) {
            addPlugin((ComponentPlugin) cm.getColumn(0));
        }
    }

    public ListStore<GWTReportElement> getStore() {
        return super.getStore();
    }

    public static GroupingStore<GWTReportElement> buildStore(final Map<String, List<GWTJahiaProcessJobAction>> actions, final Map<String, String> titleForObjectKey, boolean enableExpander, final Map<String, Map<String, GWTJahiaNodeOperationResult>> errorsAndWarnings) {
        GroupingStore<GWTReportElement> store = new GroupingStore<GWTReportElement>();

        if (enableExpander && errorsAndWarnings != null) {
            for (String action : actions.keySet()) {
                for (GWTJahiaProcessJobAction gwtaction : actions.get(action)) {
                    Map<String, GWTJahiaNodeOperationResult> errorsAndWarningsForThisKey = errorsAndWarnings.get(gwtaction.getKey());
                    for (String lang : gwtaction.getLangs()) {
                        String errs = null;
                        if (errorsAndWarningsForThisKey != null && errorsAndWarningsForThisKey.containsKey(lang)) {
                            StringBuilder sbWars = new StringBuilder("<ul class=\"batchWarnings\">");
                            StringBuilder sbErrs = new StringBuilder("<ul class=\"batchErrors\">");
                            for (GWTJahiaNodeOperationResultItem err : errorsAndWarningsForThisKey.get(lang).getErrorsAndWarnings()) {
                                if (err.getLevel().intValue() == GWTJahiaNodeOperationResultItem.ERROR) {
                                    sbErrs.append("\n<li>").append(err.getMessage()).append("</li>");
                                } else if (err.getLevel().intValue() == GWTJahiaNodeOperationResultItem.WARNING) {
                                    sbWars.append("\n<li>").append(err.getMessage()).append("</li>");
                                }
                            }
                            sbWars.append("\n</ul>");
                            sbErrs.append("\n</ul>");
                            errs = new StringBuilder(sbErrs.toString()).append("\n").append(sbWars.toString()).toString();
                        }
                        store.add(new GWTReportElement(gwtaction.getKey(), titleForObjectKey.get(gwtaction.getKey()), action, lang, errs));
                    }
                }
            }
        } else {
            for (String action : actions.keySet()) {
                for (GWTJahiaProcessJobAction gwtaction : actions.get(action)) {
                    for (String lang : gwtaction.getLangs()) {
                        store.add(new GWTReportElement(gwtaction.getKey(), titleForObjectKey.get(gwtaction.getKey()), action, lang));
                    }
                }
            }
        }
        store.groupBy("action");
        return store;
    }

    public static ColumnModel buildColumnModel(boolean enableExpander, final Map<String, Map<String, GWTJahiaNodeOperationResult>> errorsAndWarnings) {
        XTemplate tpl = XTemplate.create("<div class=\"batchErrorsAndWarnings\">{errors}</div>");

        RowExpander expander = new RowExpander();
        expander.setRenderer(new GridCellRenderer<ReportGrid.GWTReportElement>() {
            public String render(ReportGrid.GWTReportElement model, String property, ColumnData d, int rowIndex, int colIndex, ListStore store) {
                if (errorsAndWarnings != null) {
                    Map<String, GWTJahiaNodeOperationResult> stringSetMap = errorsAndWarnings.get(model.getKey());
                    if (stringSetMap != null && !stringSetMap.isEmpty()) {
                        GWTJahiaNodeOperationResult operationResult = stringSetMap.get(model.getLanguage());
                        if (operationResult != null && operationResult.hasErrorsOrWarning()) {
                            return "<div class='x-grid3-row-expander'>&#160;</div>";
                        }
                    }
                }
                return "";
            }
        });
        expander.setTemplate(tpl);

        List<ColumnConfig> config = new ArrayList<ColumnConfig>();
        if (enableExpander) {
            config.add(expander);
        }
        ColumnConfig title = new ColumnConfig("title", "Title", 200) ;
        title.setRenderer(new GridCellRenderer<ReportGrid.GWTReportElement>() {
            public String render(ReportGrid.GWTReportElement model, String property, ColumnData d, int rowIndex, int colIndex, ListStore store) {
                if (errorsAndWarnings != null) {
                    Map<String, GWTJahiaNodeOperationResult> stringSetMap = errorsAndWarnings.get(model.getKey());
                    if (stringSetMap != null && !stringSetMap.isEmpty()) {
                        GWTJahiaNodeOperationResult operationResult = stringSetMap.get(model.getLanguage());
                        if (operationResult != null && operationResult.hasErrorsOrWarning()) {
                            return "<span class=\"hasErrorsOrWarnings\">" + model.getTitle() + "</span>" ;
                        }
                    }
                }
                return model.getTitle() ;
            }
        });
        config.add(title);
        config.add(new ColumnConfig("action", "Action", 100));
        ColumnConfig langCol = new ColumnConfig("language", "Language", 70);
        langCol.setFixed(true);
        config.add(langCol);
        config.add(new ColumnConfig("key", "Key", 120));
        final ColumnModel cm = new ColumnModel(config);

        return cm;
    }


    public static class GWTReportElement extends BaseModelData {

        public GWTReportElement() {
        }

        public GWTReportElement(String key, String title, String action, String language) {
            this(key, title, action, language, "");
        }

        public GWTReportElement(String key, String title, String action, String language, String errors) {
            setKey(key);
            setTitle(title);
            setAction(action);
            setLanguage(language);
            setErrors(errors);
        }

        public void setKey(String key) {
            set("key", key);
        }

        public void setTitle(String title) {
            set("title", title);
        }

        public void setAction(String action) {
            set("action", action);
        }

        public void setLanguage(String language) {
            set("language", language);
        }

        public void setErrors(String errors) {
            set("errors", errors);
        }

        public String getKey() {
            return get("key");
        }

        public String getTitle() {
            return get("title");
        }

        public String getAction() {
            return get("action");
        }

        public String getLanguage() {
            return get("language");
        }

        public String getErrors() {
            return get("errors");
        }

    }


}
