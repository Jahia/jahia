/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.client.data.wcag.WCAGValidationResult;
import org.jahia.ajax.gwt.client.data.wcag.WCAGViolation;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.ckeditor.CKEditor;
import org.jahia.ajax.gwt.client.widget.ckeditor.CKEditorConfig;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.RowNumberer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.tips.QuickTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;

/**
 * User: ktlili
 * Date: Nov 25, 2009
 * Time: 12:51:25 PM
 * <p/>
 * Code inspired from AdapterField. Update this class if GXT version is updated
 */
public class CKEditorField extends Field<String> {

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

	/**
     * Creates a new adapter field.
     */
    public CKEditorField() {
        this(null);
    }

    public CKEditorField(CKEditorConfig config) {
    	super();
        ckeditor = new CKEditor(config);
        html = new Html();
//        panel = new ContentPanel(new FitLayout());
//        panel.setHeaderVisible(false);
//        panel.setBorders(true);
//        panel.add(new Label("kj alskfh salkf sakf hdakjfhs sadlkjhf"));
//        panel.add(ckeditor);
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
    public boolean isValid(boolean silent) {
        return true;
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

    @Override
    protected void onBlur(ComponentEvent ce) {

    }

    @Override
    protected void onDetach() {
        try {
            onUnload();
        } finally {
            ComponentHelper.doDetach(getComponent());
        }
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
    protected void onFocus(ComponentEvent ce) {

    }

    @Override
    protected void onRender(Element target, int index) {
        final Component component = getComponent();
        if (!component.isRendered()) {
            component.render(target, index);
        }
        setElement(component.getElement(), target, index);
    }


    @Override
    protected boolean validateValue(String value) {
    	boolean isValid = super.validateValue(value);
    	if (!isValid) {
    		return false;
    	}
    	if (wcagValidationResult != null && (lastValidatedContent == null || !lastValidatedContent.equals(getRawValue()))) {
    		wcagValidationResult = null;
    		ignoreWcagWarnings = false;
    		lastValidatedContent = null;
    	}
    	
    	if (!ignoreWcagWarnings && wcagValidationResult != null && !wcagValidationResult.isEmpty()) {
    		showWarnings(wcagValidationResult);
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

	public void showWarnings(WCAGValidationResult wcagResult) {
		FieldSet parent = (FieldSet) getParent();
		el().getParent().addStyleName(invalidStyle);
		if (wcagPanel != null) {
			parent.remove(wcagPanel);
		}
		wcagPanel = new ContentPanel(new FitLayout());
		wcagPanel.setHeading(Messages.getWithArgs("label.wcag.report.title", "WCAG Compliance ({0} errors / {1} warnings / {2} infos)", new String[] {String.valueOf(wcagResult.getErrors().size()), String.valueOf(wcagResult.getWarnings().size()), String.valueOf(wcagResult.getInfos().size())}));
		wcagPanel.getHeader().setIcon(GXT.IMAGES.field_invalid());
		final CheckMenuItem ignore = new CheckMenuItem(Messages.get("label.wcag.ignore", "Ignore all warnings"));
		ignore.addListener(Events.OnClick, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				ignore.setChecked(true, true);
				ignoreWcagWarnings = true;
				wcagPanel.el().fadeToggle(FxConfig.NONE);
				el().getParent().removeStyleName(invalidStyle);
            }
		});
		wcagPanel.getHeader().addTool(ignore);
		wcagPanel.setBorders(true);
		wcagPanel.add(getWarningGrid(wcagResult));
		
		parent.insert(wcagPanel, parent.indexOf(this));
		parent.layout();
	}

	private Widget getWarningGrid(WCAGValidationResult wcagResult) {
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
				} else if ("info".equalsIgnoreCase(model.getType())) {
					html = new Html(StandardIconsProvider.STANDARD_ICONS.about().getHTML());
					html.setToolTip(Messages.get("label.information", "Error"));
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
		column.setHeader(Messages.get("label.line", "Line"));
		column.setWidth(50);
		configs.add(column);

		column = new ColumnConfig();
		column.setId("column");
		column.setHeader(Messages.get("label.column", "Column"));
		column.setWidth(50);
		configs.add(column);

		column = new ColumnConfig();
		column.setId("message");
		column.setHeader(Messages.get("label.description", "Description"));
		column.setRenderer(new GridCellRenderer<WCAGViolation>() {
			public Object render(WCAGViolation model, String property, ColumnData config,
                    int rowIndex, int colIndex, ListStore<WCAGViolation> store,
                    Grid<WCAGViolation> grid) {
				Text txt = new Text(model.getMessage());
				txt.setToolTip(model.getMessage());
				
	            return txt;
            }
		});
		configs.add(column);

		column = new ColumnConfig();
		column.setId("context");
		column.setHeader(Messages.get("label.context", "Context"));
		column.setWidth(60);
		column.setAlignment(HorizontalAlignment.CENTER);
		column.setRenderer(new GridCellRenderer<WCAGViolation>() {
			public Object render(WCAGViolation model, String property, ColumnData config,
                    int rowIndex, int colIndex, ListStore<WCAGViolation> store,
                    Grid<WCAGViolation> grid) {
				if (model.getContext() == null || model.getContext().length() == 0) {
					return "";
				}

				ToolTipConfig tt = new ToolTipConfig();
				tt.setTitle(Messages.get("label.context", "Context"));
				tt.setTemplate(new Template(model.getContext()));
				Html icon = new Html(StandardIconsProvider.STANDARD_ICONS.information().getHTML());
				icon.setToolTip(tt);
				
	            return icon;
            }
		});
		configs.add(column);

		column = new ColumnConfig();
		column.setId("example");
		column.setHeader(Messages.get("label.example", "Example"));
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
		
		return grid;
	}

	public boolean isIgnoreWcagWarnings() {
    	return ignoreWcagWarnings;
    }
}