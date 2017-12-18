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
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * Button Item for create and new
 */
public class WorkInProgressButtonItem implements ButtonItem {

    private static final long serialVersionUID = -648440265273432614L;

    private boolean checkedByDefault = false;

    @Override
    public BoxComponent create(final AbstractContentEngine engine) {

        final CheckboxWorkInProgress checkbox = new CheckboxWorkInProgress();
        engine.setWorkInProgressCheckedByDefault(checkedByDefault);
        checkbox.addListener(Events.Change, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                engine.setWorkInProgress(checkbox.getValue());
            }
        });
        checkbox.setBoxLabel(Messages.get("label.saveAsWIP", "Save as work in progress"));
        checkbox.setToolTip(Messages.get("label.saveAsWIP.information", "If checked, this content will ne be part of publication process"));
        return checkbox;
    }

    public void setCheckedByDefault(boolean checkedByDefault) {
        this.checkedByDefault = checkedByDefault;
    }
}
