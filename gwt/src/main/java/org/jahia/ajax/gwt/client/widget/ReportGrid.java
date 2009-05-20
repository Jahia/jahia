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
        this(actions, titleForObjectKey, false, null, false);
    }

    public ReportGrid(final Map<String, List<GWTJahiaProcessJobAction>> actions, final Map<String, String> titleForObjectKey, boolean enableExpander, final Map<String, Map<String, GWTJahiaNodeOperationResult>> errorsAndWarnings, boolean enableWorkflowStates) {
        super(buildStore(actions, titleForObjectKey, enableExpander, errorsAndWarnings), buildColumnModel(enableExpander, errorsAndWarnings, enableWorkflowStates));

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
            if (errorsAndWarnings.containsKey(null) && errorsAndWarnings.get(null).containsKey(null)) {
                GWTJahiaNodeOperationResult res = errorsAndWarnings.get(null).get(null);
                for (GWTJahiaNodeOperationResultItem err : res.getErrorsAndWarnings()) {
                    store.add(new GWTReportElement("", err.getMessage(), "", "", "", ""));
                }
            }
            for (String action : actions.keySet()) {
                for (GWTJahiaProcessJobAction gwtaction : actions.get(action)) {
                    Map<String, GWTJahiaNodeOperationResult> errorsAndWarningsForThisKey = errorsAndWarnings.get(gwtaction.getKey());
                    for (String lang : gwtaction.getLangs()) {
                        String errs = null;
                        if (errorsAndWarningsForThisKey != null && errorsAndWarningsForThisKey.containsKey(lang)) {
                            List<String> warList = new ArrayList<String>();
                            List<String> errList = new ArrayList<String>();

                            for (GWTJahiaNodeOperationResultItem err : errorsAndWarningsForThisKey.get(lang).getErrorsAndWarnings()) {
                                String msg = err.getMessage();
                                if (err.getLevel().intValue() == GWTJahiaNodeOperationResultItem.ERROR) {
                                    if (!errList.contains(msg)) {
                                        errList.add(msg);
                                    }
                                } else if (err.getLevel().intValue() == GWTJahiaNodeOperationResultItem.WARNING) {
                                    if (!warList.contains(msg)) {
                                        warList.add(msg);
                                    }
                                }
                            }
                            StringBuilder sbWars = new StringBuilder("<ul class=\"batchWarnings\">");
                            StringBuilder sbErrs = new StringBuilder("<ul class=\"batchErrors\">");
                            for (String err: errList) {
                                sbErrs.append("\n<li>").append(err).append("</li>");
                            }
                            for (String war: warList) {
                                sbWars.append("\n<li>").append(war).append("</li>");
                            }
                            sbWars.append("\n</ul>");
                            sbErrs.append("\n</ul>");
                            errs = new StringBuilder(sbErrs.toString()).append("\n").append(sbWars.toString()).toString();
                        }
                        store.add(new GWTReportElement(gwtaction.getKey(), titleForObjectKey.get(gwtaction.getKey()), action, lang, errs, gwtaction.getWorkflowStateForLanguage().get(lang)));
                    }
                }
            }
        } else {
            for (String action : actions.keySet()) {
                for (GWTJahiaProcessJobAction gwtaction : actions.get(action)) {
                    for (String lang : gwtaction.getLangs()) {
                        store.add(new GWTReportElement(gwtaction.getKey(), titleForObjectKey.get(gwtaction.getKey()), action, lang, gwtaction.getWorkflowStateForLanguage().get(lang)));
                    }
                }
            }
        }
        store.groupBy("action");
        return store;
    }

    public static ColumnModel buildColumnModel(boolean enableExpander, final Map<String, Map<String, GWTJahiaNodeOperationResult>> errorsAndWarnings, boolean enableWorkflowStates) {
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

        if (enableWorkflowStates) {
            ColumnConfig wfCol = new ColumnConfig("workflow", "Workflow", 60);
            wfCol.setRenderer(new GridCellRenderer<ReportGrid.GWTReportElement>() {
                public String render(ReportGrid.GWTReportElement modelData, String s, ColumnData columnData, int i, int i1, ListStore listStore) {
                    String state = modelData.getWorkflowState() ;
                    if (state != null) {
                        return "<div class='workflow-" + modelData.getWorkflowState() + "'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>" ;
                    } else {
                        return "" ;
                    }
                }
            });
            config.add(wfCol) ;
        }

        config.add(new ColumnConfig("key", "Key", 120));
        final ColumnModel cm = new ColumnModel(config);

        return cm;
    }


    public static class GWTReportElement extends BaseModelData {

        public GWTReportElement() {
        }

        public GWTReportElement(String key, String title, String action, String language, String workflowStates) {
            this(key, title, action, language, "", workflowStates);
        }

        public GWTReportElement(String key, String title, String action, String language, String errors, String workflowState) {
            setKey(key);
            setTitle(title);
            setAction(action);
            setLanguage(language);
            setErrors(errors);
            setWorkflowState(workflowState) ;
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

        public void setWorkflowState(String wfState) {
            set("workflowState", wfState) ;
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

        public String getWorkflowState() {
            return get("workflowState") ;
        }

    }


}
