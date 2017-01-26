/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.DomEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.PreviewEvent;
import com.extjs.gxt.ui.client.util.BaseEventPreview;
import com.extjs.gxt.ui.client.util.Size;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

/**
 * Created by kevan inspired by {@link com.extjs.gxt.ui.client.widget.form.TriggerField}
 *
 * Provides a convenient wrapper for NumberFields that adds a clickable trigger
 * button (looks like a combobox by default).
 *
 * <dl>
 * <dt><b>Events:</b></dt>
 *
 * <dd><b>TriggerClick</b> : FieldEvent(field, event)<br>
 * <div>Fires after the trigger is clicked.</div>
 * <ul>
 * <li>field : this</li>
 * <li>event : event</li>
 * </ul>
 * </dd>
 * </dl>
 *
 * <dl>
 * <dt>Inherited Events:</dt>
 * <dd>Field Focus</dd>
 * <dd>Field Blur</dd>
 * <dd>Field Change</dd>
 * <dd>Field Invalid</dd>
 * <dd>Field Valid</dd>
 * <dd>Field KeyPress</dd>
 * <dd>Field SpecialKey</dd>
 * </dl>
 */
@SuppressWarnings("deprecation")
class NumberTriggerField extends NumberField {

    private BaseEventPreview focusEventPreview;
    protected El trigger;
    private String triggerStyle = "x-form-trigger-arrow";
    private boolean mimicing;

    private boolean editable = true;
    private boolean monitorTab = true;
    private boolean hideTrigger;

    public NumberTriggerField() {
        super();
    }

    /**
     * Returns the trigger style.
     *
     * @return the trigger style
     */
    public String getTriggerStyle() {
        return triggerStyle;
    }

    /**
     * Returns true if the combo is editable.
     *
     * @return true if editable
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Returns true if the trigger is hidden.
     *
     * @return the hide trigger state
     */
    public boolean isHideTrigger() {
        return hideTrigger;
    }

    /**
     * Returns true if tab key events are being monitored.
     *
     * @return true if monitoring
     */
    public boolean isMonitorTab() {
        return monitorTab;
    }

    /**
     * Allow or prevent the user from directly editing the field text. If false is
     * passed, the user will only be able to select from the items defined in the
     * dropdown list.
     *
     * @param editable true to allow the user to directly edit the field text
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
        if (rendered) {
            El fromEl = getInputEl();
            if (!readOnly) {
                fromEl.dom.setPropertyBoolean("readOnly", !editable);
            }
            fromEl.setStyleName("x-triggerfield-noedit", !editable);
            if (GXT.isAriaEnabled()) {
                fromEl.dom.setAttribute("aria-readonly", editable ? "false" : "true");
            }
        }
    }

    /**
     * True to hide the trigger (defaults to false, pre-render).
     *
     * @param hideTrigger true to hide the trigger
     */
    public void setHideTrigger(boolean hideTrigger) {
        this.hideTrigger = hideTrigger;
    }

    /**
     * True to monitor tab key events to force the bluring of the field (defaults
     * to true).
     *
     * @param monitorTab true to monitor tab key events
     */
    public void setMonitorTab(boolean monitorTab) {
        this.monitorTab = monitorTab;
    }

    /**
     * Sets the trigger style name.
     *
     * @param triggerStyle
     */
    public void setTriggerStyle(String triggerStyle) {
        this.triggerStyle = triggerStyle;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        if (rendered) {
            el().setStyleName(readOnlyFieldStyle, readOnly);
            if (editable || (readOnly && !editable)) {
                getInputEl().dom.setPropertyBoolean("readOnly", readOnly);
            }
        }
    }

    @Override
    protected Size adjustInputSize() {
        return new Size(hideTrigger ? 0 : trigger.getStyleSize().width, 0);
    }

    @Override
    protected void afterRender() {
        super.afterRender();
        addStyleOnOver(trigger.dom, "x-form-trigger-over");
        removeStyleName(fieldStyle);
    }

    protected void beforeBlur() {
    }

    protected void mimicBlur(PreviewEvent e, Element target) {
        if (!el().dom.isOrHasChild(target) && validateBlur(e, target)) {
            triggerBlur(null);
        }
    }

    @Override
    protected void onKeyDown(FieldEvent fe) {
        super.onKeyDown(fe);
        if (monitorTab && fe.getKeyCode() == KeyCodes.KEY_TAB) {
            triggerBlur(fe);
        }
    }

    @Override
    protected void onBlur(ComponentEvent ce) {
    }

    @Override
    protected void onClick(ComponentEvent ce) {
        if (!readOnly
                && ((!editable && getInputEl().dom.isOrHasChild(ce.getTarget())) || (trigger.dom.isOrHasChild(ce.getTarget())))) {
            onTriggerClick(ce);
        }
        super.onClick(ce);
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        addStyleName("x-item-disabled");
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        removeStyleName("x-item-disabled");
    }

    @Override
    protected void onFocus(ComponentEvent ce) {
        super.onFocus(ce);
        if (!mimicing) {
            addStyleName("x-trigger-wrap-focus");
            mimicing = true;
            focusEventPreview.add();
        }
    }

    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);

        focusEventPreview = new BaseEventPreview() {
            protected boolean onAutoHide(final PreviewEvent ce) {
                if (ce.getEventTypeInt() == Event.ONMOUSEDOWN) {
                    mimicBlur(ce, ce.getTarget());
                }
                return false;
            }
        };

        addStyleName("x-form-field-wrap");

        input.addStyleName(fieldStyle);

        trigger = new El(GXT.isHighContrastMode ? DOM.createDiv() : DOM.createImg());
        trigger.dom.setClassName("x-form-trigger " + triggerStyle);
        trigger.dom.setPropertyString("src", GXT.BLANK_IMAGE_URL);
        if (GXT.isAriaEnabled()) {
            trigger.dom.setPropertyString("alt", "Dropdown");
        }

        el().appendChild(input.dom);
        el().appendChild(trigger.dom);

        if (hideTrigger) {
            trigger.setVisible(false);
        }

        if (!editable) {
            setEditable(false);
        }
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
        if ((GXT.isIE6 || GXT.isIE7) && !hideTrigger) {
            int y;
            if ((y = input.getY()) != trigger.getY()) {
                trigger.setY(y);
            }
        }
    }

    protected void onTriggerClick(ComponentEvent ce) {
        fireEvent(Events.TriggerClick, ce);
    }

    protected void triggerBlur(ComponentEvent ce) {
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                getFocusEl().blur();
            }
        });
        mimicing = false;
        focusEventPreview.remove();
        beforeBlur();
        removeStyleName("x-trigger-wrap-focus");
        super.onBlur(ce);
    }

    protected boolean validateBlur(DomEvent ce, Element target) {
        return true;
    }

}

