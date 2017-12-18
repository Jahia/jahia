/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
import com.extjs.gxt.ui.client.event.PreviewEvent;
import com.extjs.gxt.ui.client.util.BaseEventPreview;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.google.gwt.user.client.DOM;
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
class NumberTriggerField extends NumberField {

    public NumberTriggerField() {
        super();
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

        if (isHideTrigger()) {
            trigger.setVisible(false);
        }

        if (!isEditable()) {
            setEditable(false);
        }
    }
}

