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
package org.jahia.ajax.gwt.client.widget.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.*;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.wcag.WCAGValidationResult;
import org.jahia.ajax.gwt.client.data.wcag.WCAGViolation;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.ckeditor.CKEditor;
import org.jahia.ajax.gwt.client.widget.ckeditor.CKEditorConfig;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.HideMode;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.RowNumberer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

/**
 * User: ktlili
 * Date: Nov 25, 2009
 * Time: 12:51:25 PM
 * <p/>
 * Code inspired from AdapterField. Update this class if GXT version is updated
 */
public class CKEditorField extends Field<String> {

    private static Map<String, CKEditorField> instances = new HashMap<String, CKEditorField>();

    /**
     * The wrapped widget.
     */
    protected CKEditor ckeditor;
    protected Html html;
    protected ContentPanel panel;

    private boolean resizeWidget;

    private WCAGValidationResult wcagValidationResult;
    private ContentPanel wcagPanel;
    private boolean ignoreWcagWarnings;
    private String lastValidatedContent;
    private boolean allowBlank = true;
    private String blankText = GXT.MESSAGES.textField_blankText();
    /**
     * Creates a new adapter field.
     */
    public CKEditorField() {
        this(null);
    }

    /**
     * Returns the field's allow blank state.
     *
     * @return true if blank values are allowed
     */
    public boolean getAllowBlank() {
        return allowBlank;
    }

    /**
     * Sets whether a field is valid when its value length = 0 (default to true).
     *
     * @param allowBlank true to allow blanks, false otherwise
     */
    public void setAllowBlank(boolean allowBlank) {
        this.allowBlank = allowBlank;
    }

    public CKEditorField(CKEditorConfig config) {
        super();
        ckeditor = new CKEditor(config, this);
        html = new Html();
        html.setStyleAttribute("overflow", "scroll");
    }

    public Component getComponent() {
        return readOnly ? html : ckeditor;
    }

    @Override
    public Element getElement() {
        // we need this because of lazy rendering
        return getComponent().getElement();
    }

    @Override
    public boolean isAttached() {
        if (getComponent() != null) {
            return getComponent().isAttached();
        }
        return false;
    }

    /**
     * Returns true if the wrapped widget is being resized.
     *
     * @return true is resizing is enabled
     */
    public boolean isResizeWidget() {
        return resizeWidget;
    }

    @Override
    public void onBrowserEvent(Event event) {
        // Fire any handler added to the CKEditorField itself.
        super.onBrowserEvent(event);

        // Delegate events to the widget.
        getComponent().onBrowserEvent(event);
    }

    /**
     * True to resize the wrapped widget when the field is resized (defaults to
     * false).
     *
     * @param resizeWidget true to resize the wrapped widget
     */
    public void setResizeWidget(boolean resizeWidget) {
        this.resizeWidget = resizeWidget;
    }

    @Override
    protected void onAttach() {
        ComponentHelper.doAttach(getComponent());
        DOM.setEventListener(getElement(), this);
        onLoad();
    }

    public void onBlur() {
        super.onBlur(null);
    }

    public void onFocus() {
        super.onFocus(null);
    }

    @Override
    protected void onDetach() {
        try {
            onUnload();
        } finally {
            ComponentHelper.doDetach(getComponent());
        }
        onDetachHelper();
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        getComponent().disable();
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        getComponent().enable();
    }

    @Override
    protected void onRender(Element target, int index) {
        final Component component = getComponent();
        ckeditor.setName(name);
        if (!component.isRendered()) {
            component.render(target, index);
        }
        setElement(component.getElement(), target, index);
        if (GXT.isAriaEnabled()) {
          if (!getAllowBlank()) {
            setAriaState("aria-required", "true");
          }
        }
    }


    public void afterCKEditorInstanceReady() {
        String instanceId = ckeditor.getInstanceId();
        if (instanceId != null) {
            instances.put(instanceId, this);
        } else {
            Log.warn("CKEditor instance ID is null."
                    + " Unable to store the reference to this instance of the CKEditorField");
        }
    }

    @Override
    public void clearInvalid() {
        getInputEl().getParent().removeStyleName(invalidStyle);
        super.clearInvalid();
    }

    @Override
    public void markInvalid(String msg) {
        getInputEl().getParent().addStyleName(invalidStyle);
        if (!"side".equals(getMessageTarget())) {
            super.markInvalid(msg);
        } else {
            if (errorIcon == null) {
                errorIcon = new WidgetComponent(getImages().getInvalid().createImage());
                Element p = el().getParent().getParent().dom;
                errorIcon.render(p, 0);
                errorIcon.setHideMode(HideMode.VISIBILITY);
                errorIcon.hide();
                errorIcon.setStyleAttribute("display", "block");
                errorIcon.setStyleAttribute("float", "right");
                errorIcon.getAriaSupport().setRole("alert");
                if (GXT.isAriaEnabled()) {
                    setAriaState("aria-describedby", errorIcon.getId());
                    errorIcon.setTitle(getErrorMessage());
                }

            } else if (!errorIcon.el().isConnected()) {
                Element p = el().getParent().getParent().dom;
                p.insertFirst(errorIcon.getElement());
            }
            if (!errorIcon.isAttached()) {
                ComponentHelper.doAttach(errorIcon);
            }

            // needed to prevent flickering
            DeferredCommand.addCommand(new Command() {

                @Override
                public void execute() {
                    if (errorIcon.isAttached()) {
                        errorIcon.show();
                    }
                }
            });
            errorIcon.setToolTip(msg);
            errorIcon.getToolTip().addStyleName("x-form-invalid-tip");
            el().repaint();
            setMessageTarget("none");
            try {
                super.markInvalid(msg);
            } finally {
                setMessageTarget("side");
            }
        }
    }

    @Override
    protected boolean validateValue(String value) {
        setData("optionalValidation", null);
        boolean isValid = super.validateValue(value);
        if (value !=null && (value.length() < 1 || value.equals(""))) {
          if (allowBlank) {
            clearInvalid();
            return true;
          } else {
            markInvalid(blankText);
            return false;
          }
        }
        if (!isValid) {
            return false;
        }
        if (wcagValidationResult != null && (lastValidatedContent == null || !lastValidatedContent.equals(getRawValue()))) {
            wcagValidationResult = null;
            ignoreWcagWarnings = false;
            lastValidatedContent = null;
        }

        if (!ignoreWcagWarnings && wcagValidationResult != null && !wcagValidationResult.isEmpty()) {
            showWarnings(wcagValidationResult, false);
            setData("optionalValidation", true);
             return false;
        } else {
            return true;
        }
    }

    @Override
    public void clear() {
        if (!readOnly) {
            ckeditor.clear();
        }
        super.clear();
    }

    @Override
    public boolean isDirty() {
        return readOnly ? false : ckeditor.isDirty();
    }

    @Override
    public String getRawValue() {
        return readOnly ? html.getHtml() : ckeditor.getData();
    }

    @Override
    public void setRawValue(String html) {
        if (readOnly) {
            this.html.setHtml(html);
        } else {
            ckeditor.setData(html);
        }

        super.setRawValue(html);
    }

    public void setWcagValidationResult(WCAGValidationResult wcagValidationResult) {
        this.wcagValidationResult = wcagValidationResult;
        lastValidatedContent = getRawValue();
    }

    protected void showWarnings(WCAGValidationResult wcagResult, final boolean userTriggered) {

        LayoutContainer parent = (LayoutContainer) getParent();
        parent.addStyleName(invalidStyle);
        if (wcagPanel != null) {
            parent.remove(wcagPanel);
        }
        wcagPanel = new ContentPanel(new FitLayout());
        wcagPanel.setHeadingHtml(Messages.getWithArgs("label.wcag.report.title", "WCAG Compliance ({0} errors / {1} warnings / {2} infos)", new String[] {String.valueOf(wcagResult.getErrors().size()), String.valueOf(wcagResult.getWarnings().size()), String.valueOf(wcagResult.getInfos().size())}));
        wcagPanel.getHeader().setIcon(GXT.IMAGES.field_invalid());
        final CheckMenuItem ignore = new CheckMenuItem(userTriggered ? Messages.get("label.close", "Close") : Messages.get("label.wcag.ignore", "Ignore errors"));
        ignore.addListener(Events.OnClick, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                ignore.setChecked(true, true);
                ignoreWcagWarnings = !userTriggered;
                wcagPanel.el().fadeToggle(FxConfig.NONE);
                el().getParent().removeStyleName(invalidStyle);
            }
        });
        wcagPanel.getHeader().addTool(ignore);
        wcagPanel.setBorders(true);
        wcagPanel.add(getWarningGrid(wcagResult));
        wcagPanel.addStyleName("wcag-panel");
        parent.insert(wcagPanel, parent.indexOf(this));
        parent.layout();
    }

    private static Widget getWarningGrid(WCAGValidationResult wcagResult) {
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        RowNumberer rowNumberer = new RowNumberer();
        configs.add(rowNumberer);

        ColumnConfig column = new ColumnConfig();
        column.setId("type");
        column.setAlignment(HorizontalAlignment.CENTER);
        column.setWidth(40);
        column.setRenderer(new GridCellRenderer<WCAGViolation>() {
            public Object render(WCAGViolation model, String property, ColumnData config,
                                 int rowIndex, int colIndex, ListStore<WCAGViolation> store,
                                 Grid<WCAGViolation> grid) {
                Html html = null;
                if ("warning".equalsIgnoreCase(model.getType())) {
                    html = new Html(StandardIconsProvider.STANDARD_ICONS.warning().getHTML());
                    html.setToolTip(Messages.get("label.warning", "Warning"));
                } else if ("information".equalsIgnoreCase(model.getType())) {
                    html = new Html(StandardIconsProvider.STANDARD_ICONS.information().getHTML());
                    html.setToolTip(Messages.get("label.information", "Information"));
                } else {
                    html = new Html(StandardIconsProvider.STANDARD_ICONS.error().getHTML());
                    html.setToolTip(Messages.get("label.error", "Error"));
                }

                return html;
            }
        });
        configs.add(column);

        column = new ColumnConfig();
        column.setId("line");
        column.setHeaderHtml(Messages.get("label.line", "Line"));
        column.setWidth(50);
        configs.add(column);

        column = new ColumnConfig();
        column.setId("column");
        column.setHeaderHtml(Messages.get("label.column", "Column"));
        column.setWidth(50);
        configs.add(column);

        column = new ColumnConfig();
        column.setId("message");
        column.setHeaderHtml(Messages.get("label.description", "Description"));
        column.setRenderer(new GridCellRenderer<WCAGViolation>() {

            @Override
            public Object render(WCAGViolation model, String property, ColumnData config,
                                 int rowIndex, int colIndex, ListStore<WCAGViolation> store,
                                 Grid<WCAGViolation> grid) {
                Html txt = new Html(model.getMessage());
                txt.setToolTip(model.getMessage());

                return txt;
            }
        });
        configs.add(column);

        column = new ColumnConfig();
        column.setId("context");
        column.setHeaderHtml(Messages.get("label.context", "Context"));
        column.setWidth(60);
        column.setAlignment(HorizontalAlignment.CENTER);
        column.setRenderer(new GridCellRenderer<WCAGViolation>() {

            @Override
            public Object render(WCAGViolation model, String property, ColumnData config,
                                 int rowIndex, int colIndex, ListStore<WCAGViolation> store,
                                 Grid<WCAGViolation> grid) {
                if (model.getContext() == null || model.getContext().length() == 0) {
                    return "";
                }

                ToolTipConfig tt = new ToolTipConfig();
                tt.setTitle(Messages.get("label.context", "Context"));
                tt.setTemplate(new Template(model.getContext()));
                Html icon = new Html(StandardIconsProvider.STANDARD_ICONS.about().getHTML());
                icon.setToolTip(tt);

                return icon;
            }
        });
        configs.add(column);

        column = new ColumnConfig();
        column.setId("code");
        column.setHeaderHtml(Messages.get("label.code", "Code"));
        column.setWidth(60);
        column.setAlignment(HorizontalAlignment.CENTER);
        column.setRenderer(new GridCellRenderer<WCAGViolation>() {

            @Override
            public Object render(WCAGViolation model, String property, ColumnData config,
                                 int rowIndex, int colIndex, ListStore<WCAGViolation> store,
                                 Grid<WCAGViolation> grid) {
                if (model.getCode() == null || model.getCode().length() == 0) {
                    return "";
                }

                ToolTipConfig tt = new ToolTipConfig();
                tt.setTitle(Messages.get("label.code", "Code"));
                tt.setTemplate(new Template(model.getCode()));
                Html icon = new Html(StandardIconsProvider.STANDARD_ICONS.about().getHTML());
                icon.setToolTip(tt);

                return icon;
            }
        });
        configs.add(column);

        column = new ColumnConfig();
        column.setId("example");
        column.setHeaderHtml(Messages.get("label.example", "Example"));
        column.setAlignment(HorizontalAlignment.CENTER);
        column.setWidth(80);
        configs.add(column);

        ListStore<WCAGViolation> store = new ListStore<WCAGViolation>();
        store.add(wcagResult.getErrors());
        store.add(wcagResult.getWarnings());
        store.add(wcagResult.getInfos());

        final Grid<WCAGViolation> grid = new Grid<WCAGViolation>(store, new ColumnModel(configs));
        grid.setHeight(store.getCount() > 3 ? 100 : (35 + 20 * store.getCount()));
        grid.setStyleAttribute("borderTop", "none");
        grid.setAutoExpandColumn("message");
        grid.setAutoExpandMax(900);
        grid.setBorders(false);
        grid.setStripeRows(true);
        grid.setColumnLines(true);
        grid.setColumnReordering(true);
        grid.addPlugin(rowNumberer);
        grid.addStyleName("wcag-warning-grid");
        return grid;
    }

    public boolean isIgnoreWcagWarnings() {
        return ignoreWcagWarnings;
    }

    public void checkWCAGCompliance() {
        if (wcagPanel != null) {
            el().getParent().removeStyleName(invalidStyle);
            ((LayoutContainer) getParent()).remove(wcagPanel);
            wcagPanel = null;
        }

        String text = ckeditor.getData();
        if (text == null || text.length() == 0) {
            MessageBox.info(Messages.get("label.information", "Information"), Messages.getWithArgs(
                    "label.wcag.report.title",
                    "WCAG Compliance ({0} errors / {1} warnings / {2} infos)", new String[] {
                    String.valueOf(0), String.valueOf(0), String.valueOf(0) }), null);
            return;
        }
        final Map<String, String> toValidate = new HashMap<String, String>(1);
        toValidate.put("text", text);
        JahiaContentManagementService.App.getInstance().validateWCAG(toValidate,
                new BaseAsyncCallback<Map<String, WCAGValidationResult>>() {

                    @Override
                    public void onSuccess(Map<String, WCAGValidationResult> result) {
                        WCAGValidationResult validationResult = result.get("text");
                        if (validationResult.isEmpty()) {
                            MessageBox.info(
                                    Messages.get("label.information", "Information"),
                                    Messages.getWithArgs(
                                            "label.wcag.report.title",
                                            "WCAG Compliance ({0} errors / {1} warnings / {2} infos)",
                                            new String[] { String.valueOf(0), String.valueOf(0),
                                                    String.valueOf(0) }), null);
                        } else {
                            showWarnings(validationResult, true);
                        }
                    }

                    @Override
                    public void onApplicationFailure(Throwable caught) {
                        super.onApplicationFailure(caught);
                        // unable to do WCAG check, skipping
                    }
                });
        return;
    }

    @Override
    protected void onUnload() {
        instances.remove(ckeditor.getInstanceId());
        super.onUnload();
    }

    public static CKEditorField getInstance(String editorInstanceId) {
        return instances.get(editorInstanceId);
    }

    @Override
    public void focus() {
        ckeditor.focus();
    }
}
