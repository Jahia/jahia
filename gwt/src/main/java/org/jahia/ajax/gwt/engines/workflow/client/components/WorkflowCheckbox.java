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

package org.jahia.ajax.gwt.engines.workflow.client.components;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.Events;
import com.allen_sauer.gwt.log.client.Log;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 8 ao�t 2008 - 16:48:53
 */
public class WorkflowCheckbox extends HorizontalPanel {

    private CheckBox checkbox ;

    public WorkflowCheckbox(String extendedWorkflowState,
                            final String key,
                            final String title,
                            final String lang,
                            final Set<String> actions,
                            final WorkflowToolbar toolbar,
                            final Map<String, String> titleForObjectKey,
                            final Map<String, Set<String>> checked) {
        super() ;
        checkbox = new CheckBox() ;
        checkbox.addStyleName("checkbox-space");
        checkbox.addListener(Events.Change, new Listener() {
            public void handleEvent(BaseEvent event) {
                if (checkbox.getValue()) {
                    if (!checked.containsKey(key)) {
                        Log.debug("add "+key);
                        Set<String> l = new HashSet<String>();
                        checked.put(key,l);
                    }
                    checked.get(key).add(lang);
                    titleForObjectKey.put(key, title) ;
                } else {
                    if (checked.containsKey(key)) {
                        Log.debug("remove "+key ) ;
                        checked.get(key).remove(lang);
                        if (checked.get(key).size() == 0) {
                            checked.remove(key) ;
                        }
                    }
                }
            }

        });

        HTML wfIcon = new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;") ;
        wfIcon.setStyleName("workflow-" + extendedWorkflowState);

        wfIcon.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                if (actions != null && !actions.isEmpty()) {
                    toolbar.cycleAction(actions);
                }
            }
        });

        add(wfIcon) ;
        add(checkbox) ;
        addStyleName("workflow-checkbox") ;
        /*HTML valid = new HTML("&nbsp;&nbsp;&nbsp;") ;
        valid.setStyleName("validation-s-" + String.valueOf(validationState));
        add(valid) ;*/
    }

    public boolean isChecked() {
        return checkbox.getValue().booleanValue() ;
    }

    public void setChecked(boolean checked) {
        checkbox.setValue(Boolean.valueOf(checked));
        checkbox.fireEvent(Events.Change) ;
    }

    public void setChecked(boolean checked, boolean fireEvent) {
        checkbox.setValue(Boolean.valueOf(checked));
        if (fireEvent) {
            checkbox.fireEvent(Events.Change) ;
        }
    }

    public boolean isEnabled() {
        return checkbox.isEnabled() ;
    }

    public void setEnabled(boolean state) {
        checkbox.setEnabled(state);
    }

}
