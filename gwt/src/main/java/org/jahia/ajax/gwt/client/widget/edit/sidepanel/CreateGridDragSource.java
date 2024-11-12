/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;
import org.jahia.ajax.gwt.client.widget.edit.EditModeGridDragSource;

/**
 *
 * User: toto
 * Date: Aug 21, 2009
 * Time: 4:22:02 PM
 *
 */
public class CreateGridDragSource extends EditModeGridDragSource {
    public CreateGridDragSource(Grid<GWTJahiaNodeType> grid) {
        super(grid);
    }

    @Override
    protected void onDragStart(DNDEvent e) {
        Object nodeType = grid.getSelectionModel().getSelectedItem();
        if (nodeType != null && !((GWTJahiaNodeType) nodeType).isMixin()) {
            e.setCancelled(false);

            e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.CREATE_CONTENT_SOURCE_TYPE);
            e.getStatus().setData(EditModeDNDListener.SOURCE_NODETYPE, nodeType);
            e.setOperation(DND.Operation.COPY);

            super.onDragStart(e);
        } else {
            e.setCancelled(true);
        }
    }
}
